package com.example.limousine

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.limousine.databinding.FragmentEditProfileBinding
import com.example.limousine.databinding.FragmentHomeBinding
import com.example.limousine.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.fragment_register.*
import java.util.*

class EditProfileFragment : Fragment() {

    private var imageUri: Uri? = null
    private var storageRef = Firebase.storage
    private lateinit var progressBar : ProgressBar

    private var _binding: FragmentEditProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ccp = binding.countryCode2
        ccp.registerCarrierNumberEditText(et_phoneNumber)

        binding.etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
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

        val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val database =
            Firebase.database("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val ref = database.getReference("Users/$userId")

        ref.get().addOnSuccessListener {
            val name = it.child("fullName").value as String
            val address = it.child("homeAddress").value as String
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
            binding.etHomeAddress.setText(address)
            binding.etDate.setText(birthDate)

            if (gender == "Male") {
                binding.radMale.isChecked = true
            } else if (gender == "Female") {
                binding.radFemale.isChecked = true
            }
        }

        val galleryImg = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback {
                binding.imgProfile.setImageURI(it)
                imageUri = it
            })

        binding.imgProfile.setOnClickListener {
            galleryImg.launch("image/*")
        }

        binding.btnChangeProfile.setOnClickListener {
            if (validateInput()) {
                val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.change_profile_dialog_layout, null)
                val dialog = AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .create()

                val buttonYes = dialogView.findViewById<Button>(R.id.dialog_button_yes)
                val buttonCancel = dialogView.findViewById<Button>(R.id.dialog_button_cancel)

                buttonYes.setOnClickListener {
                    saveProfileInformation()
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

        binding.tvRemoveImage.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
            val databaseReference =
                FirebaseDatabase.getInstance("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference("Users/$userId")

            val updates = mapOf<String, Any?>("profilePicture" to mapOf("url" to ""))

            databaseReference.updateChildren(updates)
                .addOnSuccessListener {
                    Toast.makeText(context, "Profile picture back to default", Toast.LENGTH_SHORT).show()
                    binding.imgProfile.setImageResource(R.drawable.ic_baseline_account_circle_white_24)
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to remove profile picture", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun validateInput(): Boolean {
        val fullName = binding.etFullName.text.toString()
        val homeAddress = binding.etHomeAddress.text.toString()
        val phoneNum = binding.etPhoneNumber.text.toString()
        val group = binding.radGender.checkedRadioButtonId
        val dateOfBirth = binding.etDate.text.toString()

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
            Toast.makeText(context, "Date Of Birth cannot be letter", Toast.LENGTH_SHORT).show()
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
        return true
    }

    private fun saveProfileInformation() {
        try {
            progressBar = binding.progressBar2
            progressBar.indeterminateDrawable.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.app_theme),
                PorterDuff.Mode.SRC_IN
            )
            progressBar.setVisibility(View.VISIBLE)

            if (binding.radMale.isChecked) {
                val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
                val databaseReference =
                    FirebaseDatabase.getInstance("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
                        .getReference("Users/$userId")

                val updates = mutableMapOf<String, Any?>()

                val countryCodePicker = binding.countryCode2.selectedCountryCode
                val phoneNum = binding.etPhoneNumber.text.toString()

                val phoneNumWithCCP = "+$countryCodePicker$phoneNum"

                updates["fullName"] = binding.etFullName.text.toString()
                updates["homeAddress"] = binding.etHomeAddress.text.toString()
                updates["phoneNumber"] = phoneNumWithCCP
                updates["dateOfBirth"] = binding.etDate.text.toString()
                updates["gender"] = "Male"

                if (imageUri != null) {
                    val storageRef = FirebaseStorage.getInstance().reference
                    val imageRef =
                        storageRef.child("Images").child(System.currentTimeMillis().toString())
                    imageRef.putFile(imageUri!!)
                        .addOnSuccessListener { task ->
                            task.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                                val imageMap = mapOf("url" to uri.toString())
                                updates["profilePicture"] = imageMap

                                databaseReference.updateChildren(updates)
                                    .addOnSuccessListener {
                                        progressBar.visibility = View.GONE
                                        findNavController().navigate(R.id.action_nav_editProfile_to_nav_profile)
                                        Toast.makeText(
                                            context,
                                            "Change Profile Successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .addOnFailureListener {
                                        progressBar.visibility = View.GONE
                                        Toast.makeText(
                                            context,
                                            "Failed to edit profile",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        }
                        .addOnFailureListener {
                            progressBar.visibility = View.GONE
                            Toast.makeText(
                                context,
                                "Error uploading profile picture",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    databaseReference.updateChildren(updates)
                        .addOnSuccessListener {
                            progressBar.visibility = View.GONE
                            findNavController().navigate(R.id.action_nav_editProfile_to_nav_profile)
                            Toast.makeText(
                                context,
                                "Change Profile Successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener {
                            progressBar.visibility = View.GONE
                            Toast.makeText(context, "Failed to edit profile", Toast.LENGTH_SHORT)
                                .show()
                        }
                }
            } else if (binding.radFemale.isChecked) {
                val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
                val databaseReference =
                    FirebaseDatabase.getInstance("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
                        .getReference("Users/$userId")

                val updates = mutableMapOf<String, Any?>()

                val countryCodePicker = binding.countryCode2.selectedCountryCode
                val phoneNum = binding.etPhoneNumber.text.toString()

                val phoneNumWithCCP = "$countryCodePicker$phoneNum"

                updates["fullName"] = binding.etFullName.text.toString()
                updates["homeAddress"] = binding.etHomeAddress.text.toString()
                updates["phoneNumber"] = phoneNumWithCCP
                updates["dateOfBirth"] = binding.etDate.text.toString()
                updates["gender"] = "Female"

                if (imageUri != null) {
                    val storageRef = FirebaseStorage.getInstance().reference
                    val imageRef =
                        storageRef.child("Images").child(System.currentTimeMillis().toString())
                    imageRef.putFile(imageUri!!)
                        .addOnSuccessListener { task ->
                            task.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                                val imageMap = mapOf("url" to uri.toString())
                                updates["profilePicture"] = imageMap

                                databaseReference.updateChildren(updates)
                                    .addOnSuccessListener {
                                        progressBar.visibility = View.GONE
                                        findNavController().navigate(R.id.action_nav_editProfile_to_nav_profile)
                                        Toast.makeText(
                                            context,
                                            "Changed Profile Successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            context,
                                            "Failed to edit profile",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        }
                        .addOnFailureListener {
                            progressBar.visibility = View.GONE
                            Toast.makeText(
                                context,
                                "Error uploading profile picture",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    databaseReference.updateChildren(updates)
                        .addOnSuccessListener {
                            progressBar.visibility = View.GONE
                            findNavController().navigate(R.id.action_nav_editProfile_to_nav_profile)
                            Toast.makeText(
                                context,
                                "Changed Profile Successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener {
                            progressBar.visibility = View.GONE
                            Toast.makeText(context, "Failed to edit profile", Toast.LENGTH_SHORT)
                                .show()
                        }
                }
            }
        } catch (e:Exception) {

        }
    }
}