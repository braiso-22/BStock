package com.braiso22.bstock.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.braiso22.bstock.data.Item
import com.braiso22.bstock.data.ItemRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface ItemsUiState {
    data object Loading : ItemsUiState
    data class Error(val message: String) : ItemsUiState
    data class Success(val items: List<Item>) : ItemsUiState {
        val belowMinimum: List<Item> = items.filter { it.isBelowMinimum }
    }
}

/** Compartido por la pantalla de inventario y la de "por reponer": ambas leen la misma escucha en tiempo real. */
class InventoryViewModel(repository: ItemRepository) : ViewModel() {
    val state: StateFlow<ItemsUiState> = repository.observeItems()
        .map<List<Item>, ItemsUiState> { ItemsUiState.Success(it) }
        .catch { emit(ItemsUiState.Error(it.message ?: "Error al cargar los items")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ItemsUiState.Loading)
}
