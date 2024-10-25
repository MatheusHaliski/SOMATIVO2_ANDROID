package com.example.testjetpackcompose

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.media3.exoplayer.ExoPlayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.ui.PlayerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.delay
import com.squareup.picasso.Picasso

@Composable
fun VideoScreen(navController: NavHostController, usuario: String) {
    val context = LocalContext.current
    val gifUri = "android.resource://${context.packageName}/raw/icons8" // Nome do seu GIF sem extensão

    // Exibir o GIF e o texto "Loading..." em uma coluna
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .wrapContentSize(Alignment.Center) // Centraliza a coluna
    ) {
        // Exibir o GIF
        AndroidView(
            factory = { ImageView(context).apply {
                Glide.with(context)
                    .asGif() // Importante: usar asGif() para garantir que o Glide trate como GIF
                    .load(gifUri)
                    .override(90, 90) // Define a largura e altura do GIF (ajuste conforme necessário)
                    .into(this)
            }},
            modifier = Modifier.size(100.dp) // Define o tamanho do AndroidView
        )

        // Exibir o texto "Loading..." abaixo do GIF
        Text(
            text = "Loading...",
            fontSize = 24.sp, // Aumenta o tamanho da fonte
            modifier = Modifier.padding(top = 16.dp), // Adiciona um espaçamento entre o GIF e o texto
            textAlign = TextAlign.Center // Centraliza o texto
        )
    }

    // Usar LaunchedEffect para atrasar a navegação
    LaunchedEffect(Unit) {
        // Atrasar por 3 segundos (3000 milissegundos)
        kotlinx.coroutines.delay(1500)
        // Redirecionar após o atraso
        navController.navigate("quiz/$usuario")
    }
}

@Composable
fun CadastroScreen(navController: NavHostController, usuarioDao: UsuarioDao, usuarioscoreDao: UserScoreDao) {
    val nome = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    var isTextVisible by remember { mutableStateOf(false) }

    // Definindo as cores para a transição
    val lightBlue = Color(173, 216, 230)  // Azul claro
    val skinTone = Color(255, 224, 189)   // Cor de pele

    // Definindo as cores para a transição do texto
    val blueText = Color(0, 0, 255)       // Azul
    val darkGrayText = Color(64, 64, 64)  // Cinza escuro

    // Animação de transição de cores com InfiniteTransition
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val backgroundColor by infiniteTransition.animateColor(
        initialValue = lightBlue,
        targetValue = skinTone,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = 3000), // Tempo para transição entre as cores
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    // Animação para o texto "Bem-Vindo ao IQ Quizz!"
    val textColor by infiniteTransition.animateColor(
        initialValue = blueText,
        targetValue = darkGrayText,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = 3000),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    LaunchedEffect(Unit) {
        isTextVisible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor) // Aplicando a cor animada ao fundo
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animação do texto "Bem-vindo ao IQ Quizz!" com fonte aumentada e cor
        AnimatedVisibility(
            visible = isTextVisible,
            enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(initialOffsetY = { -70 }, animationSpec = tween(1000))
        ) {
            Text(
                text = "Bem-vindo ao IQ Quizz!",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold, // Negrito
                color = textColor
            )
        }

        OutlinedTextField(
            value = nome.value,
            onValueChange = { nome.value = it },
            label = { Text("Nome") }
        )
        OutlinedTextField(
            value = email.value,
            onValueChange = { email.value = it },
            label = { Text("Email") }
        )

        // Botão de Iniciar Quiz
        Button(onClick = {
            if (nome.value.isNotEmpty() && email.value.isNotEmpty()) {
                coroutineScope.launch(Dispatchers.IO) {
                    val usuarioId = usuarioDao.inserirUsuario(Usuario(nome = nome.value, email = email.value)).toString()
                    val usuario = nome.value
                    withContext(Dispatchers.Main) {
                        // Navega para a tela de vídeo, passando o nome do usuário
                        navController.navigate("video/$usuario")
                    }

                }
            }
        }) {
            Text("Iniciar Quiz")
        }

        // Botão para ver Scores
        Button(onClick = {
            navController.navigate("resultado2")
        }) {
            Text("Ver Scores")
        }

        // Botão para limpar banco de dados
        Button(onClick = {
            coroutineScope.launch(Dispatchers.IO) {
                usuarioscoreDao.deleteAllScores()
                withContext(Dispatchers.Main) {
                    // Feedback ao usuário, se necessário
                }
            }
        }) {
            Text("Limpar Banco de Dados")
        }
    }
}
