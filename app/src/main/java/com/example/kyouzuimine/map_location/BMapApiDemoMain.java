package com.example.kyouzuimine.map_location;


import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.baidu.mapapi.overlayutil.TransitRouteOverlay;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;

/**
 * 此demo用来展示如何进行驾车、步行、公交路线搜索并在地图使用RouteOverlay、TransitOverlay绘制
 * 同时展示如何进行节点浏览并弹出泡泡
 */
public class BMapApiDemoMain extends Activity implements BaiduMap.OnMapClickListener,
		OnGetRoutePlanResultListener {
	//浏览路线节点相关
	RouteLine route = null;
	OverlayManager routeOverlay = null;
	boolean useDefaultIcon = false;
	private TextView popupText = null;//泡泡view
	PlanNode stNode = null;

	//地图相关，
	MapView mMapView = null;    // 地图View
	BaiduMap mBaidumap = null;

	//路径搜索相关
	RoutePlanSearch mSearch = null;    // 搜索模块，也可去掉地图模块独立使用

	//定位相关
	LocationClient mLocClient;
	boolean isFirstLoc = true;// 是否首次定位
	public MyLocationListenner myListener = new MyLocationListenner();//定位监视

	//信息相关，用于创造以及用例显示信息
	private Marker mMarker1;
	private Marker mMarker2;
	private Marker mMarker3;
	private InfoWindow mInfoWindow;


	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_routeplan);
		CharSequence titleLable = "求助";
		setTitle(titleLable);

		//初始化地图
		mMapView = (MapView) findViewById(R.id.map);
		mBaidumap = mMapView.getMap();

		//消除多余图标or空间
		int count = mMapView.getChildCount();
		init();
		for (int i = 0; i < count; i++) {
			View child = mMapView.getChildAt(i);
			if (child instanceof ZoomControls || child instanceof ImageView) {
				child.setVisibility(View.INVISIBLE);
			}
		}

		//地图点击事件处理
		mBaidumap.setOnMapClickListener(this);
		// 初始化搜索模块，注册事件监听
		mSearch = RoutePlanSearch.newInstance();
		mSearch.setOnGetRoutePlanResultListener(this);

		//定位相关
		mBaidumap.setMyLocationEnabled(true);
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(1000);
		mLocClient.setLocOption(option);
		mLocClient.start();

		//取消求助、继续求助、结束求助按钮
		/*
		canButton = (Button) findViewById(R.id.canHelp);
		endButton = (Button) findViewById(R.id.endHelp);
		conButton = (Button) findViewById(R.id.conHelp);
		*/


		// 样例按钮设置
		mBaidumap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
			public boolean onMarkerClick(final Marker marker) {
				Button button = new Button(getApplicationContext());
				button.setBackgroundResource(R.drawable.popup);
				InfoWindow.OnInfoWindowClickListener listener = null;
				if (marker == mMarker1) {
					button.setText("基本信息&跳转");
					button.setTextColor(Color.BLACK);
                    /*这里是跳转button
                    listener = new InfoWindow.OnInfoWindowClickListener() {
                        public void onInfoWindowClick() {
                        }
                    };
                    */
					LatLng ll = marker.getPosition();
					mInfoWindow = new InfoWindow(BitmapDescriptorFactory.fromView(button), ll, -47, listener);
					mBaidumap.showInfoWindow(mInfoWindow);
				} else if (marker == mMarker2) {
					button.setText("这里是跳转");
					button.setTextColor(Color.BLACK);
                    /*这里是跳转button
                    button.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                        }

                    });
                    */
					LatLng ll = marker.getPosition();
					mInfoWindow = new InfoWindow(button, ll, -47);
					mBaidumap.showInfoWindow(mInfoWindow);
				} else if (marker == mMarker3) {
					button.setText("这里是跳转");
					button.setTextColor(Color.BLACK);
                    /*这里是跳转button
                    button.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                        }
                    });
                    */
					LatLng ll = marker.getPosition();
					mInfoWindow = new InfoWindow(button, ll, -47);
					mBaidumap.showInfoWindow(mInfoWindow);
				}
				return true;
			}
		});

	}

	/**
	 * 发起路线规划搜索示例
	 *
	 * @param v
	 */
	public void SearchButtonProcess(View v) {
		//重置浏览节点的路线数据
		route = null;
		mBaidumap.clear();
		// 处理搜索按钮响应
		init();
		LatLng pt2 = new LatLng(23.03777, 113.496627);
		PlanNode enNode = PlanNode.withLocation(pt2);
		BitmapDescriptor bd = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);
		OverlayOptions o2 = new MarkerOptions().icon(bd).position(pt2);
		mBaidumap.addOverlay(o2);

		//按键控制
		if (v.getId() == R.id.drive) {
			mSearch.drivingSearch((new DrivingRoutePlanOption())
					.from(stNode)
					.to(enNode));
		} else if (v.getId() == R.id.walk) {
			mSearch.walkingSearch((new WalkingRoutePlanOption())
					.from(stNode)
					.to(enNode));
		}
	}

	/**
	 * 切换路线图标，刷新地图使其生效
	 * 注意： 起终点图标使用中心对齐.
	 */


	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	//获得走路路线
	public void onGetWalkingRouteResult(WalkingRouteResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(BMapApiDemoMain.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
		}
		if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
			//起终点或途经点地址有岐义，通过以下接口获取建议查询信息
			//result.getSuggestAddrInfo()
			return;
		}
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			route = result.getRouteLines().get(0);
			WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaidumap);
			mBaidumap.setOnMarkerClickListener(overlay);
			routeOverlay = overlay;
			overlay.setData(result.getRouteLines().get(0));
			overlay.addToMap();
			overlay.zoomToSpan();
		}

	}

	@Override
	public void onGetTransitRouteResult(TransitRouteResult result) {}

	@Override
	//获得驾车路线
	public void onGetDrivingRouteResult(DrivingRouteResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(BMapApiDemoMain.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
		}
		if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
			//起终点或途经点地址有岐义，通过以下接口获取建议查询信息
			//result.getSuggestAddrInfo()
			return;
		}
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			route = result.getRouteLines().get(0);
			DrivingRouteOverlay overlay = new MyDrivingRouteOverlay(mBaidumap);
			routeOverlay = overlay;
			mBaidumap.setOnMarkerClickListener(overlay);
			overlay.setData(result.getRouteLines().get(0));
			overlay.addToMap();
			overlay.zoomToSpan();
		}
	}

	//定制RouteOverly
	private class MyDrivingRouteOverlay extends DrivingRouteOverlay {

		public MyDrivingRouteOverlay(BaiduMap baiduMap) {
			super(baiduMap);
		}
        /*
        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }
        */
	}

	private class MyWalkingRouteOverlay extends WalkingRouteOverlay {

		public MyWalkingRouteOverlay(BaiduMap baiduMap) {
			super(baiduMap);
		}

		@Override
		//设置起点图标
		public BitmapDescriptor getStartMarker() {
			if (useDefaultIcon) {
				return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
			}
			return null;
		}

		@Override
		//设置重点图标
		public BitmapDescriptor getTerminalMarker() {
			if (useDefaultIcon) {
				return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
			}
			return null;
		}
	}

	@Override
	public void onMapClick(LatLng point) {
		mBaidumap.hideInfoWindow();
	}

	@Override
	public boolean onMapPoiClick(MapPoi poi) {
		return false;
	}

	@Override
	protected void onPause() {
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		mSearch.destroy();
		mMapView.onDestroy();
		super.onDestroy();
	}


	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置
			if (location == null || mMapView == null)
				return;
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
							// 此处设置开发者获取到的方向信息，顺时针0-360
					.direction(100).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			mBaidumap.setMyLocationData(locData);

			//判断是否第一次定位
			if (isFirstLoc) {
				isFirstLoc = false;
				LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaidumap.animateMapStatus(u);
			}
			LatLng pt1 = new LatLng(location.getLatitude(),
					location.getLongitude());
			stNode = PlanNode.withLocation(pt1);
		}

		// 没被调用
		public void onReceivePoi(BDLocation poiLocation) {}
	}


	//初始化测试点测试样例
	public void init() {
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		LatLng pt4 = new LatLng(23.063309, 113.394004);
		LatLng pt2 = new LatLng(23.062578, 113.410821);
		LatLng pt3 = new LatLng(23.045286, 113.405934);

		BitmapDescriptor bd = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);
		OverlayOptions o1 = new MarkerOptions().icon(bd).position(pt4);
		OverlayOptions o2 = new MarkerOptions().icon(bd).position(pt2);
		OverlayOptions o3 = new MarkerOptions().icon(bd).position(pt3);

		mBaidumap.addOverlay(o1);
		mBaidumap.addOverlay(o2);
		mBaidumap.addOverlay(o3);

		builder.include(pt4);
		builder.include(pt2);
		builder.include(pt3);

		LatLng pt5 = new LatLng(23.03777, 113.496627);
		OverlayOptions o5 = new MarkerOptions().icon(bd).position(pt5);
		mBaidumap.addOverlay(o5);

		mMarker1 = (Marker) (mBaidumap.addOverlay(o1));
		mMarker2 = (Marker) (mBaidumap.addOverlay(o2));
		mMarker3 = (Marker) (mBaidumap.addOverlay(o3));
	}
	public void endButton(View view) {

	}
	public void conButton(View view) {}
	public void canButton(View view) {}
}
