package com.example.limousine

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.example.limousine.Model.ChatModel
import com.example.limousine.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.nav_header_admin.*
import kotlinx.android.synthetic.main.nav_header_admin.imageView
import kotlinx.android.synthetic.main.nav_header_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*

class MainActivity : AppCompatActivity() {

    private var firebaseAuth = FirebaseAuth.getInstance()
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

      /*  binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }*/
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_profile, R.id.nav_contacts, R.id.nav_booking
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Set up a listener for fragment changes
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Check if the destination corresponds to the profile fragment
            if (destination.id == R.id.nav_profile) {
                // Call newUpdateHeader to refresh the header content
                newUpdateHeader()
            }
        }
        newUpdateHeader()
    }

    private fun newUpdateHeader() {
        try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
            val ref = Firebase.database("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Users/$userId")

            ref.get().addOnSuccessListener { dataSnapshot ->
                val navView: NavigationView = binding.navView
                val view: View = navView.getHeaderView(0)
                val fullName = dataSnapshot.child("fullName").getValue(String::class.java)
                val email = dataSnapshot.child("email").getValue(String::class.java)
                val profilePicUrl = dataSnapshot.child("profilePicture").child("url").getValue(String::class.java)

                view.tv_fullName.text = fullName
                view.tv_email.text = email

                if (!profilePicUrl.isNullOrEmpty()) {
                    val requestOptions = RequestOptions().signature(ObjectKey(System.currentTimeMillis()))

                    val imageView = view.findViewById<ImageView>(R.id.imageView) // Adjust the ID as needed
                    Glide.with(this).load(profilePicUrl).apply(requestOptions).into(imageView)
                } else {
                    // Set default profile image
                    view.findViewById<ImageView>(R.id.imageView)
                        .setImageResource(R.drawable.ic_baseline_account_circle_white_24)
                }
            }
        } catch (e: NullPointerException) {
            // Handle exceptions here
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
       // Inflate the menu; this adds items to the action bar if it is present.
       menuInflater.inflate(R.menu.main, menu)
       return true
   }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.action_log_out -> {
                val dialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog_layout, null)
                val dialog = AlertDialog.Builder(this)
                    .setView(dialogView)
                    .create()

                val buttonYes = dialogView.findViewById<Button>(R.id.dialog_button_yes)
                val buttonCancel = dialogView.findViewById<Button>(R.id.dialog_button_cancel)

                buttonYes.setOnClickListener {
                    // Remove the user from the "OnlineUsers" node
                    val uid = firebaseAuth.currentUser?.uid
                    if (uid != null) {
                        val onlineUsersRef = Firebase.database("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
                            .getReference("OnlineUsers").child(uid)
                        onlineUsersRef.removeValue()
                    }

                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser != null) {
                        val receiverId = currentUser.uid

                        val databaseReference: DatabaseReference =
                            Firebase.database("https://limousine-2309d-default-rtdb.asia-southeast1.firebasedatabase.app/")
                                .getReference("Chat")

                        // Create a query to find messages where senderId and receiverId match
                        val query = databaseReference.orderByChild("receiverId").equalTo(receiverId)

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

                    firebaseAuth.signOut()
                    startActivity(Intent(applicationContext, LoginActivity::class.java))
                    finish()
                    dialog.dismiss()
                }

                buttonCancel.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.show()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onResume() {
        super.onResume()

        // Refresh the header content every time the activity is resumed
        newUpdateHeader()
    }
}