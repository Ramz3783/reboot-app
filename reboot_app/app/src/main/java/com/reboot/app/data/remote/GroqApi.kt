package com.reboot.app.data.remote

import com.reboot.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
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
 * Talks directly to Groq's OpenAI-compatible API from the Android client, using a key
 * embedded at build time via BuildConfig (set in app/build.gradle.kts). No manual entry,
 * no backend server. The key is baked into the APK, which is fine for a private/personal
 * key with a free-tier budget, but anyone who decompiles the APK can read it.
 */
object GroqApi {

    private val KEY = BuildConfig.GROQ_TEST_KEY
    private const val BASE_URL = "https://api.groq.com/openai/v1"

    // Short, chat-like answers by default — this is a phone chat screen, not an essay.
    private const val MAX_TOKENS = 220

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
        model: String,
        systemPrompt: String,
        history: List<Pair<String, String>>, // role to content
    ): Result = withContext(Dispatchers.IO) {
        if (KEY.isBlank()) {
            return@withContext Result.Failure("AI временно недоступен (нет ключа в сборке).")
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
                put("temperature", JsonPrimitive(0.7))
                put("max_tokens", JsonPrimitive(MAX_TOKENS))
            }
            val body = bodyJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL/chat/completions")
                .addHeader("Authorization", "Bearer $KEY")
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
    suspend fun transcribeAudio(file: File, sttModel: String = "whisper-large-v3-turbo"): Result =
        withContext(Dispatchers.IO) {
            if (KEY.isBlank()) return@withContext Result.Failure("AI временно недоступен (нет ключа в сборке).")
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
                    .addHeader("Authorization", "Bearer $KEY")
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
}
