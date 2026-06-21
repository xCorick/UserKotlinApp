package com.example.proyecto.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Esta clase configura Retrofit, que es la herramienta que usamos para
 * hacer peticiones HTTP a la API.
 *
 * Es un "singleton", es decir, solo existe una instancia en toda la app.
 */
object RetrofitClient {

    /**
     * Dirección base de la API.
     * Todas las peticiones usan esta dirección + la ruta del endpoint.
     * Ejemplo: BASE_URL + "Auth/LogIn" = dirección completa
     */
    private const val BASE_URL = "https://userapi-m8ef.onrender.com/api/"

    /**
     * Interceptor que muestra en LogCat todas las peticiones y respuestas.
     * Útil para depurar errores durante el desarrollo.
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * Cliente HTTP con configuraciones básicas.
     * - addInterceptor: permite ver los logs
     * - connectTimeout: tiempo máximo para establecer conexión
     * - readTimeout: tiempo máximo para leer la respuesta
     * - writeTimeout: tiempo máximo para enviar datos
     */
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Instancia principal de Retrofit.
     * Convierte las funciones de ApiService en peticiones HTTP reales.
     * Usa Gson para convertir JSON a objetos Kotlin y viceversa.
     */
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /**
     * El servicio API que usaremos en toda la aplicación.
     *
     * Cómo se usa:
     *   val api = RetrofitClient.apiService
     *   val respuesta = api.login(LoginRequest("user", "pass"))
     */
    val apiService: ApiService = retrofit.create(ApiService::class.java)
}