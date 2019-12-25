package com.btsoft.opencvapp2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import com.btsoft.opencvapp2.ui.main.SectionsPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import org.opencv.android.OpenCVLoader;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    // UI
    private static MainActivity mContext;
    private ViewPager viewPager;
    private TabLayout tabs;

    public void makeFullScreen(){
        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus) {
            makeFullScreen();
        }
    }

    public static Context getContextOfApp(){
        return mContext;
    }


    static {
        if(!OpenCVLoader.initDebug())
            Log.d("ERROR", "Unable to load CV");
        else
            Log.d("SUCCESS", "OpenCV loaded");
    }



    public MainActivity(){
        Log.i(TAG, "Instantiated new " + this.getClass());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkAppPermissions();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        mContext = this;


        // Create Pager Adapter
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        try {
            sectionsPagerAdapter.AddFragment(new ScannerActivity(), "Scanner");
        } catch (IOException e) {
            e.printStackTrace();
        }
        sectionsPagerAdapter.AddFragment(new LibraryActivity(), "Lıbrary");

        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
    }

    private void checkAppPermissions() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
        }
    }

}
