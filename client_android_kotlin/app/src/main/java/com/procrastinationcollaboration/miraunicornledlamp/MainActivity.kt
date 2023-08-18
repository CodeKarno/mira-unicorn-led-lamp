package com.procrastinationcollaboration.miraunicornledlamp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.procrastinationcollaboration.miraunicornledlamp.services.DeviceConnectionService
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {
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
                R.id.navigation_timer,
                R.id.navigation_setup
            )
        )
        setupActionBarWithNavController(navController!!, appBarConfiguration)
        navView.setupWithNavController(navController)

        val content: View = findViewById(android.R.id.content)


        content.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                content.viewTreeObserver.removeOnPreDrawListener(this)
                var readyToRender: Boolean
                var lampConnected: Boolean
                runBlocking {
                    lampConnected = DeviceConnectionService.checkDeviceServerAvailable()
                    readyToRender = true
                }
                if (!lampConnected) {
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
                return readyToRender
            }
        })
    }
}