package com.adrianbl.mantenimiento_control

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.adrianbl.mantenimiento_control.fragments.TimePickerFragment
import com.adrianbl.mantenimiento_control.models.Usuario
import com.google.firebase.firestore.FirebaseFirestore

class PerfilAdministrador : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    var listaUsuarios: List<String> = emptyList()
    lateinit var usuarioSeleccionado: String
    lateinit var rolSeleccionado: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_administrador)

        //Obtener datos para el spinner de Usuarios
        db.collection("usuarios")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                    listaUsuarios += listOf(document.id)
                    spinnerUsuarios(listaUsuarios)
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }

        //Spinner Roles
        val spinner_roles = findViewById<Spinner>(R.id.spinnerRol)
        val listaRoles = resources.getStringArray(R.array.roles)
        val adaptadorRoles = ArrayAdapter(this, android.R.layout.simple_spinner_item,listaRoles)
        spinner_roles.adapter = adaptadorRoles

        spinner_roles.onItemSelectedListener = object:
            AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                Toast.makeText(this@PerfilAdministrador,"Seleccionó: "+listaRoles[position],
                    Toast.LENGTH_SHORT).show()
                rolSeleccionado = listaRoles[position].toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        //Botón para obtener los datos
        val obtener_datos_usuario = findViewById<Button>(R.id.getProfileAppCompatButton)
        obtener_datos_usuario.setOnClickListener {
            obtenerDatosUsuario(usuarioSeleccionado)
        }

        val nombre = findViewById<EditText>(R.id.nombreText)
        val direccion = findViewById<EditText>(R.id.direccionText)
        val telefono = findViewById<EditText>(R.id.telefonoText)
        val horaEntrada = findViewById<EditText>(R.id.horarioEntradaText)
        val horaSalida = findViewById<EditText>(R.id.horarioSalidaText)
        val saveButton = findViewById<AppCompatButton>(R.id.saveProfileData)

        //TimePicker, hora entrada
        horaEntrada.setOnClickListener {
            val timePicker = TimePickerFragment {
                horaEntrada.setText(it)
            }
            timePicker.show(supportFragmentManager,"time")
        }

        //TimePicker, hora salida
        horaSalida.setOnClickListener {
            val timePicker = TimePickerFragment {
                horaSalida.setText(it)
            }
            timePicker.show(supportFragmentManager,"time")
        }

        //Botón para guardar los datos
        saveButton.setOnClickListener {
            if (nombre.text.toString().isNotEmpty() && direccion.text.toString().isNotEmpty() && telefono.text.toString().isNotEmpty()){

                db.collection("usuarios").document(usuarioSeleccionado).set(
                    Usuario(
                        nombre = nombre.text.toString(),
                        direccion = direccion.text.toString(),
                        telefono = telefono.text.toString(),
                        fotoUsuario = null,
                        rol = rolSeleccionado,
                        horaEntrada = horaEntrada.text.toString(),
                        horaSalida = horaSalida.text.toString()
                    )
                ).addOnCompleteListener {
                    Toast.makeText(this,"Datos almacenados.", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this,"Error al almacenar los datos.", Toast.LENGTH_SHORT).show()
                }
            }else{
                comprobarFaltantes()
            }
        }
    }

    //Comprobar faltantes
    private fun comprobarFaltantes() {
        val nombre = findViewById<EditText>(R.id.nombreText)
        val direccion = findViewById<EditText>(R.id.direccionText)
        val telefono = findViewById<EditText>(R.id.telefonoText)

        if(nombre.text.toString() == "")
            Toast.makeText(this, "Ingresa un nombre.", Toast.LENGTH_SHORT).show()
        else if(direccion.text.toString() == "")
            Toast.makeText(this, "Ingresa una dirección.", Toast.LENGTH_SHORT).show()
        else if(telefono.text.toString() == "")
            Toast.makeText(this, "Ingresa un número de telefono.", Toast.LENGTH_SHORT).show()
    }

    //Spinner de Usuarios registrados en la aplicación obtenidos de Firebase Cloud Firestore
    private fun spinnerUsuarios(listaUsuarios: List<String>) {
        //Spinner Usuarios
        val spinner_usuarios = findViewById<Spinner>(R.id.spinnerUsuario)

        val adaptadorUsuarios = ArrayAdapter(this, android.R.layout.simple_spinner_item,listaUsuarios)
        Log.d(TAG, "Lista de usuarios: ${listaUsuarios}")
        spinner_usuarios.adapter = adaptadorUsuarios

        spinner_usuarios.onItemSelectedListener = object:
            AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                Toast.makeText(this@PerfilAdministrador,"Seleccionó: "+listaUsuarios[position],
                    Toast.LENGTH_SHORT).show()
                usuarioSeleccionado = listaUsuarios[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }

    //Obtener los datos del usuario de Firebase Cloud Firestore tomando como identificador el email seleccionado
    private fun obtenerDatosUsuario(email: String) {
        val nombreEditText = findViewById<EditText>(R.id.nombreText)
        val direccionEditText = findViewById<EditText>(R.id.direccionText)
        val telefonoEditText = findViewById<EditText>(R.id.telefonoText)

        val horaEntrada = findViewById<EditText>(R.id.horarioEntradaText)
        val horaSalida = findViewById<EditText>(R.id.horarioSalidaText)

        if(email == null){
            Toast.makeText(this,"No se pudo obtener el correo.", Toast.LENGTH_SHORT).show()
        }else{
            db.collection("usuarios").document(email).get().addOnSuccessListener {
                nombreEditText.setText(it.get("nombre") as String?)
                direccionEditText.setText(it.get("direccion") as String?)
                telefonoEditText.setText(it.get("telefono") as String?)
            }
        }

        horaEntrada.setText("")
        horaSalida.setText("")
    }
}