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
import kotlinx.coroutines.launch

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

        /** Lo que costará la compra con las cantidades elegidas en la tienda. */
        fun shoppingCostCents(shopping: ShoppingUiState): Int = belowMinimum.sumOf { shopping.costCents(it) }

        /** Parte de [shoppingCostCents] que corresponde a los items ya metidos en el carro. */
        fun cartCostCents(shopping: ShoppingUiState): Int =
            belowMinimum.filter { it.id in shopping.checkedIds }.sumOf { shopping.costCents(it) }
    }
}

/** Lista de la compra: los items por reponer que ya se han metido en el carro y las cantidades cambiadas en la tienda. */
data class ShoppingUiState(
    val checkedIds: Set<String> = emptySet(),
    /** Packs (o unidades) a comprar por item cuando no son los que harían falta para llegar al mínimo. */
    val packs: Map<String, Int> = emptyMap(),
) {
    fun packsToBuy(item: Item): Int = packs[item.id] ?: item.packsToBuy

    fun costCents(item: Item): Int = packsToBuy(item) * item.priceCents
}

/** Compartido por la pantalla de inventario y la de "por reponer": ambas leen la misma escucha en tiempo real. */
class InventoryViewModel(private val repository: ItemRepository) : ViewModel() {
    val state: StateFlow<ItemsUiState> = repository.observeItems()
        .map<List<Item>, ItemsUiState> { ItemsUiState.Success(it) }
        .catch { emit(ItemsUiState.Error(it.message ?: "Error al cargar los items")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ItemsUiState.Loading)

    private val _shopping = MutableStateFlow(ShoppingUiState())
    val shopping: StateFlow<ShoppingUiState> = _shopping.asStateFlow()

    /** Vacía el carro y olvida las cantidades cambiadas para que la próxima compra empiece de cero. */
    fun finishShopping() {
        _shopping.value = ShoppingUiState()
    }

    /** Ajusta el precio (del pack, si lo hay) al real de la tienda; el total se recalcula solo con la escucha en tiempo real. */
    fun updatePrice(id: String, priceCents: Int) {
        viewModelScope.launch {
            runCatching { repository.updatePrice(id, priceCents) }
        }
    }

    /** Cambia cuánto se compra de un item solo para esta compra; volver a lo sugerido deja de contar como cambio. */
    fun setPacksToBuy(item: Item, packs: Int) {
        _shopping.update {
            it.copy(packs = if (packs == item.packsToBuy) it.packs - item.id else it.packs + (item.id to packs))
        }
    }

    fun toggleChecked(id: String) {
        _shopping.update {
            it.copy(checkedIds = if (id in it.checkedIds) it.checkedIds - id else it.checkedIds + id)
        }
    }
}
