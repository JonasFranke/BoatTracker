package de.jo.boattracker.main;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import de.jo.boattracker.R;
import de.jo.boattracker.util.GPX;
import de.jo.boattracker.util.SpeedConverter;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LocationListener, SensorEventListener {

    LocationManager locationManager;
    private SensorManager SensorManage;

    final static String[] PERMS = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    final static int PERM_ALL = 1;

    TextView loc, kn, maxSpeed, avgSpeed, heading, acc;
    Button write;

    float maxSpeedD = 0.00f;
    private final ArrayList<Float> speeds = new ArrayList<>();

    float degreeStart = 0f;

    private GPX gpx;

    private static Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gpx = new GPX(getFilesDir());

        init();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        requestPermissions(PERMS, PERM_ALL);

        SensorManage = (SensorManager) getSystemService(SENSOR_SERVICE);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        context = this;
    }

    private void init() {
        loc = findViewById(R.id.LongLat);
        kn = findViewById(R.id.kn);
        maxSpeed = findViewById(R.id.maxSpeed);
        avgSpeed = findViewById(R.id.avgSpeed);
        heading = findViewById(R.id.heading);
        acc = findViewById(R.id.acc);
        write = findViewById(R.id.writeButton);

        loc.setTextSize(16f);
        loc.setText("Lon:  Lat:");
        kn.setTextSize(85f);
        kn.setText("Kn: ");
        maxSpeed.setText("Max Kn: ");
        maxSpeed.setTextSize(50f);
        avgSpeed.setText("Avg. Kn: ");
        avgSpeed.setTextSize(50f);
        heading.setTextSize(65f);
        heading.setText("0");

        speeds.clear();

        write.setOnClickListener(gpx);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        try {
            assert loc != null;
            assert kn != null;
            assert heading != null;
            loc.setText(String.format("Lon: %s Lat: %s", location.getLongitude(), location.getLatitude()));
            if (location.getAccuracy() <= 16 && location.hasSpeed()) {
                float speed = location.getSpeed();
                float speedKnots = SpeedConverter.getMetersPerSecondToKnots(speed);
                kn.setText(String.format("Kn: %s", SpeedConverter.shortenSpeed(speedKnots)));
                maxSpeed.setText(String.format("Max Kn: %s", maxSpeedD));
                avgSpeed.setText(String.format("Avg. Kn: %s", calcAvg(speeds)));

                if (speed > maxSpeedD) {
                    maxSpeedD = speed;
                } else if (maxSpeedD == 0.00f) {
                    maxSpeedD = speed;
                }

                speeds.add(SpeedConverter.shortenSpeed(speedKnots));

                gpx.addLocation(location);
            } else {
                Toast.makeText(this, "No Loc :(", Toast.LENGTH_SHORT).show();
                Log.w("GPS","NO Speed");
            }
        } catch (Exception e) {
            Log.e("GPS", e.getMessage());
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        // to stop the listener and save battery
        SensorManage.unregisterListener(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        // code for system's orientation sensor registered listeners
        SensorManage.registerListener(this, SensorManage.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] perms, @NonNull int[] grantResult) {
        super.onRequestPermissionsResult(requestCode, perms, grantResult);
        if (grantResult.length > 0 && grantResult[0] == PackageManager.PERMISSION_GRANTED) {
            requestLocation();
        }
    }

    public void requestLocation() {
        try {
            assert locationManager != null;
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "loc", Toast.LENGTH_SHORT).show();
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, this);
                }
            }
        } catch (Exception e) {
            Log.e("GPS", e.getMessage());
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        try {
            float degree = Math.round(event.values[0]);
            heading.setText(String.format("%s°", (int) degree));
            degreeStart = -degree;
        } catch (Exception e) {
            Log.e("GPS", e.getMessage());
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //Set acc to accuracy
        acc.setText(String.format("Acc: %s m", i));
    }



    private String calcAvg(ArrayList<Float> speeds) {
        float sum = 0;
        for (float speed : speeds) {
            sum += speed;
        }
        float avg = sum / speeds.size();
        DecimalFormat df = new DecimalFormat("#.##");

        if (speeds.size() > 200) {
            clearList(avg);
        }

        return df.format(avg);
    }

    private void clearList(float currentAvg) {
        speeds.clear();
        speeds.add(currentAvg);
    }

    public static Context getContext() {
        assert context != null;
        return context;
    }

}