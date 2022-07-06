package de.jo.boattracker.main;

import android.Manifest;
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

    TextView lon;
    TextView lat;
    TextView kn;
    TextView maxSpeed;
    TextView heading;

    double maxSpeedD = 0.00D;

    float degreeStart = 0f;

    double oldLat = -1;
    double oldLong = -1;
    long oldTime = -1;


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
        lon = findViewById(R.id.Long);
        lat = findViewById(R.id.Lat);
        kn = findViewById(R.id.kn);
        maxSpeed = findViewById(R.id.maxSpeed);
        heading = findViewById(R.id.heading);

        lon.setTextSize(16f);
        lat.setTextSize(16f);
        lon.setText("Lon: ");
        lat.setText("Lat: ");
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

        assert lon != null;
        assert lat != null;
        assert kn != null;
        assert maxSpeed != null;
        assert heading != null;
        lon.setText(String.format("Lon: %s", location.getLongitude()));
        lat.setText(String.format("Lat: %s", location.getLatitude()));
        if (location.getAccuracy() <= 16) {
            if (oldLat != -1 && oldLong != -1 && oldTime != -1) {
                DecimalFormat df = new DecimalFormat("#0,00");
                double speed = getSpeed(calculateDistance(oldLat, oldLong, location.getLatitude(), location.getLongitude()), oldTime, location.getTime());
                double speedKnots = SpeedConverter.getMetersPerSecondToKnots(getSpeed(calculateDistance(oldLat, oldLong, location.getLatitude(), location.getLongitude()), oldTime, location.getTime()));
                kn.setText(String.format("Kn: %s M/S: %s", df.format(speedKnots), df.format(speed)));
                maxSpeed.setText(String.format("Max Kn: %s", df.format(maxSpeedD)));

                if (speed > maxSpeedD) {
                    maxSpeedD = speed;
                }
            }
            oldTime = System.currentTimeMillis();
            oldLat = location.getLatitude();
            oldLong = location.getLongitude();

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
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 2, this);
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

    //https://stackoverflow.com/questions/20398898/how-to-get-speed-in-android-app-using-location-or-accelerometer-or-some-other-wa
    private long calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return Math.round(6371000 * c);
    }

    private long getSpeed(long dist, long oldTime, long time) {
        return  dist / (time - oldTime);
    }
}