package com.braiso22.bstock.ui.form

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.braiso22.bstock.data.Item
import com.braiso22.bstock.data.ItemRepository
import com.braiso22.bstock.data.formatAmount
import com.braiso22.bstock.data.parsePrice
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Crea un item nuevo (`itemId == null`) o edita uno existente. */
class ItemFormViewModel(
    private val repository: ItemRepository,
    private val itemId: String?,
) : ViewModel() {

    val isEditing = itemId != null

    var name by mutableStateOf("")
    var quantity by mutableStateOf("")
    var minimum by mutableStateOf("")
    var imageUrl by mutableStateOf("")
    var price by mutableStateOf("")
    var packSize by mutableStateOf("1")

    var isLoading by mutableStateOf(isEditing)
        private set
    var isSaving by mutableStateOf(false)
        private set
    var isSaved by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    val canSave: Boolean
        get() = !isLoading && !isSaving && name.isNotBlank() &&
            quantity.toIntOrNull() != null && minimum.toIntOrNull() != null &&
            (price.isBlank() || parsePrice(price) != null) &&
            (packSize.toIntOrNull() ?: 0) >= 1

    init {
        if (itemId != null) {
            viewModelScope.launch {
                runCatching { repository.observeItem(itemId).first() }
                    .onSuccess { item ->
                        if (item != null) {
                            name = item.name
                            quantity = item.quantity.toString()
                            minimum = item.minimum.toString()
                            imageUrl = item.imageUrl
                            price = if (item.priceCents > 0) formatAmount(item.priceCents) else ""
                            packSize = item.packSize.toString()
                        }
                    }
                    .onFailure { error = it.message }
                isLoading = false
            }
        }
    }

    fun save() {
        if (!canSave) return
        val item = Item(
            id = itemId.orEmpty(),
            name = name.trim(),
            quantity = quantity.toInt(),
            minimum = minimum.toInt(),
            imageUrl = imageUrl.trim(),
            priceCents = parsePrice(price) ?: 0,
            packSize = packSize.toInt(),
        )
        isSaving = true
        error = null
        viewModelScope.launch {
            runCatching { if (isEditing) repository.update(item) else repository.add(item) }
                .onSuccess { isSaved = true }
                .onFailure { error = "No se pudo guardar: ${it.message}" }
            isSaving = false
        }
    }
}
