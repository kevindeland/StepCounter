package com.example.kevindeland.accelerometergraph;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.util.LinkedList;


public class TurningActivity extends AppCompatActivity implements SensorEventListener {

    private static final Double PEAK_THRESHOLD = 1.5;
    private String TAG = "TURNING";

    private static final double ESTIMATED_AVERAGE_VALUE = 10.8;
    private SensorManager sensorManager;
    private Sensor accSensor;
    private Sensor ortSensor;

    private String counterType = null;

    private int myCount = 0;

    private int WINDOW_SIZE = 3;
    private LinkedList<Double> lastValues;
    private Double lastValue;
    private Double lastLastValue;
    private LinkedList<String> lastEvents = new LinkedList<String>();


    private int X_ORT_WINDOW_SIZE = 20;
    private LinkedList<Integer> xOrtWindow;

    private int Z_ORT_WINDOW_SIZE = 200;
    private LinkedList<Integer> zOrtWindow;

    private int zIntegral;
    private int zIntegralCount;
    private int Z_INTEGRAL_THRESHOLD_DROP = 200;

    private int MIN_TURN_TIME_IN_MS;
    private int MAX_TURN_TIME_IN_MS;


    private int counter = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_turning);

        //counterType = getIntent().getStringExtra("COUNTER_TYPE");
        //Log.d(TAG, counterType);

        //updateSensorDisplay(0);

        // Sensor things
        sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_UI);

        ortSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        sensorManager.registerListener(this, ortSensor, SensorManager.SENSOR_DELAY_UI);

        lastValues = new LinkedList<>();
        xOrtWindow = new LinkedList<>();
        zOrtWindow = new LinkedList<>();

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO

        Sensor mySensor = event.sensor;
        if(mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            //processAccelerometer(event);

        } else if (mySensor.getType() == Sensor.TYPE_ORIENTATION) {

            //Log.v(TAG, "found orientation");
            processOrientation(event);
        }
    }

    private void processOrientation(SensorEvent event) {

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        xOrtWindow.add((int) x);

        if (xOrtWindow.size() > X_ORT_WINDOW_SIZE) {
            xOrtWindow.pollFirst();
        }

        zOrtWindow.add((int) z);
        zIntegral += z;
        zIntegralCount++;
        if (zOrtWindow.size() > Z_ORT_WINDOW_SIZE) {
            // subtract last value from the running integral
            zIntegral -= zOrtWindow.pollFirst();
            zIntegralCount--;

        }


        if(counter++ >= 10) {
            //updateSensorDisplay("x: "+(int) x + "\ny: "+ (int) y+ "\nz: " +(int) z);
            updateSensorDisplay("Z: " + z  + "\nZintegral: " + zIntegral);
            counter = 0;
        }


    }


    private void processAccelerometer(SensorEvent event) {
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
                    updateSensorDisplay(""+myCount);
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
                        updateSensorDisplay("" + myCount);
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

    private double calculateDetrendedMagnitude(float x, float y, float z) {
        double magnitude = Math.sqrt(x*x + y*y + z*z);
        return magnitude - ESTIMATED_AVERAGE_VALUE;
    }

    private void updateSensorDisplay(String value) {

        TextView textView = (TextView) findViewById(R.id.orientation_display);
        textView.setText(value);


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
