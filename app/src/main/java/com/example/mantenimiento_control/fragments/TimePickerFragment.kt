package com.example.mantenimiento_control.fragments

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.Calendar

class TimePickerFragment (val listener: (String) -> Unit): DialogFragment(), TimePickerDialog.OnTimeSetListener {
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        //Obteniedo tiempo
        var hour = hourOfDay
        var minutes = minute
        //AM-PM
        var timeSet : String

        if (hour > 12) {
            hour -= 12;
            timeSet = "PM"
        } else if (hour == 0) {
            hour += 12;
            timeSet = "AM"
        } else if (hour == 12){
            timeSet = "PM"
        }else{
            timeSet = "AM"
        }

        var min = ""
        if (minutes < 10)
            min = "0" + minutes
        else
            min = minutes.toString()

        //Creando formato del Tiempo obtenido
        val aTime = StringBuilder().append(hour).append(':')
            .append(min).append(" ").append(timeSet).toString()

        //listener("$hourOfDay:$minute")
        listener("$aTime")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val dialog = TimePickerDialog(activity as Context, this, hour, minute, false)
        return dialog
    }
}