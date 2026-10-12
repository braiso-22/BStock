package com.braiso22.bstock

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import bstock.shared.generated.resources.Res
import bstock.shared.generated.resources.ic_basket
import bstock.shared.generated.resources.ic_cart
import bstock.shared.generated.resources.ic_home
import com.braiso22.bstock.data.ItemRepository
import com.braiso22.bstock.ui.NoSound
import com.braiso22.bstock.ui.SoundPlayer
import com.braiso22.bstock.ui.detail.ItemDetailScreen
import com.braiso22.bstock.ui.detail.ItemDetailViewModel
import com.braiso22.bstock.ui.form.ItemFormScreen
import com.braiso22.bstock.ui.form.ItemFormViewModel
import com.braiso22.bstock.ui.inventory.InventoryScreen
import com.braiso22.bstock.ui.inventory.InventoryViewModel
import com.braiso22.bstock.ui.inventory.ItemsUiState
import com.braiso22.bstock.ui.inventory.RestockScreen
import com.braiso22.bstock.ui.inventory.ShoppingScreen
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.painterResource

@Serializable
data object InventoryRoute

@Serializable
data object RestockRoute

@Serializable
data object ShoppingRoute

@Serializable
data class DetailRoute(val id: String)

@Serializable
data class FormRoute(val id: String? = null)

@Composable
fun App(repository: ItemRepository, sounds: SoundPlayer = NoSound) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
        val navController = rememberNavController()
        val inventoryViewModel = viewModel { InventoryViewModel(repository) }
        val itemsState by inventoryViewModel.state.collectAsStateWithLifecycle()

        val destination = navController.currentBackStackEntryAsState().value?.destination
        val onInventory = destination?.hasRoute<InventoryRoute>() == true
        val onRestock = destination?.hasRoute<RestockRoute>() == true
        val onShopping = destination?.hasRoute<ShoppingRoute>() == true

        Scaffold(
            contentWindowInsets = WindowInsets(0),
            bottomBar = {
                if (onInventory || onRestock || onShopping) {
                    val belowMinimumCount = (itemsState as? ItemsUiState.Success)?.belowMinimum?.size ?: 0
                    // Las tres etiquetas tienen que caber a lo ancho: con letra muy grande se partían ("Inventar-io").
                    val density = LocalDensity.current
                    CompositionLocalProvider(
                        LocalDensity provides Density(density.density, density.fontScale.coerceAtMost(1.3f)),
                    ) {
                        NavigationBar {
                            NavigationBarItem(
                                selected = onInventory,
                                onClick = { navController.navigateToTab(InventoryRoute) },
                                icon = { Icon(painterResource(Res.drawable.ic_home), contentDescription = null) },
                                label = { Text("Inventario") },
                            )
                            NavigationBarItem(
                                selected = onRestock,
                                onClick = { navController.navigateToTab(RestockRoute) },
                                icon = {
                                    BadgedBox(badge = {
                                        if (belowMinimumCount > 0) Badge { Text(belowMinimumCount.toString()) }
                                    }) {
                                        Icon(painterResource(Res.drawable.ic_cart), contentDescription = null)
                                    }
                                },
                                label = { Text("Por reponer") },
                            )
                            NavigationBarItem(
                                selected = onShopping,
                                onClick = { navController.navigateToTab(ShoppingRoute) },
                                icon = { Icon(painterResource(Res.drawable.ic_basket), contentDescription = null) },
                                label = { Text("Compra") },
                            )
                        }
                    }
                }
            },
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = InventoryRoute,
                modifier = Modifier.padding(padding).consumeWindowInsets(padding),
            ) {
                composable<InventoryRoute> {
                    InventoryScreen(
                        state = itemsState,
                        onItemClick = { navController.navigate(DetailRoute(it.id)) },
                        onAddClick = { navController.navigate(FormRoute()) },
                    )
                }
                composable<RestockRoute> {
                    RestockScreen(
                        state = itemsState,
                        onItemClick = { navController.navigate(DetailRoute(it.id)) },
                    )
                }
                composable<ShoppingRoute> {
                    val shopping by inventoryViewModel.shopping.collectAsStateWithLifecycle()
                    ShoppingScreen(
                        state = itemsState,
                        shopping = shopping,
                        onToggleChecked = {
                            if (it.id !in shopping.checkedIds) sounds.playCheck()
                            inventoryViewModel.toggleChecked(it.id)
                        },
                        onPriceChange = { item, priceCents -> inventoryViewModel.updatePrice(item.id, priceCents) },
                        onPacksChange = inventoryViewModel::setPacksToBuy,
                        onFinishShopping = inventoryViewModel::finishShopping,
                    )
                }
                composable<DetailRoute> { entry ->
                    val route = entry.toRoute<DetailRoute>()
                    ItemDetailScreen(
                        viewModel = viewModel { ItemDetailViewModel(repository, route.id) },
                        onBack = dropUnlessResumed { navController.popBackStack() },
                        onEdit = { navController.navigate(FormRoute(route.id)) },
                    )
                }
                composable<FormRoute> { entry ->
                    val route = entry.toRoute<FormRoute>()
                    ItemFormScreen(
                        viewModel = viewModel { ItemFormViewModel(repository, route.id) },
                        onBack = dropUnlessResumed { navController.popBackStack() },
                    )
                }
            }
        }
    }
}

private fun NavHostController.navigateToTab(route: Any) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
