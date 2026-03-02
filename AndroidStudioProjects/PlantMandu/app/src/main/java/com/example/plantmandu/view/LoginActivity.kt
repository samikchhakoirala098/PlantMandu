package com.example.plantmandu.view

import com.example.plantmandu.viewmodel.UserViewModel
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plantmandu.R
import com.example.plantmandu.ui.theme.Blue
import com.example.plantmandu.ui.theme.LightGrayBackground
import com.example.plantmandu.repository.UserRepoImpl
import com.example.plantmandu.view.DashboardActivity
import com.example.plantmandu.view.ForgetPasswordActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LoginBody()
        }
    }
}

@Composable
fun LoginBody() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var visibility by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as Activity
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }

    val fieldModifier = Modifier.fillMaxWidth()

    val inputColors = TextFieldDefaults.colors(
        unfocusedContainerColor = LightGrayBackground,
        focusedContainerColor = LightGrayBackground,
        focusedIndicatorColor = Blue,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 28.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        Text(
            text = "PlantMandu",
            style = TextStyle(fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Blue)
        )

        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("Email", fontWeight = FontWeight.Light) },
            modifier = fieldModifier.testTag("emailInput"),
            shape = RoundedCornerShape(12.dp),
            colors = inputColors,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Password", fontWeight = FontWeight.Light) },
            modifier = fieldModifier.testTag("passwordInput"),
            visualTransformation = if (visibility) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { visibility = !visibility }) {
                    Icon(
                        painter = painterResource(
                            if (!visibility) R.drawable.baseline_visibility_off_24
                            else R.drawable.baseline_visibility_24
                        ),
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = inputColors,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                } else {
                    userViewModel.login(email, password) { success, message ->
                        if (success) {
                            val userId = FirebaseAuth.getInstance().currentUser?.uid
                            if (userId != null) {
                                userViewModel.getUserById(userId) { userSuccess, userModel ->
                                    if (userSuccess && userModel != null) {
                                        if (userModel.role == "admin") {
                                            context.startActivity(Intent(context, AdminDashboardActivity::class.java))
                                        } else {
                                            context.startActivity(Intent(context, UserDashboardActivity::class.java))
                                        }
                                        activity.finish()
                                    } else {
                                        // Fallback if user model not found but logged in
                                        context.startActivity(Intent(context, UserDashboardActivity::class.java))
                                        activity.finish()
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(context, "Login Failed: $message", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .testTag("loginButton"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue)
        ) {
            Text("Log In", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Forgotten your login details? Reset your password.",
            fontSize = 13.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.clickable {
                context.startActivity(Intent(context, ForgetPasswordActivity::class.java))
            }
        )

        Spacer(modifier = Modifier.height(30.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
            Text("  OR  ", color = Color.Gray, fontSize = 12.sp)
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
        }

        Spacer(modifier = Modifier.height(30.dp))

        // --- Gmail Login ---
        OutlinedButton(
            onClick = { Toast.makeText(context, "Gmail login not implemented", Toast.LENGTH_SHORT).show() },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.img),
                contentDescription = "Gmail",
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log in with Gmail", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(30.dp))

        // --- Bottom SignUp Text ---
        Row(modifier = Modifier.padding(bottom = 30.dp)) {
            Text("Don't have an account? ", color = Color.Gray)
            Text(
                "SignUp",
                color = Blue,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    context.startActivity(Intent(context, RegistrationActivity::class.java))
                    activity.finish()
                }
            )
        }
    }
}
@Preview
@Composable
fun PreviewLogin(){
    LoginBody()
}