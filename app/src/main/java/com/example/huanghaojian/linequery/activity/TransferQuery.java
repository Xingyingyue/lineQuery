package com.example.huanghaojian.linequery.activity;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;
import com.example.huanghaojian.linequery.R;
import com.example.huanghaojian.linequery.tools.BusResultListAdapter;
import com.example.huanghaojian.linequery.util.AMapUtil;
import com.example.huanghaojian.linequery.util.ToastUtil;

import java.util.List;

/**
 * Created by huanghaojian on 16/12/8.
 */

public class TransferQuery extends Activity implements AMap.OnMapClickListener,
        AMap.OnMarkerClickListener, RouteSearch.OnRouteSearchListener,GeocodeSearch.OnGeocodeSearchListener {

    private AMap aMap;
    private MapView mapView;
    private Context mContext;
    private RouteSearch mRouteSearch;
    private DriveRouteResult mDriveRouteResult;
    private BusRouteResult mBusRouteResult;
    private WalkRouteResult mWalkRouteResult;
    //private LatLonPoint mStartPoint = new LatLonPoint(39.942295, 116.335891);//起点，116.335891,39.942295
    //private LatLonPoint mEndPoint = new LatLonPoint(39.995576, 116.481288);//终点，116.481288,39.995576
    private LatLonPoint mStartPoint;
    private LatLonPoint mEndPoint;
    private String mCurrentCityName = "北京";
    private final int ROUTE_TYPE_BUS = 1;
    private final int ROUTE_TYPE_DRIVE = 2;
    private final int ROUTE_TYPE_WALK = 3;
    //private final int ROUTE_TYPE_CROSSTOWN = 4;

    private LinearLayout mBusResultLayout;
    private RelativeLayout mBottomLayout;
    private TextView mRotueTimeDes, mRouteDetailDes;
    private ListView mBusResultList;
    private ProgressDialog progDialog = null;// 搜索时进度条
    private GeocodeSearch geocodeSearch;
    private String city;
    private String startName;
    private String endName;
    private double latitude;
    private double longitude;
    private int count=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.line_query);
        mContext = this.getApplicationContext();
        mapView = (MapView) findViewById(R.id.route_map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        init();
        Intent intent = getIntent();
        startName = intent.getStringExtra("start");
        endName = intent.getStringExtra("end");
        city = intent.getStringExtra("currentCity");
        mCurrentCityName=city;
        GeocodeQuery query = new GeocodeQuery(startName, city);
        geocodeSearch.getFromLocationNameAsyn(query);
        //mStartPoint=new LatLonPoint(latitude,longitude);
        GeocodeQuery query1=new GeocodeQuery(endName,city);
        geocodeSearch.getFromLocationNameAsyn(query1);
        //mEndPoint=new LatLonPoint(latitude,longitude);
//		getIntentData();
        //setfromandtoMarker();
    }

    /*private void setfromandtoMarker() {
        aMap.addMarker(new MarkerOptions()
                .position(AMapUtil.convertToLatLng(mStartPoint))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.start)));
        aMap.addMarker(new MarkerOptions()
                .position(AMapUtil.convertToLatLng(mEndPoint))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.end)));
    }*/

    /**
     * 初始化AMap对象
     */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        registerListener();
        mRouteSearch = new RouteSearch(this);
        mRouteSearch.setRouteSearchListener(this);
        mBottomLayout = (RelativeLayout) findViewById(R.id.bottom_layout);
        mRotueTimeDes = (TextView) findViewById(R.id.firstline);
        mRouteDetailDes = (TextView) findViewById(R.id.secondline);

        mBusResultLayout = (LinearLayout) findViewById(R.id.bus_result);
        mBusResultList = (ListView) findViewById(R.id.bus_result_list);
        geocodeSearch = new GeocodeSearch(this);
        geocodeSearch.setOnGeocodeSearchListener(this);
    }

    /**
     * 注册监听
     */
    private void registerListener() {
        aMap.setOnMapClickListener(this);
        aMap.setOnMarkerClickListener(this);

    }

    public void onDriveClick(View view) {
        searchRouteResult(ROUTE_TYPE_BUS, RouteSearch.BusDefault);
        mapView.setVisibility(View.GONE);
        mBusResultLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 开始搜索路径规划方案
     */
    public void searchRouteResult(int routeType, int mode) {
        if (mStartPoint == null) {
            ToastUtil.show(mContext, "起点未设置");
            return;
        }
        if (mEndPoint == null) {
            ToastUtil.show(mContext, "终点未设置");
        }
        showProgressDialog();
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
                mStartPoint, mEndPoint);
        if (routeType == ROUTE_TYPE_BUS) {// 公交路径规划
            RouteSearch.BusRouteQuery query = new RouteSearch.BusRouteQuery(fromAndTo, mode,
                    mCurrentCityName, 0);// 第一个参数表示路径规划的起点和终点，第二个参数表示公交查询模式，第三个参数表示公交查询城市区号，第四个参数表示是否计算夜班车，0表示不计算
            mRouteSearch.calculateBusRouteAsyn(query);// 异步路径规划公交模式查询
        } else if (routeType == ROUTE_TYPE_DRIVE) {// 驾车路径规划
            RouteSearch.DriveRouteQuery query = new RouteSearch.DriveRouteQuery(fromAndTo, mode, null,
                    null, "");// 第一个参数表示路径规划的起点和终点，第二个参数表示驾车模式，第三个参数表示途经点，第四个参数表示避让区域，第五个参数表示避让道路
            mRouteSearch.calculateDriveRouteAsyn(query);// 异步路径规划驾车模式查询
        } else if (routeType == ROUTE_TYPE_WALK) {// 步行路径规划
            RouteSearch.WalkRouteQuery query = new RouteSearch.WalkRouteQuery(fromAndTo, mode);
            mRouteSearch.calculateWalkRouteAsyn(query);// 异步路径规划步行模式查询
        } //else if (routeType == ROUTE_TYPE_CROSSTOWN) {
        //RouteSearch.FromAndTo fromAndTo_bus = new RouteSearch.FromAndTo(
        //mStartPoint_bus, mEndPoint_bus);
        //RouteSearch.BusRouteQuery query = new RouteSearch.BusRouteQuery(fromAndTo_bus, mode,
        //"呼和浩特市", 0);// 第一个参数表示路径规划的起点和终点，第二个参数表示公交查询模式，第三个参数表示公交查询城市区号，第四个参数表示是否计算夜班车，0表示不计算
//            query.setCityd("农安县");
        //mRouteSearch.calculateBusRouteAsyn(query);// 异步路径规划公交模式查询
        // }
    }


    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onBusRouteSearched(BusRouteResult result, int errorCode) {
        dissmissProgressDialog();
        mBottomLayout.setVisibility(View.GONE);
        aMap.clear();// 清理地图上的所有覆盖物
        if (errorCode == 1000) {
            if (result != null && result.getPaths() != null) {
                if (result.getPaths().size() > 0) {
                    mBusRouteResult = result;
                    BusResultListAdapter mBusResultListAdapter = new BusResultListAdapter(mContext, mBusRouteResult);
                    mBusResultList.setAdapter(mBusResultListAdapter);
                } else if (result != null && result.getPaths() == null) {
                    ToastUtil.show(mContext, R.string.no_result);
                }
            } else {
                ToastUtil.show(mContext, R.string.no_result);
            }
        } else {
            ToastUtil.showerror(this.getApplicationContext(), errorCode);
        }
    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {

    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {

    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

    }


    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if (progDialog == null)
            progDialog = new ProgressDialog(this);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(true);
        progDialog.setMessage("正在搜索...");
        progDialog.show();
    }

    /**
     * 隐藏进度框
     */
    private void dissmissProgressDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
    public void onGeocodeSearched(GeocodeResult result, int rCode) {
        if (rCode == 1000) {
            if (result != null && result.getGeocodeAddressList() != null
                    && result.getGeocodeAddressList().size() > 0) {
                GeocodeAddress address = result.getGeocodeAddressList().get(0);
                latitude=address.getLatLonPoint().getLatitude();
                longitude=address.getLatLonPoint().getLongitude();
                if(count==0){
                    mStartPoint=new LatLonPoint(latitude,longitude);
                    LatLng ll=new LatLng(latitude,longitude);
                    CameraUpdate update= CameraUpdateFactory.newLatLng(ll);
                    aMap.animateCamera(update);
                    aMap.addMarker(new MarkerOptions()
                            .position(AMapUtil.convertToLatLng(mStartPoint))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.start)));
                    count=1;
                }else if(count==1){
                    mEndPoint=new LatLonPoint(latitude,longitude);
                    aMap.addMarker(new MarkerOptions()
                            .position(AMapUtil.convertToLatLng(mEndPoint))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.end)));
                    count=0;
                }
            } else {
                ToastUtil.show(TransferQuery.this, R.string.no_result);
            }
        } else {
            ToastUtil.showerror(this, rCode);
        }
    }

    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
    }
}

