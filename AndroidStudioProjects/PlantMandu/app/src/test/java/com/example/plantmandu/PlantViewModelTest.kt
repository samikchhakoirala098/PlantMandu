package com.example.plantmandu

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.plantmandu.model.PlantModel
import com.example.plantmandu.model.BookingModel
import com.example.plantmandu.repository.PlantRepo
import com.example.plantmandu.viewmodel.PlantViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class PlantViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Test
    fun addPlant_success_test() {
        val repo = mock<PlantRepo>()
        val viewModel = PlantViewModel(repo)
        
        val testPlant = PlantModel(
            plantId = "p1",
            name = "Rose",
            description = "Red rose",
            price = 10.0,
            stock = 5
        )

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Plant added successfully")
            null
        }.`when`(repo).addPlant(eq(testPlant), any())

        var successResult = false
        var messageResult = ""

        viewModel.addPlant(testPlant) { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertTrue(successResult)
        assertEquals("Plant added successfully", messageResult)
        verify(repo).addPlant(eq(testPlant), any())
    }

    @Test
    fun bookPlant_success_test() {
        val repo = mock<PlantRepo>()
        val viewModel = PlantViewModel(repo)
        
        val testBooking = BookingModel(
            bookingId = "b1",
            userId = "u1",
            plantId = "p1",
            plantName = "Rose",
            quantity = 1,
            totalPrice = 10.0
        )

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(1)
            callback(true, "Booking successful")
            null
        }.`when`(repo).bookPlant(eq(testBooking), any())

        var successResult = false
        var messageResult = ""

        viewModel.bookPlant(testBooking) { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertTrue(successResult)
        assertEquals("Booking successful", messageResult)
        verify(repo).bookPlant(eq(testBooking), any())
    }
}
