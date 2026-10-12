package com.braiso22.bstock.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bstock.shared.generated.resources.Res
import bstock.shared.generated.resources.ic_cart
import bstock.shared.generated.resources.ic_check_circle
import com.braiso22.bstock.data.Item
import com.braiso22.bstock.data.formatPrice
import com.braiso22.bstock.ui.components.ItemRow
import com.braiso22.bstock.ui.components.LoadingContent
import com.braiso22.bstock.ui.components.MessageContent
import org.jetbrains.compose.resources.painterResource

/**
 * Items con cantidad por debajo del mínimo. Se actualiza solo gracias a la escucha en tiempo real de Firestore.
 * En modo compra, tocar un item lo tacha (o lo destacha) en vez de abrir su detalle.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestockScreen(
    state: ItemsUiState,
    shopping: ShoppingUiState,
    onItemClick: (Item) -> Unit,
    onToggleChecked: (Item) -> Unit,
    onStartShopping: () -> Unit,
    onFinishShopping: () -> Unit,
) {
    val hasItems = (state as? ItemsUiState.Success)?.belowMinimum?.isNotEmpty() == true
    Scaffold(
        topBar = { TopAppBar(title = { Text(if (shopping.active) "Comprando" else "Por reponer") }) },
        floatingActionButton = {
            if (shopping.active) {
                ExtendedFloatingActionButton(
                    onClick = onFinishShopping,
                    icon = { Icon(painterResource(Res.drawable.ic_check_circle), contentDescription = null) },
                    text = { Text("Terminar compra") },
                )
            } else if (hasItems) {
                ExtendedFloatingActionButton(
                    onClick = onStartShopping,
                    icon = { Icon(painterResource(Res.drawable.ic_cart), contentDescription = null) },
                    text = { Text("Empezar compra") },
                )
            }
        },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when (state) {
            ItemsUiState.Loading -> LoadingContent(modifier)
            is ItemsUiState.Error -> MessageContent(state.message, modifier)
            is ItemsUiState.Success -> if (state.belowMinimum.isEmpty()) {
                MessageContent(
                    message = "Todo en orden.\nNingún item está por debajo del mínimo.",
                    modifier = modifier,
                    icon = painterResource(Res.drawable.ic_check_circle),
                    iconTint = MaterialTheme.colorScheme.primary,
                )
            } else {
                Column(modifier.fillMaxSize()) {
                    // Fuera de la lista para que siga visible al hacer scroll.
                    RestockTotal(
                        costCents = state.restockCostCents,
                        withoutPrice = state.restockWithoutPrice,
                        cartCents = if (shopping.active) state.cartCostCents(shopping.checkedIds) else null,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.belowMinimum, key = { it.id }) { item ->
                            val checked = shopping.active && item.id in shopping.checkedIds
                            ItemRow(
                                item = item,
                                supportingText = "Tienes ${item.quantity} · Mínimo: ${item.minimum}",
                                onClick = { if (shopping.active) onToggleChecked(item) else onItemClick(item) },
                                crossedOut = checked,
                                trailing = {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Faltan ${item.missing}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (checked) {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            } else {
                                                MaterialTheme.colorScheme.error
                                            },
                                        )
                                        if (item.priceCents > 0) {
                                            Text(formatPrice(item.restockCostCents), style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

/** [cartCents] solo se pasa en modo compra: lo que ya está en el carro. */
@Composable
private fun RestockTotal(costCents: Int, withoutPrice: Int, cartCents: Int?, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Total estimado", style = MaterialTheme.typography.titleMedium)
            Text(formatPrice(costCents), style = MaterialTheme.typography.headlineMedium)
            if (cartCents != null) {
                Text(
                    text = "En el carro: ${formatPrice(cartCents)} · Falta: ${formatPrice(costCents - cartCents)}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (withoutPrice > 0) {
                Text(
                    text = if (withoutPrice == 1) "1 item sin precio no está incluido" else "$withoutPrice items sin precio no están incluidos",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
