package com.mdanwarul.event;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;
    private Context context;
    private DatabaseReference databaseEvents; // Add a DatabaseReference
    private OnEventActionListener actionListener;

    public interface OnEventActionListener {
        void onEditEvent(int position);
        void onDeleteEventConfirmed(int position); // Updated to trigger after confirmation

        void onDeleteEvent(int position);
    }

    public EventAdapter(List<Event> eventList, Context context, DatabaseReference databaseEvents, OnEventActionListener actionListener) {
        this.eventList = eventList;
        this.context = context;
        this.databaseEvents = databaseEvents; // Initialize the DatabaseReference
        this.actionListener = actionListener;
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.eventName.setText(event.getName());
        holder.eventDate.setText(event.getDate());
        holder.eventDescription.setText(event.getDescription());

        // Alternate background colors based on position
        if (position % 2 == 0) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorEven));
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorOdd));
        }

        // Edit button click
        holder.editButton.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onEditEvent(position);
            }
        });

        // Delete button click
        holder.deleteButton.setOnClickListener(v -> {
            if (actionListener != null) {
                showDeleteConfirmationDialog(position); // Pass the event position for deletion
            }
        });
    }

    private void showDeleteConfirmationDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Event");
        builder.setMessage("Are you sure you want to delete this event? This action cannot be undone.");

        // Set "Yes" button action
        builder.setPositiveButton("Yes", (dialog, which) -> {
            // Perform the delete operation on confirmation
            deleteEvent(position);
        });

        // Set "No" button action
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void deleteEvent(int position) {
        // Log list size and position for debugging
        int listSize = eventList.size();
        System.out.println("Attempting to delete at position: " + position + ", List size: " + listSize);

        // Validate position
        if (position < 0 || position >= listSize) {
            Toast.makeText(context, "Invalid position. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String eventId = eventList.get(position).getId(); // Get the event ID
        databaseEvents.child(eventId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Remove the event from the list and notify adapter
                        eventList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, eventList.size()); // Refresh remaining items
                        Toast.makeText(context, "Event deleted successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Failed to delete event. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {

        TextView eventName, eventDate, eventDescription;
        ImageButton editButton, deleteButton;

        public EventViewHolder(View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.event_name_input498);
            eventDate = itemView.findViewById(R.id.event_date_input498);
            eventDescription = itemView.findViewById(R.id.event_description_input498);
            editButton = itemView.findViewById(R.id.edit_event_button);
            deleteButton = itemView.findViewById(R.id.delete_event_button);
        }
    }

    public void updateEvents(List<Event> newEvents) {
        this.eventList = newEvents; // Update the adapter's data
        notifyDataSetChanged(); // Refresh the RecyclerView
    }
}
