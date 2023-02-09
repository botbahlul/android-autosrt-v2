package com.android.autosrt;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;
import static android.text.TextUtils.substring;

import static com.arthenica.mobileffmpeg.Config.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    ArrayList<String> arraylist_src = new ArrayList<>();
    ArrayList<String> arraylist_dst = new ArrayList<>();
    ArrayList<String> arraylist_src_languages = new ArrayList<>();
    ArrayList<String> arraylist_dst_languages = new ArrayList<>();
    Map<String, String> map_src_country = new HashMap<>();
    Map<String, String> map_dst_country = new HashMap<>();
    public static String src_country, dst_country;
    ArrayList<String> arraylist_subtitle_format = new ArrayList<>();

    Spinner spinner_src_languages;
    @SuppressLint("StaticFieldLeak")
    public static TextView textview_src;
    Spinner spinner_dst_languages;
    @SuppressLint("StaticFieldLeak")
    public static TextView textview_dst;
    @SuppressLint("StaticFieldLeak")
    public static TextView textview_fileURI;
    @SuppressLint("StaticFieldLeak")
    public static TextView textview_filePath;
    @SuppressLint("StaticFieldLeak")
    public static TextView textview_fileDisplayName;
    @SuppressLint("StaticFieldLeak")
    public static Button button_browse;
    @SuppressLint("StaticFieldLeak")
    Spinner spinner_subtitle_format;
    @SuppressLint("StaticFieldLeak")
    public static TextView textview_subtitle_format;
    public static Button button_start;
    @SuppressLint("StaticFieldLeak")
    public static TextView textview_output;

    int STORAGE_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        arraylist_src.add("af");
        arraylist_src.add("sq");
        arraylist_src.add("am");
        arraylist_src.add("ar");
        arraylist_src.add("hy");
        arraylist_src.add("as");
        arraylist_src.add("ay");
        arraylist_src.add("az");
        arraylist_src.add("bm");
        arraylist_src.add("eu");
        arraylist_src.add("be");
        arraylist_src.add("bn");
        arraylist_src.add("bho");
        arraylist_src.add("bs");
        arraylist_src.add("bg");
        arraylist_src.add("ca");
        arraylist_src.add("ceb");
        arraylist_src.add("ny");
        arraylist_src.add("zh-CN");
        arraylist_src.add("zh-TW");
        arraylist_src.add("co");
        arraylist_src.add("hr");
        arraylist_src.add("cs");
        arraylist_src.add("da");
        arraylist_src.add("dv");
        arraylist_src.add("doi");
        arraylist_src.add("nl");
        arraylist_src.add("en");
        arraylist_src.add("eo");
        arraylist_src.add("et");
        arraylist_src.add("ee");
        arraylist_src.add("fil");
        arraylist_src.add("fi");
        arraylist_src.add("fr");
        arraylist_src.add("fy");
        arraylist_src.add("gl");
        arraylist_src.add("ka");
        arraylist_src.add("de");
        arraylist_src.add("el");
        arraylist_src.add("gn");
        arraylist_src.add("gu");
        arraylist_src.add("ht");
        arraylist_src.add("ha");
        arraylist_src.add("haw");
        arraylist_src.add("he");
        arraylist_src.add("hi");
        arraylist_src.add("hmn");
        arraylist_src.add("hu");
        arraylist_src.add("is");
        arraylist_src.add("ig");
        arraylist_src.add("ilo");
        arraylist_src.add("id");
        arraylist_src.add("ga");
        arraylist_src.add("it");
        arraylist_src.add("ja");
        arraylist_src.add("jv");
        arraylist_src.add("kn");
        arraylist_src.add("kk");
        arraylist_src.add("km");
        arraylist_src.add("rw");
        arraylist_src.add("gom");
        arraylist_src.add("ko");
        arraylist_src.add("kri");
        arraylist_src.add("kmr");
        arraylist_src.add("ckb");
        arraylist_src.add("ky");
        arraylist_src.add("lo");
        arraylist_src.add("la");
        arraylist_src.add("lv");
        arraylist_src.add("ln");
        arraylist_src.add("lt");
        arraylist_src.add("lg");
        arraylist_src.add("lb");
        arraylist_src.add("mk");
        arraylist_src.add("mg");
        arraylist_src.add("ms");
        arraylist_src.add("ml");
        arraylist_src.add("mt");
        arraylist_src.add("mi");
        arraylist_src.add("mr");
        arraylist_src.add("mni-Mtei");
        arraylist_src.add("lus");
        arraylist_src.add("mn");
        arraylist_src.add("my");
        arraylist_src.add("ne");
        arraylist_src.add("no");
        arraylist_src.add("or");
        arraylist_src.add("om");
        arraylist_src.add("ps");
        arraylist_src.add("fa");
        arraylist_src.add("pl");
        arraylist_src.add("pt");
        arraylist_src.add("pa");
        arraylist_src.add("qu");
        arraylist_src.add("ro");
        arraylist_src.add("ru");
        arraylist_src.add("sm");
        arraylist_src.add("sa");
        arraylist_src.add("gd");
        arraylist_src.add("nso");
        arraylist_src.add("sr");
        arraylist_src.add("st");
        arraylist_src.add("sn");
        arraylist_src.add("sd");
        arraylist_src.add("si");
        arraylist_src.add("sk");
        arraylist_src.add("sl");
        arraylist_src.add("so");
        arraylist_src.add("es");
        arraylist_src.add("su");
        arraylist_src.add("sw");
        arraylist_src.add("sv");
        arraylist_src.add("tg");
        arraylist_src.add("ta");
        arraylist_src.add("tt");
        arraylist_src.add("te");
        arraylist_src.add("th");
        arraylist_src.add("ti");
        arraylist_src.add("ts");
        arraylist_src.add("tr");
        arraylist_src.add("tk");
        arraylist_src.add("tw");
        arraylist_src.add("uk");
        arraylist_src.add("ur");
        arraylist_src.add("ug");
        arraylist_src.add("uz");
        arraylist_src.add("vi");
        arraylist_src.add("cy");
        arraylist_src.add("xh");
        arraylist_src.add("yi");
        arraylist_src.add("yo");
        arraylist_src.add("zu");

        arraylist_src_languages.add("Afrikaans");
        arraylist_src_languages.add("Albanian");
        arraylist_src_languages.add("Amharic");
        arraylist_src_languages.add("Arabic");
        arraylist_src_languages.add("Armenian");
        arraylist_src_languages.add("Assamese");
        arraylist_src_languages.add("Aymara");
        arraylist_src_languages.add("Azerbaijani");
        arraylist_src_languages.add("Bambara");
        arraylist_src_languages.add("Basque");
        arraylist_src_languages.add("Belarusian");
        arraylist_src_languages.add("Bengali");
        arraylist_src_languages.add("Bhojpuri");
        arraylist_src_languages.add("Bosnian");
        arraylist_src_languages.add("Bulgarian");
        arraylist_src_languages.add("Catalan");
        arraylist_src_languages.add("Cebuano");
        arraylist_src_languages.add("Chichewa");
        arraylist_src_languages.add("Chinese (Simplified)");
        arraylist_src_languages.add("Chinese (Traditional)");
        arraylist_src_languages.add("Corsican");
        arraylist_src_languages.add("Croatian");
        arraylist_src_languages.add("Czech");
        arraylist_src_languages.add("Danish");
        arraylist_src_languages.add("Dhivehi");
        arraylist_src_languages.add("Dogri");
        arraylist_src_languages.add("Dutch");
        arraylist_src_languages.add("English");
        arraylist_src_languages.add("Esperanto");
        arraylist_src_languages.add("Estonian");
        arraylist_src_languages.add("Ewe");
        arraylist_src_languages.add("Filipino");
        arraylist_src_languages.add("Finnish");
        arraylist_src_languages.add("French");
        arraylist_src_languages.add("Frisian");
        arraylist_src_languages.add("Galician");
        arraylist_src_languages.add("Georgian");
        arraylist_src_languages.add("German");
        arraylist_src_languages.add("Greek");
        arraylist_src_languages.add("Guarani");
        arraylist_src_languages.add("Gujarati");
        arraylist_src_languages.add("Haitian Creole");
        arraylist_src_languages.add("Hausa");
        arraylist_src_languages.add("Hawaiian");
        arraylist_src_languages.add("Hebrew");
        arraylist_src_languages.add("Hindi");
        arraylist_src_languages.add("Hmong");
        arraylist_src_languages.add("Hungarian");
        arraylist_src_languages.add("Icelandic");
        arraylist_src_languages.add("Igbo");
        arraylist_src_languages.add("Ilocano");
        arraylist_src_languages.add("Indonesian");
        arraylist_src_languages.add("Irish");
        arraylist_src_languages.add("Italian");
        arraylist_src_languages.add("Japanese");
        arraylist_src_languages.add("Javanese");
        arraylist_src_languages.add("Kannada");
        arraylist_src_languages.add("Kazakh");
        arraylist_src_languages.add("Khmer");
        arraylist_src_languages.add("Kinyarwanda");
        arraylist_src_languages.add("Konkani");
        arraylist_src_languages.add("Korean");
        arraylist_src_languages.add("Krio");
        arraylist_src_languages.add("Kurdish (Kurmanji)");
        arraylist_src_languages.add("Kurdish (Sorani)");
        arraylist_src_languages.add("Kyrgyz");
        arraylist_src_languages.add("Lao");
        arraylist_src_languages.add("Latin");
        arraylist_src_languages.add("Latvian");
        arraylist_src_languages.add("Lingala");
        arraylist_src_languages.add("Lithuanian");
        arraylist_src_languages.add("Luganda");
        arraylist_src_languages.add("Luxembourgish");
        arraylist_src_languages.add("Macedonian");
        arraylist_src_languages.add("Malagasy");
        arraylist_src_languages.add("Malay");
        arraylist_src_languages.add("Malayalam");
        arraylist_src_languages.add("Maltese");
        arraylist_src_languages.add("Maori");
        arraylist_src_languages.add("Marathi");
        arraylist_src_languages.add("Meiteilon (Manipuri)");
        arraylist_src_languages.add("Mizo");
        arraylist_src_languages.add("Mongolian");
        arraylist_src_languages.add("Myanmar (Burmese)");
        arraylist_src_languages.add("Nepali");
        arraylist_src_languages.add("Norwegian");
        arraylist_src_languages.add("Odiya (Oriya)");
        arraylist_src_languages.add("Oromo");
        arraylist_src_languages.add("Pashto");
        arraylist_src_languages.add("Persian");
        arraylist_src_languages.add("Polish");
        arraylist_src_languages.add("Portuguese");
        arraylist_src_languages.add("Punjabi");
        arraylist_src_languages.add("Quechua");
        arraylist_src_languages.add("Romanian");
        arraylist_src_languages.add("Russian");
        arraylist_src_languages.add("Samoan");
        arraylist_src_languages.add("Sanskrit");
        arraylist_src_languages.add("Scots Gaelic");
        arraylist_src_languages.add("Sepedi");
        arraylist_src_languages.add("Serbian");
        arraylist_src_languages.add("Sesotho");
        arraylist_src_languages.add("Shona");
        arraylist_src_languages.add("Sindhi");
        arraylist_src_languages.add("Sinhala");
        arraylist_src_languages.add("Slovak");
        arraylist_src_languages.add("Slovenian");
        arraylist_src_languages.add("Somali");
        arraylist_src_languages.add("Spanish");
        arraylist_src_languages.add("Sundanese");
        arraylist_src_languages.add("Swahili");
        arraylist_src_languages.add("Swedish");
        arraylist_src_languages.add("Tajik");
        arraylist_src_languages.add("Tamil");
        arraylist_src_languages.add("Tatar");
        arraylist_src_languages.add("Telugu");
        arraylist_src_languages.add("Thai");
        arraylist_src_languages.add("Tigrinya");
        arraylist_src_languages.add("Tsonga");
        arraylist_src_languages.add("Turkish");
        arraylist_src_languages.add("Turkmen");
        arraylist_src_languages.add("Twi (Akan)");
        arraylist_src_languages.add("Ukrainian");
        arraylist_src_languages.add("Urdu");
        arraylist_src_languages.add("Uyghur");
        arraylist_src_languages.add("Uzbek");
        arraylist_src_languages.add("Vietnamese");
        arraylist_src_languages.add("Welsh");
        arraylist_src_languages.add("Xhosa");
        arraylist_src_languages.add("Yiddish");
        arraylist_src_languages.add("Yoruba");
        arraylist_src_languages.add("Zulu");

        for (int i = 0; i < arraylist_src_languages.size(); i++) {
            map_src_country.put(arraylist_src_languages.get(i), arraylist_src.get(i));
        }

        arraylist_dst.add("af");
        arraylist_dst.add("sq");
        arraylist_dst.add("am");
        arraylist_dst.add("ar");
        arraylist_dst.add("hy");
        arraylist_dst.add("as");
        arraylist_dst.add("ay");
        arraylist_dst.add("az");
        arraylist_dst.add("bm");
        arraylist_dst.add("eu");
        arraylist_dst.add("be");
        arraylist_dst.add("bn");
        arraylist_dst.add("bho");
        arraylist_dst.add("bs");
        arraylist_dst.add("bg");
        arraylist_dst.add("ca");
        arraylist_dst.add("ceb");
        arraylist_dst.add("ny");
        arraylist_dst.add("zh-CN");
        arraylist_dst.add("zh-TW");
        arraylist_dst.add("co");
        arraylist_dst.add("hr");
        arraylist_dst.add("cs");
        arraylist_dst.add("da");
        arraylist_dst.add("dv");
        arraylist_dst.add("doi");
        arraylist_dst.add("nl");
        arraylist_dst.add("en");
        arraylist_dst.add("eo");
        arraylist_dst.add("et");
        arraylist_dst.add("ee");
        arraylist_dst.add("fil");
        arraylist_dst.add("fi");
        arraylist_dst.add("fr");
        arraylist_dst.add("fy");
        arraylist_dst.add("gl");
        arraylist_dst.add("ka");
        arraylist_dst.add("de");
        arraylist_dst.add("el");
        arraylist_dst.add("gn");
        arraylist_dst.add("gu");
        arraylist_dst.add("ht");
        arraylist_dst.add("ha");
        arraylist_dst.add("haw");
        arraylist_dst.add("he");
        arraylist_dst.add("hi");
        arraylist_dst.add("hmn");
        arraylist_dst.add("hu");
        arraylist_dst.add("is");
        arraylist_dst.add("ig");
        arraylist_dst.add("ilo");
        arraylist_dst.add("id");
        arraylist_dst.add("ga");
        arraylist_dst.add("it");
        arraylist_dst.add("ja");
        arraylist_dst.add("jv");
        arraylist_dst.add("kn");
        arraylist_dst.add("kk");
        arraylist_dst.add("km");
        arraylist_dst.add("rw");
        arraylist_dst.add("gom");
        arraylist_dst.add("ko");
        arraylist_dst.add("kri");
        arraylist_dst.add("kmr");
        arraylist_dst.add("ckb");
        arraylist_dst.add("ky");
        arraylist_dst.add("lo");
        arraylist_dst.add("la");
        arraylist_dst.add("lv");
        arraylist_dst.add("ln");
        arraylist_dst.add("lt");
        arraylist_dst.add("lg");
        arraylist_dst.add("lb");
        arraylist_dst.add("mk");
        arraylist_dst.add("mg");
        arraylist_dst.add("ms");
        arraylist_dst.add("ml");
        arraylist_dst.add("mt");
        arraylist_dst.add("mi");
        arraylist_dst.add("mr");
        arraylist_dst.add("mni-Mtei");
        arraylist_dst.add("lus");
        arraylist_dst.add("mn");
        arraylist_dst.add("my");
        arraylist_dst.add("ne");
        arraylist_dst.add("no");
        arraylist_dst.add("or");
        arraylist_dst.add("om");
        arraylist_dst.add("ps");
        arraylist_dst.add("fa");
        arraylist_dst.add("pl");
        arraylist_dst.add("pt");
        arraylist_dst.add("pa");
        arraylist_dst.add("qu");
        arraylist_dst.add("ro");
        arraylist_dst.add("ru");
        arraylist_dst.add("sm");
        arraylist_dst.add("sa");
        arraylist_dst.add("gd");
        arraylist_dst.add("nso");
        arraylist_dst.add("sr");
        arraylist_dst.add("st");
        arraylist_dst.add("sn");
        arraylist_dst.add("sd");
        arraylist_dst.add("si");
        arraylist_dst.add("sk");
        arraylist_dst.add("sl");
        arraylist_dst.add("so");
        arraylist_dst.add("es");
        arraylist_dst.add("su");
        arraylist_dst.add("sw");
        arraylist_dst.add("sv");
        arraylist_dst.add("tg");
        arraylist_dst.add("ta");
        arraylist_dst.add("tt");
        arraylist_dst.add("te");
        arraylist_dst.add("th");
        arraylist_dst.add("ti");
        arraylist_dst.add("ts");
        arraylist_dst.add("tr");
        arraylist_dst.add("tk");
        arraylist_dst.add("tw");
        arraylist_dst.add("uk");
        arraylist_dst.add("ur");
        arraylist_dst.add("ug");
        arraylist_dst.add("uz");
        arraylist_dst.add("vi");
        arraylist_dst.add("cy");
        arraylist_dst.add("xh");
        arraylist_dst.add("yi");
        arraylist_dst.add("yo");
        arraylist_dst.add("zu");

        arraylist_dst_languages.add("Afrikaans");
        arraylist_dst_languages.add("Albanian");
        arraylist_dst_languages.add("Amharic");
        arraylist_dst_languages.add("Arabic");
        arraylist_dst_languages.add("Armenian");
        arraylist_dst_languages.add("Assamese");
        arraylist_dst_languages.add("Aymara");
        arraylist_dst_languages.add("Azerbaijani");
        arraylist_dst_languages.add("Bambara");
        arraylist_dst_languages.add("Basque");
        arraylist_dst_languages.add("Belarusian");
        arraylist_dst_languages.add("Bengali");
        arraylist_dst_languages.add("Bhojpuri");
        arraylist_dst_languages.add("Bosnian");
        arraylist_dst_languages.add("Bulgarian");
        arraylist_dst_languages.add("Catalan");
        arraylist_dst_languages.add("Cebuano");
        arraylist_dst_languages.add("Chichewa");
        arraylist_dst_languages.add("Chinese (Simplified)");
        arraylist_dst_languages.add("Chinese (Traditional)");
        arraylist_dst_languages.add("Corsican");
        arraylist_dst_languages.add("Croatian");
        arraylist_dst_languages.add("Czech");
        arraylist_dst_languages.add("Danish");
        arraylist_dst_languages.add("Dhivehi");
        arraylist_dst_languages.add("Dogri");
        arraylist_dst_languages.add("Dutch");
        arraylist_dst_languages.add("English");
        arraylist_dst_languages.add("Esperanto");
        arraylist_dst_languages.add("Estonian");
        arraylist_dst_languages.add("Ewe");
        arraylist_dst_languages.add("Filipino");
        arraylist_dst_languages.add("Finnish");
        arraylist_dst_languages.add("French");
        arraylist_dst_languages.add("Frisian");
        arraylist_dst_languages.add("Galician");
        arraylist_dst_languages.add("Georgian");
        arraylist_dst_languages.add("German");
        arraylist_dst_languages.add("Greek");
        arraylist_dst_languages.add("Guarani");
        arraylist_dst_languages.add("Gujarati");
        arraylist_dst_languages.add("Haitian Creole");
        arraylist_dst_languages.add("Hausa");
        arraylist_dst_languages.add("Hawaiian");
        arraylist_dst_languages.add("Hebrew");
        arraylist_dst_languages.add("Hindi");
        arraylist_dst_languages.add("Hmong");
        arraylist_dst_languages.add("Hungarian");
        arraylist_dst_languages.add("Icelandic");
        arraylist_dst_languages.add("Igbo");
        arraylist_dst_languages.add("Ilocano");
        arraylist_dst_languages.add("Indonesian");
        arraylist_dst_languages.add("Irish");
        arraylist_dst_languages.add("Italian");
        arraylist_dst_languages.add("Japanese");
        arraylist_dst_languages.add("Javanese");
        arraylist_dst_languages.add("Kannada");
        arraylist_dst_languages.add("Kazakh");
        arraylist_dst_languages.add("Khmer");
        arraylist_dst_languages.add("Kinyarwanda");
        arraylist_dst_languages.add("Konkani");
        arraylist_dst_languages.add("Korean");
        arraylist_dst_languages.add("Krio");
        arraylist_dst_languages.add("Kurdish (Kurmanji)");
        arraylist_dst_languages.add("Kurdish (Sorani)");
        arraylist_dst_languages.add("Kyrgyz");
        arraylist_dst_languages.add("Lao");
        arraylist_dst_languages.add("Latin");
        arraylist_dst_languages.add("Latvian");
        arraylist_dst_languages.add("Lingala");
        arraylist_dst_languages.add("Lithuanian");
        arraylist_dst_languages.add("Luganda");
        arraylist_dst_languages.add("Luxembourgish");
        arraylist_dst_languages.add("Macedonian");
        arraylist_dst_languages.add("Malagasy");
        arraylist_dst_languages.add("Malay");
        arraylist_dst_languages.add("Malayalam");
        arraylist_dst_languages.add("Maltese");
        arraylist_dst_languages.add("Maori");
        arraylist_dst_languages.add("Marathi");
        arraylist_dst_languages.add("Meiteilon (Manipuri)");
        arraylist_dst_languages.add("Mizo");
        arraylist_dst_languages.add("Mongolian");
        arraylist_dst_languages.add("Myanmar (Burmese)");
        arraylist_dst_languages.add("Nepali");
        arraylist_dst_languages.add("Norwegian");
        arraylist_dst_languages.add("Odiya (Oriya)");
        arraylist_dst_languages.add("Oromo");
        arraylist_dst_languages.add("Pashto");
        arraylist_dst_languages.add("Persian");
        arraylist_dst_languages.add("Polish");
        arraylist_dst_languages.add("Portuguese");
        arraylist_dst_languages.add("Punjabi");
        arraylist_dst_languages.add("Quechua");
        arraylist_dst_languages.add("Romanian");
        arraylist_dst_languages.add("Russian");
        arraylist_dst_languages.add("Samoan");
        arraylist_dst_languages.add("Sanskrit");
        arraylist_dst_languages.add("Scots Gaelic");
        arraylist_dst_languages.add("Sepedi");
        arraylist_dst_languages.add("Serbian");
        arraylist_dst_languages.add("Sesotho");
        arraylist_dst_languages.add("Shona");
        arraylist_dst_languages.add("Sindhi");
        arraylist_dst_languages.add("Sinhala");
        arraylist_dst_languages.add("Slovak");
        arraylist_dst_languages.add("Slovenian");
        arraylist_dst_languages.add("Somali");
        arraylist_dst_languages.add("Spanish");
        arraylist_dst_languages.add("Sundanese");
        arraylist_dst_languages.add("Swahili");
        arraylist_dst_languages.add("Swedish");
        arraylist_dst_languages.add("Tajik");
        arraylist_dst_languages.add("Tamil");
        arraylist_dst_languages.add("Tatar");
        arraylist_dst_languages.add("Telugu");
        arraylist_dst_languages.add("Thai");
        arraylist_dst_languages.add("Tigrinya");
        arraylist_dst_languages.add("Tsonga");
        arraylist_dst_languages.add("Turkish");
        arraylist_dst_languages.add("Turkmen");
        arraylist_dst_languages.add("Twi (Akan)");
        arraylist_dst_languages.add("Ukrainian");
        arraylist_dst_languages.add("Urdu");
        arraylist_dst_languages.add("Uyghur");
        arraylist_dst_languages.add("Uzbek");
        arraylist_dst_languages.add("Vietnamese");
        arraylist_dst_languages.add("Welsh");
        arraylist_dst_languages.add("Xhosa");
        arraylist_dst_languages.add("Yiddish");
        arraylist_dst_languages.add("Yoruba");
        arraylist_dst_languages.add("Zulu");

        for (int i = 0; i < arraylist_dst_languages.size(); i++) {
            map_dst_country.put(arraylist_dst_languages.get(i), arraylist_dst.get(i));
        }

        arraylist_subtitle_format.add("srt");
        arraylist_subtitle_format.add("vtt");
        arraylist_subtitle_format.add("json");
        arraylist_subtitle_format.add("raw");

        setContentView(R.layout.activity_main);
        spinner_src_languages = findViewById(R.id.spinner_src_languages);
        setup_src_spinner(arraylist_src_languages);
        textview_src = findViewById(R.id.textview_src);
        spinner_dst_languages = findViewById(R.id.spinner_dst_languages);
        setup_dst_spinner(arraylist_dst_languages);
        textview_dst = findViewById(R.id.textview_dst);
        textview_fileURI = findViewById(R.id.textview_fileURI);
        textview_filePath = findViewById(R.id.textview_filePath);
        textview_fileDisplayName = findViewById(R.id.textview_fileDisplayName);
        button_browse = findViewById(R.id.button_browse);
        spinner_subtitle_format = findViewById(R.id.spinner_subtitle_format);
        setup_subtitle_format(arraylist_subtitle_format);
        textview_subtitle_format = findViewById(R.id.textview_subtitle_format);
        button_start = findViewById(R.id.button_start);
        textview_output = findViewById(R.id.textview_output);
        spinner_src_languages.setFocusable(true);
        spinner_src_languages.requestFocus();

        textview_fileURI.setMovementMethod(new ScrollingMovementMethod());
        textview_output.setMovementMethod(new ScrollingMovementMethod());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.actionbar_title);
        }

        TRANSCRIBE_STATUS.IS_TRANSCRIBING = false;
        CANCEL_STATUS.IS_CANCELING = true;
        SUBTITLE.FORMAT = "srt";

        if (SDK_INT >= Build.VERSION_CODES.Q && Environment.isExternalStorageRemovable()) {
            Uri uri = Uri.parse("package:" + MainActivity.this.getPackageName());
            startActivity(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, uri));
        } else {
            checkPermission(WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
        }

        spinner_src_languages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                src_country = spinner_src_languages.getSelectedItem().toString();
                dst_country = spinner_dst_languages.getSelectedItem().toString();
                LANGUAGE.SRC = map_src_country.get(src_country);
                LANGUAGE.DST = map_dst_country.get(dst_country);
                LANGUAGE.SRC_COUNTRY = src_country;
                LANGUAGE.DST_COUNTRY = dst_country;
                runOnUiThread(() -> {
                    String lsrc = "LANGUAGE.SRC = " + LANGUAGE.SRC;
                    textview_src.setText(lsrc);
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                src_country = spinner_src_languages.getSelectedItem().toString();
                dst_country = spinner_dst_languages.getSelectedItem().toString();
                LANGUAGE.SRC = map_src_country.get(src_country);
                LANGUAGE.DST = map_dst_country.get(dst_country);
                LANGUAGE.SRC_COUNTRY = src_country;
                LANGUAGE.DST_COUNTRY = dst_country;
                runOnUiThread(() -> {
                    String lsrc = "LANGUAGE.SRC = " + LANGUAGE.SRC;
                    textview_src.setText(lsrc);
                });
            }
        });

        spinner_dst_languages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                src_country = spinner_src_languages.getSelectedItem().toString();
                dst_country = spinner_dst_languages.getSelectedItem().toString();
                LANGUAGE.SRC = map_src_country.get(src_country);
                LANGUAGE.DST = map_dst_country.get(dst_country);
                LANGUAGE.SRC_COUNTRY = src_country;
                LANGUAGE.DST_COUNTRY = dst_country;
                runOnUiThread(() -> {
                    String ldst = "LANGUAGE.DST = " + LANGUAGE.DST;
                    textview_dst.setText(ldst);
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                src_country = spinner_src_languages.getSelectedItem().toString();
                dst_country = spinner_dst_languages.getSelectedItem().toString();
                LANGUAGE.SRC = map_src_country.get(src_country);
                LANGUAGE.DST = map_dst_country.get(dst_country);
                LANGUAGE.SRC_COUNTRY = src_country;
                LANGUAGE.DST_COUNTRY = dst_country;
                runOnUiThread(() -> {
                    String ldst = "LANGUAGE.DST = " + LANGUAGE.DST;
                    textview_dst.setText(ldst);
                });
            }
        });

        button_browse.setOnClickListener(view -> {
            textview_output.setText("");
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            String[] mimeTypes = {"video/*", "audio/*"};
            intent.setType("*/*");
            mStartForActivity.launch(intent);
        });

        spinner_subtitle_format.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                SUBTITLE.FORMAT = spinner_subtitle_format.getSelectedItem().toString();
                runOnUiThread(() -> {
                    String sf = "SUBTITLE.FORMAT = " + SUBTITLE.FORMAT;
                    textview_subtitle_format.setText(sf);
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                runOnUiThread(() -> {
                    String sf = "SUBTITLE.FORMAT = " + SUBTITLE.FORMAT;
                    textview_subtitle_format.setText(sf);
                });
            }
        });


        button_start.setOnClickListener(view -> {
            if (FILE.URI != null) {
                TRANSCRIBE_STATUS.IS_TRANSCRIBING = true;
                CANCEL_STATUS.IS_CANCELING = false;
                Intent intent = new Intent(MainActivity.this, TranscribeActivity.class);
                MainActivity.this.startActivity(intent);
            }
            else {
                TRANSCRIBE_STATUS.IS_TRANSCRIBING = false;
                CANCEL_STATUS.IS_CANCELING = true;
                runOnUiThread(() -> {
                    String m = "You should browse a file first\n";
                    textview_output.setText(m);
                });
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(MainActivity.this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
                String m = "Storage permission granted";
                textview_output.setText(m);
            } else {
                //Toast.makeText(MainActivity.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
                String m = "Storage permission denied";
                textview_output.setText(m);
            }
        }
    }

    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { permission }, requestCode);
        }
    }

    public void setup_src_spinner(ArrayList<String> supported_languages) {
        Collections.sort(supported_languages);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_textview_align, supported_languages);
        adapter.setDropDownViewResource(R.layout.spinner_textview_align);
        spinner_src_languages.setAdapter(adapter);
        spinner_src_languages.setSelection(supported_languages.indexOf("English"));
    }

    public void setup_dst_spinner(ArrayList<String> supported_languages) {
        Collections.sort(supported_languages);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_textview_align, supported_languages);
        adapter.setDropDownViewResource(R.layout.spinner_textview_align);
        spinner_dst_languages.setAdapter(adapter);
        spinner_dst_languages.setSelection(supported_languages.indexOf("Indonesian"));
    }

    public void setup_subtitle_format(ArrayList<String> supported_formats) {
        Collections.sort(supported_formats);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_textview_align, supported_formats);
        adapter.setDropDownViewResource(R.layout.spinner_textview_align);
        spinner_subtitle_format.setAdapter(adapter);
        spinner_subtitle_format.setSelection(supported_formats.indexOf("srt"));
    }


    ActivityResultLauncher<Intent> mStartForActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        if (intent != null) {
                            FILE.URI = intent.getData();
                        }
                        FILE.PATH = Uri2Path(getApplicationContext(), FILE.URI);
                        FILE.DISPLAY_NAME = queryName(getApplicationContext(), FILE.URI);
                        SUBTITLE.FILE_PATH = substring(FILE.PATH,0,FILE.PATH.length()-4) + "." + SUBTITLE.FORMAT;
                        SUBTITLE.TRANSLATED_FILE_PATH = substring(FILE.PATH,0,FILE.PATH.length()-4) + "_translated." + SUBTITLE.FORMAT;
                        runOnUiThread(() -> {
                            String t1 = "FILE.URI = " + FILE.URI;
                            textview_fileURI.setText(t1);
                            String t2 = "FILE.PATH = " + FILE.PATH;
                            textview_filePath.setText(t2);
                            String t3 = "FILE.DISPLAY_NAME = " + FILE.DISPLAY_NAME;
                            textview_fileDisplayName.setText(t3);
                        });
                    }
                }
            });

    private static String queryName(Context context, Uri uri) {
        Cursor returnCursor = context.getContentResolver().query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }

    public String Uri2Path(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }

        if(ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            System.out.println("uri.getPath() = " + uri.getPath());
            return uri.getPath();
        }

        else if(ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            String authority = uri.getAuthority();
            String idStr = "";

            if(authority.startsWith("com.android.externalstorage")) {
                String docId = DocumentsContract.getDocumentId(uri);
                String[] split = docId.split(":");
                String type = split[0];
                String fullPath = getPathFromExtSD(split);
                if (!fullPath.equals("")) {
                    System.out.println("fullPath = " + fullPath);
                    return fullPath;
                } else {
                    return null;
                }
            }

            else {
                if(authority.equals("media")) {
                    idStr = uri.toString().substring(uri.toString().lastIndexOf('/') + 1);
                    System.out.println("media idStr = " + idStr);
                }
                else if(authority.startsWith("com.android.providers")) {
                    idStr = DocumentsContract.getDocumentId(uri).split(":")[1];
                    System.out.println("providers idStr = " + idStr);
                }

                ContentResolver contentResolver = context.getContentResolver();
                Cursor cursor = contentResolver.query(MediaStore.Files.getContentUri("external"),
                        new String[] {MediaStore.Files.FileColumns.DATA},
                        "_id=?",
                        new String[]{idStr}, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    try {
                        int idx = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                        System.out.println("cursor.getString(idx) = " + cursor.getString(idx));
                        return cursor.getString(idx);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        cursor.close();
                    }
                }
            }
        }
        return null;
    }


    private String getPathFromExtSD(String[] pathData) {
        final String type = pathData[0];
        final String relativePath = File.separator + pathData[1];
        String fullPath = "";

        if ("primary".equalsIgnoreCase(type)) {
            System.out.println("PRIMARY");
            System.out.println("type = " + type);
            fullPath = Environment.getExternalStorageDirectory() + relativePath;
            if (fileExists(fullPath)) {
                return fullPath;
            }
        }
        // CHECK SECONDARY STORAGE
        else {
            fullPath = "/storage/" + type + File.separator + relativePath;
            if (fileExists(fullPath)) {
                return fullPath;
            }
        }
        return fullPath;
    }

    private static boolean fileExists(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    private void writeTextFile(String fileName) {
        OutputStream outputStream;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName); // file name required to contain extestion file mime
            values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS+"/DIRECTORY_NAME"); //DIRECTORY
            Uri extVolumeUri = MediaStore.Files.getContentUri("external");
            Uri fileUri = getApplicationContext().getContentResolver().insert(extVolumeUri, values);
            try {
                outputStream = getApplicationContext().getContentResolver().openOutputStream(fileUri);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            File root = new File(Environment.getExternalStorageDirectory()+File.separator+"DIRECTORY_NAME", "images");
            File file = new File(root, fileName );
            Log.d(TAG, "saveFile: file path - " + file.getAbsolutePath());
            try {
                outputStream = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        Uri uri = Uri.fromFile(new File(SUBTITLE.TRANSLATED_FILE_PATH));
        InputStream inputStream;
        try {
            inputStream = getApplicationContext().getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        //byte[] bytes = bodyData.getBytes();
        byte[] bytes = new byte[1024];
        int length;
        while (true) {
            try {
                if (!((length = inputStream.read(bytes)) > 0)) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                outputStream.write(bytes, 0, length);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
