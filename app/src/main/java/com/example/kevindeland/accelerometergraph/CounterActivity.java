package com.example.kevindeland.accelerometergraph;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;


public class CounterActivity extends AppCompatActivity implements SensorEventListener {

    private static final Double PEAK_THRESHOLD = 1.5;
    private String TAG = "COUNTER";

    private static final double ESTIMATED_AVERAGE_VALUE = 10.8;
    private SensorManager sensorManager;
    private Sensor accSensor;

    private String counterType = null;

    private int myCount = 0;

    private int WINDOW_SIZE = 3;
    private LinkedList<Double> lastValues;
    private Double lastValue;
    private Double lastLastValue;
    private LinkedList<String> lastEvents = new LinkedList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter);

        counterType = getIntent().getStringExtra("COUNTER_TYPE");
        Log.d(TAG, counterType);

        updateSensorDisplay(0);

        // Sensor things
        sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_UI);

        lastValues = new LinkedList<Double>();

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO

        Sensor mySensor = event.sensor;
        if(mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            double detrendedMagnitude = calculateDetrendedMagnitude(x, y, z);

            if(counterType.equals("ZERO_CROSSING")) {

                if (lastValues.size() > 0) {
                    double lastValue = lastValues.peekLast();

                    double sign = lastValue * detrendedMagnitude;

                    // if crosses zero from the negative direction, it's a step
                    if ( sign < 0 && lastValue < 0) {
                        myCount++;
                        updateSensorDisplay(myCount);
                    }

                }

                lastValues.addLast(detrendedMagnitude);
                if (lastValues.size() > WINDOW_SIZE) {
                    lastValues.pollFirst();
                }
            } else if (counterType.equals("EVENT_STATE_MACHINE")) {


                // TODO check that it's gone through a whole cycle!!!

                if (lastValue == null) {
                    lastValue = detrendedMagnitude;
                } else if (lastLastValue == null) {
                    lastLastValue = lastValue;
                    lastValue = detrendedMagnitude;
                } else {

                    // Zero crossing
                    double sign = lastValue * detrendedMagnitude;
                    if (sign < 0 && lastValue < 0) {
                        // add "zero_up"
                        Log.d(TAG, "zero up!");
                        lastEvents.clear();
                        lastEvents.add("zero_up");
                    } else if (sign < 0 && lastValue > 0) {
                        // add "zero_down"
                        Log.d(TAG, "zero down!");
                        if(lastEvents.size() == 2 && lastEvents.peekLast().equals("peak_up"))
                            lastEvents.add("zero_down");
                    }

                    // Peak detection
                    double diff0 = lastValue - lastLastValue;
                    double diff1 = detrendedMagnitude - lastValue;
                    double diffSign = diff0 * diff1;
                    if (diffSign < 0 && diff0 < 0) {
                        // TODO add negative peak
                        Log.d(TAG, "negative peak!");
                        if(lastEvents.size() == 3 && lastEvents.peekLast().equals("zero_down")) {
                            lastEvents.clear();
                            myCount++;
                            updateSensorDisplay(myCount);
                        }
                    } else if (diffSign < 0 && diff0 > 0 && lastValue > PEAK_THRESHOLD) {
                        // add positive peak
                        Log.d(TAG, "positive peak!");
                        if(lastEvents.size() == 1 && lastEvents.peekLast().equals("zero_up"))
                            lastEvents.add("peak_up");
                    }

                    Log.d(TAG, "last events size: " + lastEvents.size());
                    lastLastValue = lastValue;
                    lastValue = detrendedMagnitude;

                }



            }

        }
    }

    private double calculateDetrendedMagnitude(float x, float y, float z) {
        double magnitude = Math.sqrt(x*x + y*y + z*z);
        return magnitude - ESTIMATED_AVERAGE_VALUE;
    }

    private void updateSensorDisplay(int value) {

        TextView textView = (TextView) findViewById(R.id.count_display);
        textView.setText("" + value);


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
