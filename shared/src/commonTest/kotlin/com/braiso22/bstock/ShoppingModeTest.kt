package com.braiso22.bstock

import com.braiso22.bstock.data.InMemoryItemRepository
import com.braiso22.bstock.ui.inventory.InventoryViewModel
import com.braiso22.bstock.ui.inventory.ShoppingUiState
import kotlin.test.Test
import kotlin.test.assertEquals

class ShoppingModeTest {

    private val viewModel = InventoryViewModel(InMemoryItemRepository(initialItems = emptyList()))

    @Test
    fun togglingChecksAndUnchecksItems() {
        viewModel.toggleChecked("a")
        viewModel.toggleChecked("b")
        viewModel.toggleChecked("a")

        assertEquals(ShoppingUiState(checkedIds = setOf("b")), viewModel.shopping.value)
    }

    @Test
    fun finishingEmptiesTheCart() {
        viewModel.toggleChecked("a")
        viewModel.finishShopping()

        assertEquals(ShoppingUiState(), viewModel.shopping.value)
    }
}
