package com.example.limousine

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.limousine.Adapter.*
import com.example.limousine.Model.BookingModel
import com.example.limousine.Model.ContactsModel
import com.example.limousine.Model.UserDetailsModel
import com.example.limousine.databinding.FragmentBookingBinding
import com.example.limousine.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class BookingFragment : Fragment(), NavController.OnDestinationChangedListener{

    private lateinit var dbref : DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var bookingModelArrayList : ArrayList<BookingModel>
    private lateinit var bookingAdapter: BookingAdapter
    private lateinit var bookingDoneModelArrayList : ArrayList<BookingModel>
    private lateinit var bookingDoneAdapter: BookingDoneAdapter

    private var _binding: FragmentBookingBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentBookingBinding.inflate(inflater, container, false)

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()
        navController.addOnDestinationChangedListener(this)

        binding.btnRequesting.setBackgroundColor(Color.parseColor("#80A9A9A9"))

        //search
        binding.etSearch.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //called as and when user type anything
                try{
                    bookingAdapter.filter.filter(s)
                    binding.cardViewDropdown.visibility = View.INVISIBLE
                    // Check if the filter is empty and adjust the visibility of tvContacts
                    if (s.isNullOrBlank()) {
                        binding.tvNoBooking.visibility = View.INVISIBLE
                    } else {
                        binding.tvNoBooking.visibility = View.VISIBLE
                    }
                }
                catch (e: Exception){

                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        if (binding.tvNoBooking.visibility == View.VISIBLE){
            binding.igbtnFilter.isEnabled = false
        }

        binding.igbtnFilter.setOnClickListener {
            if (binding.cardViewDropdown.visibility == View.VISIBLE) {
                binding.cardViewDropdown.visibility = View.INVISIBLE
            } else {
                binding.cardViewDropdown.visibility = View.VISIBLE
            }
        }
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)
        bookingModelArrayList = arrayListOf<BookingModel>()

        getData()

        binding.btnRequesting.setOnClickListener {
            val navController = findNavController()
            navController.addOnDestinationChangedListener(this)

            binding.etSearch.visibility = View.VISIBLE
            binding.etSearch3.visibility = View.INVISIBLE

            binding.etSearch.text.clear()
            binding.etSearch3.text.clear()

            binding.recyclerView.visibility = View.VISIBLE
            binding.recyclerViewDone.visibility = View.INVISIBLE

            // Change the background color
            binding.btnRequesting.setBackgroundColor(Color.parseColor("#80A9A9A9"))

            binding.btnDone.setBackgroundColor(Color.parseColor("#009dff"))

            //search
            binding.etSearch.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    //called as and when user type anything
                    try{
                        bookingAdapter.filter.filter(s)
                        binding.cardViewDropdown.visibility = View.INVISIBLE
                        // Check if the filter is empty and adjust the visibility of tvContacts
                        if (s.isNullOrBlank()) {
                            binding.tvNoBooking.visibility = View.INVISIBLE
                        } else {
                            binding.tvNoBooking.visibility = View.VISIBLE
                        }
                    }
                    catch (e: Exception){

                    }
                }

                override fun afterTextChanged(p0: Editable?) {
                }
            })

            recyclerView = view.findViewById(R.id.recyclerView)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.setHasFixedSize(true)
            bookingModelArrayList = arrayListOf<BookingModel>()

            getData()

            binding.cardViewDropdown.visibility = View.INVISIBLE
        }

        try {
            binding.btnAlmost.setOnClickListener {
                binding.cardViewDropdown.visibility = View.INVISIBLE
                if (binding.etSearch.visibility == View.VISIBLE) {
                    bookingAdapter.sortByPickUpDateTime()
                } else {
                    bookingDoneAdapter.sortByPickUpDateTime()
                }
            }
        } catch(e: Exception){

        }

        try {
            binding.btnUpcoming.setOnClickListener {
                binding.cardViewDropdown.visibility = View.INVISIBLE
                if (binding.etSearch.visibility == View.VISIBLE){
                    bookingAdapter.sortDescendingByPickUpDateTime()
                }else {
                    bookingDoneAdapter.sortDescendingByPickUpDateTime()
                }
            }
        } catch(e: Exception){

        }

        binding.btnDone.setOnClickListener {
            val navController = findNavController()
            navController.addOnDestinationChangedListener(this)

            binding.etSearch.visibility = View.INVISIBLE
            binding.etSearch3.visibility = View.VISIBLE

            binding.etSearch.text.clear()
            binding.etSearch3.text.clear()

            binding.recyclerView.visibility = View.INVISIBLE
            binding.recyclerViewDone.visibility = View.VISIBLE

            // Change the background color
            binding.btnDone.setBackgroundColor(Color.parseColor("#80A9A9A9"))

            binding.btnRequesting.setBackgroundColor(Color.parseColor("#009dff"))

            // search
            binding.etSearch3.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    //called as and when user type anything
                    try{
                        bookingDoneAdapter.filter.filter(s)
                        binding.cardViewDropdown.visibility = View.INVISIBLE

                        // Check if the filter is empty and adjust the visibility of tvContacts
                        if (s.isNullOrBlank()) {
                            binding.tvNoBooking.visibility = View.INVISIBLE
                        } else {
                            binding.tvNoBooking.visibility = View.VISIBLE
                        }
                    }
                    catch (e: Exception){

                    }
                }

                override fun afterTextChanged(p0: Editable?) {
                }
            })

            recyclerView = view.findViewById(R.id.recyclerViewDone)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.setHasFixedSize(true)
            bookingDoneModelArrayList = arrayListOf<BookingModel>()

            getData2()

            binding.cardViewDropdown.visibility = View.INVISIBLE
        }
    }

    private fun getData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()

        dbref = FirebaseDatabase.getInstance("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("Pre-Booking").child(userId)

        dbref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (snapshot.exists()) {
                        for (firstChildSnapshot in snapshot.children) {
                            val booking = firstChildSnapshot.getValue(BookingModel::class.java)
                            bookingModelArrayList.add(booking!!)
                        }
                        val context = context
                        if (context != null) {
                            bookingAdapter = BookingAdapter(bookingModelArrayList, context)

                            // Sort the data by pick-up date and time
                            bookingAdapter.sortByPickUpDateTime()

                            recyclerView.adapter = bookingAdapter

                            // Check if the adapter is empty, and set tvNoBooking visibility accordingly
                            if (bookingModelArrayList.isEmpty()) {
                                binding.tvNoBooking.visibility = View.VISIBLE
                            } else {
                                binding.tvNoBooking.visibility = View.GONE
                            }
                        }
                    } else {
                        // Data doesn't exist, set tvNoBooking visibility to visible
                        binding.tvNoBooking.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    Log.e("getData", "Failed getting data from firebase: ${e.message}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun getData2(){
        val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()

        dbref =
            FirebaseDatabase.getInstance("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Pre-Booking (Done)").child(userId)

        dbref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (snapshot.exists()) {
                        for (firstChildSnapshot in snapshot.children) {
                            val booking =
                                firstChildSnapshot.getValue(BookingModel::class.java)
                            bookingDoneModelArrayList.add(booking!!)
                        }
                        val context = context
                        if (context != null) {
                            bookingDoneAdapter = BookingDoneAdapter(
                                bookingDoneModelArrayList,
                                context
                            )

                            // Sort the data by pick-up date and time
                            bookingDoneAdapter.sortByPickUpDateTime()

                            recyclerView.adapter =  bookingDoneAdapter

                            // Check if the adapter is empty, and set tvNoBooking visibility accordingly
                            if (bookingDoneModelArrayList.isEmpty()) {
                                binding.tvNoBooking.visibility = View.VISIBLE
                            } else {
                                binding.tvNoBooking.visibility = View.GONE
                            }
                        }
                    } else {
                        // Data doesn't exist, set tvNoBooking visibility to visible
                        binding.tvNoBooking.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    Log.e("getData", "Failed getting data from firebase: ${e.message}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val navController = findNavController()
        navController.removeOnDestinationChangedListener(this)
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        if (destination.id == R.id.nav_home) {
            binding.etSearch.text.clear()
        } else if (destination.id == R.id.nav_profile){
            binding.etSearch.text.clear()
        } else if (destination.id == R.id.nav_contacts){
            binding.etSearch.text.clear()
        }
    }
}