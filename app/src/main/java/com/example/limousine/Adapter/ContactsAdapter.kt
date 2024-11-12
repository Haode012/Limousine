package com.example.limousine.Adapter

import ContactsFilter
import ContactsFilterAdmin
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.example.limousine.Model.ChatModel
import com.example.limousine.Model.ContactsModel
import com.example.limousine.R
import com.example.limousine.databinding.ChatItemBinding
import com.example.limousine.databinding.ChatItemUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ContactsAdapter: RecyclerView.Adapter<ContactsAdapter.MyViewHolder> , Filterable {

    public var contactsModelArrayList: ArrayList<ContactsModel>
    private val context: Context
    private lateinit var binding: ChatItemUserBinding
    private var filterList: ArrayList<ContactsModel>

    private var filter: ContactsFilter? = null

    constructor(contactsModelArrayList: ArrayList<ContactsModel>, context: Context){
        this.contactsModelArrayList = contactsModelArrayList
        this.context = context
        this.filterList = contactsModelArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsAdapter.MyViewHolder {

        binding = ChatItemUserBinding.inflate(LayoutInflater.from(context), parent, false)

        return MyViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ContactsAdapter.MyViewHolder, position: Int) {
        val contactsModel = contactsModelArrayList[position]
        val uid = contactsModel.uid
        val currentUser = FirebaseAuth.getInstance().currentUser
        val receiverUid = currentUser?.uid

        // Fetch contacts from Firebase based on the uid
        val userRef = Firebase.database("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("Users").child(uid)

        val uniqueKey = "$uid-$receiverUid"

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(userSnapshot: DataSnapshot) {
                if (userSnapshot.exists()) {
                    val fullName = userSnapshot.child("fullName").value.toString()
                    val profileImageUrl =
                        userSnapshot.child("profilePicture").child("url").value.toString()

                    // Update the fullName property of userDetailsModel
                    contactsModel.fullName = fullName

                    // Set the fullName and profileImage into your MyViewHolder
                    holder.name.text = contactsModel.fullName

                    // Call getLastMessage to retrieve and set the last message
                    if (receiverUid != null) {
                        getLastMessage(uid, receiverUid, holder, contactsModel)
                    }

                    getStatusMessage(uid, holder)

                    getUnreadCount(uniqueKey, holder)

                    // Load the profile image with RequestOptions
                    val requestOptions = RequestOptions()
                        .placeholder(R.drawable.ic_baseline_account_circle_yellow_24) // Placeholder image

                    Glide.with(context)
                        .load(profileImageUrl)
                        .apply(requestOptions) // Apply the defined RequestOptions
                        .into(holder.profilePicture)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        holder.itemView.setOnClickListener {
            // Navigate to the chat room fragment
            chatRoomFragment(contactsModel, holder)
        }
    }

    private fun getStatusMessage(uid: String, holder: ContactsAdapter.MyViewHolder) {
        // Check if the user is online
        val onlineUsersRef = Firebase.database("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("OnlineUsers").child(uid)

        val status = holder.status

        onlineUsersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isOnline = snapshot.getValue(Boolean::class.java) ?: false

                if (isOnline) {
                    status.text = "Online"
                } else {
                    status.apply {
                        text = "Offline"
                        setTextColor(ContextCompat.getColor(context, R.color.grey))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                status.apply {
                    text = "Offline"
                    setTextColor(ContextCompat.getColor(context, R.color.grey))
                }
            }
        })
    }

    private fun getUnreadCount(uniqueKey: String, holder: MyViewHolder) {
        // Access the corresponding "UnreadCount" node based on the uid
        val unreadCountsRef = Firebase.database("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("UnreadCount").child(uniqueKey)

        unreadCountsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Retrieve the unread count as an integer
                    val unreadCount = snapshot.getValue(Int::class.java)

                    if (unreadCount != null) {
                        // Update the unread message count text
                        holder.unreadCount.text = unreadCount.toString()

                        // Update the visibility based on the unreadCount value
                        holder.unreadCount.visibility = View.VISIBLE
                        holder.unreadCountBackground.visibility = View.VISIBLE
                    } else {
                        // If unreadCount is null, hide the views
                        holder.unreadCount.visibility = View.INVISIBLE
                        holder.unreadCountBackground.visibility = View.INVISIBLE
                    }
                } else {
                    // If the snapshot doesn't exist, hide the views
                    holder.unreadCount.visibility = View.INVISIBLE
                    holder.unreadCountBackground.visibility = View.INVISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error for "UnreadCount" retrieval
                Log.e("UnreadCountError", "Error reading unread count: ${error.message}")
            }
        })
    }

    private fun getLastMessage(currentUserUid: String, receiverUid: String, holder: ContactsAdapter.MyViewHolder, contact: ContactsModel) {
        val ref = FirebaseDatabase.getInstance("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("Chat")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val lastMessage = extractLastMessageForSenderAndReceiver(dataSnapshot, currentUserUid, receiverUid)

                // Update the lastMessage property of the ContactsModel
                val messageText = lastMessage?.let {
                    if (it.image.isNotEmpty()) {
                        "Image"
                    } else if (it.recording.isNotEmpty()) {
                        "Audio"
                    } else {
                        it.message
                    }
                } ?: "No messages"

                contact.lastMessage = messageText

                // Update the UI with the last message
                holder.lastMessage.text = messageText
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle any errors that occur during the read operation
                println("Error: ${databaseError.toException()}")
            }
        })
    }

    private fun extractLastMessageForSenderAndReceiver(snapshot: DataSnapshot, currentUserUid: String, receiverUid: String): ChatModel? {
        val messages = mutableListOf<ChatModel>()
        for (dataSnapshot in snapshot.children) {
            val chat = dataSnapshot.getValue(ChatModel::class.java)
            chat?.let {
                if ((it.senderId == currentUserUid && it.receiverId == receiverUid) ||
                    (it.senderId == receiverUid && it.receiverId == currentUserUid)) {
                    messages.add(it)
                }
            }
        }

        // Sort messages by some criteria to get the most recent, since there's no timestamp
        val sortedMessages = messages.sortedByDescending { it.currentDate + it.currentTime }

        return sortedMessages.firstOrNull()
    }

    private fun chatRoomFragment(contactsModel: ContactsModel, holder: ContactsAdapter.MyViewHolder) {
        val name = contactsModel.fullName
        val uid = contactsModel.uid
        val status = holder.status.text
        val profileImageUrl = contactsModel.profilePicture

        // Create RequestOptions for loading images with Glide
        val requestOptions = RequestOptions()
            .placeholder(R.drawable.ic_baseline_account_circle_yellow_24) // Placeholder image
            .error(R.drawable.ic_baseline_account_circle_yellow_24) // Error image

        val bundle = Bundle().apply {
            putString("profilePicture", profileImageUrl)
            putString("name", name)
            putString("uid", uid)
            putString("status", status as String?)
        }

        // Load the profile picture with RequestOptions
        Glide.with(holder.itemView.context)
            .load(profileImageUrl)
            .apply(requestOptions) // Apply the defined RequestOptions
            .into(holder.profilePicture)

        // Access the corresponding "UnreadCount" node based on the uid
        val unreadCountsRef = Firebase.database("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("UnreadCount").child(uid)

       // Remove the "UnreadCount" node
        unreadCountsRef.removeValue()
            .addOnSuccessListener {
                // Successfully removed the node, you can perform any additional actions here if needed.
            }
            .addOnFailureListener { error ->
                // Handle the failure to remove the node
                Log.e("UnreadCountError", "Error removing unread count: ${error.message}")
            }

        // Navigate to the chat room fragment
        holder.itemView.findNavController().navigate(R.id.action_nav_contacts_to_nav_chat_room, bundle)
    }

    override fun getItemCount(): Int {
        return contactsModelArrayList.size
    }

    inner class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val profilePicture : ImageView = binding.imgPicture
        val name : TextView = binding.tvName
        val status: TextView = binding.tvStatus
        val lastMessage: TextView = binding.tvLastMessage2
        val unreadCount: TextView = binding.tvUnreadMessage
        val unreadCountBackground: ImageView = binding.igUnreadMessageBackground
        var itemView : CardView = binding.chatItemCardView
    }

    override fun getFilter(): Filter {
        if(filter == null){
            filter = ContactsFilter(filterList, this)
        }
        return filter as ContactsFilter
    }
}