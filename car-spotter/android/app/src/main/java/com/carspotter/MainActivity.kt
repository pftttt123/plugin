package com.carspotter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.carspotter.ui.CatalogViewModel
import com.carspotter.ui.navigation.CarSpotterNavGraph
import com.carspotter.ui.theme.CarSpotterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CarSpotterTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // One ViewModel shared across all screens (activity-scoped).
                    val viewModel: CatalogViewModel = viewModel(factory = CatalogViewModel.Factory)
                    CarSpotterNavGraph(viewModel = viewModel)
                }
            }
        }
    }
}
