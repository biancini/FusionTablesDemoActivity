// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.fusiontables;

import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.fusiontables.ftclient.ClientLogin;
import com.google.fusiontables.ftclient.FtClient;

/**
 * Main activity class.
 * 
 * @author kbrisbin@google.com (Kathryn Hurley)
 */
public class FusionTablesDemoActivity extends Activity {

  private Button button;

  private MyLocationListener locationListener;
  private LocationManager locationManager;
  private String locationProvider = LocationManager.GPS_PROVIDER;

  private FtClient ftclient;
  private long tableid = 123456;
  private String username = "<username>";
  private String password = "<password>";
  private String embedLink = "http://gmaps-samples.googlecode.com/svn/trunk/"
      + "fusiontables/potholes.html";
  
  private String TAG = "FusionTablesDemoActivity";

  /**
   * Authorizes the user and initializes the UI.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    // Initialize FTClient
    String token = ClientLogin.authorize(username, password);
    ftclient = new FtClient(token);

    initSpinner();
    initButton();
    initLocation();
  }

  /**
   * Sets the button to enabled or disabled.
   * 
   * @param enabled true to enable the button, false to disable
   */
  public void enableButton(boolean enabled) {
    button.setEnabled(enabled);
  }

  /**
   * Updates location text view.
   */
  public void setLocationText(String address) {
    TextView tv = (TextView) findViewById(R.id.location);
    tv.setText(address);
  }

  /**
   * Stops GPS reading when the app is paused.
   */
  @Override
  protected void onPause() {
    super.onPause();
    locationManager.removeUpdates(locationListener);
  }

  /**
   * Starts GPS readings when the app resumes.
   */
  @Override
  protected void onResume() {
    super.onResume();
    requestUpdates();
    enableButton(true);
  }

  /**
   * Initializes the spinner menu.
   */
  private void initSpinner() {
    Spinner spinner = (Spinner) findViewById(R.id.status);
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
        R.array.statuses, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(
        android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);
  }

  /**
   * Initializes the button and the click event.
   */
  private void initButton() {
    button = (Button) findViewById(R.id.submitform);

    // Disable until GPS reading is obtained.
    enableButton(false);

    button.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        // Tell the user thanks
        Toast.makeText(FusionTablesDemoActivity.this, "Thanks for reporting!", 10).show();

        // Open the map in a browser
        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
            Uri.parse(embedLink));
        startActivity(browserIntent);

        // Get the current spinner selection
        Spinner spinner = (Spinner) findViewById(R.id.status);
        long state = spinner.getSelectedItemId();

        // Generate INSERT statement
        StringBuilder insert = new StringBuilder();
        insert.append("INSERT INTO ");
        insert.append(tableid);
        insert.append(" (Severity, Location, Address, Timestamp) VALUES ");
        insert.append("(");
        insert.append(state);
        insert.append(", '");
        insert.append(locationListener.getStringLocation());
        insert.append("', '");
        insert.append(locationListener.getAddress());
        insert.append("', ");
        insert.append(new Date().getTime());
        insert.append(")");

        // Save the data to Fusion Tables
        ftclient.query(insert.toString());
      }
    });
  }
  
  /**
   * try to get the 'best' location selected from all providers
   */
  private Location getBestLocation() {
      Location gpslocation = getLocationByProvider(LocationManager.GPS_PROVIDER);
      Location networkLocation = getLocationByProvider(LocationManager.NETWORK_PROVIDER);

      //if we have only one location available, the choice is easy
      if (gpslocation == null) {
          Log.d(TAG, "No GPS Location available.");
          return networkLocation;
      }
      if (networkLocation == null) {
          Log.d(TAG, "No Network Location available");
          return gpslocation;
      }

      // both are old return the newer of those two
      if (gpslocation.getTime() > networkLocation.getTime()) {
          Log.d(TAG, "Both are old, returning gps(newer)");
          return gpslocation;
      } else {
          Log.d(TAG, "Both are old, returning network(newer)");
          return networkLocation;
      }
  }
  
  /**
   * get the last known location from a specific provider (network/gps)
   */
  private Location getLocationByProvider(String provider) {
      Location location = null;

      try {
          if (locationManager.isProviderEnabled(provider)) {
              location = locationManager.getLastKnownLocation(provider);
          }
      } catch (IllegalArgumentException e) {
          Log.d(TAG, "Cannot acces Provider " + provider);
      }
      return location;
  }

  /**
   * Initializes the location listener.
   */
  private void initLocation() {
    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    //Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
	Location lastKnownLocation = getBestLocation();
    
    Toast.makeText(this, "lastKnownLocation: "+lastKnownLocation, 15).show();
    
    locationListener = new MyLocationListener(this);
    if (lastKnownLocation != null) {
      locationListener.setLocation(lastKnownLocation);
    }
    setLocationText(locationListener.getAddress());

    requestUpdates();
  }

  /**
   * Requests location updates.
   */
  private void requestUpdates() {
    Toast.makeText(this, "Getting location...", 15).show();
    locationManager.requestLocationUpdates(locationProvider, 5000, 1,
        locationListener);
  }
}
