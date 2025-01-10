package com.example.eventorias

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.eventorias.ui.add.CreateEventViewModel
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class) // Use MockitoJUnitRunner to initialize mocks
class CreateEventViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockUri: Uri // Mock the Uri class

    private lateinit var viewModel: CreateEventViewModel
    private val testDispatcher = TestCoroutineDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher) // Use TestCoroutineDispatcher for coroutines
        viewModel = CreateEventViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // Reset the main dispatcher after tests
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `updateTitle should update the title in the state`() {
        val title = "Test Event"
        viewModel.updateTitle(title)
        assertEquals(title, viewModel.uiState.value.title)
    }

    @Test
    fun `updateDescription should update the description in the state`() {
        val description = "Test Description"
        viewModel.updateDescription(description)
        assertEquals(description, viewModel.uiState.value.description)
    }

    @Test
    fun `updateDate should update the date in the state`() {
        val date = "2023-10-01"
        viewModel.updateDate(date)
        assertEquals(date, viewModel.uiState.value.date)
    }

    @Test
    fun `updateTime should update the time in the state`() {
        val time = "10:00 AM"
        viewModel.updateTime(time)
        assertEquals(time, viewModel.uiState.value.time)
    }

    @Test
    fun `updateAddress should update the address in the state`() {
        val address = "123 Test St"
        viewModel.updateAddress(address)
        assertEquals(address, viewModel.uiState.value.address)
    }

    @Test
    fun `handleImageUri should update the imageUri in the state`() {
        // Use the mocked Uri
        viewModel.handleImageUri(mockUri)
        assertEquals(mockUri, viewModel.imageUri.value)
    }

    @Test
    fun `eventSaved should be false initially`() {
        assertEquals(false, viewModel.eventSaved.value)
    }
}