package com.example.plantmandu.viewmodel

import androidx.lifecycle.ViewModel
import com.example.plantmandu.model.PlantModel
import com.example.plantmandu.model.BookingModel
import com.example.plantmandu.repository.PlantRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PlantViewModel(private val repo: PlantRepo) : ViewModel() {
    private val _plants = MutableStateFlow<List<PlantModel>>(emptyList())
    val plants: StateFlow<List<PlantModel>> = _plants

    private val _bookings = MutableStateFlow<List<BookingModel>>(emptyList())
    val bookings: StateFlow<List<BookingModel>> = _bookings

    init {
        fetchPlants()
    }

    fun fetchPlants() {
        repo.getAllPlants { success, list ->
            if (success) _plants.value = list
        }
    }

    fun addPlant(model: PlantModel, callback: (Boolean, String) -> Unit) {
        repo.addPlant(model, callback)
    }

    fun updatePlant(plantId: String, data: Map<String, Any?>, callback: (Boolean, String) -> Unit) {
        repo.updatePlant(plantId, data, callback)
    }

    fun deletePlant(plantId: String, callback: (Boolean, String) -> Unit) {
        repo.deletePlant(plantId, callback)
    }

    fun bookPlant(model: BookingModel, callback: (Boolean, String) -> Unit) {
        repo.bookPlant(model, callback)
    }

    fun fetchBookingsByUser(userId: String) {
        repo.getBookingsByUser(userId) { success, list ->
            if (success) _bookings.value = list
        }
    }
    
    fun fetchAllBookings() {
        repo.getAllBookings { success, list ->
            if (success) _bookings.value = list
        }
    }

    fun getBookingsByPlantId(plantId: String, callback: (Boolean, List<BookingModel>) -> Unit) {
        repo.getBookingsByPlantId(plantId, callback)
    }

    fun updateBooking(bookingId: String, newQuantity: Int, callback: (Boolean, String) -> Unit) {
        repo.updateBooking(bookingId, newQuantity, callback)
    }

    fun deleteBooking(bookingId: String, callback: (Boolean, String) -> Unit) {
        repo.deleteBooking(bookingId, callback)
    }
}
