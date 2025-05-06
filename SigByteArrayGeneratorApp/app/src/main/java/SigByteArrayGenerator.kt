class SigByteArrayGenerator {
    companion object {
        @JvmField
        val customSig: ByteArray = hexStringToByteArray(
            "<replace with sigtool's toCharsString>"
        )

        private fun hexStringToByteArray(s: String): ByteArray {
            val len = s.length
            require(len % 2 == 0) { "Hex string must have even length" }
            return ByteArray(len / 2) { i ->
                s.substring(i * 2, i * 2 + 2).toInt(16).toByte()
            }
        }
    }
}