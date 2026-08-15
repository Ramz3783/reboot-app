package com.reboot.app.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Talks directly to Groq's OpenAI-compatible API from the Android client.
 * Base URL: https://api.groq.com/openai/v1
 *
 * IMPORTANT: This project has no backend server (per requirements). That means the Groq
 * API key is stored locally on-device (DataStore) and sent from the client, entered by the
 * user themselves in Settings. This is NOT the same as hardcoding a key into the APK at
 * build time, but it is still less secure than a server-side proxy. If real security is
 * needed later, move this call behind a small backend and never ship the key in the client.
 */
object GroqApi {
    private val embeddedGroqKey = BuildConfig.GROQ_TEST_KEY

    private const val BASE_URL = "https://api.groq.com/openai/v1"

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    sealed class Result {
        data class Success(val text: String) : Result()
        data class Failure(val message: String) : Result()
    }

    suspend fun chatCompletion(
        apiKey: String = embeddedGroqKey,
        model: String,
        systemPrompt: String,
        history: List<Pair<String, String>>, // role to content
    ): Result = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.Failure("Groq API key не задан. Открой Настройки и вставь свой ключ с console.groq.com.")
        }
        try {
            val messages = buildJsonArray {
                add(buildJsonObject {
                    put("role", JsonPrimitive("system"))
                    put("content", JsonPrimitive(systemPrompt))
                })
                history.forEach { (role, content) ->
                    add(buildJsonObject {
                        put("role", JsonPrimitive(role))
                        put("content", JsonPrimitive(content))
                    })
                }
            }
            val bodyJson = buildJsonObject {
                put("model", JsonPrimitive(model))
                put("messages", messages)
                put("temperature", JsonPrimitive(0.8))
                put("max_tokens", JsonPrimitive(600))
            }
            val body = bodyJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL/chat/completions")
                .addHeader("Authorization", "Bearer $embeddedGroqKey")
                .post(body)
                .build()

            client.newCall(request).execute().use { resp ->
                val respBody = resp.body?.string().orEmpty()
                if (!resp.isSuccessful) {
                    val errMsg = runCatching {
                        json.parseToJsonElement(respBody).jsonObject["error"]
                            ?.jsonObject?.get("message")?.jsonPrimitive?.content
                    }.getOrNull() ?: respBody.take(200)
                    return@withContext Result.Failure("Groq API ошибка (${resp.code}): $errMsg")
                }
                val parsed = json.parseToJsonElement(respBody).jsonObject
                val choices = parsed["choices"]?.jsonArray
                val content = choices?.get(0)?.jsonObject
                    ?.get("message")?.jsonObject
                    ?.get("content")?.jsonPrimitive?.content
                    ?: return@withContext Result.Failure("Пустой ответ от Groq API.")
                Result.Success(content)
            }
        } catch (e: Exception) {
            Result.Failure("Ошибка сети: ${e.message}")
        }
    }

    /** Sends a recorded audio file to Groq's Whisper STT endpoint and returns the transcript. */
    suspend fun transcribeAudio(apiKey: String = embeddedGroqKey, file: File, sttModel: String = "whisper-large-v3-turbo"): Result =
        withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) return@withContext Result.Failure("Groq API key не задан. Открой Настройки.")
            try {
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("model", sttModel)
                    .addFormDataPart(
                        "file", file.name,
                        file.asRequestBody("audio/m4a".toMediaType())
                    )
                    .build()
                val request = Request.Builder()
                    .url("$BASE_URL/audio/transcriptions")
                    .addHeader("Authorization", "Bearer $embeddedGroqKey")
                    .post(requestBody)
                    .build()
                client.newCall(request).execute().use { resp ->
                    val respBody = resp.body?.string().orEmpty()
                    if (!resp.isSuccessful) {
                        return@withContext Result.Failure("Ошибка распознавания (${resp.code}): ${respBody.take(150)}")
                    }
                    val text = runCatching {
                        json.parseToJsonElement(respBody).jsonObject["text"]?.jsonPrimitive?.content
                    }.getOrNull() ?: return@withContext Result.Failure("Пустой ответ распознавания.")
                    Result.Success(text)
                }
            } catch (e: Exception) {
                Result.Failure("Ошибка сети: ${e.message}")
            }
        }

    /** List currently available models from Groq, so GROQ_MODEL is validated, not hardcoded forever. */
    suspend fun listModels(apiKey: String = embeddedGroqKey): Result = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext Result.Failure("Groq API key не задан.")
        try {
            val request = Request.Builder()
                .url("$BASE_URL/models")
                .addHeader("Authorization", "Bearer $embeddedGroqKey")
                .get()
                .build()
            client.newCall(request).execute().use { resp ->
                val respBody = resp.body?.string().orEmpty()
                if (!resp.isSuccessful) return@withContext Result.Failure("Ошибка (${resp.code})")
                val ids = runCatching {
                    json.parseToJsonElement(respBody).jsonObject["data"]?.jsonArray
                        ?.mapNotNull { it.jsonObject["id"]?.jsonPrimitive?.content }
                        ?.joinToString(", ")
                }.getOrNull() ?: "?"
                Result.Success(ids)
            }
        } catch (e: Exception) {
            Result.Failure("Ошибка сети: ${e.message}")
        }
    }
}
