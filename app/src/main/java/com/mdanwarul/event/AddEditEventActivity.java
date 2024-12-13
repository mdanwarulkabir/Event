package com.mdanwarul.event;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddEditEventActivity extends AppCompatActivity {

    private EditText eventNameEdit498, eventDateEdit498, eventDescriptionEdit498;
    private Button saveEventButton498;
    private DatabaseReference databaseEvents;
    private FirebaseUser currentUser;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase if not already done
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this);
        }

        setContentView(R.layout.activity_add_edit_event);

        // Initialize Firebase Authentication and Database reference
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseEvents = FirebaseDatabase.getInstance().getReference("events");

        // Ensure the user is logged in before proceeding
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to create or edit events.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI elements
        eventNameEdit498 = findViewById(R.id.event_name_input498);
        eventDateEdit498 = findViewById(R.id.event_date_input498);
        eventDescriptionEdit498 = findViewById(R.id.event_description_input498);
        saveEventButton498 = findViewById(R.id.save_event_button498);

        // Check if it's edit mode (i.e., eventId is passed from the previous screen)
        if (getIntent().hasExtra("EVENT_ID")) {
            eventId = getIntent().getStringExtra("EVENT_ID");
            eventNameEdit498.setText(getIntent().getStringExtra("EVENT_NAME"));
            eventDateEdit498.setText(getIntent().getStringExtra("EVENT_DATE"));
            eventDescriptionEdit498.setText(getIntent().getStringExtra("EVENT_DESCRIPTION"));
        }

        // Show Date Picker when the event date EditText is clicked
        eventDateEdit498.setOnClickListener(v -> showDatePicker());

        // Save event to Firebase when the save button is clicked
        saveEventButton498.setOnClickListener(v -> validateAndSaveEvent());
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            String selectedDate = year1 + "-" + (month1 + 1) + "-" + dayOfMonth;
            eventDateEdit498.setText(selectedDate);
        }, year, month, day).show();
    }

    private void validateAndSaveEvent() {
        String name = eventNameEdit498.getText().toString().trim();
        String date = eventDateEdit498.getText().toString().trim();
        String description = eventDescriptionEdit498.getText().toString().trim();

        // Check for empty fields
        if (name.isEmpty() || date.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        // Query Firebase to check for existing events on the same date for the user
        databaseEvents.orderByChild("userIdDate").equalTo(userId + "_" + date)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // An event already exists for the user on this date
                            Toast.makeText(AddEditEventActivity.this, "You already have an event on this date!", Toast.LENGTH_SHORT).show();
                        } else {
                            // No event exists, proceed to save
                            saveEventToFirebase(name, date, description, userId);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(AddEditEventActivity.this, "Error checking event date. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveEventToFirebase(String name, String date, String description, String userId) {
        // Validate the date: Check if it's in the past
        if (isDateInPast(date)) {
            Toast.makeText(this, "The selected date cannot be in the past.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check for existing events on the same date for the user
        String userIdDateKey = userId + "_" + date;
        databaseEvents.orderByChild("userIdDate").equalTo(userIdDateKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && (eventId == null || !snapshot.hasChild(eventId))) {
                            // If a matching event exists and it's not the same event being edited
                            Toast.makeText(AddEditEventActivity.this, "An event already exists on this date.", Toast.LENGTH_SHORT).show();
                        } else {
                            // Proceed to save the event
                            saveEventToFirebaseDatabase(name, date, description, userId);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AddEditEventActivity.this, "Error checking existing events. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Helper method to actually save the event after validation
    private void saveEventToFirebaseDatabase(String name, String date, String description, String userId) {
        // Generate a new event ID if it's a new event
        if (eventId == null) {
            eventId = databaseEvents.push().getKey(); // Generate unique key for the event
        }

        // Create the event object with the current user's ID and userIdDate
        String userIdDateKey = userId + "_" + date;
        Event event = new Event(eventId, name, date, description, userId);
        event.setUserIdDate(userIdDateKey);

        // Save the event object to Firebase under "events" node
        databaseEvents.child(eventId).setValue(event)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Event saved successfully!", Toast.LENGTH_SHORT).show();

                        // After saving, navigate back to ContentActivity
                        Intent intent = new Intent(AddEditEventActivity.this, ContentActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to save event. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to check if a date is in the past
    private boolean isDateInPast(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            sdf.setLenient(false); // Ensure strict date parsing
            Date selectedDate = sdf.parse(date);

            // Strip time component from current date
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            return selectedDate.before(today.getTime()); // Past dates only
        } catch (ParseException e) {
            e.printStackTrace();
            return true; // If date parsing fails, consider it invalid
        }
    }


}
