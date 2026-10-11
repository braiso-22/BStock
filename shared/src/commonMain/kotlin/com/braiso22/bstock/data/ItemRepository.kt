package com.braiso22.bstock.data

import kotlinx.coroutines.flow.Flow

interface ItemRepository {
    /** Emite la lista completa cada vez que cambia en el servidor. */
    fun observeItems(): Flow<List<Item>>

    /** Emite el item cada vez que cambia, o `null` si no existe (p. ej. tras borrarlo). */
    fun observeItem(id: String): Flow<Item?>

    suspend fun add(item: Item)

    suspend fun update(item: Item)

    suspend fun updateQuantity(id: String, quantity: Int)

    suspend fun delete(id: String)
}
