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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bstock.shared.generated.resources.Res
import bstock.shared.generated.resources.ic_check_circle
import com.braiso22.bstock.data.Item
import com.braiso22.bstock.data.formatPrice
import com.braiso22.bstock.ui.components.ItemRow
import com.braiso22.bstock.ui.components.LoadingContent
import com.braiso22.bstock.ui.components.MessageContent
import org.jetbrains.compose.resources.painterResource

/** Items con cantidad por debajo del mínimo. Se actualiza solo gracias a la escucha en tiempo real de Firestore. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestockScreen(
    state: ItemsUiState,
    onItemClick: (Item) -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Por reponer") }) },
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
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item(key = "total") {
                        RestockTotal(state.restockCostCents, state.restockWithoutPrice)
                    }
                    items(state.belowMinimum, key = { it.id }) { item ->
                        ItemRow(
                            item = item,
                            supportingText = "Tienes ${item.quantity} · Mínimo: ${item.minimum}",
                            onClick = { onItemClick(item) },
                            trailing = {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Faltan ${item.missing}",
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

@Composable
private fun RestockTotal(costCents: Int, withoutPrice: Int) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Total estimado", style = MaterialTheme.typography.titleMedium)
            Text(formatPrice(costCents), style = MaterialTheme.typography.headlineMedium)
            if (withoutPrice > 0) {
                Text(
                    text = if (withoutPrice == 1) "1 item sin precio no está incluido" else "$withoutPrice items sin precio no están incluidos",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
