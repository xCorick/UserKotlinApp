package com.example.proyecto.models

/**
 * Modelo que representa un cliente en el sistema.
 * Corresponde exactamente a la estructura que devuelve la API .NET.
 */

data class Cliente (
    val clave: String,            // Identificador único del cliente (ejemplo: "0001")
    val nombre: String,           // Nombre completo del cliente
    val edad: Int,                // Edad en años (número entero)
    val fecha_Nacimiento: String  // Fecha de nacimiento en formato "YYYY-MM-DD"

)






