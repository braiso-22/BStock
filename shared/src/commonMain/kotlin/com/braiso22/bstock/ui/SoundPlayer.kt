package com.braiso22.bstock.ui

/** Sonidos de la app; cada plataforma decide cómo reproducirlos. */
interface SoundPlayer {
    /** Campanita al meter un item en el carro. */
    fun playCheck()
}

/** Para plataformas o pruebas sin sonido. */
object NoSound : SoundPlayer {
    override fun playCheck() = Unit
}
