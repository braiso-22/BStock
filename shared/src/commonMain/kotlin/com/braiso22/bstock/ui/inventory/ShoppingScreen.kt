package com.braiso22.bstock.ui.inventory

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import bstock.shared.generated.resources.Res
import bstock.shared.generated.resources.ic_add
import bstock.shared.generated.resources.ic_check_circle
import bstock.shared.generated.resources.ic_remove
import com.braiso22.bstock.data.Item
import com.braiso22.bstock.data.formatPrice
import com.braiso22.bstock.data.parsePrice
import com.braiso22.bstock.ui.components.ItemImage
import com.braiso22.bstock.ui.components.LoadingContent
import com.braiso22.bstock.ui.components.MessageContent
import org.jetbrains.compose.resources.painterResource

/**
 * Lista de la compra: los items por reponer que se van tachando al meterlos en el carro.
 * Las cards dan más espacio a la imagen para reconocer el producto de un vistazo en la tienda.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ShoppingScreen(
    state: ItemsUiState,
    shopping: ShoppingUiState,
    onToggleChecked: (Item) -> Unit,
    onPriceChange: (Item, Int) -> Unit,
    onPacksChange: (Item, Int) -> Unit,
    onFinishShopping: () -> Unit,
) {
    val success = state as? ItemsUiState.Success
    val hasItems = success?.belowMinimum?.isNotEmpty() == true
    // Barra grande que se encoge al hacer scroll pero nunca desaparece: el total queda siempre a la vista.
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var editingPriceId by remember { mutableStateOf<String?>(null) }
    var editingPacksId by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    // La lista de la compra no es una alarma (rojo) sino una lista de tareas (verde).
    val dark = isSystemInDarkTheme()
    val shoppingContainer = if (dark) Color(0xFF1E3B28) else Color(0xFFDDF3E2)
    val shoppingAccent = if (dark) Color(0xFF8FD5A6) else Color(0xFF2E7D4F)
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    if (success == null || !hasItems) {
                        Text("Compra")
                    } else {
                        Text(
                            shoppingTitle(
                                cartCents = success.cartCostCents(shopping),
                                totalCents = success.shoppingCostCents(shopping),
                                cartColor = MaterialTheme.colorScheme.primary,
                                totalColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                },
                subtitle = if (success != null && hasItems) {
                    { Text(shoppingSubtitle(success, shopping)) }
                } else {
                    null
                },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            if (shopping.checkedIds.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onFinishShopping,
                    icon = { Icon(painterResource(Res.drawable.ic_check_circle), contentDescription = null) },
                    text = { Text("Terminar compra") },
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
                    state = listState,
                    modifier = modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.shoppingOrder(shopping.checkedIds), key = { it.id }) { item ->
                        ShoppingItemCard(
                            item = item,
                            checked = item.id in shopping.checkedIds,
                            packs = shopping.packsToBuy(item),
                            containerColor = shoppingContainer,
                            accentColor = shoppingAccent,
                            modifier = Modifier.animateItem(),
                            onClick = {
                                // El item marcado baja al final; sin esto la lista se iría detrás de él.
                                // Se fija la posición actual por índice para que lo de arriba siga arriba.
                                listState.requestScrollToItem(
                                    listState.firstVisibleItemIndex,
                                    listState.firstVisibleItemScrollOffset,
                                )
                                onToggleChecked(item)
                            },
                            onEditPrice = { editingPriceId = item.id },
                            onEditPacks = { editingPacksId = item.id },
                        )
                    }
                }
            }
        }
    }

    // Se busca en el estado actual para editar siempre la versión más reciente del item.
    val editingItem = success?.items?.find { it.id == editingPriceId }
    if (editingItem != null) {
        PriceDialog(
            item = editingItem,
            packs = shopping.packsToBuy(editingItem),
            onDismiss = { editingPriceId = null },
            onConfirm = {
                editingPriceId = null
                onPriceChange(editingItem, it)
            },
        )
    }
    val editingPacksItem = success?.items?.find { it.id == editingPacksId }
    if (editingPacksItem != null) {
        PacksDialog(
            item = editingPacksItem,
            packs = shopping.packsToBuy(editingPacksItem),
            onDismiss = { editingPacksId = null },
            onConfirm = {
                editingPacksId = null
                onPacksChange(editingPacksItem, it)
            },
        )
    }
}

/** Card de la lista de la compra: imagen grande a sangre a la izquierda y, al lado, qué coger y cuánto cuesta. */
@Composable
private fun ShoppingItemCard(
    item: Item,
    checked: Boolean,
    packs: Int,
    containerColor: Color,
    accentColor: Color,
    onClick: () -> Unit,
    onEditPrice: () -> Unit,
    onEditPacks: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val decoration = if (checked) TextDecoration.LineThrough else null
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().alpha(if (checked) 0.6f else 1f),
        colors = CardDefaults.cardColors(
            containerColor = if (checked) MaterialTheme.colorScheme.surfaceContainerHighest else containerColor,
        ),
    ) {
        Row {
            // La card ya recorta sus esquinas: la imagen va sin redondeo propio para quedar pegada al borde.
            ItemImage(item.imageUrl, Modifier.size(128.dp), grayscale = checked, shape = RectangleShape)
            Column(
                modifier = Modifier.weight(1f).padding(start = 16.dp, top = 12.dp, end = 8.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = decoration,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "Tienes ${item.quantity} · Mínimo ${item.minimum}" +
                        if (item.packSize > 1) " · Pack de ${item.packSize}" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textDecoration = decoration,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Pegado al texto de arriba aunque el botón tenga su propio margen interior.
                    TextButton(
                        onClick = onEditPacks,
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier.offset(x = (-8).dp),
                    ) {
                        Text(
                            text = toBuyLabel(item, packs),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (checked) MaterialTheme.colorScheme.onSurfaceVariant else accentColor,
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onEditPrice, contentPadding = PaddingValues(horizontal = 8.dp)) {
                        Text(if (item.priceCents > 0) formatPrice(packs * item.priceCents) else "Añadir precio")
                    }
                }
            }
        }
    }
}

/** Lo que va en el carro en grande y con color, y el total al lado más pequeño ("1,90 € de 3,25 €"). */
private fun shoppingTitle(cartCents: Int, totalCents: Int, cartColor: Color, totalColor: Color): AnnotatedString =
    buildAnnotatedString {
        withStyle(SpanStyle(color = cartColor)) { append(formatPrice(cartCents)) }
        withStyle(SpanStyle(color = totalColor, fontSize = 0.6.em)) { append(" de ${formatPrice(totalCents)}") }
    }

/** Explica el número del título y añade lo que falta por coger. */
private fun shoppingSubtitle(state: ItemsUiState.Success, shopping: ShoppingUiState): String {
    val parts = mutableListOf(
        "En el carro",
        "Falta ${formatPrice(state.shoppingCostCents(shopping) - state.cartCostCents(shopping))}",
    )
    if (state.restockWithoutPrice > 0) parts += "${state.restockWithoutPrice} sin precio"
    return parts.joinToString(" · ")
}

/** Qué se va a coger en la tienda: "Comprar 3" en unidades sueltas o "Comprar 2 packs". */
private fun toBuyLabel(item: Item, packs: Int): String = when {
    item.packSize == 1 -> "Comprar $packs"
    packs == 1 -> "Comprar 1 pack"
    else -> "Comprar $packs packs"
}

/** Elige cuánto comprar de un item con +/-; parte de lo que haya ahora y nunca baja de uno. */
@Composable
private fun PacksDialog(item: Item, packs: Int, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var value by remember { mutableStateOf(packs) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(item.name) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(onClick = { value-- }, enabled = value > 1) {
                        Icon(painterResource(Res.drawable.ic_remove), contentDescription = "Uno menos")
                    }
                    Text(
                        text = value.toString(),
                        style = MaterialTheme.typography.displaySmall,
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )
                    FilledTonalIconButton(onClick = { value++ }) {
                        Icon(painterResource(Res.drawable.ic_add), contentDescription = "Uno más")
                    }
                }
                Text(
                    text = packsSummary(item, value),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(value) }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

/** Debajo del contador: lo que suponen esos packs en unidades y euros, y lo que haría falta para llegar al mínimo. */
private fun packsSummary(item: Item, packs: Int): String {
    val parts = mutableListOf<String>()
    if (item.packSize > 1) parts += "${packs * item.packSize} unidades"
    if (item.priceCents > 0) parts += formatPrice(packs * item.priceCents)
    val needed = if (item.packSize > 1) "${item.packsToBuy} ${if (item.packsToBuy == 1) "pack" else "packs"}" else "${item.missing}"
    return parts.joinToString(" · ") + (if (parts.isEmpty()) "" else "\n") + "Para llegar al mínimo: $needed"
}

@Composable
private fun PriceDialog(item: Item, packs: Int, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    // Texto seleccionado de entrada para que lo que se escriba reemplace el precio actual.
    var field by remember {
        val text = if (item.priceCents > 0) formatPrice(item.priceCents).removeSuffix(" €") else ""
        mutableStateOf(TextFieldValue(text, selection = TextRange(0, text.length)))
    }
    val value = parsePrice(field.text)
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(item.name) },
        text = {
            OutlinedTextField(
                value = field,
                onValueChange = { new -> field = new.copy(text = new.text.filter { it.isDigit() || it == ',' || it == '.' }) },
                label = { Text(if (item.packSize > 1) "Precio del pack de ${item.packSize} (€)" else "Precio (€)") },
                supportingText = { value?.let { Text("${toBuyLabel(item, packs)} · ${formatPrice(it * packs)}") } },
                singleLine = true,
                isError = value == null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { value?.let(onConfirm) }),
                modifier = Modifier.focusRequester(focusRequester),
            )
        },
        confirmButton = {
            TextButton(onClick = { value?.let(onConfirm) }, enabled = value != null) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
