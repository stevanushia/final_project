package com.example.final_project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class InputStatsAdapter(private val players: List<DummyPlayer>) :
    RecyclerView.Adapter<InputStatsAdapter.StatsViewHolder>() {

    class StatsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById<TextView>(R.id.playerName)
        val tvPoints = itemView.findViewById<TextView>(R.id.tvPoints)
        val btnPlus = itemView.findViewById<Button>(R.id.btnPtsPlus)
        val btnMinus = itemView.findViewById<Button>(R.id.btnPtsMinus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.input_stats_item, parent, false)
        return StatsViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatsViewHolder, position: Int) {
        val player = players[position]
        holder.name.text = player.name
        holder.tvPoints.text = player.points.toString()

        holder.btnPlus.setOnClickListener {
            player.points++
            notifyItemChanged(position)
        }

        holder.btnMinus.setOnClickListener {
            if (player.points > 0) player.points--
            notifyItemChanged(position)
        }
    }

    override fun getItemCount() = players.size
}