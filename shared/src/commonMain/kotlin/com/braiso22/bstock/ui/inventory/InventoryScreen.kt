package com.braiso22.bstock.ui.inventory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import bstock.shared.generated.resources.Res
import bstock.shared.generated.resources.ic_add
import bstock.shared.generated.resources.ic_close
import bstock.shared.generated.resources.ic_search
import com.braiso22.bstock.data.Item
import com.braiso22.bstock.data.filterByName
import com.braiso22.bstock.ui.components.ItemRow
import com.braiso22.bstock.ui.components.LoadingContent
import com.braiso22.bstock.ui.components.MessageContent
import org.jetbrains.compose.resources.painterResource

/** Todos los items, con un buscador fijo arriba que filtra la lista por nombre según se escribe. */
@Composable
fun InventoryScreen(
    state: ItemsUiState,
    onItemClick: (Item) -> Unit,
    onAddClick: () -> Unit,
) {
    // Saveable: la búsqueda se conserva al cambiar de pestaña y volver.
    var query by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()
    Scaffold(
        topBar = {
            SearchField(
                query = query,
                onQueryChange = {
                    query = it
                    // Cada búsqueda nueva empieza mirando desde el primer resultado.
                    listState.requestScrollToItem(0)
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(painterResource(Res.drawable.ic_add), contentDescription = "Añadir item")
            }
        },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when (state) {
            ItemsUiState.Loading -> LoadingContent(modifier)
            is ItemsUiState.Error -> MessageContent(state.message, modifier)
            is ItemsUiState.Success -> {
                val items = state.items.filterByName(query)
                when {
                    state.items.isEmpty() -> MessageContent("Aún no hay items.\nPulsa + para añadir el primero.", modifier)
                    items.isEmpty() -> MessageContent("Ningún producto coincide con «${query.trim()}».", modifier)
                    else -> LazyColumn(
                        state = listState,
                        modifier = modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(items, key = { it.id }) { item ->
                            ItemRow(
                                item = item,
                                supportingText = "Cantidad: ${item.quantity} · Mínimo: ${item.minimum}",
                                onClick = { onItemClick(item) },
                                trailing = {
                                    Text(
                                        text = item.quantity.toString(),
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = if (item.isBelowMinimum) {
                                            MaterialTheme.colorScheme.error
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    val focusManager = LocalFocusManager.current
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
        placeholder = { Text("Buscar producto") },
        leadingIcon = { Icon(painterResource(Res.drawable.ic_search), contentDescription = null) },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(painterResource(Res.drawable.ic_close), contentDescription = "Borrar búsqueda")
                }
            }
        } else {
            null
        },
        singleLine = true,
        shape = CircleShape,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        // La lista ya está filtrada al escribir: "Buscar" en el teclado solo lo cierra para ver los resultados.
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
    )
}
