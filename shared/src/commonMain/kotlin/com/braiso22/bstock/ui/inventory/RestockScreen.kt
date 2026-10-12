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
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
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
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RestockScreen(
    state: ItemsUiState,
    shopping: ShoppingUiState,
    onItemClick: (Item) -> Unit,
    onToggleChecked: (Item) -> Unit,
    onStartShopping: () -> Unit,
    onFinishShopping: () -> Unit,
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
                    when {
                        success == null || !hasItems -> Text("Por reponer")
                        shopping.active -> Text(
                            shoppingTitle(
                                cartCents = success.cartCostCents(shopping.checkedIds),
                                totalCents = success.restockCostCents,
                                cartColor = MaterialTheme.colorScheme.primary,
                                totalColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                        else -> Text(formatPrice(success.restockCostCents))
                    }
                },
                subtitle = if (success != null && hasItems) {
                    { Text(restockSubtitle(success, shopping)) }
                } else {
                    null
                },
                scrollBehavior = scrollBehavior,
            )
        },
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
                LazyColumn(
                    modifier = modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val list = if (shopping.active) state.shoppingOrder(shopping.checkedIds) else state.belowMinimum
                    items(list, key = { it.id }) { item ->
                        val checked = shopping.active && item.id in shopping.checkedIds
                        ItemRow(
                            item = item,
                            modifier = Modifier.animateItem(),
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

/** En modo compra: lo que va en el carro en grande y con color, y el total al lado más pequeño ("1,90 € de 3,25 €"). */
private fun shoppingTitle(cartCents: Int, totalCents: Int, cartColor: Color, totalColor: Color): AnnotatedString =
    buildAnnotatedString {
        withStyle(SpanStyle(color = cartColor)) { append(formatPrice(cartCents)) }
        withStyle(SpanStyle(color = totalColor, fontSize = 0.6.em)) { append(" de ${formatPrice(totalCents)}") }
    }

/** Explica el número del título; en modo compra, añade lo que falta por coger. */
private fun restockSubtitle(state: ItemsUiState.Success, shopping: ShoppingUiState): String {
    val parts = mutableListOf<String>()
    if (shopping.active) {
        parts += "En el carro"
        parts += "Falta ${formatPrice(state.restockCostCents - state.cartCostCents(shopping.checkedIds))}"
    } else {
        parts += "Total estimado por reponer"
    }
    if (state.restockWithoutPrice > 0) parts += "${state.restockWithoutPrice} sin precio"
    return parts.joinToString(" · ")
}
