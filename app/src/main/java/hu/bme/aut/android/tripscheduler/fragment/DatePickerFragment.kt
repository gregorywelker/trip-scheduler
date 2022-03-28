package hu.bme.aut.android.tripscheduler.fragment

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*
//Class for picking date and returning the picked date with the help of the listener
class DatePickerFragment(private val listener:DatePickerDialog.OnDateSetListener,private val year:Int,private val month:Int, private val day:Int) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return DatePickerDialog(requireActivity(), listener, year, month, day)
    }
}