package com.galaxykud.notes.domain

class DeleteNoteUseCase(
    private val repository: NotesRepository
) {

    suspend operator fun invoke(noteId: Int) {
        repository.deleteNote(notedId = noteId)
    }
}