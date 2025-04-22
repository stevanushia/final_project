package com.example.final_project

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import androidx.fragment.app.Fragment

class HomePlayersFragment : Fragment() {

    private val dummyPlayers = listOf("John", "Mike", "Chris")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_player_grid, container, false)
        val gridView = view.findViewById<GridView>(R.id.gridViewPlayers)

        gridView.adapter = PlayerGridAdapter(dummyPlayers) { selectedPlayer ->
            sendResultBack(selectedPlayer)
        }

        return view
    }

    private fun sendResultBack(playerName: String) {
        val statType = requireActivity().intent.getStringExtra(InputStatsActivity.EXTRA_STAT_TYPE)

        val resultIntent = Intent().apply {
            putExtra(InputStatsActivity.EXTRA_SELECTED_PLAYER, playerName)
            putExtra(InputStatsActivity.EXTRA_STAT_TYPE, statType)
            putExtra("team", "HOME")
        }
        requireActivity().setResult(Activity.RESULT_OK, resultIntent)
        requireActivity().finish()
    }

}

