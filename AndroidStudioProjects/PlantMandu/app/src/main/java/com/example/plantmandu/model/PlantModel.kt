package com.example.plantmandu.model

data class PlantModel(
    var plantId: String = "",
    var name: String = "",
    var description: String = "",
    var price: Double = 0.0,
    var stock: Int = 0,
    var imageUrl: String = ""
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "plantId" to plantId,
            "name" to name,
            "description" to description,
            "price" to price,
            "stock" to stock,
            "imageUrl" to imageUrl
        )
    }
}
