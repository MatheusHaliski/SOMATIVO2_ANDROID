package com.example.testjetpackcompose

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Usuario::class, Pergunta::class, UserScore::class], version = 8)
@TypeConverters(Converters::class)  // Adicione isso para usar o TypeConverter
abstract class QuizDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun perguntaDao(): PerguntaDao
    abstract fun userScoreDao(): UserScoreDao

    companion object {
        @Volatile private var INSTANCE: QuizDatabase? = null

        fun getDatabase(context: Context): QuizDatabase { // Alterado para Context
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuizDatabase::class.java,
                    "quiz_database"
                )
                    .fallbackToDestructiveMigration() // Isso permite a migração destrutiva
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
