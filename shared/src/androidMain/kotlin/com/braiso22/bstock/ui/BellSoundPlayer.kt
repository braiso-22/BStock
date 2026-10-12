package com.braiso22.bstock.ui

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin

/**
 * Campanita sintetizada al vuelo (sin ficheros de audio): dos toques de campana pequeña, el segundo
 * más agudo, para que suene a "hecho". Se genera una vez y se reutiliza.
 */
class BellSoundPlayer : SoundPlayer {

    private val track: AudioTrack by lazy {
        val samples = synthesize()
        AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build(),
            )
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(samples.size * 2)
            .build()
            .apply { write(samples, 0, samples.size) }
    }

    override fun playCheck() {
        runCatching {
            track.stop()
            track.reloadStaticData()
            track.play()
        }
    }

    private fun synthesize(): ShortArray {
        val count = (SAMPLE_RATE * DURATION_SECONDS).toInt()
        return ShortArray(count) { i ->
            val t = i.toDouble() / SAMPLE_RATE
            val tone = 0.45 * bell(t, FIRST_HZ, decay = 0.10) +
                0.55 * bell(t - SECOND_DELAY_SECONDS, SECOND_HZ, decay = 0.14)
            (tone * VOLUME * Short.MAX_VALUE).toInt().toShort()
        }
    }

    /** Un toque de campana que empieza en `t = 0`: nota base más parciales inarmónicos que se apagan antes. */
    private fun bell(t: Double, hz: Double, decay: Double): Double {
        if (t < 0) return 0.0
        val attack = min(1.0, t / 0.004) // evita el "clic" del arranque
        return attack * (
            0.70 * sin(2 * PI * hz * t) * exp(-t / decay) +
                0.22 * sin(2 * PI * hz * 2.76 * t) * exp(-t / (decay * 0.4)) +
                0.08 * sin(2 * PI * hz * 5.40 * t) * exp(-t / (decay * 0.2))
            )
    }

    private companion object {
        const val SAMPLE_RATE = 44_100
        const val DURATION_SECONDS = 0.45
        const val FIRST_HZ = 1318.5 // Mi6
        const val SECOND_HZ = 1975.5 // Si6
        const val SECOND_DELAY_SECONDS = 0.07
        const val VOLUME = 0.35
    }
}
