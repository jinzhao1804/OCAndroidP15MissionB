package com.example.eventorias.data

data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val date: String = "",
    val time: String = "",
    val address: String = "",
    val user: User = User()
)
