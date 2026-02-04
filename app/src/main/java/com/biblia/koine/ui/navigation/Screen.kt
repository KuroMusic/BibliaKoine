package com.biblia.koine.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck

import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Inicio : Screen("inicio", "Inicio", Icons.Default.Home)
    object Biblia : Screen("biblia", "Biblia", Icons.Default.AutoStories)
    object Lexico : Screen("lexico", "Léxico", Icons.Default.MenuBook) {
        fun createRoute(query: String? = null) = if (query != null) "lexico?query=$query" else "lexico"
    }
    object Descubrir : Screen("descubrir", "Descubrir", Icons.Default.Search)
    object Tu : Screen("tu", "Tú", Icons.Default.Person)
}

val items = listOf(
    Screen.Inicio,
    Screen.Biblia,
    Screen.Lexico,
    Screen.Descubrir,
    Screen.Tu
)
