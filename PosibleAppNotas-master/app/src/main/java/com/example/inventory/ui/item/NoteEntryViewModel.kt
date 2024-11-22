package com.example.inventory.ui.item

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.Note
import com.example.inventory.data.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoteEntryViewModel(
    private val notesRepository: NotesRepository
) : ViewModel() {

    private val _noteUiState = MutableStateFlow(NoteUiState())
    val noteUiState: StateFlow<NoteUiState> = _noteUiState.asStateFlow()

    fun updateTitle(newTitle: String) {
        val currentNoteDetails = _noteUiState.value.noteDetails ?: NoteDetails() // Proporciona un valor predeterminado
        _noteUiState.value = _noteUiState.value.copy(
            noteDetails = currentNoteDetails.copy(title = newTitle),
            isEntryValid = validateInput(newTitle, currentNoteDetails.content)
        )
    }

    fun updateContent(newContent: String) {
        val currentNoteDetails = _noteUiState.value.noteDetails ?: NoteDetails() // Proporciona un valor predeterminado
        _noteUiState.value = _noteUiState.value.copy(
            noteDetails = currentNoteDetails.copy(content = newContent),
            isEntryValid = validateInput(currentNoteDetails.title, newContent)
        )
    }



    private fun validateInput(title: String, content: String): Boolean {
        return title.isNotBlank() && content.isNotBlank()
    }

    fun saveNote() {
        val noteDetails = _noteUiState.value.noteDetails
        if (_noteUiState.value.isEntryValid && noteDetails != null) {
            viewModelScope.launch {
                notesRepository.insertNote(noteDetails.toNote())
            }
        }
    }
}