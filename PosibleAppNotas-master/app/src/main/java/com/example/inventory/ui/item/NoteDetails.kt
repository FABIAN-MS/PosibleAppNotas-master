package com.example.inventory.ui.item
import com.example.inventory.data.Note

data class NoteDetails(
    val id: Int = 0,
    val title: String = "",
    val content: String = ""
){
    // Método para verificar si la entrada es válida
    fun isEntryValid(): Boolean {
        return title.isNotBlank() && content.isNotBlank()
    }
}

// Convierte un objeto `NoteDetails` a `Note`.
fun NoteDetails.toNote(): Note = Note(
    id = id,
    title = title,
    content = content
)
// Agrega esto en algún lugar accesible (por ejemplo, en Note.kt o NoteDetails.kt)
fun Note.toNoteDetails(): NoteDetails = NoteDetails(
    id = id,
    title = title,
    content = content
)