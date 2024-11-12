package com.example.limousine.Adapter

import BookingFilter
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
import com.example.limousine.Model.BookingModel
import com.example.limousine.Model.UserDetailsModel
import com.example.limousine.R
import com.example.limousine.databinding.BookingItemBinding
import com.example.limousine.databinding.FragmentHomeAdminBinding
import com.example.limousine.databinding.FragmentHomeBinding
import com.example.limousine.databinding.UserBookingDetailsItemBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import javax.sql.DataSource

class BookingAdapter: RecyclerView.Adapter<BookingAdapter.MyViewHolder> , Filterable {

    public var bookingModelArrayList: ArrayList<BookingModel>
    private val context: Context
    private lateinit var binding: BookingItemBinding
    private var filterList: ArrayList<BookingModel>

    private var filter: BookingFilter? = null

    constructor(bookingModelArrayList: ArrayList<BookingModel>, context: Context){
        this.bookingModelArrayList = bookingModelArrayList
        this.context = context
        this.filterList = bookingModelArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingAdapter.MyViewHolder {

        binding = BookingItemBinding.inflate(LayoutInflater.from(context), parent, false)

        return MyViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: BookingAdapter.MyViewHolder, position: Int) {
        val bookingModel = bookingModelArrayList[position]
        val bookingUid = bookingModel.uid
        val currentPosition = position // Store the position in a local variable

        // Fetch user details from Firebase based on the booking's uid
        val userRef = Firebase.database("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("Users").child(bookingUid)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(userSnapshot: DataSnapshot) {
                if (userSnapshot.exists()) {
                    // Populate other details (id, pickUpLocation, etc.) from userDetailsModel
                    holder.id.text = bookingModel.id
                    holder.pickUpLocation.text = bookingModel.pickUpLocation
                    holder.dropOffLocation.text = bookingModel.dropOffLocation
                    holder.pickUpDate.text = bookingModel.pickUpDate
                    holder.pickUpDateView.text = "Pick Up Date: ${bookingModel.pickUpDate}"
                    holder.pickUpTimeMain.text = bookingModel.pickUpTime
                    holder.pickUpTime.text = bookingModel.pickUpTime
                    holder.distance.text = bookingModel.distance
                    holder.typeOfVehicle.text = bookingModel.typeOfVehicle
                    holder.pax.text = bookingModel.pax
                    holder.luggage.text = bookingModel.luggage
                    holder.price.text = bookingModel.price

                    // Display pickUpDateView only for the first item when multiple items have the same pickUpDate
                    if (currentPosition > 0 && bookingModelArrayList[currentPosition - 1].pickUpDate == bookingModel.pickUpDate) {
                        holder.pickUpDateView.visibility = View.GONE
                    } else {
                        holder.pickUpDateView.visibility = View.VISIBLE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        holder.itemView.setOnClickListener {
            bookingRequestingFragment(bookingModel, holder)
        }
    }

   private fun bookingRequestingFragment(
        bookingModel: BookingModel,
        holder: BookingAdapter.MyViewHolder
    ) {
        val id = bookingModel.id
        val pickUpLocation = bookingModel.pickUpLocation
        val dropOffLocation = bookingModel.dropOffLocation
        val pickUpDate = bookingModel.pickUpDate
        val pickUpTime = bookingModel.pickUpTime
        val distance = bookingModel.distance
        val typeOfVehicle = bookingModel.typeOfVehicle
        val pax = bookingModel.pax
        val luggage = bookingModel.luggage
        val price = bookingModel.price
        val bookingUid = bookingModel.uid

        // Fetch user details from Firebase based on the booking's uid
        val userRef = Firebase.database("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("Users").child(bookingUid)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(userSnapshot: DataSnapshot) {
                if (userSnapshot.exists()) {

                    // Navigate after loading the image
                    Navigation.findNavController(holder.itemView)
                        .navigate(R.id.action_nav_booking_to_nav_booking_requesting, Bundle().apply {
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
                        })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    override fun getItemCount(): Int {

        return bookingModelArrayList.size
    }

    inner class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val id : TextView = binding.tvBookingID
        val pickUpLocation : TextView = binding.tvPickUpLocation
        val dropOffLocation : TextView = binding.tvDropOffLocation
        val pickUpDate : TextView = binding.tvPickUpDate
        val pickUpTimeMain : TextView = binding.tvPickUpTimeMain
        val pickUpTime : TextView = binding.tvPickUpTime
        val distance : TextView = binding.tvDistance
        val typeOfVehicle : TextView = binding.tvTypeOfVehicle2
        val pax : TextView = binding.tvPax
        val luggage: TextView = binding.tvLuggages
        val price : TextView = binding.tvPrice
        val pickUpDateView : TextView = binding.tvPickUpDate3
        var itemView : CardView = binding.bookingItemCardView
    }

    override fun getFilter(): Filter {
        if(filter == null){
            filter = BookingFilter(filterList, this)
        }
        return filter as BookingFilter
    }

    fun sortByPickUpDateTime() {
        bookingModelArrayList.sortWith(compareBy<BookingModel> {
            convertDateToSortableFormat(it.pickUpDate)
        }.thenBy { it.pickUpTime })
        notifyDataSetChanged()
    }

    fun sortDescendingByPickUpDateTime() {
        bookingModelArrayList.sortWith(compareByDescending<BookingModel> {
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