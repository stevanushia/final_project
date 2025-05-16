package com.example.final_project

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GameStateViewModel : ViewModel() {
    // UI Display Values
    val gameTime = MutableLiveData("12:00")
    val quarter = MutableLiveData("Q1")
    val homeScore = MutableLiveData(0)
    val awayScore = MutableLiveData(0)
    val shotClock = MutableLiveData("24")

    // Internal timer state
    val isGameTimerRunning = MutableLiveData(false)
    val gameTimeRemainingMillis = MutableLiveData(12 * 60 * 1000L)
    val isShotClockRunning = MutableLiveData(false)
    val shotClockRemainingMillis = MutableLiveData(24 * 1000L)

    // Timeout state
    val isTimeoutRunning = MutableLiveData(false)
    val wasShotClockRunningBeforePause = MutableLiveData(false)

    // Actual timer instances
    private var gameTimer: CountDownTimer? = null
    private var shotClockTimer: CountDownTimer? = null

    // Default duration values
    private val defaultGameTimeMillis = 12 * 60 * 1000L
    private val defaultShotClockMillis = 24 * 1000L

    // Game timer control functions
    fun startGameTimer(restartFromSaved: Boolean = false) {
        if (isTimeoutRunning.value == true) {
            return
        }

        // Cancel existing timer
        gameTimer?.cancel()

        val remainingTime = gameTimeRemainingMillis.value ?: defaultGameTimeMillis

        gameTimer = object : CountDownTimer(remainingTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                gameTimeRemainingMillis.postValue(millisUntilFinished)
                updateGameTimeDisplay(millisUntilFinished)
            }

            override fun onFinish() {
                isGameTimerRunning.postValue(false)
                gameTimeRemainingMillis.postValue(0)
                updateGameTimeDisplay(0)
                pauseShotClock()
                isTimeoutRunning.postValue(false)
            }
        }.start()

        isGameTimerRunning.postValue(true)

        // Handle shot clock when game timer starts
        if (!isShotClockRunning.value!! && !restartFromSaved) {
            if (wasShotClockRunningBeforePause.value == true) {
                resumeShotClock()
            } else if (shotClockRemainingMillis.value == defaultShotClockMillis || shotClockRemainingMillis.value!! <= 0) {
                startShotClock(defaultShotClockMillis)
            }
        } else if (!isShotClockRunning.value!! && restartFromSaved && isShotClockRunning.value == true) {
            startShotClock(shotClockRemainingMillis.value ?: defaultShotClockMillis)
        }
    }

    fun pauseGameTimer() {
        // Save shot clock state
        wasShotClockRunningBeforePause.postValue(isShotClockRunning.value)

        // Cancel game timer
        gameTimer?.cancel()
        isGameTimerRunning.postValue(false)

        // Pause shot clock
        pauseShotClock()
    }

    private fun updateGameTimeDisplay(timeMillis: Long) {
        val minutes = (timeMillis / 1000) / 60
        val seconds = (timeMillis / 1000) % 60
        val timeString = String.format("%02d:%02d", minutes, seconds)
        gameTime.postValue(timeString)
    }

    // Shot clock control functions
    fun startShotClock(duration: Long = defaultShotClockMillis, isTimeout: Boolean = false) {
        shotClockTimer?.cancel()
        shotClockRemainingMillis.postValue(duration)

        shotClockTimer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                shotClockRemainingMillis.postValue(millisUntilFinished)
                updateShotClockDisplay(millisUntilFinished, isTimeout)
            }

            override fun onFinish() {
                if (isTimeout) isTimeoutRunning.postValue(false)
                shotClockRemainingMillis.postValue(0)
                updateShotClockDisplay(0, isTimeout)
                isShotClockRunning.postValue(false)

                if (!isTimeout) {
                    pauseGameTimer()
                }
            }
        }.start()

        isShotClockRunning.postValue(true)
    }

    fun pauseShotClock() {
        shotClockTimer?.cancel()
        isShotClockRunning.postValue(false)
    }

    fun resumeShotClock() {
        val remaining = shotClockRemainingMillis.value ?: defaultShotClockMillis
        if (remaining <= 0) {
            shotClockRemainingMillis.postValue(defaultShotClockMillis)
            startShotClock(defaultShotClockMillis)
        } else {
            startShotClock(remaining)
        }
    }

    private fun updateShotClockDisplay(timeMillis: Long, isTimeout: Boolean = false) {
        val seconds = (timeMillis / 1000).toInt()
        shotClock.postValue(seconds.toString())
    }

    fun resetShotClockTo24() {
        shotClockTimer?.cancel()
        shotClockRemainingMillis.postValue(24_000L)
        shotClock.postValue("24")

        if (isGameTimerRunning.value == true) {
            startShotClock(24_000L)
        }
        isTimeoutRunning.postValue(false)
    }

    fun setShotClockTo14() {
        shotClockTimer?.cancel()
        shotClockRemainingMillis.postValue(14_000L)
        shotClock.postValue("14")

        if (isGameTimerRunning.value == true) {
            startShotClock(14_000L)
        }
        isTimeoutRunning.postValue(false)
    }

    fun startTimeout(duration: Long) {
        pauseGameTimer()
        startShotClock(duration, isTimeout = true)
        isTimeoutRunning.postValue(true)
    }

    fun skipTimeout() {
        pauseShotClock()
        shotClockRemainingMillis.postValue(24_000L)
        shotClock.postValue("24")
        isTimeoutRunning.postValue(false)
    }

    // Clean up timers when ViewModel is cleared
    override fun onCleared() {
        gameTimer?.cancel()
        shotClockTimer?.cancel()
        super.onCleared()
    }
}