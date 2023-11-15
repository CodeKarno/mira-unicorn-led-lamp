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
import android.widget.Switch
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.procrastinationcollaboration.miraunicornledlamp.R
import com.procrastinationcollaboration.miraunicornledlamp.databinding.FragmentSetupBinding
import com.procrastinationcollaboration.miraunicornledlamp.services.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SetupFragment : Fragment() {
    private companion object {
        private const val TAG = "Setup Fragment"
    }

    private lateinit var viewModel: SetupViewModel
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
        val context = requireContext()
        viewModel = ViewModelProvider(this)[SetupViewModel::class.java]

        binding.setupViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner


        lifecycleScope.launch {
            val connectedSuccessfully = DeviceConnectionService.checkDeviceServiceAvailable(viewModel.ledLampServiceUrl.value)
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
                    LampSetup.getApiService(context.applicationContext).setup(
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
            reset(context)
            viewModel.setConnectionState(DeviceConnection.NOT_AVAILABLE)
        }

        binding.saveIpBtn.setOnClickListener()
        {
            viewModel.setServiceIpAddress(binding.ipAddrInput.text.toString())
        }

        binding.extSettingsSwitch.setOnClickListener{ v ->
            val isChecked = (v as Switch).isChecked
            viewModel.setExtendedSettingsEnabled(isChecked)
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
                .setMessage(
                    getString(
                        R.string.message_old_api_temp_ap_network,
                        Consts.TEMP_AP_SSID
                    )
                )
                .show()
        } else {
            dialogBuilder
                .setMessage(getString(R.string.message_old_api_home_network))
                .show()
        }
    }

    private fun reset(context: Context) {
        lifecycleScope.launch {
            try {
                LedLamp.getApiService(viewModel.ledLampServiceUrl.value).reset()
                Log.d(TAG, "Connection reset.")
            } catch (e: Exception) {
                Log.e(TAG, e.message.toString())
                try {
                    LampSetup.getApiService(context.applicationContext).reset()
                    Log.d(TAG, "Connection reset (AP mode).")
                } catch (ex: Exception) {
                    Log.e(TAG, "Reset failed completely.")
                    Log.e(TAG, e.message.toString())
                }
            }
        }
    }
}