package com.example.limousine.Adapter

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.text.Selection
import android.text.method.TextKeyListener
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.example.limousine.Model.ChatModel
import com.example.limousine.Model.ContactsModel
import com.example.limousine.R
import com.example.limousine.databinding.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(
    private val chatModelArrayList: List<ChatModel>,
    private val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val MESSAGE_TYPE_LEFT = 0
    private val MESSAGE_TYPE_RIGHT = 1
    private val MESSAGE_TYPE_LEFT_IMAGE = 2
    private val MESSAGE_TYPE_RIGHT_IMAGE = 3
    private val MESSAGE_TYPE_LEFT_AUDIO = 4
    private val MESSAGE_TYPE_RIGHT_AUDIO = 5

    private val firebaseUser = FirebaseAuth.getInstance().currentUser
    private val mediaPlayer: MediaPlayer = MediaPlayer()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(context)

        return when (viewType) {
            MESSAGE_TYPE_RIGHT -> {
                val binding = ChatItemRightUserBinding.inflate(inflater, parent, false)
                MyViewHolderText(binding.root, binding, null, parent.context, chatModelArrayList)
            }
            MESSAGE_TYPE_LEFT -> {
                val binding2 = ChatItemLeftUserBinding.inflate(inflater, parent, false)
                MyViewHolderText(binding2.root, null, binding2, parent.context, chatModelArrayList)
            }
            MESSAGE_TYPE_RIGHT_IMAGE -> {
                val binding = ChatItemImageRightUserBinding.inflate(inflater, parent, false)
                MyViewHolderImage(binding.root, binding, null)
            }
            MESSAGE_TYPE_LEFT_IMAGE -> {
                val binding2 = ChatItemImageLeftUserBinding.inflate(inflater, parent, false)
                MyViewHolderImage(binding2.root, null, binding2)
            }
            MESSAGE_TYPE_LEFT_AUDIO -> {
                val binding2 = ChatItemAudioLeftUserBinding.inflate(inflater, parent, false)
                MyViewHolderAudio(binding2.root, null, binding2)
            }
            MESSAGE_TYPE_RIGHT_AUDIO -> {
                val binding2 = ChatItemAudioRightUserBinding.inflate(inflater, parent, false)
                MyViewHolderAudio(binding2.root, binding2, null)
          }
            else -> throw IllegalArgumentException("Invalid viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chatModel = chatModelArrayList[position]

        when (holder.itemViewType) {
            MESSAGE_TYPE_RIGHT -> {
                val textHolder = holder as MyViewHolderText
                textHolder.message.text = chatModel.message
                textHolder.currentDate.text = chatModel.currentDate
                textHolder.currentTime.text = chatModel.currentTime
            }
            MESSAGE_TYPE_LEFT -> {
                val textHolder = holder as MyViewHolderText
                textHolder.message.text = chatModel.message
                textHolder.currentDate.text = chatModel.currentDate
                textHolder.currentTime.text = chatModel.currentTime

                // Check if the message is from an earlier time and if it has unread
                if (isMessageFromEarlierTime(position) && chatModel.unread.isNotEmpty()) {
                    textHolder.unread.text = chatModel.unread
                    textHolder.unread.visibility = View.VISIBLE
                } else {
                    textHolder.unread.visibility = View.GONE
                    // Set the top margin of the FrameLayout to 0
                    val layoutParams = textHolder.frameLayout.layoutParams as ViewGroup.MarginLayoutParams
                    layoutParams.topMargin = 0
                    textHolder.frameLayout.layoutParams = layoutParams
                }
            }
            MESSAGE_TYPE_RIGHT_IMAGE -> {
                val imageHolder = holder as MyViewHolderImage
                if (chatModel.image.isNotEmpty()) {
                    Glide.with(context).load(chatModel.image).into(imageHolder.image)
                    imageHolder.currentDate.text = chatModel.currentDate
                    imageHolder.currentTime.text = chatModel.currentTime
                    imageHolder.image.setOnClickListener {
                        // Show the full-size image overlay
                        showFullSizeImage(chatModel.image)
                    }
                    imageHolder.image.setOnLongClickListener {
                        showDeleteConfirmationDialog(position)
                        true // Consume the long click event
                    }
                } else {
                    imageHolder.image.setImageDrawable(null)
                }
            }
            MESSAGE_TYPE_LEFT_IMAGE -> {
                val imageHolder = holder as MyViewHolderImage
                if (chatModel.image.isNotEmpty()) {
                    Glide.with(context).load(chatModel.image).into(imageHolder.image)
                    imageHolder.currentDate.text = chatModel.currentDate
                    imageHolder.currentTime.text = chatModel.currentTime

                    // Check if the message is from an earlier time and if it has unread
                    if (isMessageFromEarlierTime(position) && chatModel.unread.isNotEmpty()) {
                        imageHolder.unread.text = chatModel.unread
                        imageHolder.unread.visibility = View.VISIBLE
                    } else {
                        imageHolder.unread.visibility = View.GONE
                        // Set the top margin of the FrameLayout to 0
                        val layoutParams = imageHolder.frameLayout.layoutParams as ViewGroup.MarginLayoutParams
                        layoutParams.topMargin = 0
                        imageHolder.frameLayout.layoutParams = layoutParams
                    }

                    imageHolder.image.setOnClickListener {
                        // Show the full-size image overlay
                        showFullSizeImage(chatModel.image)
                    }
                } else {
                    imageHolder.image.setImageDrawable(null)
                }
            }
            MESSAGE_TYPE_LEFT_AUDIO -> {
                val audioHolder = holder as MyViewHolderAudio

                audioHolder.currentDate.text = chatModel.currentDate
                audioHolder.currentTime.text = chatModel.currentTime

                // Check if the message is from an earlier time and if it has unread
                if (isMessageFromEarlierTime(position) && chatModel.unread.isNotEmpty()) {
                    audioHolder.unread.text = chatModel.unread
                    audioHolder.unread.visibility = View.VISIBLE
                } else {
                    audioHolder.unread.visibility = View.GONE
                    // Set the top margin of the FrameLayout to 0
                    val layoutParams = audioHolder.frameLayout.layoutParams as ViewGroup.MarginLayoutParams
                    layoutParams.topMargin = 0
                    audioHolder.frameLayout.layoutParams = layoutParams
                }


                // Inside onBindViewHolder
                audioHolder.playPauseAudio.setOnClickListener {
                    if (mediaPlayer?.isPlaying == true) {
                        mediaPlayer?.reset()
                        // Hide pause icon, show play icon
                        audioHolder.playPauseAudio.setImageResource(R.drawable.ic_baseline_play_circle_24)
                    } else {
                        // Reset MediaPlayer
                        mediaPlayer?.reset()
                        try {
                            // Set the data source to the Firebase storage URL
                            val audioUrl = chatModel.recording // Firebase storage URL
                            mediaPlayer?.setDataSource(audioUrl)

                            // Use setOnPreparedListener to start playing when prepared
                            mediaPlayer?.setOnPreparedListener { mp ->
                                mp.start()
                                // Show pause icon
                                audioHolder.playPauseAudio.setImageResource(R.drawable.ic_baseline_pause_circle_24)
                            }

                            // Use setOnCompletionListener to detect when audio playback is completed
                            mediaPlayer?.setOnCompletionListener { mp ->
                                // Reset MediaPlayer when audio playback ends
                                mp.reset()
                                // Show play icon
                                audioHolder.playPauseAudio.setImageResource(R.drawable.ic_baseline_play_circle_24)
                            }

                            // Prepare the media player asynchronously
                            mediaPlayer?.prepareAsync()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            MESSAGE_TYPE_RIGHT_AUDIO -> {
                val audioHolder = holder as MyViewHolderAudio

                audioHolder.currentDate.text = chatModel.currentDate
                audioHolder.currentTime.text = chatModel.currentTime

                // Inside onBindViewHolder
                audioHolder.playPauseAudio.setOnClickListener {
                    if (mediaPlayer?.isPlaying == true) {
                        mediaPlayer?.reset()
                        // Hide pause icon, show play icon
                        audioHolder.playPauseAudio.setImageResource(R.drawable.ic_baseline_play_circle_24)
                    } else {
                        // Reset MediaPlayer
                        mediaPlayer?.reset()
                        try {
                            // Set the data source to the Firebase storage URL
                            val audioUrl = chatModel.recording // Firebase storage URL
                            mediaPlayer?.setDataSource(audioUrl)

                            // Use setOnPreparedListener to start playing when prepared
                            mediaPlayer?.setOnPreparedListener { mp ->
                                mp.start()
                                // Show pause icon
                                audioHolder.playPauseAudio.setImageResource(R.drawable.ic_baseline_pause_circle_24)
                            }

                            // Use setOnCompletionListener to detect when audio playback is completed
                            mediaPlayer?.setOnCompletionListener { mp ->
                                // Reset MediaPlayer when audio playback ends
                                mp.reset()
                                // Show play icon
                                audioHolder.playPauseAudio.setImageResource(R.drawable.ic_baseline_play_circle_24)
                            }

                            // Prepare the media player asynchronously
                            mediaPlayer?.prepareAsync()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }

                audioHolder.audioLine.setOnLongClickListener {
                    showDeleteConfirmationDialog(position)
                    true // Consume the long click event
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return chatModelArrayList.size
    }

    override fun getItemViewType(position: Int): Int {
        val chatModel = chatModelArrayList[position]

        return if (chatModel.senderId == firebaseUser?.uid) {
            when {
                chatModel.image.isNotEmpty() -> MESSAGE_TYPE_RIGHT_IMAGE
                chatModel.recording.isNotEmpty() -> MESSAGE_TYPE_RIGHT_AUDIO
                else -> MESSAGE_TYPE_RIGHT
            }
        } else {
            when {
                chatModel.image.isNotEmpty() -> MESSAGE_TYPE_LEFT_IMAGE
                chatModel.recording.isNotEmpty() -> MESSAGE_TYPE_LEFT_AUDIO
                else -> MESSAGE_TYPE_LEFT
            }
        }
    }

    class MyViewHolderText(
        itemView: View,
        private val binding: ChatItemRightUserBinding?,
        private val binding2: ChatItemLeftUserBinding?,
        private val context: Context,
        private val chatModelArrayList: List<ChatModel>
    ) : RecyclerView.ViewHolder(itemView) {

        val message: TextView = binding?.tvMessage ?: binding2!!.tvMessage
        val currentDate: TextView = binding?.tvDate ?: binding2!!.tvDate
        val currentTime: TextView = binding?.tvTime ?: binding2!!.tvTime
        val unread: TextView = binding?.tvUnread ?: binding2!!.tvUnread2
        val frameLayout: FrameLayout = binding?.frameLayout ?: binding2!!.frameLayout
        val receiverId: TextView? = binding?.tvReceiverId4

        init {

            // Add long click listener to the item to handle long clicks on the item
            message.setOnLongClickListener {
                showOptionsDialog(message.text.toString(), adapterPosition)
                true // Consume the long click event
            }
        }

        private fun showOptionsDialog(messageText: String, position: Int) {
            val options = arrayOf("Select Text","Copy All", "Delete")

            val builder = AlertDialog.Builder(context)
            builder.setTitle("Select an option")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> {
                            // Make the text selectable
                            message.setTextIsSelectable(true)

                            Toast.makeText(context, "Double tap to select text", Toast.LENGTH_SHORT).show()

                        }
                        1 -> copyToClipboard(messageText)
                        2 -> showDeleteConfirmationDialog(position)
                    }
                }

            builder.create().show()
        }

        private fun showDeleteConfirmationDialog(position: Int) {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.delete_chat_dialog_layout, null)
            val dialog = AlertDialog.Builder(context)
                .setView(dialogView)
                .create()

            val buttonYes = dialogView.findViewById<Button>(R.id.dialog_button_yes)
            val buttonCancel = dialogView.findViewById<Button>(R.id.dialog_button_cancel)

            buttonYes.setOnClickListener {
                // Get the message ID from the chatModelArrayList at the specified position
                val messageId = chatModelArrayList[position].id
                val receiverId = chatModelArrayList[position].receiverId

                // Call the deleteMessage function to delete the message
                deleteMessage(messageId, receiverId)
                dialog.dismiss()
            }

            buttonCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }

        private fun deleteMessage(messageId: String, receiverId: String) {
            val database = Firebase.database("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
            val reference = database.getReference("Chat")

            // Reference the specific message using its ID
            val messageReference = reference.child(messageId)

            // Remove the message from the database
            messageReference.removeValue()

            Toast.makeText(context, "Deleted Successfully!", Toast.LENGTH_SHORT).show()

            updateUnreadCount(receiverId)
        }

        private fun updateUnreadCount(receiverId: String) {
            // Initialize a reference to your Firebase Realtime Database
            val database = Firebase.database("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")

            val senderId = FirebaseAuth.getInstance().currentUser?.uid.toString()

            // Create a reference to the location where you store unread message counts
            val unreadCountsRef = database.getReference("UnreadCount")

            // Create a unique key based on the combination of senderId and receiverUid
            val uniqueKey = "$senderId-$receiverId"

            unreadCountsRef.child(uniqueKey).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val currentCount = dataSnapshot.getValue(Int::class.java) ?: 0
                    val newCount = currentCount - 1

                    if (newCount <= 0){
                        dataSnapshot.ref.removeValue()
                    } else {
                        dataSnapshot.ref.setValue(newCount)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle the error
                }
            })
        }

        private fun copyToClipboard(text: String) {
            val clipboard =
                itemView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied Text", text)
            clipboard.setPrimaryClip(clip)

            Toast.makeText(itemView.context, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    inner class MyViewHolderImage(
        itemView: View,
        private val binding: ChatItemImageRightUserBinding?,
        private val binding2: ChatItemImageLeftUserBinding?
    ) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = binding?.igPic ?: binding2!!.igPic
        val currentDate: TextView = binding?.tvDate ?: binding2!!.tvDate
        val currentTime: TextView = binding?.tvTime ?: binding2!!.tvTime
        val unread: TextView = binding?.tvUnread ?: binding2!!.tvUnread2
        val frameLayout: FrameLayout = binding?.frameLayout ?: binding2!!.frameLayout
        val receiverId: TextView? = binding?.tvReceiverId5
    }

    inner class MyViewHolderAudio(
        itemView: View,
       private val binding: ChatItemAudioRightUserBinding?,
       private val binding2: ChatItemAudioLeftUserBinding?
   ) : RecyclerView.ViewHolder(itemView) {
       val playPauseAudio: ImageButton = binding?.igButtonPlaypause ?: binding2!!.igButtonPlaypause
        val audioLine: TextView = binding?.tvAudio ?: binding2!!.tvAudio
        val currentDate: TextView = binding?.tvDate ?: binding2!!.tvDate
       val currentTime: TextView = binding?.tvTime ?: binding2!!.tvTime
       val unread: TextView = binding?.tvUnread ?: binding2!!.tvUnread2
       val frameLayout: FrameLayout = binding?.frameLayout ?: binding2!!.frameLayout
        val receiverId: TextView? = binding?.tvReceiverId6
   }

    // Inside the showDeleteConfirmationDialog function
    private fun showDeleteConfirmationDialog(position: Int) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.delete_chat_dialog_layout, null)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        val buttonYes = dialogView.findViewById<Button>(R.id.dialog_button_yes)
        val buttonCancel = dialogView.findViewById<Button>(R.id.dialog_button_cancel)

        buttonYes.setOnClickListener {
            // Get the message ID from the chatModelArrayList at the specified position
            val messageId = chatModelArrayList[position].id
            val receiverId = chatModelArrayList[position].receiverId

            // Call the deleteMessage function to delete the message
            deleteMessage(messageId, receiverId)
            dialog.dismiss()
        }

        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun deleteMessage(messageId: String, receiverId: String) {
        val database = Firebase.database("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val reference = database.getReference("Chat")

        // Reference the specific message using its ID
        val messageReference = reference.child(messageId)

        // Remove the message from the database
        messageReference.removeValue()

        Toast.makeText(context, "Deleted Successfully!", Toast.LENGTH_SHORT).show()

        updateUnreadCount(receiverId)
    }

    private fun updateUnreadCount(receiverId: String) {
        // Initialize a reference to your Firebase Realtime Database
        val database = Firebase.database("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")

        val senderId = FirebaseAuth.getInstance().currentUser?.uid.toString()

        // Create a reference to the location where you store unread message counts
        val unreadCountsRef = database.getReference("UnreadCount")

        // Create a unique key based on the combination of senderId and receiverUid
        val uniqueKey = "$senderId-$receiverId"

        unreadCountsRef.child(uniqueKey).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val currentCount = dataSnapshot.getValue(Int::class.java) ?: 0
                val newCount = currentCount - 1

                if (newCount <= 0){
                    dataSnapshot.ref.removeValue()
                } else {
                    dataSnapshot.ref.setValue(newCount)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error
            }
        })
    }

    // Function to check if a message is from an earlier time and has unread content
    private fun isMessageFromEarlierTime(position: Int): Boolean {
        if (position == 0) {
            // The first message is always considered from an earlier time
            return true
        }

        val currentMessage = chatModelArrayList[position]
        val previousMessage = chatModelArrayList[position - 1]

        // Check if the current message has unread content
        val hasUnreadInCurrent = currentMessage.unread.isNotEmpty()

        // Check if the previous message has unread content
        val previousHasUnread = previousMessage.unread.isNotEmpty()

        // Only display the unread TextView if the current message has unread content
        return hasUnreadInCurrent && !previousHasUnread
    }

    // Function to show the full-size image overlay
    private fun showFullSizeImage(imageUrl: String) {
        val inflater = LayoutInflater.from(context)
        val fullSizeView = inflater.inflate(R.layout.full_image_view, null)

        val fullSizeImageView: ImageView = fullSizeView.findViewById(R.id.imageView7)
        val closeButton: ImageButton = fullSizeView.findViewById(R.id.igbutton_close)

        val dialog = AlertDialog.Builder(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
            .setView(fullSizeView)
            .create()

        // Load the image into the ImageView using Glide or any other image loading library
        if (imageUrl.isNotEmpty()) {
            Glide.with(context).load(imageUrl).into(fullSizeImageView)

            dialog.show()

            // Set an OnClickListener for the close button
            closeButton.setOnClickListener {
                dialog.dismiss()
            }
        } else {
            // Handle the case where the image URL is empty or invalid
            // You can display an error message or take appropriate action here
        }
    }
}