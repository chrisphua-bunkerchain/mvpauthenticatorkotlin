package com.example.mvpauthenticatorkotlin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.registerReceiver
import androidx.navigation.fragment.findNavController
import com.example.mvpauthenticatorkotlin.databinding.FragmentFirstBinding
import com.example.mvpauthenticatorkotlin.service.MyService
import com.google.android.material.snackbar.Snackbar
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Snackbar.make(binding.root, "Cancelled", Snackbar.LENGTH_LONG).show()
        } else {
            Snackbar.make(binding.root, "Scanned: " + result.contents, Snackbar.LENGTH_LONG).show()
            binding.textviewFirst.text = result.contents
        }
    }

    // *** 1. DEFINE THE BROADCAST RECEIVER ***
    private val mvpResultReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == MyService.ACTION_MVP_RESULT) {
                val status = intent.getStringExtra(MyService.EXTRA_STATUS)
                val message = "Result from MVP App: $status"
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
                // Update the TextView with the result
                binding.textviewFirst.text = message
            }
        }
    }

    // *** 2. REGISTER AND UNREGISTER THE RECEIVER ***
    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(MyService.ACTION_MVP_RESULT)
        registerReceiver(
            requireActivity(),
            mvpResultReceiver,
            intentFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onPause() {
        super.onPause()
        requireActivity().unregisterReceiver(mvpResultReceiver)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        binding.scannerButton.setOnClickListener {
            startScanner()
        }

        binding.launchMvpAppButton.setOnClickListener {
            binding.textviewFirst.text = "Starting background verification..."
            Log.d("FirstFragment", "Starting background verification process.")
            Snackbar.make(binding.root, "Verification sent to background...", Snackbar.LENGTH_SHORT).show()

            // Step 1: Generate the token payload.
            // The service in the MVP app is the only thing that needs this data.
            val token = MVPVerificationService.generateToken()
            Log.d("FirstFragment", "Generated Token for background service: $token")

            // Step 2: Send the token directly to the MVP app's background service.
            // This will NOT open the MVP app's UI.
            MVPVerificationService.sendToken(requireContext(), token)

            // The result will come back to MyService -> BroadcastReceiver automatically.
            binding.textviewFirst.text = "Token sent. Waiting for background result..."
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun startScanner() {
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setPrompt("Scan a QR code")
        options.setCameraId(0) // Use a specific camera of the device
        options.setBeepEnabled(false)
        options.setBarcodeImageEnabled(true)
        barcodeLauncher.launch(options)
    }
}