package com.braiso22.bstock

import com.braiso22.bstock.data.Item
import com.braiso22.bstock.data.filterByName
import kotlin.test.Test
import kotlin.test.assertEquals

class SearchTest {

    private val items = listOf("Plátano", "Pan de molde 820 g", "Pan Pannier", "Jamón curado 225 g", "Piña")
        .map { Item(id = it, name = it, quantity = 0, minimum = 1, imageUrl = "") }

    private fun search(query: String) = items.filterByName(query).map { it.name }

    @Test
    fun emptyQueryKeepsEverything() {
        assertEquals(items.map { it.name }, search(""))
        assertEquals(items.map { it.name }, search("   "))
    }

    @Test
    fun ignoresCaseAndAccents() {
        assertEquals(listOf("Plátano"), search("PLATANO"))
        assertEquals(listOf("Jamón curado 225 g"), search("jamon"))
        assertEquals(listOf("Piña"), search("pina"))
    }

    @Test
    fun everyWordMustAppearInAnyOrder() {
        assertEquals(listOf("Pan de molde 820 g"), search("molde pan"))
        assertEquals(listOf("Pan de molde 820 g", "Pan Pannier"), search("pan"))
        assertEquals(emptyList(), search("pan jamón"))
    }
}
