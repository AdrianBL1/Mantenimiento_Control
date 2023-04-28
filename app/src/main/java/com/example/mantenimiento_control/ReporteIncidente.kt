package com.example.mantenimiento_control

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.mantenimiento_control.models.Incidencia
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class ReporteIncidente : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    lateinit var emailReport : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reporte_incidente)

        val emailTransfer = intent.getStringExtra("email")

        //Card View 1
        val txtdatetime = findViewById<TextView>(R.id.report_datetime)
        val report_usuario = findViewById<TextView>(R.id.report_usuario)
        val txtrol = findViewById<TextView>(R.id.report_rol)
        val txt_desc = findViewById<TextView>(R.id.report_descripcion_incidente)
        val txt_area = findViewById<TextView>(R.id.report_area)
        val img_inci = findViewById<ImageView>(R.id.imageViewIncidente)

        //Card View 2
        val atenciondatetime = findViewById<TextView>(R.id.report_atencion_datetime)
        val atencion_usuario = findViewById<TextView>(R.id.atencion_usuario)
        val estado = findViewById<TextView>(R.id.estadoIncidente)
        val atencion_rol = findViewById<TextView>(R.id.atencion_rol)
        val descripcion_reporte = findViewById<TextView>(R.id.report_descripcion_estado_incidente)
        val img_estado_inci = findViewById<ImageView>(R.id.imageViewEstadoIncidente)

        val btn_accion = findViewById<Button>(R.id.btn_registrar_accion)

        val key = intent.getStringExtra("key")
        val database = Firebase.database
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS") val ref =
            key?.let { database.getReference("incidencias").child(it) }

        ref?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val incidencia: Incidencia? = dataSnapshot.getValue(Incidencia::class.java)

                //Mostrar toda la informacion en los CardView
                if (incidencia != null) {
                    //CardView 1
                    txtdatetime.setText(incidencia.datetimeReporte.toString())
                    report_usuario.setText(incidencia.usuarioReporte.toString())
                    emailReport = incidencia.usuarioReporte.toString()
                    txtrol.setText(incidencia.rol.toString())
                    txt_desc.setText(incidencia.descripcion.toString())
                    txt_area.setText(incidencia.area.toString())
                    mostrarImagenIncidencia(incidencia.imagenIncidencia.toString())

                    //CardView 2
                    estado.setText(incidencia?.estado.toString())
                    atenciondatetime.setText(incidencia.datetimeAtencion.toString())
                    atencion_usuario.setText(incidencia.usuarioAtencion.toString())
                    atencion_rol.setText(incidencia?.rol.toString())
                    descripcion_reporte.setText(incidencia?.descripcionEstado.toString())
                    mostrarImagenEstadoIncidencia(incidencia.imagenEstadoIncidencia.toString())
                }

                //Configurado del Color por Estado
                if(incidencia?.estado == "Solucionado"){
                    estado.setBackgroundColor(Color.GREEN)
                }else if (incidencia?.estado == "No Solucionado"){
                    estado.setBackgroundColor(Color.RED)
                }else if (incidencia?.estado == "En revisión" || incidencia?.estado == "Indefinido"){
                    estado.setBackgroundColor(Color.GRAY)
                }

                //Bloqueo del boton de registro de accion
                if (incidencia?.estado == "Solucionado"){
                    btn_accion.setOnClickListener {
                        showAlert()
                    }
                }
            }

            //Mostrar Imágen del Incidente
            private fun mostrarImagenIncidencia(imgName: String) {
                //Acceder a Firebase Storage, se crea referencia a incidencias
                val storageRef = FirebaseStorage.getInstance().reference.child("/incidencias/$imgName")
                //Se crea el archivo temporal
                val localfile = File.createTempFile("tempImage","jpg")
                //Se obtiene el archivo
                storageRef.getFile(localfile).addOnCompleteListener{
                    //Se pasa la imagen obtenida de Firebase Storage a una imagen Bitmap
                    val Bitlocalfile = BitmapFactory.decodeFile(localfile.absolutePath)
                    //Se asigna la imagen al ImageView
                    img_inci.setImageBitmap(Bitlocalfile)
                }.addOnFailureListener{ exception ->
                    //Mensaje de error
                    Toast.makeText(applicationContext, "Ha ocurrido un Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }

            //Mostrar Imágen del estado del Incidente
            private fun mostrarImagenEstadoIncidencia(imgName: String) {
                //Acceder a Firebase Storage, se crea referencia a incidencias_estados
                val storageRef = FirebaseStorage.getInstance().reference.child("/incidencias_estados/$imgName")
                //Se crea el archivo temporal
                val localfile = File.createTempFile("tempImage","jpg")
                //Se obtiene el archivo
                storageRef.getFile(localfile).addOnCompleteListener{
                    //Se pasa la imagen obtenida de Firebase Storage a una imagen Bitmap
                    val Bitlocalfile = BitmapFactory.decodeFile(localfile.absolutePath)
                    //Se asigna la imagen al ImageView
                    img_estado_inci.setImageBitmap(Bitlocalfile)
                }.addOnFailureListener{ exception ->
                    //Mensaje de error
                    Toast.makeText(applicationContext, "Ha ocurrido un Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("TAG", "Failed to read value.", error.toException())
            }
        })

        //Botón para registrar el estado del Incidente
        btn_accion.setOnClickListener {
            val intent = Intent(this, RegistrarEstadoIncidente::class.java).apply {
                putExtra("key", key)
                putExtra("emailTransfer",emailTransfer)
            }
            startActivity(intent)
        }

        //Clic a nombre del usuario
        report_usuario.setOnClickListener {
            //Asignacion de valores
            val builder = AlertDialog.Builder(this)
            val view = layoutInflater.inflate(R.layout.dialog_datos_usuario, null)

            //Pasando la vista al Builder
            builder.setView(view)

            //Creando el dialog
            val dialog = builder.create()
            dialog.show()

            //Tomando los atributos dentro del dialog
            val nameTextView = view.findViewById<TextView>(R.id.nameTextView)
            val correoTextView = view.findViewById<TextView>(R.id.correoTextView)
            val rolTextView = view.findViewById<TextView>(R.id.rolTextView)

            val horaEntrada = view.findViewById<TextView>(R.id.horarioEntradaTextView)
            val horaSalida = view.findViewById<TextView>(R.id.horarioSalidaTextView)

            val exitbutton = view.findViewById<Button>(R.id.exitbutton)

            //Obteniendo los datos de firebase del usuario (email) seleccionado
            db.collection("usuarios").document(emailReport).get().addOnSuccessListener {
                nameTextView.setText(it.get("nombre") as String?)
                correoTextView.text = emailReport
                rolTextView.setText(it.get("rol") as String?)

                if (it.get("horaEntrada") == null){
                    horaEntrada.setText("Horario Entrada: (Horario no establecido)")
                }else{
                    horaEntrada.setText("Horario Entrada: ${it.get("horaEntrada")}")
                }
                if(it.get("horaSalida") == null){
                    horaSalida.setText("Horario Salida: (Horario no establecido)")
                }else{
                    horaSalida.setText("Horario Salida: ${it.get("horaSalida")}")
                }
            }

            exitbutton.setOnClickListener {
                dialog.dismiss()
            }
        }
    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(this)

        builder.setTitle("AVISO")
        builder.setMessage("Esta incidencia a sigo marcada como: \n Solucionado.")
        builder.setPositiveButton("Aceptar", null)

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}