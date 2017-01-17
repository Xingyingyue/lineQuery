package com.example.huanghaojian.linequery.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import com.example.huanghaojian.linequery.R;

/**
 * Created by huanghaojian on 16/10/21.
 */

public class MainActivity extends Activity implements View.OnClickListener{
    private Button queryLine;
    private Button myLocation;
    private Button queryWeather;
    private TextView appState;
    private EditText start;
    private EditText destination;
    private EditText city;
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_layout);
        queryLine=(Button)findViewById(R.id.query_line);
        myLocation=(Button)findViewById(R.id.my_location);
        queryWeather=(Button)findViewById(R.id.weather);
        appState=(TextView)findViewById(R.id.app_state);
        start=(EditText)findViewById(R.id.start);
        destination=(EditText)findViewById(R.id.destination);
        city=(EditText)findViewById(R.id.input_city);
        queryLine.setOnClickListener(this);
        myLocation.setOnClickListener(this);
        queryWeather.setOnClickListener(this);
    }


    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.query_line:
                Intent intent2 = new Intent(this, TransferQuery.class);
                intent2.putExtra("start", start.getText().toString());
                intent2.putExtra("end", destination.getText().toString());
                intent2.putExtra("currentCity", city.getText().toString());
                startActivity(intent2);
                break;
            case R.id.my_location:
                Intent intent1 = new Intent(this, Map.class);
                startActivity(intent1);
                break;
            case R.id.weather:
                Intent intent = new Intent(this, ChooseArea.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
