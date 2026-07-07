package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE username = :username")
    fun observeUser(username: String): Flow<User?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)
}

@Dao
interface BmiEntryDao {
    @Query("SELECT * FROM bmi_entries WHERE username = :username ORDER BY timestamp DESC")
    fun getEntriesForUser(username: String): Flow<List<BmiEntry>>

    @Query("SELECT * FROM bmi_entries WHERE username = :username ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestEntryForUser(username: String): BmiEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: BmiEntry)

    @Delete
    suspend fun deleteEntry(entry: BmiEntry)

    @Query("DELETE FROM bmi_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Int)
}
