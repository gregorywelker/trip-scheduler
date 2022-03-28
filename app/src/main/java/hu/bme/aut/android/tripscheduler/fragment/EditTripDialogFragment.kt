package hu.bme.aut.android.tripscheduler.fragment
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import hu.bme.aut.android.tripscheduler.data.Trip
import hu.bme.aut.android.tripscheduler.databinding.DialogNewActiveTripBinding

//Dialog for editing the a trip, essentially setting its name or creating a new trip
class EditTripDialogFragment(val trip: Trip?) : DialogFragment() {

    private lateinit var binding: DialogNewActiveTripBinding
    private lateinit var listener: TripFragment.TripListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as TripFragment.TripListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogNewActiveTripBinding.inflate(LayoutInflater.from(context))

        //If the trip is not null then this is an edit, set the name to the current name
        if(trip != null){
            binding.tripName.setText(trip.name)
        }

        //Return the created dialog
        return AlertDialog.Builder(requireContext())
            .setTitle(if(trip==null) "Create new trip" else "Edit trip")
            .setView(binding.root)
            .setPositiveButton("OK") { dialogInterface, i ->
                if(trip == null)
                {
                    listener.onCreateNewActiveTrip(binding.tripName.text.toString())
                }
                else{
                    trip.name = binding.tripName.text.toString()
                    listener.onUpdateTrip(trip, TripFragment.TripUpdateType.RENAME_TRIP,null, null, null)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
    }

    companion object {
        const val TAG = "NewActiveTripDialogFragment"
    }
}
