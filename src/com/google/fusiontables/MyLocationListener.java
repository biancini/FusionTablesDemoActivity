// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.fusiontables;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

/**
 * Location listener - gets updates from GPS to set current location.
 * 
 * @author kbrisbin@google.com (Kathryn Hurley)
 */
public class MyLocationListener implements LocationListener {
  private Location location;
  private String address = "";
  private FusionTablesDemoActivity activity;
  private static final int TWO_MINUTES = 1000 * 60 * 2;

  /**
   * Constructor, sets the activity.
   * 
   * @param act the FusionTablesDemoActivity
   */
  public MyLocationListener(FusionTablesDemoActivity act) {
    this.activity = act;
  }

  /**
   * Set the new location when a new location is acquired.
   * 
   * @param location the new location
   */
  public void onLocationChanged(Location location) {
    setLocation(location);
  }

  /**
   * Sets the location if the new one is more accurate or timely than
   * the current one..
   * 
   * @param location the new location
   */
  public void setLocation(Location location) {
    // Only change the location if the new one is better
    if (isBetterLocation(location, this.location)) {
      Geocoder gc = new Geocoder(this.activity, Locale.getDefault());
      try {
        // Get the first address in the list of possible addresses for
        // the lat/lon coordinates
        List<Address> addresses = gc.getFromLocation(location.getLatitude(),
            location.getLongitude(), 1);

        // If an address was returned, concatenate the results into
        // a string
        StringBuilder formattedAddress = new StringBuilder();
        if (!addresses.isEmpty()) {
          Address address = addresses.get(0);

          for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
            formattedAddress.append(address.getAddressLine(i) + " ");
          }

          this.location = location;
          this.address = formattedAddress.toString();
          this.activity.setLocationText(this.address);
          this.activity.enableButton(true);
        }
      } catch (IOException e) {
      }
    }
  }

  /**
   * Returns location.
   */
  public Location getLocation() {
    return this.location;
  }

  /**
   * Returns string-formatted location.
   */
  public String getStringLocation() {
    return this.location.getLatitude() + ", " + this.location.getLongitude();
  }

  /**
   * Returns address.
   */
  public String getAddress() {
    return this.address;
  }

  /**
   * Does nothing when the provider is disabled.
   */
  public void onProviderDisabled(String provider) {
  }

  /**
   * Does nothing when the provider is enabled.
   */
  public void onProviderEnabled(String provider) {
  }

  /**
   * Does nothing when the status has changed.
   */
  public void onStatusChanged(String provider, int status, Bundle extras) {
  }

  /**
   * Returns true is the new {@link Location} is better than the current one.
   * 
   * @param location the new Location
   * @param currentBestLocation the current Location, to which you
   *        want to compare the new one
   * 
   * @return true if the new {@link Location} is more recent or more accurate
   */
  protected boolean isBetterLocation(Location newLocation, Location currentBestLocation) {
    if (currentBestLocation == null) {
      return true;
    }

    // Check whether the new location fix is newer or older
    long timeDelta = newLocation.getTime() - currentBestLocation.getTime();
    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;

    // If it's been more than two minutes since the current location,
    // use the new location because the user has likely moved
    if (isSignificantlyNewer) {
      return true;
    } else if (isSignificantlyOlder) {
      // If the new location is more than two minutes older, it must be worse
      return false;
    }

    // Check whether the new location fix is more or less accurate
    int accuracyDelta = (int) (newLocation.getAccuracy() - currentBestLocation
        .getAccuracy());
    boolean isMoreAccurate = accuracyDelta < 0;

    // Determine location quality using the accuracy
    if (isMoreAccurate) {
      return true;
    }
    return false;
  }
}
