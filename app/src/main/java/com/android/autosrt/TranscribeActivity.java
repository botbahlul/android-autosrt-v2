package com.android.autosrt;

import static android.text.TextUtils.substring;
import static java.lang.Math.round;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
//import android.os.FileUtils;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.OpenableColumns;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.Objects;

public class TranscribeActivity extends AppCompatActivity {

    @SuppressLint("StaticFieldLeak")
    public static TextView textview_src, textview_dst, textview_filepath, textview_debug;
    @SuppressLint("StaticFieldLeak")
    public static Button button_cancel;

    public static Python py;
    public static PyObject pyObjectSRTFileTranslated;
    public static Thread threadTranscriber;
    public static String cancelFile = null;
    public static String srtFileTranslated = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transcribe);
        textview_src = findViewById(R.id.textview_src);
        textview_dst = findViewById(R.id.textview_dst);
        textview_filepath = findViewById(R.id.textview_filepath);
        textview_debug = findViewById(R.id.textview_debug);
        button_cancel = findViewById(R.id.button_cancel);

        textview_src.setText("Voice Language = " + LANGUAGE.SRC_COUNTRY);
        textview_dst.setText("Translation Language = " + LANGUAGE.DST_COUNTRY);
        textview_filepath.setText("filePath = " + FILE.PATH);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.actionbar_title);
        }

        textview_debug.setMovementMethod(new ScrollingMovementMethod());

        cancelFile = getApplicationContext().getExternalFilesDir(null) + File.separator + "cancel.txt";
        File f = new File(cancelFile);
        if (f.exists()) {
            f.delete();
        }

        srtFileTranslated = null;
        if (threadTranscriber != null) {
            threadTranscriber.interrupt();
            try {
                if (pyObjectSRTFileTranslated != null) pyObjectSRTFileTranslated.close();
                threadTranscriber.join();
                threadTranscriber = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        File frs = new File(getApplicationContext().getExternalCacheDir().getAbsoluteFile() + File.separator + "region_start.txt");
        File fet = new File(getApplicationContext().getExternalCacheDir().getAbsoluteFile() + File.separator + "elapsed_time.txt");
        if (frs.exists()) {
            frs.delete();
        }
        if (fet.exists()) {
            fet.delete();
        }

        if (TRANSCRIBE_STATUS.IS_TRANSCRIBING && !CANCEL_STATUS.IS_CANCELING) {
            transcribe();
        }

        button_cancel.setOnClickListener(view -> {
            showConfirmationDialogue();
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                //onBackPressed();
                //break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        showConfirmationDialogue();
        //finish();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    String workPath = "";
    private void transcribe() {
        threadTranscriber = null;
        threadTranscriber = new Thread(() -> {
            if (Looper.myLooper() == null) {
                Looper.prepare();
            }

            Log.d("Current Thread", "Running");
            if (!CANCEL_STATUS.IS_CANCELING) {
                try {
                    // ALTERNATIVE 1 : CREATE A COPY OF ORIGINAL MEDIA FILE
                    /*String srtFolder = substring(FILE.DISPLAY_NAME,0,FILE.DISPLAY_NAME.length()-4);
                    String prefix = "Creating a copy of " + FILE.DISPLAY_NAME + " : ";
                    workPath = copyFileToExternalFilesDir(FILE.URI, srtFolder, prefix);
                    if (workPath != null) {
                        FILE.PATH = workPath;
                    }
                    if (workPath == null) {
                        threadTranscriber.interrupt();
                        threadTranscriber = null;
                        transcribe();
                    }*/

                    if (!Python.isStarted()) {
                        Python.start(new AndroidPlatform(TranscribeActivity.this));
                        py = Python.getInstance();
                    }

                    // ALTERNATIVE 2 : DIRECTLY USE ORIGINAL MEDIA FILE
                    if (FILE.PATH != null) {
                        if (!CANCEL_STATUS.IS_CANCELING) {
                            pyObjectSRTFileTranslated = py.getModule("autosrt").callAttr("transcribe", LANGUAGE.SRC, LANGUAGE.DST, FILE.PATH, FILE.DISPLAY_NAME, TranscribeActivity.this, textview_debug);
                            if (pyObjectSRTFileTranslated != null) {
                                srtFileTranslated = pyObjectSRTFileTranslated.toString();
                            }
                            if (!CANCEL_STATUS.IS_CANCELING && FILE.URI != null && srtFileTranslated != null) {
                                runOnUiThread(() -> {
                                    TRANSCRIBE_STATUS.IS_TRANSCRIBING = false;
                                    CANCEL_STATUS.IS_CANCELING = true;
                                    if (threadTranscriber != null) {
                                        try {
                                            if (pyObjectSRTFileTranslated != null) {
                                                pyObjectSRTFileTranslated.close();
                                                pyObjectSRTFileTranslated = null;
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
                                MainActivity.textview_output.setText("");
                            }

                            else if (CANCEL_STATUS.IS_CANCELING && FILE.URI != null) {
                                runOnUiThread(() -> {
                                    if (threadTranscriber != null) {
                                        threadTranscriber.interrupt();
                                        if (pyObjectSRTFileTranslated != null) {
                                            pyObjectSRTFileTranslated.clear();
                                            pyObjectSRTFileTranslated.close();
                                        }
                                        threadTranscriber = null;
                                    }
                                    TRANSCRIBE_STATUS.IS_TRANSCRIBING = false;
                                    String m = "Process has been canceled\n";
                                    MainActivity.textview_output.setText(m);
                                });
                            }
                        }
                        else {
                            runOnUiThread(() -> {
                                if (threadTranscriber != null) {
                                    threadTranscriber.interrupt();
                                    if (pyObjectSRTFileTranslated != null) {
                                        pyObjectSRTFileTranslated.clear();
                                        pyObjectSRTFileTranslated.close();
                                    }
                                    threadTranscriber = null;
                                }
                                TRANSCRIBE_STATUS.IS_TRANSCRIBING = false;
                                String m = "Process has been canceled\n";
                                MainActivity.textview_output.setText(m);
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
                    MainActivity.textview_output.setText(m);
                    if (threadTranscriber != null) {
                        threadTranscriber.interrupt();
                        try {
                            if (pyObjectSRTFileTranslated != null) {
                                pyObjectSRTFileTranslated.clear();
                                pyObjectSRTFileTranslated.close();
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
                    MainActivity.textview_output.setText(m);

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
                        if (pyObjectSRTFileTranslated != null) {
                            pyObjectSRTFileTranslated.clear();
                            pyObjectSRTFileTranslated.close();
                            pyObjectSRTFileTranslated = null;
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

    @SuppressLint("SetTextI18n")
    private String copyFileToExternalFilesDir(Uri uri, String newDirName, String prefix) {
        @SuppressLint("Recycle") Cursor returnCursor = getApplicationContext().getContentResolver().query(uri, new String[]{
                OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE
        }, null, null, null);

        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String name = (returnCursor.getString(nameIndex));
        String size = (Long.toString(returnCursor.getLong(sizeIndex)));

        File output;
        if (!newDirName.equals("")) {
            File dir = new File(getApplicationContext().getExternalFilesDir(null) + "/" + newDirName);
            if (!dir.exists()) {
                dir.mkdir();
            }
            output = new File(getApplicationContext().getExternalFilesDir(null) + "/" + newDirName + "/" + name);
        } else {
            output = new File(getApplicationContext().getExternalFilesDir(null) + "/" + name);
        }
        try {
            InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(output);
            long length  = Long.parseLong(size);
            long counter = 0;
            int read;
            int bufferSize = 1024;
            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                counter += read;
                outputStream.write(buffers, 0, read);
                //pBar(counter, length, prefix);
                int bar_length = 10;
                int rounded = round(bar_length * counter/(float)(length));
                int filled_up_Length = (int)(rounded);
                float percentage = round(100.0 * counter /(float)(length));
                String pounds = StringUtils.repeat('#', filled_up_Length);
                String equals = StringUtils.repeat('=', (bar_length - filled_up_Length));
                String bar = pounds + equals;
                runOnUiThread(() -> {
                        textview_debug.setText(prefix + " [" + bar + "] " + percentage + '%');
                });
            }
            inputStream.close();
            outputStream.close();

        } catch (Exception e) {
            Log.e("Exception", Objects.requireNonNull(e.getMessage()));
        }
        return output.getPath();
    }

    private static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }

    private static void copyFileUsingChannel(File source, File dest) throws IOException {
        FileChannel sourceChannel = null;
        FileChannel destChannel = null;
        try {
            sourceChannel = new FileInputStream(source).getChannel();
            destChannel = new FileOutputStream(dest).getChannel();
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        }finally{
            sourceChannel.close();
            destChannel.close();
        }
    }

    private static void copyFileUsingApacheCommonsIO(File source, File dest) throws IOException {
        //FileUtils.copyFile(source, dest); // need to import org.apache.commons.io.FileUtils;
            //is = new FileInputStream(source);
        //InputStream is;
        OutputStream os;
        os = new FileOutputStream(dest);
        //FileUtils.copyInputStreamToFile(is, dest);
        FileUtils.copyFile(source, os);
        os.close();
    }

    private static void copyFileUsingJava7Files(File source, File dest) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Files.copy(source.toPath(), dest.toPath());
        }
    }

    /*@RequiresApi(api = Build.VERSION_CODES.Q)
    private static void copyFileUsingAndroidFileUtils(File source, File dest) throws IOException {
        InputStream is;
        OutputStream os;
        is = new FileInputStream(source);
        os = new FileOutputStream(dest);
        FileUtils.copy(is, os);
    }*/


    /*public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (resultCode == RESULT_OK) {
            Uri treeUri = resultData.getData();
            DocumentFile pickedDir = DocumentFile.fromTreeUri(this, treeUri);

            // List all existing files inside picked directory
            for (DocumentFile file : pickedDir.listFiles()) {
                Log.d(TAG, "Found file " + file.getName() + " with size " + file.length());
            }

            // Create a new file and write into it
            DocumentFile newFile = pickedDir.createFile("text/plain", "My Novel");
            OutputStream out = getContentResolver().openOutputStream(newFile.getUri());
            out.write("A long time ago...".getBytes());
            out.close();
        }
    }*/


}
