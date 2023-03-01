package com.android.autosrt;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ContentResolver;
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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    ArrayList<String> arraylist_src_language_codes = new ArrayList<>();
    ArrayList<String> arraylist_dst_language_codes = new ArrayList<>();
    ArrayList<String> arraylist_src_languages = new ArrayList<>();
    ArrayList<String> arraylist_dst_languages = new ArrayList<>();
    Map<String, String> map_src_languages = new HashMap<>();
    Map<String, String> map_dst_languages = new HashMap<>();
    ArrayList<String> arraylist_subtitle_formats = new ArrayList<>();

    @SuppressLint("StaticFieldLeak")
    public static CheckBox checkbox_debug_mode;

    Spinner spinner_src_languages;
    TextView textview_src_code;

    @SuppressLint("StaticFieldLeak")
    public static CheckBox checkbox_create_translation;

    TextView textview_text2;
    Spinner spinner_dst_languages;
    TextView textview_dst_code;

    Spinner spinner_subtitle_format;
    TextView textview_subtitle_format;

    TextView textview_fileURI;
    TextView textview_filepath;
    TextView textview_fileDisplayName;

    Button button_browse;

    Button button_start;
    TextView textview_isTranscribing;

    @SuppressLint("StaticFieldLeak")
    public static TextView textview_output_messages_1;

    int STORAGE_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        arraylist_src_language_codes.add("af");
        arraylist_src_language_codes.add("sq");
        arraylist_src_language_codes.add("am");
        arraylist_src_language_codes.add("ar");
        arraylist_src_language_codes.add("hy");
        arraylist_src_language_codes.add("as");
        arraylist_src_language_codes.add("ay");
        arraylist_src_language_codes.add("az");
        arraylist_src_language_codes.add("bm");
        arraylist_src_language_codes.add("eu");
        arraylist_src_language_codes.add("be");
        arraylist_src_language_codes.add("bn");
        arraylist_src_language_codes.add("bho");
        arraylist_src_language_codes.add("bs");
        arraylist_src_language_codes.add("bg");
        arraylist_src_language_codes.add("ca");
        arraylist_src_language_codes.add("ceb");
        arraylist_src_language_codes.add("ny");
        arraylist_src_language_codes.add("zh-CN");
        arraylist_src_language_codes.add("zh-TW");
        arraylist_src_language_codes.add("co");
        arraylist_src_language_codes.add("hr");
        arraylist_src_language_codes.add("cs");
        arraylist_src_language_codes.add("da");
        arraylist_src_language_codes.add("dv");
        arraylist_src_language_codes.add("doi");
        arraylist_src_language_codes.add("nl");
        arraylist_src_language_codes.add("en");
        arraylist_src_language_codes.add("eo");
        arraylist_src_language_codes.add("et");
        arraylist_src_language_codes.add("ee");
        arraylist_src_language_codes.add("fil");
        arraylist_src_language_codes.add("fi");
        arraylist_src_language_codes.add("fr");
        arraylist_src_language_codes.add("fy");
        arraylist_src_language_codes.add("gl");
        arraylist_src_language_codes.add("ka");
        arraylist_src_language_codes.add("de");
        arraylist_src_language_codes.add("el");
        arraylist_src_language_codes.add("gn");
        arraylist_src_language_codes.add("gu");
        arraylist_src_language_codes.add("ht");
        arraylist_src_language_codes.add("ha");
        arraylist_src_language_codes.add("haw");
        arraylist_src_language_codes.add("he");
        arraylist_src_language_codes.add("hi");
        arraylist_src_language_codes.add("hmn");
        arraylist_src_language_codes.add("hu");
        arraylist_src_language_codes.add("is");
        arraylist_src_language_codes.add("ig");
        arraylist_src_language_codes.add("ilo");
        arraylist_src_language_codes.add("id");
        arraylist_src_language_codes.add("ga");
        arraylist_src_language_codes.add("it");
        arraylist_src_language_codes.add("ja");
        arraylist_src_language_codes.add("jv");
        arraylist_src_language_codes.add("kn");
        arraylist_src_language_codes.add("kk");
        arraylist_src_language_codes.add("km");
        arraylist_src_language_codes.add("rw");
        arraylist_src_language_codes.add("gom");
        arraylist_src_language_codes.add("ko");
        arraylist_src_language_codes.add("kri");
        arraylist_src_language_codes.add("kmr");
        arraylist_src_language_codes.add("ckb");
        arraylist_src_language_codes.add("ky");
        arraylist_src_language_codes.add("lo");
        arraylist_src_language_codes.add("la");
        arraylist_src_language_codes.add("lv");
        arraylist_src_language_codes.add("ln");
        arraylist_src_language_codes.add("lt");
        arraylist_src_language_codes.add("lg");
        arraylist_src_language_codes.add("lb");
        arraylist_src_language_codes.add("mk");
        arraylist_src_language_codes.add("mg");
        arraylist_src_language_codes.add("ms");
        arraylist_src_language_codes.add("ml");
        arraylist_src_language_codes.add("mt");
        arraylist_src_language_codes.add("mi");
        arraylist_src_language_codes.add("mr");
        arraylist_src_language_codes.add("mni-Mtei");
        arraylist_src_language_codes.add("lus");
        arraylist_src_language_codes.add("mn");
        arraylist_src_language_codes.add("my");
        arraylist_src_language_codes.add("ne");
        arraylist_src_language_codes.add("no");
        arraylist_src_language_codes.add("or");
        arraylist_src_language_codes.add("om");
        arraylist_src_language_codes.add("ps");
        arraylist_src_language_codes.add("fa");
        arraylist_src_language_codes.add("pl");
        arraylist_src_language_codes.add("pt");
        arraylist_src_language_codes.add("pa");
        arraylist_src_language_codes.add("qu");
        arraylist_src_language_codes.add("ro");
        arraylist_src_language_codes.add("ru");
        arraylist_src_language_codes.add("sm");
        arraylist_src_language_codes.add("sa");
        arraylist_src_language_codes.add("gd");
        arraylist_src_language_codes.add("nso");
        arraylist_src_language_codes.add("sr");
        arraylist_src_language_codes.add("st");
        arraylist_src_language_codes.add("sn");
        arraylist_src_language_codes.add("sd");
        arraylist_src_language_codes.add("si");
        arraylist_src_language_codes.add("sk");
        arraylist_src_language_codes.add("sl");
        arraylist_src_language_codes.add("so");
        arraylist_src_language_codes.add("es");
        arraylist_src_language_codes.add("su");
        arraylist_src_language_codes.add("sw");
        arraylist_src_language_codes.add("sv");
        arraylist_src_language_codes.add("tg");
        arraylist_src_language_codes.add("ta");
        arraylist_src_language_codes.add("tt");
        arraylist_src_language_codes.add("te");
        arraylist_src_language_codes.add("th");
        arraylist_src_language_codes.add("ti");
        arraylist_src_language_codes.add("ts");
        arraylist_src_language_codes.add("tr");
        arraylist_src_language_codes.add("tk");
        arraylist_src_language_codes.add("tw");
        arraylist_src_language_codes.add("uk");
        arraylist_src_language_codes.add("ur");
        arraylist_src_language_codes.add("ug");
        arraylist_src_language_codes.add("uz");
        arraylist_src_language_codes.add("vi");
        arraylist_src_language_codes.add("cy");
        arraylist_src_language_codes.add("xh");
        arraylist_src_language_codes.add("yi");
        arraylist_src_language_codes.add("yo");
        arraylist_src_language_codes.add("zu");

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
            map_src_languages.put(arraylist_src_languages.get(i), arraylist_src_language_codes.get(i));
        }

        arraylist_dst_language_codes.add("af");
        arraylist_dst_language_codes.add("sq");
        arraylist_dst_language_codes.add("am");
        arraylist_dst_language_codes.add("ar");
        arraylist_dst_language_codes.add("hy");
        arraylist_dst_language_codes.add("as");
        arraylist_dst_language_codes.add("ay");
        arraylist_dst_language_codes.add("az");
        arraylist_dst_language_codes.add("bm");
        arraylist_dst_language_codes.add("eu");
        arraylist_dst_language_codes.add("be");
        arraylist_dst_language_codes.add("bn");
        arraylist_dst_language_codes.add("bho");
        arraylist_dst_language_codes.add("bs");
        arraylist_dst_language_codes.add("bg");
        arraylist_dst_language_codes.add("ca");
        arraylist_dst_language_codes.add("ceb");
        arraylist_dst_language_codes.add("ny");
        arraylist_dst_language_codes.add("zh-CN");
        arraylist_dst_language_codes.add("zh-TW");
        arraylist_dst_language_codes.add("co");
        arraylist_dst_language_codes.add("hr");
        arraylist_dst_language_codes.add("cs");
        arraylist_dst_language_codes.add("da");
        arraylist_dst_language_codes.add("dv");
        arraylist_dst_language_codes.add("doi");
        arraylist_dst_language_codes.add("nl");
        arraylist_dst_language_codes.add("en");
        arraylist_dst_language_codes.add("eo");
        arraylist_dst_language_codes.add("et");
        arraylist_dst_language_codes.add("ee");
        arraylist_dst_language_codes.add("fil");
        arraylist_dst_language_codes.add("fi");
        arraylist_dst_language_codes.add("fr");
        arraylist_dst_language_codes.add("fy");
        arraylist_dst_language_codes.add("gl");
        arraylist_dst_language_codes.add("ka");
        arraylist_dst_language_codes.add("de");
        arraylist_dst_language_codes.add("el");
        arraylist_dst_language_codes.add("gn");
        arraylist_dst_language_codes.add("gu");
        arraylist_dst_language_codes.add("ht");
        arraylist_dst_language_codes.add("ha");
        arraylist_dst_language_codes.add("haw");
        arraylist_dst_language_codes.add("he");
        arraylist_dst_language_codes.add("hi");
        arraylist_dst_language_codes.add("hmn");
        arraylist_dst_language_codes.add("hu");
        arraylist_dst_language_codes.add("is");
        arraylist_dst_language_codes.add("ig");
        arraylist_dst_language_codes.add("ilo");
        arraylist_dst_language_codes.add("id");
        arraylist_dst_language_codes.add("ga");
        arraylist_dst_language_codes.add("it");
        arraylist_dst_language_codes.add("ja");
        arraylist_dst_language_codes.add("jv");
        arraylist_dst_language_codes.add("kn");
        arraylist_dst_language_codes.add("kk");
        arraylist_dst_language_codes.add("km");
        arraylist_dst_language_codes.add("rw");
        arraylist_dst_language_codes.add("gom");
        arraylist_dst_language_codes.add("ko");
        arraylist_dst_language_codes.add("kri");
        arraylist_dst_language_codes.add("kmr");
        arraylist_dst_language_codes.add("ckb");
        arraylist_dst_language_codes.add("ky");
        arraylist_dst_language_codes.add("lo");
        arraylist_dst_language_codes.add("la");
        arraylist_dst_language_codes.add("lv");
        arraylist_dst_language_codes.add("ln");
        arraylist_dst_language_codes.add("lt");
        arraylist_dst_language_codes.add("lg");
        arraylist_dst_language_codes.add("lb");
        arraylist_dst_language_codes.add("mk");
        arraylist_dst_language_codes.add("mg");
        arraylist_dst_language_codes.add("ms");
        arraylist_dst_language_codes.add("ml");
        arraylist_dst_language_codes.add("mt");
        arraylist_dst_language_codes.add("mi");
        arraylist_dst_language_codes.add("mr");
        arraylist_dst_language_codes.add("mni-Mtei");
        arraylist_dst_language_codes.add("lus");
        arraylist_dst_language_codes.add("mn");
        arraylist_dst_language_codes.add("my");
        arraylist_dst_language_codes.add("ne");
        arraylist_dst_language_codes.add("no");
        arraylist_dst_language_codes.add("or");
        arraylist_dst_language_codes.add("om");
        arraylist_dst_language_codes.add("ps");
        arraylist_dst_language_codes.add("fa");
        arraylist_dst_language_codes.add("pl");
        arraylist_dst_language_codes.add("pt");
        arraylist_dst_language_codes.add("pa");
        arraylist_dst_language_codes.add("qu");
        arraylist_dst_language_codes.add("ro");
        arraylist_dst_language_codes.add("ru");
        arraylist_dst_language_codes.add("sm");
        arraylist_dst_language_codes.add("sa");
        arraylist_dst_language_codes.add("gd");
        arraylist_dst_language_codes.add("nso");
        arraylist_dst_language_codes.add("sr");
        arraylist_dst_language_codes.add("st");
        arraylist_dst_language_codes.add("sn");
        arraylist_dst_language_codes.add("sd");
        arraylist_dst_language_codes.add("si");
        arraylist_dst_language_codes.add("sk");
        arraylist_dst_language_codes.add("sl");
        arraylist_dst_language_codes.add("so");
        arraylist_dst_language_codes.add("es");
        arraylist_dst_language_codes.add("su");
        arraylist_dst_language_codes.add("sw");
        arraylist_dst_language_codes.add("sv");
        arraylist_dst_language_codes.add("tg");
        arraylist_dst_language_codes.add("ta");
        arraylist_dst_language_codes.add("tt");
        arraylist_dst_language_codes.add("te");
        arraylist_dst_language_codes.add("th");
        arraylist_dst_language_codes.add("ti");
        arraylist_dst_language_codes.add("ts");
        arraylist_dst_language_codes.add("tr");
        arraylist_dst_language_codes.add("tk");
        arraylist_dst_language_codes.add("tw");
        arraylist_dst_language_codes.add("uk");
        arraylist_dst_language_codes.add("ur");
        arraylist_dst_language_codes.add("ug");
        arraylist_dst_language_codes.add("uz");
        arraylist_dst_language_codes.add("vi");
        arraylist_dst_language_codes.add("cy");
        arraylist_dst_language_codes.add("xh");
        arraylist_dst_language_codes.add("yi");
        arraylist_dst_language_codes.add("yo");
        arraylist_dst_language_codes.add("zu");

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
            map_dst_languages.put(arraylist_dst_languages.get(i), arraylist_dst_language_codes.get(i));
        }

        arraylist_subtitle_formats.add("srt");
        arraylist_subtitle_formats.add("vtt");
        arraylist_subtitle_formats.add("json");
        arraylist_subtitle_formats.add("raw");

        setContentView(R.layout.activity_main);

        checkbox_debug_mode = findViewById(R.id.checkbox_debug_mode);

        spinner_src_languages = findViewById(R.id.spinner_src_languages);
        setup_src_spinner(arraylist_src_languages);
        textview_src_code = findViewById(R.id.textview_src_code);

        checkbox_create_translation = findViewById(R.id.checkbox_create_translation);

        textview_text2 = findViewById(R.id.textview_text2);
        spinner_dst_languages = findViewById(R.id.spinner_dst_languages);
        setup_dst_spinner(arraylist_dst_languages);
        textview_dst_code = findViewById(R.id.textview_dst_code);

        spinner_subtitle_format = findViewById(R.id.spinner_subtitle_format);
        setup_subtitle_format(arraylist_subtitle_formats);
        textview_subtitle_format = findViewById(R.id.textview_subtitle_format);

        textview_fileURI = findViewById(R.id.textview_fileURI);
        textview_filepath = findViewById(R.id.textview_filepath);
        textview_fileDisplayName = findViewById(R.id.textview_fileDisplayName);

        button_browse = findViewById(R.id.button_browse);
        button_start = findViewById(R.id.button_start);
        textview_isTranscribing = findViewById(R.id.textview_isTranscribing);
        textview_output_messages_1 = findViewById(R.id.textview_output_messages_1);

        textview_fileURI.setTextIsSelectable(true);
        textview_filepath.setTextIsSelectable(true);
        textview_fileDisplayName.setTextIsSelectable(true);
        textview_output_messages_1.setTextIsSelectable(true);

        textview_fileURI.setSelected(true);
        textview_filepath.setSelected(true);
        textview_fileDisplayName.setSelected(true);
        textview_output_messages_1.setSelected(true);

        spinner_src_languages.setFocusable(true);
        spinner_src_languages.requestFocus();

        textview_fileURI.setMovementMethod(new ScrollingMovementMethod());
        textview_filepath.setMovementMethod(new ScrollingMovementMethod());
        textview_fileDisplayName.setMovementMethod(new ScrollingMovementMethod());
        textview_output_messages_1.setMovementMethod(new ScrollingMovementMethod());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.actionbar_title);
        }

        TRANSCRIBE_STATUS.IS_TRANSCRIBING = false;
        CANCEL_STATUS.IS_CANCELING = true;

        String t = "TRANSCRIBE_STATUS.IS_TRANSCRIBING = " + TRANSCRIBE_STATUS.IS_TRANSCRIBING;
        runOnUiThread(() -> textview_isTranscribing.setText(t));

        checkbox_debug_mode.setOnClickListener(view -> {
            runOnUiThread(() -> {
                textview_fileURI.setText("");
                textview_filepath.setText("");
                textview_fileDisplayName.setText("");
            });
            if(((CompoundButton) view).isChecked()){
                textview_src_code.setVisibility(View.VISIBLE);
                textview_dst_code.setVisibility(View.VISIBLE);
                textview_subtitle_format.setVisibility(View.VISIBLE);
                textview_fileURI.setVisibility(View.VISIBLE);
                textview_fileDisplayName.setVisibility(View.VISIBLE);
                textview_isTranscribing.setVisibility(View.VISIBLE);
                runOnUiThread(() -> {
                    if (FILE.PATH_LIST != null) {
                        for (int i = 0; i < FILE.URI_LIST.size(); i++) {
                            String t1 = "FILE.URI_LIST.get(" + i + ") = " + FILE.URI_LIST.get(i);
                            textview_fileURI.append(t1 + "\n");
                            String t2 = "FILE.PATH_LIST.get(" + i + ") = " + FILE.PATH_LIST.get(i);
                            textview_filepath.append(t2 + "\n");
                            String t3 = "FILE.DISPLAY_NAME_LIST.get(" + i + ") = " + FILE.DISPLAY_NAME_LIST.get(i);
                            textview_fileDisplayName.append(t3 + "\n");
                        }

                    } else {
                        textview_filepath.setHint("FILE.PATH_LIST");
                    }
                });
            }
            else {
                textview_src_code.setVisibility(View.GONE);
                textview_dst_code.setVisibility(View.GONE);
                textview_subtitle_format.setVisibility(View.GONE);
                textview_fileURI.setVisibility(View.GONE);
                textview_fileDisplayName.setVisibility(View.GONE);
                textview_isTranscribing.setVisibility(View.GONE);
                runOnUiThread(() -> {
                    if (FILE.PATH_LIST != null) {
                        for (int i=0; i< FILE.PATH_LIST.size(); i++) {
                            //String fp = "File path [" + i + "] = " + FILE.PATH_LIST.get(i) + "\n";
                            String fp = FILE.PATH_LIST.get(i) + "\n";
                            textview_filepath.append(fp);
                        }
                    }
                    else {
                        textview_filepath.setHint("File path");
                    }
                });
            }
        });

        if(checkbox_debug_mode.isChecked()){
            textview_fileURI.setText("");
            textview_filepath.setText("");
            textview_fileDisplayName.setText("");
            textview_src_code.setVisibility(View.VISIBLE);
            textview_dst_code.setVisibility(View.VISIBLE);
            textview_subtitle_format.setVisibility(View.VISIBLE);
            textview_fileURI.setVisibility(View.VISIBLE);
            textview_fileDisplayName.setVisibility(View.VISIBLE);
            textview_isTranscribing.setVisibility(View.VISIBLE);
            runOnUiThread(() -> {
                if (FILE.PATH_LIST != null) {
                    for (int i = 0; i < FILE.URI_LIST.size(); i++) {
                        String t1 = "FILE.URI_LIST.get(" + i + ") = " + FILE.URI_LIST.get(i);
                        textview_fileURI.append(t1 + "\n");
                        String t2 = "FILE.PATH_LIST.get(" + i + ") = " + FILE.PATH_LIST.get(i);
                        textview_filepath.append(t2 + "\n");
                        String t3 = "FILE.DISPLAY_NAME_LIST.get(" + i + ") = " + FILE.DISPLAY_NAME_LIST.get(i);
                        textview_fileDisplayName.append(t3 + "\n");
                    }
                }
                else {
                    textview_filepath.setHint("FILE.PATH_LIST");
                }
            });
        }
        else {
            textview_src_code.setVisibility(View.GONE);
            textview_dst_code.setVisibility(View.GONE);
            textview_subtitle_format.setVisibility(View.GONE);
            textview_fileURI.setVisibility(View.GONE);
            textview_fileDisplayName.setVisibility(View.GONE);
            textview_isTranscribing.setVisibility(View.GONE);
            runOnUiThread(() -> {
                if (FILE.PATH_LIST != null) {
                    for (int i=0; i< FILE.PATH_LIST.size(); i++) {
                        //String fp = "File path [" + i + "] = " + FILE.PATH_LIST.get(i) + "\n";
                        String fp = FILE.PATH_LIST.get(i) + "\n";
                        textview_filepath.append(fp);
                    }
                }
                else {
                    textview_filepath.setHint("File path");
                }
            });
        }

        spinner_src_languages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                LANGUAGE.SRC_LANGUAGE = spinner_src_languages.getSelectedItem().toString();
                LANGUAGE.DST_LANGUAGE = spinner_dst_languages.getSelectedItem().toString();
                LANGUAGE.SRC_CODE = map_src_languages.get(LANGUAGE.SRC_LANGUAGE);
                LANGUAGE.DST_CODE = map_dst_languages.get(LANGUAGE.DST_LANGUAGE);
                runOnUiThread(() -> {
                    String lsrc = "LANGUAGE.SRC_CODE = " + LANGUAGE.SRC_CODE;
                    textview_src_code.setText(lsrc);
                });
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                LANGUAGE.SRC_LANGUAGE = spinner_src_languages.getSelectedItem().toString();
                LANGUAGE.DST_LANGUAGE = spinner_dst_languages.getSelectedItem().toString();
                LANGUAGE.SRC_CODE = map_src_languages.get(LANGUAGE.SRC_LANGUAGE);
                LANGUAGE.DST_CODE = map_dst_languages.get(LANGUAGE.DST_LANGUAGE);
                runOnUiThread(() -> {
                    String lsrc = "LANGUAGE.SRC_CODE = " + LANGUAGE.SRC_CODE;
                    textview_src_code.setText(lsrc);
                });
            }
        });

        spinner_dst_languages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                LANGUAGE.SRC_LANGUAGE = spinner_src_languages.getSelectedItem().toString();
                LANGUAGE.DST_LANGUAGE = spinner_dst_languages.getSelectedItem().toString();
                LANGUAGE.SRC_CODE = map_src_languages.get(LANGUAGE.SRC_LANGUAGE);
                LANGUAGE.DST_CODE = map_dst_languages.get(LANGUAGE.DST_LANGUAGE);
                runOnUiThread(() -> {
                    String ldst = "LANGUAGE.DST_CODE = " + LANGUAGE.DST_CODE;
                    textview_dst_code.setText(ldst);
                });
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                LANGUAGE.SRC_LANGUAGE = spinner_src_languages.getSelectedItem().toString();
                LANGUAGE.DST_LANGUAGE = spinner_dst_languages.getSelectedItem().toString();
                LANGUAGE.SRC_CODE = map_src_languages.get(LANGUAGE.SRC_LANGUAGE);
                LANGUAGE.DST_CODE = map_dst_languages.get(LANGUAGE.DST_LANGUAGE);
                runOnUiThread(() -> {
                    String ldst = "LANGUAGE.DST_CODE = " + LANGUAGE.DST_CODE;
                    textview_dst_code.setText(ldst);
                });
            }
        });

        checkbox_create_translation.setOnClickListener(view -> {
            if(((CompoundButton) view).isChecked()){
                textview_text2.setVisibility(View.VISIBLE);
                spinner_dst_languages.setVisibility(View.VISIBLE);
                textview_dst_code.setVisibility(View.VISIBLE);

                spinner_dst_languages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        LANGUAGE.SRC_LANGUAGE = spinner_src_languages.getSelectedItem().toString();
                        LANGUAGE.DST_LANGUAGE = spinner_dst_languages.getSelectedItem().toString();
                        LANGUAGE.SRC_CODE = map_src_languages.get(LANGUAGE.SRC_LANGUAGE);
                        LANGUAGE.DST_CODE = map_dst_languages.get(LANGUAGE.DST_LANGUAGE);
                        if (checkbox_debug_mode.isChecked()) {
                            runOnUiThread(() -> {
                                String ldst = "LANGUAGE.DST_CODE = " + LANGUAGE.DST_CODE;
                                textview_dst_code.setText(ldst);
                            });
                        }
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                        LANGUAGE.SRC_LANGUAGE = spinner_src_languages.getSelectedItem().toString();
                        LANGUAGE.DST_LANGUAGE = spinner_dst_languages.getSelectedItem().toString();
                        LANGUAGE.SRC_CODE = map_src_languages.get(LANGUAGE.SRC_LANGUAGE);
                        LANGUAGE.DST_CODE = map_dst_languages.get(LANGUAGE.DST_LANGUAGE);
                        if (checkbox_debug_mode.isChecked()) {
                            runOnUiThread(() -> {
                                String ldst = "LANGUAGE.DST_CODE = " + LANGUAGE.DST_CODE;
                                textview_dst_code.setText(ldst);
                            });
                        }
                    }
                });
            }
            else {
                textview_text2.setVisibility(View.GONE);
                spinner_dst_languages.setVisibility(View.GONE);
                textview_dst_code.setVisibility(View.GONE);

                LANGUAGE.DST_LANGUAGE = LANGUAGE.SRC_LANGUAGE;
                spinner_dst_languages.setSelection(arraylist_dst_languages.indexOf(LANGUAGE.DST_LANGUAGE));
                LANGUAGE.DST_CODE = map_dst_languages.get(LANGUAGE.DST_LANGUAGE);
            }
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
                SUBTITLE.FORMAT = spinner_subtitle_format.getSelectedItem().toString();
                runOnUiThread(() -> {
                    String sf = "SUBTITLE.FORMAT = " + SUBTITLE.FORMAT;
                    textview_subtitle_format.setText(sf);
                });
            }
        });

        button_browse.setOnClickListener(view -> {
            textview_output_messages_1.setText("");
            textview_filepath.setText("");
            FILE.URI_LIST = null;
            FILE.URI_LIST = new ArrayList<>();
            FILE.PATH_LIST = null;
            FILE.PATH_LIST = new ArrayList<>();
            FILE.DISPLAY_NAME_LIST = null;
            FILE.DISPLAY_NAME_LIST = new ArrayList<>();
            SUBTITLE.FILE_PATH_LIST = null;
            SUBTITLE.FILE_PATH_LIST = new ArrayList<>();
            SUBTITLE.TRANSLATED_FILE_PATH_LIST = null;
            SUBTITLE.TRANSLATED_FILE_PATH_LIST = new ArrayList<>();
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            String[] mimeTypes = {"video/*", "audio/*"};
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            mStartForActivity.launch(intent);
        });

        button_start.setOnClickListener(view -> {
            if (FILE.URI_LIST != null) {
                TRANSCRIBE_STATUS.IS_TRANSCRIBING = true;
                CANCEL_STATUS.IS_CANCELING = false;
                String it = "TRANSCRIBE_STATUS.IS_TRANSCRIBING = " + TRANSCRIBE_STATUS.IS_TRANSCRIBING;
                runOnUiThread(() -> textview_isTranscribing.setText(it));
                Intent intent = new Intent(MainActivity.this, TranscribeActivity.class);
                MainActivity.this.startActivity(intent);
            }
            else {
                TRANSCRIBE_STATUS.IS_TRANSCRIBING = false;
                CANCEL_STATUS.IS_CANCELING = true;
                runOnUiThread(() -> {
                    String m = "Please select at least 1 video/audio file\n";
                    textview_output_messages_1.setText(m);
                });
            }
        });

        if (SDK_INT >= Build.VERSION_CODES.Q && Environment.isExternalStorageRemovable()) {
            Uri uri = Uri.parse("package:" + MainActivity.this.getPackageName());
            startActivity(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, uri));
        } else {
            checkPermission(WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(MainActivity.this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
                String m = "Storage permission granted";
                runOnUiThread(() -> textview_output_messages_1.setText(m));
            } else {
                //Toast.makeText(MainActivity.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
                String m = "Storage permission denied";
                runOnUiThread(() -> textview_output_messages_1.setText(m));
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
                        ClipData cd = null;
                        if (intent != null) {
                            cd = intent.getClipData();
                        }
                        if (cd == null) {
                            Uri fileURI = intent.getData();
                            FILE.URI_LIST.add(fileURI);
                            String filePath = Uri2Path(getApplicationContext(), fileURI);
                            FILE.PATH_LIST.add(filePath);
                            String fileDisplayName = queryName(getApplicationContext(), fileURI);
                            FILE.DISPLAY_NAME_LIST.add(fileDisplayName);
                            runOnUiThread(() -> {
                                textview_fileURI.setText("");
                                textview_filepath.setText("");
                                textview_fileDisplayName.setText("");
                                for (int i = 0; i < FILE.URI_LIST.size(); i++) {
                                    String t1 = "FILE.URI_LIST.get(" + i + ") = " + FILE.URI_LIST.get(i);
                                    textview_fileURI.append(t1 + "\n");
                                    if (checkbox_debug_mode.isChecked()) {
                                        String t2 = "FILE.PATH_LIST.get(" + i + ") = " + FILE.PATH_LIST.get(i);
                                        textview_filepath.append(t2 + "\n");
                                    } else {
                                        String t2 = FILE.PATH_LIST.get(i);
                                        textview_filepath.append(t2 + "\n");
                                    }
                                    String t3 = "FILE.DISPLAY_NAME_LIST.get(" + i + ") = " + FILE.DISPLAY_NAME_LIST.get(i);
                                    textview_fileDisplayName.append(t3 + "\n");
                                }
                            });
                        }
                        else if (intent != null && cd != null) {
                            for (int i = 0; i < cd.getItemCount(); i++) {
                                Uri fileURI = intent.getClipData().getItemAt(i).getUri();
                                FILE.URI_LIST.add(fileURI);
                                String filePath = Uri2Path(getApplicationContext(), fileURI);
                                FILE.PATH_LIST.add(filePath);
                                String fileDisplayName = queryName(getApplicationContext(), fileURI);
                                FILE.DISPLAY_NAME_LIST.add(fileDisplayName);
                            }
                            runOnUiThread(() -> {
                                textview_fileURI.setText("");
                                textview_filepath.setText("");
                                textview_fileDisplayName.setText("");
                                if (checkbox_debug_mode.isChecked()) {
                                    for (int i = 0; i < FILE.URI_LIST.size(); i++) {
                                        String t1 = "FILE.URI_LIST.get(" + i + ") = " + FILE.URI_LIST.get(i);
                                        textview_fileURI.append(t1 + "\n");
                                        String t2 = "FILE.PATH_LIST.get(" + i + ") = " + FILE.PATH_LIST.get(i);
                                        textview_filepath.append(t2 + "\n");
                                        String t3 = "FILE.DISPLAY_NAME_LIST.get(" + i + ") = " + FILE.DISPLAY_NAME_LIST.get(i);
                                        textview_fileDisplayName.append(t3 + "\n");
                                    }
                                } else {
                                    for (int i = 0; i < FILE.URI_LIST.size(); i++) {
                                        String t2 = FILE.PATH_LIST.get(i);
                                        textview_filepath.append(t2 + "\n");
                                    }
                                }
                            });
                        }
                        else {
                            runOnUiThread(() -> {
                                String msg = "Please select at least 1 video/audio file";
                                textview_output_messages_1.setText(msg);
                            });
                        }
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
                    return idStr;
                }
                else if(authority.startsWith("com.android.providers")) {
                    idStr = DocumentsContract.getDocumentId(uri).split(":")[1];
                    System.out.println("providers idStr = " + idStr);
                    return idStr;
                }

                ContentResolver contentResolver = context.getContentResolver();
                Cursor cursor = contentResolver.query(MediaStore.Files.getContentUri("external"),
                        new String[] {MediaStore.Files.FileColumns.DATA},
                        "_id=?",
                        new String[]{idStr}, null);
                if (cursor != null && cursor.getCount()>0 && cursor.moveToFirst()) {
                    //cursor.moveToFirst();
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
        String fullPath;

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

}
