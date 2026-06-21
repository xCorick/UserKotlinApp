package com.example.proyecto.models

/**
 * Datos que se envían al endpoint de login.
 * La API espera los campos con mayúscula inicial: UserName y Password.
 */

data class LoginRequest(
    val UserName: String,     // Nombre de usuario para la autenticación
    val Password: String      // Contraseña del usuario
)