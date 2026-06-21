package com.example.proyecto.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Esta clase guarda la información de sesión del usuario.
 *
 * SharedPreferences es un almacenamiento interno que guarda datos
 * aunque la aplicación se cierre. Aquí guardamos el token JWT.
 *
 * Los datos se guardan en un archivo llamado "AppPrefs" que solo
 * puede ser leído por esta aplicación.
 */
class SessionManager(context: Context) {

    /**
     * Archivo interno donde se guardan los datos de sesión.
     * MODE_PRIVATE significa que solo esta aplicación puede acceder.
     */
    private val prefs: SharedPreferences =
        context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

    /**
     * Guardar el token JWT.
     * El token se usa para autenticar todas las peticiones a la API.
     *
     * @param token Token JWT recibido del login
     */
    fun saveToken(token: String) {
        prefs.edit().putString("token", token).apply()
    }

    /**
     * Obtener el token guardado.
     *
     * @return El token si existe, o null si no hay sesión
     */
    fun getToken(): String? {
        return prefs.getString("token", null)
    }

    /**
     * Guardar el nombre de usuario para mostrarlo en la interfaz.
     * No es necesario para la autenticación, solo para mostrar información.
     *
     * @param username Nombre de usuario
     */
    fun saveUsername(username: String) {
        prefs.edit().putString("username", username).apply()
    }

    /**
     * Obtener el nombre de usuario guardado.
     *
     * @return El nombre de usuario, o null si no existe
     */
    fun getUsername(): String? {
        return prefs.getString("username", null)
    }

    /**
     * Verificar si hay una sesión activa.
     *
     * @return true si hay un token guardado, false en caso contrario
     */
    fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    /**
     * Cerrar la sesión eliminando todos los datos guardados.
     * Esto obliga al usuario a volver a iniciar sesión.
     */
    fun clearSession() {
        prefs.edit().clear().apply()
    }
}