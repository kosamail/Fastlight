package com.andrey.kostin.fastlight;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener{

    private static final String TAG = "MYLOG";      //переменная для таггироования логов
    private static final int Dark = 0xFF455A64;     //темный цвет фона
    private static final int Light = 0xFF94A4AC;    //светлый цвет фона
    private static final int White = 0xFFFFFFFF;    //белый цвет фона
    private Camera mCamera;     //переменная для работы с камерой
    Parameters parameters;      //переменная для хранения параметров камеры
    private boolean lightOn;    //переменная определяющая 1-включить вспышку 0-выключить
    private boolean previewOn;  //переменная хранящая состояние превью включено или нет
    boolean screenASflash =false;//переменная для работы с экраном вместо вспышки
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
    WindowManager.LayoutParams layoutparams;
    Intent intent;              //переменная интента для вызова активити настроек
    private Switch swscreen;    //переменная для переключателя экран/вспышка

    SharedPreferences sp;   //переменная для обращения к хранимым настройкам приложения
    float sysbrigtness = 0; //переменная со значением системной яркости
    Boolean check1=false;   //переменная для выбора настройки экрана как источника света при старте


    @Override
    public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.main);
            toolbar = (Toolbar) findViewById(R.id.toolbar); //привязываем переменную к тулбару
            setSupportActionBar(toolbar);                   //устанавливает для тулбара поведение экшнбара
            fab = (FloatingActionButton) findViewById(R.id.fab);//привязываем переменную к флоатинг экшн буттон
            fab.setOnClickListener(this);                   //вешаем на кнопку обработку нажатия
            swscreen=(Switch)findViewById(R.id.swscreen);   //приязываем переменную к переключателю
            swscreen.setOnCheckedChangeListener(this);      //вешаем слушателя на переключатель
            swscreen.setShadowLayer(2, 0, 0, Color.BLACK);

            layout=(RelativeLayout)findViewById(R.id.fon);  //привязываем лайот к переменной. в дальнейшем будем испольшовать для установки цвета фона
            coord=(CoordinatorLayout)findViewById(R.id.coord);//привязываем координатор лайот к переменной

            preview = (SurfaceView) this.findViewById(R.id.preview);//Обязательным условием при работе с камерой является создание окна предпросмотра (preview)
            surfHold = preview.getHolder();//берем холдер с превью
            //surfHold.addCallback(this);
        //Далее привязываем сообщения о состоянии сурфейса(Каллбек) к mCamera.превью и стартуем превью
            surfHold.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
            try {mCamera.setPreviewDisplay(holder);mCamera.startPreview();} catch (Exception e) {e.printStackTrace();}  }
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format,int width, int height) {}
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {}
            });

            //surfHold.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);//настройка типа нужна только для android версии ниже 3.0
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//отключаем засыпание экрана не используя wakelock

       //Получаем системную яркость экрана
            try {sysbrigtness = android.provider.Settings.System.getInt(getContentResolver(),android.provider.Settings.System.SCREEN_BRIGHTNESS); //берем в переменную текущую яркость экрана в формате от 1 до 255
            Log.d(TAG,"Current Brighness is: "+ sysbrigtness);
            sysbrigtness=sysbrigtness/255;                                                      //преобразуем полученное значение в диапазон от 0.01 до 1
            BigDecimal bd = new BigDecimal(sysbrigtness).setScale(2, RoundingMode.HALF_EVEN);   //округляем до 2 знаков после запятой через использование формата Big Decimal
            sysbrigtness = bd.floatValue();                                                     //назначаем в переменную яркости округленное до float значение
            layoutparams = getWindow().getAttributes();                                         //берем текущие лайотпарамс, в дальнейшем будем их использовать в методах установки яркости
            Log.d(TAG,"Current Brighness is: "+ sysbrigtness);
            } catch (Exception e) {Log.d(TAG, "Exception on britness");}//ловим исключение при обращении к опросу яркости экрана

      //Сохраняем в настройки значение системной яркости экрана
            sp= PreferenceManager.getDefaultSharedPreferences(this);//получаем ШаредПреференсес которое работает с файлом настроек
            //sp.edit().clear().commit();//команда очистки настроек (пока мне не нужна)
            SharedPreferences.Editor ed = sp.edit();//чтобы редактировать данные, необходим объект Editor – получаем его из sp
            ed.putString("brigtness", Float.toString(sysbrigtness) + "F");//В метод putString указываем наименование переменной  и значение взятое из переменной системной яркости
            ed.apply();                             //Чтобы данные сохранились, необходимо выполнить apply.

            //check1 = sp.getBoolean("check1", false);// берем значение переменной выбора источника света по умолчанию из настроек - Я написал функцию выбора источника света при загрузке но пока считаю ее непопулярной
            Log.d(TAG, "Metod onCreate Done");
    }

    @Override
    public void onResume() {
            super.onResume();
        try{
            if (mCamera == null) {              //если камера не привязана к переменной то:
                try {mCamera = Camera.open();   //привязываем к переменной открытую камеру.
                     screenASflash =false;      //переменная скринасфлеш устанавливаем в ноль - работаем с камерой
                     if(!hasFlash()){           //если вспышки в смартфоне нет hasflash=0 то:
                        screenASflash=true;            //установить переменною скринасфлеш в 1
                        swscreen.setChecked(true);     //задать переключателю положение ЭКРАН
                      //swscreen.setEnabled(false);    //делаем переключатель неактивным
                        swscreen.setVisibility(View.INVISIBLE);}//делаем переключатель невидимым, потому как выбрать вспышку мы однозначно не можем
                }
                catch (Exception e) {           //если не получается привязаться к камере, то
                     screenASflash =true;        //переменная скринасфлеш в единицу - будем работать с экраном вместо камеры
                     swscreen.setChecked(true);     //задать переключателю положение ЭКРАН
                   //swscreen.setEnabled(false);//делаем переключатель неактивным
                     swscreen.setVisibility(View.INVISIBLE);//делаем переключатель невидимым, потому как выбрать вспышку мы однозначно не можем
                  //   snackbar=Snackbar.make(coord, R.string.usescreen, Snackbar.LENGTH_LONG);//готовим сообщение снекбара что будем работать вместо вспышки с экраном
                  //   snackbar.show();           //выводим сообщение снекбара
                     Log.d(TAG, "Ошибка "+ e.getMessage());}}//пишем в лог возникшую ошибку

/*          //Опрос чекбокса настройки светить экраном при запуске - Я написал эту функцию, но считаю что она не будет популярна, поэтому закоментировано
            if(check1){                          //если чекбокс настойки светить экраном при старте установлен, то
                screenASflash=true;            //установить переменною скринасфлеш в 1
                swscreen.setChecked(true);     //задать переключателю положение ЭКРАН
                check1=false;}                   //сбросить переменную чек1 в ноль чтобы в следующих обращениях к онРезюм не переключаться в режим экрана
*/

            if (!previewOn && mCamera != null) {//если превью пустое и камера не пуста:
                mCamera.startPreview();         //вызываем startPreview чтобы включить отображение изображения с камеры в preview
                previewOn = true;}               //устанавливаем переменную превьюОн в единицу - превью используется
            turnLightOn(); //включаем вспышку
        }catch(Exception e) { Log.d(TAG, "\n"+"Перехвачено исключение в методе onResume: "+ e.getMessage()); }//отлавливаем исключение в методе onResume
            Log.d(TAG, "Metod onResume Done");
    }


    public boolean hasFlash() {     //Функция возвращающая 1-если вспышка на смартфоне есть и 0-если вспышки нет
        //if (mCamera == null) return false;
        parameters = mCamera.getParameters();
        if (parameters.getFlashMode() == null) return false;
        flashModes = parameters.getSupportedFlashModes();
        if (flashModes == null || flashModes.isEmpty() || flashModes.size() == 1 && flashModes.get(0).equals(Camera.Parameters.FLASH_MODE_OFF))return false;
        return true;
    }




    private void turnLightOn() {     //Метод для включения вспышки
        lightOn = true;                                                     //устанавливаем переменную лайтон(вспышка включена) в истину
        if (screenASflash) {layout.setBackgroundColor(White); return;}      // Если переменная скринасфлеш установлена, значит вспышки нет и красим лайот в белый цвет и выходим из метода
        try{
        parameters = mCamera.getParameters();                               //берем параметры камеры в переменную
        if (parameters == null) {layout.setBackgroundColor(White); return;} //если параметры взять не удалось то значит с камерой проблемы, будем использовать всесто вспышки фон лайота. Красим фон и выходим из метода
        flashModes = parameters.getSupportedFlashModes();                   //берем из параметров камеры список поддерживаемых режимов в строковый список flashmodes
        if (flashModes == null) {layout.setBackgroundColor(White); return;} //если список режимов камеры пуст,то с камерой проблемы, будем использовать всесто вспышки фон лайота. Красим фон и выходим из метода
        flashMode = parameters.getFlashMode();                              //берем текущий режим работы камеры в переменную
        Log.d(TAG, "Сейчас установлен флешмод: " + flashMode + " Поддерживаются флешмоды: " + flashModes);
        if (!Parameters.FLASH_MODE_TORCH.equals(flashMode)) {               //если текущий режим вспышки не TORCH то:
            if (flashModes.contains(Parameters.FLASH_MODE_TORCH)) {         //если среди поддерживаемых режимов ЕСТЬ TORCH то:
                parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);       //в переменную параметров устанавливаем режим вспышки TORCH
                mCamera.setParameters(parameters);                          //ВКЛЮЧАЕМ ВСПЫШКУ УСТАНАВЛИВАЯ ПАРАМЕТР КАМЕРЫ FLASH_MODE_TORCH
                Log.d(TAG, "Устанавливаем FLASH_MODE_TORCH");
                layout.setBackgroundColor(Light);                           //подкрашиваем фон лайота светлым
                //nosleepON();                                              //здесь запрещаем смартфону засыпать - устанавливаем wakelock
            } else  {                                                       //если среди поддерживаемых режиимов TORCH НЕТ то:
//                snackbar=Snackbar.make(coord, R.string.snacknotsupport, Snackbar.LENGTH_LONG);//готовим снекбар с сообщением что режим TORCH не поддерживается
//                snackbar.show();                                            //показываем снекбар
                layout.setBackgroundColor(White);                           //Красим фон лайота белым
                Log.d(TAG, "FLASH_MODE_TORCH не поддерживается");
            }
        }
    }catch(Exception e) { Log.d(TAG, "\n"+"Перехвачено исключение в методе turnLightOn: "+ e.getMessage()); }//отлавливаем исключение в методе turnLightOn
    }

    private void turnLightOff() {  //Метод для выключения вспышки
        if (!lightOn) return;                                           //если переменная лайтон не установлена значит вспышка выключена, выходим из метода
            lightOn = false;                                            //устанавливаем переменную лайтон в ноль, - признак вспышка выключена
            layout.setBackgroundColor(Dark);                            //устанавливаем темный фон лайота
            if (screenASflash ||(mCamera == null))return;               //если переменная скринасфлеш установлена или переменная камеры пуста, значит вспышки нет и выходим из метода
            try {
            parameters = mCamera.getParameters();                       //берем параметры камеры
            if (parameters == null) return;                             //если параметры пусты то выходим из метода
            flashMode = parameters.getFlashMode();                      //из параметров берем текущий режим
            if (!Parameters.FLASH_MODE_OFF.equals(flashMode)) {         //Есди параметр вспышки не равен "Выключено" то:
                if (flashModes.contains(Parameters.FLASH_MODE_OFF)) {   //Если в списке допустимых параметров flashmodes есть параметр ФлешМодОФФ то:
                    parameters.setFlashMode(Parameters.FLASH_MODE_OFF); //присваиваем параметр  ФлешМодОфф в параметры
                    mCamera.setParameters(parameters);                  //устанавливаем камере параметры с выключенной вспышкой - ВЫКЛЮЧАЕМ ВСПЫШКУ
                    Log.d(TAG, "Устанавливаем FLASH_MODE_OFF ");
                    //nosleepOFF();//отключаем запрет на засыпание смартфона - снимаем wakelock
                } else {
                    Log.d(TAG, "FLASH_MODE_OFF не поддерживается");
                }
            }

            }catch(Exception e) { Log.d(TAG, "\n"+"Перехвачено исключение в методе turnLightOff: "+ e.getMessage()); }//отлавливаем исключение в методе turnLightOff

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
        setSysBrigt();          //устанавливаем системную яркость экрана сохраненную в настройках
        try {
            if(mCamera != null) {   //если камера не пуста
            turnLightOff();     //запускаем выключение вспышки
            if (previewOn && mCamera != null) { //если используется превью и камера не пуста
            mCamera.stopPreview();              //останавливаем превью
            previewOn = false;                  //заносим в переменную состояния превью ноль
            }
            mCamera.release();                  //освобождаем камеру
                        }
        Log.d(TAG, "Method onDestroy finish");
        }catch(Exception e) { Log.d(TAG, "\n"+"Перехвачено исключение в методе onDestroy: "+ e.getMessage()); }//отлавливаем исключение в методе onDestroy
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {       //Необходимая функция Создает меню из файла мейнменю.хмл
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void setFullBrigt(){//Метод устанавливающий 100% яркость экрана
        layoutparams.screenBrightness = 1F;                             //лайотпарамс скринбрайтнес задаем максимальное значение 1F
        getWindow().setAttributes(layoutparams);                        //Задаем текущей активити атрибуты с измененной яркостью
        //sysbrigtness=239;                                             //альтернативный способ задачи яркости. для начала присваиваем переменной максимальное значение
        //android.provider.Settings.System.putInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS, (int)sysbrigtness);//альтернативный способ задачи яркости через андройд провайдер сеттингс.
    }

    public void setSysBrigt(){//Метод возвращающий системную яркость из памяти
        sysbrigtness = Float.valueOf(sp.getString("brigtness", "0.7")); //считываем содержимое предыдущего результата из настроек
        layoutparams.screenBrightness = sysbrigtness;                   //лайотпарамс скринбрайтнес задаем значение из переменной
        getWindow().setAttributes(layoutparams);                        //Задаем текущей активити атрибуты с измененной яркостью
        Log.d(TAG, "Brighness from Memory: " + sysbrigtness);
        //android.provider.Settings.System.putInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS, (int) sysbrigtness); //альтернативный способ задачи яркости через андройд провайдер сеттингс.
        //Log.d(TAG, "sysbrigtness on finish "+sysbrigtness);                                                                                   //только в андройдпровайдере sysbritness нужно использовать в диапазоне 0-255
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

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {        //метод обрабатывающий переключение режимов фонарика
        turnLightOff();         //гасим вспышку
        if (b) {                    //если выбран режим экран:
            screenASflash =true;    //устанавливаем переменную скринасфлеш в единицу. В дальшейшем эту переменную проверит функция по нажатию fab кнопки
            setFullBrigt();         //устанавливаем максимальную яркость экрана
//            snackbar=Snackbar.make(coord, "SCREEN AS TORCH", Snackbar.LENGTH_SHORT);//готовим сообщение снекбара что будем работать вместо вспышки с экраном
//            snackbar.show();           //выводим сообщение снекбара
        } else {
            screenASflash =false;   //устанавливаем переменную скринасфлеш в ноль. В дальшейшем эту переменную проверит функция по нажатию fab кнопки
            setSysBrigt();          //устанавливаем системную яркость экрана сохраненную в настройках
//            snackbar=Snackbar.make(coord, "FLASH AS TORCH", Snackbar.LENGTH_SHORT);//готовим сообщение снекбара что будем работать вспышкой
//            snackbar.show();           //выводим сообщение снекбара
        }
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