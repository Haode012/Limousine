package com.example.limousine

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
import com.example.limousine.Adapter.ContactsAdapter
import com.example.limousine.Adapter.ContactsAdapterAdmin
import com.example.limousine.Model.ChatModel
import com.example.limousine.Model.ContactsModel
import com.example.limousine.databinding.FragmentContactsBinding
import com.google.firebase.database.*

class ContactsFragment : Fragment(), NavController.OnDestinationChangedListener {
    private lateinit var dbref : DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var contactsModelArrayList : ArrayList<ContactsModel>
    private lateinit var contactsAdapter: ContactsAdapter

    private var _binding: FragmentContactsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentContactsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()
        navController.addOnDestinationChangedListener(this)

        //search
        binding.etSearch.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //called as and when user type anything
                try{
                    contactsAdapter.filter.filter(s)

                    // Check if the filter is empty and adjust the visibility of tvContacts
                    if (s.isNullOrBlank()) {
                        binding.tvContacts.visibility = View.INVISIBLE
                    } else {
                        binding.tvContacts.visibility = View.VISIBLE
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
        contactsModelArrayList = arrayListOf<ContactsModel>()

        getData()
    }

    private fun getData() {
        val dbref = FirebaseDatabase.getInstance("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("Users")

        dbref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                contactsModelArrayList.clear() // Clear the list before populating

                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        val userType = userSnapshot.child("userType").getValue(String::class.java)
                        if (userType == "Admin") {
                            val uid = userSnapshot.key
                            val fullName = userSnapshot.child("fullName").getValue(String::class.java)
                            val profilePicture = userSnapshot.child("profilePicture").child("url").getValue(String::class.java)

                            // Always create a ContactsModel instance for this user
                            val contacts = ContactsModel(profilePicture ?: "", uid ?: "", fullName ?: "", "", 0)
                            contactsModelArrayList.add(contacts)
                        }
                    }
                    // Initialize the adapter here once you have populated the ArrayList
                    val context = context
                    if (context != null) {
                        contactsAdapter = ContactsAdapter(contactsModelArrayList, context)
                        recyclerView.adapter = contactsAdapter
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("getData", "Failed getting data from Firebase: ${error.message}")
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
        } else if (destination.id == R.id.nav_booking){
            binding.etSearch.text.clear()
        }
    }
}