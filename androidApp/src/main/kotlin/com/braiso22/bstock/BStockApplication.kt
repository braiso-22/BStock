package com.braiso22.bstock

import android.app.Application
import com.braiso22.bstock.data.FirestoreItemRepository
import com.braiso22.bstock.data.InMemoryItemRepository
import com.braiso22.bstock.data.ItemRepository
import com.braiso22.bstock.ui.BellSoundPlayer

class BStockApplication : Application() {
    /**
     * Vive mientras viva el proceso, para que los datos en memoria sobrevivan a rotaciones
     * y todas las pantallas compartan la misma instancia.
     */
    val repository: ItemRepository by lazy {
        if (BuildConfig.USE_IN_MEMORY_DATA) InMemoryItemRepository() else FirestoreItemRepository()
    }

    val sounds by lazy { BellSoundPlayer() }
}
