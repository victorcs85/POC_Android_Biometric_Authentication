package br.com.victorcs.biometricauth.data.repository

import android.content.Context
import javax.crypto.Cipher

interface ICryptographyManager {
    fun getInitializedCipherForEncryption(keyName: String): Cipher

    fun getInitializedCipherForDecryption(
        keyName: String,
        initializationVector: ByteArray,
        context: Context,
        filename: String,
        mode: Int,
        reAuthAction: () -> Unit
    ): Cipher?

    fun encryptData(plaintext: String, cipher: Cipher): CiphertextWrapper

    fun decryptData(ciphertext: ByteArray, cipher: Cipher): String

    fun persistCiphertextWrapperToSharedPrefs(
        ciphertextWrapper: CiphertextWrapper,
        context: Context,
        filename: String,
        mode: Int,
        prefKey: String
    )

    fun getCiphertextWrapperFromSharedPrefs(
        context: Context,
        filename: String,
        mode: Int,
        prefKey: String
    ): CiphertextWrapper?

    fun clear(
        keyName: String,
        context: Context,
        filename: String,
        mode: Int
    )
}