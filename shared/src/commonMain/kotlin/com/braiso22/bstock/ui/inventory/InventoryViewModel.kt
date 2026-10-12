package com.braiso22.bstock.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.braiso22.bstock.data.Item
import com.braiso22.bstock.data.ItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

sealed interface ItemsUiState {
    data object Loading : ItemsUiState
    data class Error(val message: String) : ItemsUiState
    data class Success(val items: List<Item>) : ItemsUiState {
        val belowMinimum: List<Item> = items.filter { it.isBelowMinimum }

        /** Coste estimado de reponer todo lo que está por debajo del mínimo, en céntimos. */
        val restockCostCents: Int = belowMinimum.sumOf { it.restockCostCents }

        /** Items por reponer que no tienen precio y por tanto no cuentan en [restockCostCents]. */
        val restockWithoutPrice: Int = belowMinimum.count { it.priceCents == 0 }

        /** Lista de la compra: primero lo que falta por coger y al final lo ya marcado, cada grupo en su orden. */
        fun shoppingOrder(checkedIds: Set<String>): List<Item> = belowMinimum.sortedBy { it.id in checkedIds }

        /** Parte de [restockCostCents] que corresponde a los items ya metidos en el carro. */
        fun cartCostCents(checkedIds: Set<String>): Int =
            belowMinimum.filter { it.id in checkedIds }.sumOf { it.restockCostCents }
    }
}

/** Modo compra: la lista de "por reponer" pasa a ser una lista de la compra en la que se van tachando items. */
data class ShoppingUiState(
    val active: Boolean = false,
    val checkedIds: Set<String> = emptySet(),
)

/** Compartido por la pantalla de inventario y la de "por reponer": ambas leen la misma escucha en tiempo real. */
class InventoryViewModel(repository: ItemRepository) : ViewModel() {
    val state: StateFlow<ItemsUiState> = repository.observeItems()
        .map<List<Item>, ItemsUiState> { ItemsUiState.Success(it) }
        .catch { emit(ItemsUiState.Error(it.message ?: "Error al cargar los items")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ItemsUiState.Loading)

    private val _shopping = MutableStateFlow(ShoppingUiState())
    val shopping: StateFlow<ShoppingUiState> = _shopping.asStateFlow()

    fun startShopping() {
        _shopping.value = ShoppingUiState(active = true)
    }

    fun finishShopping() {
        _shopping.value = ShoppingUiState()
    }

    fun toggleChecked(id: String) {
        _shopping.update {
            it.copy(checkedIds = if (id in it.checkedIds) it.checkedIds - id else it.checkedIds + id)
        }
    }
}
