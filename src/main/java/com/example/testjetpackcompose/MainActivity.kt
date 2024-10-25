package com.example.testjetpackcompose

import ResultadoScreen
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = QuizDatabase.getDatabase(this)
        val usuarioDao = database.usuarioDao()
        val perguntaDao = database.perguntaDao()
        val userScoreDao = database.userScoreDao()

        setContent {
            QuizAppNavHost(
                navController = rememberNavController(),
                usuarioDao = usuarioDao,
                perguntaDao = perguntaDao,
                userScoreDao = userScoreDao
            )
        }
    }
}

@Composable
fun QuizAppNavHost(
    navController: NavHostController,
    usuarioDao: UsuarioDao,
    perguntaDao: PerguntaDao,
    userScoreDao: UserScoreDao
) {
    NavHost(navController = navController, startDestination = "cadastro") {
        composable("cadastro") {
            CadastroScreen(navController, usuarioDao, userScoreDao)
        }
        composable("quiz/{usuarioId}") { backStackEntry ->
            val usuarioId = backStackEntry.arguments?.getString("usuarioId") ?: "0"
            QuizScreen(navController, usuarioId, perguntaDao, userScoreDao)
        }
        composable("resultado/{score}/{usuarioId}") { backStackEntry ->
            val score = backStackEntry.arguments?.getString("score")?.toInt() ?: 0
            val usuarioId = backStackEntry.arguments?.getString("usuarioId") ?: "0"
            val viewModel: QuizViewModel = viewModel()
            viewModel.saveUserScore(usuarioId, score)
            ResultadoScreen(navController)
        }
        composable("resultado2") {
            ResultadoScreen(navController)
        }
        composable("video/{usuarioId}") { backStackEntry ->
            val usuarioId = backStackEntry.arguments?.getString("usuarioId") ?: "0"
            VideoScreen(navController, usuarioId) // Adicione a função VideoScreen, se necessário
        }
    }
}




@Composable
fun QuizScreen(navController: NavHostController, usuarioId: String, perguntaDao: PerguntaDao, userScoreDao: UserScoreDao) {
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var isVisible by remember { mutableStateOf(true) } // Controle da visibilidade para animação

    val perguntas = listOf(
        // Nível Básico (1-4)
        Pergunta(1, "Se Maria tem 5 maçãs e dá 2 para João, quantas maçãs ela terá?", listOf("1", "2", "3", "4"), "3"),
        Pergunta(2, "Qual é o próximo número da sequência: 2, 4, 6, 8, ...?", listOf("10", "12", "14", "16"), "10"),
        Pergunta(3, "Se um triângulo tem 3 lados e um quadrado tem 4, quantos lados tem 2 triângulos e 1 quadrado juntos?", listOf("10", "11", "12", "13"), "10"),
        Pergunta(4, "Se hoje é segunda-feira, que dia será daqui a 4 dias?", listOf("Quinta-feira", "Sexta-feira", "Sábado", "Domingo"), "Sexta-feira"),

        // Nível Moderado (5-7)
        Pergunta(5, "João está 4º na fila, e Pedro está 3 lugares atrás dele. Que posição está Pedro?", listOf("5º", "6º", "7º", "8º"), "7º"),
        Pergunta(6, "Se uma camisa custa R$50 e está com 20% de desconto, quanto você pagaria pela camisa?", listOf("R$30", "R$35", "R$40", "R$45"), "R$40"),
        Pergunta(7, "Uma loja vende 5 camisetas por R$100. Qual o preço de 9 camisetas?", listOf("R$150", "R$180", "R$200", "R$220"), "R$180"),

        // Nível Avançado (8-10)
        Pergunta(8, "Qual é o número que, dividido por 3, somado com 4 e multiplicado por 2, resulta em 16?", listOf("1", "2", "3", "4"), "2"),
        Pergunta(9, "Uma sala tem 10 lâmpadas. Se apago metade e ligo novamente 3 lâmpadas, quantas lâmpadas estão acesas?", listOf("3", "5", "7", "8"), "8"),
        Pergunta(10, "A soma de dois números é 15, e sua diferença é 5. Quais são os números?", listOf("5 e 10", "6 e 9", "7 e 8", "4 e 11"), "5 e 10")
    )

    val totalQuestions = perguntas.size

    if (currentQuestionIndex < totalQuestions) {
        val perguntaAtual = perguntas[currentQuestionIndex]

        // Animação para a transição das perguntas
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(), // Animação de fadeIn na entrada
            exit = fadeOut()  // Animação de fadeOut na saída
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Pergunta com estilo ampliado
                Text(
                    text = perguntaAtual.texto,
                    fontSize = 28.sp,  // Aumentando o tamanho da fonte
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))  // Espaço entre pergunta e opções

                // Exibição das opções de resposta
                perguntaAtual.opcoes.forEach { opcao ->
                    Button(
                        onClick = {
                            isVisible = false // Iniciar a animação de saída
                            if (opcao == perguntaAtual.respostaCorreta) {
                                score++
                            }
                            currentQuestionIndex++
                            if (currentQuestionIndex < totalQuestions) {
                                isVisible = true // Mostrar a próxima pergunta
                            } else {
                                navController.navigate("resultado/$score/$usuarioId")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .height(60.dp),  // Aumentando o tamanho dos botões
                        shape = RoundedCornerShape(12.dp),  // Canto arredondado
                    ) {
                        Text(
                            text = opcao,
                            fontSize = 20.sp,  // Aumentando o tamanho da fonte das opções
                        )
                    }
                }
            }
        }
    }
}
