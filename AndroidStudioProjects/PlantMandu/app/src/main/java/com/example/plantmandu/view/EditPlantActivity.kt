package com.example.plantmandu.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plantmandu.model.PlantModel
import com.example.plantmandu.repository.PlantRepoImpl
import com.example.plantmandu.viewmodel.PlantViewModel
import com.example.plantmandu.ui.theme.LeafGreen
import com.example.plantmandu.ui.theme.DarkGreen

class EditPlantActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val viewModel = PlantViewModel(PlantRepoImpl())
        val plantId = intent.getStringExtra("plantId") ?: ""
        val plantName = intent.getStringExtra("name") ?: ""
        val plantDesc = intent.getStringExtra("description") ?: ""
        val plantPrice = intent.getDoubleExtra("price", 0.0)
        val plantStock = intent.getIntExtra("stock", 0)

        setContent {
            EditPlantContent(
                viewModel,
                initialPlant = PlantModel(plantId, plantName, plantDesc, plantPrice, plantStock),
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPlantContent(viewModel: PlantViewModel, initialPlant: PlantModel, onBack: () -> Unit) {
    var name by remember { mutableStateOf(initialPlant.name) }
    var description by remember { mutableStateOf(initialPlant.description) }
    var price by remember { mutableStateOf(initialPlant.price.toString()) }
    var stock by remember { mutableStateOf(initialPlant.stock.toString()) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Plant", fontWeight = FontWeight.Bold, color = DarkGreen) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = DarkGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Plant Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price ($)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = stock,
                onValueChange = { stock = it },
                label = { Text("Stock Number") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    val p = price.toDoubleOrNull()
                    val s = stock.toIntOrNull()
                    if (name.isBlank() || description.isBlank() || p == null || s == null) {
                        Toast.makeText(context, "Please enter valid details", Toast.LENGTH_SHORT).show()
                    } else {
                        val updates = mapOf(
                            "name" to name,
                            "description" to description,
                            "price" to p,
                            "stock" to s
                        )
                        viewModel.updatePlant(initialPlant.plantId, updates) { success, message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            if (success) onBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LeafGreen)
            ) {
                Text("Update Plant", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
