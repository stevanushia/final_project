package com.example.final_project

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.final_project.databinding.MatchHistoryItemBinding

class MatchHistoryAdapter(
    private val logs: List<String>
) : RecyclerView.Adapter<MatchHistoryAdapter.LogViewHolder>() {

    inner class LogViewHolder(val binding: MatchHistoryItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val binding = MatchHistoryItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val log = logs[position]
        val parts = log.split(" • ")

        // This shows only the main text, e.g. "12:05 - John . 3PT"
        holder.binding.txtLogEntry.text = parts[0]

        // Color based on team info (if available)
        val team = parts.getOrNull(1)
        val color = when (team) {
            "HOME" -> Color.parseColor("#00FF00")
            "AWAY" -> Color.parseColor("#FF5555")
            else -> Color.WHITE
        }

        holder.binding.txtLogEntry.setTextColor(color)
    }


    override fun getItemCount(): Int = logs.size
}

