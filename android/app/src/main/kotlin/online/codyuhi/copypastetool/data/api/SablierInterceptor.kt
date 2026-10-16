package online.codyuhi.copypastetool.data.api

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException

class SablierInterceptor(
    private val maxWaitMs: Long = 60_000L,
    private val initialDelayMs: Long = 2_000L,
) : Interceptor {

    private val _isWaking = MutableStateFlow(false)
    val isWaking: StateFlow<Boolean> = _isWaking.asStateFlow()

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()
            .header("Accept", "application/json")

        var response: Response
        try {
            response = chain.proceed(requestBuilder.build())
        } catch (e: IOException) {
            _isWaking.value = false
            throw e
        }

        val path = originalRequest.url.encodedPath
        val isApiCall = path.startsWith("/api/") || path == "/healthz"

        if (!isApiCall) {
            return response
        }

        val contentType = response.header("Content-Type")?.lowercase().orEmpty()
        val isHtmlResponse = contentType.contains("text/html")

        if (response.isSuccessful && isHtmlResponse) {
            _isWaking.value = true
            response.close()

            val startTime = System.currentTimeMillis()
            var delayMs = initialDelayMs

            while (System.currentTimeMillis() - startTime < maxWaitMs) {
                try {
                    Thread.sleep(delayMs)
                } catch (ie: InterruptedException) {
                    Thread.currentThread().interrupt()
                    _isWaking.value = false
                    throw IOException("Interrupted while waiting for Sablier cold start", ie)
                }

                try {
                    val retryResponse = chain.proceed(requestBuilder.build())
                    val retryContentType = retryResponse.header("Content-Type")?.lowercase().orEmpty()

                    if (retryResponse.isSuccessful && retryContentType.contains("text/html")) {
                        retryResponse.close()
                        delayMs = (delayMs * 2).coerceAtMost(8_000L)
                    } else {
                        _isWaking.value = false
                        return retryResponse
                    }
                } catch (e: IOException) {
                    delayMs = (delayMs * 2).coerceAtMost(8_000L)
                }
            }

            _isWaking.value = false
            // Timeout reached: synthesize 504 Gateway Timeout
            return Response.Builder()
                .request(originalRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(504)
                .message("Gateway Timeout: Sablier scale-to-zero wake timeout")
                .body("{\"error\":\"Sablier wake timeout\"}".toResponseBody("application/json".toMediaTypeOrNull()))
                .build()
        }

        _isWaking.value = false
        return response
    }
}
