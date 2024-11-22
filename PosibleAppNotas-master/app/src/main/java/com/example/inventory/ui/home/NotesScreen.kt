// File: NotesScreen.kt
package com.example.inventory.ui.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventory.R
import com.example.inventory.data.Note
import com.example.inventory.ui.AppViewModelProvider
import com.example.inventory.ui.navigation.NavigationDestination
import java.text.SimpleDateFormat
import java.util.*

object NotesDestination : NavigationDestination {
    override val route = "notes"
    override val titleRes = R.string.app_name
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    navigateToNoteEntry: () -> Unit,
    navigateToNoteDetail: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val notesUiState by viewModel.notesUiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(NotesDestination.titleRes)) },
                actions = {
                    IconButton(onClick = navigateToNoteEntry) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_note))
                    }
                }
            )
        }
    ) { padding ->
        NotesList(
            notes = notesUiState.notes,
            onNoteClick = navigateToNoteDetail,
            modifier = modifier.padding(padding)
        )
    }
}

@Composable
private fun NotesList(
    notes: List<Note>,
    onNoteClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        items(notes, key = { it.id }) { note ->
            NoteItem(note = note, onClick = { onNoteClick(note.id) })
        }
    }
}

@Composable
private fun NoteItem(
    note: Note,
    onClick: () -> Unit
) {
    // Formateadores de fecha y hora
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = note.title, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = note.content, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            // Mostrar la fecha y hora formateadas
            Text(
                text = "Fecha: ${dateFormatter.format(Date(note.fecha))}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Hora: ${timeFormatter.format(Date(note.hora))}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
