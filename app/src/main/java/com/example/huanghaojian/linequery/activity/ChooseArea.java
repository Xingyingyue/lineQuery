package com.example.huanghaojian.linequery.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.huanghaojian.linequery.R;
import com.example.huanghaojian.linequery.db.DBOperation;
import com.example.huanghaojian.linequery.model.City;
import com.example.huanghaojian.linequery.model.County;
import com.example.huanghaojian.linequery.model.Province;
import com.example.huanghaojian.linequery.util.HttpCallbackListener;
import com.example.huanghaojian.linequery.util.HttpUtil;
import com.example.huanghaojian.linequery.util.Utillity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huanghaojian on 16/10/24.
 */

public class ChooseArea extends Activity implements View.OnClickListener{
    //private static final int QUERY_DATA=1;//用于异步消息处理
    private Button goBack;
    private static TextView titleView;
    private static ListView listView;
    private ArrayAdapter<String> adapter;
    private DBOperation dbOperation;
    private List<String> dataList=new ArrayList<String>();
    private List<Province>provinceList;
    private List<City>cityList;
    private List<County>countyList;
    private Province selectedProvince;
    private City selectedCity;
    private County selectedCounty;
    private static final int level_province=0;
    private static final int level_city=1;
    private static final int level_county=2;
    private int currentLevel;
    private ProgressDialog progressDialog;
    private boolean isFromWeatherActivity;
    /*private Handler handler=new Handler(){
       public void handleMessage(Message message){
            switch (message.what){
                case QUERY_DATA:
                    closeProgressDialog();
                    if("province".equals())
                    break;
                default:
                    break;
            }
        }
    }*/
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        isFromWeatherActivity=getIntent().getBooleanExtra("from_weather_activity",false);
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        if(sharedPreferences.getBoolean("city_selected",false)&&!isFromWeatherActivity){
            Intent intent=new Intent(this,Weather.class);
            startActivity(intent);
            finish();
            return;
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);
        goBack=(Button)findViewById(R.id.go_back);
        goBack.setOnClickListener(this);
        titleView=(TextView)findViewById(R.id.title_text);
        listView=(ListView)findViewById(R.id.list_view);
        adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        dbOperation=DBOperation.getInstance(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel==level_province){
                    selectedProvince=provinceList.get(position);
                    queryCity();
                }else if(currentLevel==level_city) {
                    selectedCity = cityList.get(position);
                    queryCounty();
                }else if (currentLevel==level_county){
                    String countyCode=countyList.get(position).getCountyCode();
                    Intent intent=new Intent(ChooseArea.this,Weather.class);
                    intent.putExtra("county_code",countyCode);
                    startActivity(intent);
                    finish();
                }
            }
        });
        queryProvince();
    }
    private  void queryProvince(){
        provinceList=dbOperation.readProvinceFromDB();
        if(provinceList.size()>0){
            dataList.clear();
            for(Province province:provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleView.setText("中国");
            currentLevel=level_province;
        }else{
            queryFromServer(null,"province");
        }
    }
    private void queryCity(){
        cityList=dbOperation.readCityFromDB(selectedProvince.getId());
        if(cityList.size()>0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleView.setText(selectedProvince.getProvinceName());
            currentLevel = level_city;
        }else{
            queryFromServer(selectedProvince.getProvinceCode(),"city");
        }
    }
    private void queryCounty(){
        countyList=dbOperation.readCountyFromDB(selectedCity.getId());
        if (countyList.size()>0){
            dataList.clear();
            for (County county:countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleView.setText(selectedCity.getCityName());
            currentLevel=level_county;
        }else{
            queryFromServer(selectedCity.getCityCode(),"county");
        }
    }
    private void queryFromServer(String code, final String type){
        String address;
        if(!TextUtils.isEmpty(code)){
            address="http://www.weather.com.cn/data/list3/city"+code+".xml";
        }else{
            address="http://www.weather.com.cn/data/list3/city.xml";
        }
        showprogressDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                boolean result=false;
                if("province".equals(type)){
                    result= Utillity.handleProvinceResponse(dbOperation,response);
                }else if("city".equals(type)){
                    result=Utillity.handleCityResponse(dbOperation,response,selectedProvince.getId());
                }else if("county".equals(type)){
                    result=Utillity.handleCountyResponse(dbOperation,response,selectedCity.getId());
                }
                /**if(result){
                 new Thread(new Runnable() {
                @Override
                public void run() {
                Message message=new Message();
                message.what=QUERY_DATA;
                handler.sendMessage(message);
                }
                }).start();*/
                if(result){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvince();
                            }else if("city".equals(type)){
                                queryCity();
                            }else if("county".equals(type)){
                                queryCounty();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseArea.this,"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    private  void showprogressDialog(){
        if(progressDialog==null){
            progressDialog=new ProgressDialog(ChooseArea.this);
            progressDialog.setMessage("加载中");
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
    }
    private void closeProgressDialog(){
        if(progressDialog!=null){
            progressDialog.dismiss();
        }
    }
    public void onBackPressed(){
        if(currentLevel==level_county){
            queryCity();
        }else if(currentLevel==level_city){
            queryProvince();
        }else{
            if(isFromWeatherActivity){
                Intent intent=new Intent(this,Weather.class);
                startActivity(intent);
            }
            finish();
        }
    }
    public void onClick(View view){
        switch (view.getId()){
            case R.id.go_back:
                Intent intent=new Intent(this,MainActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
