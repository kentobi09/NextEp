package com.animenotifier

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.animenotifier.ui.AnimeViewModel
import com.animenotifier.ui.components.PermissionHandler
import com.animenotifier.ui.screens.HomeScreen
import com.animenotifier.ui.screens.ScheduleScreen
import com.animenotifier.ui.screens.SearchScreen
import com.animenotifier.ui.theme.*

enum class NavScreen(val label: String, val icon: ImageVector) {
    WATCHLIST("Watchlist", Icons.Default.Home),
    SCHEDULE("Schedule", Icons.Default.CalendarMonth),
    SEARCH("Search", Icons.Default.Search)
}

class MainActivity : ComponentActivity() {

    private val viewModel: AnimeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AnimeNotifierTheme {
                PermissionHandler()

                var currentScreen by remember { mutableStateOf(NavScreen.WATCHLIST) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            containerColor = SurfaceRoot,
                            tonalElevation = 0.dp,
                            modifier = Modifier.border(width = 1.dp, color = SurfaceBorder)
                        ) {
                            NavScreen.values().forEach { screen ->
                                val isSelected = (currentScreen == screen)
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { currentScreen = screen },
                                    icon = {
                                        Icon(
                                            imageVector = screen.icon,
                                            contentDescription = screen.label,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = screen.label.uppercase(),
                                            fontSize = 9.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            letterSpacing = 1.sp
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = AccentPrimary,
                                        selectedTextColor = AccentPrimary,
                                        indicatorColor = SurfaceElevated,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = SurfaceRoot
                    ) {
                        when (currentScreen) {
                            NavScreen.WATCHLIST -> HomeScreen(
                                viewModel = viewModel,
                                onNavigateToSearch = { currentScreen = NavScreen.SEARCH }
                            )
                            NavScreen.SCHEDULE -> ScheduleScreen(viewModel = viewModel)
                            NavScreen.SEARCH -> SearchScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
