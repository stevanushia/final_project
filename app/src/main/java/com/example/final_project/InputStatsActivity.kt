package com.example.final_project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.final_project.databinding.ActivityInputStatsBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.math.abs

class InputStatsActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE_PLAYER_SELECT = 101
        const val EXTRA_STAT_TYPE = "stat_type"
        const val EXTRA_SELECTED_PLAYER = "selected_player"
    }

    private lateinit var binding: ActivityInputStatsBinding
    private val viewModel = GameViewModelProvider.gameStateViewModel

    private lateinit var gestureDetector: GestureDetector
    private var maxPointerCountDuringGesture = 0
    private var isDebugMode = true // Set to false for production

    private var selectedTeam = "HOME" // optional if you're planning team logic

    private val matchLogList = mutableListOf<String>()
    private lateinit var matchLogAdapter: MatchHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadMatchLogs()
        setupGestureDetection()

        // Match history setup
        matchLogAdapter = MatchHistoryAdapter(matchLogList)
        binding.recyclerHistory.adapter = matchLogAdapter
        binding.recyclerHistory.layoutManager = LinearLayoutManager(this)

        // Set up all button click listeners
        setupButtonClickListeners()

        // Set up ViewModel observers
        setupViewModelObservers()
    }

    override fun onResume() {
        super.onResume()
        // Make sure the UI reflects the current state when activity becomes visible
        // No need to start/stop timers - they continue in the ViewModel
        updateTimerDisplays()
    }

    private fun updateTimerDisplays() {
        // Update the UI based on current ViewModel state
        binding.txtGameTime.text = viewModel.gameTime.value ?: "12:00"
        binding.txtShotTime.text = viewModel.shotClock.value ?: "24"
        binding.txtQuarter.text = viewModel.quarter.value ?: "Q1"
        binding.txtScoreHome.text = (viewModel.homeScore.value ?: 0).toString()
        binding.txtScoreAway.text = (viewModel.awayScore.value ?: 0).toString()
    }

    private fun setupViewModelObservers() {
        // Observe ViewModel changes and update UI accordingly
        viewModel.gameTime.observe(this) { timeText ->
            binding.txtGameTime.text = timeText
        }

        viewModel.shotClock.observe(this) { shotClockText ->
            binding.txtShotTime.text = shotClockText
        }

        viewModel.quarter.observe(this) { quarterText ->
            binding.txtQuarter.text = quarterText
        }

        viewModel.homeScore.observe(this) { score ->
            binding.txtScoreHome.text = score.toString()
        }

        viewModel.awayScore.observe(this) { score ->
            binding.txtScoreAway.text = score.toString()
        }

        // Add observations for timer state
        viewModel.isGameTimerRunning.observe(this) { isRunning ->
            // Update UI elements that should reflect timer state
            // For example, you might want to change the color of the timer text
            binding.txtGameTime.setTextColor(
                if (isRunning) getColor(R.color.running_timer) else getColor(R.color.paused_timer)
            )
        }
    }

    private fun setupButtonClickListeners() {
        // Add timer control
        binding.txtGameTime.setOnClickListener {
            toggleGameTimer()
        }

        binding.txtShotTime.setOnClickListener {
            // Optional: Add shot clock control if needed
            // Maybe reset to 24 on quick tap, 14 on long press
        }

        // Scoring buttons - Made shots
        binding.btn1pt.setOnClickListener { openPlayerSelect("1PT") }
        binding.btn2pt.setOnClickListener { openPlayerSelect("2PT") }
        binding.btn3pt.setOnClickListener { openPlayerSelect("3PT") }

        // Scoring buttons - Missed shots
        binding.btn1ptMiss.setOnClickListener { openPlayerSelect("1PT MISS") }
        binding.btn2ptMiss.setOnClickListener { openPlayerSelect("2PT MISS") }
        binding.btn3ptMiss.setOnClickListener { openPlayerSelect("3PT MISS") }

        // Stat buttons
        binding.btnAssist.setOnClickListener { openPlayerSelect("ASSIST") }
        binding.btnRebound.setOnClickListener { openPlayerSelect("REBOUND") }
        binding.btnSteal.setOnClickListener { openPlayerSelect("STEAL") }
        binding.btnBlock.setOnClickListener { openPlayerSelect("BLOCK") }
        binding.btnTurnover.setOnClickListener { openPlayerSelect("TURNOVER") }
        binding.btnFoul.setOnClickListener { openPlayerSelect("FOUL") }

        // Navigation button
        binding.btnBackToMain.setOnClickListener { goToMain() }
    }

    // Game Timer Control - matches MainActivity logic
    private fun toggleGameTimer() {
        if (viewModel.isTimeoutRunning.value == true) {
            Toast.makeText(this, "Cannot start timer during timeout", Toast.LENGTH_SHORT).show()
            return
        }

        if (viewModel.isGameTimerRunning.value == true) {
            viewModel.pauseGameTimer()
            Toast.makeText(this, "Game timer paused", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.startGameTimer()
            Toast.makeText(this, "Game timer started", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupGestureDetection() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                // Always return true for down events to ensure other gesture events are processed
                return true
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (maxPointerCountDuringGesture >= 2 && e1 != null) {
                    val diffX = e2.x - e1.x

                    // Debug logs
                    if (isDebugMode) {
                        Log.d("GestureDebug", "Fling detected with $maxPointerCountDuringGesture pointers")
                        Log.d("GestureDebug", "Fling distance X: $diffX, velocity X: $velocityX")
                    }

                    // Check for horizontal swipe with sufficient distance and velocity
                    if (abs(diffX) > 100 && abs(velocityX) > 200) {
                        if (diffX < 0) {
                            // Left swipe - go to main
                            if (isDebugMode) {
                                Toast.makeText(this@InputStatsActivity, "Two-finger swipe LEFT", Toast.LENGTH_SHORT).show()
                            }
                            goToMain()
                            return true
                        }
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

            if (isDebugMode) {
                Log.d("GestureDebug", "Pointer count increased to: $maxPointerCountDuringGesture")
            }
        }

        // Let the gesture detector process the event
        gestureDetector.onTouchEvent(ev)

        // Reset when gesture sequence ends
        if (ev.actionMasked == MotionEvent.ACTION_UP || ev.actionMasked == MotionEvent.ACTION_CANCEL) {
            if (isDebugMode) {
                Log.d("GestureDebug", "Gesture ended with max pointers: $maxPointerCountDuringGesture")
            }
            maxPointerCountDuringGesture = 0
        }

        // Pass the event to the view hierarchy regardless of gesture detection
        return super.dispatchTouchEvent(ev)
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun openPlayerSelect(statType: String) {
        val intent = Intent(this, PlayerSelectionActivity::class.java)
        intent.putExtra(EXTRA_STAT_TYPE, statType)
        startActivityForResult(intent, REQUEST_CODE_PLAYER_SELECT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_PLAYER_SELECT && resultCode == RESULT_OK) {
            val playerName = data?.getStringExtra(EXTRA_SELECTED_PLAYER) ?: return
            val statType = data.getStringExtra(EXTRA_STAT_TYPE) ?: return
            val team = data.getStringExtra("team") ?: "HOME" // fallback

            selectedTeam = team // Update team

            // Log the stat for both made and missed shots
            logStat(playerName, statType)

            // Only update score for made shots (not misses)
            if (!statType.contains("MISS")) {
                val currentHomeScore = viewModel.homeScore.value ?: 0
                val currentAwayScore = viewModel.awayScore.value ?: 0

                if (team == "HOME") {
                    when (statType) {
                        "1PT" -> viewModel.homeScore.postValue(currentHomeScore + 1)
                        "2PT" -> viewModel.homeScore.postValue(currentHomeScore + 2)
                        "3PT" -> viewModel.homeScore.postValue(currentHomeScore + 3)
                    }
                } else if (team == "AWAY") {
                    when (statType) {
                        "1PT" -> viewModel.awayScore.postValue(currentAwayScore + 1)
                        "2PT" -> viewModel.awayScore.postValue(currentAwayScore + 2)
                        "3PT" -> viewModel.awayScore.postValue(currentAwayScore + 3)
                    }
                }
            }
        }
    }

    private fun logStat(player: String, stat: String) {
        val time = binding.txtGameTime.text.toString()
        val quarter = binding.txtQuarter.text.toString()
        val entry = "$quarter - $time - $player : $stat • $selectedTeam"

        // Save locally
        matchLogList.add(0, entry)
        matchLogAdapter.notifyItemInserted(0)
        binding.recyclerHistory.scrollToPosition(0)

        // Push to Firebase
        val logRef = FirebaseDatabase.getInstance().getReference("match_logs")
        logRef.push().setValue(entry)

        // Show feedback
        val feedback = if (stat.contains("MISS")) {
            "$player missed a ${stat.replace(" MISS", "")} shot"
        } else {
            "$player recorded a $stat"
        }
        Toast.makeText(this, feedback, Toast.LENGTH_SHORT).show()
    }

    private fun loadMatchLogs() {
        val logRef = FirebaseDatabase.getInstance().getReference("match_logs")

        logRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                matchLogList.clear()
                for (child in snapshot.children) {
                    val log = child.getValue(String::class.java)
                    if (log != null) {
                        matchLogList.add(0, log) // add in reverse for latest first
                    }
                }
                matchLogAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to load logs: ${error.message}")
            }
        })
    }
}