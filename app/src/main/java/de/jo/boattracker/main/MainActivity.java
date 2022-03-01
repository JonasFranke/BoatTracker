package de.jo.boattracker.main;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import de.jo.boattracker.R;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements LocationListener {

    LocationManager locationManager;

    final static String[] PERMS = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    final static int PERM_ALL = 1;

    TextView lon;
    TextView lat;
    TextView kn;
    TextView maxSpeed;
    TextView heading;

    double maxSpeedD = 0.00D;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        requestPermissions(PERMS, PERM_ALL);
    }

    private void init() {
        lon = findViewById(R.id.Long);
        lat = findViewById(R.id.Lat);
        kn = findViewById(R.id.kn);
        maxSpeed = findViewById(R.id.maxSpeed);
        heading = findViewById(R.id.heading);

        lon.setY(10);
        lon.setX(80);
        lon.setTextSize(20f);
        lat.setY(10);
        lat.setX(580);
        lat.setTextSize(20f);
        lon.setText("Lon: ");
        lat.setText("Lat: ");
        kn.setY(50);
        kn.setX(80);
        kn.setTextSize(85f);
        kn.setText("Kn: ");
        maxSpeed.setText("Max Kn: ");
        maxSpeed.setTextSize(50f);
        maxSpeed.setX(80);
        maxSpeed.setY(290);
        heading.setX(85);
        heading.setY(500);
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
            DecimalFormat df = new DecimalFormat("#0,00");
            double speed = location.getSpeed() * 0.5399568035;
            kn.setText(String.format("Kn: %s", df.format(speed)));
            maxSpeed.setText(String.format("Max Kn: %s", df.format(maxSpeedD)));

            heading.setText(String.valueOf(location.getBearing()));

            if (speed > maxSpeedD) {
                maxSpeedD = speed;
            }
        }

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
}