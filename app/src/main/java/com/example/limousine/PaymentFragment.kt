package com.example.limousine

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.limousine.databinding.FragmentPaymentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.PaymentSheetResultCallback
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class PaymentFragment : Fragment(){

    // Declare the PaymentSheet and related variables
    private lateinit var paymentSheet: PaymentSheet
    private lateinit var paymentIntentClientSecret: String
    private lateinit var configuration: PaymentSheet.CustomerConfiguration
    // Declare a boolean flag to track whether the fetchApi() operation is in progress
    private var isFetchingApi = false

    private var _binding: FragmentPaymentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentPaymentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireContext().getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE)
        val storedPrice = sharedPreferences.getString("price", "")
        val storedDistance = sharedPreferences.getString("distance", "")
        val storedPickUpLocation = sharedPreferences.getString("pickUpLocation", "")
        val storedDropOffLocation = sharedPreferences.getString("dropOffLocation", "")
        val storedPickUpDate = sharedPreferences.getString("pickUpDate", "")
        val storedPickUpTime = sharedPreferences.getString("pickUpTime", "")
        val storedCustomerName = sharedPreferences.getString("customerName", "")
        val storedPhoneNumber = sharedPreferences.getString("phoneNumber", "")


        val sharedPreferences2 = requireContext().getSharedPreferences("MySharedPreferences2", Context.MODE_PRIVATE)

        val storedVehicle = sharedPreferences2.getString("typeOfVehicle", "")
        val storedPassengers = sharedPreferences2.getString("passenger", "")
        val storedLuggage = sharedPreferences2.getString("luggage", "")

        val sharedPreferences3 = requireContext().getSharedPreferences("MySharedPreferences3", Context.MODE_PRIVATE)
        val storedPrice2 = sharedPreferences3.getString("price", "")
        val storedVehiclePrice = sharedPreferences3.getString("vehiclePrice", "")

        binding.tvCustomerName2.text = storedCustomerName
        binding.tvCustomerPhoneNum.text = storedPhoneNumber
        binding.tvPickUpLocation2.text = storedPickUpLocation
        binding.tvDropOffLocation2.text = storedDropOffLocation
        binding.tvPickUpDate2.text = storedPickUpDate
        binding.tvPickUpTime2.text = storedPickUpTime
        binding.tvDistance2.text = storedDistance

        binding.tvTypeOfVehicle.text = storedVehicle
        binding.tvNumOfPax.text = storedPassengers
        binding.tvNumOfLuggages.text = storedLuggage

        binding.tvPrice3.text = storedPrice + " (Route)"
        binding.tvPrice5.text = "+ " + storedVehiclePrice + " (Vehicle Charge)"
        binding.tvPrice6.text = "= RM" + storedPrice2


        val sharedPreferences6 = requireContext().getSharedPreferences("MySharedPreferences6", Context.MODE_PRIVATE)

        val editor = sharedPreferences6.edit()
        editor.putString("storedPrice2", storedPrice2)
        editor.apply()

        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)

        fetchApi()

        // Button click listener
        binding.btnProceed.setOnClickListener {
            // Check if fetchApi() is in progress
            if (isFetchingApi) {
                // API data is being fetched, show loading Toast
                Toast.makeText(
                    requireContext(),
                    "Loading, click again to proceed",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // API data has been fetched, proceed with PaymentSheet
                if (paymentIntentClientSecret != null) {
                    paymentSheet.presentWithPaymentIntent(
                        paymentIntentClientSecret,
                        PaymentSheet.Configuration(
                            merchantDisplayName = "Pre-Booking Price",
                            customer = configuration
                        )
                    )

                    // Show success message
                    Toast.makeText(
                        requireContext(),
                        "Payment information fetched successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Handle the case where paymentIntentClientSecret is not initialized
                    Toast.makeText(
                        requireContext(),
                        "Payment information not available",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // Handle the result from PaymentSheet
    private fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        when (paymentSheetResult) {
            is PaymentSheetResult.Canceled -> {
                Toast.makeText(requireContext(), "Canceled", Toast.LENGTH_SHORT).show()
            }
            is PaymentSheetResult.Failed -> {
                Toast.makeText(
                    requireContext(),
                    paymentSheetResult.error.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
            is PaymentSheetResult.Completed -> {
                Toast.makeText(requireContext(), "Payment Success", Toast.LENGTH_SHORT).show()
                saveData()
            }
        }
    }

    private fun fetchApi() {
        // Set the flag to true to indicate that fetchApi() is in progress
        isFetchingApi = true

        val queue = Volley.newRequestQueue(requireContext())
        val url = "https://5886-60-50-97-166.ngrok-free.app"

        val sharedPreferences3 =
            requireContext().getSharedPreferences("MySharedPreferences3", Context.MODE_PRIVATE)
        val storedPrice2 = sharedPreferences3.getString("price", "")
        val sharedPreferences =
            requireContext().getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE)
        val storedCustomerName = sharedPreferences.getString("customerName", "")

        val request = object : StringRequest(
            Method.POST, url,
            { response ->
                // Set the flag to false as fetchApi() is completed
                isFetchingApi = false
                try {
                    // Parse the JSON response
                    val jsonResponse = JSONObject(response)
                    configuration = PaymentSheet.CustomerConfiguration(
                        jsonResponse.getString("customer"),
                        jsonResponse.getString("ephemeralKey")
                    )
                    paymentIntentClientSecret = jsonResponse.getString("paymentIntent")

                    PaymentConfiguration.init(
                        requireContext(),
                        jsonResponse.getString("publishableKey")
                    )

                    /*// Handle the successful response here
                    Toast.makeText(
                        requireContext(),
                        "Payment information fetched successfully",
                        Toast.LENGTH_SHORT
                    ).show()*/
                } catch (e: JSONException) {
                   /* // Handle the JSONException here
                    e.printStackTrace()
                    // Show an error toast
                    Toast.makeText(
                        requireContext(),
                        "Error parsing payment information",
                        Toast.LENGTH_SHORT
                    ).show()*/
                }
            },
            { error ->
               /* // Set the flag to false as fetchApi() encountered an error
                isFetchingApi = false
                // Handle the error here
                error.printStackTrace()
                // Log the error message
                Log.e(
                    "NetworkError",
                    "Error fetching payment information: ${error.message}",
                    error
                )
                // Show an error toast
                Toast.makeText(
                    requireContext(),
                    "Error fetching payment information: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()*/
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["customerName"] = storedCustomerName ?: "DefaultName"
                if (storedPrice2 != null) {
                    params["amount"] = (storedPrice2.toDouble() * 100).toInt().toString()
                }
                return params
            }
        }

        queue.add(request)
    }

    private fun toyyibpay() {
        // Construct the ToyyibPay URL with the necessary parameters, including the dynamic values
        val toyyibPayUrl = "https://toyyibpay.com/Pre-Booking"

        // Open the ToyyibPay URL in the default web browser
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(toyyibPayUrl))
        requireActivity().startActivity(intent)
    }

 /*   private fun createPaymentLink() {
        val sharedPreferences3 = requireContext().getSharedPreferences("MySharedPreferences3", Context.MODE_PRIVATE)
        val storedPrice2 = sharedPreferences3.getString("price", "")

        // Construct the request to your backend to create a payment link
        val createPaymentLinkUrl = "https://dashboard.stripe.com/payment-links/create"

        val stringRequest = object : StringRequest(
            Method.POST, createPaymentLinkUrl,
            { response ->
                // Handle the response from your server, which should contain the created payment link
                openPaymentLink(response)
            },
            { error ->
                // Handle the error here
                error.printStackTrace()
            }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["name"] = "Booking Price"
                params["price"] = storedPrice2 ?: "0" // Replace "0" with a default value if needed
                return params
            }
        }

        val queue = Volley.newRequestQueue(requireContext())
        queue.add(stringRequest)
    }

    private fun openPaymentLink(paymentLink: String) {
        // Open the created payment link in a web browser
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(paymentLink))
        requireActivity().startActivity(intent)
    }*/

    private fun saveData() {
       try {
           val currentUser = FirebaseAuth.getInstance().currentUser

           if (currentUser != null) {
               val userUID = currentUser.uid
               val senderId = "QYQ559BbH3TcfpAtwUzNoYSfAL72"

               val database =
                   Firebase.database("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
               val reference = database.getReference("Pre-Booking")
               val key = reference.push().key

               val distance = binding.tvDistance2.text
               val price = binding.tvPrice6.text?.toString() // Use toString() and add null safety with ?
               val priceWithoutEqual = price?.replace("= ", "") ?: "" // Check for null and replace the equal sign or use an empty string if null
               val pickUpLocation = binding.tvPickUpLocation2.text
               val dropOffLocation = binding.tvDropOffLocation2.text
               val pickUpDate =  binding.tvPickUpDate2.text
               val pickUpTime =  binding.tvPickUpTime2.text
               val customerName = binding.tvCustomerName2.text
               val phoneNumber = binding.tvCustomerPhoneNum.text
               val typeOfVehicle = binding.tvTypeOfVehicle.text
               val pax = binding.tvNumOfPax.text
               val luggage = binding.tvNumOfLuggages.text

               val hashMap = HashMap<String, Any>()
               hashMap["uid"] = userUID
               hashMap["id"] = "$key"
               hashMap["distance"] = distance
               hashMap["price"] = priceWithoutEqual
               hashMap["customerName"] = customerName
               hashMap["phoneNumber"] = phoneNumber
               hashMap["pickUpLocation"] = pickUpLocation
               hashMap["dropOffLocation"] = dropOffLocation
               hashMap["pickUpDate"] = pickUpDate
               hashMap["pickUpTime"] = pickUpTime
               hashMap["typeOfVehicle"] = typeOfVehicle
               hashMap["pax"] = pax
               hashMap["luggage"] = luggage

               // Update the user's booking using their UID and the generatedKey
               val userBookingsRef = reference.child(userUID)
               userBookingsRef.child("$key")
                   .setValue(hashMap).addOnCompleteListener { task ->
                       if (task.isSuccessful) {
                           // Create the booking details string
                           val bookingDetailsString = "Your Pre-Booking Details\n\n\n" +
                                   "Booking ID: \n$key\n\n" +
                                   "Customer Name: $customerName\n\n" +
                                   "Phone Number: $phoneNumber\n\n" +
                                   "Pick Up Location: \n$pickUpLocation\n\n" +
                                   "Drop Off Location: \n$dropOffLocation\n\n" +
                                   "Pick Up Date: $pickUpDate\n\n" +
                                   "Pick Up Time: $pickUpTime\n\n" +
                                   "Distance: $distance\n\n" +
                                   "Type Of Vehicle: $typeOfVehicle\n\n" +
                                   "Total Passengers: $pax\n\n" +
                                   "Total Luggages: $luggage\n\n" +
                                   "Total Price: $priceWithoutEqual\n"

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
   }


    private fun showCustomDialogBox() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        val dialogView = requireActivity().layoutInflater.inflate(R.layout.pre_order_successful, null)

        // Get a reference to the "ok" button with ID btn_ok
        val okButton = dialogView.findViewById<Button>(R.id.btn_ok)

        okButton.setOnClickListener {
            dialog.dismiss()
            findNavController().navigate(R.id.action_nav_payment_to_nav_home)
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

        unreadCountsRef.child(uniqueKey).addListenerForSingleValueEvent(object :
            ValueEventListener {
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
}