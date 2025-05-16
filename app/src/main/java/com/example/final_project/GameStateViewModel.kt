package com.example.final_project

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GameStateViewModel : ViewModel() {
    val gameTime = MutableLiveData("12:00")
    val quarter = MutableLiveData("Q1")
    val homeScore = MutableLiveData(0)
    val awayScore = MutableLiveData(0)
}