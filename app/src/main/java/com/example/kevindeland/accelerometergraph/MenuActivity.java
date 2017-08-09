package com.example.kevindeland.accelerometergraph;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }

    public void switchToGraphScreen(View view) {
        Intent intent = new Intent(this, GraphActivity.class);
        startActivity(intent);
    }

    public void switchToZeroCountingScreen(View view) {
        Intent intent = new Intent(this, CounterActivity.class);
        intent.putExtra("COUNTER_TYPE", "ZERO_CROSSING");
        startActivity(intent);
    }

    public void switchToEventMachineScreen(View view) {
        Intent intent = new Intent(this, CounterActivity.class);
        intent.putExtra("COUNTER_TYPE", "EVENT_STATE_MACHINE");
        startActivity(intent);
    }

    public void switchToOrientationScreen(View view) {
        Intent intent = new Intent(this, OrientationGraphActivity.class);
        startActivity(intent);
    }

    public void switchToTurningScreen(View view) {
        Intent intent = new Intent(this, TurningActivity.class);
        startActivity(intent);
    }
}
