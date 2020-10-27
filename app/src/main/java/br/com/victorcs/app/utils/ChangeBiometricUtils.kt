package br.com.victorcs.app.utils

import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method


object ChangeBiometricUtils {
    @RequiresApi(Build.VERSION_CODES.M)
    fun getFingerprintInfo(context: Context) {
        try {
            val fingerprintManager =
                context.getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager
            val method: Method =
                FingerprintManager::class.java.getDeclaredMethod("getEnrolledFingerprints")
            val obj: Any = method.invoke(fingerprintManager)
            if (obj != null) {
                val clazz =
                    Class.forName("android.hardware.fingerprint.Fingerprint")
                val getFingerId: Method = clazz.getDeclaredMethod("getFingerId")
                for (i in (obj as List<*>).indices) {
                    val item = obj[i]
                    if (item != null) {
                        Log.e("LOGGGGGGGG", "fkie4. fingerId: " + getFingerId.invoke(item))
                    }
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
    }
}