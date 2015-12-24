package com.andrey.kostin.fastlight;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
//import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class PrefActivity extends PreferenceActivity {
    Boolean market=true;            //переменная для определяния установлен плеймаркет на устройстве или нет
    PreferenceScreen rootScreen;    //переменная для обращения к корневому преференсскрин
//    Preference fordelete;           //переменная для выбора пункта меню для дальнейшего удаления - для варианта 2
//    String target="market";         //строка с меткой для удаления лишнего пункта меню           - для варианта 2

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref);                             //вариант 1 - формируем настройки из файла xml
        rootScreen = getPreferenceScreen();                                 //вариант 1 - берем в переменную преференсскрин
        //rootScreen = getPreferenceManager().createPreferenceScreen(this); //вариант 2 - создаем экран программно вместо верхних двух строчек
        setPreferenceScreen(rootScreen);                        // сообщаем активити что rootScreen - корневой

        //создаем итем ссылку на страничку программы в гугл плей
        PreferenceScreen intentPref = getPreferenceManager().createPreferenceScreen(this);
        try {                                                                   //определяем есть ли приложение маркет на устройстве
            this.getPackageManager().getPackageInfo("com.android.vending", 0);
            market=true;
        } catch ( final Exception e ) {market=false;}
        if(market){intentPref.setIntent(new Intent().setAction(Intent.ACTION_VIEW).setData(Uri.parse("market://details?id=com.andrey.kostin.fastlight")));}
        else {intentPref.setIntent(new Intent().setAction(Intent.ACTION_VIEW).setData(Uri.parse("http://play.google.com/store/apps/details?id=com.andrey.kostin.fastlight")));
        }
        intentPref.setTitle(R.string.rate);
        intentPref.setSummary(R.string.rate_full);
        rootScreen.addPreference(intentPref);       //добавляем итем

 /*     Вариант 2 - Часть кода для выбора пункта оценки маркета из двух присутствующих в XML

        try {                                   //определяем есть ли приложение маркет на устройстве
            this.getPackageManager().getPackageInfo("com.android.vending", 0);
            market=true;
        } catch ( final Exception e ) {market=false;}

        if(market){target="http";}              //если маркет на устройстве установлен то удалять будем пункт меню с http протоколом
        fordelete = getPreferenceManager().findPreference(target);  //находим пункт меню для удаления
        rootScreen.removePreference(fordelete);                     //удаляем пункт меню
*/
    }
}
