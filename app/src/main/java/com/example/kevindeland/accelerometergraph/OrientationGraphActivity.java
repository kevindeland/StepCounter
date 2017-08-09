package com.example.kevindeland.accelerometergraph;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.androidplot.Plot;
import com.androidplot.util.PlotStatistics;
import com.androidplot.util.Redrawer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Random;

public class OrientationGraphActivity extends AppCompatActivity implements SensorEventListener {


    public String TAG = "Orientation";

    private static final int HISTORY_SIZE = 500;
    private SensorManager sensorManager;
    private Sensor orientationSensor;

    private XYPlot xyzLevelsPlot = null;
    private XYPlot xyzHistoryPlot = null;

    private SimpleXYSeries xLvlSeries;
    private SimpleXYSeries yLvlSeries;
    private SimpleXYSeries zLvlSeries;

    private SimpleXYSeries xHistorySeries;
    private SimpleXYSeries yHistorySeries;
    private SimpleXYSeries zHistorySeries;

    private Redrawer redrawer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orientation);

        // Sensor thingssiz
        sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        sensorManager.registerListener(this, orientationSensor, SensorManager.SENSOR_DELAY_UI);

        /*
         * Plot things
         */
        /*
        xyzLevelsPlot = (XYPlot) findViewById(R.id.xyzLevelsPlot);
        xyzLevelsPlot.setDomainBoundaries(-1, 1, BoundaryMode.FIXED);

        xLvlSeries = new SimpleXYSeries("X");
        yLvlSeries = new SimpleXYSeries("Y");
        zLvlSeries = new SimpleXYSeries("Z");

        xyzLevelsPlot.addSeries(xLvlSeries,
                new BarFormatter(Color.rgb(0, 200, 0), Color.rgb(0, 80, 0)));
        xyzLevelsPlot.addSeries(yLvlSeries,
                new BarFormatter(Color.rgb(200, 0, 0), Color.rgb(80, 0, 0)));
        xyzLevelsPlot.addSeries(zLvlSeries,
                new BarFormatter(Color.rgb(0, 0, 200), Color.rgb(0, 0, 80)));

        xyzLevelsPlot.setDomainStepValue(3);
        xyzLevelsPlot.setLinesPerRangeLabel(3);


        // TODO what are min amd max readings?
        xyzLevelsPlot.setRangeBoundaries(-20, 20, BoundaryMode.FIXED);
        xyzLevelsPlot.setDomainLabel("");
        xyzLevelsPlot.getDomainTitle().pack();
        xyzLevelsPlot.setRangeLabel("acceleration (m/s)");
        xyzLevelsPlot.getRangeTitle().pack();
        xyzLevelsPlot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).
                setFormat(new DecimalFormat("#"));
        // update domain and range axis labels
        */


        xyzHistoryPlot = (XYPlot) findViewById(R.id.xyzHistoryPlot);

        xHistorySeries = new SimpleXYSeries("exes");
        xHistorySeries.useImplicitXVals();
        yHistorySeries = new SimpleXYSeries("whys");
        yHistorySeries.useImplicitXVals();
        zHistorySeries = new SimpleXYSeries("zees");
        zHistorySeries.useImplicitXVals();

        xyzHistoryPlot.setRangeBoundaries(-180, 180, BoundaryMode.FIXED);
        xyzHistoryPlot.setDomainBoundaries(0, HISTORY_SIZE, BoundaryMode.FIXED);
        xyzHistoryPlot.addSeries(xHistorySeries,
                new LineAndPointFormatter(
                        Color.rgb(100, 100, 200), null, null, null));
        xyzHistoryPlot.addSeries(yHistorySeries,
                new LineAndPointFormatter(
                        Color.rgb(200, 100, 100), null, null, null));
        xyzHistoryPlot.addSeries(zHistorySeries,
                new LineAndPointFormatter(
                        Color.rgb(100, 200, 100), null, null, null));
        xyzHistoryPlot.setDomainStepMode(StepMode.INCREMENT_BY_VAL);
        xyzHistoryPlot.setDomainStepValue(HISTORY_SIZE/10);
        xyzHistoryPlot.setLinesPerRangeLabel(3);
        xyzHistoryPlot.setDomainLabel("Sample Index");
        xyzHistoryPlot.getDomainTitle().pack();
        xyzHistoryPlot.setRangeLabel("rotation");
        xyzHistoryPlot.getRangeTitle().pack();

        xyzHistoryPlot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).
                setFormat(new DecimalFormat("#"));

        xyzHistoryPlot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).
                setFormat(new DecimalFormat("#"));

        final PlotStatistics levelStats = new PlotStatistics(1000, false);
        final PlotStatistics histStats = new PlotStatistics(1000, false);

        //xyzLevelsPlot.addListener(levelStats);
        xyzHistoryPlot.addListener(histStats);


        // Redrawer
        redrawer = new Redrawer(
                Arrays.asList(new Plot[]{xyzHistoryPlot}), 100, false);

    }


    @Override
    public void onResume() {
        super.onResume();
        redrawer.start();
    }

    @Override
    public void onPause() {
        redrawer.pause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        redrawer.finish();
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        if (mySensor.getType() != Sensor.TYPE_ORIENTATION) return;

        // levels (idk why they did it this way)
        /*xLvlSeries.setModel(Arrays.asList(
                new Number[]{sensorEvent.values[0], 0.1, 0.1}),
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
        yLvlSeries.setModel(Arrays.asList(
                new Number[]{0.1, sensorEvent.values[1], 0.1}),
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
        zLvlSeries.setModel(Arrays.asList(
                new Number[]{0.1, 0.1, sensorEvent.values[2]}),
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);*/

        // history
        if(xHistorySeries.size() > HISTORY_SIZE) {
            xHistorySeries.removeFirst();
            yHistorySeries.removeFirst();
            zHistorySeries.removeFirst();
        }

        xHistorySeries.addLast(null, sensorEvent.values[0]);
        yHistorySeries.addLast(null, sensorEvent.values[1]);
        zHistorySeries.addLast(null, sensorEvent.values[2]);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void saveData(View view) {
        Log.d(TAG, "Saving data");

        if(!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Toast.makeText(this, "External SD Card is not mounted.", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Not mounted");
        } else Log.d(TAG, "External SD is mounted");

        if (Build.VERSION.SDK_INT >= 23) {
            // what does this do???
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission is granted");

                prepData(view.getTag().toString());

            } else {
                Log.d(TAG, "Permission denied, requesting permission");

                int MY_PERMISSION_CODE = 1;
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, view.getTag().toString()},
                        MY_PERMISSION_CODE);


            }

            return;
        } else {

            prepData(view.getTag().toString());
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission: " + permissions[0] + " was " + grantResults[0]);

            prepData(permissions[1]);

        } else {
            Log.d(TAG, "Permission denied");
        }
    }

    private void prepData(String tag) {
        // begin code block
        String csvFile = "Index,X,Y,Z\n";
        for (int i=0; i < xHistorySeries.size(); i++) {
            csvFile += "" + i;
            csvFile += "," + xHistorySeries.getY(i);
            csvFile += "," + yHistorySeries.getY(i);
            csvFile += "," + zHistorySeries.getY(i);
            csvFile += "\n";
        }
        String allData = xHistorySeries.getyVals().toString();
        //allData += yHistorySeries.toString() + zHistorySeries.toString();

        String filename = tag + "_" + (new Random()).nextInt(1000) + ".csv";
        addLog(filename, csvFile, false);
        // end code block
    }

    private void addLog(String filename, String text, boolean timestamp)
    {

        String extStore = System.getenv("EXTERNAL_STORAGE");
        String location = extStore + "/UFSS/" + filename;
        Log.d(TAG, "Logging to external storage " + location);
        File f_exts = new File(extStore);

        File logFile = new File(location);
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));

            if(timestamp) {
                // Getting the current timestamp
                Long tsLong = System.currentTimeMillis();
                String ts = tsLong.toString();

                buf.append(ts + ";"); //Adding timestamp to everything is good practice
            }
            buf.append(text);
            buf.append("\n");
            buf.close();
            Log.d(TAG, "Write to " + location + " was successful");
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}