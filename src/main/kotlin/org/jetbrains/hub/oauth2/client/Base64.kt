package org.jetbrains.hub.oauth2.client

object Base64 {
    private val PEM_ARRAY = sequenceOf(
            ('A'..'Z').asSequence(),
            ('a'..'z').asSequence(),
            ('0'..'9').asSequence(),
            sequenceOf('+', '/')
    ).flatMap { it }.toList().toCharArray()

    private val FILL = '='


    fun encode(bytes: ByteArray): String {
        val builder = StringBuilder((bytes.size * 4 + 1) / 3)
        if (bytes.isNotEmpty()) {
            for (i in (0..bytes.lastIndex / 3)) {
                val base = i * 3
                val b1 = bytes.get(base).toInt()
                val b2 = bytes.getOrNull(base + 1)?.toInt()
                val b3 = bytes.getOrNull(base + 2)?.toInt()

                builder.append(PEM_ARRAY[(b1 ushr 2) and 63])
                builder.append(PEM_ARRAY[((b1 shl 4) and 48) + ((b2 ?: 0) ushr 4 and 15)])
                if (b2 != null) {
                    builder.append(PEM_ARRAY[((b2 shl 2) and 60) + ((b3 ?: 0) ushr 6 and 3)])
                    if (b3 != null) {
                        builder.append(PEM_ARRAY[b3 and 63])
                    } else {
                        builder.append(FILL)
                    }
                } else {
                    builder.append(FILL)
                    builder.append(FILL)
                }

            }
        }
        return builder.toString()
    }


}