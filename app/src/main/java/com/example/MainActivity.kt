package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.screens.MainGameApp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MathQuestViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: MathQuestViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        MainGameApp(viewModel = viewModel)
      }
    }
  }
}
