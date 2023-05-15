package com.android.autosrt;

import static android.os.Environment.DIRECTORY_DOCUMENTS;
import static android.provider.DocumentsContract.EXTRA_INITIAL_URI;
import static android.provider.Settings.AUTHORITY;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.storage.StorageManager;
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
import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    ArrayList<String> arraylist_src_language_codes = new ArrayList<>();
    ArrayList<String> arraylist_dst_language_codes = new ArrayList<>();
    ArrayList<String> arraylist_src_languages = new ArrayList<>();
    ArrayList<String> arraylist_dst_languages = new ArrayList<>();
    Map<String, String> map_src_languages = new HashMap<>();
    Map<String, String> map_dst_languages = new HashMap<>();
    ArrayList<String> arraylist_subtitle_formats = new ArrayList<>();

    Spinner spinner_src_languages;
    @SuppressLint("StaticFieldLeak")
    public static CheckBox checkbox_create_translation;
    TextView textview_text2;
    Spinner spinner_dst_languages;
    Spinner spinner_subtitle_format;
    TextView textview_filepath;
    Button button_browse;
    Button button_grant_storage_permission;
    TextView textview_grant_storage_permission_notes;
    Button button_grant_manage_app_all_files_access_permission;
    TextView textview_grant_manage_app_all_files_access_permission_notes;
    Button button_grant_persisted_tree_uri_permission;
    TextView textview_grant_persisted_tree_uri_permission_notes;
    Button button_start;
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

        spinner_src_languages = findViewById(R.id.spinner_src_languages);
        setup_src_spinner(arraylist_src_languages);

        checkbox_create_translation = findViewById(R.id.checkbox_create_translation);

        textview_text2 = findViewById(R.id.textview_text2);
        spinner_dst_languages = findViewById(R.id.spinner_dst_languages);
        setup_dst_spinner(arraylist_dst_languages);

        spinner_subtitle_format = findViewById(R.id.spinner_subtitle_format);
        setup_subtitle_format(arraylist_subtitle_formats);

        textview_filepath = findViewById(R.id.textview_filepath);

        button_grant_storage_permission = findViewById(R.id.button_grant_storage_permission);
        textview_grant_storage_permission_notes = findViewById(R.id.textview_grant_storage_permission_notes);
        button_grant_manage_app_all_files_access_permission = findViewById(R.id.button_grant_manage_app_all_files_access_permission);
        textview_grant_manage_app_all_files_access_permission_notes = findViewById(R.id.textview_grant_manage_app_all_files_access_permission_notes);
        button_grant_persisted_tree_uri_permission = findViewById(R.id.button_grant_persisted_tree_uri_permission);
        textview_grant_persisted_tree_uri_permission_notes = findViewById(R.id.textview_grant_persisted_tree_uri_permission_notes);

        button_browse = findViewById(R.id.button_browse);
        button_start = findViewById(R.id.button_start);
        textview_output_messages_1 = findViewById(R.id.textview_output_messages_1);

        textview_filepath.setTextIsSelectable(true);
        textview_output_messages_1.setTextIsSelectable(true);

        textview_filepath.setSelected(true);
        textview_output_messages_1.setSelected(true);

        spinner_src_languages.setFocusable(true);
        spinner_src_languages.requestFocus();

        textview_filepath.setMovementMethod(new ScrollingMovementMethod());
        textview_output_messages_1.setMovementMethod(new ScrollingMovementMethod());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            button_grant_manage_app_all_files_access_permission.setVisibility(View.VISIBLE);
            textview_grant_manage_app_all_files_access_permission_notes.setVisibility(View.VISIBLE);
        }
        else {
            button_grant_manage_app_all_files_access_permission.setVisibility(View.GONE);
            textview_grant_manage_app_all_files_access_permission_notes.setVisibility(View.GONE);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.actionbar_title);
        }

        TRANSCRIBE_STATUS.IS_TRANSCRIBING = false;
        //CANCEL_STATUS.IS_CANCELING = true;


        spinner_src_languages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                LANGUAGE.SRC_LANGUAGE = spinner_src_languages.getSelectedItem().toString();
                LANGUAGE.DST_LANGUAGE = spinner_dst_languages.getSelectedItem().toString();
                LANGUAGE.SRC_CODE = map_src_languages.get(LANGUAGE.SRC_LANGUAGE);
                LANGUAGE.DST_CODE = map_dst_languages.get(LANGUAGE.DST_LANGUAGE);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                LANGUAGE.SRC_LANGUAGE = spinner_src_languages.getSelectedItem().toString();
                LANGUAGE.DST_LANGUAGE = spinner_dst_languages.getSelectedItem().toString();
                LANGUAGE.SRC_CODE = map_src_languages.get(LANGUAGE.SRC_LANGUAGE);
                LANGUAGE.DST_CODE = map_dst_languages.get(LANGUAGE.DST_LANGUAGE);
            }
        });


        spinner_dst_languages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                LANGUAGE.SRC_LANGUAGE = spinner_src_languages.getSelectedItem().toString();
                LANGUAGE.DST_LANGUAGE = spinner_dst_languages.getSelectedItem().toString();
                LANGUAGE.SRC_CODE = map_src_languages.get(LANGUAGE.SRC_LANGUAGE);
                LANGUAGE.DST_CODE = map_dst_languages.get(LANGUAGE.DST_LANGUAGE);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                LANGUAGE.SRC_LANGUAGE = spinner_src_languages.getSelectedItem().toString();
                LANGUAGE.DST_LANGUAGE = spinner_dst_languages.getSelectedItem().toString();
                LANGUAGE.SRC_CODE = map_src_languages.get(LANGUAGE.SRC_LANGUAGE);
                LANGUAGE.DST_CODE = map_dst_languages.get(LANGUAGE.DST_LANGUAGE);
            }
        });


        checkbox_create_translation.setOnClickListener(view -> {
            if(((CompoundButton) view).isChecked()){
                textview_text2.setVisibility(View.VISIBLE);
                spinner_dst_languages.setVisibility(View.VISIBLE);

                spinner_dst_languages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        LANGUAGE.SRC_LANGUAGE = spinner_src_languages.getSelectedItem().toString();
                        LANGUAGE.DST_LANGUAGE = spinner_dst_languages.getSelectedItem().toString();
                        LANGUAGE.SRC_CODE = map_src_languages.get(LANGUAGE.SRC_LANGUAGE);
                        LANGUAGE.DST_CODE = map_dst_languages.get(LANGUAGE.DST_LANGUAGE);
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                        LANGUAGE.SRC_LANGUAGE = spinner_src_languages.getSelectedItem().toString();
                        LANGUAGE.DST_LANGUAGE = spinner_dst_languages.getSelectedItem().toString();
                        LANGUAGE.SRC_CODE = map_src_languages.get(LANGUAGE.SRC_LANGUAGE);
                        LANGUAGE.DST_CODE = map_dst_languages.get(LANGUAGE.DST_LANGUAGE);
                    }
                });
            }
            else {
                textview_text2.setVisibility(View.GONE);
                spinner_dst_languages.setVisibility(View.GONE);

                LANGUAGE.DST_LANGUAGE = LANGUAGE.SRC_LANGUAGE;
                spinner_dst_languages.setSelection(arraylist_dst_languages.indexOf(LANGUAGE.DST_LANGUAGE));
                LANGUAGE.DST_CODE = map_dst_languages.get(LANGUAGE.DST_LANGUAGE);
            }
        });


        spinner_subtitle_format.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                SUBTITLE.FORMAT = spinner_subtitle_format.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                SUBTITLE.FORMAT = spinner_subtitle_format.getSelectedItem().toString();
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
            SUBTITLE.TMP_FILE_PATH_LIST = null;
            SUBTITLE.TMP_FILE_PATH_LIST = new ArrayList<>();
            SUBTITLE.TMP_TRANSLATED_FILE_PATH_LIST = null;
            SUBTITLE.TMP_TRANSLATED_FILE_PATH_LIST = new ArrayList<>();
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            String[] mimeTypes = {"video/*", "audio/*"};
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            intent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                            | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                            | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
            startForBrowseFileActivity.launch(intent);
        });


        button_grant_storage_permission.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            button_grant_manage_app_all_files_access_permission.setOnClickListener(view -> {
                if (!Environment.isExternalStorageManager()) {
                    try {
                        Uri uri = Uri.parse("package:${BuildConfig.LIBRARY_PACKAGE_NAME}");
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri);
                        //Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION, uri);
                        intent.addCategory("android.intent.category.DEFAULT");
                        intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                        //startActivity(intent);
                        startForRequestManageAppAllFileAccessPermissionActivity.launch(intent);
                    } catch (Exception e) {
                        Log.e("Exception: ", Objects.requireNonNull(e.getMessage()));
                        e.printStackTrace();
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                        startActivity(intent);
                    }
                }
            });
        }


        button_grant_persisted_tree_uri_permission.setOnClickListener(view -> {
            FOLDER.SAVED_URI_LIST = loadSavedTreeUrisFromSharedPreference();
            Log.d("onCreated", "FOLDER.SAVED_URI_LIST.size() = " + FOLDER.SAVED_URI_LIST.size());
            for (int i=0; i<FOLDER.SAVED_URI_LIST.size(); i++) {
                Log.d("onCreated", "FOLDER.SAVED_URI_LIST.get(" + i + ") = " + FOLDER.SAVED_URI_LIST.get(i));
            }
            requestTreeUriPermissions();
        });


        button_start.setOnClickListener(view -> {
            TranscribeActivity.cancelFile = getApplicationContext().getExternalFilesDir(null) + File.separator + "cancel.txt";
            if (new File(TranscribeActivity.cancelFile).exists() && new File(TranscribeActivity.cancelFile).delete()) {
                Log.d("button_start", TranscribeActivity.cancelFile + " deleted");
            }
            if (FILE.URI_LIST != null) {
                TRANSCRIBE_STATUS.IS_TRANSCRIBING = true;
                //CANCEL_STATUS.IS_CANCELING = false;
                Intent intent = new Intent(MainActivity.this, TranscribeActivity.class);
                MainActivity.this.startActivity(intent);
            }
            else {
                TRANSCRIBE_STATUS.IS_TRANSCRIBING = false;
                //CANCEL_STATUS.IS_CANCELING = true;
                runOnUiThread(() -> {
                    String m = "Please select at least 1 video/audio file\n";
                    textview_output_messages_1.setText(m);
                });
            }
        });


        // ASK WRITE PERMISSION
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }


        FOLDER.SAVED_URI_LIST = loadSavedTreeUrisFromSharedPreference();
        Log.d("onCreated", "FOLDER.SAVED_URI_LIST.size() = " + FOLDER.SAVED_URI_LIST.size());
        for (int i=0; i<FOLDER.SAVED_URI_LIST.size(); i++) {
            Log.d("onCreated", "FOLDER.SAVED_URI_LIST.get(" + i + ") = " + FOLDER.SAVED_URI_LIST.get(i));
        }
        if (FOLDER.SAVED_URI_LIST.size() == 0) {
            requestTreeUriPermissions();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    Uri uri = Uri.parse("package:${BuildConfig.LIBRARY_PACKAGE_NAME}");
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri);
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                    startForRequestManageAppAllFileAccessPermissionActivity.launch(intent);
                } catch (Exception e) {
                    Log.e("Exception: ", Objects.requireNonNull(e.getMessage()));
                    e.printStackTrace();
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivity(intent);
                }
            }
        }


        // REMOVE BUTTONS IF PERMISSIONS HAVE ALREADY GRANTED BECAUSE THERE'S NO ANY WAY TO REVOKE PERMISSION PROGRAMMATICALLY
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            button_grant_storage_permission.setVisibility(View.GONE);
            textview_grant_storage_permission_notes.setVisibility(View.GONE);
        }
        else {
            button_grant_storage_permission.setVisibility(View.VISIBLE);
            textview_grant_storage_permission_notes.setVisibility(View.VISIBLE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                button_grant_manage_app_all_files_access_permission.setVisibility(View.GONE);
                textview_grant_manage_app_all_files_access_permission_notes.setVisibility(View.GONE);
            }
            else {
                button_grant_manage_app_all_files_access_permission.setVisibility(View.VISIBLE);
                textview_grant_manage_app_all_files_access_permission_notes.setVisibility(View.VISIBLE);
            }
        }
        FOLDER.SAVED_URI_LIST = loadSavedTreeUrisFromSharedPreference();
        Log.d("onCreated", "FOLDER.SAVED_URI_LIST.size() = " + FOLDER.SAVED_URI_LIST.size());
        for (int i=0; i<FOLDER.SAVED_URI_LIST.size(); i++) {
            Log.d("onCreated", "FOLDER.SAVED_URI_LIST.get(" + i + ") = " + FOLDER.SAVED_URI_LIST.get(i));
        }

        if (!isInternetAvailable()) {
            setText(textview_output_messages_1, "It seems that you're not connected to internet, this app won't work without internet connection.");
        }

        try {
            Class.forName("dalvik.system.CloseGuard")
                    .getMethod("setEnabled", boolean.class)
                    .invoke(null, true);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.v("onRequestPermissionsResult","Permission: " + permissions[0] + " was "+ grantResults[0]);

                button_grant_storage_permission.setVisibility(View.GONE);
                textview_grant_storage_permission_notes.setVisibility(View.GONE);
                setText(textview_output_messages_1, "Storage permission is granted\n");

                FOLDER.SAVED_URI_LIST = loadSavedTreeUrisFromSharedPreference();
                Log.d("onRequestPermissionsResult", "FOLDER.SAVED_URI_LIST.size() = " + FOLDER.SAVED_URI_LIST.size());
                if (FOLDER.SAVED_URI_LIST.size() > 0) {
                    appendText(textview_output_messages_1, "Persisted tree uri permission is granted for folders :\n");
                    for (int i=0; i<FOLDER.SAVED_URI_LIST.size(); i++) {
                        appendText(textview_output_messages_1, FOLDER.SAVED_URI_LIST.get(i).toString() + "\n");
                        Log.d("onRequestPermissionsResult", "FOLDER.SAVED_URI_LIST.get(" + i + ") = " + FOLDER.SAVED_URI_LIST.get(i));
                    }
                    if (FILE.PATH_LIST != null && FILE.PATH_LIST.size()>0) {
                        if (isTreeUriPermissionGrantedForDirPathOfFilePath(FILE.PATH_LIST.get(0))) {
                            setText(textview_output_messages_1, "Persisted tree uri permission is granted for :\n" + new File(FILE.PATH_LIST.get(0)).getParent() + "\n");
                            appendText(textview_output_messages_1, "All subtitle files will be saved into :\n" + new File(FILE.PATH_LIST.get(0)).getParent() + "\n");
                        }
                        else {
                            setText(textview_output_messages_1, "Persisted tree uri permission request is not granted for " + new File(FILE.PATH_LIST.get(0)).getParent() + "\n");
                            appendText(textview_output_messages_1, "All subtitle files will be saved into :\n/storage/emulated/0/" + DIRECTORY_DOCUMENTS + File.separator + getPackageName() + File.separator);
                        }
                    }
                    else {
                        appendText(textview_output_messages_1, "All subtitle files will be saved into your selected folder.");
                    }
                }
                else {
                    appendText(textview_output_messages_1, "Persisted tree uri permission is not granted for any folders\n");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (Environment.isExternalStorageManager()) {
                            button_grant_manage_app_all_files_access_permission.setVisibility(View.GONE);
                            textview_grant_manage_app_all_files_access_permission_notes.setVisibility(View.GONE);
                            appendText(textview_output_messages_1, "Manage app all files access permission is granted.\n");
                            appendText(textview_output_messages_1, "All subtitle files will be saved into :\n/storage/emulated/0/" + DIRECTORY_DOCUMENTS + "/com.android.autosrt/");
                        }
                        else {
                            button_grant_manage_app_all_files_access_permission.setVisibility(View.VISIBLE);
                            textview_grant_manage_app_all_files_access_permission_notes.setVisibility(View.VISIBLE);
                            appendText(textview_output_messages_1, "Manage app all files access permission is not granted.\n");
                            appendText(textview_output_messages_1, "All subtitle files will always be saved as new files into :\n/storage/emulated/0/" + DIRECTORY_DOCUMENTS + "/com.android.autosrt/");
                        }
                    }
                    else {
                        button_grant_manage_app_all_files_access_permission.setVisibility(View.GONE);
                        textview_grant_manage_app_all_files_access_permission_notes.setVisibility(View.GONE);
                        appendText(textview_output_messages_1, "All subtitle files will be saved into :\n/storage/emulated/0/" + DIRECTORY_DOCUMENTS + "/com.android.autosrt/");
                    }
                }
            }
            else {
                button_grant_storage_permission.setVisibility(View.VISIBLE);
                textview_grant_storage_permission_notes.setVisibility(View.VISIBLE);
                setText(textview_output_messages_1, "Storage permission is not granted, this app won't work.");
            }
            //Toast.makeText(MainActivity.this, m1 + m2 + m3, Toast.LENGTH_SHORT).show();
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


    ActivityResultLauncher<Intent> startForRequestManageAppAllFileAccessPermissionActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (Environment.isExternalStorageManager()) {
                            button_grant_manage_app_all_files_access_permission.setVisibility(View.GONE);
                            textview_grant_manage_app_all_files_access_permission_notes.setVisibility(View.GONE);
                            setText(textview_output_messages_1, "Manage app all files access permission is granted.\n");

                            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                button_grant_storage_permission.setVisibility(View.GONE);
                                textview_grant_storage_permission_notes.setVisibility(View.GONE);
                                appendText(textview_output_messages_1, "Storage permission is granted.\n");

                                FOLDER.SAVED_URI_LIST = loadSavedTreeUrisFromSharedPreference();
                                Log.d("startForRequestManageAppAllFileAccessPermissionActivity", "FOLDER.SAVED_URI_LIST.size() = " + FOLDER.SAVED_URI_LIST.size());
                                if (FOLDER.SAVED_URI_LIST.size() > 0) {
                                    appendText(textview_output_messages_1, "Persisted tree uri permission is granted for folders :\n");
                                    for (int i=0; i<FOLDER.SAVED_URI_LIST.size(); i++) {
                                        appendText(textview_output_messages_1, FOLDER.SAVED_URI_LIST.get(i).toString() + "\n");
                                        Log.d("startForRequestManageAppAllFileAccessPermissionActivity", "FOLDER.SAVED_URI_LIST.get(" + i + ") = " + FOLDER.SAVED_URI_LIST.get(i));
                                    }
                                    if (FILE.PATH_LIST.size()>0) {
                                        if (isTreeUriPermissionGrantedForDirPathOfFilePath(FILE.PATH_LIST.get(0))) {
                                            setText(textview_output_messages_1, "Persisted tree uri permission is granted for :\n" + new File(FILE.PATH_LIST.get(0)).getParent() + "\n");
                                            appendText(textview_output_messages_1, "All subtitle files will be saved into :\n" + new File(FILE.PATH_LIST.get(0)).getParent() + "\n");
                                        }
                                        else {
                                            setText(textview_output_messages_1, "Persisted tree uri permission request is not granted for " + new File(FILE.PATH_LIST.get(0)).getParent() + "\n");
                                            appendText(textview_output_messages_1, "All subtitle files will always be saved as new files into :\n/storage/emulated/0/" + DIRECTORY_DOCUMENTS + File.separator + getPackageName() + File.separator);
                                        }
                                    }
                                    else {
                                        appendText(textview_output_messages_1, "All subtitle files will be saved into your selected folder.");
                                    }

                                }
                                else {
                                    appendText(textview_output_messages_1, "Persisted tree uri permission is not granted for any folder");
                                    appendText(textview_output_messages_1, "All subtitle files will be saved into :\n/storage/emulated/0/" + DIRECTORY_DOCUMENTS + File.separator + getPackageName() + File.separator);
                                }
                            }
                            else {
                                button_grant_storage_permission.setVisibility(View.VISIBLE);
                                textview_grant_storage_permission_notes.setVisibility(View.VISIBLE);
                                setText(textview_output_messages_1, "Storage permission is not granted, this app won't work.");
                            }
                        }
                        else {
                            button_grant_manage_app_all_files_access_permission.setVisibility(View.VISIBLE);
                            textview_grant_manage_app_all_files_access_permission_notes.setVisibility(View.VISIBLE);
                            setText(textview_output_messages_1, "Manage all files permission is not granted.\n");

                            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                button_grant_storage_permission.setVisibility(View.GONE);
                                textview_grant_storage_permission_notes.setVisibility(View.GONE);
                                appendText(textview_output_messages_1, "Storage permission is granted.\n");

                                FOLDER.SAVED_URI_LIST = loadSavedTreeUrisFromSharedPreference();
                                Log.d("startForRequestManageAppAllFileAccessPermissionActivity", "FOLDER.SAVED_URI_LIST.size() = " + FOLDER.SAVED_URI_LIST.size());
                                if (FOLDER.SAVED_URI_LIST.size() > 0) {
                                    appendText(textview_output_messages_1, "Persisted tree uri permission is granted for folders :\n");
                                    for (int i=0; i<FOLDER.SAVED_URI_LIST.size(); i++) {
                                        appendText(textview_output_messages_1, FOLDER.SAVED_URI_LIST.get(i).toString() + "\n");
                                        Log.d("startForRequestManageAppAllFileAccessPermissionActivity", "FOLDER.SAVED_URI_LIST.get(" + i + ") = " + FOLDER.SAVED_URI_LIST.get(i));
                                    }
                                    if (FILE.PATH_LIST != null && FILE.PATH_LIST.size()>0) {
                                        if (isTreeUriPermissionGrantedForDirPathOfFilePath(FILE.PATH_LIST.get(0))) {
                                            setText(textview_output_messages_1, "Persisted tree uri permission is granted for :\n" + new File(FILE.PATH_LIST.get(0)).getParent() + "\n");
                                            appendText(textview_output_messages_1, "All subtitle files will be saved into :\n" + new File(FILE.PATH_LIST.get(0)).getParent() + "\n");
                                        }
                                        else {
                                            setText(textview_output_messages_1, "Persisted tree uri permission request is not granted for " + new File(FILE.PATH_LIST.get(0)).getParent() + "\n");
                                            appendText(textview_output_messages_1, "All subtitle files will always be saved as new files into :\n/storage/emulated/0/" + DIRECTORY_DOCUMENTS + File.separator + getPackageName() + File.separator);
                                        }
                                    }
                                    else {
                                        appendText(textview_output_messages_1, "All subtitle files will be saved into your selected folder.");
                                    }
                                }
                                else {
                                    appendText(textview_output_messages_1, "Persisted tree uri permission is not granted for any folder");
                                    appendText(textview_output_messages_1, "All subtitle files will always be saved as new files into :\n/storage/emulated/0/" + DIRECTORY_DOCUMENTS + "/com.android.autosrt/");
                                }
                            }
                            else {
                                button_grant_storage_permission.setVisibility(View.VISIBLE);
                                textview_grant_storage_permission_notes.setVisibility(View.VISIBLE);
                                setText(textview_output_messages_1, "Storage permission is not granted, this app won't work");
                            }
                        }
                    }
                }
            });


    ActivityResultLauncher<Intent> startForRequestPersistedTreeUriPermissionActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Uri treeUri;
                        Intent intent = result.getData();
                        if (intent != null) {
                            treeUri = intent.getData();

                            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                button_grant_storage_permission.setVisibility(View.GONE);
                                textview_grant_storage_permission_notes.setVisibility(View.GONE);
                                setText(textview_output_messages_1, "Storage permission is granted.\n");

                                appendText(textview_output_messages_1, "Persisted tree uri permission is granted for :\n" + TreeUri2Path(treeUri) + "\n");
                                DocumentFile dfSelectedDir = DocumentFile.fromTreeUri(MainActivity.this, treeUri);
                                DocumentFile dfFile;
                                if (dfSelectedDir != null) {
                                    dfFile = dfSelectedDir.createFile("*/*", "test.txt");
                                    if (dfFile != null && dfFile.canWrite()) {
                                        Uri uriFile = dfFile.getUri();
                                        Log.d("startForRequestPersistedTreeUriPermissionActivity", "uriFile = " + uriFile);
                                        try {
                                            testWrite(uriFile);
                                            if (dfFile.exists() && dfFile.delete()) {
                                                appendText(textview_output_messages_1, "Write test succeed\n");
                                                appendText(textview_output_messages_1, "All subtitle files will be saved into :\n" + TreeUri2Path(treeUri));
                                            }
                                        } catch (FileNotFoundException e) {
                                            throw new RuntimeException(e);
                                        }
                                    } else {
                                        Log.d("startForRequestPersistedTreeUriPermissionActivity", "File is not exist or cannot write dfFile");
                                        setText(textview_output_messages_1, "Write test error!");
                                    }
                                }

                                FOLDER.SAVED_URI_LIST = loadSavedTreeUrisFromSharedPreference();
                                boolean alreadySaved = false;
                                for (int i = 0; i < FOLDER.SAVED_URI_LIST.size(); i++) {
                                    Log.d("startForRequestPersistedTreeUriPermissionActivity", "FOLDER.SAVED_URI_LIST.get(i) = " + FOLDER.SAVED_URI_LIST.get(i));
                                    if (FOLDER.SAVED_URI_LIST.get(i) == treeUri) {
                                        alreadySaved = true;
                                        Log.d("startForRequestPersistedTreeUriPermissionActivity", "alreadySaved = true");
                                    }
                                }
                                if (!alreadySaved) {
                                    int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                                    getContentResolver().takePersistableUriPermission(treeUri, takeFlags);
                                    FOLDER.SAVED_URI_LIST.add(treeUri);
                                    Log.d("startForRequestPersistedTreeUriPermissionActivity", "alreadySaved = false -> saveTreeUrisToSharedPreference");
                                    saveTreeUrisToSharedPreference(FOLDER.SAVED_URI_LIST);
                                }
                            }
                            else {
                                button_grant_storage_permission.setVisibility(View.VISIBLE);
                                textview_grant_storage_permission_notes.setVisibility(View.VISIBLE);
                                setText(textview_output_messages_1, "Storage permission is not granted, this app won't work");
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                            }
                        }
                    }
                    else {
                        Log.d("startForRequestPersistedTreeUriPermissionActivity", "result.getResultCode() != Activity.RESULT_OK");
                    }
                }
            });


    ActivityResultLauncher<Intent> startForBrowseFileActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        ClipData cd;
                        FILE.URI_LIST.clear();
                        FILE.PATH_LIST.clear();
                        FILE.DISPLAY_NAME_LIST.clear();
                        FOLDER.PATH = null;
                        FOLDER.URI = null;

                        // USER SELECT MULTIPLE FILES
                        if (intent != null && intent.getClipData() != null) {
                            Log.d("startForBrowseFileActivity", "intent != null && intent.getClipData() != null");
                            cd = intent.getClipData();

                            for (int i=0; i<cd.getItemCount(); i++) {
                                Uri fileUri = cd.getItemAt(i).getUri();
                                FILE.URI_LIST.add(fileUri);
                                String filePath = Uri2Path(getApplicationContext(), fileUri);
                                FILE.PATH_LIST.add(filePath);
                                String fileDisplayName = queryName(getApplicationContext(), fileUri);
                                //String fileDisplayName = FilenameUtils.getName(tmpSubtitleFilePath);
                                FILE.DISPLAY_NAME_LIST.add(fileDisplayName);
                                FOLDER.PATH = new File(FILE.PATH_LIST.get(i)).getParent();

                                boolean alreadySaved = isTreeUriPermissionGrantedForDirPathOfFilePath(FILE.PATH_LIST.get(i));
                                if (!alreadySaved) {
                                    Log.d("startForBrowseFileActivity", "alreadySaved = false -> requestTreeUriPermissions()");
                                    button_grant_persisted_tree_uri_permission.setVisibility(View.VISIBLE);
                                    setText(textview_output_messages_1, "Folder " + FOLDER.PATH + " has not been granted yet for persisted tree uri permission.\n");
                                    if (i==0) requestTreeUriPermissions();
                                }
                                else {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                        if (Environment.isExternalStorageManager()) {
                                            button_grant_manage_app_all_files_access_permission.setVisibility(View.GONE);
                                            textview_grant_manage_app_all_files_access_permission_notes.setVisibility(View.GONE);
                                            appendText(textview_output_messages_1, "Manage app all files access permission is granted.\n");

                                            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                                button_grant_storage_permission.setVisibility(View.GONE);
                                                textview_grant_storage_permission_notes.setVisibility(View.GONE);
                                                setText(textview_output_messages_1, "Storage permission is granted.\n");

                                                if (isTreeUriPermissionGrantedForDirPathOfFilePath(FILE.PATH_LIST.get(0))) {
                                                    setText(textview_output_messages_1, "Persisted tree uri permission is granted for :\n" + new File(FILE.PATH_LIST.get(0)).getParent() + "\n");
                                                    appendText(textview_output_messages_1, "All subtitle files will be saved into :\n" + new File(FILE.PATH_LIST.get(0)).getParent() + "\n");
                                                } else {
                                                    setText(textview_output_messages_1, "Persisted tree uri permission request has not been granted yet for " + new File(FILE.PATH_LIST.get(0)).getParent() + "\n");
                                                    appendText(textview_output_messages_1, "All subtitle files will always be saved as new files into :\n/storage/emulated/0/" + DIRECTORY_DOCUMENTS + File.separator + getPackageName() + File.separator);
                                                }
                                            } else {
                                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                                                requestTreeUriPermissions();
                                                setText(textview_output_messages_1, "Storage permission is not granted, this app won't work");
                                            }
                                        }
                                        else {
                                            button_grant_manage_app_all_files_access_permission.setVisibility(View.VISIBLE);
                                            textview_grant_manage_app_all_files_access_permission_notes.setVisibility(View.VISIBLE);
                                            appendText(textview_output_messages_1, "Manage app all files access permission is not granted.\n");

                                            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                                button_grant_storage_permission.setVisibility(View.GONE);
                                                textview_grant_storage_permission_notes.setVisibility(View.GONE);
                                                setText(textview_output_messages_1, "Storage permission is granted.\n");

                                                if (isTreeUriPermissionGrantedForDirPathOfFilePath(FILE.PATH_LIST.get(0))) {
                                                    setText(textview_output_messages_1, "Persisted tree uri permission is granted for :\n" + new File(FILE.PATH_LIST.get(0)).getParent() + "\n");
                                                    appendText(textview_output_messages_1, "All subtitle files will be saved into :\n" + new File(FILE.PATH_LIST.get(0)).getParent() + "\n");
                                                } else {
                                                    setText(textview_output_messages_1, "Persisted tree uri permission request has not been granted yet for " + new File(FILE.PATH_LIST.get(0)).getParent() + "\n");
                                                    appendText(textview_output_messages_1, "All subtitle files will always be saved as new files into :\n/storage/emulated/0/" + DIRECTORY_DOCUMENTS + "/com.android.autosrt/");
                                                }
                                            } else {
                                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                                                if (i==0) requestTreeUriPermissions();
                                                setText(textview_output_messages_1, "Storage permission is not granted, this app won't work");
                                            }
                                        }
                                    }
                                    else {
                                        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                            button_grant_storage_permission.setVisibility(View.GONE);
                                            textview_grant_storage_permission_notes.setVisibility(View.GONE);
                                            setText(textview_output_messages_1, "Storage permission is granted.\n");
                                            if (isTreeUriPermissionGrantedForDirPathOfFilePath(FILE.PATH_LIST.get(0))) {
                                                setText(textview_output_messages_1, "Persisted tree uri permission is granted for :\n" + new File(FILE.PATH_LIST.get(0)).getParent() + "\n");
                                                appendText(textview_output_messages_1, "All subtitle files will be saved into :\n" + new File(FILE.PATH_LIST.get(0)).getParent() + "\n");
                                            } else {
                                                setText(textview_output_messages_1, "Persisted tree uri permission request has not been granted yet for " + new File(FILE.PATH_LIST.get(0)).getParent() + "\n");
                                                appendText(textview_output_messages_1, "All subtitle files will always be saved as new files into :\n/storage/emulated/0/" + DIRECTORY_DOCUMENTS + File.separator + getPackageName() + File.separator);
                                            }
                                        } else {
                                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                                            if (i==0) requestTreeUriPermissions();
                                            setText(textview_output_messages_1, "Storage permission is not granted, this app won't work");
                                        }
                                    }
                                }
                            }

                            runOnUiThread(() -> {
                                textview_filepath.setText("");
                                for (int i = 0; i < FILE.URI_LIST.size(); i++) {
                                    String t2 = FILE.PATH_LIST.get(i);
                                    textview_filepath.append(t2 + "\n");
                                }
                            });

                        }

                        // USER SELECTS ONLY 1 SINGLE FILE
                        if (intent !=null && intent.getClipData() == null) {
                            Log.d("startForBrowseFileActivity", "intent !=null && intent.getClipData() == null");
                            Uri fileUri = intent.getData();
                            FILE.URI_LIST.add(fileUri);
                            String selectedFilePath = Uri2Path(getApplicationContext(), fileUri);
                            FILE.PATH_LIST.add(selectedFilePath);
                            String fileDisplayName = queryName(getApplicationContext(), fileUri);
                            FILE.DISPLAY_NAME_LIST.add(fileDisplayName);
                            FOLDER.PATH = new File(selectedFilePath).getParent();

                            boolean alreadySaved = isTreeUriPermissionGrantedForDirPathOfFilePath(selectedFilePath);
                            if (!alreadySaved) {
                                Log.d("startForBrowseFileActivity", "alreadySaved = false -> requestTreeUriPermissions()");
                                requestTreeUriPermissions();
                            }

                            else {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    if (Environment.isExternalStorageManager()) {
                                        button_grant_manage_app_all_files_access_permission.setVisibility(View.GONE);
                                        textview_grant_manage_app_all_files_access_permission_notes.setVisibility(View.GONE);
                                        appendText(textview_output_messages_1, "Manage app all files access permission is granted.\n");
                                        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                            if (isTreeUriPermissionGrantedForDirPathOfFilePath(FILE.PATH_LIST.get(0))) {
                                                setText(textview_output_messages_1, "Persisted tree uri permission is granted for :\n" + new File(FILE.PATH_LIST.get(0)).getParent() + "\n");
                                                appendText(textview_output_messages_1, "All subtitle files will be saved into :\n" + new File(FILE.PATH_LIST.get(0)).getParent() + "\n");
                                            } else {
                                                setText(textview_output_messages_1, "Persisted tree uri permission request is not granted for " + new File(FILE.PATH_LIST.get(0)).getParent() + "\n");
                                                appendText(textview_output_messages_1, "All subtitle files will always be saved as new files into :\n/storage/emulated/0/" + DIRECTORY_DOCUMENTS + File.separator + getPackageName() + File.separator);
                                            }
                                        }
                                        else {
                                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                                            requestTreeUriPermissions();
                                            setText(textview_output_messages_1, "Storage permission is not granted, this app won't work");
                                        }
                                    }
                                    else {
                                        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                            if (isTreeUriPermissionGrantedForDirPathOfFilePath(FILE.PATH_LIST.get(0))) {
                                                setText(textview_output_messages_1, "Persisted tree uri permission is granted for :\n" + new File(FILE.PATH_LIST.get(0)).getParent() + "\n");
                                                appendText(textview_output_messages_1, "All subtitle files will always be saved as new files into " + new File(FILE.PATH_LIST.get(0)).getParent() + "\n");
                                            } else {
                                                setText(textview_output_messages_1, "Persisted tree uri permission request is not granted for " + new File(FILE.PATH_LIST.get(0)).getParent() + "\n");
                                                appendText(textview_output_messages_1, "All subtitle files will always be saved as new files into :\n/storage/emulated/0/" + DIRECTORY_DOCUMENTS + File.separator + getPackageName() + File.separator);
                                            }
                                        }
                                        else {
                                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                                            requestTreeUriPermissions();
                                            setText(textview_output_messages_1, "Storage permission is not granted, this app won't work");
                                        }
                                    }
                                }
                                else {
                                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                        if (isTreeUriPermissionGrantedForDirPathOfFilePath(FILE.PATH_LIST.get(0))) {
                                            setText(textview_output_messages_1, "Persisted tree uri permission is granted for :\n" + new File(FILE.PATH_LIST.get(0)).getParent() + "\n");
                                            appendText(textview_output_messages_1, "All subtitle files will be saved into :\n" + new File(FILE.PATH_LIST.get(0)).getParent() + "\n");
                                        } else {
                                            setText(textview_output_messages_1, "Persisted tree uri permission request is not granted for " + new File(FILE.PATH_LIST.get(0)).getParent() + "\n");
                                            appendText(textview_output_messages_1, "All subtitle files will be saved into :\n" + Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS).toString() + ".");
                                        }
                                    }
                                    else {
                                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                                        requestTreeUriPermissions();
                                        setText(textview_output_messages_1, "Storage permission is not granted, this app won't work");
                                    }
                                }
                            }

                            runOnUiThread(() -> {
                                textview_filepath.setText("");
                                for (int i = 0; i < FILE.URI_LIST.size(); i++) {
                                    String t2 = FILE.PATH_LIST.get(i);
                                    textview_filepath.append(t2 + "\n");
                                }
                            });
                        }
                        else if (intent == null) {
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


    public static Uri getFolderUri(String folderPath) {
        File folder = new File(folderPath);
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            uri = DocumentsContract.buildDocumentUri(
                    "com.android.externalstorage.documents",
                    folder.getAbsolutePath().substring(1));
        } else {
            uri = Uri.fromFile(folder);
        }
        return uri;
    }


    private String TreeUri2Path(Uri uri) {
        if (uri == null) {
            return null;
        }
        String docId = DocumentsContract.getTreeDocumentId(uri);
        Log.d("TreeUri2Path", "docId = " + docId);
        String[] split = docId.split(":");
        Log.d("TreeUri2Path", "split = " + Arrays.toString(split));
        String fullPath = getPathFromExtSD(split);
        if (!fullPath.equals("")) {
            Log.d("TreeUri2Path", "fullPath = " + fullPath);
            return fullPath;
        } else {
            return null;
        }
    }


    private String Uri2Path(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }

        if(ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            Log.d("Uri2Path", "uri.getPath() = " + uri.getPath());
            return uri.getPath();
        }

        else if(ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            String authority = uri.getAuthority();
            Log.d("Uri2Path", "authority = " + authority);
            String idStr = "";

            if(authority.startsWith("com.android.externalstorage")) {
                String docId = DocumentsContract.getDocumentId(uri);
                String[] split = docId.split(":");
                String fullPath = getPathFromExtSD(split);
                if (!fullPath.equals("")) {
                    Log.d("Uri2Path", "fullPath = " + fullPath);
                    return fullPath;
                } else {
                    return null;
                }
            }

            else {
                if(authority.equals("media")) {
                    idStr = uri.toString().substring(uri.toString().lastIndexOf('/') + 1);
                    Log.d("Uri2Path", "media idStr = " + idStr);
                }
                else if(authority.startsWith("com.android.providers")) {
                    idStr = DocumentsContract.getDocumentId(uri).split(":")[1];
                    Log.d("Uri2Path", "providers idStr = " + idStr);
                }

                ContentResolver contentResolver = context.getContentResolver();
                Cursor cursor = contentResolver.query(MediaStore.Files.getContentUri("external"),
                        new String[] {MediaStore.Files.FileColumns.DATA},
                        "_id=?",
                        new String[]{idStr}, null);
                if (cursor != null && cursor.getCount()>0 && cursor.moveToFirst()) {
                    cursor.moveToFirst();
                    try {
                        int idx = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                        Log.d("Uri2Path", "cursor.getString(idx) = " + cursor.getString(idx));
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
        String fullPath = null;

        if ("primary".equalsIgnoreCase(type)) {
            Log.d("getPathFromExtSD", "PRIMARY");
            Log.d("getPathFromExtSD", "type = " + type);
            if (new File(Environment.getExternalStorageDirectory() + relativePath).exists()) {
                fullPath = Environment.getExternalStorageDirectory() + relativePath;
            }
        }
        // CHECK SECONDARY STORAGE
        else {
            if (new File("/storage/" + type + relativePath).exists()) {
                fullPath = "/storage/" + type + relativePath;
            }
        }
        Log.d("getPathFromExtSD", "fullPath = " + fullPath);
        return fullPath;
    }


    private static boolean fileExists(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }


    private void requestTreeUriPermissions() {
        // Choose a directory using the system's file picker.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            new AlertDialog.Builder(this)
                    .setMessage("Please select folder of your audio/video files so this app can write subtitle files on same folder")
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        setText(textview_output_messages_1, "Persisted tree uri permission request is canceled.\n");

                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                            button_grant_storage_permission.setVisibility(View.GONE);
                            textview_grant_storage_permission_notes.setVisibility(View.GONE);
                            setText(textview_output_messages_1, "Storage permission is granted.\n");

                            FOLDER.SAVED_URI_LIST = loadSavedTreeUrisFromSharedPreference();
                            Log.d("requestTreeUriPermissions", "FOLDER.SAVED_URI_LIST.size() = " + FOLDER.SAVED_URI_LIST.size());
                            if (FOLDER.SAVED_URI_LIST.size() > 0) {
                                appendText(textview_output_messages_1, "Persisted tree uri permission is granted for folders :\n");
                                for (int i=0; i<FOLDER.SAVED_URI_LIST.size(); i++) {
                                    appendText(textview_output_messages_1, FOLDER.SAVED_URI_LIST.get(i).toString() + "\n");
                                    Log.d("requestTreeUriPermissions", "FOLDER.SAVED_URI_LIST.get(" + i + ") = " + FOLDER.SAVED_URI_LIST.get(i));
                                }
                                if (FILE.PATH_LIST != null && FILE.PATH_LIST.size()>0) {
                                    if (isTreeUriPermissionGrantedForDirPathOfFilePath(FILE.PATH_LIST.get(0))) {
                                        setText(textview_output_messages_1, "Persisted tree uri permission is granted for :\n" + new File(FILE.PATH_LIST.get(0)).getParent() + "\n");
                                        appendText(textview_output_messages_1, "All subtitle files will be saved into :\n" + new File(FILE.PATH_LIST.get(0)).getParent() + "\n");
                                    }
                                    else {
                                        setText(textview_output_messages_1, "Persisted tree uri permission request is not granted for " + new File(FILE.PATH_LIST.get(0)).getParent() + "\n");
                                        appendText(textview_output_messages_1, "All subtitle files will always be saved as new files into :\n/storage/emulated/0/" + DIRECTORY_DOCUMENTS + File.separator + getPackageName() + File.separator);
                                    }
                                }
                                else {
                                    appendText(textview_output_messages_1, "All subtitle files will be saved into your selected folder.");
                                }
                            }
                            else {
                                appendText(textview_output_messages_1, "Persisted tree uri permission is not granted for any folders\n");
                                if (Environment.isExternalStorageManager()) {
                                    button_grant_manage_app_all_files_access_permission.setVisibility(View.GONE);
                                    textview_grant_manage_app_all_files_access_permission_notes.setVisibility(View.GONE);
                                    appendText(textview_output_messages_1, "Manage app all files access permission is granted.\n");
                                    appendText(textview_output_messages_1, "All subtitle files will always be saved as new files into :\n/storage/emulated/0/" + DIRECTORY_DOCUMENTS + File.separator + getPackageName() + File.separator);
                                }
                                else {
                                    button_grant_manage_app_all_files_access_permission.setVisibility(View.VISIBLE);
                                    textview_grant_manage_app_all_files_access_permission_notes.setVisibility(View.VISIBLE);
                                    appendText(textview_output_messages_1, "Manage app all files access permission is not granted.\n");
                                    appendText(textview_output_messages_1, "All subtitle files will always be saved as new files into :\n/storage/emulated/0/" + DIRECTORY_DOCUMENTS + "/com.android.autosrt/");
                                }
                            }
                        }

                        else {
                            button_grant_storage_permission.setVisibility(View.VISIBLE);
                            textview_grant_storage_permission_notes.setVisibility(View.VISIBLE);
                            setText(textview_output_messages_1, "Storage permission is not granted, this app won't work");
                        }
                    })
                    .setPositiveButton("Ok", (dialog, which) -> {
                        StorageManager sm = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
                        Intent intent = sm.getPrimaryStorageVolume().createOpenDocumentTreeIntent();
                        String startDir = "Documents";
                        Uri uri;
                        if (intent != null) {
                            uri = intent.getParcelableExtra("android.provider.extra.INITIAL_URI");
                            String scheme;
                            if (uri != null) {
                                scheme = uri.toString().replace("/root/", "/document/");
                                scheme += "%3A" + startDir;
                                uri = Uri.parse(scheme);
                                Uri rootUri = DocumentsContract.buildDocumentUri(AUTHORITY, uri.toString());
                                sm.getPrimaryStorageVolume().createOpenDocumentTreeIntent().putExtra(EXTRA_INITIAL_URI, rootUri);

                                // Optionally, specify a URI for the directory that should be opened in
                                // the system file picker when it loads.
                                Intent intent2 = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                                intent2.addFlags(
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                                                | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
                                intent2.putExtra(EXTRA_INITIAL_URI, rootUri);
                                //startActivity(intent2);
                                startForRequestPersistedTreeUriPermissionActivity.launch(intent2);
                            }
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
        else {
            new AlertDialog.Builder(this)
                    .setMessage("Please select folder of your audio/video files so this app can write subtitle files on same folder")
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        setText(textview_output_messages_1, "Persisted tree uri permission request is canceled.\n");

                        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                            button_grant_storage_permission.setVisibility(View.GONE);
                            textview_grant_storage_permission_notes.setVisibility(View.GONE);
                            setText(textview_output_messages_1, "Storage permission is granted.\n");

                            FOLDER.SAVED_URI_LIST = loadSavedTreeUrisFromSharedPreference();
                            Log.d("requestTreeUriPermissions", "FOLDER.SAVED_URI_LIST.size() = " + FOLDER.SAVED_URI_LIST.size());
                            if (FOLDER.SAVED_URI_LIST.size() > 0) {
                                appendText(textview_output_messages_1, "Persisted tree uri permission is granted for folders :\n");
                                for (int i=0; i<FOLDER.SAVED_URI_LIST.size(); i++) {
                                    appendText(textview_output_messages_1, FOLDER.SAVED_URI_LIST.get(i).toString() + "\n");
                                    Log.d("requestTreeUriPermissions", "FOLDER.SAVED_URI_LIST.get(" + i + ") = " + FOLDER.SAVED_URI_LIST.get(i));
                                }
                                if (FILE.PATH_LIST != null && FILE.PATH_LIST.size()>0) {
                                    if (isTreeUriPermissionGrantedForDirPathOfFilePath(FILE.PATH_LIST.get(0))) {
                                        setText(textview_output_messages_1, "Persisted tree uri permission is granted for :\n" + new File(FILE.PATH_LIST.get(0)).getParent() + "\n");
                                        appendText(textview_output_messages_1, "All subtitle files will be saved into :\n" + new File(FILE.PATH_LIST.get(0)).getParent() + "\n");
                                    }
                                    else {
                                        setText(textview_output_messages_1, "Persisted tree uri permission request is not granted for " + new File(FILE.PATH_LIST.get(0)).getParent() + "\n");
                                        appendText(textview_output_messages_1, "All subtitle files will always be saved as new files into :\n/storage/emulated/0/" + DIRECTORY_DOCUMENTS + File.separator + getPackageName() + File.separator);
                                    }
                                }
                                else {
                                    appendText(textview_output_messages_1, "All subtitle files will be saved into your selected folder.");
                                }
                            }
                            else {
                                appendText(textview_output_messages_1, "Persisted tree uri permission is not granted for any folders\n");
                                appendText(textview_output_messages_1, "All subtitle files will be saved into :\n/storage/emulated/0/" + DIRECTORY_DOCUMENTS + File.separator + getPackageName() + File.separator);
                            }
                        }

                        else {
                            setText(textview_output_messages_1, "Storage permission is not granted, this app won't work");
                        }
                    })
                    .setPositiveButton("Ok", (dialog, which) -> {
                        //Intent intent = sm.getPrimaryStorageVolume().createAccessIntent(DIRECTORY_DOCUMENTS);
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                        intent.addFlags(
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                        | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                                        | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);

                        //startActivity(intent);
                        startForRequestPersistedTreeUriPermissionActivity.launch(intent);
                    })
                    .setCancelable(false)
                    .show();
        }
    }


    @SuppressLint("Recycle")
    private void testWrite(Uri uri) throws FileNotFoundException {
        @SuppressLint("Recycle")
        ParcelFileDescriptor parcelFileDescriptor;
        parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "w");
        try (FileOutputStream fos = new FileOutputStream(parcelFileDescriptor.getFileDescriptor())) {
            long currentTimeMillis = System.currentTimeMillis();
            fos.write(("String written at " + currentTimeMillis + "\n").getBytes());
            //Log.d("testWrite", "Write test succeed");
        }
        catch (IOException e) {
            Log.e("IOException: ", e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    private InetAddress[] checkGoogleHost() {
        final InetAddress[] ipAddr = new InetAddress[1];
        Thread netThread = new Thread(() -> {
            try {
                ipAddr[0] = InetAddress.getByName("www.google.com");
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
            Log.d("isInternetAvailable", "ipAddr = " + ipAddr[0]);
        });
        netThread.start();
        return ipAddr;
    }


    private boolean isInternetAvailable() {
        try {
            InetAddress[] ipAddr = checkGoogleHost();
            return !Arrays.toString(ipAddr).equals("");
        } catch (Exception e) {
            return false;
        }
    }


    private void saveTreeUrisToSharedPreference(ArrayList<Uri> savedTreesUri) {
        SharedPreferences sp = getSharedPreferences("com.android.autosubtitle.prefs", 0);
        SharedPreferences.Editor mEdit1 = sp.edit();
        mEdit1.putInt("arrayListSize", savedTreesUri.size());
        for(int i=0;i<savedTreesUri.size();i++) {
            mEdit1.remove("arrayList_" + i);
            mEdit1.putString("arrayList_" + i, savedTreesUri.get(i).toString());
            Log.d("saveTreeUrisToSharedPreference", "arrayList_" + i + " = " + savedTreesUri.get(i).toString());
        }
        mEdit1.apply();
    }


    private ArrayList<Uri> loadSavedTreeUrisFromSharedPreference() {
        ArrayList<Uri> savedTreesUri = new ArrayList<>();
        SharedPreferences sp = getSharedPreferences("com.android.autosubtitle.prefs", 0);
        int size = sp.getInt("arrayListSize", 0);
        for(int i=0;i<size;i++) {
            Uri uri = Uri.parse(sp.getString("arrayList_" + i, null));
            savedTreesUri.add(uri);
        }
        return savedTreesUri;
    }


    private boolean isTreeUriPermissionGrantedForDirPathOfFilePath(String filePath) {
        String dirName = Objects.requireNonNull(new File(filePath).getParentFile()).getName();
        Uri dirUri = getFolderUri(dirName);

        FOLDER.SAVED_URI_LIST = loadSavedTreeUrisFromSharedPreference();
        if (FOLDER.SAVED_URI_LIST.size() > 0) {
            for (int j=0; j<FOLDER.SAVED_URI_LIST.size(); j++) {
                Uri savedTreeUri = Uri.parse(FOLDER.SAVED_URI_LIST.get(j).toString());

                Log.d("isTreeUriPermissionGrantedForFilePath", "savedTreeUri = " + savedTreeUri);
                Log.d("isTreeUriPermissionGrantedForFilePath", "savedTreeUri.getLastPathSegment() = " + savedTreeUri.getLastPathSegment());
                Log.d("isTreeUriPermissionGrantedForFilePath", "dirUri = " + dirUri);
                Log.d("isTreeUriPermissionGrantedForFilePath", "dirUri.getLastPathSegment() = " + dirUri.getLastPathSegment());

                if (savedTreeUri.getLastPathSegment().contains(dirUri.getLastPathSegment())) {
                    FOLDER.URI = savedTreeUri;
                    Log.d("isTreeUriPermissionGrantedForDirPathOfFilePath", "FOLDER.URI = " + FOLDER.URI);
                    Log.d("isTreeUriPermissionGrantedForDirPathOfFilePath", "alreadySaved = true");
                    return true;
                }
                else {
                    Log.d("isTreeUriPermissionGrantedForDirPathOfFilePath", "alreadySaved = false");
                }
            }
        }
        return false;
    }


    private void setText(final TextView tv, final String text){
        runOnUiThread(() -> tv.setText(text));
    }

    private void appendText(final TextView tv, final String text){
        runOnUiThread(() -> tv.append(text));
    }

}
