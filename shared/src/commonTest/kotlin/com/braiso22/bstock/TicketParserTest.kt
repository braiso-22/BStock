package com.braiso22.bstock

import com.braiso22.bstock.data.ticket.TicketLine
import com.braiso22.bstock.data.ticket.TicketParser
import com.braiso22.bstock.data.ticket.unitsByProduct
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TicketParserTest {

    @Test
    fun singleUnitRowsDropPromoCodesAndSkipDiscounts() {
        val ticket = TicketParser.parse(TicketFixtures.ticket0926)

        assertEquals("26/09/26", ticket.date)
        assertEquals(
            listOf(
                TicketLine("PAN DE MOLDE 820G", 1, 115),
                TicketLine("PIZZA CAMPOFRIO 380G", 1, 325),
                TicketLine("PIZZA PEPPERONI", 1, 299),
                TicketLine("PIZZA Y SALSA POLLO", 1, 299),
                TicketLine("PIMIENTO ROJO", 1, 140),
                TicketLine("PAN PANNIER PLUS MED", 1, 135),
                TicketLine("EMPANADA BACALAO CRF", 1, 575),
                TicketLine("EMPANADA CARNE CRF", 1, 575),
            ),
            ticket.lines,
        )
    }

    @Test
    fun multiUnitRowsTakeNameFromPreviousRow() {
        val ticket = TicketParser.parse(TicketFixtures.ticket0930)

        assertEquals(
            listOf(
                TicketLine("CHOCOLATE ALMENDRA", 3, 597),
                TicketLine("CHOCOLATE EXCELLEN", 3, 1275),
                TicketLine("GUACAMOLE 200GR", 1, 169),
                TicketLine("PREPARADO COCIDO", 1, 489),
            ),
            ticket.lines,
        )
    }

    @Test
    fun everyTicketMatchesItsDeclaredArticles() {
        TicketFixtures.all.map(TicketParser::parse).forEach { ticket ->
            assertTrue(ticket.isConsistent, "${ticket.date}: ${ticket.units} != ${ticket.declaredArticles}")
        }
    }

    @Test
    fun unitsAreAddedUpAcrossTickets() {
        val units = TicketFixtures.all.map(TicketParser::parse).unitsByProduct()

        assertEquals(8, units["PANANCOTTA CARAMELO"])
        assertEquals(6, units["JAMON CURADO 225G"])
        assertEquals(5, units["GUACAMOLE 200GR"])
        assertEquals(3, units["LOMO TROZO"])
        assertEquals(3, units["PAN DE MOLDE 820G"])
    }
}
