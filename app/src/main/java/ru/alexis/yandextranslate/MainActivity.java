package ru.alexis.yandextranslate;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.StrictMode;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getCanonicalName();
    private final String API_KEY = "trnsl.1.1.20170321T125421Z.58be4f25aa10ff14.ec511308fecc6dc5452b88b56a706b247938d19d";
    String trUrl = "https://translate.yandex.net/api/v1.5/tr.json/translate?";
    String trLangs = "https://translate.yandex.net/api/v1.5/tr.json/getLangs?";
    String trLng = "en-ru";
    String trText = "";
    String trFormat = "html";
    private JSONArray arrText;
    private JSONArray arrLangs;
    EditText editText;
    TextView tv;
    String sLangFrom = "en";
    String sLangTo = "ru";
    TextView langFrom;
    TextView langTo;
    Map<String, String> langs = new LinkedHashMap<>();

    final int DIALOG_ITEMS = 1;
    String data[] = {};
    private AlertDialog alert;
    private String chooseLang = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.textTranslate);
        tv = (TextView) findViewById(R.id.translatedText);

        langFrom = (TextView) findViewById(R.id.langFrom);
        langTo = (TextView) findViewById(R.id.langTo);


        StrictMode.ThreadPolicy policy = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
            policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        TabLayout tabs = (TabLayout) findViewById(R.id.bottomtab);
        tabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition() == 0) {
                    // translate activity
                }else if(tab.getPosition() == 1) {
                    //bookmarked
                    Intent intent = new Intent(MainActivity.this, History.class);
                    MainActivity.this.startActivity(intent);
                }else {
                    //settings
                    Toast.makeText(MainActivity.this, "Settings under construction.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
//                Toast.makeText(MainActivity.this, "oTU ", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
//                Toast.makeText(MainActivity.this, "oTR ", Toast.LENGTH_SHORT).show();
            }
        });
        loadLanguages();
    }
    private void loadLanguages(){
        //TODO Change use ui from locale.
        //trUrl = "https://translate.yandex.net/api/v1.5/tr.json/getLangs?";
        //trUrl + "key=" + API_KEY + "&ui="+uiLang
        final String lang = (!sLangTo.equalsIgnoreCase("")?sLangTo:sLangFrom);
        if (lang.equalsIgnoreCase("")){
            langTo.setText("Auto");
            sLangTo = "";
            return;
        }
        this.runOnUiThread(new Thread(new Runnable() {
                        @Override
                        public void run() {
                    JSONObject obj = null;
                    HttpURLConnection uc = null;
                    try {
                        URL url = new URL(trLangs + "key=" + API_KEY + "&ui=" + lang);
                        uc =  (HttpURLConnection) url.openConnection();
                        uc.connect();
                        BufferedReader br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                        }
                        br.close();
                        Log.d(LOG_TAG, "run: " + trLangs + "key=" + API_KEY + "&ui=" + lang);
                        obj = new JSONObject(sb.toString());

                        arrLangs = obj.getJSONObject("langs").names();
                        JSONArray names = obj.getJSONObject("langs").names();
                        for (int i = 0; i< names.length(); i++) {
                            langs.put(names.getString(i), obj.getJSONObject("langs").getString(names.getString(i)));
                        }
                        data = langs.values().toArray(data);
                        Log.d(LOG_TAG, "load to data: " + langs.size()+ "=" + data.length);
                        createDialog();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }finally{
                        if (uc != null) uc.disconnect();
                    }
                }
            }
    ));
    }
    public void translateText(View view) {

        /*https://translate.yandex.net/api/v1.5/tr.json/translate ?
        key=<API-ключ>
                & text=<переводимый текст>
        & lang=<направление перевода>
        & [format=<формат текста>]
        & [options=<опции перевода>]
        & [callback=<имя callback-функции>]
        */

        final String lang = (!sLangFrom.equalsIgnoreCase("")?(sLangFrom+"-"):"") + sLangTo;
        //final String lang = (!langFrom.getText().toString().equalsIgnoreCase("")?(langFrom.getText().toString()+"-"):"")+langTo.getText().toString();

this.runOnUiThread(
new Thread
        (
         new Runnable() {
            @Override
            public void run() {
                JSONObject obj = null;
                HttpURLConnection uc = null;
                try {
                    trText = editText.getText().toString();
                    trText = URLEncoder.encode(trText, "UTF-8");
                    URL url = new URL(trUrl + "key=" + API_KEY + "&lang=" + lang + "&format=" + trFormat + "&text=" + trText);
                    Log.d(LOG_TAG, url.toString());
                    uc =  (HttpURLConnection) url.openConnection();
                    uc.connect();
                    BufferedReader br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    br.close();

                    obj = new JSONObject(sb.toString());
                    if(obj.getInt("code") == 200){
                        arrText = obj.getJSONArray("text");
                        StringBuilder s = new StringBuilder();
                        for (int i = 0; i < arrText.length(); i++) {
                            Log.d(LOG_TAG, "trText = " + arrText.get(i));
                            s.append(arrText.getString(i));
                        }
                        setEditText(s.toString());
                    }

                    Toast.makeText(getBaseContext(), "Translated", Toast.LENGTH_SHORT).show();


                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally{
                    if (uc != null) uc.disconnect();
                }
            }
         }
        ));
     }

    public void onclickLang(View v) {
        switch (v.getId()) {
            case R.id.langFrom:
                alert.show();
                break;
            case R.id.langTo:
                alert.show();
                break;
            default:
                break;
        }
    }
/*
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        switch (id) {
            // массив
            case DIALOG_ITEMS:
                adb.setTitle("Choose a language");
                adb.setSingleChoiceItems(data, -1, (DialogInterface.OnClickListener) myClickListener);
                break;
        }
        adb.setPositiveButton("OK", (DialogInterface.OnClickListener) myClickListener);
        return adb.create();
    }
*/
    public void createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose language");
        builder.setItems(data, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ListView lv = ((AlertDialog) dialog).getListView();

                if (which == Dialog.BUTTON_POSITIVE) {
                    // выводим в лог позицию выбранного элемента
                    Log.d(LOG_TAG, "pos = " + lv.getCheckedItemPosition() + "=" +
                            lv.getSelectedItem().toString());
                } else {
                    // выводим в лог позицию нажатого элемента
                    chooseLang = data[which];
                    Log.d(LOG_TAG, "which = " + which + "=" + data[which] + "=" + chooseLang);
                    langTo.setText(chooseLang);
                }
            }
        });
        alert = builder.create();
    }

    private void setEditText(String s){
        tv.setText(s);
    }

    public void listenText(View view) {
        Toast.makeText(this, "Listen text", Toast.LENGTH_SHORT).show();
    }

    public void bookmarkText(View view) {
        Toast.makeText(this, "Bookmark text", Toast.LENGTH_SHORT).show();

    }
    public void speakText(View view) {
        Toast.makeText(this, "Speak text", Toast.LENGTH_SHORT).show();
    }

    public void uploadText(View view) {
        Toast.makeText(this, "Upload text", Toast.LENGTH_SHORT).show();
    }

    public void resizeWindow(View view) {
        Toast.makeText(this, "Resize edit window", Toast.LENGTH_SHORT).show();
    }

    public void clearText(View view) {
        editText.setText("");
    }

    public void swapLanguages(View view) {
        String temp = langFrom.getText().toString();
        langFrom.setText(langTo.getText().toString());
        langTo.setText(temp);

        temp = sLangFrom;
        sLangFrom = sLangTo;
        sLangTo = temp;
    }
}
