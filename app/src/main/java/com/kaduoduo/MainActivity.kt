package com.kaduoduo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kaduoduo.domain.AddCardViewModel
import com.kaduoduo.domain.CardDetailViewModel
import com.kaduoduo.domain.HomeViewModel
import com.kaduoduo.domain.KaduoduoViewModelFactory
import com.kaduoduo.ui.AddCardScreen
import com.kaduoduo.ui.CardDetailRoute
import com.kaduoduo.ui.HomeRoute
import com.kaduoduo.ui.theme.KaduoduoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = (application as KaduoduoApplication).repository

        setContent {
            KaduoduoTheme {
                KaduoduoApp(
                    factory = KaduoduoViewModelFactory(repository)
                )
            }
        }
    }
}

@Composable
private fun KaduoduoApp(factory: KaduoduoViewModelFactory) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            val homeViewModel: HomeViewModel = viewModel(factory = factory)
            HomeRoute(
                viewModel = homeViewModel,
                onAddClick = { navController.navigate("add") },
                onCardClick = { cardId -> navController.navigate("detail/$cardId") }
            )
        }

        composable("add") {
            val addCardViewModel: AddCardViewModel = viewModel(factory = factory)
            AddCardScreen(
                viewModel = addCardViewModel,
                onBackClick = { navController.popBackStack() },
                onSaved = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "detail/{cardId}",
            arguments = listOf(navArgument("cardId") { type = NavType.LongType })
        ) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getLong("cardId") ?: return@composable
            val detailViewModel: CardDetailViewModel = viewModel(
                key = "detail-$cardId",
                factory = factory.withCardId(cardId)
            )
            CardDetailRoute(
                viewModel = detailViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
