package com.example.final_project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.final_project.databinding.ActivityInputStatsBinding
import com.example.final_project.databinding.InputStatsItemBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class InputStatsActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE_PLAYER_SELECT = 101
        const val EXTRA_STAT_TYPE = "stat_type"
        const val EXTRA_SELECTED_PLAYER = "selected_player"
    }

    private lateinit var binding: ActivityInputStatsBinding

    private lateinit var gestureDetector: GestureDetector
    private var maxPointerCountDuringGesture = 0

    private var homeScore = 0
    private var awayScore = 0
    private var selectedTeam = "HOME" // optional if you're planning team logic

    private val matchLogList = mutableListOf<String>()
    private lateinit var matchLogAdapter: MatchHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadMatchLogs()

        // Match history setup
        matchLogAdapter = MatchHistoryAdapter(matchLogList)
        binding.recyclerHistory.adapter = matchLogAdapter
        binding.recyclerHistory.layoutManager = LinearLayoutManager(this)

        // Scoring buttons
        binding.btn1pt.setOnClickListener { openPlayerSelect("1PT") }
        binding.btn2pt.setOnClickListener { openPlayerSelect("2PT") }
        binding.btn3pt.setOnClickListener { openPlayerSelect("3PT") }

        // Stat buttons
        binding.btnAssist.setOnClickListener { openPlayerSelect("ASSIST") }
        binding.btnRebound.setOnClickListener { openPlayerSelect("REBOUND") }
        binding.btnSteal.setOnClickListener { openPlayerSelect("STEAL") }
        binding.btnBlock.setOnClickListener { openPlayerSelect("BLOCK") }
        binding.btnTurnover.setOnClickListener { openPlayerSelect("TURNOVER") }
        binding.btnFoul.setOnClickListener { openPlayerSelect("FOUL") }


        val gameStateRef = FirebaseDatabase.getInstance().getReference("game_state")
        gameStateRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val time = snapshot.child("time").getValue(String::class.java) ?: "00:00"
                val quarter = snapshot.child("quarter").getValue(String::class.java) ?: "Q1"
                val home = snapshot.child("score_home").getValue(Int::class.java) ?: 0
                val away = snapshot.child("score_away").getValue(Int::class.java) ?: 0

                binding.txtGameTime.text = time
                binding.txtQuarter.text = quarter
                binding.txtScoreHome.text = home.toString()
                binding.txtScoreAway.text = away.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to read game state: ${error.message}")
            }
        })

        binding.btnBackToMain.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Optional: clear activity from stack
        }

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
                            Toast.makeText(this@InputStatsActivity, "Swipe Left to go back!", Toast.LENGTH_SHORT).show()
                        } else {
                            // Left swipe
                            Toast.makeText(this@InputStatsActivity, "Two-finger swipe left", Toast.LENGTH_SHORT).show()
                            goToMain()
                        }
                        return true
                    }
                }
                return false
            }
        })

    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
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
            logStat(playerName, statType)

            if (team == "HOME") {
                when (statType) {
                    "1PT" -> homeScore += 1
                    "2PT" -> homeScore += 2
                    "3PT" -> homeScore += 3
                }
            } else if (team == "AWAY") {
                when (statType) {
                    "1PT" -> awayScore += 1
                    "2PT" -> awayScore += 2
                    "3PT" -> awayScore += 3
                }
            }

            updateScoreDisplay()
        }
    }

    private fun logStat(player: String, stat: String) {
        val time = binding.txtGameTime.text.toString()
        val entry = "$time - $player . $stat • $selectedTeam"

        // Save locally
        matchLogList.add(0, entry)
        matchLogAdapter.notifyItemInserted(0)
        binding.recyclerHistory.scrollToPosition(0)

        // Push to Firebase
        val logRef = FirebaseDatabase.getInstance().getReference("match_logs")
        logRef.push().setValue(entry)
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




    private fun updateScoreDisplay() {
        binding.txtScoreHome.text = homeScore.toString()
        binding.txtScoreAway.text = awayScore.toString()
    }
}
