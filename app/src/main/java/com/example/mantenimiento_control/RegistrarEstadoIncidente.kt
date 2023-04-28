package com.example.mantenimiento_control

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class RegistrarEstadoIncidente : AppCompatActivity() {

    lateinit var estadoSeleccionado: String

    //Fecha y Hora
    private val dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm:ss a", Locale("es", "MX")))

    private val database = Firebase.database
    private val db = FirebaseFirestore.getInstance()

    var imageUri : Uri = Uri.EMPTY

    //Nombre de Archivo de Imagen
    //Como ref se usa tambien la fecha y hora de la carga
    val fileName = SimpleDateFormat("dd_MM_yyyy_HH_mm_ss", Locale.getDefault()).format(Date())

    //PICKMEDIA
    val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()){
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
        setContentView(R.layout.activity_registrar_estado_incidente)

        setup()
    }

    private fun setup() {
        val btn_registrar_estado = findViewById<Button>(R.id.btn_registrar_estado)
        val btn_limpiar = findViewById<Button>(R.id.btn_limpiar)

        title = "Registrar Estado Incidencia"

        //Spinner Estados
        val spinner_estados = findViewById<Spinner>(R.id.spinner_estados)
        val listaEstados = resources.getStringArray(R.array.estados)
        val adaptadorEstados = ArrayAdapter(this, android.R.layout.simple_spinner_item,listaEstados)
        spinner_estados.adapter = adaptadorEstados

        spinner_estados.onItemSelectedListener = object:
            AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                Toast.makeText(this@RegistrarEstadoIncidente,"Seleccionó: "+listaEstados[position],
                    Toast.LENGTH_SHORT).show()
                estadoSeleccionado = listaEstados[position].toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        //Image Picker
        btnImagenEvidencia = findViewById(R.id.btn_evidencia_estado)
        imgView = findViewById(R.id.imageview)
        btnImagenEvidencia.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        //Acciones debajo del formulario

        btn_registrar_estado.setOnClickListener {
            validar()
        }

        btn_limpiar.setOnClickListener {
            limpiar()
        }
    }

    //Limpieza de datos
    private fun limpiar() {
        val descripcionEstadoIncidencia = findViewById<EditText>(R.id.txt_descripcion_estado_incidencia)
        descripcionEstadoIncidencia.setText("")
    }

    //Validar datos faltantes
    private fun validar() {
        val descripcionEstadoIncidencia = findViewById<EditText>(R.id.txt_descripcion_estado_incidencia)
        if (estadoSeleccionado.isNotEmpty() && descripcionEstadoIncidencia.text.toString().isNotEmpty()){
            registrar()
        } else {
            if(estadoSeleccionado=="")
                Toast.makeText(this, "Selecciona un Estado.", Toast.LENGTH_SHORT).show()
            else if(descripcionEstadoIncidencia.text.toString()=="")
                Toast.makeText(this, "Ingresa los detalles de la incidencia.", Toast.LENGTH_SHORT).show()
        }
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

    //Confirmacion de estado
    private fun showAlertConfirmar(){
        val builder = AlertDialog.Builder(this)
        val positiveButtonClick = { dialog: DialogInterface, which: Int ->
            uploadImage()
            updateFirebase()
            Toast.makeText(applicationContext, "Se ha confirmado el estado de la Incidencia", Toast.LENGTH_SHORT).show()
        }

        builder.setTitle("CONFIRMAR ESTADO DE LA INCIDENCIA")
        builder.setMessage("¿Desea confirmar el estado?")
        builder.setNegativeButton("Cancelar",null)
        builder.setPositiveButton("Aceptar", DialogInterface.OnClickListener(function = positiveButtonClick))

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    //Confirmacion de estado "Sin Imagen"
    private fun showAlertConfirmarNoImagen() {
        val builder = AlertDialog.Builder(this)
        val positiveButtonClick = { dialog: DialogInterface, which: Int ->
            updateFirebase()
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
            Toast.makeText(applicationContext, "Se ha dado enviado el estado de la Incidencia", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        builder.setTitle("ESTADO REGISTRADO")
        builder.setMessage("Estado de Incidencia registrado como "+estadoSeleccionado+"" +
                "\n "+dateTime)
        builder.setPositiveButton("Aceptar", DialogInterface.OnClickListener(function = positiveButtonClick))
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    //Cargar datos a firebase Database
    private fun updateFirebase(){
        val key = intent.getStringExtra("key")

        val email = intent.getStringExtra("emailTransfer")

        val ref = database.getReference("incidencias")

        val descripcionEstadoIncidencia = findViewById<EditText>(R.id.txt_descripcion_estado_incidencia)

        //TODO: CAMBIAR HASMAP POR EL MODELO DE LAS INCIDENCIAS
        val incidencias = mapOf(
            "estado" to estadoSeleccionado,
            "datetimeAtencion" to dateTime.toString(),
            "usuarioAtencion"  to "$email",
            "descripcionEstado" to descripcionEstadoIncidencia.text.toString(),
            "imagenEstadoIncidencia" to fileName
        )

        ref.child(key.toString()).updateChildren(incidencias).addOnSuccessListener {
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

        val storageRef = FirebaseStorage.getInstance().getReference("incidencias_estados/$fileName")

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