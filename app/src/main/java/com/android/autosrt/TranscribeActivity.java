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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
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
    public static PyObject pyObjTmpResults;
    public static Thread threadTranscriber;
    public static String cancelFile = null;
    public static String subtitleFile = null;

    int heightOfOutputMessages;
    int maxLinesOfOutputMessages;
    int equalMaxChars = 0;
    int dashMaxChars = 0;
    String equalChars = StringUtils.repeat('=', 49);
    String dashChars = StringUtils.repeat('-', 89);

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

        for (int i = 0; i< MEDIA_FILE.PATH_LIST.size(); i++) {
            //String fp = "File path [" + i + "] = " + MEDIA_FILE.PATH_LIST.get(i) + "\n";
            String fp = MEDIA_FILE.PATH_LIST.get(i) + "\n";
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
        textview_output_messages_2.post(this::adjustOutputMessagesHeight);

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


    @SuppressLint("DefaultLocale")
    private void transcribe() {
        if (threadTranscriber != null && threadTranscriber.isAlive()) threadTranscriber.interrupt();
        threadTranscriber = null;
        threadTranscriber = new Thread(() -> {
            if (Looper.myLooper() == null) {
                Looper.prepare();
            }

            if (MEDIA_FILE.URI_LIST != null) {
                if (TRANSCRIBE_STATUS.IS_TRANSCRIBING) {
                    Log.d("transcribe", "Running");

                    transcribeStartTime = System.currentTimeMillis();
                    Log.d("transcribe", "transcribeStartTime = " + transcribeStartTime);

                    try {
                        if (!Python.isStarted()) {
                            Python.start(new AndroidPlatform(TranscribeActivity.this));
                            py = Python.getInstance();
                        }

                        if (MEDIA_FILE.PATH_LIST == null) {
                            threadTranscriber.interrupt();
                            threadTranscriber = null;
                            transcribe();
                        }

                        SUBTITLE.SAVED_FILE = new File[MEDIA_FILE.URI_LIST.size()];

                        runOnUiThread(() -> {
                            setText(textview_output_messages_2, "");
                            equalChars = readStringFromFile(TranscribeActivity.this, "equalChars");
                            Log.d("transcribe", "equalChars.length() = " + equalChars.length());
                            appendText(textview_output_messages_2, equalChars + "\n");
                        });

                        for (int i = 0; i< MEDIA_FILE.URI_LIST.size(); i++) {
                            if (!TRANSCRIBE_STATUS.IS_TRANSCRIBING) return;

                            SUBTITLE.TMP_SAVED_FILE_PATH_LIST = new ArrayList<>();

                            MEDIA_FILE.FORMAT = MEDIA_FILE.PATH_LIST.get(i).substring(MEDIA_FILE.PATH_LIST.get(i).lastIndexOf(".") + 1);
                            String subtitleFolderDisplayName = StringUtils.substring(MEDIA_FILE.DISPLAY_NAME_LIST.get(i), 0, MEDIA_FILE.DISPLAY_NAME_LIST.get(i).length() - MEDIA_FILE.FORMAT.length() - 1);
                            Log.d("transcribe", "subtitleFolderDisplayName = " + subtitleFolderDisplayName);

                            setText(textview_current_file, "Processing file : " + MEDIA_FILE.DISPLAY_NAME_LIST.get(i));
                            int finalI = i;
                            textview_output_messages_2.post(() -> {
                                appendText(textview_output_messages_2, "Processing file : " + MEDIA_FILE.DISPLAY_NAME_LIST.get(finalI) + "\n");
                                appendText(textview_output_messages_2, equalChars + "\n");
                            });

                            if (!Python.isStarted()) {
                                Python.start(new AndroidPlatform(TranscribeActivity.this));
                                py = Python.getInstance();
                            }

                            pyObjTmpResults = py.getModule("autosrt").callAttr(
                                    "transcribe",
                                    LANGUAGE.SRC_CODE,
                                    LANGUAGE.DST_CODE,
                                    MEDIA_FILE.PATH_LIST.get(i),
                                    MEDIA_FILE.DISPLAY_NAME_LIST.get(i),
                                    SUBTITLE.FORMAT,
                                    TranscribeActivity.this,
                                    textview_output_messages_2,
                                    textview_progress,
                                    progressBar,
                                    textview_percentage,
                                    textview_time
                            );

                            Log.d("transcribe", "pyObjTmpResults = " + pyObjTmpResults);

                            // Extract pyObjTmpResults tuple
                            if (pyObjTmpResults != null) {

                                String extension;
                                List<PyObject> tupleTmpResults = new ArrayList<>(pyObjTmpResults.asList());

                                for (int idx = 0; idx < tupleTmpResults.size(); idx++) {
                                    Log.d("transcribe", "tupleTmpResults.get(" + idx + ") = " + tupleTmpResults.get(idx));

                                    extension = tupleTmpResults.get(idx).toString().substring(tupleTmpResults.get(idx).toString().lastIndexOf(".") + 1);
                                    Log.d("transcribe", "extension = " + extension);

                                    if (extension.equals("srt") || extension.equals("vtt") || extension.equals("json") || extension.equals("raw")) {
                                        SUBTITLE.TMP_SAVED_FILE_PATH_LIST.add(tupleTmpResults.get(idx).toString());
                                    }
                                }
                            }

                            if (SUBTITLE.TMP_SAVED_FILE_PATH_LIST != null) {

                                for (int idx = 0; idx < SUBTITLE.TMP_SAVED_FILE_PATH_LIST.size(); idx++) {
                                    Log.d("transcribe", "SUBTITLE.TMP_SAVED_FILE_PATH_LIST.get(" + idx + ") = " + SUBTITLE.TMP_SAVED_FILE_PATH_LIST.get(idx));
                                }

                                SUBTITLE.TMP_SAVED_SRC_FILE_PATH = SUBTITLE.TMP_SAVED_FILE_PATH_LIST.get(0);
                                Log.d("transcribe", "SUBTITLE.TMP_SAVED_SRC_FILE_PATH = " + SUBTITLE.TMP_SAVED_SRC_FILE_PATH);

                                if (SUBTITLE.TMP_SAVED_FILE_PATH_LIST.size() == 2 && SUBTITLE.TMP_SAVED_FILE_PATH_LIST.get(1) != null) {
                                    SUBTITLE.TMP_SAVED_DST_FILE_PATH = SUBTITLE.TMP_SAVED_FILE_PATH_LIST.get(1);
                                    Log.d("transcribe", "SUBTITLE.TMP_SAVED_DST_FILE_PATH = " + SUBTITLE.TMP_SAVED_DST_FILE_PATH);
                                }

                                Log.d("transcribe", "new File(tmpSrcSubtitleFilePath).exists() = " + new File(SUBTITLE.TMP_SAVED_SRC_FILE_PATH).exists());
                                Log.d("transcribe", "new File(tmpSrcSubtitleFilePath).length() = " + new File(SUBTITLE.TMP_SAVED_SRC_FILE_PATH).length());

                                if (new File(SUBTITLE.TMP_SAVED_SRC_FILE_PATH).exists() && new File(SUBTITLE.TMP_SAVED_SRC_FILE_PATH).length() > 1) {
                                    FOLDER.SAVED_URI_LIST = loadSavedTreeUrisFromSharedPreference();
                                    if (FOLDER.SAVED_URI_LIST.size() == 0) {

                                        Log.d("transcribe", "Saving subtitle file using saveSubtitleFileToDocumentsDir()");
                                        SUBTITLE.SAVED_FILE[i] = saveSubtitleFileToDocumentsDir(SUBTITLE.TMP_SAVED_SRC_FILE_PATH, SUBTITLE.TMP_SAVED_DST_FILE_PATH, subtitleFolderDisplayName);
                                        Log.d("transcribe", "SUBTITLE.SAVED_FILE[" + i + "] = " + SUBTITLE.SAVED_FILE[i]);

                                        if (SUBTITLE.SAVED_FILE[i].exists() && SUBTITLE.SAVED_FILE[i].length() > 1) {
                                            Log.d("transcribe", SUBTITLE.SAVED_FILE[i] + " created");
                                            appendText(textview_output_messages_2, equalChars + "\n");
                                            appendText(textview_output_messages_2, "Overall results for " + MEDIA_FILE.DISPLAY_NAME_LIST.get(i) + " : \n");
                                            appendText(textview_output_messages_2, equalChars + "\n");
                                            appendText(textview_output_messages_2, SUBTITLE.SAVED_FILE[i] + "\n");

                                            if (!Objects.equals(LANGUAGE.SRC_CODE, LANGUAGE.DST_CODE)) {
                                                String savedDstSubtitleFilePath = StringUtils.replace(SUBTITLE.SAVED_FILE[i].toString(), LANGUAGE.SRC_CODE + ".srt", LANGUAGE.DST_CODE + ".srt");
                                                Log.d("transcribe", "savedDstSubtitleFilePath = " + savedDstSubtitleFilePath);
                                                if (new File(savedDstSubtitleFilePath).exists() && new File(savedDstSubtitleFilePath).length() > 1) {
                                                    appendText(textview_output_messages_2, equalChars + "\n");
                                                    appendText(textview_output_messages_2, savedDstSubtitleFilePath + "\n");
                                                }
                                            }
                                            appendText(textview_output_messages_2, equalChars + "\n");
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

                                        boolean alreadySaved = isTreeUriPermissionGrantedForDirPathOfFilePath(MEDIA_FILE.PATH_LIST.get(i));
                                        if (alreadySaved) {

                                            Log.d("transcribe", "Saving subtitle file using saveSubtitleFileToSelectedDir()");
                                            Log.d("transcribe", "FOLDER.URI = " + FOLDER.URI);
                                            SUBTITLE.SAVED_FILE[i] = saveSubtitleFileToSelectedDir(SUBTITLE.TMP_SAVED_SRC_FILE_PATH, SUBTITLE.TMP_SAVED_DST_FILE_PATH, FOLDER.URI);

                                            if (SUBTITLE.SAVED_FILE[i].exists() && SUBTITLE.SAVED_FILE[i].length() > 1) {
                                                Log.d("transcribe", SUBTITLE.SAVED_FILE[i].toString() + " created");
                                                appendText(textview_output_messages_2, equalChars + "\n");
                                                appendText(textview_output_messages_2, "Overall results for " + MEDIA_FILE.DISPLAY_NAME_LIST.get(i) + " : \n");
                                                appendText(textview_output_messages_2, SUBTITLE.SAVED_FILE[i].toString() + "\n");

                                                if (!Objects.equals(LANGUAGE.SRC_CODE, LANGUAGE.DST_CODE)) {
                                                    String savedDstSubtitleFilePath = StringUtils.replace(SUBTITLE.SAVED_FILE[i].toString(), LANGUAGE.SRC_CODE + ".srt", LANGUAGE.DST_CODE + ".srt");
                                                    Log.d("transcribe", "savedDstSubtitleFilePath = " + savedDstSubtitleFilePath);
                                                    if (new File(savedDstSubtitleFilePath).exists() && new File(savedDstSubtitleFilePath).length() > 1) {
                                                        appendText(textview_output_messages_2, savedDstSubtitleFilePath + "\n");
                                                    }
                                                }

                                                appendText(textview_output_messages_2, equalChars + "\n");
                                            }

                                        }
                                        else {

                                            Log.d("transcribe", "Saving subtitle file using saveSubtitleFileToDocumentsDir()");
                                            SUBTITLE.SAVED_FILE[i] = saveSubtitleFileToDocumentsDir(SUBTITLE.TMP_SAVED_SRC_FILE_PATH, SUBTITLE.TMP_SAVED_DST_FILE_PATH, subtitleFolderDisplayName);
                                            Log.d("transcribe", "SUBTITLE.SAVED_FILE[" + i + "] = " + SUBTITLE.SAVED_FILE[i]);

                                            if (new File(SUBTITLE.SAVED_FILE[i].toString()).exists() && new File(SUBTITLE.SAVED_FILE[i].toString()).length() > 1) {
                                                Log.d("transcribe", SUBTITLE.SAVED_FILE[i] + " created");
                                                appendText(textview_output_messages_2, equalChars + "\n");
                                                appendText(textview_output_messages_2, "Overall results for " + MEDIA_FILE.DISPLAY_NAME_LIST.get(i) + " : \n");
                                                appendText(textview_output_messages_2, SUBTITLE.SAVED_FILE[i] + "\n");

                                                if (!Objects.equals(LANGUAGE.SRC_CODE, LANGUAGE.DST_CODE)) {
                                                    String savedDstSubtitleFilePath = StringUtils.replace(SUBTITLE.SAVED_FILE[i].toString(), LANGUAGE.SRC_CODE + ".srt", LANGUAGE.DST_CODE + ".srt");
                                                    Log.d("transcribe", "savedDstSubtitleFilePath = " + savedDstSubtitleFilePath);
                                                    if (new File(savedDstSubtitleFilePath).exists() && new File(savedDstSubtitleFilePath).length() > 1) {
                                                        appendText(textview_output_messages_2, savedDstSubtitleFilePath + "\n");
                                                    }
                                                }

                                                appendText(textview_output_messages_2, equalChars);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        setText(textview_current_file, "");

                        if (TRANSCRIBE_STATUS.IS_TRANSCRIBING && MEDIA_FILE.URI_LIST != null) {
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
                            appendText(textview_output_messages_2, "Total running time : " + formattedElapsedTime + "\n");
                            appendText(textview_output_messages_2, equalChars + "\n");
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
                    String m = "Please select at least 1 media file\n";
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
                }
                catch (IOException e) {
                    Log.e("showConfirmationDialogue", e.getMessage());
                    e.printStackTrace();
                }

                if (fc.exists()) {
                    Log.i("showConfirmationDialogue", "cancelFile exists");
                }
                else {
                    Log.i("showConfirmationDialogue", "cancelFile is not exist");
                }

                if (SUBTITLE.TMP_SAVED_SRC_FILE_PATH != null) {
                    File sf = new File(SUBTITLE.TMP_SAVED_SRC_FILE_PATH).getAbsoluteFile();
                    if (sf.exists() && sf.delete()) {
                        Log.i("showConfirmationDialogue", new File(SUBTITLE.TMP_SAVED_SRC_FILE_PATH).getAbsoluteFile() + " deleted");
                    }
                }

                if (SUBTITLE.TMP_SAVED_DST_FILE_PATH != null) {
                    File stf = new File(SUBTITLE.TMP_SAVED_DST_FILE_PATH).getAbsoluteFile();
                    if (stf.exists() && stf.delete()) {
                        Log.i("showConfirmationDialogue", new File(SUBTITLE.TMP_SAVED_DST_FILE_PATH).getAbsoluteFile() + " deleted");
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
            finish();
        }
    }


    /*private int convertToWav(String filePath, File tmpWavFile, int channels, int rate) {
        AtomicReference<Float> progress = new AtomicReference<>((float) 0);
        Uri fileURI = Uri.fromFile(new File(filePath));
        int mediaDuration = MediaPlayer.create(getApplicationContext(), fileURI).getDuration();

        Config.resetStatistics();
        Config.enableStatisticsCallback(newStatistics -> {
            progress.set(Float.parseFloat(String.valueOf(newStatistics.getTime())) / mediaDuration);
            int progressFinal = (int) (progress.get() * 100);
            Log.d(Config.TAG, "progressFinal = " + progressFinal);
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
    private File saveSubtitleFileToDocumentsDir(String tmpSrcSubtitleFilePath, String tmpDstSubtitleFilePath, String subtitleFolderDisplayName) {
        InputStream tmpSrcSubtitleInputStream;
        Uri tmpSrcSubtitleUri = Uri.fromFile(new File(tmpSrcSubtitleFilePath));
        try {
            tmpSrcSubtitleInputStream = getApplicationContext().getContentResolver().openInputStream(tmpSrcSubtitleUri);
        }
        catch (FileNotFoundException e) {
            Log.e("FileNotFoundException: ", e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        String srcSubtitleFileDisplayName = tmpSrcSubtitleFilePath.substring(tmpSrcSubtitleFilePath.lastIndexOf("/") + 1);
        Log.d("saveSubtitleFileToDocumentsDir", "srcSubtitleFileDisplayName = " + srcSubtitleFileDisplayName);
        String savedFolderPath = getExternalStorageDirectory() + File.separator + DIRECTORY_DOCUMENTS + File.separator + getPackageName() + File.separator + subtitleFolderDisplayName;
        Log.d("saveSubtitleFileToDocumentsDir", "savedFolderPath = " + savedFolderPath);
        OutputStream savedSrcSubtitleFilesOutputStream;
        Uri savedSrcSubtitleUri = null;
        Uri savedDstSubtitleUri = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            TranscribeActivity.this.getActivityResultRegistry().register("key", new ActivityResultContracts.OpenDocument(), result -> TranscribeActivity.this.getApplicationContext().getContentResolver().takePersistableUriPermission(result, Intent.FLAG_GRANT_READ_URI_PERMISSION));
            ContentValues savedSrcSubtitleValues = new ContentValues();
            savedSrcSubtitleValues.put(MediaStore.MediaColumns.DISPLAY_NAME, srcSubtitleFileDisplayName); // savedFile name srcSubtitleFileDisplayName required to contain extension savedFile mime
            savedSrcSubtitleValues.put(MediaStore.MediaColumns.MIME_TYPE, "*/*");
            savedSrcSubtitleValues.put(MediaStore.MediaColumns.RELATIVE_PATH, DIRECTORY_DOCUMENTS + File.separator + getPackageName() + File.separator + subtitleFolderDisplayName);
            Uri extVolumeUri = MediaStore.Files.getContentUri("external");
            Log.d("saveSubtitleFileToDocumentsDir", "extVolumeUri = " + extVolumeUri);

            if (Environment.isExternalStorageManager()) {
                String selection = MediaStore.MediaColumns.RELATIVE_PATH + "=?";
                String[] selectionArgs = new String[]{DIRECTORY_DOCUMENTS + File.separator + getPackageName() + File.separator + subtitleFolderDisplayName + File.separator};    //must include "/" in front and end
                Cursor cursor = getApplicationContext().getContentResolver().query(extVolumeUri, null, selection, selectionArgs, null);

                Log.d("saveSubtitleFileToDocumentsDir", "cursor.getCount() = " + cursor.getCount());
                if (cursor.getCount() == 0) {
                    savedSrcSubtitleUri = getApplicationContext().getContentResolver().insert(extVolumeUri, savedSrcSubtitleValues);
                } else {
                    while (cursor.moveToNext()) {
                        String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));
                        Log.d("saveSubtitleFileToDocumentsDir", "fileName = " + fileName);
                        if (fileName.equals(srcSubtitleFileDisplayName)) {
                            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                            savedSrcSubtitleUri = ContentUris.withAppendedId(extVolumeUri, id);
                            break;
                        }
                    }
                    if (savedSrcSubtitleUri == null) {
                        savedSrcSubtitleUri = getApplicationContext().getContentResolver().insert(extVolumeUri, savedSrcSubtitleValues);
                    }
                }
                cursor.close();
            }
            else {
                savedSrcSubtitleUri = getApplicationContext().getContentResolver().insert(extVolumeUri, savedSrcSubtitleValues);
            }
            try {
                savedSrcSubtitleFilesOutputStream = getApplicationContext().getContentResolver().openOutputStream(savedSrcSubtitleUri);
            }
            catch (FileNotFoundException e) {
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
            File savedSubtitleFiles = new File(root, srcSubtitleFileDisplayName);
            Log.d("saveSubtitleFileToDocumentsDir", "savedSubtitleFiles.getAbsolutePath() = " + savedSubtitleFiles.getAbsolutePath());
            try {
                savedSrcSubtitleFilesOutputStream = new FileOutputStream(savedSubtitleFiles);
            }
            catch (FileNotFoundException e) {
                Log.e("FileNotFoundException: ", e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        byte[] bytesSrc = new byte[1024];
        int length;
        while (true) {
            try {
                if (!((length = tmpSrcSubtitleInputStream.read(bytesSrc)) > 0)) break;
            }
            catch (IOException e) {
                Log.e("IOException: ", e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            try {
                savedSrcSubtitleFilesOutputStream.write(bytesSrc, 0, length);
            }
            catch (IOException e) {
                Log.e("IOException: ", e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        try {
            savedSrcSubtitleFilesOutputStream.close();
            tmpSrcSubtitleInputStream.close();
        }
        catch (IOException e) {
            Log.e("IOException: ", e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }


        if (!Objects.equals(LANGUAGE.SRC_CODE, LANGUAGE.DST_CODE)) {
            InputStream tmpDstSubtitleInputStream;
            String dstSubtitleFileDisplayName = tmpDstSubtitleFilePath.substring(tmpDstSubtitleFilePath.lastIndexOf("/") + 1);
            Uri tmpDstSubtitleUri = Uri.fromFile(new File(tmpDstSubtitleFilePath));
            OutputStream savedDstSubtitleFileOutputStream;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ContentValues savedDstSubtitleValues = new ContentValues();
                savedDstSubtitleValues.put(MediaStore.MediaColumns.DISPLAY_NAME, dstSubtitleFileDisplayName); // savedFile name dstSubtitleFileDisplayName required to contain extension savedFile mime
                savedDstSubtitleValues.put(MediaStore.MediaColumns.MIME_TYPE, "*/*");
                savedDstSubtitleValues.put(MediaStore.MediaColumns.RELATIVE_PATH, DIRECTORY_DOCUMENTS + File.separator + getPackageName() + File.separator + subtitleFolderDisplayName);
                Uri extVolumeUri = MediaStore.Files.getContentUri("external");

                if (Environment.isExternalStorageManager()) {
                    String selection = MediaStore.MediaColumns.RELATIVE_PATH + "=?";
                    String[] selectionArgs = new String[]{DIRECTORY_DOCUMENTS + File.separator + getPackageName() + File.separator + subtitleFolderDisplayName + File.separator};
                    @SuppressLint("Recycle") Cursor cursor = getContentResolver().query(extVolumeUri, null, selection, selectionArgs, null);

                    if (cursor.getCount() == 0) {
                        savedDstSubtitleUri = getApplicationContext().getContentResolver().insert(extVolumeUri, savedDstSubtitleValues);
                    } else {
                        while (cursor.moveToNext()) {
                            String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));
                            if (fileName.equals(dstSubtitleFileDisplayName)) {
                                long id = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                                savedDstSubtitleUri = ContentUris.withAppendedId(extVolumeUri, id);
                                break;
                            }
                        }
                        if (savedDstSubtitleUri == null) {
                            savedDstSubtitleUri = getApplicationContext().getContentResolver().insert(extVolumeUri, savedDstSubtitleValues);
                        }
                    }
                    cursor.close();
                }
                else {
                    savedDstSubtitleUri = getApplicationContext().getContentResolver().insert(extVolumeUri, savedDstSubtitleValues);
                }
                try {
                    savedDstSubtitleFileOutputStream = getApplicationContext().getContentResolver().openOutputStream(savedDstSubtitleUri);
                }
                catch (FileNotFoundException e) {
                    Log.e("FileNotFoundException: ", e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }

            } else {
                File root = new File(getExternalStorageDirectory() + File.separator + DIRECTORY_DOCUMENTS + File.separator + getPackageName() + File.separator + subtitleFolderDisplayName);
                if (!root.exists() && root.mkdirs()) {
                    Log.d("saveSubtitleFileToDocumentsDir", root + " created");
                }
                File savedDstSubtitleFile = new File(root, dstSubtitleFileDisplayName);
                Log.d("saveSubtitleFileToDocumentsDir", "savedDstSubtitleFile.getAbsolutePath() = " + savedDstSubtitleFile.getAbsolutePath());
                try {
                    savedDstSubtitleFileOutputStream = new FileOutputStream(savedDstSubtitleFile);
                }
                catch (FileNotFoundException e) {
                    Log.e("FileNotFoundException: ", e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }

            try {
                tmpDstSubtitleInputStream = getApplicationContext().getContentResolver().openInputStream(tmpDstSubtitleUri);
            }
            catch (FileNotFoundException e) {
                Log.e("FileNotFoundException: ", e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            byte[] tmpDstSubtitleBytes = new byte[1024];
            int tmpDstSubtitleLength;
            while (true) {
                try {
                    if (!((tmpDstSubtitleLength = tmpDstSubtitleInputStream.read(tmpDstSubtitleBytes)) > 0))
                        break;
                }
                catch (IOException e) {
                    Log.e("IOException: ", e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
                try {
                    savedDstSubtitleFileOutputStream.write(tmpDstSubtitleBytes, 0, tmpDstSubtitleLength);
                }
                catch (IOException e) {
                    Log.e("IOException: ", e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
            try {
                savedDstSubtitleFileOutputStream.close();
                tmpDstSubtitleInputStream.close();
            }
            catch (IOException e) {
                Log.e("IOException: ", e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return new File(Uri2Path(getApplicationContext(), savedSrcSubtitleUri));
        }
        else {
            return new File(savedFolderPath + File.separator + srcSubtitleFileDisplayName);
        }
    }


    @SuppressLint("Recycle")
    private File saveSubtitleFileToSelectedDir(String tmpSrcSubtitleFilePath, String tmpDstSubtitleFilePath, Uri selectedDirUri) {
        Uri tmpSrcSubtitleUri = Uri.fromFile(new File(tmpSrcSubtitleFilePath));

        Uri tmpDstSubtitleUri = null;
        if (tmpDstSubtitleFilePath != null) {
            tmpDstSubtitleUri = Uri.fromFile(new File(tmpDstSubtitleFilePath));
        }

        InputStream tmpSrcSubtitleInputStream;
        InputStream tmpDstSubtitleInputStream;

        OutputStream savedSrcSubtitleOutputStream = null;
        OutputStream savedDstSubtitleOutputStream = null;

        Uri savedSrcSubtitleUri;
        Uri savedDstSubtitleUri;

        String srcSubtitleFileDisplayName = tmpSrcSubtitleFilePath.substring(tmpSrcSubtitleFilePath.lastIndexOf("/") + 1);
        Log.d("saveSubtitleFileToSelectedDir", "srcSubtitleFileDisplayName = " + srcSubtitleFileDisplayName);

        String dstSubtitleFileDisplayName = null;
        if (tmpDstSubtitleFilePath != null) {
            dstSubtitleFileDisplayName = tmpDstSubtitleFilePath.substring(tmpDstSubtitleFilePath.lastIndexOf("/") + 1);
            Log.d("saveSubtitleFileToSelectedDir", "dstSubtitleFileDisplayName = " + dstSubtitleFileDisplayName);
        }

        DocumentFile selectedDirDocumentFile = DocumentFile.fromTreeUri(TranscribeActivity.this, selectedDirUri);

        DocumentFile savedSrcSubtitleDocumentFile;
        DocumentFile savedDstSubtitleDocumentFile;

        String savedSubtitleFilesPath = null;

        ParcelFileDescriptor srcSubtitleParcelFileDescriptor = null;

        try {
            tmpSrcSubtitleInputStream = getApplicationContext().getContentResolver().openInputStream(tmpSrcSubtitleUri);
        }
        catch (FileNotFoundException e) {
            Log.e("FileNotFoundException: ", e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        if (selectedDirDocumentFile != null) {
            if (!selectedDirDocumentFile.exists()) {
                Log.e("saveSubtitleFileToSelectedDir", selectedDirDocumentFile +  " is not exists");
                releasePermissions(selectedDirUri);
                setText(textview_output_messages_2, selectedDirDocumentFile + " is not exist!");
                return null;
            }
            else {
                savedSrcSubtitleDocumentFile = selectedDirDocumentFile.findFile(srcSubtitleFileDisplayName);
                Log.d("saveSubtitleFileToSelectedDir", "savedSrcSubtitleDocumentFile = " + savedSrcSubtitleDocumentFile);
                if (savedSrcSubtitleDocumentFile == null) savedSrcSubtitleDocumentFile = selectedDirDocumentFile.createFile("*/*", srcSubtitleFileDisplayName);
                if (savedSrcSubtitleDocumentFile != null && savedSrcSubtitleDocumentFile.canWrite()) {
                    savedSrcSubtitleUri = savedSrcSubtitleDocumentFile.getUri();
                    Log.d("saveSubtitleFileToSelectedDir", "subtitleFile.getUri() = " + savedSrcSubtitleDocumentFile.getUri());
                    savedSubtitleFilesPath = Uri2Path(getApplicationContext(), savedSrcSubtitleUri);
                    Log.d("saveSubtitleFileToSelectedDir", "savedSubtitleFilesPath = " + savedSubtitleFilesPath);
                    try {
                        srcSubtitleParcelFileDescriptor = getContentResolver().openFileDescriptor(savedSrcSubtitleUri, "w");
                        savedSrcSubtitleOutputStream = new FileOutputStream(srcSubtitleParcelFileDescriptor.getFileDescriptor());
                    }
                    catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
                else {
                    Log.d("saveSubtitleFileToSelectedDir", srcSubtitleFileDisplayName + " is not exist or cannot write");
                    setText(textview_output_messages_2, "Write error!");
                }
            }
        }

        byte[] bytesSrc = new byte[1024];
        int lengthSrc;
        while (true) {
            try {
                if (!((lengthSrc = tmpSrcSubtitleInputStream.read(bytesSrc)) > 0)) break;
            }
            catch (IOException e) {
                Log.e("IOException: ", e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            try {
                if (savedSrcSubtitleOutputStream != null) {
                    savedSrcSubtitleOutputStream.write(bytesSrc, 0, lengthSrc);
                }
            }
            catch (IOException e) {
                Log.e("IOException: ", e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }

        }

        try {
            if (savedSrcSubtitleOutputStream != null) {
                savedSrcSubtitleOutputStream.close();
            }
            tmpSrcSubtitleInputStream.close();
        }
        catch (IOException e) {
            Log.e("IOException: ", e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        try {
            if (srcSubtitleParcelFileDescriptor != null) {
                srcSubtitleParcelFileDescriptor.close();
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        ParcelFileDescriptor dstSubtitleParcelFileDescriptor = null;

        if (!Objects.equals(LANGUAGE.SRC_CODE, LANGUAGE.DST_CODE) && dstSubtitleFileDisplayName != null) {
            if (selectedDirDocumentFile == null || !selectedDirDocumentFile.exists()) {
                Log.e("saveSubtitleFileToSelectedDir", selectedDirDocumentFile +  " not exists");
                releasePermissions(selectedDirUri);
                setText(textview_output_messages_2, selectedDirDocumentFile + " not exist!");
                return null;
            }
            else {
                savedDstSubtitleDocumentFile = selectedDirDocumentFile.findFile(dstSubtitleFileDisplayName);
                if (savedDstSubtitleDocumentFile == null) savedDstSubtitleDocumentFile = selectedDirDocumentFile.createFile("*/*", dstSubtitleFileDisplayName);
                if (savedDstSubtitleDocumentFile != null && savedDstSubtitleDocumentFile.canWrite()) {
                    savedDstSubtitleUri = savedDstSubtitleDocumentFile.getUri();
                    Log.d("saveSubtitleFileToSelectedDir", "savedDstSubtitleDocumentFile.getUri() = " + savedDstSubtitleDocumentFile.getUri());
                    String savedDstSubtitleFile = Uri2Path(getApplicationContext(), savedDstSubtitleUri);
                    Log.d("saveSubtitleFileToSelectedDir", "savedDstSubtitleFile = " + savedDstSubtitleFile);
                    try {
                        dstSubtitleParcelFileDescriptor = getContentResolver().openFileDescriptor(savedDstSubtitleUri, "w");
                        savedDstSubtitleOutputStream = new FileOutputStream(dstSubtitleParcelFileDescriptor.getFileDescriptor());
                    }
                    catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
                else {
                    Log.d("saveSubtitleFileToSelectedDir", srcSubtitleFileDisplayName + " is not exist or cannot write");
                    setText(textview_output_messages_2, "Write error!");
                }
            }

            try {
                tmpDstSubtitleInputStream = getApplicationContext().getContentResolver().openInputStream(tmpDstSubtitleUri);
            }
            catch (FileNotFoundException e) {
                Log.e("FileNotFoundException: ", e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            byte[] bytesDst = new byte[1024];
            int lengthDst;
            while (true) {
                try {
                    if (!((lengthDst = tmpDstSubtitleInputStream.read(bytesDst)) > 0))
                        break;
                }
                catch (IOException e) {
                    Log.e("IOException: ", e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
                try {
                    if (savedDstSubtitleOutputStream != null) {
                        savedDstSubtitleOutputStream.write(bytesDst, 0, lengthDst);
                    }
                }
                catch (IOException e) {
                    Log.e("IOException: ", e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
            try {
                if (savedDstSubtitleOutputStream != null) {
                    savedDstSubtitleOutputStream.close();
                }
                tmpDstSubtitleInputStream.close();
            }
            catch (IOException e) {
                Log.e("IOException: ", e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            try {
                if (dstSubtitleParcelFileDescriptor != null) {
                    dstSubtitleParcelFileDescriptor.close();
                }
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

        if (savedSubtitleFilesPath != null) {
            Log.d("saveSubtitleFileToSelectedDir", "Succesed! Returned savedSubtitleFilesPath = " + savedSubtitleFilesPath);
            return new File(savedSubtitleFilesPath);
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

        textview_output_messages_2.setGravity(Gravity.START);
        textview_output_messages_2.scrollTo(0,0);

        int lines = textview_output_messages_2.getLineCount();
        Log.d("appendText", "lines = " + lines);
        Log.d("appendText", "maxLinesOfOutputMessages = " + maxLinesOfOutputMessages);
        if (lines >= maxLinesOfOutputMessages) {
            textview_output_messages_2.setGravity(Gravity.BOTTOM);
            Log.d("appendText", "tv.getGravity() = BOTTOM");
        }
        else {
            textview_output_messages_2.setGravity(Gravity.START);
            Log.d("appendText", "tv.getGravity() = START");
        }

        textview_output_messages_2.post(() -> {
            equalMaxChars = calculateMaxCharacterOccurrences(equalChars, textview_output_messages_2);
            dashMaxChars = calculateMaxCharacterOccurrences(dashChars, textview_output_messages_2);
            Log.d("adjustOutputMessagesHeight", "textview_output_messages_2.getWidth() = " + textview_output_messages_2.getWidth());
            Log.d("adjustOutputMessagesHeight", "textview_output_messages_2.getTextSize() = " + textview_output_messages_2.getTextSize());
            Log.d("adjustOutputMessagesHeight", "equalMaxChars = " + equalMaxChars);
            Log.d("adjustOutputMessagesHeight", "dashMaxChars = " + dashMaxChars);
            if (equalMaxChars > 0) {
                equalChars = StringUtils.repeat('=', equalMaxChars - 2);
                Log.d("adjustOutputMessagesHeight", "equalChars = " + equalChars);
                Log.d("adjustOutputMessagesHeight", "equalChars.length() = " + equalChars.length());
                storeStringToFile(TranscribeActivity.this, equalChars, "equalChars");
            }
            if (dashMaxChars > 0) {
                dashChars = StringUtils.repeat('-', dashMaxChars - 4);
                Log.d("adjustOutputMessagesHeight", "dashChars = " + dashChars);
                Log.d("adjustOutputMessagesHeight", "dashChars.length() = " + dashChars.length());
                storeStringToFile(TranscribeActivity.this, dashChars, "dashChars");
            }
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

    public int calculateMaxCharacterOccurrences(String character, TextView textView) {
        int textViewWidth = textView.getWidth();
        float textSize = textView.getTextSize();
        Paint paint = textView.getPaint();

        int maxOccurrences = 0;
        if (textViewWidth > 0 && textSize > 0) {
            float textWidth = paint.measureText(character);

            if (textWidth > 0) {
                int totalCharacters = character.length();
                float averageCharacterWidth = textWidth / totalCharacters;
                maxOccurrences = (int) (textViewWidth / averageCharacterWidth);
            }
        }
        return maxOccurrences;
    }

    public void storeStringToFile(Context context, String data, String fileName) {
        try {
            File directory = new File(context.getExternalFilesDir(null).getAbsolutePath());
            if (!directory.exists() && directory.mkdirs()) {
                Log.d("storeStringToFile", directory + "created");
            }

            File file = new File(directory, fileName);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(data.getBytes());
            fileOutputStream.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readStringFromFile(Context context, String fileName) {
        try {
            File file = new File(context.getExternalFilesDir(null), fileName);
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[(int) file.length()];
            int b = fileInputStream.read(buffer);
            Log.d("readStringFromFile", "b = " + b);
            fileInputStream.close();
            Log.d("readStringFromFile", "new String(buffer) = " + new String(buffer));
            return new String(buffer);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setText(final TextView tv, final String text){
        runOnUiThread(() -> tv.setText(text));
    }

    private void appendText(final TextView tv, final String text){
        runOnUiThread(() -> {
            int lines = tv.getLineCount();
            Log.d("appendText", "lines = " + lines);
            Log.d("appendText", "maxLinesOfOutputMessages = " + maxLinesOfOutputMessages);
            if (lines >= maxLinesOfOutputMessages) {
                tv.setGravity(Gravity.BOTTOM);
                Log.d("appendText", "tv.getGravity() = BOTTOM");
            }
            else {
                Log.d("appendText", "tv.getGravity() = START");
            }
            tv.append(text);
        });
    }


}
