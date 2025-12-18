package com.example.plantmandu.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.plantmandu.R
import com.example.postifyapp.view.LoginActivity
import kotlinx.coroutines.delay


class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SplashBody()
        }
    }
}

@Composable
fun SplashBody() {
    val context = LocalContext.current
    val activity = context as Activity
    val currentUser = FirebaseAuth.getInstance().currentUser


    LaunchedEffect (Unit) {
        delay(3000)

        val intent= Intent(context,
            LoginActivity::class.java)
        context.startActivity(intent)
        activity.finish()
//        if (currentUser != null) {
//            // User already logged in
//            val intent = Intent(context, DashboardActivity::class.java)
//            context.startActivity(intent)
//            activity.finish()
//        } else {
//            // User not logged in
//            val intent = Intent(context, LoginActivity::class.java)
//            context.startActivity(intent)
//            activity.finish()
//        }

    }
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding()
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.sam),
                contentDescription = null,
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(100.dp))
            CircularProgressIndicator(
                color = Color.Blue
            )
        }
    }
}

@Preview
@Composable
fun SplashPreview() {
    SplashBody()
}