package diploma.work.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import diploma.work.R;

public class Settings extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        getWindow().setLayout((int)(width*0.8), (int)(height*0.5));

    }
}
