package com.braiso22.bstock.ui.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bstock.shared.generated.resources.Res
import bstock.shared.generated.resources.ic_add
import bstock.shared.generated.resources.ic_back
import bstock.shared.generated.resources.ic_delete
import bstock.shared.generated.resources.ic_edit
import bstock.shared.generated.resources.ic_remove
import com.braiso22.bstock.data.Item
import com.braiso22.bstock.data.formatPrice
import com.braiso22.bstock.ui.components.ItemImage
import com.braiso22.bstock.ui.components.LoadingContent
import com.braiso22.bstock.ui.components.MessageContent
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    viewModel: ItemDetailViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        if (state is ItemDetailUiState.NotFound) onBack()
    }
    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.userMessageShown()
        }
    }

    val item = (state as? ItemDetailUiState.Loaded)?.item

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(item?.name.orEmpty()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(Res.drawable.ic_back), contentDescription = "Volver")
                    }
                },
                actions = {
                    if (item != null) {
                        IconButton(onClick = onEdit) {
                            Icon(painterResource(Res.drawable.ic_edit), contentDescription = "Editar")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(painterResource(Res.drawable.ic_delete), contentDescription = "Borrar")
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when (val current = state) {
            ItemDetailUiState.Loading, ItemDetailUiState.NotFound -> LoadingContent(modifier)
            is ItemDetailUiState.Error -> MessageContent(current.message, modifier)
            is ItemDetailUiState.Loaded -> ItemDetailContent(
                item = current.item,
                onDecrement = { viewModel.changeQuantity(-1) },
                onIncrement = { viewModel.changeQuantity(+1) },
                onSetQuantity = viewModel::setQuantity,
                modifier = modifier,
            )
        }
    }

    if (showDeleteDialog && item != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Borrar item") },
            text = { Text("¿Seguro que quieres borrar \"${item.name}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.delete()
                }) { Text("Borrar") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            },
        )
    }
}

@Composable
private fun ItemDetailContent(
    item: Item,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    onSetQuantity: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showQuantityDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ItemImage(item.imageUrl, Modifier.fillMaxWidth().height(240.dp))

        Text(item.name, style = MaterialTheme.typography.headlineMedium)
        if (item.priceCents > 0) {
            Text(formatPrice(item.priceCents), style = MaterialTheme.typography.titleLarge)
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (item.isBelowMinimum) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                },
            ),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Cantidad actual", style = MaterialTheme.typography.titleMedium)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    FilledTonalIconButton(
                        onClick = onDecrement,
                        enabled = item.quantity > 0,
                        modifier = Modifier.size(56.dp),
                    ) {
                        Icon(painterResource(Res.drawable.ic_remove), contentDescription = "Quitar uno")
                    }
                    Text(
                        text = item.quantity.toString(),
                        style = MaterialTheme.typography.displayMedium,
                        modifier = Modifier
                            .widthIn(min = 96.dp)
                            .clickable { showQuantityDialog = true },
                        textAlign = TextAlign.Center,
                    )
                    FilledTonalIconButton(onClick = onIncrement, modifier = Modifier.size(56.dp)) {
                        Icon(painterResource(Res.drawable.ic_add), contentDescription = "Añadir uno")
                    }
                }
                Text("Toca el número para escribir la cantidad", style = MaterialTheme.typography.bodySmall)
                Text("Mínimo: ${item.minimum}", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = if (item.isBelowMinimum) {
                        "Por debajo del mínimo · faltan ${item.missing}"
                    } else {
                        "Stock suficiente"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = if (item.isBelowMinimum) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                )
            }
        }
    }

    if (showQuantityDialog) {
        QuantityDialog(
            initial = item.quantity,
            onDismiss = { showQuantityDialog = false },
            onConfirm = {
                showQuantityDialog = false
                onSetQuantity(it)
            },
        )
    }
}

@Composable
private fun QuantityDialog(initial: Int, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    // Texto seleccionado de entrada para que lo que se escriba reemplace la cantidad actual.
    var field by remember {
        val text = initial.toString()
        mutableStateOf(TextFieldValue(text, selection = TextRange(0, text.length)))
    }
    val value = field.text.toIntOrNull()?.takeIf { it >= 0 }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cantidad actual") },
        text = {
            OutlinedTextField(
                value = field,
                onValueChange = { field = it.copy(text = it.text.filter(Char::isDigit)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { value?.let(onConfirm) }),
                isError = value == null,
                modifier = Modifier.focusRequester(focusRequester),
            )
        },
        confirmButton = {
            TextButton(onClick = { value?.let(onConfirm) }, enabled = value != null) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
