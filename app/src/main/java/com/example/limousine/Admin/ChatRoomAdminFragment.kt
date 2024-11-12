package com.example.limousine.Admin

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.limousine.Adapter.ChatAdapterAdmin
import com.example.limousine.ChatRoomFragment
import com.example.limousine.Model.ChatModel
import com.example.limousine.Model.ContactsModel
import com.example.limousine.R
import com.example.limousine.databinding.FragmentChatRoomAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_chat_room_admin.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatRoomAdminFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var chatModelArrayList: ArrayList<ChatModel>
    private lateinit var chatAdapterAdmin: ChatAdapterAdmin
    private lateinit var contactsModelArrayList: ArrayList<ContactsModel>

    private var isRecording = false
    private var mediaRecorder: MediaRecorder? = null
    private var audioFilePath: String? = null

    private var imageUri: Uri? = null
    private val storageRef = FirebaseStorage.getInstance().reference

    private var _binding: FragmentChatRoomAdminBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatRoomAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val profilePicture = requireArguments().getString("profilePicture").toString()
        val name = requireArguments().getString("name").toString()
        val uid = requireArguments().getString("uid").toString()
        val status = requireArguments().getString("status").toString()

        val requestOptions = RequestOptions()
            .placeholder(R.drawable.ic_baseline_account_circle_blue_24)


        Glide.with(requireContext())
            .load(profilePicture)
            .apply(requestOptions)
            .into(binding.imgPicture)

        binding.tvName3.text = name
        binding.tvUid3.text = uid

        if (status == "Offline") {
            binding.tvStatus3.text = status
            binding.tvStatus3.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
        } else {
            binding.tvStatus3.text = status
        }

        contactsModelArrayList = ArrayList()

        recyclerView = view.findViewById(R.id.recyclerViewChat)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)
        chatModelArrayList = arrayListOf<ChatModel>()

        chatAdapterAdmin = ChatAdapterAdmin(chatModelArrayList, requireContext())
        recyclerView.adapter = chatAdapterAdmin

        val currentUser = FirebaseAuth.getInstance().currentUser
        readMessage(currentUser!!.uid, uid)

        binding.etMessage.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //
            }

            override fun afterTextChanged(s: Editable?) {
                if(binding.etMessage.text.isEmpty()){
                    binding.igbtnMic.visibility = View.VISIBLE
                    binding.igbtnSend.visibility = View.INVISIBLE
                } else {
                    binding.igbtnMic.visibility = View.INVISIBLE
                    binding.igbtnSend.visibility = View.VISIBLE
                }
            }
        })

        binding.igbtnMic.setOnClickListener {
            if (!isRecording) {
                if (checkMicrophonePermission()) {
                    // Microphone permission granted, you can proceed with recording
                    startRecording()
                    binding.igbtnMic.setBackgroundResource(R.drawable.image_button_stop_background)
                    binding.igbtnMic.setImageResource(R.drawable.ic_baseline_stop_circle_24)
                } else {
                    // Microphone permission not granted, request it
                    requestMicrophonePermission()
                }
            } else {
                stopRecording()
                binding.igbtnMic.setBackgroundResource(R.drawable.image_button_background)
                binding.igbtnMic.setImageResource(R.drawable.ic_baseline_mic_24)
            }
        }

        binding.igbtnSend.setOnClickListener {
            val message = binding.etMessage.text.toString()
            val currentTimeMillis = System.currentTimeMillis()
            val sdf = SimpleDateFormat("HH:mm:ss")
            val formattedTime = sdf.format(Date(currentTimeMillis))

            val sdfDate = SimpleDateFormat("dd/MM/yyyy")
            val formattedDate = sdfDate.format(Date(currentTimeMillis))

            sendMessage(message, formattedDate, formattedTime, uid)
        }

        val galleryImg = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback { uri ->
                imageUri = uri

                val currentTimeMillis = System.currentTimeMillis()
                val sdf = SimpleDateFormat("HH:mm:ss")
                val formattedTime = sdf.format(Date(currentTimeMillis))

                val sdfDate = SimpleDateFormat("dd/MM/yyyy")
                val formattedDate = sdfDate.format(Date(currentTimeMillis))

                sendImage(imageUri!!, formattedDate, formattedTime, uid)
            })

        binding.igbtnImage.setOnClickListener {
            galleryImg.launch("image/*")
        }
    }

    private fun checkMicrophonePermission(): Boolean {
        val recordAudioPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        return recordAudioPermission
    }

    private fun requestMicrophonePermission() {
        requestPermissions(arrayOf(android.Manifest.permission.RECORD_AUDIO), 112)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 112) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Microphone permission granted, you can now start recording
                startRecording()
                binding.igbtnMic.setBackgroundResource(R.drawable.image_button_stop_background)
                binding.igbtnMic.setImageResource(R.drawable.ic_baseline_stop_circle_24)
            } else {
                // Handle microphone permission denied
                Toast.makeText(context, "Microphone Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startRecording() {
        if (mediaRecorder == null) {
            mediaRecorder = MediaRecorder()
        }

        // Set up MediaRecorder for audio recording in 3GP format
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP) // Use 3GP format
        audioFilePath = getTempFilePath() // Store the audio file path
        mediaRecorder?.setOutputFile(audioFilePath) // Use the provided file path
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB) // AMR-NB encoder

        // Prepare and start recording
        mediaRecorder?.prepare()
        mediaRecorder?.start()

        isRecording = true
    }

    private fun stopRecording() {
        if (isRecording) {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
            isRecording = false

            // After stopping the recording, store the audio URL in Firebase Realtime Database and upload to Firebase Storage
            if (audioFilePath != null) {
                val uid = requireArguments().getString("uid").toString()
                storeRecordingInFirebase(uid, audioFilePath!!)
            }
        }
    }

    private fun getTempFilePath(): String {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val audioDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC) // Use DIRECTORY_MUSIC or another suitable directory
        val audioFileName = "AUDIO_$timeStamp.3gp" // Use .3gp extension
        val audioFile = File(audioDir, audioFileName)
        return audioFile.absolutePath
    }

    private fun storeRecordingInFirebase(uid: String, audioFilePath: String) {
        // Store the audio in Firebase Storage
        val storageReference = FirebaseStorage.getInstance().reference
        val audioRef = storageReference.child("ChatAudios").child(System.currentTimeMillis().toString())

        val audioFile = Uri.fromFile(File(audioFilePath))

        audioRef.putFile(audioFile)
            .addOnSuccessListener { taskSnapshot ->
                // Get the download URL of the uploaded audio
                audioRef.downloadUrl.addOnSuccessListener { uri ->
                    val audioUrl = uri.toString()

                    // Store the audio URL in Firebase Realtime Database
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser != null) {
                        val senderId = currentUser.uid
                        val databaseReference = FirebaseDatabase.getInstance("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Chat")
                        val key = databaseReference.push().key

                        val hashMap = HashMap<String, Any>()
                        hashMap["id"] = key ?: ""
                        hashMap["senderId"] = senderId
                        hashMap["receiverId"] = uid
                        hashMap["currentTime"] = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                        hashMap["currentDate"] = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                        hashMap["unread"] = "Unread Messages"
                        hashMap["recording"] = audioUrl

                        if (key != null) {
                            // Store the data in the database
                            databaseReference.child(key).setValue(hashMap)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Audio Sent", Toast.LENGTH_SHORT).show()
                                    binding.etMessage.text.clear()
                                    updateUnreadCount(senderId, uid) // Update the unread message count
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Failed to Send", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(context, "Failed to Send", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Failed to Send", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to upload audio", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendMessage(
        messageText: String,
        formattedDate: String,
        formattedTime: String,
        receiverUid: String,
    ) {
        try {
            val currentUser = FirebaseAuth.getInstance().currentUser

            if (currentUser != null) {
                val senderId = currentUser.uid
                val database = Firebase.database("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
                val reference = database.getReference("Chat")
                val key = reference.push().key

                if (key != null) {
                    val hashMap = HashMap<String, Any>()
                    hashMap["id"] = key
                    hashMap["senderId"] = senderId
                    hashMap["receiverId"] = receiverUid
                    hashMap["currentTime"] = formattedTime
                    hashMap["currentDate"] = formattedDate
                    hashMap["unread"] = "Unread Messages"
                    hashMap["message"] = messageText

                    // Save the message with the image URL in Realtime Database
                    reference.child(key).setValue(hashMap)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(requireContext(), "Message Sent", Toast.LENGTH_SHORT).show()
                                binding.etMessage.text.clear()
                                // Pass the chat ID to updateUnreadCount
                                updateUnreadCount(senderId, receiverUid)
                            } else {
                                Toast.makeText(requireContext(), "Failed to Send", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            } else {
                Toast.makeText(context, "Failed to Send", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            // Handle exceptions
        }
    }

    private fun sendImage(imageUri: Uri, formattedDate: String, formattedTime: String, receiverId: String){
        try {
            val currentUser = FirebaseAuth.getInstance().currentUser

            if (currentUser != null) {
                val senderId = currentUser.uid
                val database = Firebase.database("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
                val reference = database.getReference("Chat")
                val key = reference.push().key

                if (key != null) {
                    val hashMap = HashMap<String, Any>()
                    hashMap["id"] = key
                    hashMap["senderId"] = senderId
                    hashMap["receiverId"] = receiverId
                    hashMap["currentTime"] = formattedTime
                    hashMap["currentDate"] = formattedDate
                    hashMap["unread"] = "Unread Messages"

                    if (imageUri != null) {
                        val imageRef = storageRef.child("ChatImages").child(System.currentTimeMillis().toString())

                        imageRef.putFile(imageUri)
                            .addOnSuccessListener { taskSnapshot ->
                                // Get the download URL of the uploaded image
                                imageRef.downloadUrl.addOnSuccessListener { uri ->
                                    val imageUrl = uri.toString()
                                    hashMap["image"] = imageUrl

                                    // Save the message with the image URL in Realtime Database
                                    reference.child(key).setValue(hashMap)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                Toast.makeText(requireContext(), "Image Sent", Toast.LENGTH_SHORT).show()
                                                binding.etMessage.text.clear()
                                                // Pass the chat ID to updateUnreadCount
                                                updateUnreadCount(senderId, receiverId)
                                            } else {
                                                Toast.makeText(requireContext(), "Failed to Send", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                }
                            }
                    } else {
                        Toast.makeText(context, "Failed to Send: No image selected", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Failed to Send", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Failed to Send", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            // Handle exceptions
        }
    }

    private fun readMessage(senderId: String, receiverId: String) {
        val databaseReference: DatabaseReference =
            Firebase.database("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Chat")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatModelArrayList.clear()
                for (dataSnapshot: DataSnapshot in snapshot.children) {
                    val chat = dataSnapshot.getValue(ChatModel::class.java)

                    if (chat!!.senderId == senderId && chat.receiverId == receiverId ||
                        chat.senderId == receiverId && chat.receiverId == senderId
                    ) {
                        chatModelArrayList.add(chat)
                    }
                }

                chatAdapterAdmin.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error here
            }
        })
    }

    private fun updateUnreadCount(senderId: String, receiverUid: String) {
        // Initialize a reference to your Firebase Realtime Database
        val database = Firebase.database("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")

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

    override fun onDestroyView() {
        super.onDestroyView()
        changeIsUnreadValueToFalse()
    }

    override fun onDestroy() {
        super.onDestroy()
        changeIsUnreadValueToFalse()
    }

    private fun changeIsUnreadValueToFalse() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = requireArguments().getString("uid").toString()

        if (currentUser != null) {
            val senderId = uid
            val receiverId = currentUser.uid

            val databaseReference: DatabaseReference =
                Firebase.database("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference("Chat")

            // Create a query to find messages where senderId and receiverId match
            val query = databaseReference.orderByChild("senderId").equalTo(senderId)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (dataSnapshot: DataSnapshot in snapshot.children) {
                        val chat = dataSnapshot.getValue(ChatModel::class.java)

                        // Check if the chat matches the receiverId
                        if (chat?.receiverId == receiverId) {
                            // Update the isUnread value to false
                            dataSnapshot.ref.child("unread").setValue("")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle the error here
                }
            })
        }

        if (currentUser != null) {
            val senderId = uid
            val receiverId = currentUser.uid

            val uniqueKey = "$senderId-$receiverId"

            val databaseReference: DatabaseReference =
                Firebase.database("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference("UnreadCount").child(uniqueKey)

            databaseReference.removeValue()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                    } else {
                    }
                }
        }
    }
}