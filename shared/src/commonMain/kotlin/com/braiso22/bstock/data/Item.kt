package com.braiso22.bstock.data

data class Item(
    val id: String = "",
    val name: String,
    val quantity: Int,
    val minimum: Int,
    val imageUrl: String,
    /** Precio de un pack (o de una unidad si [packSize] es 1) en céntimos; 0 si no se conoce. */
    val priceCents: Int = 0,
    /** Unidades que trae cada pack al comprarlo. El stock y el mínimo se cuentan siempre en unidades. */
    val packSize: Int = 1,
) {
    val isBelowMinimum: Boolean get() = quantity < minimum

    /** Unidades que faltan para volver a llegar al mínimo. */
    val missing: Int get() = (minimum - quantity).coerceAtLeast(0)

    /** Packs que hay que comprar para volver al mínimo, redondeando hacia arriba. */
    val packsToBuy: Int get() = (missing + packSize - 1) / packSize

    /** Lo que costaría reponer lo que falta, en céntimos. */
    val restockCostCents: Int get() = packsToBuy * priceCents
}

/** Céntimos en formato español: 108 → "1,08 €". */
fun formatPrice(cents: Int): String = "${cents / 100},${(cents % 100).toString().padStart(2, '0')} €"

/** Lee un precio escrito por el usuario ("1,08", "1.08", "2") en céntimos, o `null` si no es válido. */
fun parsePrice(text: String): Int? {
    val match = Regex("""^(\d{1,6})(?:[.,](\d{1,2}))?$""").matchEntire(text.trim()) ?: return null
    val (euros, decimals) = match.destructured
    return euros.toInt() * 100 + decimals.padEnd(2, '0').toInt()
}
