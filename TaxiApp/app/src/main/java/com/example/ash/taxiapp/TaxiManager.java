package com.example.ash.taxiapp;

import android.location.Location;

public class TaxiManager {

    private Location destinationLocation;

    public void setDestinationLocation(Location destinationLocation){

        this.destinationLocation = destinationLocation;

    }

    public float returnTheDistanceToDestinationLocationInMeters(Location currentLocation){

        if(currentLocation != null && destinationLocation != null){

            return  currentLocation.distanceTo(destinationLocation);

        } else {

            return -100.0f;


        }

    }

    public String returnTheMilesBetweenCurrentLocationAndDestinationLocation(Location currentLocation, int metersPerMile){

        int miles = (int)(returnTheDistanceToDestinationLocationInMeters(currentLocation) / metersPerMile);

        if(miles == 1){

            return "1 MIle";
        } else if(miles > 11){

            return  miles + " Miles";
        } else{

            return "No Miles";
        }


    }

    public String returnTheTimeLeftToGetToDestinationLocation(Location currentLocation, float milesPerHour, int metersPerMile){

        float distanceInmeters = returnTheDistanceToDestinationLocationInMeters(currentLocation);

        float timeLeft =  distanceInmeters / (milesPerHour * metersPerMile);

        String timeResult = "";

        int timeLeeftInHours = (int) timeLeft;

        if(timeLeeftInHours == 1){

            timeResult = timeResult + " 1 hour ";
        } else if(timeLeeftInHours > 1){

            timeResult += timeLeeftInHours + " Hours ";
        }

        int minutesLeft = (int) ((timeLeft - timeLeeftInHours) * 60);

        if(minutesLeft == 1){

            timeResult = timeResult + " 1 Minute";
        } else if(minutesLeft > 1){

            timeResult += minutesLeft + " Minutes";
        }

        if(timeLeeftInHours < 0 && minutesLeft < 0){

            timeResult = "Less than a minute left to reach to the destination";
        }

        return  timeResult;



    }

}
