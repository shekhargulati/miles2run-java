package org.miles2run.business.utils;

import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.*;

import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Created by shekhargulati on 12/03/14.
 */
public abstract class GeocoderUtils {

    private static Logger logger = Logger.getLogger(GeocoderUtils.class.getName());

    public static CityAndCountry parseLocation(String location) {
        logger.info("Location '" + location + "'");
        if (location == null || "".equals(location.trim())) {
            return new CityAndCountry(null, null);
        }
        try {
            String city = null;
            String country = null;
            Geocoder geocoder = new Geocoder();
            GeocoderRequest geocoderRequest = new GeocoderRequestBuilder().setAddress(location).setLanguage("en").getGeocoderRequest();
            GeocodeResponse geocodeResponse = geocoder.geocode(geocoderRequest);
            List<GeocoderResult> results = geocodeResponse.getResults();
            for (GeocoderResult result : results) {
                List<GeocoderAddressComponent> addressComponents = result.getAddressComponents();
                for (GeocoderAddressComponent addressComponent : addressComponents) {
                    if (addressComponent.getTypes().contains("locality")) {
                        city = addressComponent.getShortName();
                    }
                    if (addressComponent.getTypes().contains("country")) {
                        country = addressComponent.getShortName();
                    }
                }
            }
            return new CityAndCountry(city, country);
        } catch (Exception e) {
            return new CityAndCountry(null, null);
        }

    }

    public static double[] lngLat(String city, String country) {
        String countryDisplayName = toDisplayName(country);
        return findLngLat(new StringBuilder(city).append(" , ").append(countryDisplayName).toString());
    }

    private static String toDisplayName(String countryCode) {
        return new Locale("", countryCode).getDisplayCountry();
    }

    private static double[] findLngLat(String location) {
        try {
            final Geocoder geocoder = new Geocoder();
            GeocoderRequest geocoderRequest = new GeocoderRequestBuilder()
                    .setAddress(location).setLanguage("en").getGeocoderRequest();
            GeocodeResponse geocoderResponse = geocoder.geocode(geocoderRequest);

            if (geocoderResponse.getResults() == null || geocoderResponse.getResults().isEmpty()) {
                return new double[0];
            }

            GeocoderResult geocoderResult = geocoderResponse.getResults().get(0);
            LatLng latLng = geocoderResult.getGeometry().getLocation();
            return new double[]{latLng.getLng().doubleValue(),
                    latLng.getLat().doubleValue()};
        } catch (Exception e) {
            return new double[0];
        }

    }
}
