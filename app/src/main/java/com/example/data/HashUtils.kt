package com.example.data

import java.security.MessageDigest
import java.security.SecureRandom

object HashUtils {
    fun generateSalt(): String {
        val random = SecureRandom()
        val bytes = ByteArray(16)
        random.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun hashPassword(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val combined = (password + salt).toByteArray(Charsets.UTF_8)
        val hash = digest.digest(combined)
        return hash.joinToString("") { "%02x".format(it) }
    }
}
