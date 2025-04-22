package com.example.final_project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class PlayerGridAdapter(
    private val players: List<String>, // Change to Player model if needed
    private val onPlayerSelected: (String) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = players.size

    override fun getItem(position: Int): Any = players[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val context = parent?.context
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.player_grid_item, parent, false)

        val playerName = view.findViewById<TextView>(R.id.txtPlayerName)
        playerName.text = players[position]

        view.setOnClickListener {
            onPlayerSelected(players[position])
        }

        return view
    }
}
