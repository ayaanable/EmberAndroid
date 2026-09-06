package com.example.data.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

enum class LockTimeoutPolicy(val seconds: Int, val label: String) {
    IMMEDIATE(0, "Lock immediately"),
    THIRTY_SECONDS(30, "Lock after 30 seconds"),
    ONE_MINUTE(60, "Lock after 1 minute");

    companion object {
        fun fromSeconds(seconds: Int): LockTimeoutPolicy {
            return entries.firstOrNull { it.seconds == seconds } ?: IMMEDIATE
        }
    }
}

class SecurityManager private constructor(private val context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private var lastBackgroundTimestamp: Long = 0L

    init {
        ensureKeyStoreKeyExists()
        // If app lock is enabled, start locked
        if (isAppLockEnabled()) {
            _isLocked.value = true
        }
    }

    // --- KeyStore Cryptography for Sensitive Content ---

    private fun ensureKeyStoreKeyExists() {
        try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
                val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
                keyGenerator.init(keyGenParameterSpec)
                keyGenerator.generateKey()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        return keyStore.getKey(KEY_ALIAS, null) as SecretKey
    }

    fun encryptText(plainText: String): String {
        if (plainText.isEmpty()) return ""
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
            val iv = cipher.iv
            val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            val combined = ByteArray(iv.size + cipherBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(cipherBytes, 0, combined, iv.size, cipherBytes.size)
            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            plainText
        }
    }

    fun decryptText(cipherTextBase64: String): String {
        if (cipherTextBase64.isEmpty()) return ""
        return try {
            val combined = Base64.decode(cipherTextBase64, Base64.NO_WRAP)
            if (combined.size < 12) return cipherTextBase64
            val iv = ByteArray(12)
            val cipherBytes = ByteArray(combined.size - 12)
            System.arraycopy(combined, 0, iv, 0, 12)
            System.arraycopy(combined, 12, cipherBytes, 0, cipherBytes.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
            String(cipher.doFinal(cipherBytes), Charsets.UTF_8)
        } catch (e: Exception) {
            cipherTextBase64
        }
    }

    // --- PIN Management (Salted Hash, never plaintext) ---

    fun isAppLockEnabled(): Boolean {
        return prefs.getBoolean(KEY_APP_LOCK_ENABLED, false) && prefs.getString(KEY_PIN_HASH, null) != null
    }

    fun setAppLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_APP_LOCK_ENABLED, enabled).apply()
        if (!enabled) {
            _isLocked.value = false
        }
    }

    fun isBiometricEnabled(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, true)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun getLockTimeoutPolicy(): LockTimeoutPolicy {
        val seconds = prefs.getInt(KEY_LOCK_TIMEOUT_SECONDS, 0)
        return LockTimeoutPolicy.fromSeconds(seconds)
    }

    fun setLockTimeoutPolicy(policy: LockTimeoutPolicy) {
        prefs.edit().putInt(KEY_LOCK_TIMEOUT_SECONDS, policy.seconds).apply()
    }

    fun savePin(pin: String): Boolean {
        if (pin.length != 4 || !pin.all { it.isDigit() }) return false
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        val saltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP)
        val hash = hashPin(pin, salt)
        val hashBase64 = Base64.encodeToString(hash, Base64.NO_WRAP)

        prefs.edit()
            .putString(KEY_PIN_SALT, saltBase64)
            .putString(KEY_PIN_HASH, hashBase64)
            .putBoolean(KEY_APP_LOCK_ENABLED, true)
            .apply()
        return true
    }

    fun verifyPin(pin: String): Boolean {
        val storedSaltBase64 = prefs.getString(KEY_PIN_SALT, null) ?: return false
        val storedHashBase64 = prefs.getString(KEY_PIN_HASH, null) ?: return false

        val salt = Base64.decode(storedSaltBase64, Base64.NO_WRAP)
        val storedHash = Base64.decode(storedHashBase64, Base64.NO_WRAP)
        val enteredHash = hashPin(pin, salt)

        val matches = MessageDigest.isEqual(storedHash, enteredHash)
        if (matches) {
            _isLocked.value = false
        }
        return matches
    }

    fun unlockByBiometric() {
        _isLocked.value = false
    }

    fun lockNow() {
        if (isAppLockEnabled()) {
            _isLocked.value = true
        }
    }

    private fun hashPin(pin: String, salt: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        var hash = digest.digest(pin.toByteArray(Charsets.UTF_8))
        // Multiple iterations
        for (i in 0 until 5000) {
            digest.reset()
            digest.update(salt)
            hash = digest.digest(hash)
        }
        return hash
    }

    // --- Lifecycle Management for Timeout Policy ---

    fun onAppForegrounded() {
        if (!isAppLockEnabled()) {
            _isLocked.value = false
            return
        }
        if (lastBackgroundTimestamp > 0) {
            val elapsedSeconds = (System.currentTimeMillis() - lastBackgroundTimestamp) / 1000
            val timeout = getLockTimeoutPolicy().seconds
            if (elapsedSeconds >= timeout) {
                _isLocked.value = true
            }
        }
    }

    fun onAppBackgrounded() {
        lastBackgroundTimestamp = System.currentTimeMillis()
    }

    companion object {
        private const val PREFS_NAME = "ember_secure_settings"
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS = "EmberDataMasterKey"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"

        private const val KEY_APP_LOCK_ENABLED = "app_lock_enabled"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_PIN_SALT = "pin_salt"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_LOCK_TIMEOUT_SECONDS = "lock_timeout_seconds"

        @Volatile
        private var instance: SecurityManager? = null

        fun getInstance(context: Context): SecurityManager {
            return instance ?: synchronized(this) {
                instance ?: SecurityManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
