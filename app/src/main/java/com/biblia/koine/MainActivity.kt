package com.biblia.koine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.biblia.koine.ui.navigation.Screen
import com.biblia.koine.ui.navigation.items
import com.biblia.koine.ui.screens.*
import com.biblia.koine.ui.theme.BibliaKoineTheme
import com.biblia.koine.viewmodel.BibleViewModel
import com.biblia.koine.data.BibleBooksMetadata



import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.work.*
import com.biblia.koine.workers.DailyVerseWorker
import com.biblia.koine.utils.NotificationHelper
import java.util.Calendar
import java.util.concurrent.TimeUnit
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private val bibleViewModel: BibleViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, WorkManager will handle notifications
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Optimizar BD al iniciar (Índices + Vacuum + Analyze)
        lifecycleScope.launch(Dispatchers.IO) {
            bibleViewModel.optimizeDatabase()
        }

        // Setup Notifications and WorkManager
        NotificationHelper.createNotificationChannel(this)
        requestNotificationPermission()
        scheduleDailyVerseWork()

        setContent {
            val prefs by bibleViewModel.userPrefs.collectAsState()
            val darkTheme = when(prefs.theme) {
                "claro" -> false
                "oscuro" -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            
            BibliaKoineTheme(darkTheme = darkTheme) {
                MainContainer(bibleViewModel)
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    private fun scheduleDailyVerseWork() {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        
        calendar.set(Calendar.HOUR_OF_DAY, 8)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        
        if (calendar.timeInMillis <= now) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        val initialDelay = calendar.timeInMillis - now
        
        val workRequest = PeriodicWorkRequestBuilder<DailyVerseWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()
            
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_verse_work",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}

@Composable
fun MainContainer(viewModel: BibleViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            if (currentDestination?.route != Screen.Descubrir.route) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background, // FORCE BLACK in Dark, WHITE in Light
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    tonalElevation = 8.dp
                ) {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    // CRITICAL FIX: Pop up to the start destination of the graph to
                                    // avoid building up a large stack of destinations
                                    // on the back stack as users select items
                                    popUpTo(Screen.Inicio.route) {
                                        inclusive = false
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination when
                                    // reselecting the same item
                                    launchSingleTop = true
                                    // Restore state when reselecting a previously selected item
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary, // Gold
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) // Gold Alpha - NO BLUE
                            )
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        NavHost(
            navController, 
            startDestination = Screen.Inicio.route, 
            Modifier.padding(padding)
        ) {
            composable(Screen.Inicio.route) { 
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToReader = { bookId, chapter ->
                        // Sync ViewModel state BEFORE navigating
                        viewModel.navigateTo(bookId, chapter)
                        navController.navigate(Screen.Biblia.route) {
                            // CRITICAL FIX: Pop up to Home to maintain clean stack
                            popUpTo(Screen.Inicio.route) { 
                                inclusive = false
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                ) 
            }
            composable(Screen.Biblia.route) { 
                BibleScreen(
                    viewModel = viewModel,
                    onNavigateToSettings = {
                        navController.navigate(Screen.Tu.route) {
                            popUpTo(Screen.Inicio.route) { 
                                inclusive = false
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToSearch = {
                        navController.navigate(Screen.Descubrir.route) {
                            popUpTo(Screen.Inicio.route) { 
                                inclusive = false
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToLexicon = { query ->
                        navController.navigate(Screen.Lexico.createRoute(query)) {
                            launchSingleTop = true
                        }
                    }
                ) 
            }
            composable(
                route = "lexico?query={query}",
                arguments = listOf(
                    androidx.navigation.navArgument("query") { 
                        type = androidx.navigation.NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val query = backStackEntry.arguments?.getString("query")
                LexiconScreen(
                    initialQuery = query,
                    onNavigateToWord = { topic ->
                        navController.navigate("word_detail/$topic")
                    }
                )
            }
            
            composable(
                route = "word_detail/{topic}",
                arguments = listOf(
                    androidx.navigation.navArgument("topic") { type = androidx.navigation.NavType.StringType }
                )
            ) { backStackEntry ->
                val topic = backStackEntry.arguments?.getString("topic") ?: ""
                WordDetailScreen(
                    topic = topic,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Descubrir.route) { 
                SearchScreen(
                    viewModel = viewModel,
                    onNavigateToVerse = { result ->
                        // FIX: Use BibleBooksMetadata to convert "GEN" -> 1
                        val bookNum = BibleBooksMetadata.getNumber(result.bookId)
                        val chapter = result.chapter
                        val verse = result.verse
                        
                        android.util.Log.d("SearchNav", "Navigating to reader/$bookNum/$chapter/$verse")
                        
                        navController.navigate("reader/$bookNum/$chapter/$verse") {
                            launchSingleTop = true
                        }
                    }
                ) 
            }
            composable(Screen.Tu.route) { ProfileScreen(viewModel) }
            
            // FIX: Dedicated Reader Route with Arguments
            composable(
                route = "reader/{book}/{chapter}/{verse}",
                arguments = listOf(
                    androidx.navigation.navArgument("book") { type = androidx.navigation.NavType.IntType },
                    androidx.navigation.navArgument("chapter") { type = androidx.navigation.NavType.IntType },
                    androidx.navigation.navArgument("verse") { type = androidx.navigation.NavType.IntType; defaultValue = 1 }
                )
            ) { backStackEntry ->
                val book = backStackEntry.arguments?.getInt("book") ?: 1
                val chapter = backStackEntry.arguments?.getInt("chapter") ?: 1
                val verse = backStackEntry.arguments?.getInt("verse") ?: 1
                
                android.util.Log.d("ReaderNav", "Opening: $book:$chapter:$verse")
                BibleReaderScreen(
                    bookId = book,
                    chapter = chapter,
                    targetVerse = verse,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

