package com.braiso22.bstock.ui.detail

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
import kotlinx.coroutines.launch

sealed interface ItemDetailUiState {
    data object Loading : ItemDetailUiState
    data object NotFound : ItemDetailUiState
    data class Error(val message: String) : ItemDetailUiState
    data class Loaded(val item: Item) : ItemDetailUiState
}

class ItemDetailViewModel(
    private val repository: ItemRepository,
    private val itemId: String,
) : ViewModel() {

    val state: StateFlow<ItemDetailUiState> = repository.observeItem(itemId)
        .map { if (it == null) ItemDetailUiState.NotFound else ItemDetailUiState.Loaded(it) }
        .catch { emit(ItemDetailUiState.Error(it.message ?: "Error al cargar el item")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ItemDetailUiState.Loading)

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun changeQuantity(delta: Int) {
        val item = (state.value as? ItemDetailUiState.Loaded)?.item ?: return
        setQuantity(item.quantity + delta)
    }

    fun setQuantity(quantity: Int) = runAction("No se pudo actualizar la cantidad") {
        repository.updateQuantity(itemId, quantity.coerceAtLeast(0))
    }

    /** Al borrarse, la escucha emite [ItemDetailUiState.NotFound] y la pantalla vuelve atrás. */
    fun delete() = runAction("No se pudo borrar el item") {
        repository.delete(itemId)
    }

    fun userMessageShown() {
        _userMessage.value = null
    }

    private fun runAction(errorMessage: String, action: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching { action() }.onFailure { _userMessage.value = "$errorMessage: ${it.message}" }
        }
    }
}
