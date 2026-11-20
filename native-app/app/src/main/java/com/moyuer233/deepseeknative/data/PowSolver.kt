package com.moyuer233.deepseeknative.data

import android.util.Base64
import org.json.JSONObject

/** DeepSeekHashV1 工作量证明：在 difficulty 范围内找 nonce，使 hash(prefix+nonce)=challenge。 */
object PowSolver {
    private fun hexToBytes(hex: String): ByteArray {
        val out = ByteArray(hex.length / 2)
        for (i in out.indices) {
            out[i] = ((hex[i * 2].digitToInt(16) shl 4) or hex[i * 2 + 1].digitToInt(16)).toByte()
        }
        return out
    }

    fun solve(prefix: String, challengeHex: String, difficulty: Int): Long {
        val target = hexToBytes(challengeHex.lowercase())
        val prefixBytes = prefix.toByteArray()
        for (nonce in 0 until difficulty) {
            val digits = nonce.toString().toByteArray()
            val input = ByteArray(prefixBytes.size + digits.size)
            System.arraycopy(prefixBytes, 0, input, 0, prefixBytes.size)
            System.arraycopy(digits, 0, input, prefixBytes.size, digits.size)
            if (DeepSeekHash.digest(input).contentEquals(target)) {
                return nonce.toLong()
            }
        }
        return -1L
    }

    fun guestHeader(salt: String, answer: Long): String {
        val json = JSONObject().put("salt", salt).put("answer", answer).toString()
        return Base64.encodeToString(json.toByteArray(), Base64.NO_WRAP)
    }
}
