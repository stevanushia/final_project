package com.example.final_project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.GetCredentialRequest
import com.example.final_project.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private var isLoginMode = true // To track if we are in login or register mode

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        // Configure Google Sign-In options
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id)) // Ensure this is correctly defined in strings.xml
                    .requestEmail()
                    .build()

        // Initialize GoogleSignInClient
                googleSignInClient = GoogleSignIn.getClient(this, gso)


        // ActivityResultLauncher for Google Sign-in
        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val intent = result.data
                if (intent != null) {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
                    try {
                        val account = task.getResult(ApiException::class.java)!!
                        firebaseAuthWithGoogle(account)
                    } catch (e: ApiException) {
                        // Google Sign-in failed, update UI appropriately
                        Log.w("Google Sign-in", "Google sign in failed", e)
                        Toast.makeText(this, "Google Sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        hideProgressBar()
                    }
                } else {
                    hideProgressBar()
                }
            } else {
                hideProgressBar()
            }
        }

        binding.loginButton.setOnClickListener {
            if (isLoginMode) {
                loginWithEmailPassword()
            } else {
                registerWithEmailPassword()
            }
        }

        binding.googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }

        binding.registerTextView.setOnClickListener {
            toggleLoginRegisterMode()
        }

        updateUIMode() // Set initial UI for login mode
    }

    private fun loginWithEmailPassword() {
        val email = binding.emailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        showProgressBar()
        binding.loginButton.isEnabled = false // Prevent double clicks

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                hideProgressBar()
                binding.loginButton.isEnabled = true // Re-enable button
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = firebaseAuth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("EmailPassword", "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    private fun registerWithEmailPassword() {
        val email = binding.emailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        showProgressBar()
        binding.loginButton.isEnabled = false // Prevent double clicks

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                hideProgressBar()
                binding.loginButton.isEnabled = true // Re-enable button
                if (task.isSuccessful) {
                    // Registration success, user is signed in
                    val user = firebaseAuth.currentUser
                    Toast.makeText(baseContext, "Registration successful!", Toast.LENGTH_SHORT).show()
                    updateUI(user) // Optionally navigate directly after registration
                } else {
                    // If registration fails, display a message to the user
                    Log.w("EmailPassword", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Registration failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    private fun signInWithGoogle() {
        if (::googleSignInClient.isInitialized) {
            showProgressBar()
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        } else {
            Toast.makeText(this, "Google Sign-In is not configured properly", Toast.LENGTH_SHORT).show()
        }
    }


    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d("Google Sign-in", "firebaseAuthWithGoogle:" + acct.id)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                hideProgressBar()
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = firebaseAuth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("Google Sign-in", "firebaseSignInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "Google Sign-in failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            Toast.makeText(this, "Login Successful! Welcome ${user.displayName ?: user.email}", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "Sign-in failed. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
        binding.loginButton.isEnabled = false
        binding.googleSignInButton.isEnabled = false
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
        binding.loginButton.isEnabled = true
        binding.googleSignInButton.isEnabled = true
    }

    private fun toggleLoginRegisterMode() {
        isLoginMode = !isLoginMode
        updateUIMode()
    }

    private fun updateUIMode() {
        if (isLoginMode) {
            binding.titleTextView.text = "Welcome Back!"
            binding.subtitleTextView.text = "Login to continue"
            binding.loginButton.text = "Login"
            binding.registerTextView.text = "Don't have an account? Register"
        } else {
            binding.titleTextView.text = "Join Us!"
            binding.subtitleTextView.text = "Register for an account"
            binding.loginButton.text = "Register"
            binding.registerTextView.text = "Already have an account? Login"
        }
    }

    // Optional: Check if user is already signed in when Activity starts
    override fun onStart() {
        super.onStart()
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            // User is already logged in, go directly to MainActivity
            startActivity(Intent(this, MenuActivity::class.java))
            finish() // Close LoginActivity
        }
    }
}