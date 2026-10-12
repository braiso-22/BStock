package com.braiso22.bstock.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import bstock.shared.generated.resources.Res
import bstock.shared.generated.resources.ic_check_circle
import com.braiso22.bstock.data.Item
import com.braiso22.bstock.data.formatPrice
import com.braiso22.bstock.ui.components.ItemRow
import com.braiso22.bstock.ui.components.LoadingContent
import com.braiso22.bstock.ui.components.MessageContent
import org.jetbrains.compose.resources.painterResource

/**
 * Items con cantidad por debajo del mínimo y lo que costaría reponerlos.
 * Se actualiza solo gracias a la escucha en tiempo real de Firestore.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RestockScreen(
    state: ItemsUiState,
    onItemClick: (Item) -> Unit,
) {
    val success = state as? ItemsUiState.Success
    val hasItems = success?.belowMinimum?.isNotEmpty() == true
    // Barra grande que se encoge al hacer scroll pero nunca desaparece: el total queda siempre a la vista.
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    if (success == null || !hasItems) {
                        Text("Por reponer")
                    } else {
                        Text(formatPrice(success.restockCostCents))
                    }
                },
                subtitle = if (success != null && hasItems) {
                    { Text(restockSubtitle(success)) }
                } else {
                    null
                },
                scrollBehavior = scrollBehavior,
            )
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
                LazyColumn(
                    modifier = modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.belowMinimum, key = { it.id }) { item ->
                        ItemRow(
                            item = item,
                            modifier = Modifier.animateItem(),
                            supportingText = "Tienes ${item.quantity} · Mínimo: ${item.minimum}" +
                                if (item.packSize > 1) " · Pack de ${item.packSize}" else "",
                            onClick = { onItemClick(item) },
                            trailing = {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = buyLabel(item),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.error,
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

/** Explica el número del título. */
private fun restockSubtitle(state: ItemsUiState.Success): String {
    val parts = mutableListOf("Total estimado por reponer")
    if (state.restockWithoutPrice > 0) parts += "${state.restockWithoutPrice} sin precio"
    return parts.joinToString(" · ")
}

/** Qué hay que coger en la tienda: unidades sueltas o packs. */
private fun buyLabel(item: Item): String = when {
    item.packSize == 1 -> "Faltan ${item.missing}"
    item.packsToBuy == 1 -> "1 pack"
    else -> "${item.packsToBuy} packs"
}
