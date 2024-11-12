package com.example.limousine

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.limousine.Model.ChatModel
import com.example.limousine.databinding.FragmentPreBookingBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.fragment_pre_booking.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PreBookingFragment : Fragment(), OnMapReadyCallback
    //, LocationListener, GoogleMap.OnCameraMoveListener, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraIdleListener
{

    private var mMap: GoogleMap? = null

    lateinit var mapView: MapView

    private val MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    private val DEFAULT_ZOOM = 15f

    private var fusedLocationProviderClient: FusedLocationProviderClient? = null

    private var end_latitude = 0.0
    private var end_longtitude = 0.0
    private var origin: MarkerOptions? = null
    private var destination: MarkerOptions ?= null
    private var latitude = 0.0
    private var longtitude = 0.0

    private lateinit var pickupLatLng: LatLng
    private lateinit var dropOffLatLng: LatLng

    private val PICKUP_AUTOCOMPLETE_REQUEST_CODE = 101
    private val DROPOFF_AUTOCOMPLETE_REQUEST_CODE = 102
    val REQUEST_CODE_PERMISSION = 123

    private val chatModelArrayList: ArrayList<ChatModel> = ArrayList()

    private var _binding: FragmentPreBookingBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentPreBookingBinding.inflate(inflater, container, false)

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etPickUpDate.setOnClickListener {
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
                    binding.etPickUpDate.setText(selectedDate)
                },
                currentYear,
                currentMonth,
                currentDay
            )

            // Show the date picker dialog
            datePickerDialog.show()
        }

        binding.etPickUpTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(
                requireContext(),
                TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                    // Handle the selected time (hourOfDay and minute) here
                    // Update the text in the etPickUpTime EditText with the selected time
                    binding.etPickUpTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute))
                },
                currentHour,
                currentMinute,
                true // Set this to true to enable the spinner mode
            )
            timePickerDialog.show()
        }

        // Assuming you have already declared `mapView` as a property in your Fragment class.
        mapView = binding.map1

        // Request both fine and coarse location permissions if not granted
        val fineLocationPermission = Manifest.permission.ACCESS_FINE_LOCATION
        val coarseLocationPermission = Manifest.permission.ACCESS_COARSE_LOCATION

        val fineLocationGranted = ContextCompat.checkSelfPermission(requireContext(), fineLocationPermission) == PackageManager.PERMISSION_GRANTED
        val coarseLocationGranted = ContextCompat.checkSelfPermission(requireContext(), coarseLocationPermission) == PackageManager.PERMISSION_GRANTED

        if (!fineLocationGranted || !coarseLocationGranted) {
            val permissionsToRequest = ArrayList<String>()

            if (!fineLocationGranted) {
                permissionsToRequest.add(fineLocationPermission)
            }

            if (!coarseLocationGranted) {
                permissionsToRequest.add(coarseLocationPermission)
            }

            ActivityCompat.requestPermissions(requireActivity(), permissionsToRequest.toTypedArray(), REQUEST_CODE_PERMISSION)
        }

        getCurrentLocation()

        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)
        }

        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)

//        binding.btnClear.setOnClickListener{
//            mapView.onCreate(mapViewBundle)
//            mapView.getMapAsync(this)
//        }

        //Edit text autocomplete
        //Initialize places
        Places.initialize(requireContext(),"AIzaSyBb7DhVMt1Q2JF8dM8tL31KvxLVXAfw7gY")

        // Set EditText non focusable
        binding.etPickUpLocation.isFocusable = false
        binding.etDropOffLocation.isFocusable = false

        binding.etPickUpLocation.setOnClickListener {
            openPlaceAutocompleteForResult(PICKUP_AUTOCOMPLETE_REQUEST_CODE)
        }

        binding.etDropOffLocation.setOnClickListener {
//            openPlaceAutocompleteForResult(DROPOFF_AUTOCOMPLETE_REQUEST_CODE)

            val locations = arrayOf("Subang Airport", "KLIA1", "KLIA2")

            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Choose Location")

// Set the list of items and their click handler
            builder.setItems(locations) { dialog, which ->
                val selectedLocation = locations[which]
                binding.etDropOffLocation.setText(selectedLocation)
                dialog.dismiss() // Close the dialog
            }

            val dialog = builder.create()
            dialog.show()
       }

        binding.etPickUpLocation.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                performSearchIfBothLocationsSelected()
            }
        })

        binding.etDropOffLocation.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                performSearchIfBothLocationsSelected()
            }
        })

        val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val database =
            Firebase.database("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val ref = database.getReference("Users/$userId")

        ref.get().addOnSuccessListener {
            val name = it.child("fullName").value as String
            val phoneNumber = it.child("phoneNumber").value as String

            binding.tvCustomerName.setText(name)
            binding.tvCustomerPhone.setText(phoneNumber)
        }

        binding.btnContinue.setOnClickListener{
           if (validateInput()){
               // saveData()
               storeData()
               findNavController().navigate(R.id.action_nav_pre_booking_to_nav_vehicle)
            }
        }
    }

    private fun validateInput():Boolean {
        val pickUpLocation = binding.etPickUpLocation.text.toString()
        val dropOffLocation = binding.etDropOffLocation.text.toString()
        val pickUpDate = binding.etPickUpDate.text.toString()
        val pickUpTime = binding.etPickUpTime.text.toString()
        val distance = binding.tvDistance.text.toString()
        val currentDate = Calendar.getInstance().time
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formattedCurrentDate = sdf.format(currentDate)

       if (pickUpLocation.isEmpty()) {
           Toast.makeText(requireContext(), "Pick Up Location is required", Toast.LENGTH_SHORT).show()
           return false
        }
        if (dropOffLocation.isEmpty()){
            Toast.makeText(requireContext(), "Drop Off Location is required", Toast.LENGTH_SHORT).show()
            return false
        }
        if (pickUpLocation == dropOffLocation && distance == "0.0 km"){
            Toast.makeText(requireContext(), "Both location can't be same", Toast.LENGTH_SHORT).show()
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
        return true
    }


    private fun storeData() {
        val sharedPreferences = requireContext().getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE)

        val distance = binding.tvDistance.text.toString()
        val price = binding.tvPrice2.text.toString()
        val pickUpLocation = binding.etPickUpLocation.text.toString()
        val dropOffLocation = binding.etDropOffLocation.text.toString()
        val pickUpDate = binding.etPickUpDate.text.toString()
        val pickUpTime = binding.etPickUpTime.text.toString()
        val customerName = binding.tvCustomerName.text.toString()
        val phoneNumber = binding.tvCustomerPhone.text.toString()

        val editor = sharedPreferences.edit()
        editor.putString("distance", distance)
        editor.putString("price", price)
        editor.putString("pickUpLocation", pickUpLocation)
        editor.putString("dropOffLocation", dropOffLocation)
        editor.putString("pickUpDate", pickUpDate)
        editor.putString("pickUpTime", pickUpTime)
        editor.putString("customerName", customerName)
        editor.putString("phoneNumber", phoneNumber)
        editor.apply()
    }



  /*  private fun saveData() {
        try {
            val currentUser = FirebaseAuth.getInstance().currentUser

            if (currentUser != null) {
                val userUID = currentUser.uid
                val senderId = "QYQ559BbH3TcfpAtwUzNoYSfAL72"

                val database =
                    Firebase.database("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
                val reference = database.getReference("Pre-Booking")
                val key = reference.push().key

                // Rest of your booking creation logic
                val distance = binding.tvDistance.text.toString()
              //  val duration = binding.tvDuration.text.toString()
              //  val price = binding.tvPrice.text.toString()
                val pickUpLocation = binding.etPickUpLocation.text.toString()
                val dropOffLocation = binding.etDropOffLocation.text.toString()
                val pickUpDate = binding.etPickUpDate.text.toString()
                val pickUpTime = binding.etPickUpTime.text.toString()
                val customerName = binding.tvCustomerName.text.toString()
                val phoneNumber = binding.tvCustomerPhone.text.toString()


                val hashMap = HashMap<String, Any>()
                hashMap["uid"] = userUID
                hashMap["id"] = "$key"
                hashMap["distance"] = distance
              //  hashMap["duration"] = duration
                // hashMap["price"] = price
                hashMap["customerName"] = customerName
                hashMap["phoneNumber"] = phoneNumber
                hashMap["pickUpLocation"] = pickUpLocation
                hashMap["dropOffLocation"] = dropOffLocation
                hashMap["pickUpDate"] = pickUpDate
                hashMap["pickUpTime"] = pickUpTime

                // Update the user's booking using their UID and the generatedKey
                val userBookingsRef = reference.child(userUID)
                userBookingsRef.child("$key")
                    .setValue(hashMap).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Create the booking details string
                            val bookingDetailsString = "Your Pre-Booking Details\n\n" +
                                    "Booking ID: \n$key\n" +
                                    "Customer Name: $customerName\n" +
                                    "Phone Number: $phoneNumber\n" +
                                    "Pick Up Location: \n$pickUpLocation\n" +
                                    "Drop Off Location: \n$dropOffLocation\n" +
                                    "Pick Up Date: $pickUpDate\n" +
                                    "Pick Up Time: $pickUpTime\n" +
                                    "Distance: $distance\n"
                                   // "Duration: $duration\n" +
                                   // "Price: $price"

                            val currentUser = FirebaseAuth.getInstance().currentUser

                            if (currentUser != null) {
                                val receiverId = currentUser.uid
                                val database =
                                    Firebase.database("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
                                val reference = database.getReference("Chat")
                                val key = reference.push().key
                                // Get the current time
                                val currentTimeMillis = System.currentTimeMillis()

                                // Format current time as hh:mm
                                val sdf = SimpleDateFormat("HH:mm:ss")
                                val formattedTime = sdf.format(Date(currentTimeMillis))

                                // Format current date as dd/mm/yyyy
                                val sdfDate = SimpleDateFormat("dd/MM/yyyy")
                                val formattedDate = sdfDate.format(Date(currentTimeMillis))

                                val hashMap = HashMap<String, Any>()
                                hashMap["id"] = "$key"
                                hashMap["senderId"] = senderId
                                hashMap["receiverId"] = receiverId
                                hashMap["message"] = bookingDetailsString
                                hashMap["currentTime"] = formattedTime
                                hashMap["currentDate"] = formattedDate
                                hashMap["unread"] = "Unread Messages"

                                if (key != null) {
                                    reference.child(key).setValue(hashMap)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                updateUnreadCount(receiverId)
                                                // Handle successful booking
                                                showCustomDialogBox()
                                            } else {
                                            }
                                        }
                                } else {
                                    Toast.makeText(context, "Failed to Send", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            } else {
                                // Handle booking failure
                                Toast.makeText(context, "Failed to Add", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
            }
        } catch (e: Exception) {
            // Handle exceptions
        }
    }*/


    private fun showCustomDialogBox() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        val dialogView = requireActivity().layoutInflater.inflate(R.layout.pre_order_successful, null)

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

    private fun updateUnreadCount(receiverUid: String) {
        // Initialize a reference to your Firebase Realtime Database
        val database = Firebase.database("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")

        val senderId = "QYQ559BbH3TcfpAtwUzNoYSfAL72"

        // Create a reference to the location where you store unread message counts
        val unreadCountsRef = database.getReference("UnreadCount")

        // Create a unique key based on the combination of senderId and receiverUid
        val uniqueKey = "$senderId-$receiverUid"

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

    private fun performSearchIfBothLocationsSelected() {
        val pickupLocation = binding.etPickUpLocation.text.toString()
        val dropOffLocation = binding.etDropOffLocation.text.toString()

        if (pickupLocation.isNotEmpty() && dropOffLocation.isNotEmpty()) {
            searchArea()
        }
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
                    binding.etPickUpLocation.setText(address)
                    pickupLatLng = latLng
                }
                DROPOFF_AUTOCOMPLETE_REQUEST_CODE -> {
                    binding.etDropOffLocation.setText(address)
                    dropOffLatLng = latLng
                }
            }
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            val status = Autocomplete.getStatusFromIntent(data!!)
            Toast.makeText(requireContext(), status.statusMessage, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        if (googleMap != null) {
            mMap = googleMap
            mapView.onResume()

            binding.igZoomIn.setOnClickListener {
                val newZoom = mMap!!.cameraPosition.zoom + 1
                val maxZoom = mMap!!.maxZoomLevel
                mMap!!.animateCamera(CameraUpdateFactory.zoomTo(newZoom.coerceAtMost(maxZoom)))
            }

            binding.igZoomOut.setOnClickListener {
                val newZoom = mMap!!.cameraPosition.zoom - 1
                val minZoom = mMap!!.minZoomLevel
                mMap!!.animateCamera(CameraUpdateFactory.zoomTo(newZoom.coerceAtLeast(minZoom)))
            }
        }

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mMap!!.setMyLocationEnabled(true)
//        mMap!!.setOnCameraMoveListener(this)
//        mMap!!.setOnCameraMoveStartedListener(this)
//        mMap!!.setOnCameraIdleListener(this)
    }

    private fun searchArea() {
        val pickupLocation = binding.etPickUpLocation.text.toString()
        val dropOffLocation = binding.etDropOffLocation.text.toString()

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

            // Remove any existing markers from the map
            mMap?.clear()

            // Add markers for pickup and drop-off locations
            mMap?.apply {
                // Customize marker icon for pickup location (blue)
                val pickupMarker = addMarker(
                    MarkerOptions()
                        .position(pickupLatLng)
                        .title("Pick Up Location")
                        .snippet(pickupLocation)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                )

                // Customize marker icon for drop-off location (red)
                val dropOffMarker = addMarker(
                    MarkerOptions()
                        .position(dropOffLatLng)
                        .title("Drop Off Location")
                        .snippet(dropOffLocation)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                )

                // Show the title as a tooltip (InfoWindow) without requiring a click
                if (pickupMarker != null && dropOffMarker != null) {
                    pickupMarker.showInfoWindow()
                    dropOffMarker.showInfoWindow()
                }
            }

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

            // Move the camera to a position that shows both markers
            val bounds = LatLngBounds.Builder()
                .include(pickupLatLng)
                .include(dropOffLatLng)
                .build()
            mMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200))

            // Calculate price based on rate per kilometer
            val ratePerKilometer = 1.5 // Replace with your actual rate per kilometer
            val price = ratePerKilometer * (distanceInMeters / 1000) // Convert meters to kilometers

            // Update text view with distance, estimated time, and price
            tvDistance?.text = "%.1f km".format(distanceInMeters / 1000)

            /*if (durationInHours > 0) {
                tvDuration?.text = if (remainingMinutes > 0) {
                    "%d hr %d min".format(durationInHours, remainingMinutes)
                } else {
                    "%d hr".format(durationInHours)
                }
            } else {
                tvDuration?.text = "%d min".format(durationInMinutes)
            }*/

            tv_price2?.text = "RM%.2f".format(price)

            // Draw route between pickup and drop-off locations
            val url = getDirectionsUrl(pickupLatLng, dropOffLatLng)
            if (url != null) {
                val downloadTask: DownloadTask = DownloadTask()
                downloadTask.execute(url)
            }
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

    inner class DownloadTask : AsyncTask<String?, Void?, String>(){
        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            Log.d("API Response", result) // Log the API response
            val parserTask = ParserTask()
            parserTask.execute(result)
        }

        override fun doInBackground(vararg url: String?): String {
            var data = ""
            try{
                data = downloadUrl(url[0].toString()).toString()
            } catch (e: java.lang.Exception){
                Log.d("Background Task", e.toString())
            }
            return data
        }
    }

    inner class ParserTask : AsyncTask<String?, Int?, List<List<HashMap<String, String>>>?>() {
        // Parsing the data in a non-UI thread
        override fun doInBackground(vararg jsonData: String?): List<List<HashMap<String, String>>>? {
            val jObject: JSONObject
            var routes: List<List<HashMap<String, String>>>? = null
            try {
                val jsonString = jsonData[0]
                if (jsonString != null) {
                    jObject = JSONObject(jsonString)
                    val parser = DataParser()
                    routes = parser.parse(jObject)
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return routes
        }

        override fun onPostExecute(result: List<List<HashMap<String, String>>>?) {
            if (result != null) {
                val points = ArrayList<LatLng>()
                val lineOptions = PolylineOptions()

                for (i in result.indices) {
                    val path = result[i]
                    for (j in path.indices) {
                        val point = path[j]
                        val lat = point["lat"]?.toDoubleOrNull() ?: 0.0
                        val lng = point["lng"]?.toDoubleOrNull() ?: 0.0
                        val position = LatLng(lat, lng)
                        points.add(position)
                    }
                    lineOptions.addAll(points)
                    lineOptions.width(8f)
                    lineOptions.color(Color.RED)
                    lineOptions.geodesic(true)

                    if (points.isNotEmpty()) {
                        mMap?.addPolyline(lineOptions)
                    }
                }
            } else {
                // Handle the case where result is null
            }
        }
    }

    private fun downloadUrl(strUrl: String): String? {
        var data = ""
        var iStream: InputStream? = null
        var urlConnection: HttpURLConnection? = null
        try {
            val url = URL(strUrl)
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.connect()

            if (urlConnection.responseCode == HttpURLConnection.HTTP_OK) {
                iStream = urlConnection.inputStream
                val br = BufferedReader(InputStreamReader(iStream))
                val sb = StringBuffer()
                var line: String? = ""
                while (br.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                data = sb.toString()
                br.close()
            } else {
                Log.e("HTTP Error", "Response Code: ${urlConnection.responseCode}")
            }
        } catch (e: Exception) {
            Log.e("Exception", e.toString())
        } finally {
            iStream?.close()
            urlConnection?.disconnect()
        }
        return data
    }

    private fun getDirectionsUrl(origin: LatLng, dest: LatLng): String? {
        val str_origin = "origin=${origin.latitude},${origin.longitude}"
        val str_dest = "destination=${dest.latitude},${dest.longitude}"
        val mode = "mode=driving"
        val parameters = "$str_origin&$str_dest&$mode"
        val output = "json"
        val apiKey = "AIzaSyBb7DhVMt1Q2JF8dM8tL31KvxLVXAfw7gY" // Replace with your actual API key
        return "https://maps.googleapis.com/maps/api/directions/$output?$parameters&key=$apiKey"
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        var mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY)
        if(mapViewBundle == null){
            mapViewBundle = Bundle()
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle)
        }
        mapView.onSaveInstanceState(mapViewBundle)
    }

    private fun getCurrentLocation() {
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        try{
            @SuppressLint("MissingPermission") val location =
                fusedLocationProviderClient!!.getLastLocation()

            location.addOnCompleteListener(object : OnCompleteListener<Location> {
                override fun onComplete(loc: Task<Location>){
                    if(loc.isSuccessful) {
                        val currentLocation = loc.result as Location?
                        if (currentLocation != null) {
//                              moveCamera(
//                                LatLng(currentLocation.latitude, currentLocation.longitude),
//                             DEFAULT_ZOOM
//                  )

                            latitude = currentLocation.latitude
                            longtitude = currentLocation.longitude
                    }
                        } else {
                            Toast.makeText(requireContext(), "Permission location is required for this app", Toast.LENGTH_LONG).show()
                    }
                }
            })
        } catch (e: Exception){

        }
    }

//  private fun moveCamera(latlng: LatLng, zoom: Float){
//          mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, zoom))
//   }

//    override fun onLocationChanged(location: Location) {
//        val geocoder = Geocoder(requireContext(), Locale.getDefault())
//        var addresses: List<Address>? = null
//        try{
//            addresses = geocoder.getFromLocation(location!!.latitude, location.longitude,1)
//        } catch (e: IOException){
//            e.printStackTrace()
//        }
//
//        setAddress(addresses!![0])
//    }
//
//    private fun setAddress(addresses: Address) {
//        if(addresses != null){
//       if(addresses.getAddressLine(0) != null){
//        binding.tvAddress!!.setText(addresses.getAddressLine(0))
//       }
//            if(addresses.getAddressLine(1) != null){
//                binding.tvAddress!!.setText(
//                    binding.tvAddress.getText().toString() + addresses.getAddressLine(1))
//            }
//        }
//    }
//
//    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
//
//    }
//
//    override fun onProviderEnabled(p0: String) {
//
//    }
//
//    override fun onProviderDisabled(p0: String) {
//
//    }
//
//    override fun onCameraMove() {
//        var addresses: List<Address>? = null
//        val geocoder = Geocoder(requireContext(), Locale.getDefault())
//        try{
//            addresses = geocoder.getFromLocation(mMap!!.getCameraPosition().target.latitude, mMap!!.getCameraPosition().target.longitude,1)
//
//            setAddress(addresses!![0])
//        } catch (e: IndexOutOfBoundsException){
//            e.printStackTrace()
//        } catch (e: IOException){
//            e.printStackTrace()
//        }
//    }
//
//    override fun onCameraMoveStarted(p0: Int) {
//        var addresses: List<Address>? = null
//        val geocoder = Geocoder(requireContext(), Locale.getDefault())
//        try{
//            addresses = geocoder.getFromLocation(mMap!!.getCameraPosition().target.latitude, mMap!!.getCameraPosition().target.longitude,1)
//
//            setAddress(addresses!![0])
//        } catch (e: IndexOutOfBoundsException){
//            e.printStackTrace()
//        } catch (e: IOException){
//            e.printStackTrace()
//        }
//    }
//
//    override fun onCameraIdle() {
//        var addresses: List<Address>? = null
//        val geocoder = Geocoder(requireContext(), Locale.getDefault())
//        try{
//            addresses = geocoder.getFromLocation(mMap!!.getCameraPosition().target.latitude, mMap!!.getCameraPosition().target.longitude,1)
//
//            setAddress(addresses!![0])
//        } catch (e: IndexOutOfBoundsException){
//            e.printStackTrace()
//        } catch (e: IOException){
//            e.printStackTrace()
//        }
//    }
}