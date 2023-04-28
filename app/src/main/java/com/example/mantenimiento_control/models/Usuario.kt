package com.example.mantenimiento_control.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Usuario(
    //Datos básicos
    val nombre: String? = null,
    val direccion: String? = null,
    val telefono: String? = null,
    val fotoUsuario: String? = null,

    //Datos de trabajo
    val rol: String? = null,
    val horaEntrada: String? = null,
    val horaSalida: String? = null,
)