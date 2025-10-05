package com.galaxykud.notes.presentation.navigation

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavGraph
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.galaxykud.notes.presentation.screens.creation.CreateNoteScreen
import com.galaxykud.notes.presentation.screens.editing.EditNoteScreen
import com.galaxykud.notes.presentation.screens.notes.NotesScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Notes.route
    ) {
        composable(Screen.Notes.route) {
            NotesScreen(
                onNoteClick = {
                    navController.navigate(Screen.EditNote.createRoute(it.id))
                },
                onAddNoteClick = {
                    navController.navigate(Screen.CreateNote.route)
                }
            )
        }
        composable(Screen.CreateNote.route) {
            CreateNoteScreen(
                onFinished = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.EditNote.route) {
            val noteId = Screen.EditNote.getNoteId(it.arguments)

            EditNoteScreen(
                noteId = noteId,
                onFinished = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
fun CustomNavGraph() {
    val screen = remember {
        mutableStateOf<CustomScreen>(CustomScreen.Notes)
    }

    when(val currentScreen = screen.value) {
        CustomScreen.CreateNote -> {
            CreateNoteScreen(
                onFinished = {
                    screen.value = CustomScreen.Notes
                }
            )
        }
        is CustomScreen.EditNote -> {
            EditNoteScreen(
                noteId = currentScreen.noteId,
                onFinished = {
                    screen.value = CustomScreen.Notes
                }
            )
        }
        CustomScreen.Notes -> {
            NotesScreen(
                onNoteClick = {
                    screen.value = CustomScreen.EditNote(it.id)
                },
                onAddNoteClick = {
                    screen.value = CustomScreen.CreateNote
                }
            )
        }
    }
}

sealed class Screen(val route: String) {

    data object Notes: Screen("notes")

    data object CreateNote: Screen("create_note")

    data object EditNote: Screen("edit_note/{note_id}") {
        fun createRoute(noteId: Int): String {
            return "edit_note/$noteId"
        }

        fun getNoteId(arguments: Bundle?): Int {
            return arguments?.getString("note_id")?.toInt() ?: 0
        }
    }

}

sealed interface CustomScreen {

    data object Notes: CustomScreen

    data object CreateNote: CustomScreen

    data class EditNote(val noteId: Int): CustomScreen

}