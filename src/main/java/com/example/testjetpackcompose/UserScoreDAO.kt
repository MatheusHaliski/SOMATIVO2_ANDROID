package com.example.testjetpackcompose

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserScoreDao {
    @Insert
    suspend fun inserirScore(userScore: UserScore)

    @Query("SELECT DISTINCT * FROM UserScores ORDER BY score DESC LIMIT 10")
     fun getTopScores(): LiveData<List<UserScore>>

    @Query("DELETE FROM UserScores")
    suspend fun deleteAllScores()

    @Query("""
    SELECT id,usuarioId, MAX(score) as score 
    FROM UserScores 
    GROUP BY usuarioId 
    ORDER BY score DESC 
    LIMIT 10
""")
    fun getTopUniqueScores(): LiveData<List<UserScore>>

}
