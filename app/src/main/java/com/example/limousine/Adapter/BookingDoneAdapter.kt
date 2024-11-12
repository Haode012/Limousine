package com.example.limousine.Adapter

import BookingDoneFilter
import BookingFilter
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.limousine.Model.BookingModel
import com.example.limousine.databinding.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class BookingDoneAdapter: RecyclerView.Adapter<BookingDoneAdapter.MyViewHolder> , Filterable {

    public var bookingDoneModelArrayList: ArrayList<BookingModel>
    private val context: Context
    private lateinit var binding: BookingDoneItemBinding
    private var filterList: ArrayList<BookingModel>

    private var filter: BookingDoneFilter? = null

    constructor(bookingDoneModelArrayList: ArrayList<BookingModel>, context: Context){
        this.bookingDoneModelArrayList = bookingDoneModelArrayList
        this.context = context
        this.filterList = bookingDoneModelArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingDoneAdapter.MyViewHolder {

        binding = BookingDoneItemBinding.inflate(LayoutInflater.from(context), parent, false)

        return MyViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: BookingDoneAdapter.MyViewHolder, position: Int) {
        val bookingModel = bookingDoneModelArrayList[position]
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
                    if (currentPosition > 0 && bookingDoneModelArrayList[currentPosition - 1].pickUpDate == bookingModel.pickUpDate) {
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

//        holder.itemView.setOnClickListener {
//            preBookingFragment(userDetailsModel, holder)
//        }
    }

   /* private fun preBookingFragment(
        userDetailsModel: UserDetailsModel,
        holder: BookingAdapter.MyViewHolder
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
                        .navigate(R.id.action_nav_home_admin_to_nav_preBookingDetails, Bundle().apply {
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
    }*/

    override fun getItemCount(): Int {

        return bookingDoneModelArrayList.size
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
        var itemView : CardView = binding.bookingDoneItemCardView
    }

    override fun getFilter(): Filter {
        if(filter == null){
            filter = BookingDoneFilter(filterList, this)
        }
        return filter as BookingDoneFilter
    }

    fun sortByPickUpDateTime() {
        bookingDoneModelArrayList.sortWith(compareBy<BookingModel> {
            convertDateToSortableFormat(it.pickUpDate)
        }.thenBy { it.pickUpTime })
        notifyDataSetChanged()
    }

    fun sortDescendingByPickUpDateTime() {
        bookingDoneModelArrayList.sortWith(compareByDescending<BookingModel> {
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