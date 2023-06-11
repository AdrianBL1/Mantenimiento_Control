package com.example.mantenimiento_control

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.appcompat.app.AlertDialog
import com.example.mantenimiento_control.models.Incidencia
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class RealizarReporteIncidente : AppCompatActivity() {
    lateinit var email: String

    lateinit var rolSeleccionado: String
    lateinit var areaSeleccionada: String

    //Fecha y Hora
    private val dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm:ss a", Locale("es", "MX")))

    private val database = Firebase.database

    var imageUri : Uri = Uri.EMPTY

    //Nombre de Archivo de Imagen
    //Como ref se usa tambien la fecha y hora de la carga
    val fileName = SimpleDateFormat("dd_MM_yyyy_HH_mm_ss", Locale.getDefault()).format(Date())

    //PICKMEDIA
    val pickMedia = registerForActivityResult(PickVisualMedia()){
        uri ->
        if(uri!=null){
            //Imagen seleccionada
            Toast.makeText(this, "Imágen seleccionada.", Toast.LENGTH_SHORT).show()
            imgView.setImageURI(uri)
            imageUri = uri
        }else{
            //No seleccionada imagen
            Toast.makeText(this, "Imágen no seleccionada.", Toast.LENGTH_SHORT).show()
        }
    }

    lateinit var btnImagenEvidencia: Button
    lateinit var imgView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_realizar_reporte_incidente)

        setup()
    }

    private fun setup() {
        val btn_levantar_reporte = findViewById<Button>(R.id.btn_levantar_reporte)
        val btn_limpiar = findViewById<Button>(R.id.btn_limpiar)

        title = "Realizar Reporte Incidencia"

        //Spinner Roles
        val spinner_roles = findViewById<Spinner>(R.id.spinner_roles)
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
                Toast.makeText(this@RealizarReporteIncidente,"Seleccionó: "+listaRoles[position],
                    Toast.LENGTH_SHORT).show()
                rolSeleccionado = listaRoles[position].toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        //Spinner Areas
        val spinner_areas = findViewById<Spinner>(R.id.spinner_areas)
        val listaAreas = resources.getStringArray(R.array.areas)
        val adaptadorAreas = ArrayAdapter(this, android.R.layout.simple_spinner_item,listaAreas)
        spinner_areas.adapter = adaptadorAreas

        spinner_areas.onItemSelectedListener = object:
            AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                Toast.makeText(this@RealizarReporteIncidente,"Seleccionó: "+listaAreas[position],
                    Toast.LENGTH_SHORT).show()
                areaSeleccionada = listaAreas[position].toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        //Image Picker

        btnImagenEvidencia = findViewById(R.id.btn_evidencia)
        imgView = findViewById(R.id.imageview)
        btnImagenEvidencia.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        }

        //Acciones debajo del formulario

        btn_levantar_reporte.setOnClickListener {
            validar()
        }

        btn_limpiar.setOnClickListener {
            limpiar()
        }
    }

    //Validar datos faltantes
    private fun validar() {
        val descripcionIncidencia = findViewById<EditText>(R.id.txt_descripcion_incidencia)
        if (rolSeleccionado.isNotEmpty() && areaSeleccionada.isNotEmpty() && descripcionIncidencia.text.toString().isNotEmpty()){
            registrar()
        } else {
            if(rolSeleccionado=="")
                Toast.makeText(this, "Selecciona un Rol.", Toast.LENGTH_SHORT).show()
            else if(areaSeleccionada=="")
                Toast.makeText(this, "Selecciona un Area.", Toast.LENGTH_SHORT).show()
            else if(descripcionIncidencia.text.toString()=="")
                Toast.makeText(this, "Ingresa los detalles de la incidencia.", Toast.LENGTH_SHORT).show()
        }
    }

    //Limpieza de datos
    private fun limpiar() {
        val descripcionIncidencia = findViewById<EditText>(R.id.txt_descripcion_incidencia)
        descripcionIncidencia.setText("")
    }

    //Proceso de registro de datos
    private fun registrar() {
        //Enviar mensaje si no se cuenta con una imágen
        if (imageUri == Uri.EMPTY) {
            showAlertConfirmarNoImagen()
        }else{
            showAlertConfirmar()
        }
    }

    private fun showAlertConfirmar(){
        val builder = AlertDialog.Builder(this)
        val positiveButtonClick = { dialog: DialogInterface, which: Int ->
            uploadImage()
            addFirebase()
            Toast.makeText(applicationContext, "Se ha confirmado la Incidencia", Toast.LENGTH_SHORT).show()
        }

        builder.setTitle("CONFIRMAR INCIDENCIA")
        builder.setMessage("¿Desea confirmar incidencia?")
        builder.setNegativeButton("Cancelar",null)
        builder.setPositiveButton("Aceptar", DialogInterface.OnClickListener(function = positiveButtonClick))

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    //Confirmacion de estado "Sin Imagen"
    private fun showAlertConfirmarNoImagen() {
        val builder = AlertDialog.Builder(this)
        val positiveButtonClick = { dialog: DialogInterface, which: Int ->
            addFirebase()
            Toast.makeText(applicationContext, "Se ha confirmado el estado de la Incidencia", Toast.LENGTH_SHORT).show()
        }

        builder.setTitle("CONFIRMAR ESTADO DE LA INCIDENCIA 'NO IMAGEN'")
        builder.setMessage("No ingresaste una imágen. ¿Desea confirmar el estado?")
        builder.setNegativeButton("Cancelar",null)
        builder.setPositiveButton("Aceptar", DialogInterface.OnClickListener(function = positiveButtonClick))

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    //Mensaje de confirmacion
    private fun showAlertConfirmed() {

        val builder = AlertDialog.Builder(this)

        val positiveButtonClick = { dialog: DialogInterface, which: Int ->
            Toast.makeText(applicationContext, "Se ha dado enviado la Incidencia", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        builder.setTitle("INCIDENCIA CREADA")
        builder.setMessage("Incidencia enviada a "+rolSeleccionado+" para el area "+areaSeleccionada+"" +
                "\n "+dateTime)
        builder.setPositiveButton("Aceptar", DialogInterface.OnClickListener(function = positiveButtonClick))
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    //Cargar datos a firebase Database
    private fun addFirebase(){
        val bundle = intent.extras
        email = bundle?.getString("email").toString()

        val ref = database.getReference("incidencias")
        val descripcionIncidencia = findViewById<EditText>(R.id.txt_descripcion_incidencia)

        val incidencias = Incidencia(
            key = null,
            datetimeReporte = dateTime.toString(),
            usuarioReporte = "$email",
            rol = rolSeleccionado,
            area = areaSeleccionada,
            descripcion = descripcionIncidencia.text.toString(),
            imagenIncidencia = fileName,
            estado = "Pendiente",
            datetimeAtencion = null,
            usuarioAtencion = null,
            descripcionEstado = null,
            imagenEstadoIncidencia = null
        )

        ref.child(ref.push().key.toString()).setValue(incidencias)
            .addOnSuccessListener {
                showAlertConfirmed()
            }.addOnFailureListener { exception ->
                Toast.makeText(applicationContext, "Ha ocurrido un Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    //Cargar imágen a firebase storage
    private fun uploadImage(){
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Cargando Imágen")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val storageRef = FirebaseStorage.getInstance().getReference("incidencias/$fileName")

        storageRef.putFile(imageUri)
            .addOnCompleteListener{
                Toast.makeText(this, "Carga completa", Toast.LENGTH_SHORT).show()
                if (progressDialog.isShowing) progressDialog.dismiss()
            }.addOnFailureListener{ exception ->
                    if (progressDialog.isShowing) progressDialog.dismiss()
            Toast.makeText(applicationContext, "Carga fallida. Error: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }
}