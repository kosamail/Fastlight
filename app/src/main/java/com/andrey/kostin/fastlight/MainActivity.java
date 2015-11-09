package com.andrey.kostin.fastlight;

import android.content.Intent;
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

import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.RelativeLayout;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, SurfaceHolder.Callback{

    private static final String TAG = "MYLOG";      //переменная для таггироования логов
    private static final int Dark = 0xFF455A64;     //темный цвет фона
    private static final int Light = 0xFFCFD8DC;    //светлый цвет фона
    private static final int White = 0xFFFFFFFF;    //белый цвет фона
    private Camera mCamera;     //переменная для работы с камерой
    Parameters parameters;      //переменная для хранения параметров камеры
    private boolean lightOn;    //переменная определяющая 1-включить вспышку 0-выключить
    private boolean previewOn;  //переменная хранящая состояние превью включено или нет
    boolean screenASflafh=false;//переменная для работы с экраном вместо вспышки
    SurfaceView preview;        //Обязательным условием при работе с камерой является создание окна предпросмотра (preview)
    SurfaceHolder surfHold;     //переменная для холдера камеры. работа с preview ведется не напрямую, а через посредника – SurfaceHolder (surfholder) Именно с этим объектом умеет работать Camera. surfholder будет сообщать нам о том, что surface готов к работе, изменен или более недоступен.
                                //Camera берет holder и с его помощью выводит изображение на preview.
    List<String> flashModes;    //Переменная список поддерживаемых режимов вспышки
    String flashMode;           //переменная для текущего режима вспышки и установки режимов
    private CoordinatorLayout coord;//переменная для привязки и координаторлайоту
    Snackbar snackbar;          //переменная для снекбара
    Toolbar toolbar;            //переменная для тулбара
    FloatingActionButton fab;   //переменная для работы с плавающей кнопкой
    RelativeLayout layout;      //переменная для работы с лайотом
    Intent intent;              //переменная интента для вызова активити настроек





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
            Log.d(TAG, "Metod onCreate Done");
    }

    @Override
    public void onResume() {
            super.onResume();
            if (mCamera == null) {              //если камера не привязана к переменной то:
                try {mCamera = Camera.open();   //привязываем к переменной открытую камеру.
                     screenASflafh=false;}      //переменная скринасфлеш устанавливаем в ноль - работаем с камерой
                catch (Exception e) {           //если не получается привязаться к камере, то
                     screenASflafh=true;        //переменная скринасфлеш в единицу - будем работать с экраном вместо камеры
                     snackbar=Snackbar.make(coord, R.string.usescreen, Snackbar.LENGTH_LONG);//готовим сообщение снекбара что будем работать вместо вспышки с экраном
                     snackbar.show();           //выводим сообщение снекбара
                     Log.d(TAG, "We have error"+ e.getMessage());}}//пишем в лог возникшую ошибку
            if (!previewOn && mCamera != null) {//если превью пустое и камера не пуста:
                mCamera.startPreview();         //вызываем startPreview чтобы включить отображение изображения с камеры в preview
                previewOn = true;}               //устанавливаем переменную превьюОн в единицу - превью используется
            turnLightOn(); //включаем вспышку
            Log.d(TAG, "Metod onResume Done");
    }


    private void turnLightOn() {                                        //метод для включения вспышки
        lightOn = true;                                                 //устанавливаем переменную лайтон(вспышка включена) в истину
        if (screenASflafh) {layout.setBackgroundColor(White);   return;}    // Если переменная скринасфлеш установлена, значит вспышки нет и красим лайот в белый цвет и выходим из метода
        parameters = mCamera.getParameters();   //берем параметры камеры в переменную
        if (parameters == null) {layout.setBackgroundColor(White);return;}  //если параметры взять не удалось то значит с камерой проблемы, будем использовать всесто вспышки фон лайота. Красим фон и выходим из метода
        flashModes = parameters.getSupportedFlashModes();      //берем из параметров камеры список поддерживаемых режимов в строковый список flashmodes
        if (flashModes == null) {layout.setBackgroundColor(White);return;}  //если список режимов камеры пуст,то с камерой проблемы, будем использовать всесто вспышки фон лайота. Красим фон и выходим из метода
        flashMode = parameters.getFlashMode();  //берем текущий режим работы камеры в переменную
        Log.d(TAG, "Used flash mode: " + flashMode+ ". All supported flash modes: " + flashModes);
        if (!Parameters.FLASH_MODE_TORCH.equals(flashMode)) {//если текущий режим вспышки не TORCH то:
            if (flashModes.contains(Parameters.FLASH_MODE_TORCH)) {//если среди поддерживаемых режимов ЕСТЬ TORCH то:
                parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);   //в переменную параметров устанавливаем режим вспышки TORCH
                mCamera.setParameters(parameters);                      //ВКЛЮЧАЕМ ВСПЫШКУ УСТАНАВЛИВАЯ ПАРАМЕТР КАМЕРЫ FLASH_MODE_TORCH
                layout.setBackgroundColor(Light);                       //подкрашиваем фон лайота светлым
                //nosleepON();                                          //здесь запрещаем смартфону засыпать - устанавливаем wakelock
            } else  {                                                   //если среди поддерживаемых режиимов TORCH НЕТ то:
                snackbar=Snackbar.make(coord, R.string.snacknotsupport, Snackbar.LENGTH_LONG);//готовим снекбар с сообщением что режим TORCH не поддерживается
                snackbar.show();                                        //показываем снекбар
                layout.setBackgroundColor(White);                       //Красим фон лайота белым
                Log.d(TAG, "FLASH_MODE_TORCH not supported");
            }
        }
    }

    private void turnLightOff() {               //метод для выключения вспышки
        if (!lightOn) return;                   //если переменная лайтон не установлена значит вспышка выключена, выходим из метода
            lightOn = false;
            layout.setBackgroundColor(Dark);    //устанавливаем темный фон лайота
            if (screenASflafh) return;          //если переменная скринасфлеш установлена, значит вспышки нет и выходим из метода
            if (mCamera == null) return;
            Parameters parameters = mCamera.getParameters();
            if (parameters == null) return;
            flashModes = parameters.getSupportedFlashModes();
            flashMode = parameters.getFlashMode();
            // Check if camera flash exists
            if (flashModes == null) return;
            Log.d(TAG, "Used flash mode: " + flashMode+ ". All supported flash modes: " + flashModes);
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

    @Override
    public void onPause() { //метод при переходе в фоновой режим
        super.onPause();
        turnLightOff();     //запускаем выключение вспышки
        Log.d(TAG, "Method onPause Done");
    }

    @Override
    public void onDestroy() {   //метод перед уничтожением Активити
        super.onDestroy();
        if(mCamera != null) {   //если камера не пуста
            turnLightOff();     //запускаем выключение вспышки
            if (previewOn && mCamera != null) { //если используется превью и камера не пуста
            mCamera.stopPreview();              //останавливаем превью
            previewOn = false;                  //заносим в переменную состояния превью ноль
            }
            mCamera.release();                  //освобождаем камеру
                        }
        Log.d(TAG, "Method onDestroy finish");
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
    public boolean onCreateOptionsMenu(Menu menu) {       //Необходимая функция Создает меню из файла мейнменю.хмл
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //Необходимая функция Обрабатываем нажатия на пункты меню
        try{
            switch (item.getItemId()){

                case R.id.preferences:
                    intent = new Intent(this,PrefActivity.class);   //даем на вход интента текущий контекст и класс активити Преференсис
                    startActivity(intent);                      //запускаем вторую активити через интент
                    return true;

                case R.id.close_app:    //обрабатываем пункт меню "Закрыть"
                    finish();           //команда закрытия приложения
                    break;

                 default:break;
            }

        }catch(Exception e) { Log.d(TAG, "\n"+"Перехвачено исключение в меню"); }//отлавливаем исключение при нажатии пункта меню
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