package com.solo.map;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;


public class MainActivity extends ActionBarActivity
{

    MapView mMapView = null;
    BaiduMap mBaiduMap=null;

    //定位相关
    LocationClient mLocationLient=null;
    MyLocationListener mLocationListener=null;
    boolean isFirstIn=true;
    double mLatitude;
    double mLongitude;
    //自定义定位图标
    BitmapDescriptor mIconLocation=null;

    //方向相关
    MyOrientationListener mOrientationListener;
    float mCurrentX;

    //
    LocationMode mLocationMode;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        initViews();
        initLocation();

    }

    void initViews()
    {
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap=mMapView.getMap();
        MapStatusUpdate msu= MapStatusUpdateFactory.zoomTo(15.0f);
        mBaiduMap.setMapStatus(msu);
    }

    void initLocation()
    {
        mLocationMode = LocationMode.NORMAL;
        mLocationLient=new LocationClient(this);
        mLocationListener=new MyLocationListener();
        mLocationLient.registerLocationListener(mLocationListener);

        LocationClientOption option=new LocationClientOption();
        option.setCoorType("bd09ll");
        option.setIsNeedAddress(true);
        option.setOpenGps(true);
        option.setScanSpan(1000);

        mLocationLient.setLocOption(option);

        //初始化图标
        mIconLocation= BitmapDescriptorFactory
                .fromResource(R.mipmap.navi_map_gps_locked);

        //初始化反向监听器
        mOrientationListener=new MyOrientationListener(this);
        mOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener()
        {
            @Override
            public void onOrientationChanged(float x)
            {
                Log.i("TAG","x="+x);
                mCurrentX=x;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        mBaiduMap.setMyLocationEnabled(true);
        if(!mLocationLient.isStarted())
        {
            mLocationLient.start();
        }

        //开启方向传感器
        mOrientationListener.start();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        mBaiduMap.setMyLocationEnabled(false);
        mLocationLient.stop();
        //停止方向传感器
        mOrientationListener.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.map_common:
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                break;
            case R.id.map_site:
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.map_traffic:
                if(mBaiduMap.isTrafficEnabled())
                {
                    mBaiduMap.setTrafficEnabled(false);
                }
                else
                {
                    mBaiduMap.setTrafficEnabled(true);
                }

                break;
            case R.id.my_location:
                centerToMyLocation();
                break;
            case R.id.map_mode_normal:
                mLocationMode=LocationMode.NORMAL;
                break;
            case R.id.map_mode_follow:
                mLocationMode=LocationMode.FOLLOWING;
                break;
            case R.id.my_map_mode_compass:
                mLocationMode=LocationMode.COMPASS;
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public class MyLocationListener implements BDLocationListener
    {
        @Override
        public void onReceiveLocation(BDLocation bdLocation)
        {
//            Log.i("TAG","onReceiveLocation");
            mLatitude=bdLocation.getLatitude();
            mLongitude=bdLocation.getLongitude();
            //在地图上显示我的位置
            MyLocationData data=new MyLocationData.Builder()//
                    .accuracy(bdLocation.getRadius())//
                    .direction(mCurrentX)
                    .latitude(mLatitude)//
                    .longitude(mLongitude)//
                    .build();//
            mBaiduMap.setMyLocationData(data);


//            Log.i("TAG","la="+mLatitude+" lo="+mLongitude+" ra="+bdLocation.getRadius());
            //设置自定图标
            MyLocationConfiguration config=new
                    MyLocationConfiguration(mLocationMode,
                    true,mIconLocation);
            mBaiduMap.setMyLocationConfigeration(config);

            if(isFirstIn)
            {
                //在地图上显示到指定的位置(经纬度)
                LatLng latLng=new LatLng(mLatitude,mLongitude);
                MapStatusUpdate msu= MapStatusUpdateFactory.newLatLng(latLng);
                mBaiduMap.animateMapStatus(msu);
                isFirstIn=false;

                Toast.makeText(MainActivity.this,bdLocation.getAddrStr(),
                        Toast.LENGTH_SHORT).show();
            }

        }
    }

    void centerToMyLocation()
    {
        LatLng latLng=new LatLng(mLatitude,mLongitude);
        MapStatusUpdate msu= MapStatusUpdateFactory.newLatLng(latLng);
        mBaiduMap.animateMapStatus(msu);
    }
}


