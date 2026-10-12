package com.braiso22.bstock.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Repositorio en memoria para probar la app sin tocar Firestore.
 * Los datos se pierden al cerrar la app y se vuelve a empezar con [initialItems].
 */
class InMemoryItemRepository(initialItems: List<Item> = sampleItems) : ItemRepository {

    private data class State(val items: Map<String, Item>, val nextId: Int)

    private val state = MutableStateFlow(
        State(
            items = initialItems.mapIndexed { index, item -> item.copy(id = "item-$index") }.associateBy { it.id },
            nextId = initialItems.size,
        ),
    )

    // Mismo orden que la consulta de Firestore.
    override fun observeItems(): Flow<List<Item>> =
        state.map { it.items.values.sortedBy(Item::name) }.distinctUntilChanged()

    override fun observeItem(id: String): Flow<Item?> =
        state.map { it.items[id] }.distinctUntilChanged()

    override suspend fun add(item: Item) {
        state.update {
            val id = "item-${it.nextId}"
            State(items = it.items + (id to item.copy(id = id)), nextId = it.nextId + 1)
        }
    }

    override suspend fun update(item: Item) {
        state.update {
            if (item.id !in it.items) return@update it
            it.copy(items = it.items + (item.id to item))
        }
    }

    override suspend fun updateQuantity(id: String, quantity: Int) {
        state.update {
            val item = it.items[id] ?: return@update it
            it.copy(items = it.items + (id to item.copy(quantity = quantity.coerceAtLeast(0))))
        }
    }

    override suspend fun delete(id: String) {
        state.update { it.copy(items = it.items - id) }
    }

    companion object {
        val sampleItems = listOf(
            Item(name = "Papel higiénico", quantity = 3, minimum = 6, imageUrl = "https://picsum.photos/id/1060/600/400", priceCents = 45),
            Item(name = "Leche", quantity = 2, minimum = 4, imageUrl = "https://picsum.photos/id/30/600/400", priceCents = 95),
            Item(name = "Huevos", quantity = 12, minimum = 6, imageUrl = "https://picsum.photos/id/292/600/400", priceCents = 239),
            Item(name = "Aceite de oliva", quantity = 1, minimum = 1, imageUrl = "https://picsum.photos/id/312/600/400", priceCents = 899),
            Item(name = "Detergente", quantity = 0, minimum = 1, imageUrl = ""),
            Item(name = "Café", quantity = 5, minimum = 2, imageUrl = "https://picsum.photos/id/425/600/400"),
        )
    }
}
