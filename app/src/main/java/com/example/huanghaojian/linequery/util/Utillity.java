package com.example.huanghaojian.linequery.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.example.huanghaojian.linequery.db.DBOperation;
import com.example.huanghaojian.linequery.model.City;
import com.example.huanghaojian.linequery.model.County;
import com.example.huanghaojian.linequery.model.Province;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by huanghaojian on 16/10/24.
 */

public class Utillity {
    //处理服务器返回的省份信息
    public  static  synchronized boolean handleProvinceResponse(DBOperation dbOperation, String response){
        if(!TextUtils.isEmpty(response)){
            String[]allProvince=response.split(",");
            if(allProvince!=null&&allProvince.length>0){
                for(String p:allProvince){
                    String[]array=p.split("\\|");
                    Province province=new Province();
                    province.setProvinceCode(array[0]);
                    province.setProvinceName(array[1]);
                    dbOperation.saveProvinceToDB(province);
                }
                return  true;
            }
        }
        return  false;
    }
    //处理服务器返回的城市信息
    public static  synchronized boolean handleCityResponse(DBOperation dbOperation,String response,int province_id){
        if (!TextUtils.isEmpty(response)){
            String[]allCity=response.split(",");
            if (allCity!=null&&allCity.length>0){
                for (String c:allCity) {
                    String[] array = c.split("\\|");
                    City city = new City();
                    city.setCityCode(array[0]);
                    city.setCityName(array[1]);
                    city.setProvinceId(province_id);
                    dbOperation.saveCityToDB(city);
                }
                return true;
            }
        }
        return  false;
    }
    //处理服务器返回的县信息
    public static  synchronized  boolean handleCountyResponse(DBOperation dbOperation,String response,int city_id){
        if(!TextUtils.isEmpty(response)){
            String[]allCounty=response.split(",");
            if(allCounty!=null&&allCounty.length>0) {
                for (String c : allCounty) {
                    String[] array = c.split("\\|");
                    County county = new County();
                    county.setCountyCode(array[0]);
                    county.setCountyName(array[1]);
                    county.setCityId(city_id);
                    dbOperation.saveCountyToDB(county);
                }
                return true;
            }
        }
        return  false;
    }
    public static  synchronized String handleWeatherCodeResponse(String response){
        String weatherCode=null;
        if(!TextUtils.isEmpty(response)){
            String[]array=response.split("\\|");
            if(array!=null&&array.length==2){
                weatherCode=array[1];
            }
        }
        return weatherCode;
    }
    public static  void handleWeatherResponse(Context context, String response){
        try {
            JSONObject jsonObject=new JSONObject(response);
            JSONObject weatherInf=jsonObject.getJSONObject("weatherinfo");
            String cityName=weatherInf.getString("city");
            String weatherCode=weatherInf.getString("cityid");
            String temp1=weatherInf.getString("temp1");
            String temp2=weatherInf.getString("temp2");
            String weatherState=weatherInf.getString("weather");
            String publishTime=weatherInf.getString("ptime");
            saveWeatherInfo(context,cityName,weatherCode,temp1,temp2,weatherState,publishTime);
        }catch (JSONException e){
            e.printStackTrace();
        }
    }
    public static  void saveWeatherInfo(Context context,String cityName,String weatherCode,String temp1,String temp2,String weatherState,String publishTime ){
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
        SharedPreferences.Editor editor= PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("city_selected",true);
        editor.putString("city_name",cityName);
        editor.putString("weather_code",weatherCode);
        editor.putString("temp1",temp1);
        editor.putString("temp2",temp2);
        editor.putString("weather_state",weatherState);
        editor.putString("publish_time",publishTime);
        editor.putString("current_date",simpleDateFormat.format(new Date()));
        editor.commit();
    }
}

