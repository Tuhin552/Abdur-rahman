package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BmiRepository(
    private val userDao: UserDao,
    private val bmiEntryDao: BmiEntryDao
) {
    suspend fun authenticate(username: String, password: CharSequence): User? = withContext(Dispatchers.IO) {
        val normalizedUsername = username.trim().lowercase()
        val user = userDao.getUserByUsername(normalizedUsername) ?: return@withContext null
        val computedHash = HashUtils.hashPassword(password.toString(), user.salt)
        if (computedHash == user.passwordHash) user else null
    }

    suspend fun register(username: String, password: CharSequence, heightFt: Int, heightIn: Int): Boolean = withContext(Dispatchers.IO) {
        val normalizedUsername = username.trim().lowercase()
        if (normalizedUsername.isEmpty() || password.isEmpty()) return@withContext false
        val existing = userDao.getUserByUsername(normalizedUsername)
        if (existing != null) return@withContext false

        val salt = HashUtils.generateSalt()
        val hash = HashUtils.hashPassword(password.toString(), salt)
        val newUser = User(
            username = normalizedUsername,
            passwordHash = hash,
            salt = salt,
            heightFt = heightFt,
            heightIn = heightIn
        )
        userDao.insertUser(newUser)
        true
    }

    suspend fun updateUserTargets(username: String, targetWeight: Double?, targetBmi: Double?) = withContext(Dispatchers.IO) {
        val user = userDao.getUserByUsername(username) ?: return@withContext
        val updatedUser = user.copy(targetWeight = targetWeight, targetBmi = targetBmi)
        userDao.updateUser(updatedUser)
    }

    suspend fun updateUserProfile(username: String, heightFt: Int, heightIn: Int) = withContext(Dispatchers.IO) {
        val user = userDao.getUserByUsername(username) ?: return@withContext
        val updatedUser = user.copy(heightFt = heightFt, heightIn = heightIn)
        userDao.updateUser(updatedUser)
    }

    fun observeUser(username: String): Flow<User?> {
        return userDao.observeUser(username)
    }

    fun getEntriesForUser(username: String): Flow<List<BmiEntry>> {
        return bmiEntryDao.getEntriesForUser(username)
    }

    suspend fun insertEntry(entry: BmiEntry) = withContext(Dispatchers.IO) {
        bmiEntryDao.insertEntry(entry)
    }

    suspend fun deleteEntry(entry: BmiEntry) = withContext(Dispatchers.IO) {
        bmiEntryDao.deleteEntry(entry)
    }

    suspend fun deleteEntryById(id: Int) = withContext(Dispatchers.IO) {
        bmiEntryDao.deleteEntryById(id)
    }
}
