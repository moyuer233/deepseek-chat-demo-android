package com.moyuer233.deepseeknative.data

/** DeepSeekHashV1：SHA3-256 的变体，Keccak-p[1600] 只跑最后 23 轮（跳过第 0 轮）。 */
object DeepSeekHash {
    private const val RATE = 136
    private const val ROUNDS = 23

                private val ROUND_CONSTANTS = longArrayOf(
        0x0000000000000001UL.toLong(),
        0x0000000000008082UL.toLong(),
        0x800000000000808AUL.toLong(),
        0x8000000080008000UL.toLong(),
        0x000000000000808BUL.toLong(),
        0x0000000080000001UL.toLong(),
        0x8000000080008081UL.toLong(),
        0x8000000000008009UL.toLong(),
        0x000000000000008AUL.toLong(),
        0x0000000000000088UL.toLong(),
        0x0000000080008009UL.toLong(),
        0x000000008000000AUL.toLong(),
        0x000000008000808BUL.toLong(),
        0x800000000000008BUL.toLong(),
        0x8000000000008089UL.toLong(),
        0x8000000000008003UL.toLong(),
        0x8000000000008002UL.toLong(),
        0x8000000000000080UL.toLong(),
        0x000000000000800AUL.toLong(),
        0x800000008000000AUL.toLong(),
        0x8000000080008081UL.toLong(),
        0x8000000000008080UL.toLong(),
        0x0000000080000001UL.toLong(),
        0x8000000080008008UL.toLong(),
    )

    private val ROTATION = intArrayOf(
        0, 1, 62, 28, 27, 36, 44, 6, 55, 20, 3, 10, 43, 25, 39,
        41, 45, 15, 21, 8, 18, 2, 61, 56, 14,
    )

    fun digest(input: ByteArray): ByteArray {
        val state = LongArray(25)
        var offset = 0
        while (offset + RATE <= input.size) {
            absorb(state, input, offset)
            keccakP(state)
            offset += RATE
        }
        val tail = ByteArray(RATE)
        val remaining = input.size - offset
        System.arraycopy(input, offset, tail, 0, remaining)
        tail[remaining] = (tail[remaining].toInt() xor 0x06).toByte()
        tail[RATE - 1] = (tail[RATE - 1].toInt() xor 0x80).toByte()
        absorb(state, tail, 0)
        keccakP(state)
        val out = ByteArray(32)
        for (i in 0 until 32) {
            out[i] = ((state[i / 8] ushr ((i % 8) * 8)) and 0xFF).toByte()
        }
        return out
    }

    private fun absorb(state: LongArray, block: ByteArray, offset: Int) {
        for (i in 0 until RATE) {
            val lane = i / 8
            val shift = (i % 8) * 8
            state[lane] = state[lane] xor ((block[offset + i].toLong() and 0xFF) shl shift)
        }
    }

    private fun keccakP(state: LongArray) {
        val c = LongArray(5)
        val d = LongArray(5)
        val b = LongArray(25)
        for (round in 1 until 24) {
            for (x in 0 until 5) {
                c[x] = state[x] xor state[x + 5] xor state[x + 10] xor state[x + 15] xor state[x + 20]
            }
            for (x in 0 until 5) {
                d[x] = c[(x + 4) % 5] xor java.lang.Long.rotateLeft(c[(x + 1) % 5], 1)
            }
            for (y in 0 until 5) {
                for (x in 0 until 5) {
                    state[x + 5 * y] = state[x + 5 * y] xor d[x]
                }
            }
            for (y in 0 until 5) {
                for (x in 0 until 5) {
                    val destX = y
                    val destY = (2 * x + 3 * y) % 5
                    b[destX + 5 * destY] = java.lang.Long.rotateLeft(state[x + 5 * y], ROTATION[x + 5 * y])
                }
            }
            for (y in 0 until 5) {
                val row = 5 * y
                for (x in 0 until 5) {
                    state[x + row] = b[x + row] xor (b[((x + 1) % 5) + row].inv() and b[((x + 2) % 5) + row])
                }
            }
            state[0] = state[0] xor ROUND_CONSTANTS[round]
        }
    }
}
