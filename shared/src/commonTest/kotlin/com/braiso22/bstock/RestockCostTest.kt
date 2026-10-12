package com.braiso22.bstock

import com.braiso22.bstock.data.Item
import com.braiso22.bstock.ui.inventory.ItemsUiState
import kotlin.test.Test
import kotlin.test.assertEquals

class RestockCostTest {

    private fun item(name: String, quantity: Int, minimum: Int, priceCents: Int) =
        Item(name = name, quantity = quantity, minimum = minimum, imageUrl = "", priceCents = priceCents)

    @Test
    fun totalAddsMissingUnitsTimesPriceOfItemsBelowMinimum() {
        val state = ItemsUiState.Success(
            listOf(
                item("Panacotta", quantity = 1, minimum = 4, priceCents = 105),
                item("Jamón", quantity = 0, minimum = 2, priceCents = 489),
                item("Guacamole", quantity = 5, minimum = 2, priceCents = 169),
            ),
        )

        assertEquals(3 * 105 + 2 * 489, state.restockCostCents)
        assertEquals(0, state.restockWithoutPrice)
    }

    @Test
    fun itemsWithoutPriceAreCountedApart() {
        val state = ItemsUiState.Success(
            listOf(
                item("Leche", quantity = 0, minimum = 2, priceCents = 95),
                item("Detergente", quantity = 0, minimum = 1, priceCents = 0),
            ),
        )

        assertEquals(190, state.restockCostCents)
        assertEquals(1, state.restockWithoutPrice)
    }

    @Test
    fun cartCostOnlyCountsCheckedItems() {
        val state = ItemsUiState.Success(
            listOf(
                item("Panacotta", quantity = 1, minimum = 4, priceCents = 105).copy(id = "a"),
                item("Jamón", quantity = 0, minimum = 2, priceCents = 489).copy(id = "b"),
            ),
        )

        assertEquals(0, state.cartCostCents(emptySet()))
        assertEquals(2 * 489, state.cartCostCents(setOf("b")))
        assertEquals(state.restockCostCents, state.cartCostCents(setOf("a", "b")))
    }

    @Test
    fun shoppingOrderMovesCheckedItemsToTheEndKeepingOrder() {
        val state = ItemsUiState.Success(
            listOf("a", "b", "c", "d").map { item(it, quantity = 0, minimum = 1, priceCents = 100).copy(id = it) },
        )

        assertEquals(listOf("b", "d", "a", "c"), state.shoppingOrder(setOf("c", "a")).map { it.id })
        assertEquals(listOf("a", "b", "c", "d"), state.shoppingOrder(emptySet()).map { it.id })
    }

    @Test
    fun packsAreRoundedUpAndPricedPerPack() {
        val yogur = item("Yogur", quantity = 3, minimum = 8, priceCents = 140).copy(packSize = 4)
        val exact = item("Leche", quantity = 0, minimum = 12, priceCents = 570).copy(packSize = 6)

        assertEquals(5, yogur.missing)
        assertEquals(2, yogur.packsToBuy)
        assertEquals(280, yogur.restockCostCents)
        assertEquals(2, exact.packsToBuy)
        assertEquals(1140, exact.restockCostCents)
    }
}
