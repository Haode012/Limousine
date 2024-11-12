package com.example.limousine.Admin

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.limousine.Adapter.UserDetailsAdapter
import com.example.limousine.PreBookingFragment
import com.example.limousine.R
import com.example.limousine.databinding.FragmentEditBookingDetailsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_contacts_admin.*
import kotlinx.android.synthetic.main.fragment_pre_booking.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class EditBookingDetailsFragment : Fragment() {
    private var _binding: FragmentEditBookingDetailsBinding? = null
    private val PICKUP_AUTOCOMPLETE_REQUEST_CODE = 101
    private val DROPOFF_AUTOCOMPLETE_REQUEST_CODE = 102
    private lateinit var pickupLatLng: LatLng
    private lateinit var dropOffLatLng: LatLng

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentEditBookingDetailsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = requireArguments().getString("id").toString()
        val pickUpLocation = requireArguments().getString("pickUpLocation").toString()
        val dropOffLocation = requireArguments().getString("dropOffLocation").toString()
        val pickUpDate = requireArguments().getString("pickUpDate").toString()
        val pickUpTime = requireArguments().getString("pickUpTime").toString()
        val distance = requireArguments().getString("distance").toString()
        val typeOfVehicle = requireArguments().getString("typeOfVehicle").toString()
        val pax = requireArguments().getString("pax").toString()
        val luggage = requireArguments().getString("luggage").toString()
        val price = requireArguments().getString("price").toString()
        val uid = requireArguments().getString("uid").toString()

        binding.etBookingID3.setText(id)
        binding.etPickUpLocation2.setText(pickUpLocation)
        binding.etDropOffLocation2.setText(dropOffLocation)
        binding.etPickUpDate2.setText(pickUpDate)
        binding.etPickUpTime2.setText(pickUpTime)
        binding.etDistance.setText(distance)
        binding.etTypeOfVehicle.setText(typeOfVehicle)
        binding.etPassengers.setText(pax)
        binding.etLuggages2.setText(luggage)
        binding.etPrice.setText(price)
        binding.tvUid.setText(uid)

        binding.btnEditBookingDetails.setOnClickListener{
            if (validateInput()){
                val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.edit_booking_dialog_layout, null)
                val dialog = AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .create()

                val buttonYes = dialogView.findViewById<Button>(R.id.dialog_button_yes)
                val buttonCancel = dialogView.findViewById<Button>(R.id.dialog_button_cancel)

                buttonYes.setOnClickListener {
                    editData()
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

        binding.etTypeOfVehicle.setOnClickListener {

            val typeOfVehicle = arrayOf("Sedan", "MPV", "Van")

            val builder = android.app.AlertDialog.Builder(requireContext())
            builder.setTitle("Choose a vehicle")

            // Set the list of items and their click handler
            builder.setItems(typeOfVehicle) { dialog, which ->
                val selectedTypeOfVehicle = typeOfVehicle[which]
                binding.etTypeOfVehicle.setText(selectedTypeOfVehicle)
                dialog.dismiss() // Close the dialog
            }

            val dialog = builder.create()
            dialog.show()
        }

        binding.etPassengers.setOnClickListener {
            val pax = arrayOf("1", "2", "3", "4", "5", "6", "7")

            val builder = android.app.AlertDialog.Builder(requireContext())
            builder.setTitle("How many passengers?")

            // Set the list of items and their click handler
            builder.setItems(pax) { dialog, which ->
                val selectedPax = pax[which]
                binding.etPassengers.setText(selectedPax)

                dialog.dismiss() // Close the dialog
            }

            val dialog = builder.create()
            dialog.show()
        }

        binding.etLuggages2.setOnClickListener {
            val luggage = arrayOf("1", "2", "3", "4", "5")

            val builder = android.app.AlertDialog.Builder(requireContext())
            builder.setTitle("How many luggages?")

            // Set the list of items and their click handler
            builder.setItems(luggage) { dialog, which ->
                val selectedLuggage = luggage[which]
                binding.etLuggages2.setText(selectedLuggage)

                dialog.dismiss() // Close the dialog
            }

            val dialog = builder.create()
            dialog.show()
        }

        binding.etPickUpDate2.setOnClickListener {
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
                    binding.etPickUpDate2.setText(selectedDate)
                },
                currentYear,
                currentMonth,
                currentDay
            )

            // Show the date picker dialog
            datePickerDialog.show()


            // Customize the OK and Cancel button colors programmatically
            val positiveButton = datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE)
            val negativeButton = datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE)

            // Set the color for OK button
            positiveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.admin))

            // Set the color for Cancel button
            negativeButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.admin))
        }

        binding.etPickUpTime2.setOnClickListener {
            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(
                requireContext(),
                TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                    // Handle the selected time (hourOfDay and minute) here
                    // Update the text in the etPickUpTime EditText with the selected time
                    binding.etPickUpTime2.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute))
                },
                currentHour,
                currentMinute,
                true // Set this to true to enable the spinner mode
            )
            timePickerDialog.show()

            // Customize the OK and Cancel button colors programmatically
            val positiveButton = timePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE)
            val negativeButton = timePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE)

            // Set the color for OK button
            positiveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.admin))

            // Set the color for Cancel button
            negativeButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.admin))
        }

        binding.etBookingID3.setOnClickListener{
            Toast.makeText(requireContext(), "Not allowed to edit Booking ID", Toast.LENGTH_SHORT).show()
        }

        binding.etDistance.setOnClickListener{
            Toast.makeText(requireContext(), "Not allow to edit Distance", Toast.LENGTH_SHORT).show()
        }

        //Edit text autocomplete
        //Initialize places
        Places.initialize(requireContext(),"AIzaSyBb7DhVMt1Q2JF8dM8tL31KvxLVXAfw7gY")

        // Set EditText non focusable
        binding.etPickUpLocation2.isFocusable = false
        binding.etDropOffLocation2.isFocusable = false

        binding.etPickUpLocation2.setOnClickListener {
            openPlaceAutocompleteForResult(PICKUP_AUTOCOMPLETE_REQUEST_CODE)
        }

        binding.etDropOffLocation2.setOnClickListener {
            val locations = arrayOf("Subang Airport", "KLIA1", "KLIA2")

            val builder = android.app.AlertDialog.Builder(requireContext())
            builder.setTitle("Choose Location")

            // Set the list of items and their click handler
            builder.setItems(locations) { dialog, which ->
                val selectedLocation = locations[which]
                binding.etDropOffLocation2.setText(selectedLocation)
                dialog.dismiss() // Close the dialog
            }

            val dialog = builder.create()
            dialog.show()
        }

        binding.etPickUpLocation2.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                 performSearchIfBothLocationsSelected()
            }
        })

        binding.etDropOffLocation2.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                 performSearchIfBothLocationsSelected()
            }
        })
    }

    private fun performSearchIfBothLocationsSelected() {
        val pickupLocation = binding.etPickUpLocation2.text.toString()
        val dropOffLocation = binding.etDropOffLocation2.text.toString()

        if (pickupLocation.isNotEmpty() && dropOffLocation.isNotEmpty()) {
            searchArea()
        }
    }

    private fun searchArea() {
        val pickupLocation = binding.etPickUpLocation2.text.toString()
        val dropOffLocation = binding.etDropOffLocation2.text.toString()


        val geocoder = Geocoder(requireContext())
        var pickupAddress: Address? = null
        var dropOffAddress: Address? = null

        try {
            pickupAddress = geocoder.getFromLocationName(pickupLocation, 1)?.firstOrNull()
            dropOffAddress = geocoder.getFromLocationName(dropOffLocation, 1)?.firstOrNull()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // Check if both pickup and drop-off addresses are successfully fetched
        if (pickupAddress != null && dropOffAddress != null) {
            // Initialize pickupLatLng and dropOffLatLng
            val pickupLatLng = LatLng(pickupAddress.latitude, pickupAddress.longitude)
            val dropOffLatLng = LatLng(dropOffAddress.latitude, dropOffAddress.longitude)

            // Calculate distance, time, and perform other actions
            val distanceInMeters = calculateHaversineDistance(
                pickupLatLng.latitude, pickupLatLng.longitude,
                dropOffLatLng.latitude, dropOffLatLng.longitude
            )

            // Assume a fixed speed in meters per second
            val speedMetersPerSecond = 15.0 // Change this value as needed

            // Calculate time in seconds
            val durationInSeconds = distanceInMeters / speedMetersPerSecond

            // Convert duration from seconds to minutes
            val durationInMinutes = durationInSeconds.toInt() / 60
            val remainingSeconds = durationInSeconds.toInt() % 60

            // Calculate hours if duration is over 59 minutes
            val durationInHours = durationInMinutes / 60
            val remainingMinutes = durationInMinutes % 60

            // Calculate price based on rate per kilometer
            val ratePerKilometer = 2 // Replace with your actual rate per kilometer
            val price = ratePerKilometer * (distanceInMeters / 1000) // Convert meters to kilometers

            // Update EditText with distance, estimated time, and price
            binding.etDistance.setText("%.1f km".format(distanceInMeters / 1000))

            binding.etPrice.setText("RM%.2f".format(price)) // Display the calculated price
        }
    }

    private fun calculateHaversineDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val R = 6371000 // Earth's radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }

        private fun openPlaceAutocompleteForResult(requestCode: Int) {
        val fieldList = listOf(
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG,
            Place.Field.NAME
        )

        val intent = Autocomplete.IntentBuilder(
            AutocompleteActivityMode.OVERLAY,
            fieldList
        ).build(requireContext())

        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            val place = Autocomplete.getPlaceFromIntent(data!!)
            val address = place.address
            val latLng = place.latLng

            when (requestCode) {
                PICKUP_AUTOCOMPLETE_REQUEST_CODE -> {
                    binding.etPickUpLocation2.setText(address)
                    pickupLatLng = latLng
                }
                DROPOFF_AUTOCOMPLETE_REQUEST_CODE -> {
                    binding.etDropOffLocation2.setText(address)
                    dropOffLatLng = latLng
                }
            }
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            val status = Autocomplete.getStatusFromIntent(data!!)
            Toast.makeText(requireContext(), status.statusMessage, Toast.LENGTH_SHORT).show()
        }
    }



    private fun validateInput(): Boolean {
        val pickUpLocation = binding.etPickUpLocation2.text.toString()
        val dropOffLocation = binding.etDropOffLocation2.text.toString()
        val pickUpDate = binding.etPickUpDate2.text.toString()
        val pickUpTime = binding.etPickUpTime2.text.toString()
        val distance = binding.etDistance.text.toString()
        val typeOfVehicle = binding.etTypeOfVehicle.text.toString()
        val pax = binding.etPassengers.text.toString()
        val luggage = binding.etLuggages2.text.toString()
        val price = binding.etPrice.text.toString()
        val currentDate = Calendar.getInstance().time
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formattedCurrentDate = sdf.format(currentDate)

        if(pickUpLocation.isEmpty()){
            Toast.makeText(requireContext(), "Pick Up Location is required", Toast.LENGTH_SHORT).show()
            return false
        }
        if(dropOffLocation.isEmpty()){
            Toast.makeText(requireContext(), "Drop Off Location is required", Toast.LENGTH_SHORT).show()
            return false
        }
        if(pickUpDate.isEmpty()){
            Toast.makeText(requireContext(), "Pick Up Date is required", Toast.LENGTH_SHORT).show()
            return false
        } else if (pickUpDate.matches(Regex("^[a-zA-Z].*$"))){
            Toast.makeText(requireContext(), "Pick Up Date cannot be letter", Toast.LENGTH_SHORT).show()
            return false
        } else if (!pickUpDate.matches(Regex("^\\d{2}/\\d{2}/\\d{4}$"))){
            Toast.makeText(requireContext(), "Pick Up Date must be in format dd/MM/yyyy", Toast.LENGTH_SHORT).show()
            return false
        }  else {
            val parts = pickUpDate.split("/")
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
                Toast.makeText(requireContext(), "Invalid Pick Up Date", Toast.LENGTH_SHORT).show()
                return false
            }
            val oneDaysLaterCalendar = Calendar.getInstance()
            oneDaysLaterCalendar.add(Calendar.DAY_OF_MONTH, 1)
            val pickedDate = sdf.parse(pickUpDate)

            if (pickedDate != null && pickedDate.before(oneDaysLaterCalendar.time)) {
                Toast.makeText(requireContext(), "Pick Up Date must be at least 1 days after the current date ($formattedCurrentDate).", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        if (pickUpTime.isEmpty()) {
            Toast.makeText(requireContext(), "Pick Up Time is required", Toast.LENGTH_SHORT).show()
            return false
        } else {
            val timeParts = pickUpTime.split(":");
            if (timeParts.size != 2) {
                Toast.makeText(requireContext(), "Invalid Time format. Use hour:minutes", Toast.LENGTH_SHORT).show()
                return false
            }

            val hour = timeParts[0].toIntOrNull();
            val minute = timeParts[1].toIntOrNull();

            if (hour == null || minute == null) {
                Toast.makeText(requireContext(), "Pick Up Time must be in format hh:mm", Toast.LENGTH_SHORT).show()
                return false
            }

            if (hour < 0 || hour > 23) {
                Toast.makeText(requireContext(), "Hour must be between 0 and 23", Toast.LENGTH_SHORT).show()
                return false
            } else if ( minute < 0 || minute > 59){
                Toast.makeText(requireContext(), "Minutes must be between between 0 and 59", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        if(distance.isEmpty()){
            Toast.makeText(requireContext(), "Distance is required", Toast.LENGTH_SHORT).show()
            return false
        }
        if(typeOfVehicle.isEmpty()){
            Toast.makeText(requireContext(), "Type Of Vehicle is required", Toast.LENGTH_SHORT).show()
            return false
        }
        if(pax.isEmpty()){
            Toast.makeText(requireContext(), "Total Passengers is required", Toast.LENGTH_SHORT).show()
            return false
        }
        if(luggage.isEmpty()){
            Toast.makeText(requireContext(), "Total Luggages is required", Toast.LENGTH_SHORT).show()
            return false
        }
        if (typeOfVehicle == "Sedan" && pax !in listOf("1", "2", "3")) {
            Toast.makeText(requireContext(), "Maximum 3 passengers only allowed", Toast.LENGTH_SHORT).show()
            return false
        }
        if (typeOfVehicle == "MPV" && pax !in listOf("1", "2", "3", "4")) {
            Toast.makeText(requireContext(), "Maximum 4 passengers only allowed", Toast.LENGTH_SHORT).show()
            return false
        }
        if (typeOfVehicle == "Sedan" && luggage !in listOf("1", "2")) {
            Toast.makeText(requireContext(), "Maximum 2 luggages only allowed", Toast.LENGTH_SHORT).show()
            return false
        }
        if (typeOfVehicle == "MPV" && pax !in listOf("1", "2", "3")) {
            Toast.makeText(requireContext(), "Maximum 3 luggages only allowed", Toast.LENGTH_SHORT).show()
            return false
        }
        if(price.isEmpty()){
            Toast.makeText(requireContext(), "Price is required", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun editData() {
        val pickUpLocation = binding.etPickUpLocation2.text.toString()
        val dropOffLocation = binding.etDropOffLocation2.text.toString()
        val pickUpDate = binding.etPickUpDate2.text.toString()
        val pickUpTime = binding.etPickUpTime2.text.toString()
        val distance = binding.etDistance.text.toString()
        val typeOfVehicle = binding.etTypeOfVehicle.text.toString()
        val pax = binding.etPassengers.text.toString()
        val luggage = binding.etLuggages2.text.toString()
        val price = binding.etPrice.text.toString()
        val id = binding.etBookingID3.text.toString()
        val uid = binding.tvUid.text.toString()

        try{
            val hashMap = HashMap<String, Any>()
            hashMap["pickUpLocation"] = "$pickUpLocation"
            hashMap["dropOffLocation"] = "$dropOffLocation"
            hashMap["pickUpDate"] = "$pickUpDate"
            hashMap["pickUpTime"] = "$pickUpTime"
            hashMap["distance"] = "$distance"
            hashMap["typeOfVehicle"] = "$typeOfVehicle"
            hashMap["pax"] = "$pax"
            hashMap["luggage"] = "$luggage"
            hashMap["price"] = "$price"

            val database =  Firebase.database("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
            val ref = database.getReference("Pre-Booking").child(uid).child(id)
            ref.updateChildren(hashMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        findNavController().navigate(R.id.action_nav_editBookingDetails_to_nav_home_admin)
                        Toast.makeText(requireContext(), "Edited Successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Failed to Edit", Toast.LENGTH_SHORT).show()
                    }
                }
        } catch (e: Exception){

        }
    }
}