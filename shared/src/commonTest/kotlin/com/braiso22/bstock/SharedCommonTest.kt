package com.braiso22.bstock

import com.braiso22.bstock.data.Item
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SharedCommonTest {

    private fun item(quantity: Int, minimum: Int) =
        Item(name = "Papel", quantity = quantity, minimum = minimum, imageUrl = "")

    @Test
    fun belowMinimumWhenQuantityIsLessThanMinimum() {
        assertTrue(item(quantity = 1, minimum = 3).isBelowMinimum)
        assertEquals(2, item(quantity = 1, minimum = 3).missing)
    }

    @Test
    fun notBelowMinimumWhenQuantityReachesMinimum() {
        assertFalse(item(quantity = 3, minimum = 3).isBelowMinimum)
        assertFalse(item(quantity = 5, minimum = 3).isBelowMinimum)
        assertEquals(0, item(quantity = 5, minimum = 3).missing)
    }
}
