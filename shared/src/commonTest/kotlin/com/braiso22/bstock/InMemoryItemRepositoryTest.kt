package com.braiso22.bstock

import com.braiso22.bstock.data.InMemoryItemRepository
import com.braiso22.bstock.data.Item
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class InMemoryItemRepositoryTest {

    private fun item(name: String, quantity: Int = 1, minimum: Int = 1) =
        Item(name = name, quantity = quantity, minimum = minimum, imageUrl = "")

    @Test
    fun addAssignsIdsAndListIsSortedByName() = runTest {
        val repository = InMemoryItemRepository(initialItems = emptyList())
        repository.add(item("Leche"))
        repository.add(item("Café"))

        val items = repository.observeItems().first()

        assertEquals(listOf("Café", "Leche"), items.map { it.name })
        assertEquals(2, items.map { it.id }.toSet().size)
    }

    @Test
    fun updateQuantityIsReflectedAndNeverNegative() = runTest {
        val repository = InMemoryItemRepository(initialItems = listOf(item("Leche", quantity = 3)))
        val id = repository.observeItems().first().single().id

        repository.updateQuantity(id, 1)
        assertEquals(1, repository.observeItem(id).first()?.quantity)

        repository.updateQuantity(id, -5)
        assertEquals(0, repository.observeItem(id).first()?.quantity)
    }

    @Test
    fun updateReplacesTheWholeItem() = runTest {
        val repository = InMemoryItemRepository(initialItems = listOf(item("Leche")))
        val original = repository.observeItems().first().single()

        repository.update(original.copy(name = "Leche entera", minimum = 4))

        val updated = repository.observeItem(original.id).first()
        assertEquals("Leche entera", updated?.name)
        assertEquals(4, updated?.minimum)
    }

    @Test
    fun deleteRemovesTheItem() = runTest {
        val repository = InMemoryItemRepository(initialItems = listOf(item("Leche")))
        val id = repository.observeItems().first().single().id

        repository.delete(id)

        assertNull(repository.observeItem(id).first())
        assertEquals(emptyList(), repository.observeItems().first())
    }

    @Test
    fun updatePriceOnlyChangesThePrice() = runTest {
        val repository = InMemoryItemRepository(initialItems = listOf(item("Leche", quantity = 3).copy(priceCents = 95)))
        val id = repository.observeItems().first().single().id

        repository.updatePrice(id, 109)

        assertEquals(item("Leche", quantity = 3).copy(id = id, priceCents = 109), repository.observeItem(id).first())
    }
}
