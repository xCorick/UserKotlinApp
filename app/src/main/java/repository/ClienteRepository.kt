package com.example.proyecto.repository

import com.example.proyecto.models.Cliente
import com.example.proyecto.models.LoginResponse
import com.example.proyecto.models.LoginRequest
import com.example.proyecto.network.RetrofitClient
import retrofit2.HttpException
import java.io.IOException

/**
 * Esta clase es el intermediario entre la app y la API.
 *
 * Cada función:
 * 1. Llama a un endpoint de la API
 * 2. Maneja los errores (red, servidor, etc.)
 * 3. Devuelve un Result<T> que puede ser Éxito o Error
 *
 * Result<T> es una forma segura de manejar el éxito y el error.
 */
class ClienteRepository {

    // Conexión a la API (configurada en RetrofitClient)
    private val api = RetrofitClient.apiService

    /**
     * Iniciar sesión con usuario y contraseña.
     *
     * @param username Nombre de usuario
     * @param password Contraseña
     * @return Result<LoginResponse> - Éxito: contiene el token | Error: contiene la excepción
     */
    suspend fun login(username: String, password: String): Result<LoginResponse> {
        return try {
            // Crear la petición con los datos del login
            val respuesta = api.login(
                LoginRequest(
                    UserName = username,
                    Password = password
                )
            )

            // Verificar si la respuesta fue exitosa (código 200-299)
            if (respuesta.isSuccessful) {
                // Éxito: devolver el token
                Result.success(respuesta.body()!!)
            } else {
                // Error: la API devolvió un código de error (400, 401, 500, etc.)
                Result.failure(Exception("Error ${respuesta.code()}: ${respuesta.message()}"))
            }
        } catch (e: IOException) {
            // Error de red: sin conexión a Internet o timeout
            Result.failure(Exception("Error de red: ${e.message}"))
        } catch (e: HttpException) {
            // Error HTTP: problema en la comunicación
            Result.failure(Exception("Error HTTP: ${e.message}"))
        } catch (e: Exception) {
            // Error genérico: cualquier otra excepción
            Result.failure(Exception("Error: ${e.message}"))
        }
    }

    /**
     * Obtener la lista de todos los clientes.
     *
     * @param token Token JWT para autenticación
     * @return Result<List<Cliente>> - Éxito: lista de clientes | Error: excepción
     */
    suspend fun getClientes(token: String): Result<List<Cliente>> {
        return try {
            // Enviar el token en el header con el formato "Bearer {token}"
            val respuesta = api.getClientes("Bearer $token")
            if (respuesta.isSuccessful) {
                // Éxito: devolver la lista (vacía si es null)
                Result.success(respuesta.body() ?: emptyList())
            } else {
                Result.failure(Exception("Error ${respuesta.code()}: ${respuesta.message()}"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Error de red: ${e.message}"))
        } catch (e: HttpException) {
            Result.failure(Exception("Error HTTP: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Error: ${e.message}"))
        }
    }

    /**
     * Crear un nuevo cliente.
     *
     * @param token Token JWT para autenticación
     * @param cliente Datos del cliente a crear
     * @return Result<Cliente> - Éxito: cliente creado | Error: excepción
     */
    suspend fun crearCliente(token: String, cliente: Cliente): Result<Cliente> {
        return try {
            val respuesta = api.crearCliente("Bearer $token", cliente)
            if (respuesta.isSuccessful) {
                Result.success(respuesta.body()!!)
            } else {
                Result.failure(Exception("Error ${respuesta.code()}: ${respuesta.message()}"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Error de red: ${e.message}"))
        } catch (e: HttpException) {
            Result.failure(Exception("Error HTTP: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Error: ${e.message}"))
        }
    }

    /**
     * Actualizar un cliente existente.
     * La clave debe existir en la base de datos.
     *
     * @param token Token JWT para autenticación
     * @param cliente Datos actualizados del cliente (debe incluir la clave)
     * @return Result<Cliente> - Éxito: cliente actualizado | Error: excepción
     */
    suspend fun actualizarCliente(token: String, cliente: Cliente): Result<Cliente> {
        return try {
            val respuesta = api.actualizarCliente("Bearer $token", cliente)
            if (respuesta.isSuccessful) {
                Result.success(respuesta.body()!!)
            } else {
                Result.failure(Exception("Error ${respuesta.code()}: ${respuesta.message()}"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Error de red: ${e.message}"))
        } catch (e: HttpException) {
            Result.failure(Exception("Error HTTP: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Error: ${e.message}"))
        }
    }

    /**
     * Eliminar un cliente por su clave.
     *
     * @param token Token JWT para autenticación
     * @param clave Clave del cliente a eliminar
     * @return Result<Cliente> - Éxito: cliente eliminado | Error: excepción
     */
    suspend fun eliminarCliente(token: String, clave: String): Result<Cliente> {
        return try {
            val respuesta = api.eliminarCliente("Bearer $token", clave)
            if (respuesta.isSuccessful) {
                Result.success(respuesta.body()!!)
            } else {
                Result.failure(Exception("Error ${respuesta.code()}: ${respuesta.message()}"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Error de red: ${e.message}"))
        } catch (e: HttpException) {
            Result.failure(Exception("Error HTTP: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Error: ${e.message}"))
        }
    }
}