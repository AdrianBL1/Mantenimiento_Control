# <div style="display:flex; align-items: center;"><img src="docs/mantenimiento.png" width="50px">Control de Mantenimiento </div>

Fecha de Desarrollo: Julio - 2023

## Descripción

Aplicación para la gestión de incidencias del área de Mantenimiento, permitiendo a distintas áreas registrar incidentes y generar reportes en base a los incidentes registrados y atendidos.

## Operación 

Cada usuario o empleado de cualquier area o departamento puede levantar un incidente correspondiente a una ubicación en la que puede describir el incidente sucedio, anexar evidencia del problema y notificar al area o departamento correspondiente para que atienda la situación.

El area o personal que tiene como responsabilidad atender el incidente anexa el estado del incidente y evidencia escrita y/o opcionalemte evidencia fotografica.

El incidente atendido y marcado con el estado de "Solucionado", se puede extraer un reporte de lo sucedido donde se incluye toda la evidencia de lo suceso, incluyendo hora y fecha, nombre de la persona quien atendió el incidente, la evidencia escrita y evidencia fotográfica.

## Contenido

Modelos:
* 🟩 Incidencia
* 🟩 Usuario

Clases:
* 🟦 Autentificacion
* 🟦 Estadisticas
* 🟦 ExportReport (Exportar Reporte)
* 🟦 MainActivity (Panel Principal)
* 🟦 PerfilAdministrador
* 🟦 PerfilUsuario
* 🟦 RealizarReporteIncidencte
* 🟦 RegistrarReporteIncidente
* 🟦 ReporteIncidente
* 🟦 WithoutConnection (Sin Conexión)

Fragments:
* 🟨 TimePickerFragment

## Programación

* Kotlin

<div align="center">
    <a href="https://www.android.com/intl/en_in/" target="_blank"><img style="margin: 10px" src="https://profilinator.rishav.dev/skills-assets/android-original-wordmark.svg" alt="Android" height="50" /></a>
    <a href="https://kotlinlang.org/" target="_blank"><img style="margin: 10px" src="https://profilinator.rishav.dev/skills-assets/kotlinlang-icon.svg" alt="Kotlin" height="50" /></a>
</div>

## Firebase (BackEnd)

<div align="center">
    <a href="https://firebase.google.com/" target="_blank"><img style="margin: 10px" src="https://profilinator.rishav.dev/skills-assets/firebase.png" alt="Firebase" height="50" /></a>  
</div>

Del lado del BackEnd se usó Firebase para implementar:

* 🔐 Autentificación

* 👥 Acceso por Correo y Contraseña

* 🔄️ Restablecer Acceso

* 📓 Firebase Database

* 📊 Firebase Firestore

* ☁️ Firebase Cloud Storage


## Arquitectura

* MVVM

## Vistas

### Autentificación:

<div align="center">
    <img src="docs/autentificacion.png" height="500px" alt="Autentificación">
</div>

Se ingraesa con un *Usuario y Contraseña*, con la posibilidad de restaurar el acceso en caso de olvidarse la contraseña de acceso.

### Panel Principal:

<div align="center">
    <img src="docs/panel_principal.png" height="600px" alt="Panel Principal">
</div>

Se observa la interfaz principal donde se observa el despliegue de incidencias y el estado de estas.

Las incidencias presentes pueden variar dependiendo el usuario y es necesario completar el perfil del usuario para poder tener acceso y vista de las incidencias asi como para poder registrarlas y atendenderlas.
Se muestra la vista principal sin incidencias:

<div align="center">
    <img src="docs/panel_principal_no_incidencias.png" height="600px" alt="Panel Principal">
</div>

### Sin conexión

En caso de no contar con conexión a internet o esta verse interrumpida se despliega la siguiente vista:

<div align="center">
    <img src="docs/sin_conexion.png" height="600px" alt="Sin Conexión">
</div>

### Perfil de Usuario

En la vista de perfil de usuario es necesario completar el perfil para desbloquear las funciones disponibles.

Los roles son asigandos por el Administrador.

<div align="center">
    <img src="docs/perfil_usuario.png" height="600px" alt="Perfil Usuario">
</div>

### Perfil de Administrador

El administrador tiene el privilegio se asignar los roles a los demas usuarios registrados en la Aplicación.

<div align="center">
    <img src="docs/perfil_admin.png" height="600px" alt="Perfil Administración">
</div>

Consulta de Usuarios Registrados:

El administrador de igual forma puede consultar los datos de los usuarios y modificarlos.

<div align="center">
    <img src="docs/perfil_admin_usuarios.png" height="600px" alt="Perfil Administración">
</div>

### Uso del TimePickerFragment

Entre los parémetros de los usuarios está el horario laboral de estos y para ello se hace uso del Fragment de TimePicker para asigar los horarios.

<div align="center">
    <img src="docs/timepicker.png" height="600px" alt="Uso de TimePickerFragment">
</div>

### Realizar el reporte de una Incidencia

Se muestra la interfaz para realizar las incidencias.

Se solicitan datos como el área quien le corresponde atender el incidente, el área donde se suscita, descripción del incidente y una foto (opcional) de evidencia.

<div align="center">
    <img src="docs/incidencia_reporte1.png" height="600px" alt="Reporte de Incidencia">
</div>

Datos llenos para realizar un reporte:

<div align="center">
    <img src="docs/incidencia_reporte2.png" height="600px" alt="Reporte de Incidencia">
</div>

Confirmación:

<div align="center">
    <img src="docs/incidencia_reporte_confirmacion.png" height="600px" alt="Confirmación del Reporte">
</div>

Detalles de la Incidencia enviada:

<div align="center">
    <img src="docs/incidencia_reporte_detalles.png" height="600px" alt="Detalles del Reporte de Incidencia">
</div>

### Registrar estado de una Incidencia:

Para el registro del estado se solicitan datos como el estado que entre las opciones está (Solucionado, pendiente, en revisión y no solucionado), una descripción de lo que se hizo para atender el incidente y una foto del incidente (opcional) para anexar.

Registro del estado de la Incidencia:

<div align="center">
    <img src="docs/incidencia_estado.png" height="600px" alt="Registro del estado de la Incidencia">
</div>

Esta operación se realiza despues de haber atendido el incidente.

Confirmación del estado de la Incidencia:

<div align="center">
    <img src="docs/incidencia_estado_confirmacion.png" height="600px" alt="Confirmación del estado de la Incidencia">
</div>

Detalles del estado de la Incidencia enviada:

<div align="center">
    <img src="docs/incidencia_estado_detalles.png" height="600px" alt="Detalles del estado de la Incidencia">
</div>


### Incidencia atendida:

Vista de la Incidencia atendida:

<div align="center">
    <img src="docs/incidencia_atendida.png" height="600px" alt="Vista de la Incidencia atendida">
</div>

Datos del Usuario que registró la Incidencia:

<div align="center">
    <img src="docs/incidencia_atendida_usuario.png" height="600px" alt="Datos del Usuario de la Incidencia atendida">
</div>


### Generar Reportes:

Vista para la Exportación de Incidencias completadas:

<div align="center">
    <img src="docs/exportacion.png" height="600px" alt="Exportación">
</div>

Formato del PDF generado de una Incidencia completada:

El documento exportado se alamacena en el dispositivo movil y contiene todos los datos de la incidencia desde que se reporta hasta que se atiende y los datos correspondientes. Para este caso algunos datos son omitidos.

<div align="center">
    <img src="docs/exportacion_pdf.png" height="600px" alt="PDF Exportado">
</div>

### Estadisticas (Desarrollo trunco):

El objetivo de las estadisticas es la de mostrar la cantidad de incidencias y un registro de los tipos de incidencias desplegados y de las incidencias atenidas representado con gráficos.

*Esta una función que no se terminó de implementar* :(

Vista de las Estadísticas #1:

<div align="center">
    <img src="docs/estadisticas1.jpg" height="600px" alt="Estadísticas #1">
</div>

Vista de las Estadísticas #2:

<div align="center">
    <img src="docs/estadisticas2.jpg" height="600px" alt="Estadísticas #2">
</div>

---

<div align="center">

La aplicación se desarrolló con el objetivo de brindar una herramienta para la gestión de incidencias para el área de Mantenimiento en una dependencia.

ℹ️ Este proyecto se realizó con fines académicos.

</div>