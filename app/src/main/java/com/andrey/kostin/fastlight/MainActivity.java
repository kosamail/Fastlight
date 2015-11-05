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
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.RelativeLayout;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, SurfaceHolder.Callback{
    private static final String TAG = "MYLOG";

    private static final int COLOR_DARK = 0xFF455A64;
    private static final int COLOR_LIGHT = 0xFFCFD8DC;
    private static final int COLOR_WHITE = 0xFFFFFFFF;

    private Camera mCamera;
    private boolean lightOn;//переменная определяющая 1-включить вспышку 0-выключить
    private boolean previewOn;
    SurfaceView preview;//Обязательным условием при работе с камерой является создание окна предпросмотра (preview)
    SurfaceHolder surfHold; //переменная для холдера камеры. работа с preview ведется не напрямую, а через посредника – SurfaceHolder (surfholder) Именно с этим объектом умеет работать Camera. surfholder будет сообщать нам о том, что surface готов к работе, изменен или более недоступен.
                            //Camera берет holder и с его помощью выводит изображение на preview.
    private CoordinatorLayout coord;
    Snackbar snackbar;//переменная для снекбара
    Toolbar toolbar;  //переменная для тулбара

    FloatingActionButton fab;
    RelativeLayout layout;


    private void turnLightOn() {
        if (mCamera == null) {
            snackbar=Snackbar.make(coord, "Camera is missing", Snackbar.LENGTH_LONG);
            snackbar.show();

            // Используем экран как вспышку
            layout.setBackgroundColor(COLOR_WHITE);
            return;
        }
        lightOn = true;
        Parameters parameters = mCamera.getParameters();
        if (parameters == null) {
            // Используем экран как вспышку
            layout.setBackgroundColor(COLOR_WHITE);
            return;
        }
        List<String> flashModes = parameters.getSupportedFlashModes();
        // Check if camera flash exists
        if (flashModes == null) {
            // Используем экран как вспышку
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
                //nosleepON();//здесь запрещаем смартфону засыпать - устанавливаем wakelock
            } else {
                snackbar=Snackbar.make(coord, "Torch mode is not support", Snackbar.LENGTH_LONG);
                snackbar.show();
                // Используем экран как вспышку
                layout.setBackgroundColor(COLOR_WHITE);
                Log.d(TAG, "FLASH_MODE_TORCH not supported");
            }
        }
    }

    private void turnLightOff() {
        if (lightOn) {
            layout.setBackgroundColor(COLOR_DARK);//устанавливаем темный бекграунд
            lightOn = false;
            if (mCamera == null) return;
            Parameters parameters = mCamera.getParameters();
            if (parameters == null) return;
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
                    //nosleepOFF();//отключаем запрет на засыпание смартфона - снимаем wakelock
                } else {
                    Log.d(TAG, "FLASH_MODE_OFF not supported");
                }
            }
        }
    }


    private void stopPreview() {
        if (previewOn && mCamera != null) {
            mCamera.stopPreview();
            previewOn = false;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.main);
            toolbar = (Toolbar) findViewById(R.id.toolbar); //привязываем переменную к тулбару
            setSupportActionBar(toolbar);                   //устанавливает для тулбара поведение экшнбара
            fab = (FloatingActionButton) findViewById(R.id.fab);//привязываем переменную к флоатинг экшн буттон
            fab.setOnClickListener(this);                   //вешаем на кнопку обработку нажатия
            layout=(RelativeLayout)findViewById(R.id.fon);  //привязываем лайот к переменной. в дальнейшем будем испольшовать для установки цвета фона
            coord=(CoordinatorLayout)findViewById(R.id.coord);//привязываем координатор лайот к переменной

            preview = (SurfaceView) this.findViewById(R.id.preview);//Обязательным условием при работе с камерой является создание окна предпросмотра (preview)
            surfHold = preview.getHolder();//берем холдер с превью
            surfHold.addCallback(this);//привязываем сообщения о состоянии сюрфейса превью
            //surfHold.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);//настройка типа нужна только для android версии ниже 3.0
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//отключаем засыпание экрана не используя wakelock
    }

    @Override
    public void onResume() {
            super.onResume();
            if (mCamera == null) {             //если камера не привязана к переменной то:
                try {mCamera = Camera.open();}  //привязываем к переменной открытую камеру.
                catch (Exception e) {Log.d(TAG, "Camera.open() failed: " + e.getMessage());}
                             }
            if (!previewOn && mCamera != null) {
                mCamera.startPreview();//Чтобы включить отображение preview, вызываем startPreview
                previewOn = true;
                }
            turnLightOn(); //включаем вспышку
            Log.d(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        turnLightOff();
        Log.d(TAG, "onPause");
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
    public void surfaceChanged(SurfaceHolder holder, int I, int J, int K) {Log.d(TAG, "surfaceChanged");}

    @Override
    public void surfaceCreated(SurfaceHolder holder) {Log.d(TAG, "surfaceCreated");
        try {mCamera.setPreviewDisplay(holder);} catch (IOException e) {e.printStackTrace();}
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {return true;}
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) { //обрабатываем нажатие
        switch (view.getId()){
            case R.id.fab:    //обрабатываем нажатие плавающей кнопки
                if (lightOn) turnLightOff(); else turnLightOn(); break; // проверяем переменную состояния вспышки (если включен-выключить, иначе включить)
            default:break;   }
    }
}

/*
//import android.os.PowerManager;
//import android.os.PowerManager.WakeLock;
//    WakeLock nosleep; //переменная для функции запрета засыпания смартфона
//    PowerManager power; //переменная для передачи переменной nosleep возможности управления питанием
 private void nosleepON() {//метод запрещающий смартфону засыпать - устанавливаем wakelock
        if (nosleep == null) {// если переменная nosleep пуста,то:
            power = (PowerManager) getSystemService(Context.POWER_SERVICE);                 // привязываем к переменной power управление системным сервисом менеджер питания
            nosleep = power.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"PLEASE_DONT_SLEEP");}  // заносим в nosleep параметр управления питанием "CPU не засыпать"
        nosleep.acquire();//активируем wakelock запрет засыпания
    }

    private void nosleepOFF() {//метод снимающий запрет засыпания смартфона - снимаем wakelock
        if (nosleep != null)  nosleep.release();  //если переменная уплавляющая питанием nosleep не пуста то снять все wakelock блокировки засыпания
    }*/