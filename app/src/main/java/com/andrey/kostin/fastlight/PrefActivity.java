package com.andrey.kostin.fastlight;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
//import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class PrefActivity extends PreferenceActivity {
    Boolean market=true; //переменная для определяния установлен плеймаркет на устройстве или нет

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceScreen rootScreen = getPreferenceManager().createPreferenceScreen(this);// создаем экран
        setPreferenceScreen(rootScreen);        // говорим Activity, что rootScreen - корневой

/*        //создаем итем листпреференс для выбора количества знаков после запятой
        ListPreference listdecimal = new ListPreference(this);
        listdecimal.setKey("floatnumber");
        listdecimal.setTitle(R.string.decimal);
        listdecimal.setSummary(R.string.decimal_full);
        listdecimal.setDefaultValue("6");
        listdecimal.setEntries(R.array.listItems);
        listdecimal.setEntryValues(R.array.listValues);
        rootScreen.addPreference(listdecimal);//добавляем итем

        //создаем итем листпреференс для выбора шрифта
        ListPreference listfont = new ListPreference(this);
        listfont.setKey("fontstyle");
        listfont.setTitle(R.string.fontstyle);
        listfont.setSummary(getString(R.string.fontstyle_full));
        listfont.setDefaultValue("zekton.ttf");
        listfont.setEntries(R.array.fontItems);
        listfont.setEntryValues(R.array.fontValues);
        rootScreen.addPreference(listfont);//добавляем итем*/

        //создаем итем ссылку на страничку программы в гугл плей
        PreferenceScreen intentPref = getPreferenceManager().createPreferenceScreen(this);
        try {                                                                   //определяем есть ли приложение маркет на устройстве
            this.getPackageManager().getPackageInfo("com.android.vending", 0);
            market=true;
        } catch ( final Exception e ) {market=false;}
        if(market){intentPref.setIntent(new Intent().setAction(Intent.ACTION_VIEW).setData(Uri.parse("market://details?id=com.andrey.kostin.fastlight")));}
        else {intentPref.setIntent(new Intent().setAction(Intent.ACTION_VIEW).setData(Uri.parse("http://play.google.com/store/apps/details?id=com.andrey.kostin.fastlight")));}
        intentPref.setTitle(R.string.rate);
        intentPref.setSummary(R.string.rate_full);
        rootScreen.addPreference(intentPref);//добавляем итем

    }
}
