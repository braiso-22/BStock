package com.braiso22.bstock.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class FirestoreItemRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : ItemRepository {

    private val collection get() = firestore.collection(COLLECTION)

    override fun observeItems(): Flow<List<Item>> = flow {
        ensureSignedIn()
        emitAll(callbackFlow {
            val registration = collection
                .orderBy(FIELD_NAME, Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) trySend(snapshot.documents.mapNotNull { it.toItem() })
                }
            awaitClose { registration.remove() }
        })
    }

    override fun observeItem(id: String): Flow<Item?> = flow {
        ensureSignedIn()
        emitAll(callbackFlow {
            val registration = collection.document(id).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toItem())
            }
            awaitClose { registration.remove() }
        })
    }

    override suspend fun add(item: Item) {
        ensureSignedIn()
        collection.add(item.toMap()).await()
    }

    override suspend fun update(item: Item) {
        ensureSignedIn()
        collection.document(item.id).set(item.toMap()).await()
    }

    override suspend fun updateQuantity(id: String, quantity: Int) {
        ensureSignedIn()
        collection.document(id).update(FIELD_QUANTITY, quantity.coerceAtLeast(0)).await()
    }

    override suspend fun updatePrice(id: String, priceCents: Int) {
        ensureSignedIn()
        collection.document(id).update(FIELD_PRICE_CENTS, priceCents.coerceAtLeast(0)).await()
    }

    override suspend fun delete(id: String) {
        ensureSignedIn()
        collection.document(id).delete().await()
    }

    /** Las reglas de Firestore exigen usuario autenticado; usamos login anónimo. */
    private suspend fun ensureSignedIn() {
        if (auth.currentUser == null) auth.signInAnonymously().await()
    }

    private fun Item.toMap(): Map<String, Any> = mapOf(
        FIELD_NAME to name,
        FIELD_QUANTITY to quantity,
        FIELD_MINIMUM to minimum,
        FIELD_IMAGE_URL to imageUrl,
        FIELD_PRICE_CENTS to priceCents,
    )

    private fun DocumentSnapshot.toItem(): Item? {
        if (!exists()) return null
        return Item(
            id = id,
            name = getString(FIELD_NAME).orEmpty(),
            quantity = getLong(FIELD_QUANTITY)?.toInt() ?: 0,
            minimum = getLong(FIELD_MINIMUM)?.toInt() ?: 0,
            imageUrl = getString(FIELD_IMAGE_URL).orEmpty(),
            priceCents = getLong(FIELD_PRICE_CENTS)?.toInt() ?: 0,
        )
    }

    private companion object {
        const val COLLECTION = "items"
        const val FIELD_NAME = "name"
        const val FIELD_QUANTITY = "quantity"
        const val FIELD_MINIMUM = "minimum"
        const val FIELD_IMAGE_URL = "imageUrl"
        const val FIELD_PRICE_CENTS = "priceCents"
    }
}
