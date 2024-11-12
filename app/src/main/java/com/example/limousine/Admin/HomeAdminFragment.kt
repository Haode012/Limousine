package com.example.limousine.Admin

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AttrRes
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.limousine.Adapter.UserDetailsAdapter
import com.example.limousine.Adapter.UserDetailsDoneAdapter
import com.example.limousine.Model.UserDetailsModel
import com.example.limousine.databinding.FragmentHomeAdminBinding
import com.example.limousine.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class HomeAdminFragment : Fragment(), NavController.OnDestinationChangedListener {

    private lateinit var dbref : DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var userDetailsModelArrayList : ArrayList<UserDetailsModel>
    private lateinit var userDetailsDoneModelArrayList : ArrayList<UserDetailsModel>
    private lateinit var userDetailsAdapter: UserDetailsAdapter
    private lateinit var userDetailsDoneAdapter: UserDetailsDoneAdapter

    private var _binding: FragmentHomeAdminBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeAdminBinding.inflate(inflater, container, false)

        val root: View = binding.root

        return root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Access the color from the application theme
        val customColor = resources.getColor(R.color.admin, requireContext().theme)


        // Set the status bar color
        activity?.window?.statusBarColor = customColor

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
                    userDetailsAdapter.filter.filter(s)
                    binding.cardViewDropdown.visibility = View.INVISIBLE

                    // Check if the filter is empty and adjust the visibility of tvContacts
                    if (s.isNullOrBlank()) {
                        binding.tvNoBooking3.visibility = View.INVISIBLE
                    } else {
                        binding.tvNoBooking3.visibility = View.VISIBLE
                    }
                }
                catch (e: Exception){

                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        binding.igbtnFilterAdmin.setOnClickListener {
            if (binding.cardViewDropdown.visibility == View.VISIBLE) {
                binding.cardViewDropdown.visibility = View.INVISIBLE
            } else {
                binding.cardViewDropdown.visibility = View.VISIBLE
            }
        }

        if (binding.tvNoBooking3.visibility == View.VISIBLE){
            binding.igbtnFilterAdmin.isEnabled = false
        }

        recyclerView = view.findViewById(R.id.user_details_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)
        userDetailsModelArrayList = arrayListOf<UserDetailsModel>()

        getData()

        binding.btnRequesting.setOnClickListener {
            val navController = findNavController()
            navController.addOnDestinationChangedListener(this)

            binding.etSearch.visibility = View.VISIBLE
            binding.etSearch2.visibility = View.INVISIBLE

            binding.userDetailsRecyclerView.visibility = View.VISIBLE
            binding.userDetailsDoneRecyclerView.visibility = View.INVISIBLE

            binding.etSearch.text.clear()
            binding.etSearch2.text.clear()

            // Change the background color
            binding.btnRequesting.setBackgroundColor(Color.parseColor("#80A9A9A9"))

            binding.btnDone.setBackgroundColor(Color.parseColor("#FFAA1D"))

            //search
            binding.etSearch.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    //called as and when user type anything
                    try{
                        userDetailsAdapter.filter.filter(s)
                        binding.cardViewDropdown.visibility = View.INVISIBLE

                        // Check if the filter is empty and adjust the visibility of tvContacts
                        if (s.isNullOrBlank()) {
                            binding.tvNoBooking3.visibility = View.INVISIBLE
                        } else {
                            binding.tvNoBooking3.visibility = View.VISIBLE
                        }
                    }
                    catch (e: Exception){

                    }
                }

                override fun afterTextChanged(p0: Editable?) {
                }
            })

            recyclerView = view.findViewById(R.id.user_details_recycler_view)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.setHasFixedSize(true)
            userDetailsModelArrayList = arrayListOf<UserDetailsModel>()

            getData()

            binding.cardViewDropdown.visibility = View.INVISIBLE
        }


        try {
            binding.btnAlmost.setOnClickListener {
                binding.cardViewDropdown.visibility = View.INVISIBLE
                if (binding.etSearch.visibility == View.VISIBLE) {
                    userDetailsAdapter.sortByPickUpDateTime()
                } else {
                    userDetailsDoneAdapter.sortByPickUpDateTime()
                }
            }
        } catch(e: Exception){

        }

        try {
        binding.btnUpcoming.setOnClickListener {
            binding.cardViewDropdown.visibility = View.INVISIBLE
            if (binding.etSearch.visibility == View.VISIBLE){
                userDetailsAdapter.sortDescendingByPickUpDateTime()
            }else {
                userDetailsDoneAdapter.sortDescendingByPickUpDateTime()
            }
          }
        } catch(e: Exception){

        }

        binding.btnDone.setOnClickListener {
            val navController = findNavController()
            navController.addOnDestinationChangedListener(this)

            binding.etSearch.visibility = View.INVISIBLE
            binding.etSearch2.visibility = View.VISIBLE

            binding.userDetailsRecyclerView.visibility = View.INVISIBLE
            binding.userDetailsDoneRecyclerView.visibility = View.VISIBLE

            binding.etSearch.text.clear()
            binding.etSearch2.text.clear()

            // Change the background color
            binding.btnDone.setBackgroundColor(Color.parseColor("#80A9A9A9"))

            binding.btnRequesting.setBackgroundColor(Color.parseColor("#FFAA1D"))

            // search
            binding.etSearch2.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    //called as and when user type anything
                    try{
                        userDetailsDoneAdapter.filter.filter(s)
                        binding.cardViewDropdown.visibility = View.INVISIBLE

                        // Check if the filter is empty and adjust the visibility of tvContacts
                        if (s.isNullOrBlank()) {
                            binding.tvNoBooking3.visibility = View.INVISIBLE
                        } else {
                            binding.tvNoBooking3.visibility = View.VISIBLE
                        }
                    }
                    catch (e: Exception){

                    }
                }

                override fun afterTextChanged(p0: Editable?) {
                }
            })

            recyclerView = view.findViewById(R.id.user_details_done_recycler_view)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.setHasFixedSize(true)
            userDetailsDoneModelArrayList = arrayListOf<UserDetailsModel>()

            getData2()

            binding.cardViewDropdown.visibility = View.INVISIBLE
        }
    }

    private fun getData() {
            dbref =
                FirebaseDatabase.getInstance("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference("Pre-Booking")

            dbref.addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        if (snapshot.exists()) {
                            for (firstChildSnapshot in snapshot.children) {
                                for (secondChildSnapshot in firstChildSnapshot.children) {
                                    val userDetails =
                                        secondChildSnapshot.getValue(UserDetailsModel::class.java)
                                    userDetailsModelArrayList.add(userDetails!!)
                                }
                            }
                            val context = context
                            if (context != null) {
                                userDetailsAdapter = UserDetailsAdapter(
                                    userDetailsModelArrayList,
                                    context
                                )

                                // Sort the data by pick-up date and time
                                userDetailsAdapter.sortByPickUpDateTime()

                                recyclerView.adapter =  userDetailsAdapter
                                // Check if the adapter is empty, and set tvNoBooking visibility accordingly
                                if (userDetailsModelArrayList.isEmpty()) {
                                    binding.tvNoBooking3.visibility = View.VISIBLE
                                } else {
                                    binding.tvNoBooking3.visibility = View.GONE
                                }
                            }
                        } else {
                            // Data doesn't exist, set tvNoBooking visibility to visible
                            binding.tvNoBooking3.visibility = View.VISIBLE
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
        dbref =
            FirebaseDatabase.getInstance("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Pre-Booking (Done)")

        dbref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (snapshot.exists()) {
                        for (firstChildSnapshot in snapshot.children) {
                            for (secondChildSnapshot in firstChildSnapshot.children) {
                                val userDetails =
                                    secondChildSnapshot.getValue(UserDetailsModel::class.java)
                                userDetailsDoneModelArrayList.add(userDetails!!)
                            }
                        }
                        val context = context
                        if (context != null) {
                            userDetailsDoneAdapter = UserDetailsDoneAdapter(
                                userDetailsDoneModelArrayList,
                                context
                            )

                            // Sort the data by pick-up date and time
                            userDetailsDoneAdapter.sortByPickUpDateTime()

                            recyclerView.adapter =  userDetailsDoneAdapter
                            // Check if the adapter is empty, and set tvNoBooking visibility accordingly
                            if (userDetailsDoneModelArrayList.isEmpty()) {
                                binding.tvNoBooking3.visibility = View.VISIBLE
                            } else {
                                binding.tvNoBooking3.visibility = View.GONE
                            }
                        }
                    } else {
                        // Data doesn't exist, set tvNoBooking visibility to visible
                        binding.tvNoBooking3.visibility = View.VISIBLE
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
        if (destination.id == R.id.nav_preBookingDetails) {
           binding.etSearch.text.clear()
        } else if (destination.id == R.id.nav_contacts_admin){
            binding.etSearch.text.clear()
        }
    }
}