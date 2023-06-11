package com.example.mantenimiento_control

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.text.TextPaint
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.mantenimiento_control.models.Incidencia
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


class ExportReport : AppCompatActivity() {

    //Variables de atributos para el PDF
    lateinit var tituloText : String
    lateinit var descripcionText : String
    lateinit var img1Titulo : String
    lateinit var img2Titulo : String
    lateinit var piePagina : String

    lateinit var key : String

    //Variables de contenido para el PDF

        //Incidencia
        var pdf_datetime_incidencia : String = ""
        var pdf_user_report : String = ""
        var pdf_rol : String = ""
        var pdf_area : String = ""
        var pdf_descripcion : String = ""
        lateinit var pdf_imagen_incidencia : Bitmap
        var pdf_estado : String = ""
        var pdf_datetime_atencion : String = ""
        var pdf_usuario_atencion : String = ""
        var pdf_descripcion_estado : String = ""
        lateinit var pdf_imagen_estado_incidente : Bitmap

        //Usuario Report
        var nombreR : String = ""
        var direccionR : String = ""
        var telefonoR : String = ""
        var fotoUsuarioR : String = ""
        //Datos de trabajo
        var rolUserR : String = ""
        var horaEntradaR : String = ""
        var horaSalidaR : String = ""

        //Usuario Atencion
        var nombreA : String = ""
        var direccionA : String = ""
        var telefonoA : String = ""
        var fotoUsuarioA : String = ""
        //Datos de trabajo
        var rolUserA : String = ""
        var horaEntradaA : String = ""
        var horaSalidaA : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_export_report)

        val bundle = intent.extras
        key = bundle?.getString("key").toString()
        //emailReport = bundle?.getString("emailReport").toString()
        //emailAtencion = bundle?.getString("emailAtencion").toString()

        val keyView = findViewById<TextView>(R.id.key)
        keyView.setText("KEY: $key")

        val btn_generarPDF = findViewById<Button>(R.id.generarPDF)

        //TODO: NO FUNCIONA
        if (key.isEmpty()){
            btn_generarPDF.visibility = View.INVISIBLE
            showAlertNoKey()
        }

        obtenerDatosRealTimeDatabase(key)
        //obtenerDatosFirestore()

        if(checkPermission()) {
            Toast.makeText(this, "Permiso Aceptado", Toast.LENGTH_LONG).show()
        } else {
            requestPermissions()
        }

        btn_generarPDF.setOnClickListener(View.OnClickListener{
            generarPDF()
        })
    }

    private fun showAlertNoKey() {
        val builder = AlertDialog.Builder(this)

        builder.setTitle("AVISO")
        builder.setMessage("No existe una referencia con una incidencia seleccioanda, \n" +
                "Seleccione una incidencia primerio.")
        builder.setPositiveButton("Aceptar", null)

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun obtenerDatosRealTimeDatabase(key: String?) {

        val txt_incidencia = findViewById<TextView>(R.id.incidencia)
        val txt_atencion = findViewById<TextView>(R.id.atencion)
        val txt_estado = findViewById<TextView>(R.id.estado)
        val txt_user_report = findViewById<TextView>(R.id.user_report)
        val txt_user_atencion = findViewById<TextView>(R.id.user_atencion)

        val database = Firebase.database
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS") val ref =
            key?.let { database.getReference("incidencias").child(it) }

        ref?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val incidencia: Incidencia? = dataSnapshot.getValue(Incidencia::class.java)

                //Mostrar informacion
                if (incidencia != null) {
                    txt_incidencia.setText(incidencia.datetimeReporte.toString())
                    txt_atencion.setText(incidencia.datetimeAtencion.toString())
                    txt_estado.setText(incidencia.estado.toString())
                    txt_user_report.setText(incidencia.usuarioReporte.toString())
                    txt_user_atencion.setText(incidencia.usuarioAtencion.toString())

                    pdf_datetime_incidencia = incidencia.datetimeReporte.toString()
                    pdf_user_report = incidencia.usuarioReporte.toString()
                    pdf_rol = incidencia.rol.toString()
                    pdf_area = incidencia.area.toString()
                    pdf_descripcion = incidencia.descripcion.toString()
                    obtenerImagenIncidencia(incidencia.imagenIncidencia.toString())
                    pdf_estado = incidencia.estado.toString()
                    pdf_datetime_atencion = incidencia.datetimeAtencion.toString()
                    pdf_usuario_atencion = incidencia.usuarioAtencion.toString()
                    pdf_descripcion_estado = incidencia.descripcionEstado.toString()
                    obtenerImagenEstadoIncidencia(incidencia.imagenEstadoIncidencia.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    //Obtener Imágen del Incidente
    private fun obtenerImagenIncidencia(imgName: String) {
        //Acceder a Firebase Storage, se crea referencia a incidencias
        val storageRef = FirebaseStorage.getInstance().reference.child("/incidencias/$imgName")
        //Se crea el archivo temporal
        val localfile = File.createTempFile("tempImage","jpg")
        //Se obtiene el archivo
        storageRef.getFile(localfile).addOnCompleteListener{
            //Se pasa la imagen obtenida de Firebase Storage a una imagen Bitmap
            pdf_imagen_incidencia = BitmapFactory.decodeFile(localfile.absolutePath)
        }.addOnFailureListener{ exception ->
            //Mensaje de error
            Toast.makeText(applicationContext, "No se ha encontrado una imagen. Error: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    //Obtener Imágen del estado del Incidente
    private fun obtenerImagenEstadoIncidencia(imgName: String) {
        //Acceder a Firebase Storage, se crea referencia a incidencias_estados
        val storageRef = FirebaseStorage.getInstance().reference.child("/incidencias_estados/$imgName")
        //Se crea el archivo temporal
        val localfile = File.createTempFile("tempImage","jpg")
        //Se obtiene el archivo
        storageRef.getFile(localfile).addOnCompleteListener{
            //Se pasa la imagen obtenida de Firebase Storage a una imagen Bitmap
            pdf_imagen_estado_incidente = BitmapFactory.decodeFile(localfile.absolutePath)
        }.addOnFailureListener{ exception ->
            //Mensaje de error
            Toast.makeText(applicationContext, "No se ha encontrado una imagen. Error: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generarPDF() {

        //Fecha y Hora
        val dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm:ss a", Locale("es", "MX")))

        //Contenido del PDF

        tituloText = "REGISTRO DE INCIDENCIAS"

        img1Titulo = "Imágen del Incidente:"
        img2Titulo = "Imágen de la Atención dada:"

        piePagina = "Centro de Control de Mantenimiento del ITSM."

        descripcionText = "El siguiente reporte corresponde a los sucesos registrados en la incidencia. \n" +
                "Con la clave de identificación única: ${key}\n"+
                " \n"+
                "Incidencia generada en la fecha y horario registrada: $pdf_datetime_incidencia, \n" +
                "Por el usuario '$pdf_user_report' quien corresponde a: (nombre), \n" +
                "Atendida en la fecha y horario registrada: $pdf_datetime_atencion, \n" +
                "Por el usuario '$pdf_usuario_atencion' quien corresponde a: (nombre), \n" +
                " \n"+
                "La incidencia en cuestión se reportó en el area de $pdf_area, dirigida para el Rol de $pdf_rol. \n"+
                " \n"+
                "Descripción de la Incidencia: \n" +
                "$pdf_descripcion \n" +
                " \n"+
                "Descripción de la Atención dada a la Incidencia: \n" +
                "$pdf_descripcion_estado \n" +
                " \n"+
                "Esta incidencia finalizó como: $pdf_estado. \n"

        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titulo = TextPaint()
        val subtitulo = TextPaint()
        val descripcion = TextPaint()
        val pie_pagina = TextPaint()

        val paginaInfo = PdfDocument.PageInfo.Builder(816, 1054, 1).create()
        val pagina1 = pdfDocument.startPage(paginaInfo)

        val canvas = pagina1.canvas

        //Insertar imagen
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.mantenimiento)
        val bitmapEscala = Bitmap.createScaledBitmap(bitmap, 140,140, false)
        canvas.drawBitmap(bitmapEscala, 40f, 30f, paint)

        //Insertar titulo
        titulo.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
        titulo.textSize = 20f
        canvas.drawText(tituloText, 40f, 200f, titulo)

        //Insertar descripción
        descripcion.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL))
        descripcion.textSize = 14f

        val arrDescripcion = descripcionText.split("\n")

        var y = 250f
        for (item in arrDescripcion) {
            canvas.drawText(item, 40f, y, descripcion)
            y += 20
        }

        //Insertar titulos de Imagenes
        subtitulo.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD))
        subtitulo.textSize = 14f
        canvas.drawText(img1Titulo, 40f, 700f, subtitulo)
        canvas.drawText(img2Titulo, 450f, 700f, subtitulo)


        //Insertar Imágenes Incidencia
        val bitmapEscala2 = Bitmap.createScaledBitmap(pdf_imagen_incidencia, 140,140, false)
        canvas.drawBitmap(bitmapEscala2, 40f, 750f, paint)

        //Insertar Imágenes Solución
        val bitmapEscala3 = Bitmap.createScaledBitmap(pdf_imagen_estado_incidente, 140,140, false)
        canvas.drawBitmap(bitmapEscala3, 450f, 750f, paint)

        //Pie de Pagina
        pie_pagina.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC))
        pie_pagina.textSize = 14f
        canvas.drawText(piePagina, 40f, 1000f, pie_pagina)
        canvas.drawText("Reporte generado el: $dateTime ",40f,1020f,pie_pagina)


        //Fnalizar pagina
        pdfDocument.finishPage(pagina1)


        //Crear documento

        //Fecha y Hora
        val dateTimeExport = SimpleDateFormat("dd_MM_yyyy_HH_mm_ss", Locale.getDefault()).format(Date())
        //val file = File(Environment.getExternalStorageDirectory(), "Archivo.pdf")
        val file_name = "report_export_${dateTimeExport}.pdf"
        val file = File(applicationContext.getExternalFilesDir(null), file_name)

        try {
            //pdfDocument.writeTo(FileOutputStream(file))
            file.createNewFile()
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(this, "Se creo el PDF correctamente", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        pdfDocument.close()

        //openPDF(file_name)
    }

    private fun openPDF(file_name : String) {
        // Acceda a pdf desde el almacenamiento y use para Intent obtener opciones para ver la aplicación en las aplicaciones disponibles.
        val file = File(Environment.getExternalStorageDirectory(), file_name)
        Log.d("pdfFIle", "" + file)

        // Se obtiene la ubicación del archivo y el nombre del archivo.
        val uriPdfPath = FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", file)
        Log.d("pdfPath", "" + uriPdfPath)

        // Inicia Intent a la vista PDF desde las aplicaciones instaladas.
        val pdfOpenIntent = Intent(Intent.ACTION_VIEW)
        pdfOpenIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        pdfOpenIntent.clipData = ClipData.newRawUri("", uriPdfPath)
        pdfOpenIntent.setDataAndType(uriPdfPath, "application/pdf")
        pdfOpenIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

        try {
            startActivity(pdfOpenIntent)
        } catch (activityNotFoundException: ActivityNotFoundException) {
            Toast.makeText(this, "No hay ninguna aplicación para cargar el PDF correspondiente.", Toast.LENGTH_LONG)
                .show()
        }
    }

    // PERMISOS

    private fun checkPermission(): Boolean {
        val permission1 = ContextCompat.checkSelfPermission(applicationContext, WRITE_EXTERNAL_STORAGE)
        val permission2 = ContextCompat.checkSelfPermission(applicationContext, READ_EXTERNAL_STORAGE)
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE),
            200
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 200) {
            if(grantResults.size > 0) {
                val writeStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val readStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED

                if(writeStorage && readStorage) {
                    Toast.makeText(this, "Permisos concedidos", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Permisos rechazados", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }
}