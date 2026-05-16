package com.babyvault.android.data.local

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

private const val KEYSTORE_ALIAS = "babyvault_master_key_wrapping_key"
private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
private const val TRANSFORMATION = "AES/GCM/NoPadding"
private const val GCM_IV_LEN = 12
private const val GCM_TAG_LEN = 128

@Singleton
class KeystoreMasterKeyStore @Inject constructor() {

    private val keyStore: KeyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }

    fun wrapKey(rawKey: ByteArray): ByteArray {
        val secretKey = getOrCreateKey()
        val cipher = Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, secretKey) }
        val iv = cipher.iv
        val encrypted = cipher.doFinal(rawKey)
        return iv + encrypted
    }

    fun unwrapKey(wrappedKey: ByteArray): ByteArray {
        val iv = wrappedKey.sliceArray(0 until GCM_IV_LEN)
        val ciphertext = wrappedKey.sliceArray(GCM_IV_LEN until wrappedKey.size)
        val secretKey = getOrCreateKey()
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LEN, iv))
        }
        return cipher.doFinal(ciphertext)
    }

    private fun getOrCreateKey(): SecretKey {
        if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
            val spec = KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setUserAuthenticationRequired(false)
                .build()
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
                .apply { init(spec) }
                .generateKey()
        }
        return (keyStore.getEntry(KEYSTORE_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
    }
}
