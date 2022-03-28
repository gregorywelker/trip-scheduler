package hu.bme.aut.android.tripscheduler.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.android.tripscheduler.R
import hu.bme.aut.android.tripscheduler.data.Stop
import hu.bme.aut.android.tripscheduler.data.Trip
import hu.bme.aut.android.tripscheduler.fragment.EditStopDialogFragment
import hu.bme.aut.android.tripscheduler.fragment.TripFragment
import java.text.SimpleDateFormat
import java.util.*


//Adapter for stops inside a trip
class TripStopsAdapter(private val trip:Trip, private val listener: StopItemListener, private val activeTrips:Boolean) : RecyclerView.Adapter<TripStopsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.trip_stop_row_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val stop = trip.stops[position]
        holder.imgStopReady.visibility = if(stop.completed)View.VISIBLE else View.GONE
        holder.btnMarkStopReady.text = if(stop.completed) "Mark as not ready" else "Mark as ready"

        val calendar = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)

        calendar.time = simpleDateFormat.parse(stop.date)

        holder.place.text = stop.place
        holder.date.text = calendar.get(Calendar.YEAR).toString() + "." + calendar.get(Calendar.MONTH).toString() + "." + calendar.get(
            Calendar.DAY_OF_MONTH).toString() + ".\n" + calendar.get(Calendar.HOUR_OF_DAY).toString() + ":" + (if(calendar.get(Calendar.MINUTE) < 10) "0" + calendar.get(Calendar.MINUTE).toString() else calendar.get(Calendar.MINUTE).toString())

        //If the trip is active then set editing listeners and ui elements otherwise disable editing
        if(activeTrips) {
            val firstIncomplete = trip.stops.find { !it.completed }
            val lastComplete = trip.stops.findLast { it.completed }

            //Setting the activeness of the button which is used for setting the completion of the stops
            when {
                firstIncomplete == null -> {
                    holder.btnMarkStopReady.isEnabled = position == trip.stops.size - 1
                }
                lastComplete == null -> {
                    holder.btnMarkStopReady.isEnabled = position == 0
                }
                else -> holder.btnMarkStopReady.isEnabled =
                    stop == firstIncomplete || stop == lastComplete
            }
            //Adding click listener only if the stop is not complete, otherwise disable editing
            if(!stop.completed) {
                holder.itemView.setOnClickListener {
                    listener.onStopItemClicked(stop)
                }
            }

            holder.btnMarkStopReady.setOnClickListener {
                stop.completed = !stop.completed
                listener.onStopItemCompleteChanged(stop)
            }
        }else{
            holder.btnMarkStopReady.isEnabled = false
        }
    }

    //Listener for callbacks
    interface StopItemListener{
        fun onStopItemClicked(stop: Stop)
        fun onStopItemCompleteChanged(stop:Stop)
    }

    //Returning the item count
    override fun getItemCount(): Int {
        return trip.stops.size
    }
    
    //Returning the view holder
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val place: TextView = itemView.findViewById(R.id.place)
        val date: TextView = itemView.findViewById(R.id.date)
        val imgStopReady: ImageView = itemView.findViewById(R.id.img_stop_ready)
        val btnMarkStopReady: TextView = itemView.findViewById(R.id.btn_mark_stop_ready)
    }

}