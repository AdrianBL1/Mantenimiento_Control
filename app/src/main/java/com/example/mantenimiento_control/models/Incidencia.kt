package com.example.mantenimiento_control.models

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Incidencia(
    //Reporte Incidencia
    val datetimeReporte: String? = null,
    val usuarioReporte: String? = null,
    val rol: String? = null,
    val area: String? = null,
    val descripcion: String? = null,
    val imagenIncidencia: String? = null,

    //Estado Incidencia
    val estado: String? = null,
    val datetimeAtencion: String? = null,
    val usuarioAtencion: String? = null,
    val descripcionEstado: String? = null,
    val imagenEstadoIncidencia: String? = null,

    @Exclude val key: String? = null
)
