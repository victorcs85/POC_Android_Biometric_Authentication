package br.com.victorcs.poc_biometria.utils

import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method


object ChangedBiometricUtils {
    @RequiresApi(Build.VERSION_CODES.M)
    fun getFingerprintInfo(context: Context): MutableList<String> {
        val result = mutableListOf<String>()
        try {
            val fingerprintManager =
                context.getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager
            val method: Method =
                FingerprintManager::class.java.getDeclaredMethod("getEnrolledFingerprints")
            val obj: Any = method.invoke(fingerprintManager)
            val clazz =
                Class.forName("android.hardware.fingerprint.Fingerprint")
            val getFingerId: Method = clazz.getDeclaredMethod("getFingerId")
            for (i in (obj as List<*>).indices) {
                val item = obj[i]
                if (item != null) {
                    result.add("FingerId: " + getFingerId.invoke(item))
                    Log.e("getFingerprintInfo", "fingerId: " + getFingerId.invoke(item))
                }
            }
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
        return result
    }
}