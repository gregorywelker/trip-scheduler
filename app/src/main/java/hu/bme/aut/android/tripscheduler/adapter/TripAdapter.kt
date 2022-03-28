package hu.bme.aut.android.tripscheduler.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.android.tripscheduler.R
import hu.bme.aut.android.tripscheduler.data.Stop
import hu.bme.aut.android.tripscheduler.data.Trip
import java.text.SimpleDateFormat
import java.util.*


//Adapter for trips both active and completed
class TripAdapter(private val listener: TripItemListener, private val activeTrips:Boolean) : RecyclerView.Adapter<TripAdapter.ViewHolder>() {

    var trips = mutableListOf<Trip>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.trip_row_item, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //Clip the item because of the rounded edges
        holder.itemView.clipToOutline = true

        val trip = trips[position]

        //Listener for clicking an item
        holder.itemView.setOnClickListener{
            listener.onTripItemClicked(trip)
        }

        holder.tripName.text = trip.name

        holder.btnDeleteTrip.setOnClickListener{
            listener.onTripItemDeleted(trip)
        }
        holder.btnCompleteTrip.setOnClickListener{
            listener.onTripItemCompleteChanged(trip)
        }

        holder.btnCompleteTrip.text = if(activeTrips) "Complete trip" else "Activate trip"

        //Setting the trip`s data if there are no stops
        if(trip.stops.size <= 0){
            holder.imgTripReady.visibility = View.GONE
            holder.tripData.visibility = View.VISIBLE
            holder.btnStopReady.visibility = View.GONE

            holder.place.text = "No place available"
            holder.date.text = "-"
        }//Setting the trip`s data if all stops are complete
        else if(trip.stops.last().completed) {
            holder.imgTripReady.visibility = View.VISIBLE
            holder.tripData.visibility = View.GONE
            holder.btnStopReady.visibility = View.GONE
        }
        //Setting the trip`s data if there are stops to be completed
        else{
            holder.imgTripReady.visibility = View.GONE
            holder.tripData.visibility = View.VISIBLE
            holder.btnStopReady.visibility = View.VISIBLE

            val stop = trip.stops.first{ !it.completed }

            holder.btnStopReady.setOnClickListener{
                val stop = trip.stops.first{!it.completed}
                stop.completed = true
                listener.onTripStopItemCompleteChanged(trip,stop)
            }

            val calendar = Calendar.getInstance()
            val simpleDateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
            calendar.time = simpleDateFormat.parse(stop.date)

            holder.place.text = stop.place
            holder.date.text = calendar.get(Calendar.YEAR).toString() + "." + calendar.get(Calendar.MONTH).toString() + "." + calendar.get(
                Calendar.DAY_OF_MONTH).toString() + ".\n" + calendar.get(Calendar.HOUR_OF_DAY).toString() + ":" + (if(calendar.get(Calendar.MINUTE) < 10) "0" + calendar.get(Calendar.MINUTE).toString() else calendar.get(Calendar.MINUTE).toString())
        }

        //If the trip is completed then disable editing
        if(!activeTrips){
            holder.btnStopReady.isEnabled = false
        }
    }

    //Adding a new trip to the list
    fun insert(trip: Trip) {
        trips.add(trip)
        notifyItemInserted(trips.size - 1)
    }

    //Listener for trip actions
    interface TripItemListener{
        fun onTripItemClicked(trip: Trip)
        fun onTripStopItemCompleteChanged(trip:Trip, stop:Stop)
        fun onTripItemCompleteChanged(trip:Trip)
        fun onTripItemDeleted(trip:Trip)
    }

    //Returning item count
    override fun getItemCount(): Int {
        return trips.size
    }

    //Returning the view holder
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val tripName: TextView = itemView.findViewById(R.id.trip_name)
        val place: TextView = itemView.findViewById(R.id.place)
        val date: TextView = itemView.findViewById(R.id.date)
        val tripData: LinearLayout = itemView.findViewById(R.id.trip_data)
        val imgTripReady: ImageView = itemView.findViewById(R.id.img_trip_ready)
        val btnStopReady: Button = itemView.findViewById(R.id.btn_mark_stop_ready)
        val btnDeleteTrip: Button = itemView.findViewById(R.id.btn_delete_trip)
        val btnCompleteTrip: Button = itemView.findViewById(R.id.btn_complete_trip)
    }
}