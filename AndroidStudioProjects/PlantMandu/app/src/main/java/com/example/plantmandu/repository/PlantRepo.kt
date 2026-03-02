package com.example.plantmandu.repository

import com.example.plantmandu.model.PlantModel
import com.example.plantmandu.model.BookingModel

interface PlantRepo {
    fun addPlant(model: PlantModel, callback: (Boolean, String) -> Unit)
    fun getAllPlants(callback: (Boolean, List<PlantModel>) -> Unit)
    fun getPlantById(plantId: String, callback: (Boolean, PlantModel?) -> Unit)
    fun updatePlant(plantId: String, data: Map<String, Any?>, callback: (Boolean, String) -> Unit)
    fun deletePlant(plantId: String, callback: (Boolean, String) -> Unit)
    
    // Booking related
    fun bookPlant(model: BookingModel, callback: (Boolean, String) -> Unit)
    fun getBookingsByUser(userId: String, callback: (Boolean, List<BookingModel>) -> Unit)
    fun getAllBookings(callback: (Boolean, List<BookingModel>) -> Unit)
    fun getBookingsByPlantId(plantId: String, callback: (Boolean, List<BookingModel>) -> Unit)
    fun updateBooking(bookingId: String, newQuantity: Int, callback: (Boolean, String) -> Unit)
    fun deleteBooking(bookingId: String, callback: (Boolean, String) -> Unit)
}
