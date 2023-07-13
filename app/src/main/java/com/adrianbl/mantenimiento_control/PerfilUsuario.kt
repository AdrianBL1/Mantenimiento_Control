package com.adrianbl.mantenimiento_control

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import com.adrianbl.mantenimiento_control.models.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PerfilUsuario : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    var rol : String? = null

    var imageUri : Uri = Uri.EMPTY
    //Nombre de Archivo de Imagen
    //Como ref se usa tambien la fecha y hora de la carga
    val fileName = SimpleDateFormat("dd_MM_yyyy_HH_mm_ss", Locale.getDefault()).format(Date())
    var fileNameExist : String? = null

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

    lateinit var btnImagenPerfil: CardView
    lateinit var imgView: ImageView

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

        //guardar datos de usuario
        saveButton.setOnClickListener {
            comprobarImagen()
            db.collection("usuarios").document(email).set(
                Usuario(
                    nombre = nombre.text.toString(),
                    direccion = direccion.text.toString(),
                    telefono = telefono.text.toString(),
                    rol = rol,
                    fotoUsuario = fileNameExist
                )
            ).addOnCompleteListener {
                uploadImage(fileName)
                Toast.makeText(this,"Datos almacenados.", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this,"Error al almacenar los datos.", Toast.LENGTH_SHORT).show()
            }
        }

        //Image Picker
        btnImagenPerfil = findViewById(R.id.profileImageViewbtn)
        imgView = this.findViewById(R.id.profileImageView)
        btnImagenPerfil.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            comprobarImagen()
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
            if ((it.get("fotoUsuario") as String?) != null){
                mostrarImagenPerfil(it.get("fotoUsuario") as String)
                fileNameExist = it.get("fotoUsuario") as String?
            }
        }
    }

    private fun showAlertCerrarSesion(){
        val builder = AlertDialog.Builder(this)
        val positiveButtonClick = { dialog: DialogInterface, which: Int ->

            //Borrando datos de la sesión
            borrarDatos()

            //Cerrando sesión
            FirebaseAuth.getInstance().signOut()

            //Regresando a la vista de Autentificación
            val intent = Intent(this, Autentificacion::class.java)
            startActivity(intent)
            Toast.makeText(this, "Sesión cerrada.", Toast.LENGTH_SHORT).show()
        }

        builder.setTitle("Cerrar sesión")
        builder.setMessage("¿Desea cerrar sesión?")
        builder.setNegativeButton("Cancelar", null)
        builder.setPositiveButton("Aceptar", DialogInterface.OnClickListener(function = positiveButtonClick))
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun borrarDatos() {
        try {
            val nombreArchivo1 = "prefs_email.txt"
            val nombreArchivo2 = "prfs_provider.txt"
            val archivo1 = File(applicationContext.getExternalFilesDir(null), nombreArchivo1)
            val archivo2 = File(applicationContext.getExternalFilesDir(null), nombreArchivo2)
            archivo1.delete()
            archivo2.delete()
            Toast.makeText(this, "Archivos de sesión borrados.", Toast.LENGTH_SHORT).show()
        } catch (ex: Exception) {
            Log.e("TAG", "Error al escribir fichero a memoria interna")
        }
    }

    //Mostrar Imágen del Perfil
    private fun mostrarImagenPerfil(imgName: String) {
        //Acceder a Firebase Storage, se crea referencia a incidencias
        val storageRef = FirebaseStorage.getInstance().reference.child("/perfiles/$imgName")
        //Se crea el archivo temporal
        val localfile = File.createTempFile("tempImage","jpg")
        //Se obtiene el archivo
        storageRef.getFile(localfile).addOnCompleteListener{
            //Se pasa la imagen obtenida de Firebase Storage a una imagen Bitmap
            val Bitlocalfile = BitmapFactory.decodeFile(localfile.absolutePath)
            //Se asigna la imagen al ImageView
            imgView.setImageBitmap(Bitlocalfile)
        }.addOnFailureListener{ exception ->
            //Mensaje de error
            Toast.makeText(applicationContext, "Ha ocurrido un Error: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun comprobarImagen() {
        if (imageUri == Uri.EMPTY) {
            Toast.makeText(this, "Imágen no seleccionada.", Toast.LENGTH_SHORT).show()
            fileNameExist = null
        }

        if (fileNameExist == null) fileNameExist = fileName
    }

    //Cargar imágen a firebase storage
    private fun uploadImage(imgName: String){
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Cargando Imágen")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val storageRef = FirebaseStorage.getInstance().getReference("perfiles/$imgName")

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