package com.example.limousine

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.limousine.databinding.FragmentChangePasswordBinding
import com.example.limousine.databinding.FragmentHomeBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordFragment : Fragment() {

    private var _binding: FragmentChangePasswordBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnConfirm.setOnClickListener {
            if(validation()){
                val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.change_password_dialog_layout, null)
                val dialog = AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .create()

                val buttonYes = dialogView.findViewById<Button>(R.id.dialog_button_yes)
                val buttonCancel = dialogView.findViewById<Button>(R.id.dialog_button_cancel)

                buttonYes.setOnClickListener {
                    resetPassword()
                    dialog.dismiss()
                }

                buttonCancel.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()

                // Perform the actions after the dialog confirmation
                true
            }
        }

    }


    private fun validation(): Boolean {

        val newPassword = binding.etNewPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        val specialCharacters = setOf('!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '_', '-', '+', '=', '[', ']', '{', '}', '|', '\\', ':', ';', '<', '>', ',', '.', '?', '/')

        if (newPassword.isEmpty()){
            binding.etNewPassword.error = "Password is required"
            return false
        } else if (newPassword.length < 8){
            binding.etNewPassword.error = "Password must have at least 8 characters"
            return false
        } else if (!newPassword.any { it.isUpperCase() }){
            binding.etNewPassword.error = "Password must contain at least one uppercase letter"
            return false
        } else if (!newPassword.any { it.isLowerCase() }){
            binding.etNewPassword.error = "Password must contain at least one lowercase letter"
            return false
        } else if (!newPassword.any { it.isDigit() }) {
            binding.etNewPassword.error = "Password must contain at least one digit"
            return false
        } else if (!newPassword.any { it in specialCharacters }) {
            binding.etNewPassword.error = "Password must contain at least one special character"
            return false
        }
        if (confirmPassword.isEmpty()){
            binding.etConfirmPassword.error = "Confirm Password is required"
            return false
        } else if (newPassword != confirmPassword) {
            binding.etConfirmPassword.error = "Password is not matching"
            return false
        }
        return true
    }

    private fun resetPassword() {
        val newPasswd = binding.etNewPassword.text.toString()
        val userId = FirebaseAuth.getInstance().currentUser

        userId!!.updatePassword(newPasswd).addOnCompleteListener { task ->
            if(task.isSuccessful){
                findNavController().navigate(R.id.action_nav_changePassword_to_nav_profile)
                Toast.makeText(context, "Change Password Successfully", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(context, "Failed to change password", Toast.LENGTH_SHORT).show()
            }
        }
    }

}