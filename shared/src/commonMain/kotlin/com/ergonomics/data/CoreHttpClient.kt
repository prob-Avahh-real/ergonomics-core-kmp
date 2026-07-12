package com.ergonomics.data

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.encoding.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/// 共享 HTTP 客户端 — Ktor 跨平台, 含超时/重试/压缩
class CoreHttpClient private constructor() {
    private val json = Json {
        ignoreUnknownKeys = true; isLenient = true
        encodeDefaults = true; coerceInputValues = true
    }

    val client = HttpClient {
        install(ContentNegotiation) { json(this@CoreHttpClient.json) }
        install(ContentEncoding) { gzip(); deflate() }
        defaultRequest { contentType(ContentType.Application.Json) }
        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
            connectTimeoutMillis = 10_000
        }
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 2)
            retryOnException(maxRetries = 3)
            exponentialDelay()
        }
    }

    suspend inline fun <reified T> get(path: String, params: Map<String, String> = emptyMap()): T =
        client.get(path) { params.forEach { (k, v) -> parameter(k, v) } }.body()

    suspend fun warmup(path: String = "/health") {
        try { client.get(path) } catch (_: Exception) { /* 预热失败不阻塞 */ }
    }

    companion object {
        val shared: CoreHttpClient by lazy { CoreHttpClient() }
    }
}
