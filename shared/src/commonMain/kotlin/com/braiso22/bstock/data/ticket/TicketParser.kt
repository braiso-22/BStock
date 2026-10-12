package com.braiso22.bstock.data.ticket

/** Un producto de un ticket. [totalCents] es lo cobrado por él antes de promociones y descuentos. */
data class TicketLine(val name: String, val units: Int, val totalCents: Int)

data class Ticket(
    /** Fecha tal cual sale en el ticket: `dd/MM/yy`. */
    val date: String?,
    val lines: List<TicketLine>,
    /** Nº de artículos que declara el propio ticket ("8 ART. TOTAL A PAGAR"). */
    val declaredArticles: Int?,
) {
    val units: Int get() = lines.sumOf { it.units }

    /** El ticket se ha leído bien si las unidades coinciden con las que declara. */
    val isConsistent: Boolean get() = declaredArticles == null || declaredArticles == units
}

/**
 * Lee el texto de un ticket de Carrefour (la capa de texto del PDF).
 *
 * Los productos van entre la línea de asteriscos y la primera línea de `=`:
 * ```
 * PAN DE MOLDE 820G                    1,15   ← 1 unidad: nombre + importe
 * PIZZA PEPPERONI          2X03        2,99   ← código de promoción opcional antes del importe
 * PANANCOTTA CARAMELO                         ← varias unidades: nombre sin importe…
 *      2 x (     1,05 )                2,10   ← …y debajo unidades x precio unitario
 *    1 3x2 EN EL MÁS BA  2X03         -2,99   ← importes negativos: promociones y descuentos
 * DTO. CLIENTE(SALDO 1.234,56)       -1,41
 * ```
 * Los productos a granel (fruta, carne, pescado) salen como 1 unidad con su importe; el ticket no trae el peso.
 */
object TicketParser {

    private const val AMOUNT = """-?\d{1,3}(?:\.\d{3})*,\d{2}"""
    private val itemRow = Regex("""^(.*?)\s+($AMOUNT)$""")
    private val unitsRow = Regex("""^(\d+)\s+x\s+\(\s*$AMOUNT\s*\)\s+($AMOUNT)$""")
    private val promoCode = Regex("""\s+[0-9A-Z]{2}\d{2}$""")
    private val articlesRow = Regex("""^(\d+)\s+ART\.\s+TOTAL A PAGAR""")
    private val dateRow = Regex("""^(\d{2}/\d{2}/\d{2})\s+\d{2}:\d{2}:\d{2}""")
    private val spaces = Regex("""\s+""")

    fun parse(text: String): Ticket {
        val rows = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val start = rows.indexOfFirst { row -> row.all { it == '*' } }
        val products = if (start < 0) emptyList() else rows.drop(start + 1).takeWhile { !it.startsWith("=") }

        val lines = mutableListOf<TicketLine>()
        var pendingName: String? = null
        for (row in products) {
            val units = unitsRow.matchEntire(row)
            if (units != null) {
                if (pendingName != null) {
                    lines += TicketLine(pendingName, units.groupValues[1].toInt(), units.groupValues[2].toCents())
                }
                pendingName = null
                continue
            }
            val item = itemRow.matchEntire(row)
            pendingName = null
            when {
                item == null -> pendingName = cleanName(row)
                item.groupValues[2].startsWith("-") -> Unit
                else -> lines += TicketLine(cleanName(item.groupValues[1]), 1, item.groupValues[2].toCents())
            }
        }

        return Ticket(
            date = rows.firstNotNullOfOrNull { dateRow.find(it)?.groupValues?.get(1) },
            lines = lines,
            declaredArticles = rows.firstNotNullOfOrNull { articlesRow.find(it)?.groupValues?.get(1)?.toInt() },
        )
    }

    private fun cleanName(raw: String): String = raw.replace(promoCode, "").replace(spaces, " ").trim()

    private fun String.toCents(): Int = replace(".", "").replace(",", "").toInt()
}

/** Unidades compradas de cada producto sumando todos los tickets. */
fun List<Ticket>.unitsByProduct(): Map<String, Int> =
    flatMap { it.lines }.groupingBy { it.name }.fold(0) { total, line -> total + line.units }
