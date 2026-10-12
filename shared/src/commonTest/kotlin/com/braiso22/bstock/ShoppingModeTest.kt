package com.braiso22.bstock

import com.braiso22.bstock.data.InMemoryItemRepository
import com.braiso22.bstock.data.Item
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
    fun choosingTheSuggestedPacksAgainForgetsTheChange() {
        val yogur = Item(id = "y", name = "Yogur", quantity = 3, minimum = 8, imageUrl = "", packSize = 4)

        viewModel.setPacksToBuy(yogur, 5)
        assertEquals(mapOf("y" to 5), viewModel.shopping.value.packs)

        viewModel.setPacksToBuy(yogur, yogur.packsToBuy)
        assertEquals(emptyMap(), viewModel.shopping.value.packs)
    }

    @Test
    fun finishingEmptiesTheCartAndForgetsChangedPacks() {
        viewModel.toggleChecked("a")
        viewModel.setPacksToBuy(Item(id = "a", name = "A", quantity = 0, minimum = 1, imageUrl = ""), 3)
        viewModel.finishShopping()

        assertEquals(ShoppingUiState(), viewModel.shopping.value)
    }
}
