package nazar.cybulskij.testdirection;

import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import nazar.cybulskij.testdirection.adapter.AutoCompleteEventLocationAdapter;
import nazar.cybulskij.testdirection.model.Location;
import nazar.cybulskij.testdirection.model.Step;
import nazar.cybulskij.testdirection.network.DirectionService;
import nazar.cybulskij.testdirection.network.ServiceGenerator;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener {


    AutoCompleteTextView from;
    AutoCompleteTextView to;
    Button mLoadDirections;
    TextView mJsonTextView;

    TextView mLine;

    DirectionService service;
    String mode = "transit";

    ArrayList<Step> stepslist = new ArrayList<Step>();

    private static final LatLngBounds BOUNDS_GREATER_MOSCOW = new LatLngBounds(
            new LatLng(55.151244, 37.018423), new LatLng(56.551244, 38.318423));






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        from = (AutoCompleteTextView) findViewById(R.id.from);
        to = (AutoCompleteTextView) findViewById(R.id.to);
        mJsonTextView = (TextView)findViewById(R.id.json);
        mLine = (TextView)findViewById(R.id.line);

        findViewById(R.id.radio_driving).setOnClickListener(this);
        findViewById(R.id.radio_walking).setOnClickListener(this);
        findViewById(R.id.radio_bicycling).setOnClickListener(this);
        findViewById(R.id.radio_transit).setOnClickListener(this);



//        rGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
//        {
//            public void onCheckedChanged(RadioGroup rGroup, int checkedId)
//            {
//
//                switch (checkedId){
//                    case R.id.radio_driving:
//                        mode = "driving";
//                        break;
//                    case R.id.radio_walking:
//                        mode = "walking";
//                        break;
//                    case R.id.radio_bicycling:
//                        mode = "bicycling";
//                        break;
//                    case R.id.radio_transit:
//                        mode = "transit";
//                        break;
//                }
//            }
//        });




        
        from.setAdapter(new AutoCompleteEventLocationAdapter(this, BOUNDS_GREATER_MOSCOW));
        to.setAdapter(new AutoCompleteEventLocationAdapter(this, BOUNDS_GREATER_MOSCOW));
        mLoadDirections = (Button)findViewById(R.id.load_directions);
        service = ServiceGenerator.createService(DirectionService.class, getResources().getString(R.string.direction_url));

        mLoadDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tostr =to.getText().toString();
                String fromstr =from.getText().toString();

//                service.getDirection(fromstr, tostr, getResources().getString(R.string.SERVER_API_KEY), false,"ru", new Callback<Response>() {
//                    @Override
//                    public void success(Response responsemi, Response response2) {
//                        try {
//                            JSONObject object = new JSONObject(new String(((TypedByteArray) response.getBody()).getBytes()));
//                            String json = formatString(object.toString());
//                            mJsonTextView.setText(json);
//
//                            Log.v("TAG",object.toString());
//
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//
//                    }
//
//                    @Override
//                    public void failure(RetrofitError error) {
//
//                    }
//                });

                service.getDirection(fromstr, tostr, mode, getResources().getString(R.string.SERVER_API_KEY), false, "ru", true,new Callback<Response>() {
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

                            for (int i =0;i<results.length();i++){
                                 route = results.optJSONObject(i);
                                 legs = route.optJSONArray("legs");
                                 leg = legs.optJSONObject(0);
                                 steps = leg.optJSONArray("steps");
                                 String jsonOutput = steps.toString();
                                 stepslist = (ArrayList<Step>) gson.fromJson(jsonOutput, listType);
                                 printLine();


                                 mLine.setText(mLine.getText()+"/-----------------------------------------------/"+"\n");



                            }










                            mJsonTextView.setText(json);


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


    @Override
    public void onClick(View v) {
        int checkedId = v.getId();

        switch (checkedId){
            case R.id.radio_driving:
                mode = "driving";
                break;
            case R.id.radio_walking:
                mode = "walking";
                break;
            case R.id.radio_bicycling:
                mode = "bicycling";
                break;
            case R.id.radio_transit:
                mode = "transit";
                break;
        }
        mJsonTextView.setText("");

    }

    private  void printLine(){
        for (Step temp:stepslist){
            if(temp.getTravel_mode().equals("TRANSIT")){
                printTransit(temp);
            }

            if(temp.getTravel_mode().equals("WALKING")){
                printWalking(temp);
            }

            mLine.setText(mLine.getText()+"\n");
        }
    }

    private  void printWalking(Step step){
        mLine.setText(mLine.getText()+"\n" + "Start = "+getAdress(step.getStart_location()));

        mLine.setText(mLine.getText()+"\n" + " Duration = "+step.getDuration().getText());
        mLine.setText(mLine.getText()+"\n" + " Distance = "+step.getDistance().getText());
        mLine.setText(mLine.getText()+"\n" + " Type = "+step.getTravel_mode().toLowerCase());

        mLine.setText(mLine.getText() + "\n" + " End = " + getAdress(step.getEnd_location()));

    }

    private  void printTransit(Step step){

        mLine.setText(mLine.getText()+"\n" + "Start = "+getAdress(step.getStart_location()));

        mLine.setText(mLine.getText()+"\n" + " Duration = "+step.getDuration().getText());
        mLine.setText(mLine.getText()+"\n" + " Distance = "+step.getDistance().getText());
        mLine.setText(mLine.getText()+"\n" + " Type = "+step.getTravel_mode().toLowerCase());


        mLine.setText(mLine.getText()+"\n" + " Start = "+step.getDetail().getDeparture_stop().getName());
        mLine.setText(mLine.getText()+"\n" + " Маршрут = "+step.getDetail().getLine().getName()+" "+step.getDetail().getLine().getVehicle().getType());
        mLine.setText(mLine.getText()+"\n" + "№  = "+step.getDetail().getLine().getShort_name());

        mLine.setText(mLine.getText()+"\n" + " Зупинок = "+step.getDetail().getNum_stops());
        mLine.setText(mLine.getText()+"\n" + " End = "+step.getDetail().getArrival_stop().getName());



        mLine.setText(mLine.getText() + "\n" + " End = " + getAdress(step.getEnd_location()));



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


}
