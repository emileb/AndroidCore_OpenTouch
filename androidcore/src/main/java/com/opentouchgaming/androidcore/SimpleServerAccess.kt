package com.opentouchgaming.androidcore

import android.os.Handler
import android.os.Looper
import androidx.core.util.Consumer
import java.net.HttpURLConnection
import java.net.URL

/**
 * Tiny async HTTP GET helper. Tries each URL in order, returns the trimmed body
 * of the first 200 response (null if all fail). Best-effort, no dialogs/caching.
 */
object SimpleServerAccess
{
    private val log = DebugLog(DebugLog.Module.CORE, "SimpleServerAccess")

    @JvmStatic
    fun get(urls: List<String>, onResult: Consumer<String?>)
    {
        Thread {
            val result = urls.firstNotNullOfOrNull { tryGet(it) }
            Handler(Looper.getMainLooper()).post { onResult.accept(result) }
        }.start()
    }

    private fun tryGet(urlString: String): String?
    {
        var connection: HttpURLConnection? = null
        return try
        {
            connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("User-Agent", "OpenTouch")
                connect()
            }

            if (connection.responseCode != 200) null
            else connection.inputStream.bufferedReader().use { it.readText().trim() }
        }
        catch (e: Exception)
        {
            log.log(DebugLog.Level.D, "Failed: $urlString : $e")
            null
        }
        finally
        {
            connection?.disconnect()
        }
    }
}
