package com.example.proyecto.network

import com.example.proyecto.models.Cliente
import com.example.proyecto.models.LoginRequest
import com.example.proyecto.models.LoginResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * Esta interfaz define TODOS los endpoints (direcciones) de la API.
 *
 * Cada función representa una operación que podemos hacer:
 * - login: iniciar sesión
 * - getClientes: obtener la lista de clientes
 * - crearCliente: agregar un nuevo cliente
 * - actualizarCliente: modificar un cliente existente
 * - eliminarCliente: borrar un cliente
 *
 * Las anotaciones (@POST, @GET, @PUT, @DELETE) indican el tipo de petición HTTP.
 */
interface ApiService {

    /**
     * Iniciar sesión en la API.
     * Método: POST
     * Dirección: https://userapi-m8ef.onrender.com/api/Auth/LogIn
     *
     * @param request Objeto con UserName y Password
     * @return Respuesta con el token JWT
     */
    @POST("Auth/LogIn")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    /**
     * Obtener todos los clientes.
     * Método: GET
     * Dirección: https://userapi-m8ef.onrender.com/api/Client/Get/Clientes
     *
     * @param token Token JWT en el header (formato: "Bearer {token}")
     * @return Lista de clientes
     */
    @GET("Client/Get/Clientes")
    suspend fun getClientes(
        @Header("Authorization") token: String
    ): Response<List<Cliente>>

    /**
     * Obtener un cliente específico por su clave.
     * Método: GET
     * Dirección: https://userapi-m8ef.onrender.com/api/Client/Get/Cliente{clave}
     *
     * @param token Token JWT en el header
     * @param clave Clave del cliente a buscar
     * @return Un cliente
     */
    @GET("Client/Get/Cliente{clave}")
    suspend fun getCliente(
        @Header("Authorization") token: String,
        @Path("clave") clave: String
    ): Response<Cliente>

    /**
     * Crear un nuevo cliente.
     * Método: POST
     * Dirección: https://userapi-m8ef.onrender.com/api/Client/Create/Cliente
     *
     * @param token Token JWT en el header
     * @param cliente Datos del cliente a crear
     * @return El cliente creado
     */
    @POST("Client/Create/Cliente")
    suspend fun crearCliente(
        @Header("Authorization") token: String,
        @Body cliente: Cliente
    ): Response<Cliente>

    /**
     * Actualizar un cliente existente.
     * Método: PUT
     * Dirección: https://userapi-m8ef.onrender.com/api/Client/Update/Cliente
     *
     * @param token Token JWT en el header
     * @param cliente Datos actualizados del cliente (debe incluir la clave)
     * @return El cliente actualizado
     */
    @PUT("Client/Update/Cliente")
    suspend fun actualizarCliente(
        @Header("Authorization") token: String,
        @Body cliente: Cliente
    ): Response<Cliente>

    /**
     * Eliminar un cliente por su clave.
     * Método: DELETE
     * Dirección: https://userapi-m8ef.onrender.com/api/Client/{clave}
     *
     * @param token Token JWT en el header
     * @param clave Clave del cliente a eliminar
     * @return El cliente eliminado
     */
    @DELETE("Client/{clave}")
    suspend fun eliminarCliente(
        @Header("Authorization") token: String,
        @Path("clave") clave: String
    ): Response<Cliente>
}