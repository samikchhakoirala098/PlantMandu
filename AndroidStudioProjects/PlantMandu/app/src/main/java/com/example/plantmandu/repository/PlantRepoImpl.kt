package com.example.plantmandu.repository

import com.example.plantmandu.model.PlantModel
import com.example.plantmandu.model.BookingModel
import com.google.firebase.database.*

class PlantRepoImpl : PlantRepo {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val plantRef: DatabaseReference = database.getReference("Plants")
    private val bookingRef: DatabaseReference = database.getReference("Bookings")

    override fun addPlant(model: PlantModel, callback: (Boolean, String) -> Unit) {
        val id = plantRef.push().key ?: return callback(false, "Failed to generate ID")
        model.plantId = id
        plantRef.child(id).setValue(model).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Plant added successfully")
            } else {
                callback(false, it.exception?.message ?: "Unknown error")
            }
        }
    }

    override fun getAllPlants(callback: (Boolean, List<PlantModel>) -> Unit) {
        plantRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<PlantModel>()
                for (data in snapshot.children) {
                    data.getValue(PlantModel::class.java)?.let { list.add(it) }
                }
                callback(true, list)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, emptyList())
            }
        })
    }

    override fun getPlantById(plantId: String, callback: (Boolean, PlantModel?) -> Unit) {
        plantRef.child(plantId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback(true, snapshot.getValue(PlantModel::class.java))
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, null)
            }
        })
    }

    override fun updatePlant(plantId: String, data: Map<String, Any?>, callback: (Boolean, String) -> Unit) {
        plantRef.child(plantId).updateChildren(data).addOnCompleteListener {
            if (it.isSuccessful) {
                callback(true, "Plant updated successfully")
            } else {
                callback(false, it.exception?.message ?: "Unknown error")
            }
        }
    }

    override fun deletePlant(plantId: String, callback: (Boolean, String) -> Unit) {
        // Cascade delete: Remove all bookings associated with this plant
        bookingRef.orderByChild("plantId").equalTo(plantId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val deletionTasks = mutableListOf<com.google.android.gms.tasks.Task<Void>>()
                for (data in snapshot.children) {
                    deletionTasks.add(data.ref.removeValue())
                }
                
                // Once all bookings are marked for deletion, delete the plant
                plantRef.child(plantId).removeValue().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        callback(true, "Plant and associated bookings deleted successfully")
                    } else {
                        callback(false, task.exception?.message ?: "Failed to delete plant")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message)
            }
        })
    }

    override fun bookPlant(model: BookingModel, callback: (Boolean, String) -> Unit) {
        // First check if an existing booking exists for this user and plant
        bookingRef.orderByChild("userId").equalTo(model.userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var existingBooking: BookingModel? = null
                for (data in snapshot.children) {
                    val b = data.getValue(BookingModel::class.java)
                    if (b?.plantId == model.plantId) {
                        existingBooking = b
                        break
                    }
                }

                if (existingBooking != null) {
                    // Update existing booking
                    val updatedQuantity = existingBooking.quantity + model.quantity
                    // Transaction for stock reduction
                    plantRef.child(model.plantId).runTransaction(object : Transaction.Handler {
                        override fun doTransaction(currentData: MutableData): Transaction.Result {
                            val plant = currentData.getValue(PlantModel::class.java) ?: return Transaction.abort()
                            if (plant.stock < model.quantity) return Transaction.abort()
                            plant.stock -= model.quantity
                            currentData.value = plant
                            return Transaction.success(currentData)
                        }

                        override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                            if (committed) {
                                val plantPrice = snapshot?.getValue(PlantModel::class.java)?.price ?: 0.0
                                val updates = mapOf(
                                    "quantity" to updatedQuantity,
                                    "totalPrice" to (plantPrice * updatedQuantity)
                                )
                                bookingRef.child(existingBooking.bookingId).updateChildren(updates).addOnCompleteListener {
                                    if (it.isSuccessful) callback(true, "Booking updated")
                                    else callback(false, "Failed to update booking")
                                }
                            } else {
                                callback(false, "Stock insufficient or transaction failed")
                            }
                        }
                    })
                } else {
                    // Create new booking (existing logic)
                    val id = bookingRef.push().key ?: return callback(false, "Failed to generate ID")
                    model.bookingId = id
                    
                    plantRef.child(model.plantId).runTransaction(object : Transaction.Handler {
                        override fun doTransaction(currentData: MutableData): Transaction.Result {
                            val plant = currentData.getValue(PlantModel::class.java) ?: return Transaction.abort()
                            if (plant.stock < model.quantity) return Transaction.abort()
                            plant.stock -= model.quantity
                            currentData.value = plant
                            return Transaction.success(currentData)
                        }

                        override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                            if (committed) {
                                bookingRef.child(id).setValue(model).addOnCompleteListener {
                                    if (it.isSuccessful) callback(true, "Booking successful")
                                    else callback(false, "Failed to report booking")
                                }
                            } else {
                                callback(false, "Stock insufficient or error")
                            }
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message)
            }
        })
    }

    override fun getBookingsByUser(userId: String, callback: (Boolean, List<BookingModel>) -> Unit) {
        bookingRef.orderByChild("userId").equalTo(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<BookingModel>()
                for (data in snapshot.children) {
                    data.getValue(BookingModel::class.java)?.let { list.add(it) }
                }
                callback(true, list)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, emptyList())
            }
        })
    }

    override fun getAllBookings(callback: (Boolean, List<BookingModel>) -> Unit) {
        bookingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<BookingModel>()
                for (data in snapshot.children) {
                    data.getValue(BookingModel::class.java)?.let { list.add(it) }
                }
                callback(true, list)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, emptyList())
            }
        })
    }

    override fun getBookingsByPlantId(plantId: String, callback: (Boolean, List<BookingModel>) -> Unit) {
        bookingRef.orderByChild("plantId").equalTo(plantId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<BookingModel>()
                for (data in snapshot.children) {
                    data.getValue(BookingModel::class.java)?.let { list.add(it) }
                }
                callback(true, list)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, emptyList())
            }
        })
    }

    override fun updateBooking(bookingId: String, newQuantity: Int, callback: (Boolean, String) -> Unit) {
        bookingRef.child(bookingId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val booking = snapshot.getValue(BookingModel::class.java) ?: return callback(false, "Booking not found")
                val oldQuantity = booking.quantity
                val plantId = booking.plantId

                plantRef.child(plantId).runTransaction(object : Transaction.Handler {
                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                        val plant = currentData.getValue(PlantModel::class.java) ?: return Transaction.abort()
                        val diff = newQuantity - oldQuantity
                        if (plant.stock < diff) return Transaction.abort()

                        plant.stock -= diff
                        currentData.value = plant
                        return Transaction.success(currentData)
                    }

                    override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                        if (committed) {
                            val plantPrice = snapshot?.getValue(PlantModel::class.java)?.price ?: 0.0
                            val updates = mapOf(
                                "quantity" to newQuantity,
                                "totalPrice" to (plantPrice * newQuantity)
                            )
                            bookingRef.child(bookingId).updateChildren(updates).addOnCompleteListener {
                                if (it.isSuccessful) callback(true, "Booking updated")
                                else callback(false, "Failed to update booking")
                            }
                        } else {
                            callback(false, "Insufficient stock or error")
                        }
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message)
            }
        })
    }

    override fun deleteBooking(bookingId: String, callback: (Boolean, String) -> Unit) {
        bookingRef.child(bookingId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val booking = snapshot.getValue(BookingModel::class.java) ?: return callback(false, "Booking not found")
                val plantId = booking.plantId
                val quantity = booking.quantity

                // Restore stock
                plantRef.child(plantId).runTransaction(object : Transaction.Handler {
                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                        val plant = currentData.getValue(PlantModel::class.java) ?: return Transaction.abort()
                        plant.stock += quantity
                        currentData.value = plant
                        return Transaction.success(currentData)
                    }

                    override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                        if (committed) {
                            bookingRef.child(bookingId).removeValue().addOnCompleteListener {
                                if (it.isSuccessful) callback(true, "Booking cancelled")
                                else callback(false, "Failed to remove booking")
                            }
                        } else {
                            callback(false, "Failed to restore stock")
                        }
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message)
            }
        })
    }
}
