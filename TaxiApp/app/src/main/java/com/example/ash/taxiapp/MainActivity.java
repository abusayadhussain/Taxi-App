package com.example.ash.taxiapp;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.renderscript.RenderScript;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Printer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    public static final String TAG = "TAG";
    private static final int REQUEST_CODE = 1000;

    private GoogleApiClient googleApiClient;

    private EditText edtAddress;
    private EditText edtMilesPerHour;
    private EditText edtMetersPerMile;

    private TextView txtDistance;
    private TextView txtTime;

    private Button btnGetTheData;

    private String destinationLocationAddress = "";

    private TaxiManager taxiManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtAddress = findViewById(R.id.edtAddress);
        edtMilesPerHour = findViewById(R.id.edtMilesPerHour);
        edtMetersPerMile = findViewById(R.id.edtMetersPerMIle);

        txtDistance = findViewById(R.id.txtDistanceValue);
        txtTime = findViewById(R.id.txtTime);

        btnGetTheData = findViewById(R.id.btnGetTheData);

        btnGetTheData.setOnClickListener(this);

        taxiManager = new TaxiManager();


        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

    }

    @Override
    protected void onPause() {
        super.onPause();

        FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
        fusedLocationProviderApi.removeLocationUpdates(googleApiClient, new com.google.android.gms.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                onClick(null);

            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {

        Log.d(TAG, "We are connected top the user location");

        FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setSmallestDisplacement(5);

        if (googleApiClient.isConnected()) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            fusedLocationProviderApi.requestLocationUpdates(googleApiClient, locationRequest, new com.google.android.gms.location.LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                    onClick(null);

                }
            });
        } else{

            googleApiClient.connect();
        }


    }

    @Override
    public void onConnectionSuspended(int i) {

        Log.d(TAG, "The Connection is suspended");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.d(TAG, "The connection failed");

        if(connectionResult.hasResolution()){

            try {
                connectionResult.startResolutionForResult(this, REQUEST_CODE);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else{

            Toast.makeText(this, "Google play services is not working. EXIT!", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE && requestCode == RESULT_OK){

            googleApiClient.connect();

        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(googleApiClient != null){

            googleApiClient.connect();

        }
    }


    @Override
    public void onClick(View v) {

        String addressValue = edtAddress.getText().toString();

        boolean isGeoCoding = true;

        if(!addressValue.equals(destinationLocationAddress)){

             destinationLocationAddress = addressValue;

            Geocoder geocoder = new Geocoder(getApplicationContext());

            try{

                List<Address> myAddresses = geocoder.getFromLocationName(destinationLocationAddress, 4);

                if(myAddresses != null){

                    double latitude = myAddresses.get(0).getLatitude();
                    double longitude = myAddresses.get(0).getLongitude();

                    Location locationAddresses = new Location("MyDestination");
                    locationAddresses.setLatitude(latitude);
                    locationAddresses.setLongitude(longitude);
                    taxiManager.setDestinationLocation(locationAddresses);

                }

            } catch (Exception e){

                isGeoCoding = false;

                e.printStackTrace();
            }
        }

        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);

        if(permissionCheck == PackageManager.PERMISSION_GRANTED){


            FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
            Location userCurrentLocation = fusedLocationProviderApi.getLastLocation(googleApiClient);

            if(userCurrentLocation != null && isGeoCoding){


                txtDistance.setText(taxiManager.returnTheMilesBetweenCurrentLocationAndDestinationLocation(userCurrentLocation, Integer.parseInt(edtMetersPerMile.getText().toString())));

                txtTime.setText(taxiManager.returnTheTimeLeftToGetToDestinationLocation(userCurrentLocation, Float.parseFloat(edtMilesPerHour.getText().toString()), Integer.parseInt(edtMetersPerMile.getText().toString())));

            }

        } else{

            txtDistance.setText("This app is not allowed to access the location");

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},1);
        }
    }


}
