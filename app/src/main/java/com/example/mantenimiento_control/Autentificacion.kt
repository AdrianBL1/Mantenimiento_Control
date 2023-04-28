package com.example.mantenimiento_control

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener


class Autentificacion : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        //SplashScreen
        val screenSplash = installSplashScreen()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_autentificacion)

        //Ejecución del SplashScreen
        screenSplash.setKeepOnScreenCondition{false}


        setup()
        session()
    }

    public override fun onStart() {
        super.onStart()

        val authLinearLayout = findViewById<LinearLayout>(R.id.authLinearLayout)
        authLinearLayout.visibility = View.VISIBLE
    }

    //Manejo de Sesiones
    private fun session() {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email = prefs.getString("email", null)
        val provider = prefs.getString("provider",null)

        val authLinearLayout = findViewById<LinearLayout>(R.id.authLinearLayout)

        //Si el email y el provider son diferentes de null, cargar el Panel Principal
        if (email != null && provider != null){
            authLinearLayout.visibility = View.INVISIBLE
            showMain(email, ProviderType.valueOf(provider))
        }
    }

    private fun setup() {
        val txt_correo = findViewById<EditText>(R.id.txt_correo)
        val txt_contrasena = findViewById<EditText>(R.id.txt_contrasena)
        val btn_ingresar = findViewById<Button>(R.id.btn_ingresar)
        val btn_registrar = findViewById<Button>(R.id.btn_registrar)
        val btn_forgotPassword = findViewById<Button>(R.id.btn_forgotPassword)

        title = "Autentificación"

        //Acción botón registrar
        btn_registrar.setOnClickListener {
            if (txt_correo.text.isNotEmpty() && txt_contrasena.text.isNotEmpty()){
                FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(txt_correo.text.toString(), txt_contrasena.text.toString())
                    .addOnCompleteListener {
                        if (it.isSuccessful){
                            showMain(it.result?.user?.email?:"", ProviderType.BASIC)
                        } else {
                            showAlert("")
                        }
                    }.addOnFailureListener { exception ->
                        showAlert(exception.toString())
                    }
            }
            else{
                comprobarFaltantes()
            }
        }

        //Acción botón ingresar
        btn_ingresar.setOnClickListener {
            if (txt_correo.text.isNotEmpty() && txt_contrasena.text.isNotEmpty()){
                FirebaseAuth.getInstance().signInWithEmailAndPassword(txt_correo.text.toString(), txt_contrasena.text.toString())
                    .addOnCompleteListener {
                        if (it.isSuccessful){
                            showMain(it.result?.user?.email?:"", ProviderType.BASIC)
                        } else {
                            showAlert("")
                        }
                    }.addOnFailureListener { exception ->
                        showAlert(exception.toString())
                    }
            } else {
                comprobarFaltantes()
            }
        }

        //Acción de recuperar contraseña
        btn_forgotPassword.setOnClickListener {
            //Asignacion de valores
            val builder = AlertDialog.Builder(this)
            val view = layoutInflater.inflate(R.layout.dialog_restaurar_contrasena, null)

            //Pasando la vista al Builder
            builder.setView(view)

            //Creando el dialog
            val dialog = builder.create()
            dialog.show()

            val exitbutton = view.findViewById<Button>(R.id.buttonCancel)
            exitbutton.setOnClickListener {
                dialog.dismiss()
            }

            val recuperarbutton = view.findViewById<Button>(R.id.buttonRecuperar)
            val txt_correo = view.findViewById<EditText>(R.id.txt_correo_recuperar)

            recuperarbutton.setOnClickListener {
                recuperarContrasena(txt_correo)
            }
        }
    }


    //Recuperar Contraseña
    private fun recuperarContrasena(correo : EditText) {

        val auth = FirebaseAuth.getInstance()

        if (correo.text.toString().isEmpty()){
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo.text.toString()).matches()){
            return
        }

        auth.sendPasswordResetEmail(correo.text.toString())
            .addOnCompleteListener{ task ->
                if (task.isSuccessful){
                    Toast.makeText(this, "Correo Enviado.", Toast.LENGTH_SHORT).show()
                }else if (task.isCanceled){
                    Toast.makeText(this, "No fue posible enviar el correo.", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "No fue posible enviar el correo. Error: ${exception}", Toast.LENGTH_SHORT).show()
            }
    }

    //Comprobar datos faltantes
    private fun comprobarFaltantes() {
        val txt_correo = findViewById<EditText>(R.id.txt_correo)
        val txt_contrasena = findViewById<EditText>(R.id.txt_contrasena)
        if(txt_correo.text.toString() == "")
            Toast.makeText(this, "Ingresa un correo.", Toast.LENGTH_SHORT).show()
        else if(txt_contrasena.text.toString() == "")
            Toast.makeText(this, "Ingresa una contraseña.", Toast.LENGTH_SHORT).show()
    }

    private fun showAlert(exception: String){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        if (exception == ""){
            builder.setMessage("Se ha producido un error de autentificación de usuario.")
        }else{
            builder.setMessage("Se ha producido un error de autentificación de usuario. Error: {$exception}")
        }
        builder.setPositiveButton("Aceptar",null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    //Cargar Panel Principal
    private fun showMain(email: String, provider: ProviderType){
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("email",email)
            putExtra("provider",provider)
        }
        startActivity(intent)
        finish()
    }
}