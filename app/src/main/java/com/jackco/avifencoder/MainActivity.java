package com.jackco.avifencoder;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    Long time;
    private int fileRequestCode = 23;
    String rawFileP = null;
    ProgressBar pro = null;
    Button button2 = null;
    int width = 0;
    int height = 0;
    TextView text2 = null;
    String tofilename = "";

    int threads = 8;
    int qua1 = 50;
    int qua2 = 60;

    int speed = 10;

    SeekBar s;
    SeekBar s2;
    SeekBar ss;

    TextView textq;
    TextView textt;
    TextView texts;

    String encodedIn = null;

    private int getNumOfCores()
    {
        try
        {
            int i = Objects.requireNonNull(new File("/sys/devices/system/cpu/").listFiles(new FileFilter() {
                public boolean accept(File params) {
                    return Pattern.matches("cpu[0-9]", params.getName());
                }
            })).length;
            return i;
        }
        catch (Exception ignored)
        {
        }
        return 1;
    }

    private class AsyncCreateAVIF extends AsyncTask<String, String, Bitmap> {


        @Override
        protected Bitmap doInBackground(String... filePath) {
            return createAVIF(filePath[0]);
        }

        @Override
        protected void onPreExecute() {
            button2.setVisibility(View.GONE);
            pro.setVisibility(View.VISIBLE);
            text2.setText("Encoding to AVIF");
            time = System.currentTimeMillis();
            s.setVisibility(View.GONE);
            s2.setVisibility(View.GONE);
            ss.setVisibility(View.GONE);
            textq.setVisibility(View.GONE);
            textt.setVisibility(View.GONE);
            texts.setVisibility(View.GONE);
            textt.setVisibility(View.GONE);

        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            button2.setVisibility(View.VISIBLE);
            pro.setVisibility(View.GONE);
            s.setVisibility(View.VISIBLE);
            s2.setVisibility(View.VISIBLE);
            ss.setVisibility(View.VISIBLE);
            textq.setVisibility(View.VISIBLE);
            textt.setVisibility(View.VISIBLE);
            texts.setVisibility(View.VISIBLE);

            File file = new File(getDataDir()+  "/" + rawFileP );
            if(file.exists()){
                Log.e("l", "Deleting temp file");
                file.delete();
            }

            Log.e("DONE","POST EXECUTE!");
            Log.e("DONE","Time: " + (System.currentTimeMillis() - time));
            encodedIn = "Encoded in " + (System.currentTimeMillis() - time) + " milliseconds";

            String fileName = "b.avif";

            try{
                File f = new File(getDataDir() + "/" + fileName);
                if(f.exists()) {
                    Log.e("OK", "FILE OK");
                }
                else{
                    text2.setText("Encoding Failed....");
                    if(f.exists()) f.delete();
                    return;
                }
                InputStream i = new FileInputStream(f);
                File t = new File(getExternalFilesDir(null).getAbsolutePath() + "/" + tofilename);
                copy(i, t);
                if(f.exists()) f.delete();

                String sizecomp = "AVIF size " + t.length()/1024 + " KB";


                if(t.exists()) text2.setText("AVIF file saved to:\n\n " +getExternalFilesDir(null).getAbsolutePath() + "/" + tofilename +"\n\n"+encodedIn + "\n" + sizecomp);


            }
            catch (Exception e) {
                Log.e("ERROR", e.toString());
            }
        }
    }


    public static void copy(InputStream in, File dst) throws IOException {
        try (OutputStream out = new FileOutputStream(dst)) {
            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == fileRequestCode && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = null;
            uri = data.getData();

            try {
                String filePath = uri.getPath();
                Log.e("PATH", filePath);

                Bitmap bitmap_1 = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                ImageView image1 = findViewById(R.id.image1);

                Bitmap bitmap_f = bitmap_1.copy(Bitmap.Config.ARGB_8888,false);

                Log.e("BUF", getDataDir()+  "/" + rawFileP );

                image1.setImageBitmap(bitmap_f);

                SimpleDateFormat formatter = new SimpleDateFormat("_dd_MM_yyyy_HH_mm_ss");
                Date date = new Date();

                tofilename = filePath.replace("/","");
                tofilename += formatter.format(date);
                tofilename = tofilename.replace(".","");
                tofilename = tofilename.replace(":","_");
                tofilename += ".avif";


                File file = new File(getDataDir().getAbsolutePath()+ "/" +rawFileP);
                if(file.exists()) file.delete();
                FileOutputStream fo = new FileOutputStream(file);

                int x = 0;
                int y = 0;

                width = bitmap_f.getWidth();
                height = bitmap_f.getHeight();

                int size = bitmap_f.getRowBytes() * bitmap_f.getHeight();

                byte[] byteArray;

                ByteBuffer byteBuffer = ByteBuffer.allocate(size);
                bitmap_f.copyPixelsToBuffer(byteBuffer);

                byteArray = byteBuffer.array();

                fo.write(byteArray);



                Log.e("DONE","DONE!");

                AsyncCreateAVIF asyncTask=new AsyncCreateAVIF();
                asyncTask.execute("");



            } catch (Exception e) {
            }
        }
    }



    private String execCmd(String cmd, String[] envp) throws java.io.IOException {
        Log.e("EXECUTING", cmd);

        Process proc = null;
        proc = Runtime.getRuntime().exec(cmd, envp);
        java.io.InputStream is = proc.getInputStream();
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        String val = "";
        if (s.hasNext()) {
            val = s.next();
            Log.e("Result",val);
        }
        else {
            val = "";
        }
        return val;
    }

    Bitmap createAVIF(String filePath) {
        Log.e("Log","openAVIF() ");
        String ev = getApplicationInfo().nativeLibraryDir;
        String[] envp = {"LD_LIBRARY_PATH=" + ev};

        String name = getApplicationInfo().nativeLibraryDir + "/libavif_example1.so " + getDataDir()+ "/" + rawFileP + " " + getDataDir() + "/b.avif" + " " + width + " " + height +" " + threads + " " + qua1 +  " " + qua2 +  " " + speed;


        String res = null;
        try {
            res = execCmd(name, envp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.e("CORES", String.valueOf(getNumOfCores()));

        text2 = findViewById(R.id.text2);
        text2.setText("Ready");

        texts = findViewById((R.id.texts));

        rawFileP = "decoded.raw";

        pro = findViewById(R.id.progress1);

        pro.setVisibility(View.GONE);

        button2 = findViewById(R.id.button2);

        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setType("*/*");
                startActivityForResult(intent,fileRequestCode);
            }
        });


        ImageButton buttonA = findViewById(R.id.buttona);

        buttonA.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Dialog licenseDialog = new Dialog(MainActivity.this);
                licenseDialog.setContentView(R.layout.about_layout);

                Window window = licenseDialog.getWindow();
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                window.setGravity(Gravity.CENTER);

                licenseDialog.show();
            }
        });

        textq = findViewById(R.id.textq);

        s = findViewById(R.id.seekq);


        s.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar,int progress, boolean fromUser) {
                qua1 = 63 - progress;
                qua2 = qua1 + 10;

                if(qua2 > 63) qua2 = 63;
                if(qua1 == 0) qua2 = 0;
                String qu = String.format("Quality: %s", progress);
                textq.setText(qu);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        ss = findViewById(R.id.seeks);

        ss.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar,int progress, boolean fromUser) {
                speed = progress + 1;
                String sp = String.format("Encoding Speed: %s", speed);
                texts.setText(sp);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        textt = findViewById(R.id.textt);

        threads = getNumOfCores();
        String thr = String.format("Threads: %s", threads);
        textt.setText(thr);


        s2 = findViewById(R.id.seek2);
        s2.setMax(getNumOfCores() - 1);
        s2.setProgress(threads-1);
        s2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar,int progress, boolean fromUser) {
                threads = progress+1;
                String thr = String.format("Threads: %s", threads);
                textt.setText(thr);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}