package com.android.autosrt;

import static android.os.Environment.DIRECTORY_DOCUMENTS;
import static android.os.Environment.getExternalStorageDirectory;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;
import static java.lang.Math.round;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class TranscribeActivity extends AppCompatActivity {

    @SuppressLint("StaticFieldLeak")
    public static TextView textview_isTranscribing, textview_src, textview_dst,
            textview_filepath, textview_current_file,
            textview_output_messages_2, textview_final_results;
    @SuppressLint("StaticFieldLeak")
    public static Button button_cancel;

    public static Python py;
    public static PyObject pyObjectSubtitleFile;
    public static Thread threadTranscriber;
    public static String cancelFile = null;
    public static String subtitleFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transcribe);
        textview_isTranscribing = findViewById(R.id.textview_isTranscribing);
        textview_src = findViewById(R.id.textview_src);
        textview_dst = findViewById(R.id.textview_dst);
        textview_filepath = findViewById(R.id.textview_filepath);
        textview_current_file = findViewById(R.id.textview_current_file);
        textview_output_messages_2 = findViewById(R.id.textview_output_messages_2);
        textview_final_results = findViewById(R.id.textview_final_results);
        button_cancel = findViewById(R.id.button_cancel);

        String it = "TRANSCRIBE_STATUS.IS_TRANSCRIBING = " + TRANSCRIBE_STATUS.IS_TRANSCRIBING;
        runOnUiThread(() -> textview_isTranscribing.setText(it));

        if(MainActivity.checkbox_debug_mode.isChecked()){
            textview_isTranscribing.setVisibility(View.VISIBLE);
        }
        else {
            textview_isTranscribing.setVisibility(View.GONE);
        }

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
        textview_final_results.setTextIsSelectable(true);

        textview_filepath.setSelected(true);
        textview_output_messages_2.setSelected(true);
        textview_final_results.setSelected(true);

        textview_filepath.setMovementMethod(new ScrollingMovementMethod());
        textview_output_messages_2.setMovementMethod(new ScrollingMovementMethod());
        textview_final_results.setMovementMethod(new ScrollingMovementMethod());

        cancelFile = getApplicationContext().getExternalFilesDir(null) + File.separator + "cancel.txt";
        File f = new File(cancelFile);
        if (f.exists() && f.delete()) {
            Log.d("cancelFile", "deleted");
        }

        subtitleFile = null;
        if (threadTranscriber != null) {
            threadTranscriber.interrupt();
            try {
                if (pyObjectSubtitleFile != null) pyObjectSubtitleFile.close();
                threadTranscriber.join();
                threadTranscriber = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        if (TRANSCRIBE_STATUS.IS_TRANSCRIBING && !CANCEL_STATUS.IS_CANCELING) {
            transcribe();
        }
        button_cancel.setOnClickListener(view -> showConfirmationDialogue());

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
        threadTranscriber = null;
        threadTranscriber = new Thread(() -> {
            if (Looper.myLooper() == null) {
                Looper.prepare();
            }

            Log.d("Current Thread", "Running");
            if (!CANCEL_STATUS.IS_CANCELING) {
                try {
                    if (!Python.isStarted()) {
                        Python.start(new AndroidPlatform(TranscribeActivity.this));
                        py = Python.getInstance();
                    }

                    if (FILE.PATH_LIST != null) {
                        if (!CANCEL_STATUS.IS_CANCELING) {
                            runOnUiThread(() -> textview_final_results.setText(""));

                            File wavDir = getApplicationContext().getCacheDir();
                            File[] tmpWavFile = new File[FILE.URI_LIST.size()];
                            int[] wavFileSize = new int[FILE.URI_LIST.size()];

                            for (int i = 0; i < FILE.PATH_LIST.size(); i++) {
                                String cfp = "Processing file : " + FILE.DISPLAY_NAME_LIST.get(i);
                                runOnUiThread(() -> textview_current_file.setText(cfp));

                                tmpWavFile[i] = null;
                                try {
                                    tmpWavFile[i] = File.createTempFile("temp", ".wav", wavDir);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }

                                wavFileSize[i] = 0;
                                int channels = 1;
                                int rate = 16000;
                                Config.enableRedirection();
                                wavFileSize[i] = convertToWav(FILE.PATH_LIST.get(i), tmpWavFile[i], channels, rate);
                                Config.disableRedirection();

                                if (wavFileSize[i] != 0) {
                                    pyObjectSubtitleFile = py.getModule("autosrt").callAttr(
                                            "transcribe",
                                            LANGUAGE.SRC_CODE, LANGUAGE.DST_CODE, FILE.PATH_LIST.get(i), FILE.DISPLAY_NAME_LIST.get(i), tmpWavFile[i].toString(), SUBTITLE.FORMAT, TranscribeActivity.this, textview_output_messages_2);
                                    if (pyObjectSubtitleFile != null) {
                                        String subtitleFile = pyObjectSubtitleFile.toString();
                                        SUBTITLE.FILE_PATH_LIST.add(subtitleFile);
                                        String translatedSubtitleFile = StringUtils.substring(subtitleFile, 0, subtitleFile.length() - 4) + ".translated." + SUBTITLE.FORMAT;
                                        SUBTITLE.TRANSLATED_FILE_PATH_LIST.add(translatedSubtitleFile);
                                        saveSubtitleFileToDocumentsDir(FILE.DISPLAY_NAME_LIST.get(i), subtitleFile);
                                    }
                                }

                            }
                            textview_final_results.setGravity(Gravity.BOTTOM);

                            if (!CANCEL_STATUS.IS_CANCELING && FILE.PATH_LIST != null && SUBTITLE.FILE_PATH_LIST != null) {
                                runOnUiThread(() -> {
                                    TRANSCRIBE_STATUS.IS_TRANSCRIBING = false;
                                    CANCEL_STATUS.IS_CANCELING = true;
                                    if (threadTranscriber != null) {
                                        try {
                                            if (pyObjectSubtitleFile != null) {
                                                pyObjectSubtitleFile.close();
                                                pyObjectSubtitleFile = null;
                                            }
                                            threadTranscriber.join();
                                            threadTranscriber = null;
                                        } catch (InterruptedException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                    String t = "Done";
                                    button_cancel.setText(t);
                                });
                            }

                            else if (CANCEL_STATUS.IS_CANCELING && FILE.PATH_LIST != null) {
                                runOnUiThread(() -> {
                                    if (threadTranscriber != null) {
                                        threadTranscriber.interrupt();
                                        if (pyObjectSubtitleFile != null) {
                                            pyObjectSubtitleFile.clear();
                                            pyObjectSubtitleFile.close();
                                        }
                                        threadTranscriber = null;
                                    }
                                    TRANSCRIBE_STATUS.IS_TRANSCRIBING = false;
                                    String m = "Process has been canceled\n";
                                    MainActivity.textview_output_messages_1.setText(m);
                                });
                            }
                        }
                        else {
                            runOnUiThread(() -> {
                                if (threadTranscriber != null) {
                                    threadTranscriber.interrupt();
                                    if (pyObjectSubtitleFile != null) {
                                        pyObjectSubtitleFile.clear();
                                        pyObjectSubtitleFile.close();
                                    }
                                    threadTranscriber = null;
                                }
                                TRANSCRIBE_STATUS.IS_TRANSCRIBING = false;
                                String m = "Process has been canceled\n";
                                MainActivity.textview_output_messages_1.setText(m);
                            });
                        }
                    }

                } catch (Exception e) {
                    Log.e("Error: ", Objects.requireNonNull(e.getMessage()));
                    e.printStackTrace();
                }

            }
            else {
                runOnUiThread(() -> {
                    String m = "Process has been canceled\n";
                    MainActivity.textview_output_messages_1.setText(m);
                    if (threadTranscriber != null) {
                        threadTranscriber.interrupt();
                        try {
                            if (pyObjectSubtitleFile != null) {
                                pyObjectSubtitleFile.clear();
                                pyObjectSubtitleFile.close();
                            }
                            threadTranscriber.join();
                            threadTranscriber = null;
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    TRANSCRIBE_STATUS.IS_TRANSCRIBING = false;
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

            builder.setPositiveButton("YES", (dialog, which) -> {
                CANCEL_STATUS.IS_CANCELING = true;
                runOnUiThread(() -> {
                    String m = "Process has been canceled\n";
                    MainActivity.textview_output_messages_1.setText(m);

                    File fc = new File(cancelFile);
                    try {
                        FileWriter out = new FileWriter(fc);
                        out.write("true");
                        out.close();
                    } catch (IOException e) {
                        Log.e("Error: ", Objects.requireNonNull(e.getMessage()));
                        e.printStackTrace();
                    }

                    if (threadTranscriber != null) {
                        threadTranscriber.interrupt();
                        if (pyObjectSubtitleFile != null) {
                            pyObjectSubtitleFile.clear();
                            pyObjectSubtitleFile.close();
                            pyObjectSubtitleFile = null;
                        }
                        threadTranscriber = null;
                    }
                    TRANSCRIBE_STATUS.IS_TRANSCRIBING = false;
                    CANCEL_STATUS.IS_CANCELING = true;
                    finish();
                    dialog.dismiss();
                });
            });

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

    private void saveSubtitleFileToDocumentsDir(String fileDisplayName, String subtitleFilePath) {
        OutputStream outputStream;
        String subtitleFileDisplayName = subtitleFilePath.substring(subtitleFilePath.lastIndexOf("/")+1);
        String subtitleFolder = StringUtils.substring(subtitleFileDisplayName,0,subtitleFileDisplayName.length()-4);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, subtitleFileDisplayName); // file name subtitleFileDisplayName required to contain extension file mime
            values.put(MediaStore.MediaColumns.MIME_TYPE, "*/*");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, DIRECTORY_DOCUMENTS + File.separator + getPackageName() + File.separator + subtitleFolder);
            Uri extVolumeUri = MediaStore.Files.getContentUri("external");
            Uri fileUri = getApplicationContext().getContentResolver().insert(extVolumeUri, values);
            try {
                outputStream = getApplicationContext().getContentResolver().openOutputStream(fileUri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        else {
            File root = new File(Environment.getExternalStorageDirectory() + File.separator + DIRECTORY_DOCUMENTS + File.separator + getPackageName() + File.separator + subtitleFolder);
            if (!root.exists() && root.mkdirs()) {
                Log.d(root.toString(), "created");
            }
            File file = new File(root, subtitleFileDisplayName);
            Log.d("saveSubtitleFileToDocumentsDir", "saveFile: file path - " + file.getAbsolutePath());
            try {
                outputStream = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        Uri uri = Uri.fromFile(new File(subtitleFilePath));
        InputStream inputStream;
        try {
            inputStream = getApplicationContext().getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
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
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String translatedSubtitleFileDisplayName = StringUtils.substring(subtitleFileDisplayName, 0, subtitleFileDisplayName.length() - 4) + ".translated." + SUBTITLE.FORMAT;
        if (!Objects.equals(LANGUAGE.SRC_CODE, LANGUAGE.DST_CODE)) {
            OutputStream outputStreamTranslated;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, translatedSubtitleFileDisplayName); // file name avedTanslatedsubtitleFilePath required to contain extension file mime
                values.put(MediaStore.MediaColumns.MIME_TYPE, "*/*");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, DIRECTORY_DOCUMENTS + File.separator + getPackageName() + File.separator + subtitleFolder);
                Uri extVolumeUri = MediaStore.Files.getContentUri("external");
                Uri fileUri = getApplicationContext().getContentResolver().insert(extVolumeUri, values);
                try {
                    outputStreamTranslated = getApplicationContext().getContentResolver().openOutputStream(fileUri);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            } else {
                File root = new File(Environment.getExternalStorageDirectory() + File.separator + DIRECTORY_DOCUMENTS + File.separator + getPackageName() + File.separator + subtitleFolder);
                if (!root.exists() && root.mkdirs()) {
                    Log.d(root.toString(), "created");
                }
                File file = new File(root, translatedSubtitleFileDisplayName);
                Log.d("saveSubtitleFileToDocumentsDir", "saveFile: file path - " + file.getAbsolutePath());
                try {
                    outputStreamTranslated = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
            String translatedSubtitleFilePath = StringUtils.substring(subtitleFilePath, 0, subtitleFilePath.length() - 4) + ".translated." + SUBTITLE.FORMAT;
            Uri uriTranslated = Uri.fromFile(new File(translatedSubtitleFilePath));
            InputStream inputStreamTranslated;
            try {
                inputStreamTranslated = getApplicationContext().getContentResolver().openInputStream(uriTranslated);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            byte[] bytesTranslated = new byte[1024];
            int lengthTranslated;
            while (true) {
                try {
                    if (!((lengthTranslated = inputStreamTranslated.read(bytesTranslated)) > 0))
                        break;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                try {
                    outputStreamTranslated.write(bytesTranslated, 0, lengthTranslated);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                outputStreamTranslated.close();
                inputStreamTranslated.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        String s = "Saved subtitle files for " + fileDisplayName + " : \n";
        runOnUiThread(() -> textview_final_results.append(s));
        String savedFolderPath = getExternalStorageDirectory() + File.separator + DIRECTORY_DOCUMENTS + File.separator + getPackageName() + File.separator + subtitleFolder;
        String sf = savedFolderPath + File.separator + subtitleFileDisplayName + "\n";
        runOnUiThread(() -> textview_final_results.append(sf));
        if (!Objects.equals(LANGUAGE.SRC_CODE, LANGUAGE.DST_CODE)) {
            String tsf = savedFolderPath + File.separator + translatedSubtitleFileDisplayName + "\n\n";
            runOnUiThread(() -> textview_final_results.append(tsf));
        }
    }

    private int convertToWav(String filePath, File tmpWavFile, int channels, int rate) {
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
    }

    @SuppressLint("SetTextI18n")
    public void pBar(float counter, float total, String prefix) {
        int bar_length = 10;
        int rounded = round(bar_length * counter/(float)total);
        int filled_up_Length = (int)(rounded);
        int percentage = round(100 * counter/(float)total);
        String pounds = StringUtils.repeat('█', filled_up_Length);
        String equals = StringUtils.repeat('-', (bar_length - filled_up_Length));
        String bar = pounds + equals;
        runOnUiThread(() -> textview_output_messages_2.setText(prefix + " |" + bar + "| " + percentage + '%'));
    }


}
