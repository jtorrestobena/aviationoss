package com.bytecoders

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.bytecoders.ui.FlightViewModel
import com.bytecoders.ui.screens.FlightRadarApp
import com.bytecoders.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val viewModel = ViewModelProvider(this)[FlightViewModel::class.java]
        
        setContent {
            MyApplicationTheme {
                FlightRadarApp(viewModel = viewModel)
            }
        }
    }
}
