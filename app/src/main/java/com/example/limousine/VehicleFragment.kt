package com.example.limousine

import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.limousine.databinding.FragmentHomeBinding
import com.example.limousine.databinding.FragmentVehicleBinding
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.view.ViewCompat


class VehicleFragment : Fragment() {


    private var _binding: FragmentVehicleBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentVehicleBinding.inflate(inflater, container, false)

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val sharedPreferences = requireContext().getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE)
        val storedPrice = sharedPreferences.getString("price", "")
        val priceWithoutRM = storedPrice?.replace("RM", "")?.trim()
        // Convert the string to a double
        val priceValue = priceWithoutRM?.toDouble()

        if (priceValue  != null) {
                val totalPrice = priceValue + 5.00

                val formattedTotalPrice = String.format("%.2f", totalPrice)
                binding.tvPrice4.text = formattedTotalPrice.toString()
        }

        // invisible total price for calculation
        val sharedPreferences3 = requireContext().getSharedPreferences("MySharedPreferences3", Context.MODE_PRIVATE)

        val price = binding.tvPrice4.text.toString()

        val editor = sharedPreferences3.edit()
        editor.putString("price", price)
        editor.putString("vehiclePrice", "RM5.00")
        editor.apply()

      binding.btnSedan.setOnClickListener {
             binding.tvVehicle.text = "Sedan"
             binding.igCarShow.setImageResource(R.drawable.car_3pax)
             binding.btnPax.setText("3")
             binding.btnLuggage.setText("2")
          if (priceValue != null) {
              val totalPrice = priceValue + 5.00

              val formattedTotalPrice = String.format("%.2f", totalPrice)
              binding.tvPrice4.text = formattedTotalPrice.toString()

              val sharedPreferences3 = requireContext().getSharedPreferences("MySharedPreferences3", Context.MODE_PRIVATE)

              val price = binding.tvPrice4.text.toString()

              val editor = sharedPreferences3.edit()
              editor.putString("price", price)
              editor.putString("vehiclePrice", "RM5.00")
              editor.apply()

              // Define the desired backgroundTint color
              val backgroundTint = ContextCompat.getColor(requireContext(), R.color.background_tint_color)

              // Set the backgroundTint using ViewCompat
              ViewCompat.setBackgroundTintList(binding.btnSedan, ColorStateList.valueOf(backgroundTint))

              val backgroundTintWhite = ContextCompat.getColor(requireContext(), R.color.white)

              ViewCompat.setBackgroundTintList(binding.btnMPV, ColorStateList.valueOf(backgroundTintWhite))

              ViewCompat.setBackgroundTintList(binding.btnVan, ColorStateList.valueOf(backgroundTintWhite))
          }
      }

        binding.btnMPV.setOnClickListener{
            binding.tvVehicle.text = "MPV"
            binding.igCarShow.setImageResource(R.drawable.car_4pax)
            binding.btnPax.setText("4")
            binding.btnLuggage.setText("3")
            if (priceValue != null) {
                val totalPrice = priceValue + 10.00

                val formattedTotalPrice = String.format("%.2f", totalPrice)
                binding.tvPrice4.text = formattedTotalPrice.toString()

                val sharedPreferences3 = requireContext().getSharedPreferences("MySharedPreferences3", Context.MODE_PRIVATE)

                val price = binding.tvPrice4.text.toString()

                val editor = sharedPreferences3.edit()
                editor.putString("price", price)
                editor.putString("vehiclePrice", "RM10.00")
                editor.apply()

                // Define the desired backgroundTint color
                val backgroundTint = ContextCompat.getColor(requireContext(), R.color.background_tint_color)

                // Set the backgroundTint using ViewCompat
                ViewCompat.setBackgroundTintList(binding.btnMPV, ColorStateList.valueOf(backgroundTint))

                val backgroundTintWhite = ContextCompat.getColor(requireContext(), R.color.white)

                ViewCompat.setBackgroundTintList(binding.btnSedan, ColorStateList.valueOf(backgroundTintWhite))

                ViewCompat.setBackgroundTintList(binding.btnVan, ColorStateList.valueOf(backgroundTintWhite))
            }
        }

        binding.btnVan.setOnClickListener{
            binding.tvVehicle.text = "Van"
            binding.igCarShow.setImageResource(R.drawable.car_7pax)
            binding.btnPax.setText("7")
            binding.btnLuggage.setText("5")
            if (priceValue != null) {
                val totalPrice = priceValue + 20.00

                val formattedTotalPrice = String.format("%.2f", totalPrice)
                binding.tvPrice4.text = formattedTotalPrice.toString()

                val sharedPreferences3 = requireContext().getSharedPreferences("MySharedPreferences3", Context.MODE_PRIVATE)

                val price = binding.tvPrice4.text.toString()

                val editor = sharedPreferences3.edit()
                editor.putString("price", price)
                editor.putString("vehiclePrice", "RM20.00")
                editor.apply()
            }
            // Define the desired backgroundTint color
            val backgroundTint = ContextCompat.getColor(requireContext(), R.color.background_tint_color)

            // Set the backgroundTint using ViewCompat
            ViewCompat.setBackgroundTintList(binding.btnVan, ColorStateList.valueOf(backgroundTint))

            val backgroundTintWhite = ContextCompat.getColor(requireContext(), R.color.white)

            ViewCompat.setBackgroundTintList(binding.btnSedan, ColorStateList.valueOf(backgroundTintWhite))

            ViewCompat.setBackgroundTintList(binding.btnMPV, ColorStateList.valueOf(backgroundTintWhite))
        }

        binding.etPax.setOnClickListener {
            val pax = arrayOf("1", "2", "3", "4", "5", "6", "7")

            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("How many passengers?")

            // Set the list of items and their click handler
            builder.setItems(pax) { dialog, which ->
                val selectedPax = pax[which]
                binding.etPax.setText(selectedPax)

                val width = resources.getDimensionPixelSize(R.dimen.width_50dp)
                // Set the width to the EditText
                val layoutParams = binding.etPax.layoutParams
                layoutParams.width = width
                binding.etPax.layoutParams = layoutParams
                binding.etPax.gravity = Gravity.CENTER

                dialog.dismiss() // Close the dialog
            }

            val dialog = builder.create()
            dialog.show()
        }

        binding.etLuggages.setOnClickListener {
            val luggage = arrayOf("1", "2", "3", "4", "5")

            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("How many luggages?")

            // Set the list of items and their click handler
            builder.setItems(luggage) { dialog, which ->
                val selectedLuggage = luggage[which]
                binding.etLuggages.setText(selectedLuggage)

                val width = resources.getDimensionPixelSize(R.dimen.width_50dp)
                // Set the width to the EditText
                val layoutParams = binding.etLuggages.layoutParams
                layoutParams.width = width
                binding.etLuggages.layoutParams = layoutParams
                binding.etLuggages.gravity = Gravity.CENTER

                dialog.dismiss() // Close the dialog
            }

            val dialog = builder.create()
            dialog.show()
        }

        binding.btnContinue2.setOnClickListener {
            if (validateInput()) {
                storeData()
                findNavController().navigate(R.id.action_nav_vehicle_to_nav_payment)
            }
        }

        binding.btnPax.setOnClickListener {
            val context: Context = requireContext()
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view: View = inflater.inflate(R.layout.tooltip_layout, null)

            val tooltipText = view.findViewById<TextView>(R.id.tooltipText)

            val btnPax = binding.btnPax.text.toString()

            if (btnPax == "3"){
                tooltipText.text = "Maximum 3 pax"
            } else if (btnPax == "4"){
                tooltipText.text = "Maximum 4 pax"
            } else if (btnPax == "7"){
                tooltipText.text = "Maximum 7 pax"
            }

            val popup = PopupWindow(
                view,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            )
            popup.showAsDropDown(binding.btnPax) // Display the popup near the button

            // Schedule the tooltip to be dismissed after a delay (e.g., 3 seconds)
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                popup.dismiss()
            }, 3000) // 3000 milliseconds = 3 seconds
        }


        binding.btnLuggage.setOnClickListener {
            val context: Context = requireContext()
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view: View = inflater.inflate(R.layout.tooltip_layout, null)

            val tooltipText = view.findViewById<TextView>(R.id.tooltipText)

            val btnLuggage = binding.btnLuggage.text.toString()

            if (btnLuggage == "2"){
                tooltipText.text = "Maximum 2 luggages"
            } else if (btnLuggage == "3"){
                tooltipText.text = "Maximum 3 luggages"
            } else if (btnLuggage == "5"){
                tooltipText.text = "Maximum 5 luggages"
            }

            val popup = PopupWindow(
                view,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            )
            popup.showAsDropDown(binding.btnLuggage) // Display the popup near the button

            // Schedule the tooltip to be dismissed after a delay (e.g., 3 seconds)
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                popup.dismiss()
            }, 3000) // 3000 milliseconds = 3 seconds
        }
    }

    private fun validateInput():Boolean {
        val btnPax = binding.btnPax.text.toString()
        val btnLuggage = binding.btnLuggage.text.toString()
        val pax = binding.etPax.text.toString()
        val luggage = binding.etLuggages.text.toString()

        if (pax.isEmpty()) {
            Toast.makeText(requireContext(), "Number of passengers is required", Toast.LENGTH_SHORT).show()
            return false
        }
        if (luggage.isEmpty()){
            Toast.makeText(requireContext(), "Number of luggages is required", Toast.LENGTH_SHORT).show()
            return false
        }
        if (btnPax == "3" && pax !in listOf("1", "2", "3")) {
            Toast.makeText(requireContext(), "Maximum 3 passengers only allowed", Toast.LENGTH_SHORT).show()
            return false
        }
        if (btnPax == "4" && pax !in listOf("1", "2", "3", "4")) {
            Toast.makeText(requireContext(), "Maximum 4 passengers only allowed", Toast.LENGTH_SHORT).show()
            return false
        }
        if (btnLuggage == "2" && luggage !in listOf("1", "2")) {
            Toast.makeText(requireContext(), "Maximum 2 luggages only allowed", Toast.LENGTH_SHORT).show()
            return false
        }
        if (btnLuggage == "3" && luggage !in listOf("1", "2", "3")) {
            Toast.makeText(requireContext(), "Maximum 3 luggages only allowed", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun storeData() {
        val sharedPreferences = requireContext().getSharedPreferences("MySharedPreferences2", Context.MODE_PRIVATE)

        val typeOfVehicle = binding.tvVehicle.text.toString()
        val passenger = binding.etPax.text.toString()
        val luggage = binding.etLuggages.text.toString()

        val editor = sharedPreferences.edit()
        editor.putString("typeOfVehicle", typeOfVehicle)
        editor.putString("passenger", passenger)
        editor.putString("luggage", luggage)
        editor.apply()
    }
}