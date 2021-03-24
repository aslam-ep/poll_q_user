package com.hector.election_2021_user;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class DisplayCount extends AppCompatActivity {

    String debug = "poll_q :: ";

    String currentCount, boothName, lastUpdatedTime, boothNumber;

    TextView boothNameTextView, boothNumberTextView, lastUpdatedTextView, countTextView;
    CardView dataNotAdded, pollingEnded, pollingPeriod, queueCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_count);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_2);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent currentPage = getIntent();

        boothName = currentPage.getStringExtra("boothName");
        boothName = boothName.substring(0,1).toUpperCase() + boothName.substring(1);

        boothNumber = currentPage.getStringExtra("boothNumber");
        currentCount = currentPage.getStringExtra("currentCount");
        lastUpdatedTime = currentPage.getStringExtra("lastUpdatedTime");

        String[] updatedTime = lastUpdatedTime.split(":");

        if (updatedTime.length == 1)
            lastUpdatedTime = updatedTime[0] + ":00";
        else
            lastUpdatedTime = updatedTime[0] + ":"+updatedTime[1] + "0";

        mapViewToObject();

        boothNameTextView.setText("Booth Name: " + boothName);
        boothNumberTextView.setText("Booth Number: " + boothNumber);

        if(currentCount.equals("-1")) {
            dataNotAdded.setVisibility(View.VISIBLE);
        } else if (currentCount.equals("-2")){
            pollingEnded.setVisibility(View.VISIBLE);
        } else if( currentCount.equals("null")) {
            pollingPeriod.setVisibility(View.VISIBLE);
        }
        else {
            countTextView.setText(currentCount);
            lastUpdatedTextView.setText(lastUpdatedTime);
            queueCount.setVisibility(View.VISIBLE);
        }
    }

    private void mapViewToObject() {
        boothNameTextView = findViewById(R.id.boothName);
        boothNumberTextView = findViewById(R.id.boothNumber);
        dataNotAdded = findViewById(R.id.dataNotAdded);
        pollingEnded = findViewById(R.id.pollingEnded);
        pollingPeriod = findViewById(R.id.pollingPeriod);
        lastUpdatedTextView = findViewById(R.id.lastUpdatedTime);
        countTextView = findViewById(R.id.countValue);
        queueCount = findViewById(R.id.queueCount);
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(DisplayCount.this, BoothSelector.class));
        finish();
    }
}