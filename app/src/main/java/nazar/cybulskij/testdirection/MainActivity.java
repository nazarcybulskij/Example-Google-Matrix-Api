package nazar.cybulskij.testdirection;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.IntentSender;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import nazar.cybulskij.testdirection.adapter.AutoCompleteEventLocationAdapter;
import nazar.cybulskij.testdirection.fragment.ShowDirectionFragment;
import nazar.cybulskij.testdirection.listener.OnChangedLocationListener;
import nazar.cybulskij.testdirection.model.Direction;
import nazar.cybulskij.testdirection.model.Location;
import nazar.cybulskij.testdirection.model.Step;
import nazar.cybulskij.testdirection.model_entity.GeneralPoint;
import nazar.cybulskij.testdirection.network.DirectionService;
import nazar.cybulskij.testdirection.network.ServiceGenerator;
import nazar.cybulskij.testdirection.util.DirectionUtil;
import nazar.cybulskij.testdirection.util.DistanceUtil;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

public class MainActivity extends FragmentActivity implements com.google.android.gms.location.LocationListener,
                                GoogleApiClient.ConnectionCallbacks,
                                GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getName();


    AutoCompleteTextView from;
    AutoCompleteTextView to;
    Button mLoadDirections;
    SupportMapFragment mapFragment;
    GoogleMap map;
    DirectionService service;
    String mode = "driving";
    TextToSpeech mTextToSprech;
    ArrayList<Step> stepslist = new ArrayList<Step>();
    ArrayList<GeneralPoint>  listGeneralPoints;
    private static final LatLngBounds BOUNDS_GREATER_MOSCOW = new LatLngBounds(
            new LatLng(55.151244, 37.018423), new LatLng(56.551244, 38.318423));
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationRequest locationRequest;
    private GoogleApiClient locationClient;
    private static final int MILLISECONDS_PER_SECOND = 1000;
    private static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    private static final int FAST_CEILING_IN_SECONDS = 1;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
            * UPDATE_INTERVAL_IN_SECONDS;
    private static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
            * FAST_CEILING_IN_SECONDS;
    private android.location.Location lastLocation;
    private android.location.Location currentLocation;
    private boolean hasSetUpInitialLocation;

    ShowDirectionFragment fragment = null;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String languageToLoad = "en";
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());

        setContentView(R.layout.activity_main);

         Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment);
         if (fragment instanceof ShowDirectionFragment){
             this.fragment = (ShowDirectionFragment)fragment;
         }

        from = (AutoCompleteTextView) findViewById(R.id.from);
        to = (AutoCompleteTextView) findViewById(R.id.to);

        mTextToSprech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    Locale locale = new Locale("ru");
                    int result = mTextToSprech.setLanguage(locale);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e(TAG, "Извините, этот язык не поддерживается");
                    } else {

                    }

                } else {
                    Log.e(TAG, "Ошибка!");
                }
            }
        });

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        map = mapFragment.getMap();
        if (map == null) {
            finish();
            return;
        }

        map.setMyLocationEnabled(true);
        // Check if we were successful in obtaining the map.
        if (map != null) {
            map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(android.location.Location location) {
                    map.setTrafficEnabled(true);
                }
            });
        }

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setFastestInterval(FAST_INTERVAL_CEILING_IN_MILLISECONDS);
        locationClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        from.setAdapter(new AutoCompleteEventLocationAdapter(this, BOUNDS_GREATER_MOSCOW));
        to.setAdapter(new AutoCompleteEventLocationAdapter(this, BOUNDS_GREATER_MOSCOW));
        mLoadDirections = (Button)findViewById(R.id.load_directions);
        service = ServiceGenerator.createService(DirectionService.class, getResources().getString(R.string.direction_url));

        mLoadDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tostr =to.getText().toString();
                String fromstr =from.getText().toString();
                service.getDirection(fromstr, tostr, mode, getResources().getString(R.string.SERVER_API_KEY), false, "en", true,new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        try {

                            JSONObject object = new JSONObject(new String(((TypedByteArray) response.getBody()).getBytes()));
                            String json = formatString(object.toString());

                            Type listType = new TypeToken<List<Step>>(){}.getType();
                            JSONArray results = object.optJSONArray("routes");
                            JSONObject route;
                            JSONArray legs;
                            JSONObject leg ;
                            JSONArray steps ;
                            Gson gson = new Gson();
                            ArrayList<ArrayList<Step>> allRoutes=new ArrayList<ArrayList<Step>>();
                            listGeneralPoints =new ArrayList<GeneralPoint>();
                            listGeneralPoints.clear();


                            for (int i =0;i<results.length();i++){
                                 route = results.optJSONObject(i);
                                 legs = route.optJSONArray("legs");
                                 leg = legs.optJSONObject(0);
                                 steps = leg.optJSONArray("steps");
                                 String jsonOutput = steps.toString();
                                 stepslist = (ArrayList<Step>) gson.fromJson(jsonOutput, listType);
                                 allRoutes.add(stepslist);

                            }
                            map.clear();

                            if (allRoutes.size()>0) {
                                onDrawRoutes(allRoutes.get(0));
                                generateGeneralPoint(allRoutes.get(0));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void failure(RetrofitError error) {

                    }
                });


            }
        });

        setListener(new OnChangedLocationListener() {
            @Override
            public void onChange() {
                Toast.makeText(MainActivity.this,"onChange",Toast.LENGTH_SHORT).show();
                if (listGeneralPoints!=null){
                    if(listGeneralPoints.size()>0){
                        float mindistance=Float.MAX_VALUE;
                        GeneralPoint minDistancePoint = null;
                        for(GeneralPoint temp:listGeneralPoints){
                            if (currentLocation.distanceTo(temp.getLocaton())<mindistance) {
                                mindistance = currentLocation.distanceTo(temp.getLocaton());
                                minDistancePoint = temp;
                            }
                        }
                        if (minDistancePoint.getIsSelect()){
                            hide();
                            return;
                        }
                        if (mindistance<200.0f){

                            String spick = Html.fromHtml(minDistancePoint.getInstruction()).toString();
                            //mTextToSprech.speak(spick, TextToSpeech.QUEUE_FLUSH, null);

                            show(spick, DirectionUtil.getDirectionfromString(spick));

                            minDistancePoint.setIsSpeack(true);
                            if (mindistance<50.0f){
                                minDistancePoint.setIsSelect(true);
                            }

                        }else{
                            hide();
                        }
                    }

                }
            }
        });

    }


    @Override
    public void onStop() {
        // If the client is connected
        if (locationClient.isConnected()) {
            stopPeriodicUpdates();
        }
        locationClient.disconnect();

        super.onStop();
    }


    @Override
    public void onStart() {
        super.onStart();
        locationClient.connect();
        hide();
    }


    private void stopPeriodicUpdates() {
        locationClient.disconnect();
    }





    public void onDrawRoutes(ArrayList<Step> steps){
        for (Step tempstep:steps){
                onDrawRoute(tempstep);
        }
    }

    public void generateGeneralPoint(ArrayList<Step> steps){
        for (Step tempstep:steps){
            android.location.Location  location = new android.location.Location("");
            location.setLatitude(tempstep.getStart_location().getLat());
            location.setLongitude(tempstep.getStart_location().getLng());
            listGeneralPoints.add(new GeneralPoint(tempstep.getHtml_instructions(),location ));


        }
    }

    public void onDrawRoute(Step step){
        List<LatLng> mPoints = PolyUtil.decode(step.getPolyline().getPoints());
        PolylineOptions line;
             line = new PolylineOptions()
                    .color(Color.BLUE)
                    .width(5)
                    .visible(true)
                    .zIndex(30);
        LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
        for (int i = 0; i < mPoints.size(); i++) {

            if (i == 0) {
                MarkerOptions startMarkerOptions = new MarkerOptions().position(mPoints.get(i));
                map.addMarker(startMarkerOptions);
            } else if (i == mPoints.size() - 1) {
                MarkerOptions endMarkerOptions = new MarkerOptions().position(mPoints.get(i));
                map.addMarker(endMarkerOptions);
            }

            line.add(mPoints.get(i));
            latLngBuilder.include(mPoints.get(i));
        }
        map.addPolyline(line);
        int size = getResources().getDisplayMetrics().widthPixels;
        LatLngBounds latLngBounds = latLngBuilder.build();
        CameraUpdate track = CameraUpdateFactory.newLatLngBounds(latLngBounds, size, size, 25);
        map.moveCamera(track);
    }



    public static String formatString(String text){
        StringBuilder json = new StringBuilder();
        String indentString = "";

        for (int i = 0; i < text.length(); i++) {
            char letter = text.charAt(i);
            switch (letter) {
                case '{':
                case '[':
                    json.append("\n" + indentString + letter + "\n");
                    indentString = indentString + "\t";
                    json.append(indentString);
                    break;
                case '}':
                case ']':
                    indentString = indentString.replaceFirst("\t", "");
                    json.append("\n" + indentString + letter);
                    break;
                case ',':
                    json.append(letter + "\n" + indentString);
                    break;

                default:
                    json.append(letter);
                    break;
            }
        }

        return json.toString();
    }





    private  String getAdress(Location location){
        Geocoder geocoder;
        geocoder = new Geocoder(this);
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(
                    location.getLat(),
                    location.getLng(),
                    1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses!=null){
            if (addresses.size()!=0){
                Address address = addresses.get(0);
                String result = address.getAddressLine(0) + ", " + address.getLocality();
                return result;
            }
        }

        return "";


    }


    private double getConvertedDistance(LatLng latlng1, LatLng latlng2) {
        double distance = DistanceUtil.distance(latlng1.latitude,
                latlng1.longitude,
                latlng2.latitude,
                latlng2.longitude);
        BigDecimal bd = new BigDecimal(distance);
        BigDecimal res = bd.setScale(3, RoundingMode.DOWN);
        return res.doubleValue();
    }



    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Connected to location services");
        currentLocation = getLocation();
        if (listener!=null)
            listener.onChange();

        startPeriodicUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection has been suspend");
    }

    @Override
    public void onLocationChanged(android.location.Location location) {
        currentLocation = location;
        if (lastLocation != null
                && lastLocation.distanceTo(location)<5) {
            // If the location hasn't changed by more than 5 meters, ignore it.
            return;
        }
        lastLocation = location;
        if (listener!=null)
            listener.onChange();
        LatLng myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (!hasSetUpInitialLocation) {
            hasSetUpInitialLocation = true;
        }


    }


    public android.location.Location getLastLocation() {
        return lastLocation;
    }

    public android.location.Location getCurrentLocation() {
        return currentLocation;
    }


    public OnChangedLocationListener getListener() {
        return listener;
    }

    public void setListener(OnChangedLocationListener listener) {
        this.listener = listener;
    }

    OnChangedLocationListener listener;

    private void startPeriodicUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                locationClient, locationRequest, this);
    }





    private android.location.Location getLocation() {
        // If Google Play Services is available
        if (servicesConnected()) {
            // Get the current location
            return LocationServices.FusedLocationApi.getLastLocation(locationClient);
        } else {
            return null;
        }
    }

      public  void show(String title ,Direction direction){
          if (fragment!=null){
              fragment.setDirection(title,direction);
              android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
              fm.beginTransaction()
                      .show(fragment)
                      .commit();



          }


      }

      public void hide(){
          if (fragment!=null){
              android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
              fm.beginTransaction()
                      .hide(fragment)
                      .commit();

          }

      }


    /*
        * Verify that Google Play services is available before making a request.
        *
        * @return true if Google Play services is available, otherwise false
    */
    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.i(TAG, "Google play services available");

            // Continue
            return true;
        } else {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getFragmentManager(), TAG);
            }
            return false;
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Google Play services can resolve some errors it detects. If the error has a resolution, try
        // sending an Intent to start a Google Play services activity that can resolve error.
        if (connectionResult.hasResolution()) {
            try {

                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

            } catch (IntentSender.SendIntentException e) {

                // Thrown if Google Play services canceled the original PendingIntent
                Log.i(TAG, "An error occurred when connecting to location services.", e);

            }
        } else {
            // If no resolution is available, display a dialog to the user with the error.
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    private void showErrorDialog(int errorCode) {
        Dialog errorDialog =
                GooglePlayServicesUtil.getErrorDialog(errorCode, this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);

        if (errorDialog != null) {
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();
            errorFragment.setDialog(errorDialog);
            errorFragment.show(getFragmentManager(), TAG);
        }
    }




    public static class ErrorDialogFragment extends DialogFragment {
        private Dialog mDialog;


        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }
}
