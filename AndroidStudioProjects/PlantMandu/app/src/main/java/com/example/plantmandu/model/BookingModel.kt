package com.example.plantmandu.model

data class BookingModel(
    var bookingId: String = "",
    var userId: String = "",
    var plantId: String = "",
    var plantName: String = "",
    var quantity: Int = 1,
    var totalPrice: Double = 0.0,
    var status: String = "Pending", // Pending, Confirmed, Cancelled
    var timestamp: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "bookingId" to bookingId,
            "userId" to userId,
            "plantId" to plantId,
            "plantName" to plantName,
            "quantity" to quantity,
            "totalPrice" to totalPrice,
            "status" to status,
            "timestamp" to timestamp
        )
    }
}
