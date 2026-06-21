package com.example.proyecto

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyecto.models.Cliente
import com.example.proyecto.repository.ClienteRepository
import com.example.proyecto.utils.SessionManager
import kotlinx.coroutines.launch

/**
 * Fragmento para el CRUD de Clientes.
 *
 *
 * Funcionalidades:
 * 1. Buscar cliente automáticamente al escribir una clave
 * 2. Cargar los datos del cliente en el formulario
 * 3. Crear un nuevo cliente
 * 4. Actualizar un cliente existente
 * 5. Eliminar un cliente con confirmación
 * 6. Grid de clientes que al hacer click carga los datos
 * 7. Formato automático de fecha: DD-MM-YYYY
 */
class ClientesFragment : Fragment() {

    // Repositorio para comunicarse con la API
    private lateinit var repository: ClienteRepository

    // Manejador de sesión para obtener el token
    private lateinit var sessionManager: SessionManager

    // Adaptador para el grid de clientes
    private lateinit var adapter: ClienteGridAdapter

    // Indica si estamos editando un cliente existente
    private var isEditing = false

    // Clave del cliente que se está editando
    private var currentClienteClave: String? = null

    // Componentes de la interfaz de usuario
    private lateinit var campoClave: EditText
    private lateinit var campoNombre: EditText
    private lateinit var campoEdad: EditText
    private lateinit var campoFechaNacimiento: EditText
    private lateinit var botonNuevo: Button
    private lateinit var botonGuardar: Button
    private lateinit var botonEliminar: Button
    private lateinit var recyclerView: RecyclerView

    // Evita que el formateo de fecha entre en bucle
    private var isFormatting = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val vista = inflater.inflate(R.layout.fragment_clientes, container, false)

        // Inicializar dependencias
        repository = ClienteRepository()
        sessionManager = SessionManager(requireContext())

        // Conectar los componentes de la interfaz
        campoClave = vista.findViewById(R.id.etClave)
        campoNombre = vista.findViewById(R.id.etNombre)
        campoEdad = vista.findViewById(R.id.etEdad)
        campoFechaNacimiento = vista.findViewById(R.id.etFechaNacimiento)
        botonNuevo = vista.findViewById(R.id.btnNuevo)
        botonGuardar = vista.findViewById(R.id.btnGuardar)
        botonEliminar = vista.findViewById(R.id.btnEliminar)
        recyclerView = vista.findViewById(R.id.rvClientes)

        // Configurar el grid de clientes
        configurarRecyclerView()

        // Configurar el formato automático de fecha
        configurarFormateoFecha()

        // Configurar los eventos de los botones
        configurarListeners()

        // Cargar la lista de clientes al iniciar
        cargarClientes()

        return vista
    }

    /**
     * Configura el RecyclerView que muestra el grid de clientes.
     */
    private fun configurarRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ClienteGridAdapter { cliente ->
            // Al hacer click en un cliente, cargar sus datos en el formulario
            cargarClienteEnFormulario(cliente)
        }
        recyclerView.adapter = adapter
    }

    /**
     * Configura el formateo automático de fecha en formato DD-MM-YYYY.
     *
     * Ejemplo: cuando el usuario escribe "18012003",
     * se convierte automáticamente a "18-01-2003".
     */
    private fun configurarFormateoFecha() {
        campoFechaNacimiento.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No se necesita acción
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No se necesita acción
            }

            override fun afterTextChanged(s: Editable?) {
                // Si estamos formateando, salir para evitar bucles
                if (isFormatting) return
                if (s == null) return

                val texto = s.toString()
                if (texto.isEmpty()) return

                // Si ya tiene el formato completo, salir
                if (texto.matches(Regex("^\\d{2}-\\d{2}-\\d{4}$"))) return

                // Eliminar cualquier caracter que no sea número
                val soloNumeros = texto.replace(Regex("[^\\d]"), "")
                if (soloNumeros.isEmpty()) return

                isFormatting = true

                try {
                    // Construir el texto con guiones en formato DD-MM-YYYY
                    val textoFormateado = when {
                        soloNumeros.length <= 2 -> soloNumeros
                        soloNumeros.length <= 4 -> "${soloNumeros.substring(0, 2)}-${soloNumeros.substring(2)}"
                        soloNumeros.length <= 6 -> "${soloNumeros.substring(0, 2)}-${soloNumeros.substring(2, 4)}-${soloNumeros.substring(4)}"
                        else -> "${soloNumeros.substring(0, 2)}-${soloNumeros.substring(2, 4)}-${soloNumeros.substring(4, 8)}"
                    }

                    // Solo actualizar si el texto cambió
                    if (texto != textoFormateado) {
                        s.replace(0, s.length, textoFormateado)
                    }
                } catch (e: Exception) {
                    // No hacer nada si hay error
                } finally {
                    isFormatting = false
                }
            }
        })
    }

    /**
     * Configura los eventos de los botones y campos.
     */
    private fun configurarListeners() {
        // Buscar cliente al perder el foco del campo Clave
        campoClave.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                buscarClientePorClave()
            }
        }

        // Botón Nuevo: limpia el formulario
        botonNuevo.setOnClickListener {
            limpiarFormulario()
            isEditing = false
            currentClienteClave = null
            botonEliminar.isEnabled = false
            Toast.makeText(requireContext(), "Nuevo cliente", Toast.LENGTH_SHORT).show()
        }

        // Botón Guardar
        botonGuardar.setOnClickListener {
            guardarOActualizarCliente()
        }

        // Botón Eliminar: elimina el cliente actual
        botonEliminar.setOnClickListener {
            if (currentClienteClave != null) {
                mostrarDialogoEliminar()
            } else {
                Toast.makeText(requireContext(), "Selecciona un cliente", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Busca un cliente por su clave directamente en la API.
     *
     * Esta función se ejecuta cuando el usuario termina de escribir una clave
     * y el campo pierde el foco (setOnFocusChangeListener).
     */
    private fun buscarClientePorClave() {
        // Obtener la clave escrita por el usuario y eliminar espacios
        val clave = campoClave.text.toString().trim()

        // Si la clave está vacía, no hacer nada
        if (clave.isEmpty()) return

        // Obtener el token de sesión
        val token = sessionManager.getToken()

        // Si no hay token, la sesión expiró
        if (token == null) {
            Toast.makeText(requireContext(), "Sesión expirada", Toast.LENGTH_SHORT).show()
            return
        }

        // Iniciar una corrutina para hacer la petición a la API
        // lifecycleScope asegura que la corrutina se cancela si el fragmento se destruye
        lifecycleScope.launch {
            try {
                // Mostrar un mensaje al usuario para indicar que se está buscando
                Toast.makeText(requireContext(), "Buscando cliente...", Toast.LENGTH_SHORT).show()

                // Llamar a la API para obtener la lista completa de clientes
                val result = repository.getClientes(token)

                // Verificar si la petición fue exitosa
                if (result.isSuccess) {
                    // Obtener la lista de clientes
                    val clientes = result.getOrNull()

                    // Si la lista no es nula, buscar el cliente por su clave
                    if (clientes != null) {
                        // Buscar en la lista el cliente que coincida con la clave
                        val cliente = clientes.find { it.clave == clave }

                        if (cliente != null) {
                            // Cliente encontrado: cargar sus datos en el formulario
                            cargarClienteEnFormulario(cliente)
                            Toast.makeText(requireContext(), "Cliente encontrado", Toast.LENGTH_SHORT).show()
                        } else {
                            // Cliente no encontrado: permitir capturar uno nuevo
                            Toast.makeText(requireContext(), "Clave nueva, puedes capturar", Toast.LENGTH_SHORT).show()

                            // Limpiar el formulario pero mantener la clave escrita
                            limpiarFormulario()
                            campoClave.setText(clave)

                            // Resetear el estado de edición
                            isEditing = false
                            currentClienteClave = null
                            botonEliminar.isEnabled = false
                        }
                    }
                } else {
                    // Error en la petición: mostrar el mensaje de error
                    Toast.makeText(
                        requireContext(),
                        "Error al buscar: ${result.exceptionOrNull()?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                // Error inesperado: mostrar el mensaje de error
                Toast.makeText(requireContext(), "Error al buscar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Convierte una fecha de formato YYYY-MM-DD (API) a DD-MM-YYYY (para mostrar).
     *
     */
    private fun convertirFechaAParaMostrar(fechaAPI: String?): String {
        if (fechaAPI.isNullOrEmpty()) return ""
        if (!fechaAPI.contains("-")) return fechaAPI

        try {
            val partes = fechaAPI.split("-")
            if (partes.size == 3) {
                val anio = partes[0]
                val mes = partes[1]
                val dia = partes[2]
                return "$dia-$mes-$anio"
            }
        } catch (e: Exception) {
            // Si hay error, devolver la fecha original
        }
        return fechaAPI
    }

    /**
     * Convierte una fecha de formato DD-MM-YYYY (usuario) a YYYY-MM-DD (para la API).
     *
     */
    private fun convertirFechaParaAPI(fechaInput: String): String {
        // Si ya tiene el formato correcto, devolverla tal cual
        if (fechaInput.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
            return fechaInput
        }

        // Limpiar cualquier caracter que no sea número
        val soloNumeros = fechaInput.replace(Regex("[^\\d]"), "")
        if (soloNumeros.length == 8) {
            val dia = soloNumeros.substring(0, 2)
            val mes = soloNumeros.substring(2, 4)
            val anio = soloNumeros.substring(4, 8)
            return "$anio-$mes-$dia"
        }

        return fechaInput
    }

    /**
     * Carga un cliente en el formulario para editarlo.
     */
    private fun cargarClienteEnFormulario(cliente: Cliente) {
        try {
            campoClave.setText(cliente.clave)
            campoNombre.setText(cliente.nombre)
            campoEdad.setText(cliente.edad.toString())

            // Convertir la fecha de YYYY-MM-DD a DD-MM-YYYY para mostrar
            val fechaParaMostrar = convertirFechaAParaMostrar(cliente.fecha_Nacimiento)
            campoFechaNacimiento.setText(fechaParaMostrar)

            currentClienteClave = cliente.clave
            isEditing = true
            botonEliminar.isEnabled = true
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al cargar cliente: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Guarda o actualiza un cliente.
     *
     * El botón siempre dice "Guardar".
     * Si es un cliente nuevo, lo crea.
     * Si es un cliente existente, lo actualiza.
     * Muestra un mensaje diferente según la operación.
     */
    private fun guardarOActualizarCliente() {
        try {
            val clave = campoClave.text.toString().trim()
            val nombre = campoNombre.text.toString().trim()
            val edadTexto = campoEdad.text.toString().trim()
            val fechaInput = campoFechaNacimiento.text.toString().trim()

            // Validar que todos los campos estén llenos
            if (clave.isEmpty()) {
                Toast.makeText(requireContext(), "Clave requerida", Toast.LENGTH_SHORT).show()
                return
            }
            if (nombre.isEmpty()) {
                Toast.makeText(requireContext(), "Nombre requerido", Toast.LENGTH_SHORT).show()
                return
            }
            if (edadTexto.isEmpty()) {
                Toast.makeText(requireContext(), "Edad requerida", Toast.LENGTH_SHORT).show()
                return
            }
            if (fechaInput.isEmpty()) {
                Toast.makeText(requireContext(), "Fecha requerida", Toast.LENGTH_SHORT).show()
                return
            }

            // Validar que la edad sea un número entre 0 y 150
            val edad = edadTexto.toIntOrNull()
            if (edad == null || edad < 0 || edad > 150) {
                Toast.makeText(requireContext(), "Edad inválida", Toast.LENGTH_SHORT).show()
                return
            }

            // Convertir la fecha de DD-MM-YYYY a YYYY-MM-DD para la API
            val fechaFormateada = convertirFechaParaAPI(fechaInput)

            // Validar que la fecha convertida tenga el formato correcto
            if (!fechaFormateada.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
                Toast.makeText(requireContext(), "Fecha inválida. Usa DD-MM-YYYY", Toast.LENGTH_SHORT).show()
                return
            }

            val token = sessionManager.getToken()
            if (token == null) {
                Toast.makeText(requireContext(), "Sesión expirada", Toast.LENGTH_SHORT).show()
                return
            }

            // Crear el objeto Cliente
            val cliente = Cliente(
                clave = clave,
                nombre = nombre,
                edad = edad,
                fecha_Nacimiento = fechaFormateada
            )

            // Deshabilitar el botón mientras se procesa
            botonGuardar.isEnabled = false
            botonGuardar.text = "Guardando..."

            lifecycleScope.launch {
                try {
                    // Determinar si es creación o actualización
                    val esNuevo = !isEditing || currentClienteClave == null

                    val result = if (esNuevo) {
                        repository.crearCliente(token, cliente)
                    } else {
                        repository.actualizarCliente(token, cliente)
                    }

                    // Restaurar el botón
                    botonGuardar.isEnabled = true
                    botonGuardar.text = "Guardar"

                    if (result.isSuccess) {
                        // Mensaje diferente según la operación
                        val mensaje = if (esNuevo) {
                            "Cliente creado correctamente"
                        } else {
                            "Cliente actualizado correctamente"
                        }

                        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()

                        // Recargar la lista de clientes
                        cargarClientes()
                        limpiarFormulario()
                        isEditing = false
                        currentClienteClave = null
                        botonEliminar.isEnabled = false
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "${result.exceptionOrNull()?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Exception) {
                    botonGuardar.isEnabled = true
                    botonGuardar.text = "Guardar"
                    Toast.makeText(requireContext(), "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Muestra un diálogo para confirmar la eliminación.
     */
    private fun mostrarDialogoEliminar() {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Cliente")
            .setMessage("¿Eliminar a ${campoNombre.text}?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarCliente()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * Elimina el cliente actual.
     */
    private fun eliminarCliente() {
        val token = sessionManager.getToken()
        if (token == null || currentClienteClave == null) return

        botonEliminar.isEnabled = false
        botonEliminar.text = "Eliminando..."

        lifecycleScope.launch {
            try {
                val result = repository.eliminarCliente(token, currentClienteClave!!)

                botonEliminar.isEnabled = true
                botonEliminar.text = "Eliminar"

                if (result.isSuccess) {
                    Toast.makeText(requireContext(), "Cliente eliminado correctamente", Toast.LENGTH_SHORT).show()
                    cargarClientes()
                    limpiarFormulario()
                    isEditing = false
                    currentClienteClave = null
                    botonEliminar.isEnabled = false
                } else {
                    Toast.makeText(requireContext(), "Error al eliminar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                botonEliminar.isEnabled = true
                botonEliminar.text = "Eliminar"
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Carga la lista de clientes desde la API.
     */
    private fun cargarClientes() {
        val token = sessionManager.getToken()
        if (token == null) {
            Toast.makeText(requireContext(), "Sesión expirada", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val result = repository.getClientes(token)
                if (result.isSuccess) {
                    adapter.setClientes(result.getOrNull() ?: emptyList())
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al cargar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Limpia todos los campos del formulario.
     */
    private fun limpiarFormulario() {
        campoClave.text.clear()
        campoNombre.text.clear()
        campoEdad.text.clear()
        campoFechaNacimiento.text.clear()
        currentClienteClave = null
        isEditing = false
        botonEliminar.isEnabled = false
    }

    // ========== ADAPTADOR PARA EL GRID ==========

    /**
     * Adaptador interno para mostrar los clientes en el grid.
     *
     * Cada fila muestra:
     * - Clave (columna izquierda)
     * - Nombre (columna derecha)
     *
     * Al hacer click en una fila, se cargan los datos en el formulario.
     * Las filas alternan colores (gris claro / blanco) para mejor legibilidad.
     */
    inner class ClienteGridAdapter(
        private val onItemClick: (Cliente) -> Unit
    ) : RecyclerView.Adapter<ClienteGridAdapter.ClienteViewHolder>() {

        private var clientes: List<Cliente> = emptyList()

        /**
         * Obtiene la lista actual de clientes.
         * Útil para búsquedas locales.
         */
        fun getClientes(): List<Cliente> = clientes

        /**
         * Actualiza la lista de clientes y notifica los cambios.
         */
        fun setClientes(lista: List<Cliente>) {
            this.clientes = lista
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
            val vista = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_cliente, parent, false)
            return ClienteViewHolder(vista)
        }

        override fun onBindViewHolder(holder: ClienteViewHolder, posicion: Int) {
            holder.bind(clientes[posicion], posicion)
        }

        override fun getItemCount(): Int = clientes.size

        inner class ClienteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val textoClave: TextView = itemView.findViewById(R.id.tvClave)
            private val textoNombre: TextView = itemView.findViewById(R.id.tvNombre)

            fun bind(cliente: Cliente, posicion: Int) {
                textoClave.text = cliente.clave
                textoNombre.text = cliente.nombre

                // Alternar colores para mejor legibilidad
                if (posicion % 2 == 0) {
                    itemView.setBackgroundColor(0xFFF5F5F5.toInt())  // Gris claro
                } else {
                    itemView.setBackgroundColor(0xFFFFFFFF.toInt())  // Blanco
                }

                // Al hacer click, cargar el cliente en el formulario
                itemView.setOnClickListener { onItemClick(cliente) }
            }
        }
    }
}