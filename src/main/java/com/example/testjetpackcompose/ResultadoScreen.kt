import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.testjetpackcompose.QuizViewModel

@Composable
fun ResultadoScreen(navController: NavHostController) {
    val viewModel: QuizViewModel = viewModel()

    // Observe os scores
    val scores by viewModel.topScores.observeAsState(emptyList())
    var isTableVisible by remember { mutableStateOf(false) } // Para controlar a visibilidade da tabela

    // Ativa a animação ao carregar a tela
    LaunchedEffect(Unit) {
        isTableVisible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Resultados", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        // Tabela com cabeçalhos e animação de entrada
        AnimatedVisibility(
            visible = isTableVisible,
            enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(initialOffsetY = { -40 }, animationSpec = tween(1000))
        ) {
            if (scores.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp)
                        .background(Color(0xFFF0F0F0)) // Cor de fundo suave para a tabela
                        .border(BorderStroke(2.dp, Color(0xFF6200EE)), shape = RoundedCornerShape(10.dp)) // Borda com cantos arredondados
                        .padding(8.dp) // Preenchimento dentro da borda
                ) {
                    item {
                        // Cabeçalho da tabela
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF6200EE)) // Cor de fundo do cabeçalho
                                .padding(vertical = 8.dp)
                                .border(1.dp, Color(0xFF3700B3)), // Borda azul escuro
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Nome",
                                fontSize = 20.sp,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(8.dp),
                                fontWeight = FontWeight.Bold,
                                color = Color.White // Texto branco para destacar no fundo escuro
                            )
                            Text(
                                text = "Score",
                                fontSize = 20.sp,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(8.dp),
                                fontWeight = FontWeight.Bold,
                                color = Color.White // Texto branco
                            )
                        }
                    }
                    items(scores) { userScore ->
                        // Linhas da tabela com alternância de cores
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (userScore.score % 2 == 0) Color(0xFFF0F0F0) else Color(0xFFE0E0E0)) // Alterna as cores das linhas
                                .border(1.dp, Color(0xFF3700B3)) // Borda azul escuro
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = userScore.usuarioId,
                                fontSize = 20.sp,
                                modifier = Modifier.weight(1f).padding(8.dp)
                            )
                            Text(
                                text = userScore.score.toString(),
                                fontSize = 20.sp,
                                modifier = Modifier.weight(1f).padding(8.dp)
                            )
                        }
                    }
                }
            } else {
                // Mostra uma mensagem se não houver scores
                Text(text = "Não há resultados...", fontSize = 16.sp)
            }
        }
    }
}
