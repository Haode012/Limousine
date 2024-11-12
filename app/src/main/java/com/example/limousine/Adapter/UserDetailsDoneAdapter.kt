package com.example.limousine.Adapter

import UserDetailsDoneFilter
import UserDetailsFilter
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.example.limousine.Model.UserDetailsModel
import com.example.limousine.R
import com.example.limousine.databinding.FragmentHomeAdminBinding
import com.example.limousine.databinding.FragmentHomeBinding
import com.example.limousine.databinding.UserBookingDetailsItemBinding
import com.example.limousine.databinding.UserBookingDoneDetailsItemBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import javax.sql.DataSource

class UserDetailsDoneAdapter: RecyclerView.Adapter<UserDetailsDoneAdapter.MyViewHolder> , Filterable {

    public var userDetailsDoneModelArrayList: ArrayList<UserDetailsModel>
    private val context: Context
    private lateinit var binding: UserBookingDoneDetailsItemBinding
    private var filterList: ArrayList<UserDetailsModel>
    private val uniquePickUpDates = ArrayList<String>()

    private var filter: UserDetailsDoneFilter? = null

    constructor(userDetailsDoneModelArrayList: ArrayList<UserDetailsModel>, context: Context){

        this.context = context

        userDetailsDoneModelArrayList.sortWith(compareBy({ it.pickUpDate }, { it.pickUpTime }))

        // Populate the uniquePickUpDates list
        userDetailsDoneModelArrayList.forEach { userDetailsModel ->
            if (!uniquePickUpDates.contains(userDetailsModel.pickUpDate)) {
                uniquePickUpDates.add(userDetailsModel.pickUpDate)
            }
        }

        this.userDetailsDoneModelArrayList = userDetailsDoneModelArrayList
        this.filterList = userDetailsDoneModelArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserDetailsDoneAdapter.MyViewHolder {

        binding = UserBookingDoneDetailsItemBinding.inflate(LayoutInflater.from(context), parent, false)

        return MyViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: UserDetailsDoneAdapter.MyViewHolder, position: Int) {
        val userDetailsModel = userDetailsDoneModelArrayList[position]
        val bookingUid = userDetailsModel.uid
        val currentPosition = position // Store the position in a local variable

        // Fetch user details from Firebase based on the booking's uid
        val userRef = Firebase.database("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("Users").child(bookingUid)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(userSnapshot: DataSnapshot) {
                if (userSnapshot.exists()) {
                    val fullName = userSnapshot.child("fullName").value.toString()
                    val profileImageUrl = userSnapshot.child("profilePicture").child("url").value.toString()

                    // Update the fullName property of userDetailsModel
                    userDetailsModel.fullName = fullName

                    // Populate other details (id, pickUpLocation, etc.) from userDetailsModel
                    holder.id.text = userDetailsModel.id
                    holder.pickUpLocation.text = userDetailsModel.pickUpLocation
                    holder.dropOffLocation.text = userDetailsModel.dropOffLocation
                    holder.pickUpDate.text = userDetailsModel.pickUpDate
                    holder.pickUpDateView.text = "Pick Up Date: ${userDetailsModel.pickUpDate}"
                    holder.pickUpTime.text = userDetailsModel.pickUpTime
                    holder.distance.text = userDetailsModel.distance
                    holder.phoneNumber.text = userDetailsModel.phoneNumber
                    holder.typeOfVehicle.text = userDetailsModel.typeOfVehicle
                    holder.pax.text = userDetailsModel.pax
                    holder.luggage.text = userDetailsModel.luggage
                    holder.price.text = userDetailsModel.price

                    // Set the fullName and profileImage into your MyViewHolder
                    holder.name.text = userDetailsModel.fullName

                    if (profileImageUrl != null) {
                        Glide.with(context)
                            .load(profileImageUrl)
                            .listener(object : RequestListener<Drawable> {
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: com.bumptech.glide.request.target.Target<Drawable>?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    // If image loading fails, set a placeholder image
                                    holder.profilePicture.setImageResource(R.drawable.ic_baseline_account_circle_blue_24)
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
                            .into(holder.profilePicture)
                    } else {
                        // Set a placeholder image if profileImageUrl is null
                        holder.profilePicture.setImageResource(R.drawable.ic_baseline_account_circle_blue_24)
                    }

                    // Display pickUpDateView only for the first item in the group
                    if (uniquePickUpDates.contains(userDetailsModel.pickUpDate) &&
                        currentPosition == userDetailsDoneModelArrayList.indexOfFirst { it.pickUpDate == userDetailsModel.pickUpDate }) {
                        holder.pickUpDateView.visibility = View.VISIBLE
                        holder.pickUpDateView.text = "Pick Up Date: ${userDetailsModel.pickUpDate}"
                    } else {
                        holder.pickUpDateView.visibility = View.GONE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        holder.itemView.setOnClickListener {
            preBookingFragment(userDetailsModel, holder)
        }
    }

    private fun preBookingFragment(
        userDetailsModel: UserDetailsModel,
        holder: UserDetailsDoneAdapter.MyViewHolder
    ) {
        val name = userDetailsModel.fullName
        val id = userDetailsModel.id
        val pickUpLocation = userDetailsModel.pickUpLocation
        val dropOffLocation = userDetailsModel.dropOffLocation
        val pickUpDate = userDetailsModel.pickUpDate
        val pickUpTime = userDetailsModel.pickUpTime
        val distance = userDetailsModel.distance
        val phoneNumber = userDetailsModel.phoneNumber
        val typeOfVehicle = userDetailsModel.typeOfVehicle
        val pax = userDetailsModel.pax
        val luggage = userDetailsModel.luggage
        val price = userDetailsModel.price
        val bookingUid = userDetailsModel.uid

        // Fetch user details from Firebase based on the booking's uid
        val userRef = Firebase.database("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("Users").child(bookingUid)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(userSnapshot: DataSnapshot) {
                if (userSnapshot.exists()) {
                    val profileImageUrl = userSnapshot.child("profilePicture").child("url").value.toString()

                    if (profileImageUrl != null) {
                        Glide.with(context)
                            .load(profileImageUrl)
                            .listener(object : RequestListener<Drawable> {
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: com.bumptech.glide.request.target.Target<Drawable>?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    // If image loading fails, set a placeholder image
                                    holder.profilePicture.setImageResource(R.drawable.ic_baseline_account_circle_blue_24)
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
                            .into(holder.profilePicture)
                    } else {
                        // Set a placeholder image if profileImageUrl is null
                        holder.profilePicture.setImageResource(R.drawable.ic_baseline_account_circle_blue_24)
                    }

                    // Navigate after loading the image
                    Navigation.findNavController(holder.itemView)
                        .navigate(R.id.action_nav_home_admin_to_nav_preBookingDetailsDone, Bundle().apply {
                            putString("profilePicture", profileImageUrl)
                            putString("name", name)
                            putString("phoneNumber", phoneNumber)
                            putString("id", id)
                            putString("pickUpLocation", pickUpLocation)
                            putString("dropOffLocation", dropOffLocation)
                            putString("pickUpDate", pickUpDate)
                            putString("pickUpTime", pickUpTime)
                            putString("distance", distance)
                            putString("typeOfVehicle", typeOfVehicle)
                            putString("pax", pax)
                            putString("luggage", luggage)
                            putString("price", price)
                            putString("uid", bookingUid)
                        })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    override fun getItemCount(): Int {

        return userDetailsDoneModelArrayList.size
    }

    inner class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val name : TextView = binding.tvName
        val id : TextView = binding.tvBookingID
        val pickUpLocation : TextView = binding.tvPickUpLocation
        val dropOffLocation : TextView = binding.tvDropOffLocation
        val pickUpDate : TextView = binding.tvPickUpDate
        val pickUpTime : TextView = binding.tvPickUpTime5
        val distance : TextView = binding.tvDistance
        val typeOfVehicle : TextView = binding.tvTypeOfVehicle2
        val pax : TextView = binding.tvPax
        val luggage: TextView = binding.tvLuggages
        val phoneNumber: TextView = binding.tvPhoneNumber
        val price : TextView = binding.tvPrice
        val profilePicture : ImageView = binding.imgPicture
        val pickUpDateView : TextView = binding.tvPickUpDate3
        var itemView : CardView = binding.userBookingDetailsDoneItemCardView
    }

    override fun getFilter(): Filter {
        if(filter == null){
            filter = UserDetailsDoneFilter(filterList, this)
        }
        return filter as UserDetailsDoneFilter
    }

    fun sortByPickUpDateTime() {
        userDetailsDoneModelArrayList.sortWith(compareBy<UserDetailsModel> {
            convertDateToSortableFormat(it.pickUpDate)
        }.thenBy { it.pickUpTime })
        notifyDataSetChanged()
    }

    fun sortDescendingByPickUpDateTime() {
        userDetailsDoneModelArrayList.sortWith(compareByDescending<UserDetailsModel> {
            convertDateToSortableFormat(it.pickUpDate)
        }.thenByDescending { it.pickUpTime })
        notifyDataSetChanged()
    }

    private fun convertDateToSortableFormat(date: String): String {
        val parts = date.split("/")
        if (parts.size == 3) {
            val day = parts[0].padStart(2, '0')
            val month = parts[1].padStart(2, '0')
            val year = parts[2]
            return "$year$month$day"
        }
        return date // Return the original date if it cannot be converted
    }
}