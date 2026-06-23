package com.example.proyecto

import android.app.DatePickerDialog
import android.os.Bundle
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Fragmento para el CRUD de Clientes.
 *
 * Funcionalidades:
 * 1. Buscar cliente automáticamente al escribir una clave
 * 2. Cargar los datos del cliente en el formulario
 * 3. Crear un nuevo cliente
 * 4. Actualizar un cliente existente
 * 5. Eliminar un cliente con confirmación
 * 6. Grid de clientes que al hacer click carga los datos
 * 7. Selector de fecha con DatePicker
 * 8. Cálculo automático de la edad basado en la fecha de nacimiento
 * 9. Restricción de fechas posteriores a hoy
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
    private lateinit var tvEdadCalculada: TextView
    private lateinit var campoFechaNacimiento: EditText
    private lateinit var botonNuevo: Button
    private lateinit var botonGuardar: Button
    private lateinit var botonEliminar: Button
    private lateinit var recyclerView: RecyclerView

    // Variables para almacenar la fecha seleccionada
    private var fechaSeleccionada: Date? = null

    // Formato de fecha para mostrar
    private val formatoFecha = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

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
        tvEdadCalculada = vista.findViewById(R.id.tvEdadCalculada)
        campoFechaNacimiento = vista.findViewById(R.id.etFechaNacimiento)
        botonNuevo = vista.findViewById(R.id.btnNuevo)
        botonGuardar = vista.findViewById(R.id.btnGuardar)
        botonEliminar = vista.findViewById(R.id.btnEliminar)
        recyclerView = vista.findViewById(R.id.rvClientes)

        // Configurar el grid de clientes
        configurarRecyclerView()

        // Configurar el DatePicker
        configurarDatePicker()

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
            cargarClienteEnFormulario(cliente)
        }
        recyclerView.adapter = adapter
    }

    /**
     * Configura el DatePicker para seleccionar la fecha de nacimiento.
     * Restringe fechas futuras (solo permite fechas hasta hoy).
     */
    private fun configurarDatePicker() {
        campoFechaNacimiento.setOnClickListener {
            mostrarDatePicker()
        }

        // También permitir que el usuario haga clic en el campo para abrir el selector
        campoFechaNacimiento.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                mostrarDatePicker()
            }
        }
    }

    /**
     * Muestra el DatePickerDialog con restricción de fecha máxima (hoy).
     */
    private fun mostrarDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Crear el DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                // Configurar la fecha seleccionada
                val fechaCalendar = Calendar.getInstance()
                fechaCalendar.set(selectedYear, selectedMonth, selectedDay)
                fechaSeleccionada = fechaCalendar.time

                // Mostrar la fecha en el campo
                campoFechaNacimiento.setText(formatoFecha.format(fechaSeleccionada!!))

                // Calcular y mostrar la edad
                calcularYMostrarEdad(fechaSeleccionada!!)
            },
            year, month, day
        )

        // Restringir fechas futuras (máximo = hoy)
        datePickerDialog.datePicker.maxDate = calendar.timeInMillis

        // Mostrar el diálogo
        datePickerDialog.show()
    }

    /**
     * Calcula la edad a partir de una fecha de nacimiento y la muestra en el TextView.
     */
    private fun calcularYMostrarEdad(fechaNacimiento: Date) {
        val hoy = Date()
        val edad = calcularEdad(fechaNacimiento, hoy)

        if (edad >= 0) {
            tvEdadCalculada.text = "$edad años"
        } else {
            tvEdadCalculada.text = "Fecha inválida"
        }
    }

    /**
     * Calcula la edad exacta entre dos fechas.
     */
    private fun calcularEdad(fechaNacimiento: Date, fechaActual: Date): Int {
        val diffInMillis = fechaActual.time - fechaNacimiento.time
        val diffInYears = TimeUnit.MILLISECONDS.toDays(diffInMillis) / 365

        // Calcular edad considerando el día exacto
        val calendarNac = Calendar.getInstance().apply { time = fechaNacimiento }
        val calendarAct = Calendar.getInstance().apply { time = fechaActual }

        var edad = calendarAct.get(Calendar.YEAR) - calendarNac.get(Calendar.YEAR)

        // Si aún no ha cumplido años este año, restar 1
        if (calendarAct.get(Calendar.MONTH) < calendarNac.get(Calendar.MONTH) ||
            (calendarAct.get(Calendar.MONTH) == calendarNac.get(Calendar.MONTH) &&
                    calendarAct.get(Calendar.DAY_OF_MONTH) < calendarNac.get(Calendar.DAY_OF_MONTH))) {
            edad--
        }

        return edad
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

        // Botón Eliminar
        botonEliminar.setOnClickListener {
            if (currentClienteClave != null) {
                mostrarDialogoEliminar()
            } else {
                Toast.makeText(requireContext(), "Selecciona un cliente", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Busca un cliente por su clave.
     */
    private fun buscarClientePorClave() {
        val clave = campoClave.text.toString().trim()
        if (clave.isEmpty()) return

        val token = sessionManager.getToken()
        if (token == null) {
            Toast.makeText(requireContext(), "Sesión expirada", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                Toast.makeText(requireContext(), "Buscando cliente...", Toast.LENGTH_SHORT).show()
                val result = repository.getClientes(token)

                if (result.isSuccess) {
                    val clientes = result.getOrNull()
                    if (clientes != null) {
                        val cliente = clientes.find { it.clave == clave }

                        if (cliente != null) {
                            cargarClienteEnFormulario(cliente)
                            Toast.makeText(requireContext(), "Cliente encontrado", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Clave nueva, puedes capturar", Toast.LENGTH_SHORT).show()
                            limpiarFormulario()
                            campoClave.setText(clave)
                            isEditing = false
                            currentClienteClave = null
                            botonEliminar.isEnabled = false
                        }
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error al buscar: ${result.exceptionOrNull()?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al buscar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Convierte una fecha de formato YYYY-MM-DD (API) a DD-MM-YYYY (para mostrar).
     */
    private fun convertirFechaAParaMostrar(fechaAPI: String?): String {
        if (fechaAPI.isNullOrEmpty()) return ""

        return try {
            val formatoAPI = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val fecha = formatoAPI.parse(fechaAPI)
            if (fecha != null) {
                formatoFecha.format(fecha)
            } else {
                fechaAPI
            }
        } catch (e: Exception) {
            fechaAPI
        }
    }

    /**
     * Convierte una fecha de formato DD-MM-YYYY a YYYY-MM-DD (para la API).
     */
    private fun convertirFechaParaAPI(fechaInput: String): String {
        return try {
            val fecha = formatoFecha.parse(fechaInput)
            val formatoAPI = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            if (fecha != null) {
                formatoAPI.format(fecha)
            } else {
                fechaInput
            }
        } catch (e: Exception) {
            fechaInput
        }
    }

    /**
     * Carga un cliente en el formulario para editarlo.
     */
    private fun cargarClienteEnFormulario(cliente: Cliente) {
        try {
            campoClave.setText(cliente.clave)
            campoNombre.setText(cliente.nombre)

            // Convertir y mostrar la fecha
            val fechaParaMostrar = convertirFechaAParaMostrar(cliente.fecha_Nacimiento)
            campoFechaNacimiento.setText(fechaParaMostrar)

            // Parsear la fecha para calcular la edad
            try {
                val fecha = formatoFecha.parse(fechaParaMostrar)
                if (fecha != null) {
                    fechaSeleccionada = fecha
                    calcularYMostrarEdad(fecha)
                }
            } catch (e: Exception) {
                tvEdadCalculada.text = "${cliente.edad} años"
            }

            currentClienteClave = cliente.clave
            isEditing = true
            botonEliminar.isEnabled = true
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al cargar cliente: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Guarda o actualiza un cliente.
     */
    private fun guardarOActualizarCliente() {
        try {
            val clave = campoClave.text.toString().trim()
            val nombre = campoNombre.text.toString().trim()
            val fechaInput = campoFechaNacimiento.text.toString().trim()

            // Validar campos
            if (clave.isEmpty()) {
                Toast.makeText(requireContext(), "Clave requerida", Toast.LENGTH_SHORT).show()
                return
            }
            if (nombre.isEmpty()) {
                Toast.makeText(requireContext(), "Nombre requerido", Toast.LENGTH_SHORT).show()
                return
            }
            if (fechaInput.isEmpty()) {
                Toast.makeText(requireContext(), "Fecha requerida", Toast.LENGTH_SHORT).show()
                return
            }

            // Obtener la edad del TextView
            val edadTexto = tvEdadCalculada.text.toString().replace(" años", "")
            val edad = edadTexto.toIntOrNull()

            if (edad == null || edad < 0 || edad > 150) {
                Toast.makeText(requireContext(), "Edad inválida", Toast.LENGTH_SHORT).show()
                return
            }

            // Validar que la fecha no sea futura
            if (fechaSeleccionada != null && fechaSeleccionada!!.after(Date())) {
                Toast.makeText(requireContext(), "La fecha no puede ser futura", Toast.LENGTH_SHORT).show()
                return
            }

            // Convertir la fecha para la API
            val fechaFormateada = convertirFechaParaAPI(fechaInput)
            if (!fechaFormateada.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
                Toast.makeText(requireContext(), "Fecha inválida. Usa DD-MM-YYYY", Toast.LENGTH_SHORT).show()
                return
            }

            val token = sessionManager.getToken()
            if (token == null) {
                Toast.makeText(requireContext(), "Sesión expirada", Toast.LENGTH_SHORT).show()
                return
            }

            // Crear el objeto Cliente con la edad calculada
            val cliente = Cliente(
                clave = clave,
                nombre = nombre,
                edad = edad,
                fecha_Nacimiento = fechaFormateada
            )

            botonGuardar.isEnabled = false
            botonGuardar.text = "Guardando..."

            lifecycleScope.launch {
                try {
                    val esNuevo = !isEditing || currentClienteClave == null
                    val result = if (esNuevo) {
                        repository.crearCliente(token, cliente)
                    } else {
                        repository.actualizarCliente(token, cliente)
                    }

                    botonGuardar.isEnabled = true
                    botonGuardar.text = "Guardar"

                    if (result.isSuccess) {
                        val mensaje = if (esNuevo) {
                            "Cliente creado correctamente"
                        } else {
                            "Cliente actualizado correctamente"
                        }

                        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
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
        tvEdadCalculada.text = "0 años"
        campoFechaNacimiento.text.clear()
        fechaSeleccionada = null
        currentClienteClave = null
        isEditing = false
        botonEliminar.isEnabled = false
    }

    // ========== ADAPTADOR PARA EL GRID ==========

    /**
     * Adaptador interno para mostrar los clientes en el grid.
     */
    inner class ClienteGridAdapter(
        private val onItemClick: (Cliente) -> Unit
    ) : RecyclerView.Adapter<ClienteGridAdapter.ClienteViewHolder>() {

        private var clientes: List<Cliente> = emptyList()

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
            private val textoEdad: TextView = itemView.findViewById(R.id.tvEdad)

            fun bind(cliente: Cliente, posicion: Int) {
                textoClave.text = cliente.clave
                textoNombre.text = cliente.nombre
                textoEdad.text = cliente.edad.toString()

                if (posicion % 2 == 0) {
                    itemView.setBackgroundColor(0xFFF5F5F5.toInt())
                } else {
                    itemView.setBackgroundColor(0xFFFFFFFF.toInt())
                }

                itemView.setOnClickListener { onItemClick(cliente) }
            }
        }
    }
}