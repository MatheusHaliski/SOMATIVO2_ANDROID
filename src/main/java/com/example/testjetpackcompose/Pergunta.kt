package com.example.testjetpackcompose

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pergunta")
data class Pergunta(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val texto: String,
    val opcoes: List<String>,
    val respostaCorreta: String // índice da opção correta
)
