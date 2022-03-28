package hu.bme.aut.android.tripscheduler.fragment

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*
//Class for picking time and returning the picked time with the help of the listener
class TimePickerFragment(private val listener:TimePickerDialog.OnTimeSetListener, private val hour : Int, private val minute : Int) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return TimePickerDialog(requireActivity(), listener, hour, minute, DateFormat.is24HourFormat(activity))
    }
}