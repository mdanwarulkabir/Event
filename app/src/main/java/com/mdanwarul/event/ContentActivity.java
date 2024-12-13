package com.mdanwarul.event;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ContentActivity extends AppCompatActivity implements EventAdapter.OnEventActionListener {

    private RecyclerView eventRecyclerView498;
    private FloatingActionButton addEventButton498;
    private EventAdapter eventAdapter;
    private List<Event> eventList;
    private List<Event> fullEventList; // Holds the complete list of events

    private TextView welcomeTextView498;
    private ImageView emptyIcon498;
    private TextView emptyTextView498;

    private DatabaseReference databaseEvents;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    private final ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        String id = extras.getString("EVENT_ID");
                        String name = extras.getString("EVENT_NAME");
                        String date = extras.getString("EVENT_DATE");
                        String description = extras.getString("EVENT_DESCRIPTION");
                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        Event event = new Event(id, name, date, description, userId);
                        updateEventList(event);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        // Initialize Firebase Auth and Database
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        databaseEvents = FirebaseDatabase.getInstance().getReference("events");

        // Initialize UI components
        eventRecyclerView498 = findViewById(R.id.event_recyclerview498);
        addEventButton498 = findViewById(R.id.add_event_button498);
        welcomeTextView498 = findViewById(R.id.welcome_text_view498);
        emptyIcon498 = findViewById(R.id.empty_icon498);
        emptyTextView498 = findViewById(R.id.empty_text_view498);
        ImageView logoutButton = findViewById(R.id.logout_text_view);
        EditText searchInput = findViewById(R.id.search_input);
        FloatingActionButton searchButton = findViewById(R.id.search_button);

        // Initialize event lists
        eventList = new ArrayList<>();
        fullEventList = new ArrayList<>();

        // Setup RecyclerView and Adapter
        eventAdapter = new EventAdapter(eventList, this, databaseEvents, this);
        eventRecyclerView498.setLayoutManager(new LinearLayoutManager(this));
        eventRecyclerView498.setAdapter(eventAdapter);

        // Search functionality
        searchButton.setOnClickListener(v -> {
            String query = searchInput.getText().toString().trim();
            if (!query.isEmpty()) {
                List<Event> filteredEvents = new ArrayList<>();
                for (Event event : fullEventList) {
                    if (event.getName().toLowerCase().contains(query.toLowerCase()) ||
                            event.getDate().toLowerCase().contains(query.toLowerCase()) ||
                            event.getDescription().toLowerCase().contains(query.toLowerCase())) {
                        filteredEvents.add(event);
                    }
                }
                eventAdapter.updateEvents(filteredEvents);
            } else {
                eventAdapter.updateEvents(fullEventList);
            }
        });

        // Logout functionality
        logoutButton.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(ContentActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // User greeting and fetching events
        if (currentUser != null) {
            String username = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : currentUser.getEmail();
            welcomeTextView498.setText(String.format("Welcome, %s!", username));
            fetchEvents(currentUser.getUid());
        }

        // Add event button
        addEventButton498.setOnClickListener(view -> {
            Intent intent = new Intent(ContentActivity.this, AddEditEventActivity.class);
            resultLauncher.launch(intent);
        });

        // Update UI based on event list state
        updateEmptyState();
    }

    private void fetchEvents(String userId) {
        databaseEvents.orderByChild("userId").equalTo(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                eventList.clear();  // Clear the current list
                fullEventList.clear();  // Clear the full list
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Event event = snapshot.getValue(Event.class);
                    if (event != null) {
                        eventList.add(event);
                        fullEventList.add(event);
                    }
                }
                eventAdapter.notifyDataSetChanged();  // Notify the adapter about the data change
                updateEmptyState();  // Update the UI for empty state if needed
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ContentActivity.this, "Failed to load events.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateEventList(Event newEvent) {
        boolean updated = false;
        for (int i = 0; i < eventList.size(); i++) {
            if (eventList.get(i).getId().equals(newEvent.getId())) {
                eventList.set(i, newEvent);
                updated = true;
                break;
            }
        }
        if (!updated) {
            eventList.add(newEvent);
        }
        eventAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (eventList.isEmpty()) {
            emptyIcon498.setVisibility(View.VISIBLE);
            emptyTextView498.setVisibility(View.VISIBLE);
            eventRecyclerView498.setVisibility(View.GONE);
        } else {
            emptyIcon498.setVisibility(View.GONE);
            emptyTextView498.setVisibility(View.GONE);
            eventRecyclerView498.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onEditEvent(int position) {
        Event event = eventList.get(position);
        Intent intent = new Intent(ContentActivity.this, AddEditEventActivity.class);
        intent.putExtra("EVENT_ID", event.getId());
        intent.putExtra("EVENT_NAME", event.getName());
        intent.putExtra("EVENT_DATE", event.getDate());
        intent.putExtra("EVENT_DESCRIPTION", event.getDescription());
        resultLauncher.launch(intent);
    }

    @Override
    public void onDeleteEventConfirmed(int position) {
        onDeleteEvent(position); // Delegate to delete event
    }

    @Override
    public void onDeleteEvent(int position) {
        if (position < 0 || position >= eventList.size()) {
            Toast.makeText(this, "Invalid event selected for deletion.", Toast.LENGTH_SHORT).show();
            return;
        }

        Event event = eventList.get(position);
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (databaseEvents != null && event.getId() != null) {
                        // Remove event from the database
                        databaseEvents.child(event.getId()).removeValue()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // Event removed from the database, now update the UI
                                        fetchEvents(currentUser.getUid()); // Reload the events from the database
                                        Toast.makeText(this, "Event deleted successfully!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(this, "Failed to delete event. Please try again.", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "Error: Database reference or event ID is null.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }


    private void removeEventFromList(int position) {
        if (position < 0 || position >= eventList.size()) return;

        eventList.remove(position);
        eventAdapter.notifyItemRemoved(position);
        updateEmptyState();
    }
}