package com.jetbrains.kmpapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import cafe.adriel.voyager.navigator.Navigator
import com.jetbrains.kmpapp.data.ChatAPI
import com.jetbrains.kmpapp.data.ChatAPIImpl
import com.jetbrains.kmpapp.data.Message
import com.jetbrains.kmpapp.screens.list.ListScreen
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.random.Random

@Composable
fun App() {
    val chatAPI: ChatAPI = remember {
        ChatAPIImpl(
            HttpClient {
                install(ContentNegotiation) {
                    json(Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    })
                }
                install(Auth) {
                    bearer {
                        loadTokens {
                            BearerTokens("", "")
                        }
                    }
                }
            }
        )
    }
    var messages by remember { mutableStateOf(emptyList<Message>()) }
    var message by remember { mutableStateOf(TextFieldValue("")) }
    val sendMessage: suspend () -> Unit = {
        messages = messages + Message(
            author = "user",
            body = message.text
        )
        message = TextFieldValue("")
        delay(1000)
        val systemMessage = chatAPI.sendMessage(messages)
        messages = messages + systemMessage
    }
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(messages) { message ->
                MessageCard(message)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = message,
                onValueChange = {
                    message = it
                },
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick = {
                    coroutineScope.launch {
                        sendMessage()
                    }
                },
            ) {
                Text("Enviar")
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MessageCard(message: Message) {
    ListItem(
        icon = { Icon(Icons.Default.Person, contentDescription = null) },
        text = { Text(message.author) },
        secondaryText = { Text(message.body) },
    )
}

@Composable
@Preview
fun MessageCardPreview() {
    MessageCard(Message("", ""))
}