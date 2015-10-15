package nazar.cybulskij.testdirection;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONException;
import org.json.JSONObject;

import nazar.cybulskij.testdirection.adapter.AutoCompleteEventLocationAdapter;
import nazar.cybulskij.testdirection.network.DirectionService;
import nazar.cybulskij.testdirection.network.ServiceGenerator;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

public class MainActivity extends AppCompatActivity {


    AutoCompleteTextView from;
    AutoCompleteTextView to;
    Button mLoadDirections;
    TextView mJsonTextView;

    DirectionService service;

    private static final LatLngBounds BOUNDS_GREATER_MOSCOW = new LatLngBounds(
            new LatLng(55.151244, 37.018423), new LatLng(56.551244, 38.318423));






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        from = (AutoCompleteTextView) findViewById(R.id.from);
        to = (AutoCompleteTextView) findViewById(R.id.to);
        mJsonTextView = (TextView)findViewById(R.id.json);

        
        from.setAdapter(new AutoCompleteEventLocationAdapter(this, BOUNDS_GREATER_MOSCOW));
        to.setAdapter(new AutoCompleteEventLocationAdapter(this, BOUNDS_GREATER_MOSCOW));
        mLoadDirections = (Button)findViewById(R.id.load_directions);
        service = ServiceGenerator.createService(DirectionService.class, getResources().getString(R.string.direction_matrix_url));

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

                service.getMatrixDirection(fromstr, tostr, getResources().getString(R.string.SERVER_API_KEY), false,"ru", new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        try {
                            JSONObject object = new JSONObject(new String(((TypedByteArray) response.getBody()).getBytes()));
                            String json = formatString(object.toString());
                            //Toast.makeText(MainActivity.this,json,Toast.LENGTH_LONG).show();
                            mJsonTextView.setText(json);

                            Log.v("TAG",object.toString());

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


}
