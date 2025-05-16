package com.example.final_project

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelProvider

object GameViewModelProvider {
    private val viewModelStore = ViewModelStore()
    private val factory = ViewModelProvider.NewInstanceFactory()

    val gameStateViewModel: GameStateViewModel by lazy {
        ViewModelProvider(viewModelStore, factory)[GameStateViewModel::class.java]
    }
}