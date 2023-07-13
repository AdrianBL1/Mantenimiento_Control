package com.adrianbl.mantenimiento_control

import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.adrianbl.mantenimiento_control.models.Incidencia
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.io.File

enum class ProviderType{
    BASIC
}

class MainActivity : AppCompatActivity() {

    private val database = Firebase.database
    private val db = FirebaseFirestore.getInstance()
    private lateinit var messagesListener: ValueEventListener

    private val listaIncidencias: MutableList<Incidencia> = ArrayList()

    val ref = database.getReference("incidencias")
    //val currentUser = FirebaseAuth.getInstance().currentUser TODO: Pendiente de uso

    var listaUsuarios: List<String> = emptyList()
    var target: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Obtenemos los datos del activity anterior
        val bundle = intent.extras
        //Email
        val email = bundle?.getString("email")
        //Provider 'de ser necesario'
        val provider = bundle?.getString("provider")

        val incidenciasRecyclerView = findViewById<RecyclerView>(R.id.incidenciasRecyclerView)

        //En caso de existir la excension de no poder obtener el email
        //if(email == ""){
        //    if (currentUser != null) {
        //        email = currentUser.email.toString()
        //    }
        //}

        setup(email = email?:"", provider = provider?:"")

        //Guardando datos -> Sesión
        //TODO: COMENTAMOS ESTA SECCION TEMPORALMENTE
        //val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        //prefs.putString("email", "$email")
        //prefs.putString("provider", "$provider")
        //prefs.apply()
        guardarArchivo(email?:"No se pudo obtener el email", provider = provider?:"No se pudo obtener el proveedor")

        //Limpiando la lista de Incidencias
        listaIncidencias.clear()

        if (!checkInternet()){
            val intent = Intent(this, WithoutConnection::class.java).apply {
                putExtra("email",email)
                putExtra("provider",provider)
            }
            startActivity(intent)
            finish()
        } else{
            //Carga el reciclerview
            val btn_registrar_incidencia = findViewById<Button>(R.id.btn_registrar_incidencia)
            val btn_fab = findViewById<FloatingActionButton>(R.id.fab)
            val btn_fab2 = findViewById<FloatingActionButton>(R.id.fab2)
            target = email
            //Se ejecuta el RecyclerView que contiene las Incidencias
            db.collection("usuarios")
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        Log.d(TAG, "${document.id} => ${document.data}")
                        listaUsuarios += listOf(document.id)
                        val found = target in listaUsuarios
                        if (found){
                            db.collection("usuarios").document(email.toString()).get().addOnSuccessListener {
                                if ((it.get("rol") as String?) != "Rol no asignado"){
                                    btn_registrar_incidencia.setEnabled(true)
                                    btn_fab.setEnabled(true)
                                    btn_fab2.setEnabled(true)
                                    setupRecyclerView(incidenciasRecyclerView)
                                }else{
                                    showAlertRol()
                                }
                            }
                        }else{
                            Toast.makeText(this,"Necesita registrar sus Datos Primero", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)

        return (capabilities != null && capabilities.hasCapability(NET_CAPABILITY_INTERNET))

    }

    private fun checkInternet() : Boolean {
        if (isNetworkAvailable()) {
            return true
        }else{
            return false
        }
    }

    private fun showAlertRol() {
        val builder = AlertDialog.Builder(this)

        builder.setTitle("AVISO")
        builder.setMessage("Aún no le han asignado un Rol \n Hasta no tener uno no podrá acceder a las funciones.")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun setup(email: String, provider: String) {
        val text_correo = findViewById<TextView>(R.id.textCorreo)
        val text_provider = findViewById<TextView>(R.id.textProvider)
        val btn_perfil = findViewById<Button>(R.id.btn_perfil)
        val btn_registrar_incidencia = findViewById<Button>(R.id.btn_registrar_incidencia)
        val btn_fab = findViewById<FloatingActionButton>(R.id.fab)
        val btn_fab2 = findViewById<FloatingActionButton>(R.id.fab2)

        // Datos de Inicio
        title = "Panel Principal"
        text_correo.text = email
        text_provider.text = provider

        //Config inicial
        btn_registrar_incidencia.setEnabled(false)
        btn_fab.setEnabled(false)
        btn_fab2.setEnabled(false)

        //Obtener Rol
        /* TODO: ERROR AL CARGAR LOS DATOS DEL ROL (EN REVISIÓN)
        // ERROR: NO HAY PERSISTENCIA EN SUS PARAMETROS Y AL TRATAR DE INGRESAR DE NUEVO A LA VISTA SURGE UN ERROR POR NO OBTENER LOS DATOS
        db.collection("usuarios").document(email?:"").get().addOnSuccessListener {
                documentSnapshot ->
            if (documentSnapshot.exists()){
                val rol = documentSnapshot.getString("rol")
                text_provider.text = rol
            }else{
                Toast.makeText(this, "No se pudo recuperar el rol correspondiente.", Toast.LENGTH_SHORT).show()
            }
            // RESPALDO: text_provider.text = it.get("rol") as String?
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "No se pudo recuperar el rol correspondiente. Error: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
         */

        //Acceder al Perfil de Usuario
        btn_perfil.setOnClickListener {
            val intent = Intent(this, PerfilUsuario::class.java).apply {
                putExtra("email",email)
            }
            startActivity(intent)
        }

        //Acceder a la vista para registrar una Incidencia
        btn_registrar_incidencia.setOnClickListener{
            val intent = Intent(this, RealizarReporteIncidente::class.java).apply {
                putExtra("email", email)
            }
            startActivity(intent)
        }

        //Acceder a Filtros del botón en el FloatingActionButton
        btn_fab.setOnClickListener {
            //Asignacion de valores
            val builder = AlertDialog.Builder(this)
            val view = layoutInflater.inflate(R.layout.dialog_filtro, null)

            //Pasando la vista al Builder
            builder.setView(view)

            //Creando el dialog
            val dialog = builder.create()
            dialog.show()

        }

        btn_fab2.setOnClickListener {
            val intent = Intent(this, Estadisticas::class.java)
            startActivity(intent)
        }
    }

    //Guardado de archivos
    fun guardarArchivo(email: String, provider: String) {
        try {
            val nombreArchivo1 = "prefs_email.txt"
            val nombreArchivo2 = "prfs_provider.txt"
            val archivo1 = File(applicationContext.getExternalFilesDir(null), nombreArchivo1)
            val archivo2 = File(applicationContext.getExternalFilesDir(null), nombreArchivo2)
            archivo1.createNewFile()
            archivo1.writeText(email)
            archivo2.createNewFile()
            archivo2.writeText(provider)
            Toast.makeText(this, "Archivos creados.", Toast.LENGTH_SHORT).show()
        } catch (ex: Exception) {
            Log.e("TAG", "Error al escribir fichero a memoria interna")
        }
    }

    //Preparando el recycler view
    private fun setupRecyclerView(recyclerView: RecyclerView) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Cargando Contenido")
        progressDialog.setCancelable(false)
        progressDialog.show()

        messagesListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listaIncidencias.clear()

                if (listaIncidencias != null){
                    if (progressDialog.isShowing) progressDialog.dismiss()
                }

                dataSnapshot.children.forEach { child ->
                    val incidendia: Incidencia? =
                        Incidencia(
                            child.child("datetimeReporte").getValue<String>(),
                            child.child("usuarioReporte").getValue<String>(),
                            child.child("rol").getValue<String>(),
                            child.child("area").getValue<String>(),
                            child.child("descripcion").getValue<String>(),
                            child.child("imagenIncidencia").getValue<String>(),
                            child.child("estado").getValue<String>(),
                            child.child("datetimeAtencion").getValue<String>(),
                            child.child("usuarioAtencion").getValue<String>(),
                            child.child("descripcionEstado").getValue<String>(),
                            child.child("imagenEstadoIncidencia").getValue<String>(),
                            child.key,
                        )
                    incidendia?.let { listaIncidencias.add(it) }

                    //Orden de la lista (Desendente en base a la fecha del reportde de la incidencia)
                    listaIncidencias.sortByDescending {
                        it.datetimeReporte
                    }
                }
                recyclerView.adapter = IncidenciaViewAdapter(listaIncidencias)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TAG", "messages:onCancelled: ${error.message}")
            }
        }

        ref.addValueEventListener(messagesListener)

        deleteSwipe(recyclerView)
    }

    //Adaptador
    class IncidenciaViewAdapter(private val values: List<Incidencia>) :
        RecyclerView.Adapter<IncidenciaViewAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.incidente_contenido, parent, false)
            return ViewHolder(view)
        }

        //Creacion de elemento de la lista
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val incidencia = values[position]
            holder.txtArea.text = incidencia.area
            holder.txtDateTime.text = incidencia.datetimeReporte
            holder.txtEstado.text = incidencia.estado

            //Seleccion de un elemento + paso de parametro Key
            holder.itemView.setOnClickListener { v ->
                val intent = Intent(v.context, ReporteIncidente::class.java).apply {
                    putExtra("key", incidencia.key)
                }
                v.context.startActivity(intent)
            }

            //Configurado del Color por Estado
            if(incidencia.estado == "Solucionado"){
                holder.txtEstado.setBackgroundColor(Color.GREEN)
            }else if (incidencia.estado == "No Solucionado"){
                holder.txtEstado.setBackgroundColor(Color.RED)
            }else if (incidencia.estado == "En revisión" || incidencia.estado == "Indefinido"){
                holder.txtEstado.setBackgroundColor(Color.GRAY)
            }

            //Configurando imagen
            if (incidencia.area == "Biblioteca") holder.img?.setImageResource(R.drawable.a_biblioteca)
            else if (incidencia.area == "Campo") holder.img?.setImageResource(R.drawable.a_campo)
            else if (incidencia.area == "Centro de Computo") holder.img?.setImageResource(R.drawable.a_c_computo)
            else if (incidencia.area == "Edificio de Investigadores") holder.img?.setImageResource(R.drawable.a_e_investigadores)
            else if (incidencia.area == "Edificio Principal") holder.img?.setImageResource(R.drawable.a_e_principal)
            else if (incidencia.area == "Edificio H") holder.img?.setImageResource(R.drawable.a_e_h)
            else if (incidencia.area == "Estacionamiento") holder.img?.setImageResource(R.drawable.a_estacionamiento)
            else if (incidencia.area == "Explanada") holder.img?.setImageResource(R.drawable.a_explanada)
            else if (incidencia.area == "Humedales") holder.img?.setImageResource(R.drawable.a_humedales)
            else if (incidencia.area == "Jardines") holder.img?.setImageResource(R.drawable.a_jardines)
            else if (incidencia.area == "Nave Industrial") holder.img?.setImageResource(R.drawable.a_nave_industrial)
            else if (incidencia.area == "Lab. de Ambiental") holder.img?.setImageResource(R.drawable.a_l_ambiental)
            else if (incidencia.area == "Lab. de Bioquímica") holder.img?.setImageResource(R.drawable.a_l_bioquimica)
        }

        //Obteniendo el coteo de los valores
        override fun getItemCount() = values.size

        //Asignando valores
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val txtArea: TextView = view.findViewById(R.id.txtArea)
            val txtDateTime: TextView = view.findViewById(R.id.txtDateTime)
            val txtEstado: TextView = view.findViewById(R.id.txtEstado)
            val img: ImageView? = view.findViewById(R.id.posterImageView)
        }
    }

    private fun deleteSwipe(recyclerView: RecyclerView){
        val touchHelperCallback: ItemTouchHelper.SimpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                val builder = AlertDialog.Builder(this@MainActivity)
                val positiveButtonClick = { dialog: DialogInterface, which: Int ->

                    listaIncidencias.get(viewHolder.adapterPosition).key?.let { ref.child(it).setValue(null) }
                    listaIncidencias.removeAt(viewHolder.adapterPosition)
                    recyclerView.adapter?.notifyItemRemoved(viewHolder.adapterPosition)
                    recyclerView.adapter?.notifyDataSetChanged()

                    Toast.makeText(this@MainActivity, "Incidencia borrada.", Toast.LENGTH_SHORT).show()
                }

                builder.setTitle("AVISO")
                builder.setMessage("¿Desea borrar ésta incidencia?")
                builder.setNegativeButton("Cancelar", null)
                builder.setPositiveButton("Aceptar", DialogInterface.OnClickListener(function = positiveButtonClick))
                val dialog: AlertDialog = builder.create()
                dialog.show()
            }
        }
        val itemTouchHelper = ItemTouchHelper(touchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
}