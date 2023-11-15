package com.procrastinationcollaboration.miraunicornledlamp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.procrastinationcollaboration.miraunicornledlamp.repositories.DataStoreRepository
import com.procrastinationcollaboration.miraunicornledlamp.services.Consts
import com.procrastinationcollaboration.miraunicornledlamp.services.DeviceConnectionService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var dataStoreRepository: DataStoreRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main)
        val navController = navHostFragment?.findNavController()

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_setup
            )
        )
        setupActionBarWithNavController(navController!!, appBarConfiguration)
        navView.setupWithNavController(navController)

        val mainView: View = findViewById(android.R.id.content)

        mainView.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                mainView.viewTreeObserver.removeOnPreDrawListener(this)
                var readyToRender: Boolean
                var lampConnected: Boolean
                runBlocking {
                    lampConnected = DeviceConnectionService.checkDeviceServiceAvailable(getBaseUrlFromStore())
                    readyToRender = true
                }
                
                if (!lampConnected) {
                    showNavigationDialog(navController)
                }
                return readyToRender
            }
        })
    }

    private suspend fun getBaseUrlFromStore():String? {
        if (dataStoreRepository.readBaseUrlFromStore.first().isNullOrEmpty()) {
            dataStoreRepository.saveServiceAddressToStore(Consts.LAMP_SERVER_BASE_URL)
        }
        return dataStoreRepository.readBaseUrlFromStore.first()
    }

    private fun showNavigationDialog(navController: NavController) {
        val dialogBuilder = AlertDialog.Builder(this@MainActivity)
        dialogBuilder
            .setTitle(getString(R.string.title_connection_dialog))
            .setMessage(
                getString(R.string.message_connection_dialog)
            )
        dialogBuilder.setPositiveButton(getString(R.string.title_reset_btn_connection_dialog))
        { dialog, _ ->
            dialog.dismiss()
            navigateUpTo(Intent(this@MainActivity, MainActivity::class.java))
            startActivity(intent)
        }
        dialogBuilder.setNegativeButton(getString(R.string.title_navigate_btn_connection_dialog))
        { _, _ ->
            navController.navigate(R.id.action_navigation_home_to_navigation_setup)
        }
        dialogBuilder.show()
    }
}