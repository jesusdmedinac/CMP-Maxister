package com.jetbrains.kmpapp.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

interface ChatAPI {
    suspend fun sendMessage(messages: List<Message>): Message
}

class ChatAPIImpl(
    private val client: HttpClient
) : ChatAPI {
    override suspend fun sendMessage(messages: List<Message>): Message {
        val response = client
            .post("https://api.openai.com/v1/chat/completions") {
                contentType(ContentType.Application.Json)
                setBody(
                    RequestBody(
                        model = "gpt-3.5-turbo-0125",
                        messages = listOf(
                            listOf(
                                Message(
                                    "system",
                                    "Te llamas Lince"
                                )
                            ),
                            messages
                        )
                            .flatten()
                            .map {
                                RequestMessage(
                                    role = it.author,
                                    content = it.body
                                )
                            },
                        temperature = 0.0,
                        response_format = ResponseFormat(type = "json_object")
                    )
                )
            }
        val chatCompletionsResponse = response.body<ChatCompletionsResponse>()
        return Message(
            "system",
            Json.decodeFromString(chatCompletionsResponse.choices.first().message.content)
        )
    }
}

data class Message(val author: String, val body: String)

@Serializable
data class RequestBody(
    val model: String,
    val messages: List<RequestMessage>,
    val temperature: Double,
    val response_format: ResponseFormat,
)

@Serializable
data class ResponseFormat(
    val type: String,
)

@Serializable
data class RequestMessage(
    val role: String,
    val content: String,
)

@Serializable
data class ChatCompletionsResponse(
    val id: String,
    val `object`: String,
    val created: Int,
    val model: String,
    val usage: Usage,
    val choices: List<Choice>,
)

@Serializable
data class Choice(
    val message: RequestMessage,
    val finish_reason: String,
    val index: Int,
)

@Serializable
data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int,
)