package com.moyuer233.deepseeknative.data

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack

/** 播账号 TTS 返回的裸 PCM：24kHz / 单声道 / 16-bit LE。 */
object PcmPlayer {
    private var track: AudioTrack? = null

    fun stop() {
        try {
            track?.stop()
            track?.release()
        } catch (_: Exception) {
        }
        track = null
    }

    fun play(pcm: ByteArray): Boolean {
        stop()
        return try {
            val t = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(24000)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(pcm.size)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            t.write(pcm, 0, pcm.size)
            t.play()
            track = t
            true
        } catch (e: Exception) {
            false
        }
    }
}
