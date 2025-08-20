@file:OptIn(ExperimentalCoroutinesApi::class)

package com.galaxykud.notes.presentation.screens.notes

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.galaxykud.notes.data.TestNotesRepositoryImpl
import com.galaxykud.notes.domain.AddNoteUseCase
import com.galaxykud.notes.domain.DeleteNoteUseCase
import com.galaxykud.notes.domain.EditNoteUseCase
import com.galaxykud.notes.domain.GetAllNotesUseCase
import com.galaxykud.notes.domain.GetNoteUseCase
import com.galaxykud.notes.domain.Note
import com.galaxykud.notes.domain.SearchNotesUseCase
import com.galaxykud.notes.domain.SwitchPinnedStatusUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotesViewModel: ViewModel() {

    private val repository = TestNotesRepositoryImpl

    private val addNoteUseCase = AddNoteUseCase(repository)
    private val editNoteUseCase = EditNoteUseCase(repository)
    private val deleteNoteUseCase = DeleteNoteUseCase(repository)
    private val getAllNotesUseCase = GetAllNotesUseCase(repository)
    private val getNoteUseCase = GetNoteUseCase(repository)
    private val searchNotesUseCase = SearchNotesUseCase(repository)
    private val switchPinnedStatusUseCase = SwitchPinnedStatusUseCase(repository)

    private val query = MutableStateFlow("")

    private val _state = MutableStateFlow(NotesScreenState())
    val state = _state.asStateFlow()

    init {
        addSomeNotes()

        query
            .onEach { input ->
                _state.update { it.copy(query = input) }
            }
            .flatMapLatest { input ->
                if (input.isBlank()) {
                    getAllNotesUseCase()
                } else {
                    searchNotesUseCase(input)
                }
            }
            .onEach { notes ->
                val pinnedNotes = notes.filter { it.isPinned }
                val otherNotes = notes.filter { !it.isPinned }
                _state.update { it.copy(pinnedNotes = pinnedNotes, otherNotes = otherNotes) }

            }
            .launchIn(viewModelScope)
    }

    // TODO: don't forget to remove it

    private fun addSomeNotes() {
        viewModelScope.launch {
            repeat(50) {
                addNoteUseCase(title = "Title №$it", content = "Content №$it")
            }
        }
    }

    fun processCommand(command: NotesCommand) {
        viewModelScope.launch {
            when(command) {
                is NotesCommand.DeleteNote -> {
                    deleteNoteUseCase(command.noteId)
                }
                is NotesCommand.EditNote -> {
                    val note = getNoteUseCase(command.note.id)
                    val title = note.title
                    editNoteUseCase(note.copy(title = "$title + edited"))
                }
                is NotesCommand.InputSearchQuery -> {
                    query.update { command.query.trim() }
                }
                is NotesCommand.SwitchPinnedStatus -> {
                    switchPinnedStatusUseCase(command.noteId)
                }
            }
        }
    }
}



sealed interface NotesCommand {

    data class InputSearchQuery(val query: String): NotesCommand

    data class SwitchPinnedStatus(val noteId: Int): NotesCommand

    // Temp

    data class DeleteNote(val noteId: Int): NotesCommand

    data class EditNote(val note: Note): NotesCommand
}

data class NotesScreenState(
    val query: String = "",
    val pinnedNotes: List<Note> = listOf(),
    val otherNotes: List<Note> = listOf()
    )