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

        holder.binding.txtLogEntry.text = parts.take(2).joinToString(" • ")

        val team = parts.getOrNull(2)
        val color = when (team) {
            "HOME" -> Color.parseColor("#00FF00") // green
            "AWAY" -> Color.parseColor("#FF5555") // red
            else -> Color.WHITE
        }

        holder.binding.txtLogEntry.setTextColor(color)
    }

    override fun getItemCount(): Int = logs.size
}

