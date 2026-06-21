package com.example.proyecto

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.proyecto.repository.ClienteRepository
import com.example.proyecto.utils.SessionManager
import kotlinx.coroutines.launch

/**
 * Pantalla de Login.
 *
 * Esta es la primera pantalla que ve el usuario al abrir la aplicación.
 *
 * Funcionalidades:
 * 1. Validar las credenciales del usuario contra la API
 * 2. Si son correctas, guardar el token y redirigir a la pantalla principal
 * 3. Siempre pide login al abrir la app (no guarda sesión entre ejecuciones)
 */
class MainActivity : AppCompatActivity() {

    // Manejador de sesión para guardar y recuperar el token
    private lateinit var sessionManager: SessionManager

    // Repositorio para comunicarse con la API
    private val repository = ClienteRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Inicializar el gestor de sesión
        sessionManager = SessionManager(this)

        // Siempre limpiar la sesión al iniciar la aplicación
        // Esto asegura que siempre pida login, incluso después de cerrar la app
        // Al limpiar la sesión, se elimina cualquier token guardado
        sessionManager.clearSession()

        // Si ya hay una sesión activa, ir directamente a la pantalla principal
        // Este código ya no se ejecutará porque siempre se limpia la sesión
        // pero se mantiene por si se desea cambiar el comportamiento después
        if (sessionManager.isLoggedIn()) {
            startActivity(Intent(this, Principal::class.java))
            finish()
        }

        // Configurar el padding para la barra de notificaciones
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Referencias a los elementos de la interfaz de usuario
        val campoUsuario: EditText = findViewById(R.id.ctUsuario)
        val campoPassword: EditText = findViewById(R.id.ctPassword)
        val botonAcceder: Button = findViewById(R.id.btnAcceder)

        // Evento click del botón Acceder
        botonAcceder.setOnClickListener {
            // Obtener las credenciales y eliminar espacios en blanco
            val username = campoUsuario.text.toString().trim()
            val password = campoPassword.text.toString().trim()

            // Validar que los campos no estén vacíos
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Deshabilitar el botón mientras se procesa la petición
            // Esto evita que el usuario haga clic múltiples veces
            botonAcceder.isEnabled = false
            botonAcceder.text = "Iniciando sesión..."

            // Llamar a la API para el login usando corrutinas
            // lifecycleScope lanza la corrutina en el ciclo de vida de la actividad
            // Esto asegura que la corrutina se cancela si la actividad se destruye
            lifecycleScope.launch {
                val result = repository.login(username, password)

                // Habilitar el botón nuevamente después de la respuesta
                botonAcceder.isEnabled = true
                botonAcceder.text = "ACCEDER"

                if (result.isSuccess) {
                    // Login exitoso
                    val data = result.getOrNull()!!

                    // Guardar el token y el nombre de usuario en la sesión
                    sessionManager.saveToken(data.token)
                    sessionManager.saveUsername(username)

                    // Mostrar mensaje de bienvenida
                    Toast.makeText(
                        this@MainActivity,
                        "Bienvenido $username",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Ir a la pantalla principal (con los tabs)
                    startActivity(Intent(this@MainActivity, Principal::class.java))
                    finish()
                } else {
                    // Login fallido: mostrar el error
                    Toast.makeText(
                        this@MainActivity,
                        "Error: ${result.exceptionOrNull()?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}