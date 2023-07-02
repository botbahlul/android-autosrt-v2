package com.android.autosrt;

import static android.os.Environment.DIRECTORY_DOCUMENTS;
import static android.os.Environment.getExternalStorageDirectory;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Objects;

public class TranscribeActivity extends AppCompatActivity {

    @SuppressLint("StaticFieldLeak")
    public static TextView textview_src;
    @SuppressLint("StaticFieldLeak")
    public static TextView textview_dst;
    @SuppressLint("StaticFieldLeak")
    public static TextView textview_filepath;
    @SuppressLint("StaticFieldLeak")
    public static TextView textview_current_file;

    TextView textview_progress;
    ProgressBar progressBar;
    TextView textview_percentage;
    TextView textview_time;

    @SuppressLint("StaticFieldLeak")
    public static TextView textview_output_messages_2;

    @SuppressLint("StaticFieldLeak")
    public static Button button_cancel;

    public static Python py;
    public static PyObject pyObjTmpSubtitleFilePath;
    public static Thread threadTranscriber;
    public static String cancelFile = null;
    public static String subtitleFile = null;

    int heightOfOutputMessages;
    int maxLinesOfOutputMessages;
    int maxChars = 0;
    String equals;

    long transcribeStartTime;
    long transcribeElapsedTime;
    String formattedElapsedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transcribe);
        textview_src = findViewById(R.id.textview_src);
        textview_dst = findViewById(R.id.textview_dst);
        textview_filepath = findViewById(R.id.textview_filepath);
        textview_current_file = findViewById(R.id.textview_current_file);
        textview_output_messages_2 = findViewById(R.id.textview_output_messages_2);
        button_cancel = findViewById(R.id.button_cancel);

        textview_progress = findViewById(R.id.textview_progress);
        progressBar = findViewById(R.id.progressBar);
        textview_percentage = findViewById(R.id.textview_percentage);
        textview_time = findViewById(R.id.textview_time);

        String vl = "Voice Language = " + LANGUAGE.SRC_LANGUAGE;
        runOnUiThread(() -> textview_src.setText(vl));

        if (MainActivity.checkbox_create_translation.isChecked()) {
            String tl = "Translation Language = " + LANGUAGE.DST_LANGUAGE;
            runOnUiThread(() -> textview_dst.setText(tl));
        }
        else {
            textview_dst.setVisibility(View.GONE);
        }

        for (int i=0; i< FILE.PATH_LIST.size(); i++) {
            //String fp = "File path [" + i + "] = " + FILE.PATH_LIST.get(i) + "\n";
            String fp = FILE.PATH_LIST.get(i) + "\n";
            runOnUiThread(() -> textview_filepath.append(fp));
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.actionbar_title);
        }

        textview_filepath.setTextIsSelectable(true);
        textview_output_messages_2.setTextIsSelectable(true);

        textview_filepath.setSelected(true);
        textview_output_messages_2.setSelected(true);

        textview_filepath.setMovementMethod(new ScrollingMovementMethod());
        textview_output_messages_2.setMovementMethod(new ScrollingMovementMethod());

        cancelFile = getApplicationContext().getExternalFilesDir(null) + File.separator + "cancel.txt";
        File f = new File(cancelFile);
        if (f.exists() && f.delete()) {
            Log.d("cancelFile", "deleted");
        }

        subtitleFile = null;
        if (threadTranscriber != null) {
            threadTranscriber.interrupt();
            try {
                if (pyObjTmpSubtitleFilePath != null) pyObjTmpSubtitleFilePath.close();
                threadTranscriber.join();
                threadTranscriber = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        if (TRANSCRIBE_STATUS.IS_TRANSCRIBING) {
            transcribe();
        }
        button_cancel.setOnClickListener(view -> showConfirmationDialogue());

        textview_current_file.setHint("");
        textview_progress.setHint("");
        textview_percentage.setHint("");
        textview_time.setHint("");
        hideProgressBar();

        adjustOutputMessagesHeight();

        textview_output_messages_2.post(() -> {
            equals = StringUtils.repeat('=', 80);
            maxChars = (calculateMaxCharsInTextView(equals, textview_output_messages_2.getWidth(), (int) textview_output_messages_2.getTextSize()));
            Log.d("onCreate", "textview_output_messages_2.getWidth() = " + textview_output_messages_2.getWidth());
            Log.d("onCreate", "textview_output_messages_2.getTextSize() = " + textview_output_messages_2.getTextSize());
            Log.d("onCreate", "maxChars = " + maxChars);
            equals = StringUtils.repeat('=', maxChars - 2);
        });

        try {
            Class.forName("dalvik.system.CloseGuard")
                    .getMethod("setEnabled", boolean.class)
                    .invoke(null, true);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void onBackPressed() {
        showConfirmationDialogue();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }


    private void transcribe() {
        setText(textview_output_messages_2, "");
        runOnUiThread(() -> {
            textview_output_messages_2.setGravity(Gravity.START);
            textview_output_messages_2.scrollTo(0,0);
        });

        if (threadTranscriber != null && threadTranscriber.isAlive()) threadTranscriber.interrupt();
        threadTranscriber = null;
        threadTranscriber = new Thread(() -> {
            if (Looper.myLooper() == null) {
                Looper.prepare();
            }

            if (FILE.URI_LIST != null) {
                if (TRANSCRIBE_STATUS.IS_TRANSCRIBING) {
                    Log.d("transcribe", "Running");

                    transcribeStartTime = System.currentTimeMillis();
                    Log.d("transcribe", "transcribeStartTime = " + transcribeStartTime);

                    try {
                        if (!Python.isStarted()) {
                            Python.start(new AndroidPlatform(TranscribeActivity.this));
                            py = Python.getInstance();
                        }

                        if (FILE.PATH_LIST == null) {
                            threadTranscriber.interrupt();
                            threadTranscriber = null;
                            setText(textview_output_messages_2, "");
                            transcribe();
                        }

                        SUBTITLE.SAVED_FILE = new File[FILE.URI_LIST.size()];

                        for (int i=0; i<FILE.URI_LIST.size(); i++) {
                            if (!TRANSCRIBE_STATUS.IS_TRANSCRIBING) return;

                            if (!Python.isStarted()) {
                                Python.start(new AndroidPlatform(TranscribeActivity.this));
                                py = Python.getInstance();
                            }

                            setText(textview_current_file, "Processing file : " + FILE.DISPLAY_NAME_LIST.get(i));
                            int finalI = i;
                            textview_output_messages_2.post(() -> {
                                appendText(textview_output_messages_2, equals + "\n");
                                appendText(textview_output_messages_2, "Processing file : " + FILE.DISPLAY_NAME_LIST.get(finalI) + "\n");
                                appendText(textview_output_messages_2, equals + "\n");
                             });
                            String tmpSubtitleFilePath;
                            String tmpTranslatedSubtitleFilePath;

                            pyObjTmpSubtitleFilePath = py.getModule("autosrt").callAttr(
                                    "transcribe",
                                    LANGUAGE.SRC_CODE,
                                    LANGUAGE.DST_CODE,
                                    FILE.PATH_LIST.get(i),
                                    FILE.DISPLAY_NAME_LIST.get(i),
                                    SUBTITLE.FORMAT,
                                    TranscribeActivity.this,
                                    textview_output_messages_2,
                                    textview_progress,
                                    progressBar,
                                    textview_percentage,
                                    textview_time
                            );

                            if (pyObjTmpSubtitleFilePath != null) {
                                tmpSubtitleFilePath = pyObjTmpSubtitleFilePath.toString();
                                SUBTITLE.TMP_FILE_PATH_LIST.add(tmpSubtitleFilePath);
                                tmpTranslatedSubtitleFilePath = StringUtils.substring(tmpSubtitleFilePath, 0, tmpSubtitleFilePath.length() - SUBTITLE.FORMAT.length() - 1) + ".translated." + SUBTITLE.FORMAT;
                                SUBTITLE.TMP_TRANSLATED_FILE_PATH_LIST.add(tmpTranslatedSubtitleFilePath);

                                if (new File(tmpSubtitleFilePath).exists() && new File(tmpSubtitleFilePath).length() > 1) {
                                    FOLDER.SAVED_URI_LIST = loadSavedTreeUrisFromSharedPreference();
                                    if (FOLDER.SAVED_URI_LIST.size() == 0) {

                                        Log.d("transcribe", "Saving subtitle file using saveSubtitleFileToDocumentsDir()");
                                        SUBTITLE.SAVED_FILE[i] = saveSubtitleFileToDocumentsDir(tmpSubtitleFilePath);
                                        Log.d("transcribe", "SUBTITLE.SAVED_FILE[" + i + "] = " + SUBTITLE.SAVED_FILE[i]);

                                        if (SUBTITLE.SAVED_FILE[i].exists() && SUBTITLE.SAVED_FILE[i].length() > 1) {
                                            Log.d("transcribe", SUBTITLE.SAVED_FILE[i] + " created");
                                            appendText(textview_output_messages_2, equals + "\n");
                                            appendText(textview_output_messages_2, "Saved subtitle files for " + FILE.DISPLAY_NAME_LIST.get(i) + " : \n");
                                            appendText(textview_output_messages_2, SUBTITLE.SAVED_FILE[i] + "\n");

                                            if (!Objects.equals(LANGUAGE.SRC_CODE, LANGUAGE.DST_CODE)) {
                                                String savedTranslatedSubtitleFilePath = StringUtils.replace(SUBTITLE.SAVED_FILE[i].toString(), ".srt", ".translated.srt");
                                                Log.d("transcribe", "savedTranslatedSubtitleFilePath = " + savedTranslatedSubtitleFilePath);
                                                if (new File(savedTranslatedSubtitleFilePath).exists() && new File(savedTranslatedSubtitleFilePath).length() > 1) {
                                                    appendText(textview_output_messages_2, savedTranslatedSubtitleFilePath + "\n");
                                                }
                                            }
                                            appendText(textview_output_messages_2, equals + "\n");
                                        }

                                    }
                                    else {
                                        Log.d("transcribe", "FOLDER.SAVED_URI_LIST.size() = " + FOLDER.SAVED_URI_LIST.size());
                                        Uri dirUri = getFolderUri(FOLDER.PATH);
                                        Log.d("transcribe", "dirUri = " + dirUri);

                                        int j=0;
                                        for (Uri savedTreeUri : FOLDER.SAVED_URI_LIST) {
                                            Log.d("transcribe", "savedTreeUri[" + j + "] = " + savedTreeUri);
                                            if (dirUri.getLastPathSegment().contains(savedTreeUri.getLastPathSegment())) {
                                                FOLDER.URI = savedTreeUri;
                                                Log.d("transcribe", "FOLDER.URI = " + FOLDER.URI);
                                            }
                                            j+=1;
                                        }

                                        boolean alreadySaved = isTreeUriPermissionGrantedForDirPathOfFilePath(FILE.PATH_LIST.get(i));
                                        if (alreadySaved) {

                                            Log.d("transcribe", "Saving subtitle file using saveSubtitleFileToSelectedDir()");
                                            Log.d("transcribe", "FOLDER.URI = " + FOLDER.URI);
                                            SUBTITLE.SAVED_FILE[i] = saveSubtitleFileToSelectedDir(tmpSubtitleFilePath, FOLDER.URI);

                                            if (SUBTITLE.SAVED_FILE[i].exists() && SUBTITLE.SAVED_FILE[i].length() > 1) {
                                                Log.d("transcribe", SUBTITLE.SAVED_FILE[i].toString() + " created");
                                                appendText(textview_output_messages_2, equals + "\n");
                                                appendText(textview_output_messages_2, "Saved subtitle files for " + FILE.DISPLAY_NAME_LIST.get(i) + " : \n");
                                                appendText(textview_output_messages_2, SUBTITLE.SAVED_FILE[i].toString() + "\n");

                                                if (!Objects.equals(LANGUAGE.SRC_CODE, LANGUAGE.DST_CODE)) {
                                                    String savedTranslatedSubtitleFilePath = StringUtils.replace(SUBTITLE.SAVED_FILE[i].toString(), ".srt", ".translated.srt");
                                                    Log.d("transcribe", "savedTranslatedSubtitleFilePath = " + savedTranslatedSubtitleFilePath);
                                                    if (new File(savedTranslatedSubtitleFilePath).exists() && new File(savedTranslatedSubtitleFilePath).length() > 1) {
                                                        appendText(textview_output_messages_2, savedTranslatedSubtitleFilePath + "\n");
                                                    }
                                                }
                                                appendText(textview_output_messages_2, equals + "\n");
                                            }

                                        }
                                        else {

                                            Log.d("transcribe", "Saving subtitle file using saveSubtitleFileToDocumentsDir()");
                                            SUBTITLE.SAVED_FILE[i] = saveSubtitleFileToDocumentsDir(tmpSubtitleFilePath);
                                            Log.d("transcribe", "SUBTITLE.SAVED_FILE[" + i + "] = " + SUBTITLE.SAVED_FILE[i]);

                                            if (new File(SUBTITLE.SAVED_FILE[i].toString()).exists() && new File(SUBTITLE.SAVED_FILE[i].toString()).length() > 1) {
                                                Log.d("transcribe", SUBTITLE.SAVED_FILE[i] + " created");
                                                appendText(textview_output_messages_2, equals + "\n");
                                                appendText(textview_output_messages_2, "Saved subtitle files for " + FILE.DISPLAY_NAME_LIST.get(i) + " : \n");
                                                appendText(textview_output_messages_2, SUBTITLE.SAVED_FILE[i] + "\n");

                                                if (!Objects.equals(LANGUAGE.SRC_CODE, LANGUAGE.DST_CODE)) {
                                                    String savedTranslatedSubtitleFilePath = StringUtils.replace(SUBTITLE.SAVED_FILE[i].toString(), ".srt", ".translated.srt");
                                                    Log.d("transcribe", "savedTranslatedSubtitleFilePath = " + savedTranslatedSubtitleFilePath);
                                                    if (new File(savedTranslatedSubtitleFilePath).exists() && new File(savedTranslatedSubtitleFilePath).length() > 1) {
                                                        appendText(textview_output_messages_2, savedTranslatedSubtitleFilePath + "\n");
                                                    }
                                                }
                                                appendText(textview_output_messages_2, equals + "\n");
                                            }

                                        }
                                    }
                                }
                            }
                        }
                        setText(textview_current_file, "");

                        if (TRANSCRIBE_STATUS.IS_TRANSCRIBING && FILE.URI_LIST != null) {
                            if (threadTranscriber != null) {
                                threadTranscriber.interrupt();
                                threadTranscriber = null;
                            }
                            TRANSCRIBE_STATUS.IS_TRANSCRIBING = false;
                            String t = "Done";
                            runOnUiThread(() -> button_cancel.setText(t));

                            Log.d("transcribe", "transcribeStartTime = " + transcribeStartTime);
                            Log.d("transcribe", "transcribeEndTime = " + System.currentTimeMillis());
                            transcribeElapsedTime = System.currentTimeMillis() - transcribeStartTime;
                            Log.d("transcribe", "transcribeElapsedTime = " + transcribeElapsedTime);
                            long totalSeconds = transcribeElapsedTime / 1000;
                            Log.d("transcribe", "totalSeconds = " + totalSeconds);
                            long hours = totalSeconds / 3600;
                            long minutes = (totalSeconds % 3600) / 60;
                            long seconds = totalSeconds % 60;
                            formattedElapsedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                            appendText(textview_output_messages_2, "Transcribe total time : " + formattedElapsedTime + "\n");
                            appendText(textview_output_messages_2, equals + "\n");

                        }

                    }
                    catch (Exception e) {
                        Log.e("Exception: ", Objects.requireNonNull(e.getMessage()));
                        e.printStackTrace();
                    }

                }
                else {
                    if (threadTranscriber != null) {
                        threadTranscriber.interrupt();
                        threadTranscriber = null;
                    }
                    TRANSCRIBE_STATUS.IS_TRANSCRIBING = false;
                }

            }
            else {
                if (threadTranscriber != null) {
                    threadTranscriber.interrupt();
                    threadTranscriber = null;
                }
                TRANSCRIBE_STATUS.IS_TRANSCRIBING = false;
                runOnUiThread(() -> {
                    String m = "Please select at least 1 video/audio file\n";
                    textview_output_messages_2.setText(m);
                });
            }

        });
        threadTranscriber.start();
    }


    private void showConfirmationDialogue() {
        if (TRANSCRIBE_STATUS.IS_TRANSCRIBING) {
            AlertDialog.Builder builder = new AlertDialog.Builder(TranscribeActivity.this);
            builder.setTitle("Confirm");
            builder.setMessage("Are you sure?");

            builder.setPositiveButton("YES", (dialog, which) -> runOnUiThread(() -> {
                File fc = new File(cancelFile);
                try {
                    FileWriter out = new FileWriter(fc);
                    out.write("");
                    Log.i("showConfirmationDialogue", "cancelFile created");
                    out.close();
                } catch (IOException e) {
                    Log.e("showConfirmationDialogue", e.getMessage());
                    e.printStackTrace();
                }

                if (fc.exists()) {
                    Log.i("showConfirmationDialogue", "cancelFile exists");
                }
                else {
                    Log.i("showConfirmationDialogue", "cancelFile is not exist");
                }

                if (SUBTITLE.TMP_FILE_PATH_LIST != null) {
                    for (int i=0; i<SUBTITLE.TMP_FILE_PATH_LIST.size(); i++) {
                        File sf = new File(SUBTITLE.TMP_FILE_PATH_LIST.get(i)).getAbsoluteFile();
                        if (sf.exists() && sf.delete()) {
                            Log.i("showConfirmationDialogue", new File(SUBTITLE.TMP_FILE_PATH_LIST.get(i)).getAbsoluteFile() + " deleted");
                        }
                    }
                }
                if (SUBTITLE.TMP_TRANSLATED_FILE_PATH_LIST != null) {
                    for (int i=0; i<SUBTITLE.TMP_TRANSLATED_FILE_PATH_LIST.size(); i++) {
                        File stf = new File(SUBTITLE.TMP_TRANSLATED_FILE_PATH_LIST.get(i)).getAbsoluteFile();
                        if (stf.exists() && stf.delete()) {
                            Log.i("showConfirmationDialogue", new File(SUBTITLE.TMP_TRANSLATED_FILE_PATH_LIST.get(i)).getAbsoluteFile() + " deleted");
                        }
                    }
                }

                if (threadTranscriber != null) {
                    threadTranscriber.interrupt();
                    threadTranscriber = null;
                }
                TRANSCRIBE_STATUS.IS_TRANSCRIBING = false;
                hideProgressBar();
                dialog.dismiss();
                setText(MainActivity.textview_output_messages_1, "Process has been canceled");
                finish();
            }));

            builder.setNegativeButton("NO", (dialog, which) -> {
                // Do nothing
                dialog.dismiss();
            });

            AlertDialog alert = builder.create();
            alert.show();
        }
        else {
            setText(textview_output_messages_2, "");
            runOnUiThread(() -> {
                textview_output_messages_2.setGravity(Gravity.START);
                textview_output_messages_2.scrollTo(0,0);
            });
            finish();
        }
    }


    /*private int convertToWav(String filePath, File tmpWavFile, int channels, int rate) {
        AtomicReference<Float> progress = new AtomicReference<>((float) 0);
        Uri fileURI = Uri.fromFile(new File(filePath));
        int videoLength = MediaPlayer.create(getApplicationContext(), fileURI).getDuration();

        Config.resetStatistics();
        Config.enableStatisticsCallback(newStatistics -> {
            progress.set(Float.parseFloat(String.valueOf(newStatistics.getTime())) / videoLength);
            int progressFinal = (int) (progress.get() * 100);
            Log.d(Config.TAG, "Video Length: " + progressFinal);
            Log.d(Config.TAG, String.format("frame: %d, time: %d", newStatistics.getVideoFrameNumber(), newStatistics.getTime()));
            Log.d(Config.TAG, String.format("Quality: %f, time: %f", newStatistics.getVideoQuality(), newStatistics.getVideoFps()));
            runOnUiThread(() -> pBar(100*progress.get(), 100, "Converting to temporary WAV file : "));
        });

        String command = " -y -i " + "\"" + filePath + "\"" + " -ac " + channels + " -ar " + rate + " " + "\"" + tmpWavFile + "\"";
        int returnCode = FFmpeg.execute(command);
        if (returnCode == RETURN_CODE_SUCCESS) {
            Log.i(Config.TAG, "Command execution completed successfully.");
        } else if (returnCode == RETURN_CODE_CANCEL) {
            Log.i(Config.TAG, "Async command execution cancelled by user.");
        } else {
            Log.i(Config.TAG, String.format("Command execution failed with rc=%d.", returnCode));
        }
        return Integer.parseInt(String.valueOf(tmpWavFile.length()));
    }*/


    /*@SuppressLint("SetTextI18n")
    public void pBar(float counter, float total, String prefix) {
        int bar_length = 10;
        int filled_up_Length = round(bar_length * counter/ total);
        int percentage = round(100 * counter/ total);
        String pounds = StringUtils.repeat('█', filled_up_Length);
        String equals = StringUtils.repeat('-', (bar_length - filled_up_Length));
        String bar = pounds + equals;
        runOnUiThread(() -> textview_output_messages_2.setText(prefix + " |" + bar + "| " + percentage + '%'));
    }*/


    @SuppressLint("Recycle")
    private File saveSubtitleFileToDocumentsDir(String tmpSubtitleFilePath) {
        InputStream tmpSubtitleInputStream;
        Uri tmpSubtitleUri = Uri.fromFile(new File(tmpSubtitleFilePath));
        try {
            tmpSubtitleInputStream = getApplicationContext().getContentResolver().openInputStream(tmpSubtitleUri);
        } catch (FileNotFoundException e) {
            Log.e("FileNotFoundException: ", e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        String subtitleFileDisplayName = tmpSubtitleFilePath.substring(tmpSubtitleFilePath.lastIndexOf("/") + 1);
        Log.d("saveSubtitleFileToDocumentsDir", "subtitleFileDisplayName = " + subtitleFileDisplayName);
        String subtitleFolderDisplayName = StringUtils.substring(subtitleFileDisplayName, 0, subtitleFileDisplayName.length() - SUBTITLE.FORMAT.length() - 1);
        String savedFolderPath = getExternalStorageDirectory() + File.separator + DIRECTORY_DOCUMENTS + File.separator + getPackageName() + File.separator + subtitleFolderDisplayName;
        OutputStream savedSubtitleFileOutputStream;
        Uri savedSubtitleUri = null;
        Uri savedTranslatedSubtitleUri = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            TranscribeActivity.this.getActivityResultRegistry().register("key", new ActivityResultContracts.OpenDocument(), result -> TranscribeActivity.this.getApplicationContext().getContentResolver().takePersistableUriPermission(result, Intent.FLAG_GRANT_READ_URI_PERMISSION));
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, subtitleFileDisplayName); // savedFile name subtitleFileDisplayName required to contain extension savedFile mime
            values.put(MediaStore.MediaColumns.MIME_TYPE, "*/*");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, DIRECTORY_DOCUMENTS + File.separator + getPackageName() + File.separator + subtitleFolderDisplayName);
            Uri extVolumeUri = MediaStore.Files.getContentUri("external");
            Log.d("saveSubtitleFileToDocumentsDir", "extVolumeUri = " + extVolumeUri);

            if (Environment.isExternalStorageManager()) {
                String selection = MediaStore.MediaColumns.RELATIVE_PATH + "=?";
                String[] selectionArgs = new String[]{DIRECTORY_DOCUMENTS + File.separator + getPackageName() + File.separator + subtitleFolderDisplayName + File.separator};    //must include "/" in front and end
                Cursor cursor = getApplicationContext().getContentResolver().query(extVolumeUri, null, selection, selectionArgs, null);

                Log.d("saveSubtitleFileToDocumentsDir", "cursor.getCount() = " + cursor.getCount());
                if (cursor.getCount() == 0) {
                    savedSubtitleUri = getApplicationContext().getContentResolver().insert(extVolumeUri, values);
                } else {
                    while (cursor.moveToNext()) {
                        String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));
                        Log.d("saveSubtitleFileToDocumentsDir", "fileName = " + fileName);
                        if (fileName.equals(subtitleFileDisplayName)) {
                            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                            savedSubtitleUri = ContentUris.withAppendedId(extVolumeUri, id);
                            break;
                        }
                    }
                    if (savedSubtitleUri == null) {
                        savedSubtitleUri = getApplicationContext().getContentResolver().insert(extVolumeUri, values);
                    }
                }
            }
            else {
                savedSubtitleUri = getApplicationContext().getContentResolver().insert(extVolumeUri, values);
            }
            try {
                savedSubtitleFileOutputStream = getApplicationContext().getContentResolver().openOutputStream(savedSubtitleUri);
            } catch (FileNotFoundException e) {
                Log.e("FileNotFoundException: ", e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }

        }
        else {
            File root = new File(getExternalStorageDirectory() + File.separator + DIRECTORY_DOCUMENTS + File.separator + getPackageName() + File.separator + subtitleFolderDisplayName);
            if (!root.exists() && root.mkdirs()) {
                Log.d("saveSubtitleFileToDocumentsDir", root + " created");
            }
            File savedSubtitleFile = new File(root, subtitleFileDisplayName);
            Log.d("saveSubtitleFileToDocumentsDir", "savedSubtitleFile.getAbsolutePath() = " + savedSubtitleFile.getAbsolutePath());
            try {
                savedSubtitleFileOutputStream = new FileOutputStream(savedSubtitleFile);
            } catch (FileNotFoundException e) {
                Log.e("FileNotFoundException: ", e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        byte[] bytes = new byte[1024];
        int length;
        while (true) {
            try {
                if (!((length = tmpSubtitleInputStream.read(bytes)) > 0)) break;
            } catch (IOException e) {
                Log.e("IOException: ", e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            try {
                savedSubtitleFileOutputStream.write(bytes, 0, length);
            } catch (IOException e) {
                Log.e("IOException: ", e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        try {
            savedSubtitleFileOutputStream.close();
            tmpSubtitleInputStream.close();
        } catch (IOException e) {
            Log.e("IOException: ", e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }


        if (!Objects.equals(LANGUAGE.SRC_CODE, LANGUAGE.DST_CODE)) {
            InputStream tmpTranslatedSubtitleInputStream;
            String translatedSubtitleFileDisplayName = StringUtils.substring(subtitleFileDisplayName, 0, subtitleFileDisplayName.length() - SUBTITLE.FORMAT.length() - 1) + ".translated." + SUBTITLE.FORMAT;
            String tmpTranslatedSubtitleFilePath = StringUtils.substring(tmpSubtitleFilePath, 0, tmpSubtitleFilePath.length() - SUBTITLE.FORMAT.length() - 1) + ".translated." + SUBTITLE.FORMAT;
            Uri tmpTranslatedSubtitleUri = Uri.fromFile(new File(tmpTranslatedSubtitleFilePath));
            OutputStream savedTranslatedSubtitleFileOutputStream;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ContentValues savedTranslatedSubtitleValues = new ContentValues();
                savedTranslatedSubtitleValues.put(MediaStore.MediaColumns.DISPLAY_NAME, translatedSubtitleFileDisplayName); // savedFile name translatedSubtitleFileDisplayName required to contain extension savedFile mime
                savedTranslatedSubtitleValues.put(MediaStore.MediaColumns.MIME_TYPE, "*/*");
                savedTranslatedSubtitleValues.put(MediaStore.MediaColumns.RELATIVE_PATH, DIRECTORY_DOCUMENTS + File.separator + getPackageName() + File.separator + subtitleFolderDisplayName);
                Uri extVolumeUri = MediaStore.Files.getContentUri("external");

                if (Environment.isExternalStorageManager()) {
                    String selection = MediaStore.MediaColumns.RELATIVE_PATH + "=?";
                    String[] selectionArgs = new String[]{DIRECTORY_DOCUMENTS + File.separator + getPackageName() + File.separator + subtitleFolderDisplayName + File.separator};
                    @SuppressLint("Recycle") Cursor cursor = getContentResolver().query(extVolumeUri, null, selection, selectionArgs, null);

                    if (cursor.getCount() == 0) {
                        savedTranslatedSubtitleUri = getApplicationContext().getContentResolver().insert(extVolumeUri, savedTranslatedSubtitleValues);
                    } else {
                        while (cursor.moveToNext()) {
                            String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));
                            if (fileName.equals(translatedSubtitleFileDisplayName)) {
                                long id = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                                savedTranslatedSubtitleUri = ContentUris.withAppendedId(extVolumeUri, id);
                                break;
                            }
                        }
                        if (savedTranslatedSubtitleUri == null) {
                            savedTranslatedSubtitleUri = getApplicationContext().getContentResolver().insert(extVolumeUri, savedTranslatedSubtitleValues);
                        }
                    }
                }
                else {
                    savedTranslatedSubtitleUri = getApplicationContext().getContentResolver().insert(extVolumeUri, savedTranslatedSubtitleValues);
                }
                try {
                    savedTranslatedSubtitleFileOutputStream = getApplicationContext().getContentResolver().openOutputStream(savedTranslatedSubtitleUri);
                } catch (FileNotFoundException e) {
                    Log.e("FileNotFoundException: ", e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }

            } else {
                File root = new File(getExternalStorageDirectory() + File.separator + DIRECTORY_DOCUMENTS + File.separator + getPackageName() + File.separator + subtitleFolderDisplayName);
                if (!root.exists() && root.mkdirs()) {
                    Log.d("saveSubtitleFileToDocumentsDir", root + " created");
                }
                File savedTranslatedSubtitleFile = new File(root, translatedSubtitleFileDisplayName);
                Log.d("saveSubtitleFileToDocumentsDir", "savedTranslatedSubtitleFile.getAbsolutePath() = " + savedTranslatedSubtitleFile.getAbsolutePath());
                try {
                    savedTranslatedSubtitleFileOutputStream = new FileOutputStream(savedTranslatedSubtitleFile);
                } catch (FileNotFoundException e) {
                    Log.e("FileNotFoundException: ", e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }

            try {
                tmpTranslatedSubtitleInputStream = getApplicationContext().getContentResolver().openInputStream(tmpTranslatedSubtitleUri);
            } catch (FileNotFoundException e) {
                Log.e("FileNotFoundException: ", e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            byte[] tmpTranslatedSubtitleBytes = new byte[1024];
            int tmpTranslatedSubtitleLength;
            while (true) {
                try {
                    if (!((tmpTranslatedSubtitleLength = tmpTranslatedSubtitleInputStream.read(tmpTranslatedSubtitleBytes)) > 0))
                        break;
                } catch (IOException e) {
                    Log.e("IOException: ", e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
                try {
                    savedTranslatedSubtitleFileOutputStream.write(tmpTranslatedSubtitleBytes, 0, tmpTranslatedSubtitleLength);
                } catch (IOException e) {
                    Log.e("IOException: ", e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
            try {
                savedTranslatedSubtitleFileOutputStream.close();
                tmpTranslatedSubtitleInputStream.close();
            } catch (IOException e) {
                Log.e("IOException: ", e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return new File(Uri2Path(getApplicationContext(), savedSubtitleUri));
        }
        else {
            return new File(savedFolderPath + File.separator + subtitleFileDisplayName);
        }
    }


    @SuppressLint("Recycle")
    private File saveSubtitleFileToSelectedDir(String tmpSubtitleFilePath, Uri selectedDirUri) {
        Uri tmpSubtitleUri = Uri.fromFile(new File(tmpSubtitleFilePath));
        String tmpTranslatedSubtitleFilePath = StringUtils.substring(tmpSubtitleFilePath, 0, tmpSubtitleFilePath.length() - SUBTITLE.FORMAT.length() - 1) + ".translated." + SUBTITLE.FORMAT;
        Uri tmpTranslatedSubtitleUri = Uri.fromFile(new File(tmpTranslatedSubtitleFilePath));

        InputStream tmpSubtitleInputStream;
        InputStream tmpTranslatedSubtitleInputStream;

        OutputStream savedSubtitleOutputStream = null;
        OutputStream savedTranslatedSubtitleOutputStream = null;

        Uri savedSubtitleUri;
        Uri savedTranslatedSubtitleUri;

        String subtitleFileDisplayName = tmpSubtitleFilePath.substring(tmpSubtitleFilePath.lastIndexOf("/") + 1);
        Log.d("saveSubtitleFileToSelectedDir", "subtitleFileDisplayName = " + subtitleFileDisplayName);
        String translatedSubtitleFileDisplayName = StringUtils.substring(subtitleFileDisplayName, 0, subtitleFileDisplayName.length() - SUBTITLE.FORMAT.length() - 1) + ".translated." + SUBTITLE.FORMAT;
        Log.d("saveSubtitleFileToSelectedDir", "translatedSubtitleFileDisplayName = " + translatedSubtitleFileDisplayName);

        DocumentFile selectedDirDocumentFile = DocumentFile.fromTreeUri(TranscribeActivity.this, selectedDirUri);
        DocumentFile savedSubtitleDocumentFile;
        DocumentFile savedTranslatedSubtitleDocumentFile;

        String savedSubtitleFilePath = null;

        try {
            tmpSubtitleInputStream = getApplicationContext().getContentResolver().openInputStream(tmpSubtitleUri);
        } catch (FileNotFoundException e) {
            Log.e("FileNotFoundException: ", e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        if (selectedDirDocumentFile != null) {
            ParcelFileDescriptor parcelFileDescriptor;
            if (!selectedDirDocumentFile.exists()) {
                Log.e("saveSubtitleFileToSelectedDir", selectedDirDocumentFile +  " is not exists");
                releasePermissions(selectedDirUri);
                setText(textview_output_messages_2, selectedDirDocumentFile + " is not exist!");
                return null;
            }
            else {
                savedSubtitleDocumentFile = selectedDirDocumentFile.findFile(subtitleFileDisplayName);
                Log.d("saveSubtitleFileToSelectedDir", "savedSubtitleDocumentFile = " + savedSubtitleDocumentFile);
                if (savedSubtitleDocumentFile == null) savedSubtitleDocumentFile = selectedDirDocumentFile.createFile("*/*", subtitleFileDisplayName);
                if (savedSubtitleDocumentFile != null && savedSubtitleDocumentFile.canWrite()) {
                    savedSubtitleUri = savedSubtitleDocumentFile.getUri();
                    Log.d("saveSubtitleFileToSelectedDir", "subtitleFile.getUri() = " + savedSubtitleDocumentFile.getUri());
                    savedSubtitleFilePath = Uri2Path(getApplicationContext(), savedSubtitleUri);
                    Log.d("saveSubtitleFileToSelectedDir", "savedSubtitleFilePath = " + savedSubtitleFilePath);
                    try {
                        parcelFileDescriptor = getContentResolver().openFileDescriptor(savedSubtitleUri, "w");
                        savedSubtitleOutputStream = new FileOutputStream(parcelFileDescriptor.getFileDescriptor());
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        parcelFileDescriptor.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                else {
                    Log.d("saveSubtitleFileToSelectedDir", subtitleFileDisplayName + " is not exist or cannot write");
                    setText(textview_output_messages_2, "Write error!");
                }
            }
        }

        byte[] bytes = new byte[1024];
        int length;
        while (true) {
            try {
                if (!((length = tmpSubtitleInputStream.read(bytes)) > 0)) break;
            } catch (IOException e) {
                Log.e("IOException: ", e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            try {
                if (savedSubtitleOutputStream != null) {
                    savedSubtitleOutputStream.write(bytes, 0, length);
                }
            } catch (IOException e) {
                Log.e("IOException: ", e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        try {
            if (savedSubtitleOutputStream != null) {
                savedSubtitleOutputStream.close();
            }
            tmpSubtitleInputStream.close();
        } catch (IOException e) {
            Log.e("IOException: ", e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        if (!Objects.equals(LANGUAGE.SRC_CODE, LANGUAGE.DST_CODE)) {
            if (selectedDirDocumentFile == null || !selectedDirDocumentFile.exists()) {
                Log.e("saveSubtitleFileToSelectedDir", selectedDirDocumentFile +  " not exists");
                releasePermissions(selectedDirUri);
                setText(textview_output_messages_2, selectedDirDocumentFile + " not exist!");
                return null;
            }
            else {
                savedTranslatedSubtitleDocumentFile = selectedDirDocumentFile.findFile(translatedSubtitleFileDisplayName);
                if (savedTranslatedSubtitleDocumentFile == null) savedTranslatedSubtitleDocumentFile = selectedDirDocumentFile.createFile("*/*", translatedSubtitleFileDisplayName);
                if (savedTranslatedSubtitleDocumentFile != null && savedTranslatedSubtitleDocumentFile.canWrite()) {
                    savedTranslatedSubtitleUri = savedTranslatedSubtitleDocumentFile.getUri();
                    Log.d("saveSubtitleFileToSelectedDir", "savedTranslatedSubtitleDocumentFile.getUri() = " + savedTranslatedSubtitleDocumentFile.getUri());
                    String savedTranslatedSubtitleFile = Uri2Path(getApplicationContext(), savedTranslatedSubtitleUri);
                    Log.d("saveSubtitleFileToSelectedDir", "savedTranslatedSubtitleFile = " + savedTranslatedSubtitleFile);
                    try {
                        ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(savedTranslatedSubtitleUri, "w");
                        savedTranslatedSubtitleOutputStream = new FileOutputStream(parcelFileDescriptor.getFileDescriptor());
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
                else {
                    Log.d("saveSubtitleFileToSelectedDir", subtitleFileDisplayName + " is not exist or cannot write");
                    setText(textview_output_messages_2, "Write error!");
                }
            }

            try {
                tmpTranslatedSubtitleInputStream = getApplicationContext().getContentResolver().openInputStream(tmpTranslatedSubtitleUri);
            } catch (FileNotFoundException e) {
                Log.e("FileNotFoundException: ", e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            byte[] bytesTranslated = new byte[1024];
            int lengthTranslated;
            while (true) {
                try {
                    if (!((lengthTranslated = tmpTranslatedSubtitleInputStream.read(bytesTranslated)) > 0))
                        break;
                } catch (IOException e) {
                    Log.e("IOException: ", e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
                try {
                    if (savedTranslatedSubtitleOutputStream != null) {
                        savedTranslatedSubtitleOutputStream.write(bytesTranslated, 0, lengthTranslated);
                    }
                } catch (IOException e) {
                    Log.e("IOException: ", e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
            try {
                if (savedTranslatedSubtitleOutputStream != null) {
                    savedTranslatedSubtitleOutputStream.close();
                }
                tmpTranslatedSubtitleInputStream.close();
            } catch (IOException e) {
                Log.e("IOException: ", e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        if (savedSubtitleFilePath != null) {
            Log.d("saveSubtitleFileToSelectedDir", "Succesed! Returned savedSubtitleFilePath = " + savedSubtitleFilePath);
            return new File(savedSubtitleFilePath);
        }
        else {
            Log.d("saveSubtitleFileToSelectedDir", "Failed! Returned null!");
            return null;
        }
    }


    private void releasePermissions(Uri uri) {
        int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
        getContentResolver().releasePersistableUriPermission(uri,takeFlags);
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


    private void adjustOutputMessagesHeight() {
        textview_output_messages_2.post(() -> {
            DisplayMetrics display = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(display);
            int displayheightPixels = display.heightPixels;
            Log.d("adjustOutputMessagesHeight", "displayheightPixels = " + displayheightPixels);
            int[] location = new int[2];
            textview_output_messages_2.getLocationOnScreen(location);
            int top = location[1];
            Log.d("adjustOutputMessagesHeight", "top = " + top);
            int height = textview_output_messages_2.getHeight();
            Log.d("adjustOutputMessagesHeight", "height = " + height);
            int emptySpace = displayheightPixels - (top + height);
            Log.d("adjustOutputMessagesHeight", "emptySpace = " + emptySpace);
            int newHeight = height + emptySpace - 24;
            Log.d("adjustOutputMessagesHeight", "newHeight = " + (height + emptySpace));

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) textview_output_messages_2.getLayoutParams();
            params.height = newHeight;
            textview_output_messages_2.setLayoutParams(params);
            heightOfOutputMessages = newHeight;

            int lineHeight = textview_output_messages_2.getLineHeight();
            maxLinesOfOutputMessages = heightOfOutputMessages / lineHeight;
            Log.d("adjustOutputMessagesHeight", "maxLinesOfOutputMessages = " + maxLinesOfOutputMessages);

        });
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

    private void hideProgressBar() {
        runOnUiThread(() -> {
            textview_progress.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            textview_percentage.setVisibility(View.INVISIBLE);
            textview_time.setVisibility(View.INVISIBLE);
        });
    }

    public int calculateMaxCharsInTextView(String text, int viewWidth, int textSize) {
        Paint paint = new Paint();
        paint.setTextSize(textSize);
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        int textWidth = bounds.width();
        return (int) Math.floor((double) viewWidth / (double) textWidth * text.length());
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

    private void setText(final TextView tv, final String text){
        runOnUiThread(() -> tv.setText(text));
    }

    private void appendText(final TextView tv, final String text){
        runOnUiThread(() -> {
            int lines = textview_output_messages_2.getLineCount();
            if (lines >= maxLinesOfOutputMessages) textview_output_messages_2.setGravity(Gravity.BOTTOM);
            tv.append(text);
        });
    }


}
