package com.example.final_project

import android.content.pm.ActivityInfo
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.final_project.databinding.ActivityMainBinding // Import for your main layout binding
import com.example.final_project.databinding.ModalEditLayoutBinding // Import for modal layout binding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.IgnoreExtraProperties

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var modalBinding: ModalEditLayoutBinding // Binding for the modal layout
    private lateinit var database: DatabaseReference
    private var currentUserId: String? = null

//    Game Time Control
    private var gameTimeMillis: Long = 10 * 60 * 1200 // 10 minutes
    private var gameTimer: CountDownTimer? = null
    private var isGameRunning = false
    private var gameTimeRemaining = gameTimeMillis
//    Game Time Control

//    Score Management
    private var homeScore = 0
    private var awayScore = 0
//    Score Management

//    Timeout Functionality
    private var homeTimeouts = 0
    private var awayTimeouts = 0
//    Timeout Functionality

//  Foul Counter
    private var homeFouls = 0
    private var awayFouls = 0
//  Foul Counter

//  Shot Clock Controls
    private var shotClockMillis: Long = 24_000
    private var shotClockRemaining = shotClockMillis
    private var shotClockTimer: CountDownTimer? = null
    private var isShotClockRunning = false
//  Shot Clock Controls



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var isDataLoaded = false
        installSplashScreen().setKeepOnScreenCondition {
            !isDataLoaded
        }
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        modalBinding = ModalEditLayoutBinding.bind(binding.root) // Bind the modal layout to the inflated root view

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference

        // Set onClickListener for the main Edit button to show the modal
        binding.btnMenu.setOnClickListener {
            showModal()
        }

        binding.timerTextView.setOnClickListener {
            toggleGameTimer()
        }

        // Set onClickListener for the Close button in the modal
        modalBinding.btnCloseModal.setOnClickListener {
            hideModal()
        }

        // Optional: Close modal when clicking on the background
        modalBinding.modalEditContainer.setOnClickListener {
            hideModal()
        }

        // Example click listeners for modal buttons (replace with your actual logic)
        modalBinding.btnEditModal.setOnClickListener {
            Toast.makeText(this, "Edit Button Clicked", Toast.LENGTH_SHORT).show()
            // Add your Edit functionality here
        }
        modalBinding.btnLiveModal.setOnClickListener {
            Toast.makeText(this, "Live Button Clicked", Toast.LENGTH_SHORT).show()
            // Add your Live functionality here
        }
        modalBinding.btnShotClockModal.setOnClickListener {
            Toast.makeText(this, "Shot Clock Button Clicked", Toast.LENGTH_SHORT).show()
            // Add your Shot Clock functionality here
        }
        modalBinding.btnBreakModal.setOnClickListener {
            Toast.makeText(this, "Break Button Clicked", Toast.LENGTH_SHORT).show()
            // Add your Break functionality here
        }
        modalBinding.btnSwitchModal.setOnClickListener {
            Toast.makeText(this, "Switch Button Clicked", Toast.LENGTH_SHORT).show()
            // Add your Switch functionality here
        }
        modalBinding.btnBuzzerModal.setOnClickListener {
            Toast.makeText(this, "Buzzer Button Clicked", Toast.LENGTH_SHORT).show()
            // Add your Buzzer functionality here
        }
        modalBinding.btnWhistleModal.setOnClickListener {
            Toast.makeText(this, "Whistle Button Clicked", Toast.LENGTH_SHORT).show()
            // Add your Whistle functionality here
        }
        modalBinding.btnFoulModal.setOnClickListener {
            Toast.makeText(this, "Foul Button Clicked", Toast.LENGTH_SHORT).show()
            // Add your Foul functionality here
        }
        modalBinding.btnResetModal.setOnClickListener {
            Toast.makeText(this, "Reset Button Clicked", Toast.LENGTH_SHORT).show()
            // Add your Reset functionality here
        }

        //    Score Management
        binding.btnAddHome.setOnClickListener {
            homeScore++
            updateScores()
        }

        binding.btnMinHome.setOnClickListener {
            if (homeScore > 0) homeScore--
            updateScores()
        }

        binding.btnAddAway.setOnClickListener {
            awayScore++
            updateScores()
        }

        binding.btnMinAway.setOnClickListener {
            if (awayScore > 0) awayScore--
            updateScores()
        }
        //    Score Management

//    Timeout Functionality
        binding.txtTimeoutCtr.setOnClickListener {
            showTimeoutDialog(isHomeTeam = true)
        }

        binding.txtTimeoutCtr2.setOnClickListener {
            showTimeoutDialog(isHomeTeam = false)
        }
//    Timeout Functionality

//      Foul Counter
        binding.txtFoulCtr.setOnClickListener {
            homeFouls++
            binding.txtFoulCtr.text = "F$homeFouls"
        }

        binding.txtFoulCtr2.setOnClickListener {
            awayFouls++
            binding.txtFoulCtr2.text = "F$awayFouls"
        }
//      Foul Counter

//  Shot Clock Controls
        binding.btnResetShotClock.setOnClickListener {
            startShotClock()
        }

        binding.btnResetShotClock.setOnClickListener {
            if (!isShotClockRunning) return@setOnClickListener
            startShotClock(24_000L)
        }

        binding.btn14ShotClock.setOnClickListener {
            if (!isShotClockRunning) return@setOnClickListener
            startShotClock(14_000L)
        }
//  Shot Clock Controls

        isDataLoaded = true
    }

    private fun showModal() {
        modalBinding.modalEditContainer.visibility = View.VISIBLE
    }

    private fun hideModal() {
        modalBinding.modalEditContainer.visibility = View.GONE
    }

//    Game Time Control
    private fun toggleGameTimer() {
        if (isGameRunning) {
            pauseGameTimer()
        } else {
            startGameTimer()
        }
    }

    private fun startGameTimer() {
        gameTimer = object : CountDownTimer(gameTimeRemaining, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                gameTimeRemaining = millisUntilFinished
                updateGameTimeDisplay()
            }

            override fun onFinish() {
                isGameRunning = false
                gameTimeRemaining = 0
                updateGameTimeDisplay()
                pauseShotClock()
            }
        }.start()
        isGameRunning = true
        startShotClock()
    }

    private fun pauseGameTimer() {
        gameTimer?.cancel()
        isGameRunning = false
        pauseShotClock()
    }

    private fun updateGameTimeDisplay() {
        val minutes = (gameTimeRemaining / 1000) / 60
        val seconds = (gameTimeRemaining / 1000) % 60
        binding.timerTextView.text = String.format("%02d:%02d", minutes, seconds)
    }
//    Game Time Control


//    Score Management
    private fun updateScores() {
        binding.teamHomeScore.text = homeScore.toString()
        binding.teamAwayScore.text = awayScore.toString()
    }
//    Score Management


//    Timeout Functionality
    private fun showTimeoutDialog(isHomeTeam: Boolean) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_timeout, null)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radioGroupTimeout)
        val startButton = dialogView.findViewById<Button>(R.id.btnStartTimeout)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Start Timeout")
            .setView(dialogView)
            .create()

        startButton.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            val duration = when (selectedId) {
                R.id.radio20 -> 20_000L
                R.id.radio30 -> 30_000L
                R.id.radio60 -> 60_000L
                R.id.radio75 -> 75_000L
                else -> 30_000L
            }
            startTimeout(duration, isHomeTeam)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun startTimeout(duration: Long, isHomeTeam: Boolean) {
        pauseGameTimer()
        startShotClock(duration, isTimeout = true)
        if (isHomeTeam) {
            homeTimeouts++
            binding.txtTimeoutCtr.text = "T$homeTimeouts"
        } else {
            awayTimeouts++
            binding.txtTimeoutCtr2.text = "T$awayTimeouts"
        }
    }
//    Timeout Functionality

//  Shot Clock Controls
    private fun startShotClock(duration: Long = 24_000L, isTimeout: Boolean = false) {
        shotClockTimer?.cancel()
        shotClockRemaining = duration
        shotClockTimer = object : CountDownTimer(shotClockRemaining, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                shotClockRemaining = millisUntilFinished
                val seconds = (millisUntilFinished / 1000).toInt()
                binding.txtShotClock.text = seconds.toString()
                if (isTimeout) {
                    binding.txtShotClock.setTextColor(Color.BLUE)
                } else {
                    binding.txtShotClock.setTextColor(Color.GREEN)
                }
            }

            override fun onFinish() {
                shotClockRemaining = 0
                binding.txtShotClock.text = "0"
                binding.txtShotClock.setTextColor(Color.RED)
                pauseGameTimer() // also pause the game timer if shot clock ends
            }
        }.start()
        isShotClockRunning = true
    }


    private fun pauseShotClock() {
        shotClockTimer?.cancel()
        isShotClockRunning = false
    }

    private fun updateShotClockDisplay(isTimeout: Boolean = false) {
        val seconds = (shotClockRemaining / 1000).toInt()
        binding.txtShotClock.text = seconds.toString()
        binding.txtShotClock.setTextColor(
            if (isTimeout) Color.BLUE else Color.BLACK
        )
    }
//  Shot Clock Controls

}