package com.example.final_project

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class PlayerSelectionActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_selection)

        tabLayout = findViewById(R.id.tabLayoutTeams)
        viewPager = findViewById(R.id.viewPagerPlayers)

        val adapter = PlayerSelectionPagerAdapter(this)
        viewPager.adapter = adapter

        // Attach tabs to ViewPager
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) "Home" else "Away"
        }.attach()

        // Handle Cancel button
        findViewById<TextView>(R.id.btnCancel).setOnClickListener {
            finish()
        }
    }

}