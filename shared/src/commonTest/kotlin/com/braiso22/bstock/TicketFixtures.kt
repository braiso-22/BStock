package com.braiso22.bstock

/** Tickets reales recortados: cabecera, productos, total y fecha (sin datos de pago ni de socio). */
object TicketFixtures {

    private const val HEADER = """
        ***Centros Comerciales Carrefour S.A***
                    Tienda
                  CIF: A28425270
        **************************************
    """

    val ticket0925 = HEADER + """
        GEL SANEX 600 ML          2U65        2,75
        GALLETA CARREFOUR                     1,84
        GALLETA MARIA DORADA
             4 x (     1,29 )                 5,16
        GALLETAS TOSTADA
             4 x (     1,35 )                 5,40
        PASTAS SURTIDO                        2,29
        P.P L.FR/SAL 2X200G                   3,85
        SALMON PIEZA                         20,04
        BANANA GRANEL                         1,64
        BERENJENA CARREFOUR                   2,03
        BROCOLI GRANEL                        2,05
        LECHUGA HOJA ROBLE                    1,49
        MELON CARREFOUR                       1,99
        TOMATE GRANEL                         1,83
        BARRA GOURMET                         1,15
        LOMO TROZO                            4,37
        CAMI SRA MC ALGODO                    3,99
        CTA SRA MC TACHAS                     5,99
        DTO. CLIENTE(SALDO   1.234,56)      -5,43
        ==========================================
          SUBTOTAL                           62,43
        ==========================================
        DTO. VALE REGA.GEL SANEX 2           -2,75
        9900000000000
        ==========================================
              23 ART. TOTAL A PAGAR :        59,68
        ==========================================
        25/09/26 12:00:00   0001  001 0001  0001
    """

    val ticket0926 = HEADER + """
        PAN DE MOLDE 820G                     1,15
        PIZZA CAMPOFRIO 380G      2X03        3,25
        PIZZA PEPPERONI           2X03        2,99
        PIZZA Y SALSA POLLO       2X03        2,99
           1 3x2 EN EL MÁS BA  2X03          -2,99
        PIMIENTO ROJO                         1,40
        PAN PANNIER PLUS MED                  1,35
        EMPANADA BACALAO CRF      E061        5,75
        EMPANADA CARNE CRF        E061        5,75
        DESCUENTO EN 2ª UNIDAD    E061       -4,02
        DTO. CLIENTE(SALDO   1.234,56)      -1,41
        ==========================================
               8 ART. TOTAL A PAGAR :        16,21
        ==========================================
        TIPO            BASE           CUOTA
        4,00%           3,45            0,14
        26/09/26 12:00:00   0001  001 0001  0001
    """

    val ticket0929 = HEADER + """
                 PANANCOTTA CARAMELO
             2 x (     1,05 )                 2,10
        DTO. CLIENTE(SALDO   1.234,56)      -0,17
        ==========================================
               2 ART. TOTAL A PAGAR :         1,93
        ==========================================
        29/09/26 12:00:00   0001  001 0001  0001
    """

    val ticket0930 = HEADER + """
                 CHOCOLATE ALMENDRA
             3 x (     1,99 )                 5,97
        CHOCOLATE EXCELLEN        4046
             3 x (     4,25 )                12,75
           1 3x2 EN EL MÁS BA  4046          -4,25
        GUACAMOLE   200GR                     1,69
        PREPARADO COCIDO                      4,89
        DTO. CLIENTE(SALDO   1.234,56)      -1,69
        ==========================================
               8 ART. TOTAL A PAGAR :        19,36
        ==========================================
        30/09/26 12:00:00   0001  001 0001  0001
    """

    val ticket1002 = HEADER + """
        SAL LAVAVAJILLAS 3K                   1,35
        LANGOSTINO VANNAMEI                   2,90
        PAN DE MOLDE 820G                     1,15
        PANANCOTTA CARAMELO
             2 x (     1,05 )                 2,10
        YOGUR NATURAL CRF                     1,29
        JAMON CURADO 225G         2P03
             3 x (     4,89 )                14,67
           1 3x2 EN EL MÁS BA  2P03          -4,89
        BACALADILLA GORDA                     8,43
        AJO SPRINT MALLA                      2,98
        BANANA GRANEL                         1,67
        BROCOLI CRF 500GR
             2 x (     1,35 )                 2,70
        GUACAMOLE   200GR
             2 x (     1,69 )                 3,38
        MANGO GRANEL                          2,18
        LOMO TROZO                            4,73
        LOMO TROZO                            3,70
        PECHUGA DE POLLO                      7,87
        POLLO TRASEROS                        3,40
        POLLO TRASEROS                        3,82
        DTO. CLIENTE(SALDO   1.234,56)      -5,07
        ==========================================
          SUBTOTAL                           58,36
        ==========================================
        DTO. CUPON       4X60 EUR            -4,00
        9900000000000
        ==========================================
              22 ART. TOTAL A PAGAR :        40,71
        ==========================================
        02/10/26 12:00:00   0001  001 0001  0001
    """

    val ticket1008 = HEADER + """
        AMBIENTADOR GLADE         2W98        3,00
        KEFIR NATURAL 500G
             3 x (     1,05 )                 3,15
        MAXI BURGER CRF X4                    0,79
        PAN DE MOLDE 820G                     1,15
        PANANCOTTA CARAMELO
             4 x (     1,05 )                 4,20
        QUESO CARREFOUR 300G      2W87
             3 x (     1,87 )                 5,61
           1 3x2 EN EL MÁS BA  2W87          -1,87
        QUESO DOP ARZUA 650G                  8,09
        YOGUR NATURAL CRF
             3 x (     1,29 )                 3,87
        JAMON CURADO 225G         2P03
             3 x (     4,89 )                14,67
           1 3x2 EN EL MÁS BA  2P03          -4,89
        PESCADILLA FINA                      28,61
        BANANA GRANEL                         2,47
        GUACAMOLE   200GR
             2 x (     1,69 )                 3,38
        LECHUGA BATAVIA                       1,49
        PIMIENTO ROJO                         0,78
        ZANAHORIA BOLSA 1KG                   1,00
        BURGER MIXTA 8X90G                    4,99
        PECHUGA DE POLLO                      8,47
        EMPANADA POLLO            E061
             2 x (     5,75 )                11,50
        DESCUENTO EN 2ª UNIDAD    E061       -4,03
        DTO. CLIENTE(SALDO   1.234,56)      -7,72
        ==========================================
          SUBTOTAL                           88,71
        ==========================================
        DTO. CUPON GRATIS AMBIENTA           -3,00
        9900000000000
        ==========================================
              31 ART. TOTAL A PAGAR :        85,71
        ==========================================
        08/10/26 12:00:00   0001  001 0001  0001
    """

    val ticket1010 = HEADER + """
                TINTO DE VERANO 1,5L
             2 x (     1,09 )                 2,18
        PAN PANNIER PLUS MED                  1,35
        DTO. CLIENTE(SALDO   1.234,56)      -0,28
        ==========================================
               3 ART. TOTAL A PAGAR :         3,25
        ==========================================
        10/10/26 12:00:00   0001  001 0001  0001
    """

    val all = listOf(ticket0925, ticket0926, ticket0929, ticket0930, ticket1002, ticket1008, ticket1010)
}
