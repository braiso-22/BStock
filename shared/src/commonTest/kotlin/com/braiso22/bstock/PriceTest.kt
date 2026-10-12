package com.braiso22.bstock

import com.braiso22.bstock.data.formatPrice
import com.braiso22.bstock.data.parsePrice
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PriceTest {

    @Test
    fun formatsCentsAsEuros() {
        assertEquals("1,08 €", formatPrice(108))
        assertEquals("0,99 €", formatPrice(99))
        assertEquals("12,50 €", formatPrice(1250))
    }

    @Test
    fun parsesCommaDotAndWholeEuros() {
        assertEquals(108, parsePrice("1,08"))
        assertEquals(108, parsePrice("1.08"))
        assertEquals(150, parsePrice("1,5"))
        assertEquals(200, parsePrice(" 2 "))
    }

    @Test
    fun rejectsInvalidPrices() {
        assertNull(parsePrice(""))
        assertNull(parsePrice("1,234"))
        assertNull(parsePrice("1,"))
        assertNull(parsePrice("abc"))
    }
}
