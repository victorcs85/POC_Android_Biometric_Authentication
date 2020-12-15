package br.com.victorcs.biometricauth.data.crypto

import android.os.Build
import javax.crypto.Cipher

interface ICryptographyManager {

    fun canUseCrypto(): Boolean
    fun getCipherForEncryption(): Cipher
    fun getCipherForDecryption(initializationVector: ByteArray): Cipher
    fun encrypt(plainData: String, cipher: Cipher): EncryptedData
    fun decrypt(encryptedData: ByteArray, cipher: Cipher): String
    fun setSecretKeyType(keyType: Int)

    companion object {

        /**
         * Returns a new instance of [ICryptographyManager], which is:
         * - [CryptographyManagerLegacy] if the device is running on API level < 23
         * - [CryptographyManagerApi23] if the device is running on API level >= 23 and < 30
         * - [CryptographyManagerApi30] if the device is running on API level > 30
         */
        fun instance(): ICryptographyManager {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return CryptographyManagerLegacy()
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                return CryptographyManagerApi23()
            }
            return CryptographyManagerApi30()
        }
    }
}