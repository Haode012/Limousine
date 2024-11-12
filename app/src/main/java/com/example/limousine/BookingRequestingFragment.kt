package com.example.limousine

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
import com.example.limousine.databinding.FragmentBookingRequestingBinding
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

class BookingRequestingFragment : Fragment() {
    private var _binding: FragmentBookingRequestingBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentBookingRequestingBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    @SuppressLint("MissingInflatedId")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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


        binding.tvBookingId.setText(id)
        binding.tvPickUpLocation2.setText(pickUpLocation)
        binding.tvDropOffLocation2.setText(dropOffLocation)
        binding.tvPickUpDate2.setText(pickUpDate)
        binding.tvPickUpTime2.setText(pickUpTime)
        binding.tvDistance2.setText(distance)
        binding.tvTypeOfVehicle.setText(typeOfVehicle)
        binding.tvNumOfPax.setText(pax)
        binding.tvNumOfLuggages.setText(luggage)
        binding.tvPrice3.setText(price)
    }
}