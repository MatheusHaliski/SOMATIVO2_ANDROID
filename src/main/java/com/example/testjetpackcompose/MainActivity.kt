package com.example.testjetpackcompose

import ResultadoScreen
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.content.MediaType.Companion.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bumptech.glide.Glide
import kotlinx.coroutines.delay
import java.util.*


class MainActivity : ComponentActivity() {
    private lateinit var textToSpeech: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = QuizDatabase.getDatabase(this)
        val usuarioDao = database.usuarioDao()
        val perguntaDao = database.perguntaDao()
        val userScoreDao = database.userScoreDao()

        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale("pt", "BR")
            }
        }

        setContent {
            QuizAppNavHost(
                navController = rememberNavController(),
                usuarioDao = usuarioDao,
                perguntaDao = perguntaDao,
                userScoreDao = userScoreDao,
                textToSpeech = textToSpeech
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        textToSpeech.shutdown()
    }
}

@Composable
fun QuizAppNavHost(
    navController: NavHostController,
    usuarioDao: UsuarioDao,
    perguntaDao: PerguntaDao,
    userScoreDao: UserScoreDao,
    textToSpeech: TextToSpeech
) {
    NavHost(navController = navController, startDestination = "cadastro") {
        composable("cadastro") {
            CadastroScreen(navController, usuarioDao, userScoreDao)
        }
        composable("quiz/{usuarioId}") { backStackEntry ->
            val usuarioId = backStackEntry.arguments?.getString("usuarioId") ?: "0"
            QuizScreen(navController, usuarioId, perguntaDao, userScoreDao, textToSpeech)
        }
        composable("resultado/{score}/{usuarioId}") { backStackEntry ->
            val score = backStackEntry.arguments?.getString("score")?.toInt() ?: 0
            val usuarioId = backStackEntry.arguments?.getString("usuarioId") ?: "0"
            ResultadoScreen(navController)
        }
        composable("resultado2") {
            ResultadoScreen(navController)
        }
        composable("video/{usuarioId}") { backStackEntry ->
            val usuarioId = backStackEntry.arguments?.getString("usuarioId") ?: "0"
            VideoScreen(navController, usuarioId)
        }
        composable("score/{score}/{totalQuestions}/{usuarioId}/{acerto}") { backStackEntry ->
            val score = backStackEntry.arguments?.getString("score")?.toInt() ?: 0
            val acertou = backStackEntry.arguments?.getString("acerto")?.toInt() ?: 0
            val totalQuestions = backStackEntry.arguments?.getString("totalQuestions")?.toInt() ?: 0
            val usuarioId = backStackEntry.arguments?.getString("usuarioId") ?: "0"
            ScoreScreen(navController, score, totalQuestions, textToSpeech, usuarioId,acertou)
        }
    }
}

@Composable
fun QuizScreen(
    navController: NavHostController,
    usuarioId: String,
    perguntaDao: PerguntaDao,
    userScoreDao: UserScoreDao,
    textToSpeech: TextToSpeech
) {
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var acerto by remember { mutableIntStateOf(0) }
    var isVisible by remember { mutableStateOf(true) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var showCorrectAnswer by remember { mutableStateOf(false) }
    var showFinalScore by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableStateOf(20) }
    val viewModel: QuizViewModel = viewModel()
    var clickTime by remember { mutableStateOf(0) }

    val perguntas = listOf(
        Pergunta(
            1, "Ana tem 10 balas e come 3. Em seguida, ganha mais 5 balas. Quantas balas Ana tem agora?",
            listOf("10", "11", "12", "13"), "12"
        ),
        Pergunta(
            2, "Um fazendeiro tem 8 galinhas e cada galinha põe 2 ovos por dia. Quantos ovos o fazendeiro recolhe em um dia?",
            listOf("10", "12", "14", "16"), "16"
        ),
        Pergunta(
            3, "João compra um pacote com 12 bombons e decide dividir igualmente com seu amigo. Quantos bombons cada um recebe?",
            listOf("4", "5", "6", "8"), "6"
        ),
        Pergunta(
            4, "Qual é o próximo número na sequência: 1, 3, 6, 10, ...?",
            listOf("12", "13", "14", "15"), "15"
        ),
        Pergunta(
            5, "Carlos tem 15 livros e decide doar 5 para sua amiga. Quantos livros ele ainda tem?",
            listOf("8", "9", "10", "11"), "10"
        ),
        Pergunta(
            6, "O dobro de um número somado com 7 resulta em 19. Qual é o número?",
            listOf("3", "4", "5", "6"), "6"
        ),
        Pergunta(
            7, "Se o dobro de um número é 24 e a sua metade é 6, qual é esse número?",
            listOf("8", "10", "12", "14"), "12"
        ),
        Pergunta(
            8, "Em uma biblioteca há 50 livros. Se 20% são de ficção, quantos livros de ficção existem?",
            listOf("5", "10", "15", "20"), "10"
        ),
        Pergunta(
            9, "Uma caneta custa R$2, e uma borracha custa metade do preço da caneta. Quantas borrachas podem ser compradas com R$10?",
            listOf("5", "6", "7", "10"), "10"
        ),
        Pergunta(
            10, "Uma turma de alunos é dividida em grupos de 5 pessoas. Se há 4 grupos completos e sobram 3 alunos, quantos alunos há no total?",
            listOf("20", "22", "23", "25"), "23"
        ),
        Pergunta(
            11, "Em uma caixa, há o dobro de bolinhas verdes do que azuis e o triplo de bolinhas vermelhas do que azuis. Se há 10 bolinhas azuis, quantas bolinhas há ao todo?",
            listOf("40", "50", "60", "70"), "60"
        ),
        Pergunta(
            12, "Um trem viaja a uma velocidade de 60 km/h e leva 1 hora e 30 minutos para chegar ao destino. Qual é a distância percorrida pelo trem?",
            listOf("75 km", "80 km", "85 km", "90 km"), "90 km"
        ),
        Pergunta(
            13, "Ana, Bruno e Carlos juntos têm 45 doces. Ana tem o dobro dos doces de Bruno, e Carlos tem 5 a menos que Ana. Quantos doces tem Bruno?",
            listOf("10", "11", "12", "13"), "10"
        ),
        Pergunta(
            14, "Uma piscina é preenchida com água a uma taxa de 3 litros por minuto. Se a piscina tem capacidade para 450 litros, quantos minutos leva para enchê-la até 75% de sua capacidade?",
            listOf("75", "100", "112", "150"), "112"
        ),
        Pergunta(
            15, "Em uma corrida, há 5 competidores. Cada competidor termina a corrida com uma diferença de 2 minutos do próximo. Se o último termina em 30 minutos, quanto tempo levou o primeiro competidor?",
            listOf("20", "22", "24", "26"), "22"
        )
    )


    val totalQuestions = perguntas.size

    if (currentQuestionIndex < totalQuestions) {
        val perguntaAtual = perguntas[currentQuestionIndex]
        LaunchedEffect(perguntaAtual) {
            delay(1500)
            SpeakOut2(textToSpeech, perguntaAtual.texto)
            delay(6000)
            SpeakOut2(textToSpeech, perguntaAtual.opcoes.toString())
        }
        // Contador de tempo
        LaunchedEffect(timeLeft) {
            if (timeLeft > 0) {
                delay(1000)
                timeLeft--
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val context = LocalContext.current
                        val gifUri = "android.resource://${context.packageName}/raw/timing"
                        // Exibir o GIF
                        AndroidView(
                            factory = { ImageView(context).apply {
                                Glide.with(context)
                                    .asGif() // Importante: usar asGif() para garantir que o Glide trate como GIF
                                    .load(gifUri)
                                    .override(100, 100) // Define a largura e altura do GIF (ajuste conforme necessário)
                                    .into(this)
                            }},
                            modifier = Modifier.size(100.dp) // Define o tamanho do AndroidView
                        )

                        Text(
                            text = "Tempo restante: $timeLeft",
                            fontSize = 18.sp,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }

                    Text(
                        text = perguntaAtual.texto,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    perguntaAtual.opcoes.forEach { opcao ->
                        val buttonColor = when {
                            selectedOption == opcao && opcao == perguntaAtual.respostaCorreta -> Color.Green
                            selectedOption == opcao && opcao != perguntaAtual.respostaCorreta -> Color.Red
                            else -> Color.Gray
                        }

                        Button(
                            onClick = {
                                if (!showCorrectAnswer) {
                                    selectedOption = opcao
                                    clickTime = timeLeft // Captura o tempo em que o usuário clicou
                                    showCorrectAnswer = true
                                    isVisible = false

                                    // Aumenta a pontuação com base no tempo de clique

                                        if((clickTime in 13..20)&&(opcao == perguntaAtual.respostaCorreta)) {
                                            score += 120 // Aumenta em 40%
                                            acerto++
                                        }
                                        else if((clickTime in 6..12)&&(opcao == perguntaAtual.respostaCorreta)) {
                                            score +=80// Aumenta em 20%
                                            acerto++
                                        }
                                        else if((clickTime in 1..5)&&(opcao == perguntaAtual.respostaCorreta)){
                                            score +=40// Aumenta em 20%
                                            acerto++
                                        }
                                        else {
                                            score += 0 // Não aumenta
                                        }

                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .height(60.dp)
                                .background(buttonColor, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text(
                                text = opcao,
                                fontSize = 20.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = showCorrectAnswer,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                val userResponse = selectedOption ?: "Nenhuma resposta"
                val correctResponse = perguntaAtual.respostaCorreta

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(
                            if (userResponse == correctResponse &&(timeLeft!=0)) Color.Green else Color.Gray,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        Text(
                            text = when {
                                timeLeft == 0 -> "FIM DO TEMPO!"
                                userResponse == correctResponse -> "ACERTOU!"
                                else -> "ERROU..."
                            },
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (userResponse == correctResponse &&(timeLeft!=0)) Color.Gray else Color.White,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Text(
                            text = "Sua resposta: $userResponse",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (userResponse == correctResponse &&(timeLeft!=0)) Color.Gray else Color.White,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = "Resposta correta: $correctResponse",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (userResponse == correctResponse &&(timeLeft!=0)) Color.Gray else Color.White,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Button(
                            onClick = {
                                if (currentQuestionIndex + 1 < totalQuestions) {
                                    currentQuestionIndex++
                                    selectedOption = null
                                    showCorrectAnswer = false
                                    isVisible = true
                                    timeLeft = 20 // Reinicia o tempo
                                } else {
                                    showFinalScore = true
                                    viewModel.saveUserScore(usuarioId, score)
                                    navController.navigate("score/$score/$totalQuestions/$usuarioId/$acerto")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .height(60.dp)
                                .background(Color.Blue, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text(
                                text = "Próxima Pergunta",
                                fontSize = 20.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreScreen(
    navController: NavHostController,
    score: Int,
    totalQuestions: Int,
    textToSpeech: TextToSpeech,
    usuarioId: String,
    acertou: Int
) {
    LaunchedEffect(Unit) {
        val message = "Você obteve $score pontos."
        SpeakOut2(textToSpeech, message)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Blue)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Pontuação Final",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Você acertou $acertou de $totalQuestions perguntas.",
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Você pontuou $score.",
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                navController.navigate("resultado/$score/$usuarioId")
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(60.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Ir para o Dashboard",
                fontSize = 20.sp,
                color = Color.White
            )
        }
    }
}

fun SpeakOut2(textToSpeech: TextToSpeech, text: String) {
    textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
}
