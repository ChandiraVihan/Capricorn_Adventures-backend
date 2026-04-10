package com.capricorn_adventures.service;

import org.springframework.stereotype.Service;

@Service
public class DistanceMockService {

    /**
     * Deterministically algorithm to generate a mock distance based on the inputs.
     */
    public DistanceResult calculateDistance(String adventureLocation, Double userLat, Double userLng, String userCity) {
        long hash = 0;

        if (adventureLocation != null) {
            hash += adventureLocation.toLowerCase().hashCode();
        }

        if (userCity != null && !userCity.trim().isEmpty()) {
            hash += userCity.toLowerCase().hashCode();
        } else if (userLat != null && userLng != null) {
            hash += (long) (userLat * 1000) + (long) (userLng * 1000);
        } else {
            // Cannot calculate distance if no user location is provided
            return null;
        }

        // Magic hash based modulus to get a deterministic distance between 2.0 km and
        // 150.0 km
        long magicNumber = Math.abs(hash) % 1480;
        double distanceKm = 2.0 + (magicNumber / 10.0);

        // Round to 1 decimal place
        distanceKm = Math.round(distanceKm * 10.0) / 10.0;

        // Assume average driving speed of 50 km/h in Sri Lanka with varying traffic
        int timeMinutes = (int) Math.round((distanceKm / 50.0) * 60);

        // Add some random wait traffic time between 0-20 mins
        timeMinutes += Math.abs(hash) % 20;

        String estimatedTravelTime;
        if (timeMinutes < 60) {
            estimatedTravelTime = timeMinutes + " mins";
        } else {
            int hrs = timeMinutes / 60;
            int mins = timeMinutes % 60;
            estimatedTravelTime = hrs + " hr" + (hrs > 1 ? "s" : "") + " " + mins + " mins";
        }

        return new DistanceResult(distanceKm, estimatedTravelTime);
    }

    public static class DistanceResult {
        private final Double distanceKm;
        private final String estimatedTravelTime;

        public DistanceResult(Double distanceKm, String estimatedTravelTime) {
            this.distanceKm = distanceKm;
            this.estimatedTravelTime = estimatedTravelTime;
        }

        public Double getDistanceKm() {
            return distanceKm;
        }

        public String getEstimatedTravelTime() {
            return estimatedTravelTime;
        }
    }
}
