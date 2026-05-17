package cl.friendlypos.mypos.api

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer

class DebugHttpInterceptor : Interceptor {

    companion object {
        private const val TAG = "FriendlyPOS_HTTP"
        private const val MAX_BODY = 8_000
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()

        val reqBody = req.body?.let {
            val buf = Buffer()
            it.writeTo(buf)
            buf.readUtf8().take(MAX_BODY)
        }

        val sb = StringBuilder()
        sb.appendLine("━━━ REQUEST ━━━━━━━━━━━━━━━━━━━━━━")
        sb.appendLine("  ${req.method} ${req.url}")
        sb.appendLine("  Query params:")
        req.url.queryParameterNames.forEach { name ->
            sb.appendLine("    $name = ${req.url.queryParameter(name)}")
        }
        sb.appendLine("  Headers:")
        req.headers.forEach { (name, value) ->
            val display = if (name.equals("Cookie", ignoreCase = true) && value.length > 120)
                value.take(120) + "…"
            else value
            sb.appendLine("    $name: $display")
        }
        if (!reqBody.isNullOrBlank()) {
            sb.appendLine("  Body: $reqBody")
        }
        Log.d(TAG, sb.toString())

        val t0 = System.currentTimeMillis()

        val resp = try {
            chain.proceed(req)
        } catch (e: Exception) {
            Log.e(TAG, "REQUEST FAILED  ${req.method} ${req.url}  error=${e.message}")
            throw e
        }

        val elapsed = System.currentTimeMillis() - t0
        val respBody = resp.peekBody(MAX_BODY.toLong()).string()

        val sb2 = StringBuilder()
        sb2.appendLine("━━━ RESPONSE ━━━━━━━━━━━━━━━━━━━━━")
        sb2.appendLine("  ${req.method} ${req.url}")
        sb2.appendLine("  Status: ${resp.code} ${resp.message}  (${elapsed}ms)")
        sb2.appendLine("  Headers:")
        resp.headers.forEach { (name, value) ->
            sb2.appendLine("    $name: $value")
        }
        sb2.appendLine("  Body (${respBody.length} chars):")
        sb2.appendLine("  ${respBody.take(MAX_BODY)}")
        Log.d(TAG, sb2.toString())

        return resp
    }
}
