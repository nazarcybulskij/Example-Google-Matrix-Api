package nazar.cybulskij.testdirection.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import nazar.cybulskij.testdirection.R;
import nazar.cybulskij.testdirection.model.Direction;

/**
 * Created by Nazarko on 1/15/2016.
 */
public class ShowDirectionFragment extends Fragment {

    String title ;
    Direction direction;

    ImageView imageView;
    TextView textView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show_direction,  container, false);
        imageView = (ImageView)view.findViewById(R.id.image);
        textView = (TextView)view.findViewById(R.id.title);
        return view;
    }




    public void setDirection(String title,Direction direction ){
        this.title = title;
        this.direction = direction;
        reloadUI();
    }

    private void reloadUI() {
        textView.setText(title);
        imageView.setImageResource(direction.getValue());
    }


}
