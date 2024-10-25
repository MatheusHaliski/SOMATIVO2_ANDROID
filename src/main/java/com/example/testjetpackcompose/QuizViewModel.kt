package com.example.testjetpackcompose

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.util.concurrent.Service
import kotlinx.coroutines.launch

class QuizViewModel(application: Application) : AndroidViewModel(application) {

    private val userScoreDao = QuizDatabase.getDatabase(application).userScoreDao()

    // LiveData que expõe a lista de pontuações
    val topScores: LiveData<List<UserScore>> = userScoreDao.getTopUniqueScores() // Observe diretamente do DAO

    // Função para salvar o score
    fun saveUserScore(usuarioId: String, score: Int) {
        viewModelScope.launch {
            val userScore = UserScore(usuarioId = usuarioId, score = score)
            userScoreDao.inserirScore(userScore)
        }
    }
    private val _clearDatabaseResult = mutableStateOf(false)
    val clearDatabaseResult: Boolean get() = _clearDatabaseResult.value // Use um getter para expor o estado

    // Método para limpar todas as tabelas
    fun clearDatabase() {
        viewModelScope.launch {
            userScoreDao.deleteAllScores()
            _clearDatabaseResult.value = true // Atualiza o resultado para indicar que a limpeza foi feita
        }
    }
}
