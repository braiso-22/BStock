package com.braiso22.bstock.data

data class Item(
    val id: String = "",
    val name: String,
    val quantity: Int,
    val minimum: Int,
    val imageUrl: String,
) {
    val isBelowMinimum: Boolean get() = quantity < minimum

    /** Unidades que faltan para volver a llegar al mínimo. */
    val missing: Int get() = (minimum - quantity).coerceAtLeast(0)
}
