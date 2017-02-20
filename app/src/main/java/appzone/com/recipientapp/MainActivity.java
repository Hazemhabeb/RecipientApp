package appzone.com.recipientapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;

import org.json.JSONException;
import org.json.JSONObject;

import appzone.com.recipientapp.Helper.PubNubManager;

public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback {


    // =============================================================================================
    // Properties
    // =============================================================================================

    private static final String TAG = "Tracker - GMaps Share";
    private static final String PUBNUB_TAG = "PUBNUB";
    private boolean isFirstMessage = true;

    // Google API - Locations
    private GoogleApiClient mGoogleApiClient;

    // Google Maps
    private GoogleMap mGoogleMap;
    private PolylineOptions mPolylineOptions;
    private MarkerOptions mMarkerOptions;
    private Marker mMarker;
    private LatLng mLatLng;


    // PubNub
    private Pubnub mPubnub;
    private String channelName = "location";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Set up View: Map
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // the user didn't get the permission so they can't start or stop the location update
        stopFollowingLocation();
    }

    // to check if the GPS and network is opened or not
    private void locationCheck() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            dialog.setMessage(MainActivity.this.getResources().getString(R.string.gps_network_not_enabled));
            dialog.setPositiveButton(MainActivity.this.getResources().getString(R.string.open_location_settings),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            // TODO Auto-generated method stub
                            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(myIntent);
                            //get gps
                        }
                    });
            dialog.setNegativeButton(getString(R.string.Cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub

                }
            });
            dialog.setCancelable(false);
            dialog.show();
        }
    }

    // =========================================================================
    // Map CallBacks
    // =========================================================================

    @Override
    public void onMapReady(GoogleMap map) {
        mGoogleMap = map;
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
        mGoogleMap.setMyLocationEnabled(true);
        Log.d(TAG, "Map Ready");

        locationCheck();

        startFollowingLocation();

    }

    private void startFollowingLocation() {
        initializePolyline();
        mPubnub = PubNubManager.startPubnub();
        PubNubManager.subscribe(mPubnub, channelName, subscribeCallback);
    }

    private void stopFollowingLocation() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mPubnub.unsubscribe(channelName);
            isFirstMessage = true;
        }
    }

    // =========================================================================
    // Map Editing Methods
    // =========================================================================

    private void initializePolyline() {
        mGoogleMap.clear();
        mPolylineOptions = new PolylineOptions();
        mPolylineOptions.color(Color.BLUE).width(10);
        mGoogleMap.addPolyline(mPolylineOptions);

        mMarkerOptions = new MarkerOptions();
    }

    private void updatePolyline() {
        mPolylineOptions.add(mLatLng);
        mGoogleMap.clear();
        mGoogleMap.addPolyline(mPolylineOptions);
    }

    private void updateCamera() {
        mGoogleMap
                .animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 16));
    }

    private void updateMarker() {
//		if (!isFirstMessage) {
//			isFirstMessage = false;
//			mMarker.remove();
//		}
        mMarker = mGoogleMap.addMarker(mMarkerOptions.position(mLatLng));
    }

    // =========================================================================
    // PubNub Callback
    // =========================================================================

    Callback subscribeCallback = new Callback() {

        @Override
        public void successCallback(String channel, Object message) {
            Log.d(PUBNUB_TAG, "Message Received: " + message.toString());
            JSONObject jsonMessage = (JSONObject) message;
            try {
                double mLat = jsonMessage.getDouble("lat");
                double mLng = jsonMessage.getDouble("lng");
                mLatLng = new LatLng(mLat, mLng);
            } catch (JSONException e) {
                Log.e(TAG, e.toString());
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updatePolyline();
                    updateCamera();
                    updateMarker();
                }
            });
        }
    };
}
