package com.example.plantmandu.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plantmandu.model.BookingModel
import com.example.plantmandu.model.PlantModel
import com.example.plantmandu.repository.PlantRepoImpl
import com.example.plantmandu.viewmodel.PlantViewModel
import com.google.firebase.auth.FirebaseAuth
import com.example.plantmandu.ui.theme.LeafGreen
import com.example.plantmandu.ui.theme.SoftGreen
import com.example.plantmandu.ui.theme.DarkGreen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

class UserDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val viewModel = PlantViewModel(PlantRepoImpl())
        setContent {
            UserDashboardContent(viewModel, onLogout = {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDashboardContent(viewModel: PlantViewModel, onLogout: () -> Unit) {
    val plants by viewModel.plants.collectAsState()
    val bookings by viewModel.bookings.collectAsState()
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var selectedTab by remember { mutableIntStateOf(0) } // 0 for Explore, 1 for Bookings
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 1) viewModel.fetchBookingsByUser(userId)
        else viewModel.fetchPlants()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (selectedTab == 0) "PlantMandu" else "My Bookings", fontWeight = FontWeight.ExtraBold, color = DarkGreen) },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = DarkGreen)
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
                    icon = { Icon(Icons.Default.Home, contentDescription = "Explore") },
                    label = { Text("Explore") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = LeafGreen, selectedTextColor = LeafGreen, indicatorColor = SoftGreen)
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.List, contentDescription = "Bookings") },
                    label = { Text("Bookings") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = LeafGreen, selectedTextColor = LeafGreen, indicatorColor = SoftGreen)
                )
            }
        },
        containerColor = SoftGreen
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (selectedTab == 0) {
                ExploreContent(plants, userId, viewModel, context)
            } else {
                MyBookingsContent(bookings, viewModel, context)
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout from PlantMandu?") },
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
fun ExploreContent(plants: List<PlantModel>, userId: String, viewModel: PlantViewModel, context: android.content.Context) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        Text("Welcome, Nature Lover!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
        Text("Pick your favorite green companion", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(20.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(plants) { plant ->
                UserPlantItem(plant, onBook = {
                    if (plant.stock > 0) {
                        val booking = BookingModel(
                            userId = userId,
                            plantId = plant.plantId,
                            plantName = plant.name,
                            quantity = 1,
                            totalPrice = plant.price
                        )
                        viewModel.bookPlant(booking) { success, message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            if (success) viewModel.fetchPlants()
                        }
                    } else {
                        Toast.makeText(context, "Out of Stock", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
fun MyBookingsContent(bookings: List<BookingModel>, viewModel: PlantViewModel, context: android.content.Context) {
    var editingBooking by remember { mutableStateOf<BookingModel?>(null) }
    var bookingToDelete by remember { mutableStateOf<BookingModel?>(null) }

    if (bookings.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No bookings yet", color = Color.Gray)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { Spacer(modifier = Modifier.height(16.dp)) }
            items(bookings) { booking ->
                BookingItem(booking, onEdit = { editingBooking = booking }, onDelete = {
                    bookingToDelete = booking
                })
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }

    if (editingBooking != null) {
        EditBookingDialog(
            booking = editingBooking!!,
            onDismiss = { editingBooking = null },
            onSave = { newQty ->
                viewModel.updateBooking(editingBooking!!.bookingId, newQty) { success, message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    if (success) {
                        viewModel.fetchBookingsByUser(editingBooking!!.userId)
                        editingBooking = null
                    }
                }
            }
        )
    }

    if (bookingToDelete != null) {
        AlertDialog(
            onDismissRequest = { bookingToDelete = null },
            title = { Text("Cancel Booking") },
            text = { Text("Are you sure you want to cancel your booking for ${bookingToDelete!!.plantName}?") },
            confirmButton = {
                Button(
                    onClick = {
                        val userId = bookingToDelete!!.userId
                        viewModel.deleteBooking(bookingToDelete!!.bookingId) { success, message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            if (success) viewModel.fetchBookingsByUser(userId)
                            bookingToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { bookingToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun BookingItem(booking: BookingModel, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(booking.plantName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("$${booking.totalPrice}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = LeafGreen)
            }
            Text("Quantity: ${booking.quantity}", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onEdit) { Text("Edit", color = LeafGreen) }
                TextButton(onClick = onDelete) { Text("Cancel", color = Color.Red) }
            }
        }
    }
}

@Composable
fun EditBookingDialog(booking: BookingModel, onDismiss: () -> Unit, onSave: (Int) -> Unit) {
    var quantity by remember { mutableStateOf(booking.quantity.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Booking") },
        text = {
            Column {
                Text("Update quantity for ${booking.plantName}")
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val qty = quantity.toIntOrNull()
                if (qty != null && qty > 0) onSave(qty)
            }, colors = ButtonDefaults.buttonColors(containerColor = LeafGreen)) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun UserPlantItem(plant: PlantModel, onBook: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(plant.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text("$${plant.price}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = LeafGreen)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = plant.description,
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                val stockStatus = if (plant.stock > 0) "In Stock (${plant.stock})" else "Sold Out"
                val stockColor = if (plant.stock > 0) LeafGreen else Color.Red
                
                Surface(
                    color = stockColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(stockStatus, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = stockColor, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Button(
                    onClick = onBook,
                    enabled = plant.stock > 0,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LeafGreen, disabledContainerColor = Color.LightGray),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Book")
                }
            }
        }
    }
}
