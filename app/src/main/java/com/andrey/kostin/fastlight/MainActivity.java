package com.andrey.kostin.fastlight;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import java.io.IOException;
import java.util.List;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.RelativeLayout;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, SurfaceHolder.Callback{
    private static final String TAG = "MYLOG";
    private static final String WAKE_LOCK_TAG = "TORCH_WAKE_LOCK";

    private static final int COLOR_DARK = 0xFF455A64;
    private static final int COLOR_LIGHT = 0xFFCFD8DC;
    private static final int COLOR_WHITE = 0xFFFFFFFF;


    private Camera mCamera;
    private boolean lightOn;
    private boolean previewOn;

    private WakeLock wakeLock;

    private CoordinatorLayout coord;
    Snackbar snackbar;

    FloatingActionButton fab;
    RelativeLayout layout;


    private void toggleLight() {
        if (lightOn) {
            turnLightOff();
        } else {
            turnLightOn();
        }
    }

    private void turnLightOn() {



        if (mCamera == null) {
            snackbar=Snackbar.make(coord, "Camera is missing", Snackbar.LENGTH_LONG);
            snackbar.show();

            // Use the screen as a flashlight (next best thing)
            layout.setBackgroundColor(COLOR_WHITE);
            return;
        }
        lightOn = true;
        Parameters parameters = mCamera.getParameters();
        if (parameters == null) {
            // Use the screen as a flashlight (next best thing)
            layout.setBackgroundColor(COLOR_WHITE);
            return;
        }
        List<String> flashModes = parameters.getSupportedFlashModes();
        // Check if camera flash exists
        if (flashModes == null) {
            // Use the screen as a flashlight (next best thing)
            layout.setBackgroundColor(COLOR_WHITE);
            return;
        }
        String flashMode = parameters.getFlashMode();
        Log.d(TAG, "Flash mode: " + flashMode);
        Log.d(TAG, "Flash modes: " + flashModes);
        if (!Parameters.FLASH_MODE_TORCH.equals(flashMode)) {
            // Turn on the flash
            if (flashModes.contains(Parameters.FLASH_MODE_TORCH)) {
                parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(parameters);
                layout.setBackgroundColor(COLOR_LIGHT);
                startWakeLock();
            } else {

                snackbar=Snackbar.make(coord, "Torch mode is not support", Snackbar.LENGTH_LONG);
                snackbar.show();

                // Use the screen as a flashlight (next best thing)
                layout.setBackgroundColor(COLOR_WHITE);
                Log.d(TAG, "FLASH_MODE_TORCH not supported");
            }
        }
    }

    private void turnLightOff() {
        if (lightOn) {
            // set the background to dark
            layout.setBackgroundColor(COLOR_DARK);
            lightOn = false;
            if (mCamera == null) {
                return;
            }
            Parameters parameters = mCamera.getParameters();
            if (parameters == null) {
                return;
            }
            List<String> flashModes = parameters.getSupportedFlashModes();
            String flashMode = parameters.getFlashMode();
            // Check if camera flash exists
            if (flashModes == null) {
                return;
            }
            Log.d(TAG, "Flash mode: " + flashMode);
            Log.d(TAG, "Flash modes: " + flashModes);
            if (!Parameters.FLASH_MODE_OFF.equals(flashMode)) {
                // Turn off the flash
                if (flashModes.contains(Parameters.FLASH_MODE_OFF)) {
                    parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
                    mCamera.setParameters(parameters);
                    stopWakeLock();
                } else {
                    Log.d(TAG, "FLASH_MODE_OFF not supported");
                }
            }
        }
    }

    private void startPreview() {
        if (!previewOn && mCamera != null) {
            mCamera.startPreview();
            previewOn = true;
        }
    }

    private void stopPreview() {
        if (previewOn && mCamera != null) {
            mCamera.stopPreview();
            previewOn = false;
        }
    }

    private void startWakeLock() {
        if (wakeLock == null) {
            Log.d(TAG, "wakeLock is null, getting a new WakeLock");
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            Log.d(TAG, "PowerManager acquired");
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG);
            Log.d(TAG, "WakeLock set");
        }
        wakeLock.acquire();
        Log.d(TAG, "WakeLock acquired");
    }

    private void stopWakeLock() {
        if (wakeLock != null) {
            wakeLock.release();
            Log.d(TAG, "WakeLock released");
        }
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.main);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(this);
            layout=(RelativeLayout)findViewById(R.id.fon);
            coord=(CoordinatorLayout)findViewById(R.id.coord);




        SurfaceView surfaceView = (SurfaceView) this.findViewById(R.id.surfaceview);
        SurfaceHolder sHolder = surfaceView.getHolder();
        sHolder.addCallback(this);
        sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//отключаем засыпание экрана

        Log.d(TAG, "onCreate");

        if (mCamera == null) {             //если камера не привязана к переменной то:
            try {mCamera = Camera.open();  //привязываем к переменной открытую камеру.
            } catch (Exception e) {Log.d(TAG, "Camera.open() failed: " + e.getMessage());}
        }
        startPreview();
        turnLightOn();  //включаем вспышку
  }

    @Override
    public void onStart() {//метод выполняется после ОнКрейт перед ОнРезюме - привязка к открытию камеры
        super.onStart();
        if (mCamera == null) {             //если камера не привязана к переменной то:
            try {mCamera = Camera.open();  //привязываем к переменной открытую камеру.
            } catch (Exception e) {Log.d(TAG, "Camera.open() failed: " + e.getMessage());}
        }
        startPreview();
    }

    @Override
    public void onResume() {
        super.onResume();
        turnLightOn();
        Log.d(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        turnLightOff();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
    if(mCamera != null) {
            stopPreview();
    mCamera.release();
    mCamera = null;
        }
        Log.d(TAG, "onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    if(mCamera != null) {
            turnLightOff();
            stopPreview();
    mCamera.release();
        }
        Log.d(TAG, "onDestroy");
    }



    @Override
    public void surfaceChanged(SurfaceHolder holder, int I, int J, int K) {
        Log.d(TAG, "surfaceChanged");
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");
        try {
    mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        Integer id;
        id=view.getId();
        switch (id){
            case R.id.fab:
                toggleLight();
                break;
            default:break;
                    }
    }
}
