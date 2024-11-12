package com.example.limousine.Admin

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.limousine.MainActivity
import com.example.limousine.R
import com.example.limousine.databinding.FragmentLoginAdminBinding
import com.example.limousine.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_login.*

class LoginAdminFragment : Fragment() {
    private var _binding: FragmentLoginAdminBinding? = null

    private lateinit var auth: FirebaseAuth

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var progressBar : ProgressBar

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentLoginAdminBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Access the color from the application theme
        val customColor = resources.getColor(R.color.admin, requireContext().theme)


        // Set the status bar color
        activity?.window?.statusBarColor = customColor


        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

//        // Initialize SharedPreferences
//        sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
//
//        // Load the saved credentials and update the UI accordingly
//        val rememberMeChecked = sharedPreferences.getBoolean("rememberMe", false)// Retrieve remember me preference from SharedPreferences
//            if (rememberMeChecked) {
//                // Retrieve saved email and password from SharedPreferences
//                val savedEmail = sharedPreferences.getString("email", "")
//                val savedPassword = sharedPreferences.getString("password", "")
//                binding.etEmail.setText(savedEmail)
//                binding.etPassword.setText(savedPassword)
//                binding.chkRememberMe.isChecked = true
//            }

        binding.btnSignIn.setOnClickListener {
            performLogin()
        }

//        binding.igbtnVisibilityOff.setOnClickListener {
//        if (binding.igbtnVisibilityOff.drawable.constantState?.equals(
//                    resources.getDrawable(R.drawable.ic_baseline_visibility_off_24)?.constantState
//                ) == true){
//            binding.igbtnVisibilityOff.setImageResource(R.drawable.ic_baseline_visibility_24)
//            binding.etPassword.transformationMethod = null
//            // Move cursor to the end of the text
//            binding.etPassword.setSelection(binding.etPassword.text.length)
//        } else {
//            binding.igbtnVisibilityOff.setImageResource(R.drawable.ic_baseline_visibility_off_24)
//            binding.etPassword.transformationMethod = PasswordTransformationMethod()
//            // Move cursor to the end of the text
//            binding.etPassword.setSelection(binding.etPassword.text.length)
//        }
//
//        }
        binding.fltBack.setOnClickListener{
           /* // Clear the stored email, password, and rememberMe preference from SharedPreferences
            val editor = sharedPreferences.edit()
            editor.remove("email")
            editor.remove("password")
            editor.remove("rememberMe")
            editor.apply()
*/
            findNavController().navigate(R.id.action_loginAdminFragment_to_loginFragment)
        }
    }

    private fun performLogin() {

        progressBar = binding.progressBar
        progressBar.indeterminateDrawable.setColorFilter(
            ContextCompat.getColor(requireContext(), R.color.admin),
            PorterDuff.Mode.SRC_IN
        )
        progressBar.setVisibility(View.VISIBLE)

        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()

//        if (binding.chkRememberMe.isChecked) {
//            // Save email and password to SharedPreferences
//            val editor = sharedPreferences.edit()
//            editor.putBoolean("rememberMe", true)
//            editor.putString("email", email)
//            editor.putString("password", password)
//            editor.apply()
//        } else {
//            // Clear saved email and password from SharedPreferences
//            val editor = sharedPreferences.edit()
//            editor.remove("rememberMe")
//            editor.remove("email")
//            editor.remove("password")
//            editor.apply()
//        }

        if (email.isEmpty()) {
            progressBar.setVisibility(View.GONE)
            binding.etEmail.error = "Email is required"
        } else if (password.isEmpty()) {
            progressBar.setVisibility(View.GONE)
            binding.etPassword.error = "Password is required"
        } else {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    progressBar.visibility =
                        View.GONE // Hide the progress bar regardless of the outcome

                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val uid = user?.uid

                        if (uid != null) {
                            val database =
                                Firebase.database("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
                            val userRef = database.getReference("Users").child(uid)

                            // Add the user to the "OnlineUsers" node
                            val onlineUsersRef = database.getReference("OnlineUsers").child(uid)
                            onlineUsersRef.setValue(true)

                            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        val userType =
                                            snapshot.child("userType").getValue(String::class.java)

                                        if (userType == "Admin") {
                                            // User is of type "User", proceed with login
                                            Toast.makeText(
                                                requireContext(),
                                                "Welcome",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            val intent = Intent(context, AdminActivity::class.java)
                                            startActivity(intent)
                                        } else {
                                            // User is not of type "User", deny login
                                            auth.signOut()
                                            Toast.makeText(
                                                requireContext(),
                                                "You are not authorized to log in.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } else {
                                        // User data not found
                                        Toast.makeText(
                                            requireContext(),
                                            "User data not found.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    // Handle database error
                                    Toast.makeText(
                                        requireContext(),
                                        "Database error: " + error.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            })
                        } else {
                            // Handle situation where user's UID is not available
                            Toast.makeText(
                                requireContext(),
                                "User data not found.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        // Login failed, show error message
                        Toast.makeText(
                            requireContext(),
                            "Email or/and Password incorrect",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }
}