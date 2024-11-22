package com.example.inventory.ui.item

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.inventory.InventoryTopAppBar
import com.example.inventory.R
import com.example.inventory.ui.AppViewModelProvider
import com.example.inventory.ui.theme.InventoryTheme
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import android.Manifest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEntryScreen(
    navigateBack: () -> Unit,
    viewModel: NoteEntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val noteUiState by viewModel.noteUiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    val selectedDate =
        remember { mutableStateOf("") }  // Declara la variable para la fecha seleccionada

    var showTimePicker by remember { mutableStateOf(false) }  // Nueva variable para mostrar el reloj
    val selectedTime = remember { mutableStateOf("") }  // Hora seleccionada

    var mediaFiles by remember { mutableStateOf(listOf<File>()) }
    var showPhotoDialog by remember { mutableStateOf(false) }
    var showVideoDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Creamos el archivo temporal para fotos

    val temPhotoFile = File(
        context.cacheDir,
        "temp_photo_${System.currentTimeMillis()}.jpg"
    )

    val tempPhotoUri = try {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            temPhotoFile
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }


    //Lanzadores para fotos
    val photoCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { isSaved: Boolean ->
        if (isSaved) {
            if (temPhotoFile.exists()) {
                mediaFiles = mediaFiles + temPhotoFile
            } else {
                Toast.makeText(context, "Error al guardar la foto", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Captura de foto cancelada", Toast.LENGTH_SHORT).show()
        }
    }
    fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val tempFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(tempFile)
            inputStream?.copyTo(outputStream)
            outputStream.close()
            inputStream?.close()
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    val photoGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val file = uriToFile(context, it) // Usa uriToFile
            file?.let { mediaFiles = mediaFiles + it }
        }
    }


    // Manejar permisos en tiempo de ejecución
    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            if (tempPhotoUri != null) {
                photoCameraLauncher.launch(tempPhotoUri)
            } else {
                Toast.makeText(context, "Error al crear archivo temporal", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }
    // Manejar el diálogo de fotos
    if (showPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoDialog = false },
            title = { Text("Agregar Foto") },
            text = { Text("Elige una opción:") },
            confirmButton = {
                TextButton(onClick = {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    } else {
                        if (tempPhotoUri != null) {
                            photoCameraLauncher.launch(tempPhotoUri)
                        } else {
                            Toast.makeText(
                                context,
                                "Error al crear archivo temporal",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    showPhotoDialog = false
                }) {
                    Text("Tomar Foto")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    photoGalleryLauncher.launch("image/*")
                    showPhotoDialog = false
                }) {
                    Text("Subir Foto")
                }
            }
        )
    }


        Scaffold(
            topBar = {
                InventoryTopAppBar(
                    title = stringResource(R.string.note_entry_title),
                    canNavigateBack = true,
                    navigateUp = navigateBack
                )
            },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = noteUiState.noteDetails?.title ?: "", // Manejo de nulos
                        onValueChange = { newTitle: String -> viewModel.updateTitle(newTitle) },
                        label = { Text("Title") },
                        modifier = Modifier.padding(16.dp)
                    )
                    OutlinedTextField(
                        value = noteUiState.noteDetails?.content ?: "", // Manejo de nulos
                        onValueChange = { newContent: String -> viewModel.updateContent(newContent) },
                        label = { Text("Content") },
                        modifier = Modifier.padding(16.dp)
                    )
                    // Agregar botón de calendario
                    IconButton(
                        onClick = {
                            // Acción para abrir el calendario
                            showDatePicker = true
                        },
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,  // Ícono de calendario
                            contentDescription = "Abrir calendario",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    // Mostrar la fecha seleccionada
                    Text(
                        text = "Fecha seleccionada: ${selectedDate.value}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 16.dp)
                    )
// Agregar botón de reloj
                    IconButton(
                        onClick = {
                            // Acción para abrir el reloj
                            showTimePicker = true
                        },
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,  // Ícono de reloj
                            contentDescription = "Abrir reloj",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    // Mostrar la hora seleccionada
                    Text(
                        text = "Hora seleccionada: ${selectedTime.value}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(onClick = { showPhotoDialog = true }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_foto),
                                contentDescription = "Agregar Foto"
                            )
                        }
                        IconButton(onClick = { showVideoDialog = true }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_video),
                                contentDescription = "Agregar Video"
                            )
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.saveNote()
                            navigateBack()
                        },
                        enabled = noteUiState.noteDetails?.isEntryValid() == true, // Validar entrada
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Save Note")
                    }
                }
            }
        )

        // Mostrar el DatePickerDialog cuando showDatePicker es verdadero
        if (showDatePicker) {
            val context = LocalContext.current
            val currentDate = java.util.Calendar.getInstance()
            val year = currentDate.get(java.util.Calendar.YEAR)
            val month = currentDate.get(java.util.Calendar.MONTH)
            val day = currentDate.get(java.util.Calendar.DAY_OF_MONTH)

            DatePickerDialog(
                context,
                { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                    val formattedDate = "$selectedDayOfMonth/${selectedMonth + 1}/$selectedYear"

                    // Actualizar la variable selectedDate
                    selectedDate.value = formattedDate

                    // Mostrar un mensaje con la fecha seleccionada
                    Toast.makeText(
                        context,
                        "Fecha seleccionada: $formattedDate",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Cerrar el diálogo
                    showDatePicker = false
                },
                year,
                month,
                day
            ).show()
        }
        // Mostrar el TimePickerDialog cuando showTimePicker es verdadero
        if (showTimePicker) {
            val context = LocalContext.current
            val currentTime = java.util.Calendar.getInstance()
            val hour = currentTime.get(java.util.Calendar.HOUR_OF_DAY)
            val minute = currentTime.get(java.util.Calendar.MINUTE)

            TimePickerDialog(
                context,
                { _, selectedHour, selectedMinute ->
                    val formattedTime = "$selectedHour:${String.format("%02d", selectedMinute)}"

                    // Actualizar la variable selectedTime
                    selectedTime.value = formattedTime

                    // Mostrar un mensaje con la hora seleccionada
                    Toast.makeText(context, "Hora seleccionada: $formattedTime", Toast.LENGTH_SHORT)
                        .show()

                    // Cerrar el diálogo
                    showTimePicker = false
                },
                hour,
                minute,
                true // 24 horas
            ).show()
        }
    }
