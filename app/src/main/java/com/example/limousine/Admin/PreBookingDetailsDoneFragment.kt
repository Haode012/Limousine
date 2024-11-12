package com.example.limousine.Admin

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.example.limousine.R
import com.example.limousine.databinding.FragmentContactsAdminBinding
import com.example.limousine.databinding.FragmentPreBookingDetailsBinding
import com.example.limousine.databinding.FragmentPreBookingDetailsDoneBinding
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.HashMap

class PreBookingDetailsDoneFragment : Fragment() {
    private var _binding: FragmentPreBookingDetailsDoneBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentPreBookingDetailsDoneBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    @SuppressLint("MissingInflatedId")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val profilePicture = requireArguments().getString("profilePicture").toString()
        val name = requireArguments().getString("name").toString()
        val phoneNumber = requireArguments().getString("phoneNumber").toString()
        val id = requireArguments().getString("id").toString()
        val pickUpLocation = requireArguments().getString("pickUpLocation").toString()
        val dropOffLocation = requireArguments().getString("dropOffLocation").toString()
        val pickUpDate = requireArguments().getString("pickUpDate").toString()
        val pickUpTime = requireArguments().getString("pickUpTime").toString()
        val distance = requireArguments().getString("distance").toString()
        val duration = requireArguments().getString("duration").toString()
        val price = requireArguments().getString("price").toString()
        val typeOfVehicle = requireArguments().getString("typeOfVehicle").toString()
        val pax = requireArguments().getString("pax").toString()
        val luggage = requireArguments().getString("luggage").toString()
        val uid = requireArguments().getString("uid").toString()

        Glide.with(requireContext())
            .load(profilePicture)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.imgProfile2.setImageResource(R.drawable.ic_baseline_account_circle_blue_24)
                    return true // Return true to indicate that the error was handled
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Drawable>?,
                    dataSource: com.bumptech.glide.load.DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    // Image loaded successfully
                    return false // Return false to allow default behavior
                }
            })
            .into(binding.imgProfile2)


        binding.etName.setText(name)
        binding.etPhoneNum.setText(phoneNumber)
        binding.etBookingID.setText(id)
        binding.etPickUpLocation3.setText(pickUpLocation)
        binding.etDropOffLocation3.setText(dropOffLocation)
        binding.etPickUpDate4.setText(pickUpDate)
        binding.etPickUpTime3.setText(pickUpTime)
        binding.etDistance3.setText(distance)
        binding.etTypeOfVehicle3.setText(typeOfVehicle)
        binding.etTotalPassengers3.setText(pax)
        binding.etTotalLuggages.setText(luggage)
        binding.etPrice2.setText(price)
        binding.tvUid.setText(uid)

        binding.btnDeleteBooking.setOnClickListener {
            val uid = binding.tvUid.text.toString().trim()
            val id = binding.etBookingID.text.toString().trim()

                val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.delete_booking_dialog_layout, null)
                val dialog = AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .create()

                val buttonYes = dialogView.findViewById<Button>(R.id.dialog_button_yes)
                val buttonCancel = dialogView.findViewById<Button>(R.id.dialog_button_cancel)

                buttonYes.setOnClickListener {
                    deleteBooking(uid, id)
                    dialog.dismiss()
                }

                buttonCancel.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()

                // Perform the actions after the dialog confirmation
                true
        }

        binding.btnEditBookingDetails.setOnClickListener {
            findNavController().navigate(R.id.action_nav_preBookingDetailsDone_to_nav_editBookingDetailsDone, Bundle().apply {
                putString("id", id)
                putString("pickUpLocation", pickUpLocation)
                putString("dropOffLocation", dropOffLocation)
                putString("pickUpDate", pickUpDate)
                putString("pickUpTime", pickUpTime)
                putString("distance", distance)
                putString("duration", duration)
                putString("typeOfVehicle", typeOfVehicle)
                putString("pax", pax)
                putString("luggage", luggage)
                putString("price", price)
                putString("uid", uid)
            })
        }

        binding.fltEditBooking.setOnClickListener{
            findNavController().navigate(R.id.action_nav_preBookingDetailsDone_to_nav_editBookingDetailsDone, Bundle().apply {
                putString("id", id)
                putString("pickUpLocation", pickUpLocation)
                putString("dropOffLocation", dropOffLocation)
                putString("pickUpDate", pickUpDate)
                putString("pickUpTime", pickUpTime)
                putString("distance", distance)
                putString("duration", duration)
                putString("typeOfVehicle", typeOfVehicle)
                putString("pax", pax)
                putString("luggage", luggage)
                putString("price", price)
                putString("uid", uid)
            })
        }
    }

    private fun deleteBooking(uid: String, id: String) {
        val database =  Firebase.database("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val ref = database.getReference("Pre-Booking (Done)").child(uid).child(id)
        ref.removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    findNavController().navigate(R.id.action_nav_preBookingDetailsDone_to_nav_home_admin)
                    Toast.makeText(requireContext(), "Booking Deleted Successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to Delete Booking", Toast.LENGTH_SHORT).show()
                }
            }
    }
}