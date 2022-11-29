package com.intuisoft.plaid.common.util

import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

object AESUtils {
    private var passwordKey = mutableListOf<Byte>()

    fun setPassword(password: String) {
        if(password.isEmpty() || password.isBlank())
            return

        val paddingByte = (password[0].code + 24).toByte()
        passwordKey = mutableListOf()

        var x = 0
        var y = 0
        while(x < 16) {
            if(x > 0 && (x % password.length) == 0 && passwordKey.last() != paddingByte) {
                y = 0
                while(y < password.length && x < 16) {
                    passwordKey.add(paddingByte)
                    y++
                    x++
                }
                continue
            }

            passwordKey.add(password[x % password.length].code.toByte())
            x++
        }
    }

    fun encrypt(cleartext: String): String? {
        try {
            if(passwordKey.isEmpty()) return null

            val rawKey = rawKey
            val result = encrypt(rawKey, cleartext.toByteArray())
            return toHex(result)
        } catch (e: Throwable) {
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(e)
            return null
        }
    }

    fun decrypt(encrypted: String): String? {
        try {
            if(passwordKey.isEmpty()) return null

            val enc = toByte(encrypted)
            val result = decrypt(enc)
            return String(result)
        } catch (e: Throwable) {
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(e)
            return null
        }
    }

    @get:Throws(Exception::class)
    private val rawKey: ByteArray
        private get() {
            val key: SecretKey = SecretKeySpec(passwordKey.toByteArray(), "AES")
            return key.encoded
        }

    @Throws(Exception::class)
    private fun encrypt(raw: ByteArray, clear: ByteArray): ByteArray {
        val skeySpec: SecretKey = SecretKeySpec(raw, "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec)
        return cipher.doFinal(clear)
    }

    @Throws(Exception::class)
    private fun decrypt(encrypted: ByteArray): ByteArray {
        val skeySpec: SecretKey = SecretKeySpec(passwordKey.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, skeySpec)
        return cipher.doFinal(encrypted)
    }

    fun toByte(hexString: String): ByteArray {
        val len = hexString.length / 2
        val result = ByteArray(len)
        for (i in 0 until len) result[i] = Integer.valueOf(
            hexString.substring(2 * i, 2 * i + 2),
            16
        ).toByte()
        return result
    }

    fun toHex(buf: ByteArray?): String {
        if (buf == null) return ""
        val result = StringBuffer(2 * buf.size)
        for (i in buf.indices) {
            appendHex(result, buf[i])
        }
        return result.toString()
    }

    private const val HEX = "0123456789abcdef"
    private fun appendHex(sb: StringBuffer, b: Byte) {
        sb.append(HEX[b.toInt() shr 4 and 0x0f]).append(HEX[b.toInt() and 0x0f])
    }
}