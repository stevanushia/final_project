package com.example.final_project

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.final_project.databinding.ActivityMainBinding // Import for your main layout binding
import com.example.final_project.databinding.ModalEditLayoutBinding // Import for modal layout binding
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.IgnoreExtraProperties
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var modalBinding: ModalEditLayoutBinding // Binding for the modal layout
    private val viewModel = GameViewModelProvider.gameStateViewModel
    private lateinit var gestureDetector: GestureDetector
    private var maxPointerCountDuringGesture = 0
    private var currentUserId: String? = null

    // Game Time Control
    private var gameTimeMillis: Long = 10 * 60 * 1200 // 10 minutes
    private var gameTimer: CountDownTimer? = null
    private var isGameRunning = false
    private var gameTimeRemaining = gameTimeMillis

    // Score Management
    private var homeScore = 0
    private var awayScore = 0

    // Timeout Functionality
    private var homeTimeouts = 0
    private var awayTimeouts = 0

    // Foul Counter
    private var homeFouls = 0
    private var awayFouls = 0

    // Shot Clock Controls
    private var shotClockMillis: Long = 24_000
    private var shotClockRemaining = shotClockMillis
    private var shotClockTimer: CountDownTimer? = null
    private var isShotClockRunning = false

    // SHOTCLOCK STATE
    private var wasShotClockRunningBeforePause = false

    // GAMETIME STATE
    private var isTimeoutRunning = false

    private var homeFoulCount = 0
    private var awayFoulCount = 0

    private val homeTeamPlayers = mutableListOf(
        DummyPlayer("John"),
        DummyPlayer("Mike"),
        DummyPlayer("Chris")
    )

    private val awayTeamPlayers = mutableListOf(
        DummyPlayer("Tony"),
        DummyPlayer("Kevin"),
        DummyPlayer("Jake")
    )

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var isDataLoaded = false
        installSplashScreen().setKeepOnScreenCondition {
            !isDataLoaded
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        modalBinding = ModalEditLayoutBinding.bind(binding.root) // Bind the modal layout to the inflated root view

        // Set up ViewModel observers
        setupViewModelObservers()

        // Set onClickListener for the main Edit button to show the modal
        binding.btnMenu.setOnClickListener {
            Toast.makeText(this, "Switch Screen Clicked", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, InputStatsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish() // 🔥 This removes MainActivity from back stack
        }

        binding.timerTextView.setOnClickListener {
            toggleGameTimer()
        }

        // Modal setup
        setupModalControls()

        // Timeout Functionality
        binding.txtTimeoutCtr.setOnClickListener {
            showTimeoutDialog(isHomeTeam = true)
        }

        binding.txtTimeoutCtr2.setOnClickListener {
            showTimeoutDialog(isHomeTeam = false)
        }

        // Foul Counter
        binding.txtFoulCtr.setOnClickListener {
            homeFouls++
            binding.txtFoulCtr.text = "F$homeFouls"
        }

        binding.txtFoulCtr2.setOnClickListener {
            awayFouls++
            binding.txtFoulCtr2.text = "F$awayFouls"
        }

        // Shot Clock Controls
        binding.btnResetShotClock.setOnClickListener {
            resetShotClockTo24()
        }

        binding.btn14ShotClock.setOnClickListener {
            setShotClockTo14()
        }

        binding.btnSkipTimeout.setOnClickListener {
            pauseShotClock()
            setShotClockOnly(24_000L, isTimeout = false)
            showTimeoutUI(false)
            isTimeoutRunning = false
        }

        binding.posessionLeft.setOnClickListener {
            setPossession(isLeft = true)
        }

        binding.posessionRight.setOnClickListener {
            setPossession(isLeft = false)
        }

        binding.txtFoulCtr.setOnClickListener {
            showFoulDialog(homeTeamPlayers, "Home")
        }

        binding.txtFoulCtr2.setOnClickListener {
            showFoulDialog(awayTeamPlayers, "Away")
        }

        setupGestureDetector()

        isDataLoaded = true
    }

    private fun setupViewModelObservers() {
        // Observe ViewModel changes and update UI accordingly
        viewModel.gameTime.observe(this) { timeText ->
            binding.timerTextView.text = timeText
        }

        viewModel.quarter.observe(this) { quarterText ->
            binding.periodTextView.text = quarterText
        }

        viewModel.homeScore.observe(this) { score ->
            binding.teamHomeScore.text = score.toString()
            homeScore = score
        }

        viewModel.awayScore.observe(this) { score ->
            binding.teamAwayScore.text = score.toString()
            awayScore = score
        }
    }

    private fun setupModalControls() {
        // Set onClickListener for the Close button in the modal
        modalBinding.btnCloseModal.setOnClickListener {
            hideModal()
        }

        // Optional: Close modal when clicking on the background
        modalBinding.modalEditContainer.setOnClickListener {
            hideModal()
        }

        modalBinding.btnBreakModal.setOnClickListener {
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
    }

    private fun setupGestureDetector() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                // Check if we had at least 2 fingers during this gesture
                if (maxPointerCountDuringGesture >= 2 && e1 != null) {
                    val diffX = e2.x - e1.x

                    // Debug logs
                    Log.d("GestureDebug", "Fling detected with $maxPointerCountDuringGesture pointers")
                    Log.d("GestureDebug", "Fling distance X: $diffX, velocity X: $velocityX")

                    // Check for horizontal swipe with sufficient distance and velocity
                    if (abs(diffX) > 100 && abs(velocityX) > 200) {
                        if (diffX > 0) {
                            // Right swipe
                            Toast.makeText(this@MainActivity, "Two-finger swipe right", Toast.LENGTH_SHORT).show()
                            goToStats()
                        } else {
                            // Left swipe
                            Toast.makeText(this@MainActivity, "Swipe right to switch screen!", Toast.LENGTH_SHORT).show()
                        }
                        return true
                    }
                }
                return false
            }
        })
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        // Track the maximum number of pointers during this gesture
        if (ev.pointerCount > maxPointerCountDuringGesture) {
            maxPointerCountDuringGesture = ev.pointerCount
        }

        // Process the gesture
        val result = gestureDetector.onTouchEvent(ev)

        // Reset count when the gesture ends
        if (ev.actionMasked == MotionEvent.ACTION_UP || ev.actionMasked == MotionEvent.ACTION_CANCEL) {
            val finalPointerCount = maxPointerCountDuringGesture
            maxPointerCountDuringGesture = 0

            // For debugging
            Log.d("GestureDebug", "Gesture ended with max pointers: $finalPointerCount")
        }

        return result || super.dispatchTouchEvent(ev)
    }

    private fun goToStats() {
        val intent = Intent(this, InputStatsActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun showModal() {
        modalBinding.modalEditContainer.visibility = View.VISIBLE
    }

    private fun hideModal() {
        modalBinding.modalEditContainer.visibility = View.GONE
    }

    // Game Time Control
    private fun toggleGameTimer() {
        if (isTimeoutRunning) {
            Toast.makeText(this, "Cannot start timer during timeout", Toast.LENGTH_SHORT).show()
            return
        }

        if (isGameRunning) {
            pauseGameTimer()
        } else {
            startGameTimer()
        }
    }

    private fun startGameTimer() {
        if (isTimeoutRunning) {
            Toast.makeText(this, "Cannot start timer during timeout", Toast.LENGTH_SHORT).show()
            return
        }

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
                isTimeoutRunning = false
            }
        }.start()
        isGameRunning = true

        // Determine shot clock behavior
        if (!isShotClockRunning) {
            if (wasShotClockRunningBeforePause) {
                resumeShotClock()
            } else if (shotClockRemaining == shotClockMillis || shotClockRemaining <= 0) {
                startShotClock(shotClockMillis) // Reset to full time if at 0 or default
            }
        }
    }

    private fun resumeShotClock() {
        if (shotClockRemaining <= 0) {
            shotClockRemaining = shotClockMillis
        }
        startShotClock(shotClockRemaining)
    }

    private fun pauseGameTimer() {
        // Save shot clock state before pausing
        wasShotClockRunningBeforePause = isShotClockRunning

        // Cancel game timer
        gameTimer?.cancel()
        isGameRunning = false

        // Pause shot clock
        pauseShotClock()
    }

    private fun updateGameTimeDisplay() {
        val minutes = (gameTimeRemaining / 1000) / 60
        val seconds = (gameTimeRemaining / 1000) % 60
        val timeString = String.format("%02d:%02d", minutes, seconds)
        viewModel.gameTime.postValue(timeString)
    }

    // Timeout Functionality
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
        isTimeoutRunning = true
        showTimeoutUI(true)
        if (isHomeTeam) {
            homeTimeouts++
            binding.txtTimeoutCtr.text = "T$homeTimeouts"
        } else {
            awayTimeouts++
            binding.txtTimeoutCtr2.text = "T$awayTimeouts"
        }
    }

    // Shot Clock Controls
    private fun startShotClock(duration: Long = shotClockMillis, isTimeout: Boolean = false) {
        shotClockTimer?.cancel()
        shotClockRemaining = duration
        shotClockTimer = object : CountDownTimer(shotClockRemaining, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                shotClockRemaining = millisUntilFinished
                val seconds = (millisUntilFinished / 1000).toInt()

                // Only update UI if the value has changed
                if (binding.txtShotClock.text.toString() != seconds.toString()) {
                    binding.txtShotClock.text = seconds.toString()
                    binding.txtShotClock.setTextColor(if (isTimeout) Color.CYAN else Color.GREEN)
                }
            }

            override fun onFinish() {
                if (isTimeout) showTimeoutUI(false)
                shotClockRemaining = 0
                binding.txtShotClock.text = "0"
                binding.txtShotClock.setTextColor(Color.RED)
                isShotClockRunning = false

                if (!isTimeout) {
                    pauseGameTimer()
                }
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
            when {
                isTimeout -> Color.CYAN
                seconds <= 5 -> Color.RED
                else -> Color.GREEN
            }
        )
    }

    private fun resetShotClockTo24() {
        shotClockTimer?.cancel()
        shotClockRemaining = 24_000L
        updateShotClockDisplay(isTimeout = false)

        isShotClockRunning = isGameRunning
        if (isGameRunning) {
            startShotClock(shotClockRemaining)
        }
        isTimeoutRunning = false
    }

    private fun setShotClockTo14() {
        shotClockTimer?.cancel()
        shotClockRemaining = 14_000L
        updateShotClockDisplay(isTimeout = false)

        isShotClockRunning = isGameRunning
        if (isGameRunning) {
            startShotClock(shotClockRemaining)
        }
        isTimeoutRunning = false
    }

    private fun setShotClockOnly(duration: Long = 24_000L, isTimeout: Boolean = false) {
        shotClockTimer?.cancel()
        shotClockRemaining = duration
        isShotClockRunning = false
        updateShotClockDisplay(isTimeout)
    }

    private fun showTimeoutUI(isTimeout: Boolean) {
        binding.btnResetShotClock.visibility = if (isTimeout) View.GONE else View.VISIBLE
        binding.btn14ShotClock.visibility = if (isTimeout) View.GONE else View.VISIBLE
        binding.btnSkipTimeout.visibility = if (isTimeout) View.VISIBLE else View.GONE
    }

    private fun setPossession(isLeft: Boolean) {
        if (isLeft) {
            binding.posessionLeft.setImageResource(R.drawable.red_triangle)
            binding.posessionRight.setImageResource(R.drawable.gray_triangle_1)
        } else {
            binding.posessionLeft.setImageResource(R.drawable.gray_triangle_1)
            binding.posessionRight.setImageResource(R.drawable.red_triangle)
        }
    }

    private fun showFoulDialog(players: MutableList<DummyPlayer>, teamName: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_select_player, null)
        val listView = dialogView.findViewById<ListView>(R.id.playerListView)
        val title = dialogView.findViewById<TextView>(R.id.dialogTitle)

        title.text = "Select Foul for $teamName"

        val playerNames = players.mapIndexed { index, player ->
            "${player.name} - Fouls: ${player.fouls}"
        }.toMutableList()

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, playerNames)
        listView.adapter = adapter

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        listView.setOnItemClickListener { _, _, position, _ ->
            players[position].fouls++

            if (teamName == "Home") {
                homeFoulCount++
                binding.txtFoulCtr.text = "F$homeFoulCount"
            } else {
                awayFoulCount++
                binding.txtFoulCtr2.text = "F$awayFoulCount"
            }

            Toast.makeText(this, "${players[position].name} committed a foul!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }
}