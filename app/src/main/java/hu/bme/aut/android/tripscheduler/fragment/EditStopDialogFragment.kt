package hu.bme.aut.android.tripscheduler.fragment

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import hu.bme.aut.android.tripscheduler.data.Stop
import hu.bme.aut.android.tripscheduler.data.Trip
import hu.bme.aut.android.tripscheduler.databinding.DialogStopBinding
import java.text.SimpleDateFormat
import java.util.*

//Dialog for creating and editing stops
class EditStopDialogFragment(private val trip: Trip, private val targetStop:Stop?) : DialogFragment(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private lateinit var binding: DialogStopBinding

    private val lowerDate = Calendar.getInstance()
    private val currentDate = Calendar.getInstance()
    private val upperDate = Calendar.getInstance()
    private lateinit var editConstraint: StopEditConstraint
    private lateinit var listener:TripFragment.TripListener
    private val dateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = activity as TripFragment.TripListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogStopBinding.inflate(LayoutInflater.from(context))

        var title = ""

        //If the target stop is null then the user is creating a new stop otherwise there is a target
        // StopEditConstraints handle the time limits of the new/edited stop
        if(targetStop == null)
        {
            title = "Create new stop"

            if(trip.stops.size <= 0)
            {
                editConstraint = StopEditConstraint.NONE
            }else{
                editConstraint = StopEditConstraint.LOWER
                lowerDate.time = dateFormat.parse(trip.stops.last().date)
                currentDate.time = lowerDate.time
            }
        }else{
            title = "Update stop"
            binding.place.setText(targetStop.place)
            currentDate.time = dateFormat.parse(targetStop.date)

            if(trip.stops.size <= 1){
                editConstraint = StopEditConstraint.NONE
            }else if(trip.stops.size > 1 && targetStop == trip.stops.first())
            {
                editConstraint = StopEditConstraint.UPPER
                upperDate.time = dateFormat.parse(trip.stops[trip.stops.indexOf(targetStop) + 1].date)
            }else if(trip.stops.size > 1 && targetStop == trip.stops.last())
            {
                editConstraint = StopEditConstraint.LOWER
                lowerDate.time = dateFormat.parse(trip.stops.last().date)
            }else{
                editConstraint = StopEditConstraint.BOTH
                lowerDate.time = dateFormat.parse(trip.stops[trip.stops.indexOf(targetStop) - 1].date)
                upperDate.time = dateFormat.parse(trip.stops[trip.stops.indexOf(targetStop) + 1].date)
            }
        }

        //Setting the actual date and adding listeners
        binding.btnPickDate.text =
            currentDate.get(Calendar.YEAR).toString() + "." + currentDate.get(Calendar.MONTH).toString() + "." + currentDate.get(Calendar.DAY_OF_MONTH).toString() + "."
        binding.btnPickTime.text = currentDate.get(Calendar.HOUR_OF_DAY).toString() + ":" + (if(currentDate.get(Calendar.MINUTE) < 10) "0" + currentDate.get(Calendar.MINUTE).toString() else currentDate.get(Calendar.MINUTE).toString())

        binding.btnPickDate.setOnClickListener{
            DatePickerFragment(this, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH)).show(childFragmentManager, "startDatePicker")
        }
        binding.btnPickTime.setOnClickListener{
            TimePickerFragment(this, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE)).show(childFragmentManager, "startTimePicker")
        }

        return AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(binding.root)
            .setPositiveButton("OK") { dialogInterface, i ->
                //If the target stop is not null then edit it otherwise create a new one and update the trip
                if(targetStop != null){
                    targetStop.place = binding.place.text.toString()
                    targetStop.date = currentDate.time.toString()
                    listener.onUpdateTrip(trip,TripFragment.TripUpdateType.UPDATE_STOP, targetStop, null, null)
                }else{
                    trip.stops.add(Stop(binding.place.text.toString(), currentDate.time.toString()))
                    listener.onUpdateTrip(trip, TripFragment.TripUpdateType.CREATE_STOP, null,null,null)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
    }

    //Callback for when date has been set
    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        currentDate.set(p1,p2,p3, currentDate.get(Calendar.HOUR_OF_DAY),currentDate.get(Calendar.MINUTE))
        makeDateConsistent(currentDate)
        val targetBtn = binding.btnPickDate
        targetBtn.text =
            currentDate.get(Calendar.YEAR).toString() + "." + currentDate.get(Calendar.MONTH).toString() + "." + currentDate.get(Calendar.DAY_OF_MONTH).toString() + "."

    }

    //Callback for when time has been set
    override fun onTimeSet(p0: TimePicker?, p1: Int, p2: Int) {
        currentDate.set(currentDate.get(Calendar.YEAR),currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH),p1,p2)
        makeDateConsistent(currentDate)
        val targetBtn = binding.btnPickTime
        targetBtn.text = currentDate.get(Calendar.HOUR_OF_DAY).toString() + ":" + (if (currentDate.get(Calendar.MINUTE) < 10) "0" + currentDate.get(Calendar.MINUTE).toString() else currentDate.get(Calendar.MINUTE).toString())
    }

    //Makes the picked date consistent with the time limits set by the constraints
    fun makeDateConsistent(current:Calendar)
    {
         when(editConstraint){
            StopEditConstraint.LOWER-> {
                if(current.time < lowerDate.time){
                    current.time = lowerDate.time
                }
            }
            StopEditConstraint.UPPER-> {
                if(current.time > upperDate.time){
                    current.time = upperDate.time
                }
            }
            StopEditConstraint.BOTH-> {
                if(current.time < lowerDate.time){
                    current.time = lowerDate.time
                }else if(current.time > upperDate.time){
                    current.time = upperDate.time
                }
            }
            else -> {
                current
            }
        }
    }

    //Enum for knowing what type of constraints the stop needs to conform to
    enum class StopEditConstraint {
        LOWER, UPPER, BOTH, NONE
    }
    companion object {
        const val TAG = "StopDialogFragment"
    }

}
