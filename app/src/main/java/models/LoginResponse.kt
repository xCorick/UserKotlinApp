package com.example.proyecto.models

/**
 * Respuesta que devuelve la API después de un login exitoso.
 * La API .NET devuelve un objeto JSON con la propiedad "token".
 * Este token JWT se usará para autenticar todas las peticiones posteriores.
 */

data class LoginResponse (
    val token: String     // Token JWT para autenticación
)