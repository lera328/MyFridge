package com.example.myfridge2.Tools

import android.os.Handler
import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

public interface ApiCallback {
    fun onSuccess(response: String)
    fun onFailure(errorMessage: String)
}

class OpenAIApiClient(private val callback: ApiCallback, private val mainHandler: Handler) {
    private var client = OkHttpClient.Builder()
        .callTimeout(60, TimeUnit.SECONDS) // Таймаут выполнения запроса
        .connectTimeout(60, TimeUnit.SECONDS) // Таймаут установления соединения
        .readTimeout(60, TimeUnit.SECONDS) // Таймаут чтения данных
        .build()

    fun getResponse(t: String) {
        var url = "https://api.openai.com/v1/chat/completions"
        //val apiKey = "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"
        val apiKey = "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"

        //val t=binding.etQuestion.text
        val requestBody = """
        {
            "model": "gpt-3.5-turbo",
            "messages": [{"role": "user", "content": "I only have $t.  what I can cook at home?"},{"role": "user", "content": "I only need a recipe from $t"},{"role": "user", "content": "you are a cook and you can cook a dish from any product"},{"role": "user", "content": "Your client is a Russian speaker, so the answer must be translated into Russian"}]
        }
    """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mainHandler.post { callback.onFailure("Request failed: ${e.message}") }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (body != null) {
                    Log.v("data", parseResponseJson(body).toString())
                    mainHandler.post {
                        callback.onSuccess(parseResponseJson(body).toString())
                    }
                } else {
                    Log.v("data", "empty")
                    mainHandler.post {
                        callback.onFailure(("empty"))
                    }
                }
            }

        })
    }

    fun parseResponseJson(jsonString: String): String? {
        val jsonObject = JSONObject(jsonString)
        val choicesArray = jsonObject.getJSONArray("choices")
        if (choicesArray.length() > 0) {
            val messageObject = choicesArray.getJSONObject(0).getJSONObject("message")
            return messageObject.getString("content")
        }
        return null
    }
}