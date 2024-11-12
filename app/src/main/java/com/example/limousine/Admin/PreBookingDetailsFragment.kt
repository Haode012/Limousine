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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class PreBookingDetailsFragment : Fragment() {
    private var _binding: FragmentPreBookingDetailsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentPreBookingDetailsBinding.inflate(inflater, container, false)
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
            findNavController().navigate(R.id.action_nav_preBookingDetails_to_nav_editBookingDetails, Bundle().apply {
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
            findNavController().navigate(R.id.action_nav_preBookingDetails_to_nav_editBookingDetails, Bundle().apply {
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

        binding.btnRequestDone.setOnClickListener {
            val uid = binding.tvUid.text.toString().trim()
            val id = binding.etBookingID.text.toString().trim()

            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.request_done_dialog_layout, null)
            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()

            val buttonYes = dialogView.findViewById<Button>(R.id.dialog_button_yes)
            val buttonCancel = dialogView.findViewById<Button>(R.id.dialog_button_cancel)

            buttonYes.setOnClickListener {
                deleteRequest(uid, id)
                addRequestDone(uid, id, name, phoneNumber, pickUpLocation, dropOffLocation, pickUpDate, pickUpTime, distance, typeOfVehicle, pax, luggage, price)
                dialog.dismiss()
            }

            buttonCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()

            // Perform the actions after the dialog confirmation
            true
        }

        binding.btnSendToCustomer.setOnClickListener {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser

                if (currentUser != null) {
                    val senderId = currentUser.uid
                    val receiverId = binding.tvUid.text.toString()
                    val currentTimeMillis = System.currentTimeMillis()
                    val sdf = SimpleDateFormat("HH:mm:ss")
                    val formattedTime = sdf.format(Date(currentTimeMillis))

                    val sdfDate = SimpleDateFormat("dd/MM/yyyy")
                    val formattedDate = sdfDate.format(Date(currentTimeMillis))

                    val database = Firebase.database("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    val reference = database.getReference("Chat")
                    val key = reference.push().key

                    if (key != null) {
                        val hashMap = HashMap<String, Any>()
                        hashMap["id"] = key
                        hashMap["senderId"] = senderId
                        hashMap["receiverId"] = receiverId
                        hashMap["currentTime"] = formattedTime
                        hashMap["currentDate"] = formattedDate
                        hashMap["unread"] = "Unread Messages"

                        val bookingDetailsString = "Your Pre-Booking Details (Edited)\n\n\n" +
                                "Booking ID: \n$key\n\n" +
                                "Customer Name: $name\n\n" +
                                "Phone Number: $phoneNumber\n\n" +
                                "Pick Up Location: \n$pickUpLocation\n\n" +
                                "Drop Off Location: \n$dropOffLocation\n\n" +
                                "Pick Up Date: $pickUpDate\n\n" +
                                "Pick Up Time: $pickUpTime\n\n" +
                                "Distance: $distance\n\n" +
                                "Type Of Vehicle: $typeOfVehicle\n\n" +
                                "Total Passengers: $pax\n\n" +
                                "Total Luggages: $luggage\n\n" +
                                "Total Price: $price\n"

                        hashMap["message"] = bookingDetailsString

                        reference.child(key).setValue(hashMap)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(requireContext(), "Sent booking details to customer", Toast.LENGTH_SHORT).show()
                                    // Pass the chat ID to updateUnreadCount
                                    updateUnreadCount(senderId, receiverId)
                                } else {
                                    Toast.makeText(requireContext(), "Failed to Send", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(context, "Failed to Send", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Failed to Send", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Handle exceptions
            }
        }
    }

    private fun updateUnreadCount(senderId: String, receiverId: String) {
        // Initialize a reference to your Firebase Realtime Database
        val database = Firebase.database("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")

        // Create a reference to the location where you store unread message counts
        val unreadCountsRef = database.getReference("UnreadCount")

        // Create a unique key based on the combination of senderId and receiverUid
        val uniqueKey = "$senderId-$receiverId"

        unreadCountsRef.child(uniqueKey).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currentCount = dataSnapshot.getValue(Int::class.java) ?: 0
                val newCount = currentCount + 1
                dataSnapshot.ref.setValue(newCount)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error
            }
        })
    }


    private fun deleteBooking(uid: String, id: String) {
        val database =  Firebase.database("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val ref = database.getReference("Pre-Booking").child(uid).child(id)
        ref.removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    findNavController().navigate(R.id.action_nav_preBookingDetails_to_nav_home_admin)
                    Toast.makeText(requireContext(), "Booking Deleted Successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to Delete Booking", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun deleteRequest(uid: String, id: String) {
        val database =  Firebase.database("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val ref = database.getReference("Pre-Booking").child(uid).child(id)
        ref.removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                } else {
                }
            }
    }


    private fun addRequestDone(uid: String, id: String, name:String, phoneNumber:String, pickUpLocation: String, dropOffLocation: String, pickUpDate: String, pickUpTime: String, distance: String, typeOfVehicle: String, pax:String, luggage:String, price: String) {
        val database =
            Firebase.database("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val ref = database.getReference("Pre-Booking (Done)").child(uid)
        val hashMap = HashMap<String, Any>()
        hashMap["uid"] = uid
        hashMap["id"] = id
        hashMap["customerName"] = name
        hashMap["phoneNumber"] = phoneNumber
        hashMap["distance"] = distance
        hashMap["pickUpLocation"] = pickUpLocation
        hashMap["dropOffLocation"] = dropOffLocation
        hashMap["pickUpDate"] = pickUpDate
        hashMap["pickUpTime"] = pickUpTime
        hashMap["typeOfVehicle"] = typeOfVehicle
        hashMap["pax"] = pax
        hashMap["luggage"] = luggage
        hashMap["price"] = price

        ref.child(id).setValue(hashMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    findNavController().navigate(R.id.action_nav_preBookingDetails_to_nav_home_admin)
                    Toast.makeText(context, "Booking Request Completed", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to Send", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }
}