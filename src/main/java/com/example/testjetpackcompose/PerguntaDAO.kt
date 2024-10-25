package com.example.testjetpackcompose

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PerguntaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirPerguntas(perguntas: List<Pergunta>)

    @Query("SELECT * FROM pergunta")
    suspend fun getAllPerguntas(): List<Pergunta>
}
