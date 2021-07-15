package sg.edu.rp.c346.id19004781.c347_p09_ps1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import android.location.Location;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.FileWriter;

public class MainActivity extends AppCompatActivity {

    private GoogleMap map;
    TextView tvLatitude, tvLongtitude;
    Button btnGetLocationUpdate, btnRemoveLocationUpdate, btnCheckRecords;
    LocationRequest mLocationRequest;
    FusedLocationProviderClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnCheckRecords = findViewById(R.id.btnCheckRecords);
        btnGetLocationUpdate = findViewById(R.id.btnGetLocationUpdate);
        btnRemoveLocationUpdate = findViewById(R.id.btnRemoveLocationUpdate);
        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongtitude = findViewById(R.id.tvLongtitude);

        String[] permission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(MainActivity.this, permission, 0);
        if(checkPermission()){
            client = LocationServices.getFusedLocationProviderClient(MainActivity.this);
            Task<Location> task = client.getLastLocation();
            task.addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    String msg = "";
                    if (location != null) {
                        tvLatitude.setText("Latitude: " + location.getLatitude());
                        tvLongtitude.setText("Longitude: " + location.getLongitude());
                        msg = "Marker Exists";
                    } else {
                        msg = "No last known location found";
                    }
                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            });
        }

        FragmentManager fm = getSupportFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                UiSettings ui = map.getUiSettings();
                ui.setCompassEnabled(true);
                ui.setZoomControlsEnabled(true);
                int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION);

                if (permissionCheck == PermissionChecker.PERMISSION_GRANTED) {
                    map.setMyLocationEnabled(true);
                } else {
                    Log.e("GMap - Permission", "GPS access has not been granted");
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                }
            }
        });

        mLocationRequest = new LocationRequest();


        LocationCallback mLocationCallback = new LocationCallback() {
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Location data = locationResult.getLastLocation();
                    double lat = data.getLatitude();
                    double log = data.getLongitude();
                    try {
                        String folderLocation =
                                Environment.getExternalStorageDirectory()
                                        .getAbsolutePath() + "/Folder";
                        File folder = new File(folderLocation);
                        if (folder.exists() == false) {
                            boolean result = folder.mkdir();
                            if (result == true) {
                                Log.d("File Read/Write", "Folder created");
                            }
                        }
                        try {
                            folderLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Folder";
                            File targetFileExternal = new File(folderLocation, "data2.txt");
                            FileWriter writer = new FileWriter(targetFileExternal, true);
                            writer.write(lat + ", " + log+"\n");
                            writer.flush();
                            writer.close();
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, "Failed to write!", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Failed to create folder!", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                    Toast.makeText(MainActivity.this, lat + "\n" + log, Toast.LENGTH_SHORT).show();
                }
            }
        };
        btnGetLocationUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermission() == true) {
                    mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    mLocationRequest.setInterval(30000);
                    mLocationRequest.setFastestInterval(500);
                    mLocationRequest.setSmallestDisplacement(500);
                    client = LocationServices.getFusedLocationProviderClient(MainActivity.this);
                    client.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
            }
        });


        btnRemoveLocationUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermission() == true) {
                    client.removeLocationUpdates(mLocationCallback);
                    Toast.makeText(MainActivity.this, "Remove successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
            }
        });


        btnCheckRecords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        if(checkPermission()){
            client = LocationServices.getFusedLocationProviderClient(MainActivity.this);
            Task<Location> task = client.getLastLocation();
            task.addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    String msg = "";
                    if (location != null) {
                        tvLatitude.setText("Latitude: " + location.getLatitude());
                        tvLongtitude.setText("Longitude: " + location.getLongitude());
                        //msg = "Marker Exists again";
                    } else {
                        msg = "No last known location found";
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Log.e("GMap - Permission", "GPS access has not been granted");
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
    }


    private boolean checkPermission() {
        int permissionCheck_Fine = ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck_Fine == PermissionChecker.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return false;
        }
    }
}