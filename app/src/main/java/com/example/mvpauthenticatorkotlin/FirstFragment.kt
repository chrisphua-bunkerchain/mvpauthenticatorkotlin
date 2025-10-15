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
import androidx.navigation.fragment.findNavController
//import androidx.navigation.fragment.findNavController
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

    private var jsonResult: String = ""

    private val mvpResultReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Check if the received broadcast has the correct action
            if (intent?.action == MyService.ACTION_MVP_RESULT) {
                val status = intent.getStringExtra(MyService.EXTRA_STATUS) ?: "No status received"
                val message = "Result from MVP App: $status"

                Log.d(FirstFragment::class.java.simpleName, message)

                // Update the UI with the final result
                binding.textviewFirst.text = message
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter(MyService.ACTION_MVP_RESULT)
        // For Android Tiramisu (API 33) and above, you must specify the exported flag.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireActivity().registerReceiver(
                mvpResultReceiver,
                intentFilter,
                Context.RECEIVER_EXPORTED // Use NOT_EXPORTED for internal app broadcasts
            )
        } else {
            requireActivity().registerReceiver(mvpResultReceiver, intentFilter)
        }
        Log.d(FirstFragment::class.java.simpleName, "MVP Result Receiver registered.")
    }

    override fun onStop() {
        super.onStop()
        requireActivity().unregisterReceiver(mvpResultReceiver)
        Log.d(FirstFragment::class.java.simpleName, "MVP Result Receiver unregistered.")
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

//        binding.buttonFirst.setOnClickListener {
//            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
//        }

        binding.scannerButton.setOnClickListener {
            startScanner()
        }

        binding.checkMvpAppButton.setOnClickListener {
            if (MVPVerificationService.checkMvpAppInstalled(requireActivity())) {
                Snackbar.make(binding.root, "MVP installed!", Snackbar.LENGTH_SHORT).show()
            } else {
                Snackbar.make(binding.root, "MVP not installed!", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.authenticateButton.setOnClickListener {
            if (jsonResult.isBlank()) {
                Log.d(FirstFragment::class.java.simpleName, "No QR code scanned!")
                Snackbar.make(binding.root, "No QR code scanned!", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val scannedJson = org.json.JSONObject(jsonResult)

            val accountNumber = scannedJson.optString("accountNumber", "")
            val code = scannedJson.optString("code", "")
            val codeType = scannedJson.optString("codeType", "")

            val imoNumber = scannedJson.optString("imoNumber", "")
            val licenseNumber = scannedJson.optString("licenseNumber", "")

            var authenticationCodeValue = ""

            if (codeType == "imoNumber") {
                authenticationCodeValue = imoNumber + accountNumber
            } else if (codeType == "licenseNumber") {
                authenticationCodeValue = licenseNumber + accountNumber
            } else {
                Log.d(FirstFragment::class.java.simpleName, "Code type not recognized: $codeType")
                Snackbar.make(
                    binding.root,
                    "Code type not recognized: $codeType",
                    Snackbar.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            binding.textviewFirst.text = "Starting background verification..."
            Log.d(FirstFragment::class.java.simpleName, "Starting background verification process.")
            Snackbar.make(binding.root, "Verification sent to background...", Snackbar.LENGTH_SHORT)
                .show()

            MVPVerificationService.authenticate(view.context, authenticationCodeValue, code)

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

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Snackbar.make(binding.root, "Cancelled", Snackbar.LENGTH_LONG).show()
        } else {
            Snackbar.make(binding.root, "Scanned", Snackbar.LENGTH_LONG).show()
            binding.textviewFirst.text = result.contents
            jsonResult = result.contents
        }
    }
}