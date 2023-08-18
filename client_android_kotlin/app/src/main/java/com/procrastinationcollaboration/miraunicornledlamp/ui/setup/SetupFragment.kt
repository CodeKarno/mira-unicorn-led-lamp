package com.procrastinationcollaboration.miraunicornledlamp.ui.setup

import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.procrastinationcollaboration.miraunicornledlamp.R
import com.procrastinationcollaboration.miraunicornledlamp.databinding.FragmentSetupBinding
import com.procrastinationcollaboration.miraunicornledlamp.services.Consts
import com.procrastinationcollaboration.miraunicornledlamp.services.DeviceConnection
import com.procrastinationcollaboration.miraunicornledlamp.services.DeviceConnectionService
import com.procrastinationcollaboration.miraunicornledlamp.services.LampSetup
import com.procrastinationcollaboration.miraunicornledlamp.services.LedLamp
import kotlinx.coroutines.launch

class SetupFragment : Fragment() {
    private companion object {
        private const val TAG = "Setup Fragment"
    }

    private val viewModel: SetupViewModel by viewModels()
    private lateinit var binding: FragmentSetupBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_setup, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        val context = requireContext()

        lifecycleScope.launch {
            val connectedSuccessfully = DeviceConnectionService.checkDeviceServerAvailable()
            if (!connectedSuccessfully)
                viewModel.setConnectionState(DeviceConnection.NOT_AVAILABLE)
            else
                viewModel.setConnectionState(DeviceConnection.WIFI_CONNECTED)
        }

        binding.findDeviceBtn.setOnClickListener {
            connectToDeviceAP(context)
        }
        binding.connectWifiBtn.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val ssid = binding.ssidInput.text.toString()
                    val pass = binding.pwdInput.text.toString()
                    LampSetup(context).apiService.setup(
                        ssid,
                        pass
                    )
                    connectToWifiNetwork(
                        ssid,
                        pass,
                        context
                    )
                } catch (e: Exception) {
                    Log.e(TAG, e.message.toString())
                }
            }
        }

        binding.resetBtn.setOnClickListener {
            reset()
            viewModel.setConnectionState(DeviceConnection.NOT_AVAILABLE)
        }
    }

    private fun connectToDeviceAP(context: Context) {
        connectToNetwork(Consts.TEMP_AP_SSID, Consts.TEMP_AP_PASS, true, context)
    }

    private fun connectToWifiNetwork(ssid: String, pass: String, context: Context) {
        connectToNetwork(ssid, pass, false, context)
    }


    private fun connectToNetwork(
        ssid: String,
        pass: String,
        isAccessPoint: Boolean,
        context: Context
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val connectivityManager =
                context.applicationContext
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    Log.d(TAG, "Selected network connected")

                    connectivityManager.bindProcessToNetwork(network)
                    if (isAccessPoint) viewModel.setConnectionState(DeviceConnection.TEMP_AP_CONNECTED)
                    else viewModel.setConnectionState(DeviceConnection.WIFI_CONNECTED)
                }
            }
            val wifiSpecifier: WifiNetworkSpecifier =

                WifiNetworkSpecifier.Builder()
                    .setIsHiddenSsid(isAccessPoint)
                    .setSsid(ssid)
                    .setWpa2Passphrase(pass)
                    .build()


            val networkRequest =
                NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .setNetworkSpecifier(wifiSpecifier).build()

            connectivityManager.requestNetwork(networkRequest, networkCallback)
        } else {
            connectToNetworkApi28(isAccessPoint, context)
        }
    }

    private fun connectToNetworkApi28(
        isAccessPoint: Boolean,
        context: Context,
    ) {
        viewModel.setConnectionState(DeviceConnection.OLD_API_CONNECTED)
        val dialogBuilder = AlertDialog.Builder(context)
        dialogBuilder.setTitle(getString(R.string.title_old_api_dialog))
        dialogBuilder.setPositiveButton(getString(R.string.title_old_api_dialog_btn_ok)) { dialog, _ -> dialog.dismiss() }
        if (isAccessPoint) {
            dialogBuilder
                .setMessage(getString(R.string.message_old_api_temp_ap_network, Consts.TEMP_AP_SSID))
                .show()
        } else {
            dialogBuilder
                .setMessage(getString(R.string.message_old_api_home_network))
                .show()
        }
    }

    private fun reset() {
        lifecycleScope.launch {
            try {
                LedLamp.apiService.reset()
                Log.d(TAG, "Connection reset.")
            } catch (e: Exception) {
                Log.e(TAG, e.message.toString())
            }
        }
    }
}