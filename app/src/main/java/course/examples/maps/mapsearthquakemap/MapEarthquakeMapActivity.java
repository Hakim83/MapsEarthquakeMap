package course.examples.maps.mapsearthquakemap;

import android.net.ParseException;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.net.URL;

public class MapEarthquakeMapActivity extends FragmentActivity implements OnMapReadyCallback {

    // Coordinates used for centering the Map

    private static final double CAMERA_LNG = 87.0;
    private static final double CAMERA_LAT = 17.0;

    // URL for getting the earthquake
    // replace with your own user name

    private final static String UNAME = "aporter";
    private final static String URL = "http://api.geonames.org/earthquakesJSON?north=44.1&south=-9.9&east=-22.4&west=55.2&username="
            + UNAME;

    public static final String TAG = "MapsEarthquakeMapActivity";

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_earthquake_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Center the map
        // Should compute map center from the actual data

        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(
                CAMERA_LAT, CAMERA_LNG)));


        new GetResponseTask().execute(URL);
    }

    public class GetResponseTask extends AsyncTask<String, Void, List<EarthQuakeRec>> {

        private static final String LONGITUDE_TAG = "lng";
        private static final String LATITUDE_TAG = "lat";
        private static final String MAGNITUDE_TAG = "magnitude";
        private static final String EARTHQUAKE_TAG = "earthquakes";

        @Override
        protected List<EarthQuakeRec> doInBackground(String... strings) {
            //this part only return raw response from url!

            try {
                String raw = GetHttpResponse(strings[0]);
                if (raw != null) {
                    return ParseJSON(raw);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<EarthQuakeRec> result) {

            if (null != mMap) {

                // Add a marker for every earthquake

                for (EarthQuakeRec rec : result) {

                    // Add a new marker for this earthquake
                    mMap.addMarker(new MarkerOptions()

                            // Set the Marker's position
                            .position(new LatLng(rec.getLat(), rec.getLng()))

                            // Set the title of the Marker's information window
                            .title(String.valueOf(rec.getMagnitude()))

                            // Set the color for the Marker
                            .icon(BitmapDescriptorFactory
                                    .defaultMarker(getMarkerColor(rec
                                            .getMagnitude()))));

                }


            }
        }

        // Assign marker color
        private float getMarkerColor(double magnitude) {

            if (magnitude < 6.0) {
                magnitude = 6.0;
            } else if (magnitude > 9.0) {
                magnitude = 9.0;
            }

            return (float) (120 * (magnitude - 6));
        }

        // convert inputstream to String
        private String convertInputStreamToString(InputStream inputStream) throws IOException {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            String result = "";
            while ((line = bufferedReader.readLine()) != null)
                result += line;

            inputStream.close();
            return result;

        }

        protected String GetHttpResponse(String urlText) {
            String raw = null;

            try {
                URL url = new URL(urlText);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                raw = convertInputStreamToString(inputStream);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return raw;
        }

        // parse raw JSON data into strings list
        private List<EarthQuakeRec> ParseJSON(String raw) throws JSONException, ParseException {
            List<EarthQuakeRec> result = new ArrayList<>();
            JSONObject responseObject = new JSONObject(raw);
            // Extract value of "earthquakes" key -- a List
            JSONArray earthquakes = responseObject.getJSONArray(EARTHQUAKE_TAG);

            // Iterate over earthquakes list
            for (int idx = 0; idx < earthquakes.length(); idx++) {

                // Get single earthquake data - a Map
                JSONObject earthquake = (JSONObject) earthquakes.get(idx);

                // Summarize earthquake data as a EearthQuakeRec and add it to
                // result
                result.add(new EarthQuakeRec(earthquake.getDouble(LATITUDE_TAG),
                        earthquake.getDouble(LONGITUDE_TAG),
                        earthquake.getDouble(MAGNITUDE_TAG)));
            }
            return result;
        }
    }
}

