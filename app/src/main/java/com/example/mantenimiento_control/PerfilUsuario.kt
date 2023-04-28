package com.example.mantenimiento_control

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.mantenimiento_control.models.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PerfilUsuario : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    var rol : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_usuario)

        val bundle = intent.extras
        val email = bundle?.getString("email")
        setup(email?:"")
    }

    private fun setup(email: String) {
        obtenerDatosPerfilUsuario(email)

        val signOutImageView = findViewById<ImageView>(R.id.signOutImageView)
        val settingsadmin = findViewById<ImageView>(R.id.adminsettingsImageView)

        //Icono cerrar sesión
        signOutImageView.setOnClickListener{
            showAlertCerrarSesion()
        }

        //icono acciones de administrador
        settingsadmin.setOnClickListener {
            if (rol == "Administrador"){
                val intent = Intent(this, PerfilAdministrador::class.java)
                startActivity(intent)
            }else{
                val builder = AlertDialog.Builder(this)
                builder.setTitle("AVISO")
                builder.setMessage("No poses los permisos requeridos para acceder a las opciones de Administrador.")
                builder.setPositiveButton("Aceptar", null)
                val dialog: AlertDialog = builder.create()
                dialog.show()
            }
        }

        val saveButton = findViewById<Button>(R.id.saveProfileAppCompatButton)

        val nombre = findViewById<EditText>(R.id.nombreEditText)
        val direccion = findViewById<EditText>(R.id.direccionEditText)
        val telefono = findViewById<EditText>(R.id.telefonoEditText)
        val rol = "Rol no asignado"

        //guardar datos de usuario
        saveButton.setOnClickListener {
            db.collection("usuarios").document(email).set(
                Usuario(
                    nombre = nombre.text.toString(),
                    direccion = direccion.text.toString(),
                    telefono = telefono.text.toString(),
                    rol = rol
                )
            ).addOnCompleteListener {
                Toast.makeText(this,"Datos almacenados.", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this,"Error al almacenar los datos.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //Obtener datos del usuario
    private fun obtenerDatosPerfilUsuario(email: String) {
        val nombreTextView = findViewById<TextView>(R.id.nameTextView)
        val correoTextView = findViewById<TextView>(R.id.correoTextView)
        val rolTextView = findViewById<TextView>(R.id.rolTextView)
        val nombreEditText = findViewById<EditText>(R.id.nombreEditText)
        val direccionEditText = findViewById<EditText>(R.id.direccionEditText)
        val telefonoEditText = findViewById<EditText>(R.id.telefonoEditText)

        db.collection("usuarios").document(email).get().addOnSuccessListener {
            nombreTextView.setText(it.get("nombre") as String?)
            correoTextView.text = email
            rolTextView.setText(it.get("rol") as String?)
            rol = (it.get("rol") as String?).toString()
            nombreEditText.setText(it.get("nombre") as String?)
            direccionEditText.setText(it.get("direccion") as String?)
            telefonoEditText.setText(it.get("telefono") as String?)
        }
    }

    private fun showAlertCerrarSesion(){
        val builder = AlertDialog.Builder(this)
        val positiveButtonClick = { dialog: DialogInterface, which: Int ->

            //Borrando datos de la sesión
            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            prefs.clear()
            prefs.apply()

            //Cerrando sesión
            FirebaseAuth.getInstance().signOut()

            //Regresando a la vista de Autentificación
            val intent = Intent(this, Autentificacion::class.java)
            startActivity(intent)
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
        }

        builder.setTitle("Cerrar sesión")
        builder.setMessage("¿Desea cerrar sesión?")
        builder.setNegativeButton("Cancelar", null)
        builder.setPositiveButton("Aceptar", DialogInterface.OnClickListener(function = positiveButtonClick))
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}