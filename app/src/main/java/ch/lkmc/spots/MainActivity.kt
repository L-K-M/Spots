package ch.lkmc.spots

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ch.lkmc.spots.ui.navigation.SpotsNavHost
import ch.lkmc.spots.ui.theme.SpotsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpotsTheme {
                SpotsNavHost()
            }
        }
    }
}
