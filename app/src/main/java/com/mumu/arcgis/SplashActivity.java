package com.mumu.arcgis;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import lib.util.DensityUtil;
import lib.util.ScreenUtil;

public class SplashActivity extends BaseActivity {

    @Bind(R.id.llay_go_map)
    LinearLayout llayGoMap;

    @Bind(R.id.img_yun_1)
    ImageView imgYun1;
    @Bind(R.id.img_yun_2)
    ImageView imgYun2;
    @Bind(R.id.img_yun_3)
    ImageView imgYun3;
    @Bind(R.id.img_lighthouse)
    ImageView imgLighthouse;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        screenW = ScreenUtil.getScreenSize(this, null)[0];
        llayGoMap.setVisibility(View.GONE);
        initAnim();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                initGoMapBtnAnim(llayGoMap);
            }
        },800);
        if (permissionIsGet(REQ_lOCATION_PMS, Manifest.permission.ACCESS_FINE_LOCATION)) {
        }
        if (permissionIsGet(REQ_lOCATION_B_PMS, Manifest.permission.ACCESS_COARSE_LOCATION)) {
        }
    }

    private void initAnim() {
        initYunAnim();
        initLightAnim(imgLighthouse);
    }

    /**
     * 清除所有在跑的动画
     */
    private void clearAllAnim() {
        for(Animator animator:animatorList){
            if(animator != null){
                animator.cancel();
            }
        }
        animatorList.clear();
    }

    int screenW = 0;
    List<Animator> animatorList = new ArrayList<>();

    int left_yun1 = 0;
    int left_yun2 = 0;
    int left_yun3 = 0;
    public void initYunAnim() {
        Animator a1 = createYunAnimate(imgYun1, 30000, DensityUtil.dip2px(this,250));
        Animator a2 = createYunAnimate(imgYun2, 50000,DensityUtil.dip2px(this,20));
        Animator a3 = createYunAnimate(imgYun3, 40000,DensityUtil.dip2px(this,100));
        animatorList.add(a1);
        animatorList.add(a2);
        animatorList.add(a3);
    }

    private Animator createYunAnimate(final View target, int secondTime,int startLeft) {
        Log.e("left",startLeft+"");
        Animator step1 = ObjectAnimator.ofFloat(target, "x", screenW);
        step1.setDuration(secondTime).setInterpolator(new LinearInterpolator());
        step1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                target.setVisibility(View.GONE);
            }
        });
        Animator step2 = ObjectAnimator.ofFloat(target, "x", -screenW/5);
        step2.setDuration(100).setInterpolator(new LinearInterpolator());
        step2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                target.setVisibility(View.VISIBLE);
            }
        });
        Animator step3 = ObjectAnimator.ofFloat(target, "x", 0);
        step3.setDuration(secondTime/5).setInterpolator(new LinearInterpolator());
        final AnimatorSet yunSet = new AnimatorSet();
        yunSet.playSequentially(step2,step3,step1);
        yunSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                yunSet.start();
            }
        });

        Animator begin = ObjectAnimator.ofFloat(target, "translationX", screenW-startLeft);
        begin.setDuration(secondTime).setInterpolator(new LinearInterpolator());
        begin.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                target.setVisibility(View.GONE);
                yunSet.start();
            }
        });
        begin.start();

        return yunSet;
    }

    public void initLightAnim(View target){
        int rotateDegree = 10;
        int transNum = 20;
        Animator step1 = ObjectAnimator.ofFloat(target, "rotation", 0, rotateDegree);
        step1.setDuration(1000).setInterpolator(new DecelerateInterpolator());
        Animator step2 = ObjectAnimator.ofFloat(target, "rotation", rotateDegree, -rotateDegree);
        step2.setDuration(2000).setInterpolator(new AccelerateDecelerateInterpolator());
        Animator step3 = ObjectAnimator.ofFloat(target, "rotation", -rotateDegree, 0);
        step3.setDuration(1000).setInterpolator(new AccelerateInterpolator());

        Animator upDownStep1 = ObjectAnimator.ofFloat(target, "translationY", -transNum);
        upDownStep1.setDuration(1000).setInterpolator(new AccelerateDecelerateInterpolator());

        Animator upDownStep2 = ObjectAnimator.ofFloat(target, "translationY", 0);
        upDownStep2.setDuration(1000).setInterpolator(new LinearInterpolator());

        final AnimatorSet setTrans = new AnimatorSet();
        setTrans.playSequentially(upDownStep1, upDownStep2);
        setTrans.start();
        setTrans.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                setTrans.start();
            }
        });
        final AnimatorSet setRotate = new AnimatorSet();
        setRotate.playSequentially(step1, step2, step3);
        setRotate.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                setRotate.start();
            }
        });
        setRotate.start();
        animatorList.add(setRotate);
        animatorList.add(setTrans);
    }

    public void initGoMapBtnAnim(View target){
        target.setVisibility(View.VISIBLE);
        Animator step1 = ObjectAnimator.ofFloat(target, "scaleX",0,1f);
        step1.setDuration(800).setInterpolator(new LinearInterpolator());
        Animator step2 = ObjectAnimator.ofFloat(target, "scaleY",0,1f);
        step2.setDuration(800).setInterpolator(new LinearInterpolator());

        Animator step3 = ObjectAnimator.ofFloat(target, "alpha",0,1);
        step3.setDuration(800).setInterpolator(new LinearInterpolator());
        final AnimatorSet setScale = new AnimatorSet();
        setScale.playTogether(step1,step2,step3);
        setScale.start();
    }

    @OnClick({R.id.llay_go_map})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llay_go_map:
                Log.e("here","1");
                startActivity(new Intent(this, MapActivity.class));
            break;
        }
    }

    // 再按一次退出
    private long firstTime;
    private long secondTime;
    private long spaceTime;

    @Override
    public void onBackPressed() {
        firstTime = System.currentTimeMillis();
        spaceTime = firstTime - secondTime;
        secondTime = firstTime;
        if (spaceTime > 2000) {
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_LONG).show();
        } else {
            super.onBackPressed();
            System.exit(0);
        }
    }
}
