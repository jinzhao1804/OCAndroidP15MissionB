package com.example.eventorias.data

data class User(
    var receive_notifications: Boolean = false,
    val firstname: String = "",
    val lastname: String = "",
    val email: String = "",
    val imageUrl: String = ""
    )