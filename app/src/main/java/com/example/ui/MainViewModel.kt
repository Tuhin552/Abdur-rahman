package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BmiEntry
import com.example.data.BmiRepository
import com.example.data.User
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BmiRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = BmiRepository(database.userDao(), database.bmiEntryDao())
    }

    // Auth State
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _bmiEntries = MutableStateFlow<List<BmiEntry>>(emptyList())
    val bmiEntries: StateFlow<List<BmiEntry>> = _bmiEntries.asStateFlow()

    private var entriesJob: Job? = null
    private var userObserverJob: Job? = null

    // Theme state: "system", "light", "dark"
    private val _themeMode = MutableStateFlow("system")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    // UI Navigation State
    private val _activeTab = MutableStateFlow("dashboard")
    val activeTab: StateFlow<String> = _activeTab.asStateFlow()

    // Auth Form Feedback
    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _registerError = MutableStateFlow<String?>(null)
    val registerError: StateFlow<String?> = _registerError.asStateFlow()

    private val _isRegisterSuccess = MutableStateFlow(false)
    val isRegisterSuccess: StateFlow<Boolean> = _isRegisterSuccess.asStateFlow()

    fun selectTab(tab: String) {
        _activeTab.value = tab
    }

    fun setTheme(mode: String) {
        _themeMode.value = mode
    }

    private fun startObservingUserData(username: String) {
        entriesJob?.cancel()
        userObserverJob?.cancel()

        // Observe BMI entries
        entriesJob = viewModelScope.launch {
            repository.getEntriesForUser(username).collect {
                _bmiEntries.value = it
            }
        }

        // Observe Live User profile updates (goals, heights, etc.)
        userObserverJob = viewModelScope.launch {
            repository.observeUser(username).collect { updatedUser ->
                if (updatedUser != null) {
                    _currentUser.value = updatedUser
                }
            }
        }
    }

    fun login(username: CharSequence, password: CharSequence) {
        _loginError.value = null
        if (username.isBlank() || password.isBlank()) {
            _loginError.value = "Please enter both username and password."
            return
        }

        viewModelScope.launch {
            val user = repository.authenticate(username.toString(), password)
            if (user != null) {
                _currentUser.value = user
                startObservingUserData(user.username)
                _loginError.value = null
                _activeTab.value = "dashboard"
            } else {
                _loginError.value = "Invalid username or password."
            }
        }
    }

    fun register(username: CharSequence, password: CharSequence, heightFt: Int, heightIn: Int) {
        _registerError.value = null
        _isRegisterSuccess.value = false

        val userStr = username.toString().trim().lowercase()
        if (userStr.length < 3) {
            _registerError.value = "Username must be at least 3 characters."
            return
        }
        if (password.length < 4) {
            _registerError.value = "Password must be at least 4 characters."
            return
        }
        if (heightFt <= 0) {
            _registerError.value = "Please enter a valid height in feet."
            return
        }

        viewModelScope.launch {
            val success = repository.register(userStr, password, heightFt, heightIn)
            if (success) {
                _isRegisterSuccess.value = true
                _registerError.value = null
                // Automatically log them in
                login(userStr, password)
            } else {
                _registerError.value = "Username is already taken."
            }
        }
    }

    fun logout() {
        entriesJob?.cancel()
        userObserverJob?.cancel()
        _currentUser.value = null
        _bmiEntries.value = emptyList()
        _loginError.value = null
        _registerError.value = null
        _isRegisterSuccess.value = false
        _activeTab.value = "dashboard"
    }

    // Profile updates
    fun updateProfile(heightFt: Int, heightIn: Int) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.updateUserProfile(user.username, heightFt, heightIn)
        }
    }

    fun updateGoals(targetWeight: Double?, targetBmi: Double?) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.updateUserTargets(user.username, targetWeight, targetBmi)
        }
    }

    // Log weight
    fun addBmiEntry(weightKg: Double, heightFt: Int, heightIn: Int, note: String?) {
        val user = _currentUser.value ?: return
        
        // Calculate BMI: BMI = weight (kg) / height (m)^2
        // height in inches = ft * 12 + in
        // height in meters = inches * 0.0254
        val heightInches = (heightFt * 12 + heightIn)
        val heightMeters = heightInches * 0.0254
        val bmi = if (heightMeters > 0.0) {
            weightKg / (heightMeters * heightMeters)
        } else {
            0.0
        }

        viewModelScope.launch {
            val entry = BmiEntry(
                username = user.username,
                weightKg = weightKg,
                heightFt = heightFt,
                heightIn = heightIn,
                bmi = bmi,
                timestamp = System.currentTimeMillis(),
                note = note
            )
            repository.insertEntry(entry)
        }
    }

    fun deleteBmiEntry(entry: BmiEntry) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
        }
    }

    fun deleteBmiEntryById(id: Int) {
        viewModelScope.launch {
            repository.deleteEntryById(id)
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
