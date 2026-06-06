package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Profile
import com.example.data.model.ProgressLog
import com.example.data.repository.MathQuestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

// Sealed class representing type-safe custom offline screens
sealed interface QuestScreen {
    object ProfileSelection : QuestScreen
    object Home : QuestScreen
    class ActiveAdventure(val questType: MathQuestCategory) : QuestScreen
    object StickerShop : QuestScreen
    object ParentGate : QuestScreen
    object ParentDashboard : QuestScreen
}

// Categories of mathematical challenges
enum class MathQuestCategory {
    ADDITION,
    SUBTRACTION,
    MULTIPLICATION,
    DIVISION
}

// Representation of a single interactive math adventure quiz item
data class MathQuizItem(
    val num1: Int,
    val num2: Int,
    val operation: String, // "+", "-", "*", "/"
    val correctAnswer: Int,
    val options: List<Int>
)

// Representation of collectible virtual sticker item in the toy chest
data class VirtualToySticker(
    val id: String,
    val labelKey: String, // translation key or name
    val emoji: String, // Visual kid design asset
    val cost: Int
)

class MathQuestViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MathQuestRepository

    // Reactive streams from database
    val allProfiles: StateFlow<List<Profile>>

    private val _activeProfile = MutableStateFlow<Profile?>(null)
    val activeProfile: StateFlow<Profile?> = _activeProfile.asStateFlow()

    private val _currentScreen = MutableStateFlow<QuestScreen>(QuestScreen.ProfileSelection)
    val currentScreen: StateFlow<QuestScreen> = _currentScreen.asStateFlow()

    // Navigation Backstack standard memory helper
    private val screenStack = mutableListOf<QuestScreen>()

    // Current Math Quiz States
    var currentQuestions = mutableStateOf<List<MathQuizItem>>(emptyList())
    var currentQuestionIndex = mutableStateOf(0)
    var correctAnswersInSession = mutableStateOf(0)
    var activeQuestCoinsEarned = mutableStateOf(0)
    var showQuizSuccessOverlay = mutableStateOf(false)
    var showQuestCompletedSummary = mutableStateOf(false)
    var selectedAnswerState = mutableStateOf<Int?>(null) // null if unanswered or not validated
    var isAnswerCorrectState = mutableStateOf(false)

    // Parent Hub Entry math equation verification code lock
    var parentChallengeNum1 by mutableStateOf(0)
    var parentChallengeNum2 by mutableStateOf(0)
    var parentChallengeAnswer by mutableStateOf(0)
    var parentChallengeInput by mutableStateOf("")
    var parentChallengeErrorMessage by mutableStateOf("")

    // Parent Logs flow target
    private val _parentProgressLogs = MutableStateFlow<List<ProgressLog>>(emptyList())
    val parentProgressLogs: StateFlow<List<ProgressLog>> = _parentProgressLogs.asStateFlow()

    // List of premium visual collectible toys/stickers
    val stickerShopList = listOf(
        VirtualToySticker("toy_rocket", "toy_rocket", "🚀", 30),
        VirtualToySticker("toy_submarine", "toy_submarine", " submarine 🌊", 40),
        VirtualToySticker("toy_dino", "toy_dino", "🦖", 50),
        VirtualToySticker("toy_unicorn", "toy_unicorn", "🦄", 65),
        VirtualToySticker("toy_castle", "toy_castle", "🏰", 80),
        VirtualToySticker("toy_ufo", "toy_ufo", "🛸", 100),
        VirtualToySticker("toy_car", "toy_car", "🏎️", 45),
        VirtualToySticker("toy_balloon", "toy_balloon", "🎈", 25)
    )

    init {
        val database = AppDatabase.getDatabase(application)
        repository = MathQuestRepository(database.profileDao(), database.progressDao())

        allProfiles = repository.allProfilesFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        // Automatically load active profile
        viewModelScope.launch {
            val active = repository.getActiveProfile()
            if (active != null) {
                _activeProfile.value = active
                _currentScreen.value = QuestScreen.Home
            } else {
                _currentScreen.value = QuestScreen.ProfileSelection
            }
        }
    }

    // CUSTOM NAVIGATION HELPER
    fun navigateTo(screen: QuestScreen) {
        screenStack.add(_currentScreen.value)
        _currentScreen.value = screen
    }

    fun goBack() {
        if (screenStack.isNotEmpty()) {
            val prev = screenStack.removeAt(screenStack.size - 1)
            _currentScreen.value = prev
        } else {
            // Default escape
            _currentScreen.value = if (_activeProfile.value != null) QuestScreen.Home else QuestScreen.ProfileSelection
        }
    }

    // PROFILE ACCOUNT MANAGEMENT
    fun createAndSelectProfile(name: String, age: Int, avatar: String, difficulty: String, language: String) {
        viewModelScope.launch {
            val newProfile = Profile(
                name = name.takeIf { it.isNotBlank() } ?: "Hana",
                age = if (age > 0) age else 6,
                avatar = avatar,
                difficulty = difficulty,
                language = language,
                coins = 10, // Starting bonus
                isActive = true
            )
            repository.deactivateAll()
            val insertedId = repository.insertProfile(newProfile)
            val updatedProfile = repository.getProfileById(insertedId.toInt())
            _activeProfile.value = updatedProfile
            screenStack.clear()
            _currentScreen.value = QuestScreen.Home
        }
    }

    fun deleteProfile(profile: Profile) {
        viewModelScope.launch {
            repository.deleteProfile(profile)
            if (_activeProfile.value?.id == profile.id) {
                _activeProfile.value = null
                screenStack.clear()
                _currentScreen.value = QuestScreen.ProfileSelection
            }
        }
    }

    fun switchActiveProfile(profileId: Int) {
        viewModelScope.launch {
            repository.selectActiveProfile(profileId)
            val updated = repository.getProfileById(profileId)
            _activeProfile.value = updated
            screenStack.clear()
            _currentScreen.value = QuestScreen.Home
        }
    }

    fun updateLanguage(lang: String) {
        val current = _activeProfile.value ?: return
        viewModelScope.launch {
            val updated = current.copy(language = lang)
            repository.updateProfile(updated)
            _activeProfile.value = updated
        }
    }

    fun updateDifficulty(difficulty: String) {
        val current = _activeProfile.value ?: return
        viewModelScope.launch {
            val updated = current.copy(difficulty = difficulty)
            repository.updateProfile(updated)
            _activeProfile.value = updated
        }
    }

    // GENERATING ADVENTURE QUESTIONS
    fun startQuest(category: MathQuestCategory) {
        val currentProfile = _activeProfile.value ?: return
        val difficulty = currentProfile.difficulty // "EASY", "MEDIUM", "HARD"
        
        val questionsList = mutableListOf<MathQuizItem>()
        val totalQuestions = 5 // Standard length 5 questions per adventure

        for (i in 1..totalQuestions) {
            questionsList.add(generateSingleQuizz(category, difficulty))
        }

        currentQuestions.value = questionsList
        currentQuestionIndex.value = 0
        correctAnswersInSession.value = 0
        activeQuestCoinsEarned.value = 0
        showQuizSuccessOverlay.value = false
        showQuestCompletedSummary.value = false
        selectedAnswerState.value = null

        navigateTo(QuestScreen.ActiveAdventure(category))
    }

    private fun generateSingleQuizz(category: MathQuestCategory, difficulty: String): MathQuizItem {
        var num1 = 0
        var num2 = 0
        var correctAnswer = 0
        var operation = "+"

        when (category) {
            MathQuestCategory.ADDITION -> {
                operation = "+"
                when (difficulty) {
                    "EASY" -> {
                        num1 = Random.nextInt(1, 9)
                        num2 = Random.nextInt(1, 9)
                    }
                    "MEDIUM" -> {
                        num1 = Random.nextInt(5, 25)
                        num2 = Random.nextInt(5, 25)
                    }
                    else -> { // HARD
                        num1 = Random.nextInt(10, 55)
                        num2 = Random.nextInt(10, 45)
                    }
                }
                correctAnswer = num1 + num2
            }
            MathQuestCategory.SUBTRACTION -> {
                operation = "-"
                when (difficulty) {
                    "EASY" -> {
                        num1 = Random.nextInt(2, 10)
                        num2 = Random.nextInt(1, num1) // No negative results
                    }
                    "MEDIUM" -> {
                        num1 = Random.nextInt(15, 50)
                        num2 = Random.nextInt(5, num1)
                    }
                    else -> { // HARD
                        num1 = Random.nextInt(35, 100)
                        num2 = Random.nextInt(10, num1)
                    }
                }
                correctAnswer = num1 - num2
            }
            MathQuestCategory.MULTIPLICATION -> {
                operation = "×"
                when (difficulty) {
                    "EASY" -> {
                        num1 = Random.nextInt(1, 4)
                        num2 = Random.nextInt(1, 5)
                    }
                    "MEDIUM" -> {
                        num1 = Random.nextInt(2, 6)
                        num2 = Random.nextInt(2, 8)
                    }
                    else -> { // HARD
                        num1 = Random.nextInt(3, 10)
                        num2 = Random.nextInt(3, 10)
                    }
                }
                correctAnswer = num1 * num2
            }
            MathQuestCategory.DIVISION -> {
                operation = "÷"
                // For exact division, build backwards: num1 = num2 * result
                when (difficulty) {
                    "EASY" -> {
                        num2 = Random.nextInt(1, 3)
                        val result = Random.nextInt(1, 5)
                        num1 = num2 * result
                        correctAnswer = result
                    }
                    "MEDIUM" -> {
                        num2 = Random.nextInt(2, 5)
                        val result = Random.nextInt(2, 8)
                        num1 = num2 * result
                        correctAnswer = result
                    }
                    else -> { // HARD
                        num2 = Random.nextInt(3, 10)
                        val result = Random.nextInt(3, 10)
                        num1 = num2 * result
                        correctAnswer = result
                    }
                }
            }
        }

        // Generate options (3 items, 1 correct, 2 plausible wrong answers)
        val wrongOption1 = (correctAnswer + Random.nextInt(1, 4)).coerceAtLeast(0)
        val wrongOption2Temp = correctAnswer - Random.nextInt(1, 4)
        val wrongOption2 = if (wrongOption2Temp == wrongOption1 || wrongOption2Temp < 0) {
            correctAnswer + Random.nextInt(5, 8)
        } else {
            wrongOption2Temp
        }

        val optionsList = mutableListOf(correctAnswer, wrongOption1, wrongOption2).shuffled()

        return MathQuizItem(num1, num2, operation, correctAnswer, optionsList)
    }

    // USER SUBMIT ACTION
    fun submitAnswer(selectedValue: Int) {
        if (selectedAnswerState.value != null) return // Already processed

        selectedAnswerState.value = selectedValue
        val activeQuizz = currentQuestions.value.getOrNull(currentQuestionIndex.value) ?: return
        
        if (selectedValue == activeQuizz.correctAnswer) {
            isAnswerCorrectState.value = true
            correctAnswersInSession.value += 1
            activeQuestCoinsEarned.value += 10
            showQuizSuccessOverlay.value = true
        } else {
            isAnswerCorrectState.value = false
            showQuizSuccessOverlay.value = true
        }
    }

    fun nextQuestionOrFinish(category: MathQuestCategory) {
        showQuizSuccessOverlay.value = false
        selectedAnswerState.value = null

        val nextIndex = currentQuestionIndex.value + 1
        if (nextIndex < currentQuestions.value.size) {
            currentQuestionIndex.value = nextIndex
        } else {
            // Quest Completed! Trigger final results and local persistence
            saveQuestResult(category)
        }
    }

    private fun saveQuestResult(category: MathQuestCategory) {
        val currentProfile = _activeProfile.value ?: return
        val totalQuestionsCount = currentQuestions.value.size
        val correctTotal = correctAnswersInSession.value
        val difficultGroup = currentProfile.difficulty

        viewModelScope.launch {
            // Log this exercise into DB
            val log = ProgressLog(
                profileId = currentProfile.id,
                operationType = category.name,
                difficulty = difficultGroup,
                correctCount = correctTotal,
                totalCount = totalQuestionsCount
            )
            repository.insertProgressLog(log)

            // Update user balance and level
            val pointsAwarded = activeQuestCoinsEarned.value
            var updatedCoins = currentProfile.coins + pointsAwarded

            // Handle badge validations (virtual rewards check)
            val currentBadges = currentProfile.unlockedBadges.split(",").filter { it.isNotBlank() }.toMutableSet()
            
            // Badge logic
            if (currentBadges.isEmpty()) {
                currentBadges.add("first_step") // Badge for starting
            }
            if (correctTotal == totalQuestionsCount) {
                when (category) {
                    MathQuestCategory.ADDITION -> currentBadges.add("addition_hero")
                    MathQuestCategory.SUBTRACTION -> currentBadges.add("subtraction_hero")
                    MathQuestCategory.MULTIPLICATION -> currentBadges.add("multiplication_hero")
                    MathQuestCategory.DIVISION -> currentBadges.add("division_hero")
                }
            }
            if (updatedCoins >= 100) {
                currentBadges.add("silver_coin_collector")
            }
            if (updatedCoins >= 250) {
                currentBadges.add("gold_tycoon")
            }

            val badgeJoined = currentBadges.joinToString(",")

            // Leveling up formula
            val oldLevel = currentProfile.currentLevel
            val levelUp = oldLevel + 1

            val updatedProfile = currentProfile.copy(
                coins = updatedCoins,
                unlockedBadges = badgeJoined,
                currentLevel = levelUp
            )

            repository.updateProfile(updatedProfile)
            _activeProfile.value = updatedProfile

            showQuestCompletedSummary.value = true
        }
    }

    // REWARD STICKER SYSTEM SHOP
    fun purchaseSticker(toy: VirtualToySticker): Boolean {
        val currentProfile = _activeProfile.value ?: return false
        val cost = toy.cost
        
        if (currentProfile.coins < cost) {
            return false // Insufficient funds
        }

        val ownedStickers = currentProfile.unlockedStickers.split(",").filter { it.isNotBlank() }.toMutableSet()
        if (ownedStickers.contains(toy.id)) {
            return true // Already purchased
        }

        ownedStickers.add(toy.id)
        val updatedList = ownedStickers.joinToString(",")

        viewModelScope.launch {
            val updated = currentProfile.copy(
                coins = currentProfile.coins - cost,
                unlockedStickers = updatedList
            )
            repository.updateProfile(updated)
            _activeProfile.value = updated
        }
        return true
    }

    // PARENTS HUB SECURITY LOCK
    fun prepareParentsGate() {
        // Generates simple math challenge for parent authentication
        parentChallengeNum1 = Random.nextInt(7, 12)
        parentChallengeNum2 = Random.nextInt(6, 11)
        parentChallengeAnswer = parentChallengeNum1 * parentChallengeNum2
        parentChallengeInput = ""
        parentChallengeErrorMessage = ""
        navigateTo(QuestScreen.ParentGate)
    }

    fun submitParentsGateAnswer(): Boolean {
        val pInt = parentChallengeInput.trim().toIntOrNull()
        if (pInt == parentChallengeAnswer) {
            parentChallengeErrorMessage = ""
            // Clear past logs loader so dashboard refreshes
            loadProgressLogsForDashboard()
            navigateTo(QuestScreen.ParentDashboard)
            return true
        } else {
            parentChallengeErrorMessage = "VETËM PRINDËRIT / PARENTS ONLY!"
            return false
        }
    }

    // Fetch reports of other children for comparison and detail analysis
    fun loadProgressLogsForDashboard() {
        val currentPid = _activeProfile.value?.id ?: return
        viewModelScope.launch {
            _parentProgressLogs.value = repository.getProgressLogs(currentPid)
        }
    }
}
