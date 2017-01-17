package com.example.huanghaojian.linequery.model;

/**
 * Created by huanghaojian on 16/10/24.
 */

public class County {
    private  String county_name;
    private String county_code;
    private int id;
    private int cityId;
    public String getCountyName(){
        return county_name;
    }
    public String getCountyCode(){
        return county_code;
    }
    public int getId(){
        return id;
    }
    public void setCountyName(String countyName){
        county_name=countyName;
    }
    public void setCountyCode(String countyCode){
        county_code=countyCode;
    }
    public void setId(int id){
        this.id=id;
    }
    public int getCityId(){return cityId;}
    public void setCityId(int cityId){this.cityId=cityId;}
}

