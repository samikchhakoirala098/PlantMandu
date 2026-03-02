package com.example.plantmandu

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.plantmandu.model.UserModel
import com.example.plantmandu.repository.UserRepo
import com.example.plantmandu.viewmodel.UserViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class UserViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Test
    fun login_success_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)
        val email = "test@example.com"
        val password = "password123"

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(2)
            callback(true, "Login success")
            null
        }.`when`(repo).login(eq(email), eq(password), any())

        var successResult = false
        var messageResult = ""

        viewModel.login(email, password) { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertTrue(successResult)
        assertEquals("Login success", messageResult)
        verify(repo).login(eq(email), eq(password), any())
    }

    @Test
    fun register_success_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)
        val email = "new@example.com"
        val password = "password123"

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, String) -> Unit>(2)
            callback(true, "Registration success", "u123")
            null
        }.`when`(repo).register(eq(email), eq(password), any())

        var successResult = false
        var messageResult = ""

        viewModel.register(email, password) { success, msg, uid ->
            successResult = success
            messageResult = msg
        }

        assertTrue(successResult)
        assertEquals("Registration success", messageResult)
        verify(repo).register(eq(email), eq(password), any())
    }
}
