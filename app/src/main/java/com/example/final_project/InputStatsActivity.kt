package com.example.final_project

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.final_project.databinding.ActivityInputStatsBinding
import com.example.final_project.databinding.InputStatsItemBinding

class InputStatsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInputStatsBinding

    private val dummyPlayers = listOf(
        DummyPlayer("John"),
        DummyPlayer("Mike"),
        DummyPlayer("Kevin"),
        DummyPlayer("Jake"),
        DummyPlayer("Chris")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}