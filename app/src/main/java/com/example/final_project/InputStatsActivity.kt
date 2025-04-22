package com.example.final_project

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.final_project.databinding.ActivityInputStatsBinding
import com.example.final_project.databinding.InputStatsItemBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InputStatsActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE_PLAYER_SELECT = 101
        const val EXTRA_STAT_TYPE = "stat_type"
        const val EXTRA_SELECTED_PLAYER = "selected_player"
    }

    private lateinit var binding: ActivityInputStatsBinding

    private var homeScore = 0
    private var awayScore = 0
    private var selectedTeam = "HOME" // optional if you're planning team logic

    private val matchLogList = mutableListOf<String>()
    private lateinit var matchLogAdapter: MatchHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val entry = "$time - $player • $stat • $selectedTeam"
        matchLogList.add(0, entry)
        matchLogAdapter.notifyItemInserted(0)
        binding.recyclerHistory.scrollToPosition(0)
    }


    private fun updateScoreDisplay() {
        binding.txtScoreHome.text = homeScore.toString()
        binding.txtScoreAway.text = awayScore.toString()
    }
}
