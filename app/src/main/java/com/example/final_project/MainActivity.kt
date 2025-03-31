package com.example.final_project

import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.final_project.databinding.ActivityMainBinding // Import for your main layout binding
import com.example.final_project.databinding.ModalEditLayoutBinding // Import for modal layout binding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.IgnoreExtraProperties

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var modalBinding: ModalEditLayoutBinding // Binding for the modal layout
    private lateinit var database: DatabaseReference
    private var currentUserId: String? = null

    @IgnoreExtraProperties
    data class User(val username: String? = null, val email: String? = null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var isDataLoaded = false
        installSplashScreen().setKeepOnScreenCondition {
            !isDataLoaded
        }
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        modalBinding = ModalEditLayoutBinding.bind(binding.root) // Bind the modal layout to the inflated root view

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference

        // Set onClickListener for the main Edit button to show the modal
        binding.btnEdit.setOnClickListener {
            showModal()
        }

        // Set onClickListener for the Close button in the modal
        modalBinding.btnCloseModal.setOnClickListener {
            hideModal()
        }

        // Optional: Close modal when clicking on the background
        modalBinding.modalEditContainer.setOnClickListener {
            hideModal()
        }

        // Example click listeners for modal buttons (replace with your actual logic)
        modalBinding.btnEditModal.setOnClickListener {
            Toast.makeText(this, "Edit Button Clicked", Toast.LENGTH_SHORT).show()
            // Add your Edit functionality here
        }
        modalBinding.btnLiveModal.setOnClickListener {
            Toast.makeText(this, "Live Button Clicked", Toast.LENGTH_SHORT).show()
            // Add your Live functionality here
        }
        modalBinding.btnShotClockModal.setOnClickListener {
            Toast.makeText(this, "Shot Clock Button Clicked", Toast.LENGTH_SHORT).show()
            // Add your Shot Clock functionality here
        }
        modalBinding.btnBreakModal.setOnClickListener {
            Toast.makeText(this, "Break Button Clicked", Toast.LENGTH_SHORT).show()
            // Add your Break functionality here
        }
        modalBinding.btnSwitchModal.setOnClickListener {
            Toast.makeText(this, "Switch Button Clicked", Toast.LENGTH_SHORT).show()
            // Add your Switch functionality here
        }
        modalBinding.btnBuzzerModal.setOnClickListener {
            Toast.makeText(this, "Buzzer Button Clicked", Toast.LENGTH_SHORT).show()
            // Add your Buzzer functionality here
        }
        modalBinding.btnWhistleModal.setOnClickListener {
            Toast.makeText(this, "Whistle Button Clicked", Toast.LENGTH_SHORT).show()
            // Add your Whistle functionality here
        }
        modalBinding.btnFoulModal.setOnClickListener {
            Toast.makeText(this, "Foul Button Clicked", Toast.LENGTH_SHORT).show()
            // Add your Foul functionality here
        }
        modalBinding.btnResetModal.setOnClickListener {
            Toast.makeText(this, "Reset Button Clicked", Toast.LENGTH_SHORT).show()
            // Add your Reset functionality here
        }

        isDataLoaded = true
    }

    private fun showModal() {
        modalBinding.modalEditContainer.visibility = View.VISIBLE
    }

    private fun hideModal() {
        modalBinding.modalEditContainer.visibility = View.GONE
    }
}