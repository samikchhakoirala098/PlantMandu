package com.example.plantmandu.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plantmandu.model.PlantModel
import com.example.plantmandu.repository.PlantRepoImpl
import com.example.plantmandu.viewmodel.PlantViewModel
import com.google.firebase.auth.FirebaseAuth
import com.example.plantmandu.ui.theme.LeafGreen
import com.example.plantmandu.ui.theme.SoftGreen
import com.example.plantmandu.ui.theme.DarkGreen

import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import com.example.plantmandu.repository.UserRepoImpl
import com.example.plantmandu.viewmodel.UserViewModel
import androidx.compose.runtime.livedata.observeAsState

class AdminDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val plantViewModel = PlantViewModel(PlantRepoImpl())
        val userViewModel = UserViewModel(UserRepoImpl())
        setContent {
            AdminDashboardContent(plantViewModel, userViewModel, onAddPlant = {
                startActivity(Intent(this, AddPlantActivity::class.java))
            }, onLogout = {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardContent(plantViewModel: PlantViewModel, userViewModel: UserViewModel, onAddPlant: () -> Unit, onLogout: () -> Unit) {
    val plants by plantViewModel.plants.collectAsState()
    val bookings by plantViewModel.bookings.collectAsState()
    val allUsers by userViewModel.allUsers.observeAsState(emptyList())
    var selectedTab by remember { mutableIntStateOf(0) } // 0 for Inventory, 1 for Analytics
    var showLogoutDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(selectedTab) {
        if (selectedTab == 0) plantViewModel.fetchPlants()
        else {
            plantViewModel.fetchAllBookings()
            userViewModel.getAllUser()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (selectedTab == 0) "Admin Panel" else "Admin Analytics",
                        fontWeight = FontWeight.ExtraBold,
                        color = DarkGreen
                    )
                },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = DarkGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.List, contentDescription = "Inventory") },
                    label = { Text("Inventory") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = LeafGreen, indicatorColor = SoftGreen)
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Info, contentDescription = "Analytics") },
                    label = { Text("Analytics") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = LeafGreen, indicatorColor = SoftGreen)
                )
            }
        },
        containerColor = SoftGreen,
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(onClick = onAddPlant, containerColor = DarkGreen) {
                    Icon(Icons.Default.Add, contentDescription = "Add Plant", tint = Color.White)
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (selectedTab == 0) {
                AdminInventoryContent(plants, plantViewModel, context)
            } else {
                AdminAnalyticsContent(allUsers ?: emptyList(), bookings)
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout as Admin?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AdminInventoryContent(plants: List<PlantModel>, viewModel: PlantViewModel, context: android.content.Context) {
    var plantToDelete by remember { mutableStateOf<PlantModel?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Plant Inventory",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(plants) { plant ->
                AdminPlantItem(
                    plant,
                    onEdit = {
                        val intent = Intent(context, EditPlantActivity::class.java).apply {
                            putExtra("plantId", plant.plantId)
                            putExtra("name", plant.name)
                            putExtra("description", plant.description)
                            putExtra("price", plant.price)
                            putExtra("stock", plant.stock)
                        }
                        context.startActivity(intent)
                    },
                    onDeleteRequest = {
                        plantToDelete = plant
                    }
                )
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (plantToDelete != null) {
        AlertDialog(
            onDismissRequest = { plantToDelete = null },
            title = { Text("Delete Plant") },
            text = { Text("Are you sure you want to delete ${plantToDelete!!.name}? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.getBookingsByPlantId(plantToDelete!!.plantId) { success, list ->
                            if (list.isNotEmpty()) {
                                android.widget.Toast.makeText(context, "Cannot delete plant with active bookings", android.widget.Toast.LENGTH_LONG).show()
                                plantToDelete = null
                            } else {
                                viewModel.deletePlant(plantToDelete!!.plantId) { delSuccess, message ->
                                    android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
                                    if (delSuccess) viewModel.fetchPlants()
                                    plantToDelete = null
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Confirm Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { plantToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AdminAnalyticsContent(users: List<com.example.plantmandu.model.UserModel>, bookings: List<com.example.plantmandu.model.BookingModel>) {
    val totalRevenue = bookings.sumOf { it.totalPrice }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState())) {
        Text("Overview Metrics", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DarkGreen)
        Spacer(modifier = Modifier.height(20.dp))

        AnalyticsCard("Total Users", users.size.toString(), Icons.Default.Info, LeafGreen)
        Spacer(modifier = Modifier.height(12.dp))
        AnalyticsCard("Total Bookings", bookings.size.toString(), Icons.Default.ShoppingCart, androidx.compose.ui.graphics.Color.Blue)
        Spacer(modifier = Modifier.height(12.dp))
        AnalyticsCard("Total Revenue", "$$totalRevenue", Icons.Default.ShoppingCart, DarkGreen)
    }
}

@Composable
fun AnalyticsCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp), modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(label, fontSize = 14.sp, color = Color.Gray)
                Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}

@Composable
fun AdminPlantItem(plant: PlantModel, onEdit: () -> Unit, onDeleteRequest: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(plant.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    "$${plant.price}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = LeafGreen
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "In Stock: ${plant.stock}",
                fontSize = 14.sp,
                color = if (plant.stock > 0) Color.DarkGray else Color.Red
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onEdit) {
                    Text("Edit Info", color = LeafGreen)
                }
                TextButton(
                    onClick = onDeleteRequest,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Delete")
                }
            }
        }
    }
}

