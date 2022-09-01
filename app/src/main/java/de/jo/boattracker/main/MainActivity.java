package de.jo.boattracker.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import de.jo.boattracker.R;
import de.jo.boattracker.util.SpeedConverter;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements LocationListener, SensorEventListener {

    LocationManager locationManager;
    private SensorManager SensorManage;

    final static String[] PERMS = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    final static int PERM_ALL = 1;

    TextView loc;
    TextView kn;
    TextView maxSpeed;
    TextView heading;

    float maxSpeedD = 0.00f;

    float degreeStart = 0f;

    double oldLat = -1;
    double oldLong = -1;
    long oldTime = -1;
    Location oldLoc;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        requestPermissions(PERMS, PERM_ALL);

        SensorManage = (SensorManager) getSystemService(SENSOR_SERVICE);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void init() {
        loc = findViewById(R.id.LongLat);
        kn = findViewById(R.id.kn);
        maxSpeed = findViewById(R.id.maxSpeed);
        heading = findViewById(R.id.heading);

        loc.setTextSize(16f);
        loc.setText("Lon:  Lat:");
        kn.setTextSize(85f);
        kn.setText("Kn: ");
        maxSpeed.setText("Max Kn: ");
        maxSpeed.setTextSize(50f);
        heading.setTextSize(65f);
        heading.setText("0");
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        System.out.println(location.getLatitude());

        assert loc != null;
        assert kn != null;
        assert heading != null;
        loc.setText(String.format("Lon: %s Lat: %s", location.getLongitude(), location.getLatitude()));
        if (location.getAccuracy() <= 16 && location.hasSpeed()) {
            float speed = location.getSpeed();
            float speedKnots = SpeedConverter.getMetersPerSecondToKnots(speed);
            kn.setText(String.format("Kn: %s", SpeedConverter.shortenSpeed(speedKnots)));
            maxSpeed.setText(String.format("Max Kn: %s", maxSpeedD));

            if (speed > maxSpeedD) {
                maxSpeedD = speed;
            } else if (maxSpeedD == 0.00f) {
                maxSpeedD = speed;//TODO: Avg Speed
            }

        } else {
            Toast.makeText(this, "No Loc :(", Toast.LENGTH_SHORT).show();
            System.out.println("NO Speed");
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
        assert locationManager != null;
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "loc", Toast.LENGTH_SHORT).show();
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, this);
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float degree = Math.round(event.values[0]);
        heading.setText(String.format("%s°", (int) degree));
        degreeStart = -degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

}