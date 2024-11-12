package com.example.limousine

import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.limousine.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.hbb20.CountryCodePicker
import kotlinx.android.synthetic.main.fragment_register.*
import java.util.*

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null

    private lateinit var auth: FirebaseAuth

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val ccp = binding.countryCode
        ccp.registerCarrierNumberEditText(et_phoneNumber)

        binding.etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                R.style.DatePickerDialogTheme,
                DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    // Handle the selected date (year, month, and day) here
                    // Update the text in the etPickUpDate EditText with the selected date
                    val selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year)
                    binding.etDate.setText(selectedDate)
                },
                currentYear,
                currentMonth,
                currentDay
            )

            // Show the date picker dialog
            datePickerDialog.show()
        }

        binding.fltBack.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        binding.btnSignUp.setOnClickListener {

            if (validateInput()) {
                performRegister()
            }
        }
        binding.igbtnVisibilityOff.setOnClickListener {
            if (binding.igbtnVisibilityOff.drawable.constantState?.equals(
                    resources.getDrawable(R.drawable.ic_baseline_visibility_off_24)?.constantState
                ) == true){
                binding.igbtnVisibilityOff.setImageResource(R.drawable.ic_baseline_visibility_24)
                binding.etPassword.transformationMethod = null
                // Move cursor to the end of the text
                binding.etPassword.setSelection(binding.etPassword.text.length)
            } else {
                binding.igbtnVisibilityOff.setImageResource(R.drawable.ic_baseline_visibility_off_24)
                binding.etPassword.transformationMethod = PasswordTransformationMethod()
                // Move cursor to the end of the text
                binding.etPassword.setSelection(binding.etPassword.text.length)
            }
        }

        binding.igbtnVisibilityOff2.setOnClickListener {
            if (binding.igbtnVisibilityOff2.drawable.constantState?.equals(
                    resources.getDrawable(R.drawable.ic_baseline_visibility_off_24)?.constantState
                ) == true){
                binding.igbtnVisibilityOff2.setImageResource(R.drawable.ic_baseline_visibility_24)
                binding.etConfirmPassword.transformationMethod = null
                // Move cursor to the end of the text
                binding.etConfirmPassword.setSelection(binding.etConfirmPassword.text.length)
            } else {
                binding.igbtnVisibilityOff2.setImageResource(R.drawable.ic_baseline_visibility_off_24)
                binding.etConfirmPassword.transformationMethod = PasswordTransformationMethod()
                // Move cursor to the end of the text
                binding.etConfirmPassword.setSelection(binding.etConfirmPassword.text.length)
            }
        }
    }

    private fun validateInput():Boolean {
        val fullName = binding.etFullName.text.toString()
        val homeAddress = binding.etHomeAddress.text.toString()
        val phoneNum = binding.etPhoneNumber.text.toString()
        val group = binding.radGender.checkedRadioButtonId
        val dateOfBirth = binding.etDate.text.toString()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        val specialCharacters = setOf('!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '_', '-', '+', '=', '[', ']', '{', '}', '|', '\\', ':', ';', '<', '>', ',', '.', '?', '/')

        if (fullName.isEmpty()){
            binding.etFullName.error = "Full Name is required"
            return false
        }else if (fullName.matches(Regex(".*\\d.*"))) {
            binding.etFullName.error = "Full Name cannot have numbers"
            return false
        } else if(fullName.count { it.isLetter() } < 5){
            binding.etFullName.error = "Full Name must have at least 5 letters"
            return false
        }
        if(homeAddress.isEmpty()){
            binding.etHomeAddress.error = "Home Address is required"
            return false
        } else if(homeAddress.count { it.isLetter() } < 5){
            binding.etHomeAddress.error = "Home Address must have at least 5 letters"
            return false
        }
        if(phoneNum.isEmpty()){
            binding.etPhoneNumber.error = "Phone Number is required"
            return false
        } else if (phoneNum.matches(Regex("^[a-zA-Z].*$"))){
            binding.etPhoneNumber.error = "Phone Number cannot have letters"
            return false
        }
        if(group == -1){
            Toast.makeText(context, "Select your gender (Male / Female)", Toast.LENGTH_SHORT).show()
            return false
        }
        if(dateOfBirth.isEmpty()){
            Toast.makeText(context, "Date Of Birth is required", Toast.LENGTH_SHORT).show()
            return false
        } else if (dateOfBirth.matches(Regex("^[a-zA-Z].*$"))){
            Toast.makeText(context, "Date Of Birth cannot have letters", Toast.LENGTH_SHORT).show()
            return false
        } else if (!dateOfBirth.matches(Regex("^\\d{2}/\\d{2}/\\d{4}$"))){
            Toast.makeText(context, "Date Of Birth must be in format dd/MM/yyyy", Toast.LENGTH_SHORT).show()
            return false
        }  else {
            val parts = dateOfBirth.split("/")
            val day = parts[0].toInt()
            val month = parts[1].toInt()
            val year = parts[2].toInt()

            val isValidDate: Boolean = when (month) {
                1, 3, 5, 7, 8, 10, 12 -> day in 1..31
                4, 6, 9, 11 -> day in 1..30
                2 -> day in 1..28
                else -> false
            }

            if (!isValidDate) {
                Toast.makeText(context, "Invalid Date Of Birth", Toast.LENGTH_SHORT).show()
                return false
            } else {
                val currentDate = Calendar.getInstance()
                val eighteenYearsAgo = currentDate.apply {
                    add(Calendar.YEAR, -18)
                }
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month - 1, day)
                }
                if (selectedDate.after(eighteenYearsAgo)) {
                    Toast.makeText(context, "Less than 18 years old", Toast.LENGTH_SHORT).show()
                    return false
                }
            }
        }

        if(email.isEmpty()){
            binding.etEmail.error = "Email Address is required"
            return false
        } else if(email.count { it.isLetter() } < 3){
            binding.etEmail.error = "Email Address must have at least 3 letters"
            return false
        } else if (!email.matches(Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.com"))){
            binding.etEmail.error = "Email Address must have @ and .com"
            return false
        }
        if (password.isEmpty()){
            binding.etPassword.error = "Password is required"
            return false
        } else if (password.length < 8){
            binding.etPassword.error = "Password must have at least 8 characters"
            return false
        } else if (!password.any { it.isUpperCase() }){
            binding.etPassword.error = "Password must contain at least one uppercase letter"
            return false
        } else if (!password.any { it.isLowerCase() }){
            binding.etPassword.error = "Password must contain at least one lowercase letter"
            return false
        } else if (!password.any { it.isDigit() }) {
            binding.etPassword.error = "Password must contain at least one digit"
            return false
        } else if (!password.any { it in specialCharacters }) {
            binding.etPassword.error = "Password must contain at least one special character"
            return false
        }
        if (confirmPassword.isEmpty()){
            binding.etConfirmPassword.error = "Confirm Password is required"
            return false
        } else if (password != confirmPassword) {
            binding.etConfirmPassword.error = "Confirm Password and Password is not matching"
            return false
        }
        return true
    }

    private fun performRegister() {
        val fullName = binding.etFullName.text.toString()
        val homeAddress = binding.etHomeAddress.text.toString()
        val dateOfBirth = binding.etDate.text.toString()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()

        val countryCodePicker = binding.countryCode.selectedCountryCode
        val phoneNum = binding.etPhoneNumber.text.toString()

        val phoneNumWithCCP = "+$countryCodePicker$phoneNum"

        try {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // User registration successful
                        val firebaseUser: FirebaseUser? = task.result?.user
                        firebaseUser?.let {
                            if (binding.radMale.isChecked) {
                                // Create a HashMap to store user information
                                val userMap = HashMap<String, Any>()
                                userMap["uid"] = it.uid
                                userMap["fullName"] = fullName
                                userMap["homeAddress"] = homeAddress
                                userMap["phoneNumber"] = phoneNumWithCCP
                                userMap["gender"] = "Male"
                                userMap["dateOfBirth"] = dateOfBirth
                                userMap["email"] = email
                                userMap["userType"] = "User"
                                userMap["profilePicture"] = mapOf("url" to "")

                                // Save the user information to the Realtime Database
                                val database =
                                    FirebaseDatabase.getInstance()
                                val usersRef = database.getReference("Users")
                                usersRef.child(it.uid).setValue(userMap)
                                    .addOnCompleteListener { dbTask ->
                                        if (dbTask.isSuccessful) {
                                            showCustomDialogBox()
                                        } else {
                                            // Error saving user data
                                            Toast.makeText(
                                                context,
                                                "Failed to save user data in the database",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                            } else if (binding.radFemale.isChecked) {
                                // Create a HashMap to store user information
                                val userMap = HashMap<String, Any>()
                                userMap["uid"] = it.uid
                                userMap["fullName"] = fullName
                                userMap["homeAddress"] = homeAddress
                                userMap["phoneNumber"] = phoneNumWithCCP
                                userMap["gender"] = "Female"
                                userMap["dateOfBirth"] = dateOfBirth
                                userMap["email"] = email
                                userMap["userType"] = "User"
                                userMap["profilePicture"] = mapOf("url" to "")

                                // Save the user information to the Realtime Database
                                val database =
                                    FirebaseDatabase.getInstance()
                                val usersRef = database.getReference("Users")
                                usersRef.child(it.uid).setValue(userMap)
                                    .addOnCompleteListener { dbTask ->
                                        if (dbTask.isSuccessful) {
                                            showCustomDialogBox()
                                        } else {
                                            // Error saving user data
                                            Toast.makeText(
                                                context,
                                                "Failed to save user data in the database",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                            } else {
                                // User registration failed
                                Toast.makeText(
                                    context,
                                    "Registration failed. Try again later",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        // User registration failed, check the exception
                        val exception = task.exception
                        if (exception is FirebaseAuthUserCollisionException) {
                            // Email already exists in the system
                            Toast.makeText(
                                context,
                                "This email already exist",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
        } catch (e: Exception) {
            Toast.makeText(context, "Registration failed. Try again later", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showCustomDialogBox() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        val dialogView = requireActivity().layoutInflater.inflate(R.layout.register_successful, null)

        // Get a reference to the "ok" button with ID btn_ok
        val okButton = dialogView.findViewById<Button>(R.id.btn_ok)

        okButton.setOnClickListener {
            dialog.dismiss()
        }

        // Set the custom view for the dialog
        dialog.setContentView(dialogView)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

            //      Toast.makeText(requireContext(), "Your account has been registered", Toast.LENGTH_SHORT).show()
            //      findNavController().navigate(R.id.action_registerFragment_to_loginFragment)

        }