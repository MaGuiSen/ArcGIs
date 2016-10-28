/* Copyright 2015 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.mumu.arcgis;

import android.Manifest;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.AngularUnit;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.geometry.Unit;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import lib.util.FileUtil;

public class MapActivity extends BaseActivity {
    @Bind(R.id.lay_loading)
    LinearLayout probLoading;
    @Bind(R.id.progressBar)
    ProgressBar progressBar;
    @Bind(R.id.mapView)
    MapView mMapView;
    @Bind(R.id.txt_seconds)
    TextView txtSeconds;
    @Bind(R.id.txt_d_seconds)
    TextView txtDSeconds;

    String tileURL;
    LocationDisplayManager mLDM;
    SpatialReference mMapSr = null;


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    mMapView.setVisibility(View.VISIBLE);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Animator step1 = ObjectAnimator.ofFloat(probLoading, "scaleY", 1, 0f);
                            Animator step2 = ObjectAnimator.ofFloat(probLoading, "scaleX", 1, 0f);
                            AnimatorSet set = new AnimatorSet();
                            set.playTogether(step1,step2);
                            set.setDuration(500);
                            set.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    probLoading.setVisibility(View.GONE);
                                    needTimes = false;
                                }
                            });
                            set.start();
                        }
                    }, 500);
                    break;
                case 1:
                    if(!needTimes){
                        return;
                    }
                    currTimeMill++;
                    long seconds = currTimeMill / 10;
                    long dSeconds = currTimeMill % 10;
                    txtSeconds.setText(seconds < 10 ? "0"+seconds : seconds + "");
                    txtDSeconds.setText(dSeconds+"");
                    handler.sendEmptyMessageDelayed(1,100);
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("here","2");
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);
        progressBar.setIndeterminate(true);//设置自动
        mMapView.setVisibility(View.INVISIBLE);
        mMapView.setOnStatusChangedListener(statusChangedListener);
        mMapView.setMapBackground(Color.WHITE, Color.WHITE, 0, 0);

        mMapView.enableWrapAround(true);
//        mMapView.centerAt(getAsPoint(MyApplication.currLocation), false);
        tileURL = "http://cache1.arcgisonline.cn/ArcGIS/rest/services/ChinaOnlineCommunity/MapServer";
        final ArcGISTiledMapServiceLayer tilelayer3 = new ArcGISTiledMapServiceLayer(tileURL);

        final ArcGISTiledMapServiceLayer tilelayer2 = new ArcGISTiledMapServiceLayer("http://services.arcgisonline.com/arcgis/rest/services/ESRI_StreetMap_World_2D/MapServer");

        File patchDic = new File(FileUtil.getDataCacheDir(FileUtil.Patch));
        if (patchDic.exists()) {
            File[] files = patchDic.listFiles();
            for (File file : files) {
                if (file.exists()) {
                    String loca = "file://" + file.getAbsolutePath();
                    final ArcGISLocalTiledLayer localTiledLayerChina = new ArcGISLocalTiledLayer(loca);
                    mMapView.addLayer(localTiledLayerChina);
                }
            }
        }
        Log.e("here","3");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mMapView.addLayer(tilelayer2, 0);
            }
        }, 400);
        setupLocator();
        handler.sendEmptyMessageDelayed(1,100);
        Log.e("here","4");
    }

    int currTimeMill = 0;
    boolean needTimes = true;


    /**
     * When map is ready, set up the LocationDisplayManager.
     */
    final OnStatusChangedListener statusChangedListener = new OnStatusChangedListener() {

        private static final long serialVersionUID = 1L;

        @Override
        public void onStatusChanged(Object source, STATUS status) {
            if (source == mMapView && status == STATUS.INITIALIZED) {
                mMapSr = mMapView.getSpatialReference();
                if (mLDM == null) {
                    setupLocationListener();
                }
                if (!mMapView.isShown()) {
                    handler.sendEmptyMessageDelayed(0, 300);
                }
            }
        }
    };

    private void setupLocator() {
        findViewById(R.id.img_locate_self).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMapView.isLoaded()) {
                    // If LocationDisplayManager has a fix, pan to that location. If no
                    // fix yet, this will happen when the first fix arrives, due to
                    // callback set up previously.
                    if ((mLDM != null) && (mLDM.getLocation() != null)) {
                        // Keep current scale and go to current location, if there is one.
                        mLDM.setAutoPanMode(LocationDisplayManager.AutoPanMode.LOCATION);
                    }
                }
            }
        });
    }

    Location currLocation = null;
    private void setupLocationListener() {
        if ((mMapView != null) && (mMapView.isLoaded())) {
            mLDM = mMapView.getLocationDisplayManager();
            mLDM.setAutoPanMode(LocationDisplayManager.AutoPanMode.LOCATION);
            mLDM.setLocationListener(new LocationListener() {
                // Zooms to the current location when first GPS fix arrives.
                boolean autoCenter = true;
                @Override
                public void onLocationChanged(Location loc) {
                    currLocation = loc;
                    if(autoCenter){
                        autoCenter = false;
                        zoomToLocation(loc);
                    }
                    // After zooming, turn on the Location pan mode to show the location
                    // symbol. This will disable as soon as you interact with the map.
//                    mLDM.setAutoPanMode(LocationDisplayManager.AutoPanMode.LOCATION);
                }

                @Override
                public void onProviderDisabled(String arg0) {
                }

                @Override
                public void onProviderEnabled(String arg0) {
                }

                @Override
                public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
                }
            });
            Log.e("here","11");
            if (permissionIsGet(REQ_lOCATION_B_PMS, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Log.e("here","7");
                mLDM.start();
            }
        }
    }

    /**
     * Zoom to location using a specific size of extent.
     *
     * @param loc the location to center the MapView at
     */
    private void zoomToLocation(Location loc) {
        if (loc == null) {
            return;
        }
        try {
            Point mapPoint = getAsPoint(loc);
            Unit mapUnit = mMapSr.getUnit();
            double ZOOM_BY = 3;
            double zoomFactor = Unit.convertUnits(ZOOM_BY, Unit.create(AngularUnit.Code.DEGREE), mapUnit);
            Envelope zoomExtent = new Envelope(mapPoint, zoomFactor, zoomFactor);
            mMapView.setExtent(zoomExtent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Point getAsPoint(Location loc) {
        Point wgsPoint = new Point();
        if (loc != null) {
            wgsPoint = new Point(loc.getLongitude(), loc.getLatitude());
        }
        return (Point) GeometryEngine.project(wgsPoint, SpatialReference.create(SpatialReference.WKID_WGS84), mMapSr);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.pause();
        if (mLDM != null) {
            mLDM.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.unpause();
        mMapSr = mMapView.getSpatialReference();
        if (mLDM != null) {
            mLDM.resume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLDM != null) {
            mLDM.stop();
        }
    }
}