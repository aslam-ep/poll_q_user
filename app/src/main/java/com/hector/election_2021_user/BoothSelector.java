package com.hector.election_2021_user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BoothSelector extends AppCompatActivity {

    String debug = "poll_q :: ";
    boolean doubleBackToExitPressedOnce = false;

    LinearLayout boothSelectionForm;
    AutoCompleteTextView constituencyName;
    TextInputEditText boothNumber;
    Button getCountButton;
    ImageView exitButton;
    ProgressBar findProgressBar;

    // Firebase instances
    FirebaseFirestore db;
    FirebaseFirestoreSettings settings;

    // Fetch Variables
    String [] constituencies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booth_selector);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        boothSelectionForm = findViewById(R.id.boothFinderForm);
        constituencyName = findViewById(R.id.constituency_name);
        boothNumber = findViewById(R.id.booth_name);
        getCountButton = findViewById(R.id.get_count_button);
        exitButton = findViewById(R.id.logo_out);
        findProgressBar = findViewById(R.id.findProgress);

        db = FirebaseFirestore.getInstance();
        settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        constituencies = new String[] {"Kalpetta", "Mananthavady", "Sulthanbathery"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(BoothSelector.this, R.layout.dropdown_menu_popup_item, constituencies);
        constituencyName.setAdapter(adapter);

        getCountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(getApplicationContext(), DisplayCount.class));
//                finish();
                if (isTodayElectionDay()) {
                    if (checkData()) {
                        getCountButton.setVisibility(View.INVISIBLE);
                        findProgressBar.setVisibility(View.VISIBLE);

                        String constituency_str = String.valueOf(constituencyName.getText());
                        int constituency_id = getConstituencyId(constituency_str);
                        if (constituency_id != 0) {
                            final String booth_number = boothNumber.getText().toString();

                            db.collection(constituency_str.toLowerCase()).document(booth_number)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot documentSnapshot = task.getResult();
                                                if (documentSnapshot.exists()) {

                                                    String currentCount = String.valueOf(documentSnapshot.getLong(getCurrentTime()));

                                                    String boothName = documentSnapshot.getString("booth_name");

                                                    String lastUpdatedOn = getCurrentTime();
//                                                    Log.d(debug, lastUpdatedOn);

                                                    int i = 0;

                                                    while (currentCount.equals("-1") && i < documentSnapshot.getData().size() - 2) {
                                                        lastUpdatedOn = decreaseTime(lastUpdatedOn);
                                                        currentCount = String.valueOf(documentSnapshot.getLong(lastUpdatedOn));
                                                        i++;
                                                    }


                                                    Intent nextPage = new Intent(BoothSelector.this, DisplayCount.class);
                                                    nextPage.putExtra("currentCount", currentCount);
                                                    nextPage.putExtra("boothName", boothName);
                                                    nextPage.putExtra("boothNumber", booth_number);
                                                    nextPage.putExtra("lastUpdatedTime", lastUpdatedOn);
                                                    startActivity(nextPage);
                                                    finish();

                                                } else {
                                                    boothNumber.setError("Invalid Booth Number");
                                                    getCountButton.setVisibility(View.VISIBLE);
                                                    findProgressBar.setVisibility(View.INVISIBLE);
                                                }
                                            } else {
                                                Snackbar.make(findViewById(android.R.id.content), "Check Your Internet Connection!", Snackbar.LENGTH_SHORT).show();
                                                getCountButton.setVisibility(View.VISIBLE);
                                                findProgressBar.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    });
                        } else {
                            constituencyName.setError("Select a Valid Constituency");
                            getCountButton.setVisibility(View.VISIBLE);
                            findProgressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                }else{
                    Snackbar.make(findViewById(android.R.id.content), "Today is not Election day", Snackbar.LENGTH_SHORT).show();
                    getCountButton.setVisibility(View.VISIBLE);
                    findProgressBar.setVisibility(View.INVISIBLE);
                }
            }
        });

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private boolean isTodayElectionDay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String today = dateFormat.format(new Date());
        String election_day = "07-04-2021";

//        if (today.equals(election_day)){
//            return true;
//        }else{
//            return false;
//        }
        return true;
    }

    private String decreaseTime(String lastUpdatedOn) {
        String[] timeArray = lastUpdatedOn.split(":");

        String valueToReturn;
        if (timeArray.length == 1) {
            valueToReturn = String.valueOf(Integer.parseInt(timeArray[0]) - 1);
            if(valueToReturn.length() == 1)
                valueToReturn = "0"+valueToReturn;
            valueToReturn = valueToReturn + ":3";
        }
        else
            valueToReturn = timeArray[0];

        return valueToReturn;
    }

    private String getCurrentTime() {
        SimpleDateFormat currentHourFormatter = new SimpleDateFormat("HH");
        SimpleDateFormat currentMinuteFormatter = new SimpleDateFormat("mm");
        String currentHour = currentHourFormatter.format(new Date());
        String currentMinute = currentMinuteFormatter.format(new Date());

        currentMinute = ((Integer.parseInt(currentMinute) < 30) ?  currentHour : currentHour+":3");

        return currentMinute;
//        return "22";
    }

    private int getConstituencyId(String value) {
        if (value.equalsIgnoreCase("Kalpetta")) return 102;
        if (value.equalsIgnoreCase("Sulthanbathery")) return 103;
        if (value.equalsIgnoreCase("Mananthavady")) return  101;
        return 0;
    }

    private boolean checkData() {
        if(constituencyName.getText().toString().isEmpty()){
            constituencyName.setError("Constituency Name Required");
            return false;
        }else if(boothNumber.getText().toString().isEmpty() ){
            boothNumber.setError("Booth Number Required");
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Snackbar.make(findViewById(android.R.id.content), "Press Back Again", Snackbar.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }
}