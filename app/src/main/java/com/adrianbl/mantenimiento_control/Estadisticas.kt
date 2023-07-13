package com.adrianbl.mantenimiento_control

import android.content.ContentValues.TAG
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class Estadisticas : AppCompatActivity() {

    lateinit var totalIncidencias : TextView
    lateinit var incidenciasSolucionadas : TextView
    lateinit var totalUsuarios : TextView
    lateinit var totalRoles : TextView

    var solucionado : Float= 0.0f
    var no_solucionado : Float = 0.0f
    var pendiente : Float = 0.0f
    var en_revision : Float = 0.0f
    var indefinido : Float = 0.0f

    var arreglo : ArrayList <Float> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_estadisticas)

        setupBarChart()
        setupPieChart()
    }

    private fun obtenerValores(callback: List<(Float) -> Unit>) {
        val dbFirestore = FirebaseFirestore.getInstance()
        val query = dbFirestore.collection("usuarios")
        val database = Firebase.database

        totalIncidencias = findViewById(R.id.totalIncidencias)
        incidenciasSolucionadas = findViewById(R.id.incidenciasSolucionadas)
        totalUsuarios = findViewById(R.id.totalUsuarios)
        totalRoles = findViewById(R.id.totalRoles)

        val countQuery = query.count()
        countQuery.get(AggregateSource.SERVER).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val snapshot = task.result
                Log.d(TAG, "Conteo total de usuarios: ${snapshot.count}")
                totalUsuarios.text = snapshot.count.toString()
            } else {
                Log.d(TAG, "Conteo fallido: ", task.getException())
            }
        }

        database.getReference("incidencias")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val numHijos = dataSnapshot.childrenCount
                    Log.d(TAG, "Conteo total de incidencias: $numHijos")
                    totalIncidencias.text = numHijos.toString()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    println("Fallo la lectura: " + databaseError.code)
                }
            })

        val estados = resources.getStringArray(R.array.estados).toList()

        //Obtener datos asignados al BarChart

        database.getReference("incidencias").orderByChild("estado").
        equalTo(estados[0])
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val count : Int = dataSnapshot.childrenCount.toInt()
                    println("Cantidad de veces que '${estados[0]}' se repite: $count")
                    incidenciasSolucionadas.text = count.toString()
                    println("VALOR ANTES DE LA INSERCION: $count")
                    callback[0](count.toFloat())
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    println("Fallo la lectura: " + databaseError.code)
                }
            })

        database.getReference("incidencias").orderByChild("estado").
        equalTo(estados[1])
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val count : Int = dataSnapshot.childrenCount.toInt()
                    println("Cantidad de veces que '${estados[1]}' se repite: $count")
                    println("VALOR ANTES DE LA INSERCION: $count")
                    callback[1](count.toFloat())
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    println("Fallo la lectura: " + databaseError.code)
                }
            })

        database.getReference("incidencias").orderByChild("estado").
        equalTo(estados[2])
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val count : Int = dataSnapshot.childrenCount.toInt()
                    println("Cantidad de veces que '${estados[2]}' se repite: $count")
                    println("VALOR ANTES DE LA INSERCION: $count")
                    callback[2](count.toFloat())
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    println("Fallo la lectura: " + databaseError.code)
                }
            })

        database.getReference("incidencias").orderByChild("estado").
        equalTo(estados[3])
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val count : Int = dataSnapshot.childrenCount.toInt()
                    println("Cantidad de veces que '${estados[3]}' se repite: $count")
                    println("VALOR ANTES DE LA INSERCION: $count")
                    callback[3](count.toFloat())
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    println("Fallo la lectura: " + databaseError.code)
                }
            })

        database.getReference("incidencias").orderByChild("estado").
        equalTo(estados[4])
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val count : Int = dataSnapshot.childrenCount.toInt()
                    println("Cantidad de veces que '${estados[4]}' se repite: $count")
                    println("VALOR ANTES DE LA INSERCION: $count")
                    callback[4](count.toFloat())
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    println("Fallo la lectura: " + databaseError.code)
                }
            })

        //Conteo de Roles
        val listaRoles = resources.getStringArray(R.array.roles)
        totalRoles.text = listaRoles.count().toString()

    }

    private fun setupBarChart() {

        //val estado = Est_Estados()
        //println("Dato recibido ${estado.solucionado}")

        val valoresObtenidos: MutableList<Float> = mutableListOf()

        obtenerValores(listOf({ solucionado ->
            println("Dato recibido solucionado: $solucionado")
            arreglo[0] = solucionado
        }, { no_solucionado ->
            println("Dato recibido no solucionado: $no_solucionado")
            arreglo[1] = no_solucionado
        }, { pendiente ->
            println("Dato recibido pendiente: $pendiente")
            arreglo[2] = pendiente
        }, { en_revision ->
            println("Dato recibido en revisión: $en_revision")
            arreglo[3] = en_revision
        }, { indefinido ->
            println("Dato recibido indefinido: $indefinido")
            arreglo[4] = indefinido
        }))
        // Llama a obtenerValores() y utiliza más callbacks para obtener los demás valores

        val barChart = findViewById<BarChart>(R.id.bar_chart)

        val list: ArrayList<BarEntry> = ArrayList()

        /*
        for ((index, valor) in valoresObtenidos.withIndex()) {
            val xValue = (index + 1).toFloat()
            val entry = BarEntry(xValue, valor)
            list.add(entry)
        }
         */

        list.add(BarEntry(1f,arreglo[1])) //Solucionado
        list.add(BarEntry(2f,arreglo[1])) //No Solucionado
        list.add(BarEntry(3f,arreglo[2])) //Pendiente
        list.add(BarEntry(4f,arreglo[3])) //En revisión
        list.add(BarEntry(5f,arreglo[4])) //Indefinido

        val barDataSet = BarDataSet(list,"Estados de Incidencias")
        println("Lista: $list")

        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS,255)
        barDataSet.valueTextColor = Color.BLACK

        val barData = BarData(barDataSet)

        barChart.setFitBars(true)

        // Carga data al BarChar
        barChart.data = barData

        // Descripcion del barChar
        barChart.description.text = "Gráfico de barras de estados de Incidencias."

        // Config. animación
        barChart.animateY(2000)

        val l = barChart.legend
        l.formSize = 10f // Establece el tamaño de las formas/formas de la leyenda
        l.form = Legend.LegendForm.CIRCLE // Establece qué tipo de formulario/forma se debe utilizar
        l.textSize = 7f
        l.textColor = Color.BLACK
        l.xEntrySpace = 5f // Establece el espacio entre las entradas de la leyenda en el eje x
        l.yEntrySpace = 5f // Establece el espacio entre las entradas de la leyenda en el eje y

        // Etiquetas y colores personalizados
        val leyendas: ArrayList<LegendEntry> = ArrayList()
        leyendas.add(LegendEntry("Solucionado",Legend.LegendForm.CIRCLE,10f,2f,null,Color.YELLOW))
        leyendas.add(LegendEntry("No Solucionado",Legend.LegendForm.CIRCLE,10f,2f,null,Color.YELLOW))
        leyendas.add(LegendEntry("Pendiente",Legend.LegendForm.CIRCLE,10f,2f,null,Color.YELLOW))
        leyendas.add(LegendEntry("En revisión",Legend.LegendForm.CIRCLE,10f,2f,null,Color.YELLOW))
        leyendas.add(LegendEntry("Indefinido",Legend.LegendForm.CIRCLE,10f,2f,null,Color.YELLOW))
        l.setCustom(leyendas)
    }

    private fun setupPieChart() {
        val pieChart = findViewById<PieChart>(R.id.pie_chart)

        val list:ArrayList<PieEntry> = ArrayList()

        list.add(PieEntry(100f,"100"))
        list.add(PieEntry(101f,"101"))
        list.add(PieEntry(102f,"102"))
        list.add(PieEntry(103f,"103"))
        list.add(PieEntry(104f,"104"))

        val pieDataSet= PieDataSet(list,"List")

        pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS,255)
        pieDataSet.valueTextColor= Color.BLACK
        pieDataSet.valueTextSize=15f

        val pieData= PieData(pieDataSet)

        pieChart.data= pieData

        pieChart.description.text= "Pie Chart"

        pieChart.centerText="List"

        pieChart.animateY(2000)
    }
}