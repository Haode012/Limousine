package com.example.limousine

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.limousine.databinding.FragmentHomeBinding
import com.example.limousine.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class ProfileFragment : Fragment() {

    private var imageUri: Uri? = null
    private var storageRef = Firebase.storage
    private var _binding: FragmentProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val database =
            Firebase.database("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val ref = database.getReference("Users/$userId")

        ref.get().addOnSuccessListener {
            val name = it.child("fullName").value as String
            val email = it.child("email").value as String
            val address = it.child("homeAddress").value as String
            val phoneNumber = it.child("phoneNumber").value as String
            val birthDate = it.child("dateOfBirth").value as String
            val gender = it.child("gender").value as String
            val profilePic = it.child("profilePicture").child("url").value

            // Check if the profilePic is null or an empty string
            if (profilePic == null || profilePic.toString().isEmpty()) {
                // Set a default image from resources when no profile picture is available
                binding.imgProfile.setImageResource(R.drawable.ic_baseline_account_circle_white_24)
            } else {
                // Load the profile picture using Glide
                val profilePicUrl = profilePic.toString()
                Glide.with(this).load(profilePicUrl).into(binding.imgProfile)
            }

            binding.etFullName.setText(name)
            binding.etEmail.setText(email)
            binding.etHomeAddress.setText(address)
            binding.etPhoneNumber.setText(phoneNumber)
            binding.etDate.setText(birthDate)

            if (gender == "Male") {
                binding.radMale.isChecked = true
            } else if (gender == "Female") {
                binding.radFemale.isChecked = true
            }

            binding.btnEditProfile.setOnClickListener {
                findNavController().navigate(R.id.action_nav_profile_to_nav_editProfile)
            }

            binding.fltEdit.setOnClickListener {
                findNavController().navigate(R.id.action_nav_profile_to_nav_editProfile)
            }

            binding.btnChangePassword.setOnClickListener {
                findNavController().navigate(R.id.action_nav_profile_to_nav_changePassword)
            }
        }
    }
}