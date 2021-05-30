package com.example.root.word_timer;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import android.provider.Settings.Secure;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import examples.Main;

public class MainActivity extends AppCompatActivity{
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;
    Button b_start,b_word0;
    TextView userName_tv, loggedIn_tv;
    long tStart=0,tw0Start,tw0Stop;
    long last_pressed;
    boolean minimized_flag = false;
    File words_file;
    Menu myMenu;
    ArrayList<String> Hotwords;
    int numberOfWords = 0, disp_word;
    String[] Default_WordList={"Hey Nino","Hey Chemo","Hey Rico","Hey Nina","Hey Cilo", "Hey Leo", "Hey Nino"};
    ArrayList<String> TriggerNames=new ArrayList<>();
    ArrayList<Long> TriggerTimesStart=new ArrayList<>();
    ArrayList<Long> TriggerTimesEnd=new ArrayList<>();
    File AudioRecordingsDir;
    private String android_id, path2save = "default";
    private String destinationPath = Environment.getExternalStorageDirectory() + "/zip/" ;
    String current_ts="", u_name, u_age, u_gender, u_ID, u_country;
    private int count = 0;
    private long startMillis=0;
    boolean isRandom = true;

    FileFunctions FHelp = new FileFunctions();
    private Random r = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        b_start = findViewById(R.id.b_start);
        b_word0 = findViewById(R.id.b_word0);
        b_word0.setEnabled(false);
        userName_tv = findViewById(R.id.tv_userName);
        loggedIn_tv = findViewById(R.id.tv_loggedIn);

        Bundle info = getIntent().getExtras();
        u_name = info.getString("u_name");
        u_ID = info.getString("u_ID");
        u_gender = info.getString("u_gender");
        u_age = info.getString("u_age");
        u_country = info.getString("u_country");
        userName_tv.setText(u_name);

        if(isOnline())
            FHelp.download_word_list(MainActivity.this);

        while(FHelp.downloading_wordlist){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        String raw_file = Environment.getExternalStorageDirectory() + "/AudioRecordings/record_temp.raw" ;
        File myFile = new File(raw_file);
        if(myFile.exists())
            myFile.delete();

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mEditor = mPreferences.edit();

        android_id = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);
        FHelp.android_id = android_id;

        words_file = new File(Environment.getExternalStorageDirectory().getPath()+"/wordlist.txt");

        if(words_file.exists()){
            new update_wordlist().execute();
        }
        else{
            String words = mPreferences.getString("word_list","");
            if(!words.equals("")){
                Hotwords = new ArrayList<>(Arrays.asList(words.split(",")));
                path2save = mPreferences.getString("path2save","");
            }
            else
            {
                Hotwords = new ArrayList<String>(Arrays.asList(Default_WordList)); ;
            }
            numberOfWords = Hotwords.size();
            initialize_word();
        }

        AudioRecordingsDir = new File(Environment.getExternalStorageDirectory().getPath()+"/AudioRecordings/");

        if(!AudioRecordingsDir.exists()) {
            AudioRecordingsDir.mkdirs();
        }

        loggedIn_tv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                long time= System.currentTimeMillis();

                if (startMillis==0 || (time-startMillis> 2000) ) {
                    startMillis=time;
                    count=1;
                }
                else{
                    count++;
                }

                if (count==5) {
                    if(isRandom) {
                        isRandom = false;
                        disp_word = 0;
                    }
                    else
                        isRandom = true;

                    Toast.makeText(MainActivity.this, "Random = " + isRandom, Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

        b_word0.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        tw0Start=System.currentTimeMillis()-tStart;
                        break;
                    case MotionEvent.ACTION_UP:
                        tw0Stop=System.currentTimeMillis()-tStart;
                        TriggerNames.add(b_word0.getText().toString().toLowerCase().replace(" ","_"));
                        TriggerTimesStart.add(tw0Start);
                        TriggerTimesEnd.add(tw0Stop);
                        last_pressed=tw0Stop;
                        initialize_word();
                        Log.d("tw0:","Start:"+Long.toString(tw0Start)+",Stop:"+Long.toString(tw0Stop));

                        break;
                }
                return false;
            }
        });

        b_word0.setEnabled(false);
    }

    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isAvailable()) {
            return true;
        } else {
            return false;
        }
    }

    public void save_triggers(){
        String trigtxt=android_id+"_"+current_ts+":";
        for(int i=0;i<TriggerNames.size();i++){
            trigtxt+=Long.toString(TriggerTimesStart.get(i));
            trigtxt+="-";
            trigtxt+=Long.toString(TriggerTimesEnd.get(i));
            trigtxt+="[";
            trigtxt+=TriggerNames.get(i);
            trigtxt+="]";
            if(i!=TriggerNames.size()-1){
                trigtxt+=",";
            }
            else{
                trigtxt+=";";
            }
        }

        File tfile = new File(AudioRecordingsDir, android_id+"_"+current_ts+"_triggers.txt");
        if(!tfile.exists()){
            try {
                tfile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileOutputStream fOut = new FileOutputStream(tfile, true);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);
            osw.write(trigtxt);
            osw.flush();
            osw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        File userDetails = new File(AudioRecordingsDir, android_id+"_"+current_ts+"_user_details.txt");
        if(!userDetails.exists()){
            try {
                userDetails.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileOutputStream fOut = new FileOutputStream(userDetails, true);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);
            osw.write(android_id+"_"+current_ts + "\nName: " + u_name + "\nEmail ID: " + u_ID + "\nAge: " + u_age + "\nGender: " + u_gender + "\nCountry: " + u_country);
            osw.flush();
            osw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initialize_word(){
        if(isRandom)
            b_word0.setText(Hotwords.get(r.nextInt(numberOfWords)));
        else
            b_word0.setText(Hotwords.get((disp_word++)%numberOfWords));
    }

    public void b_start_click(View v){
        b_word0.setEnabled(true);
        b_start.setEnabled(false);
        myMenu.findItem(R.id.upload).setEnabled(false);
        myMenu.findItem(R.id.about).setEnabled(false);
        myMenu.findItem(R.id.change_user).setEnabled(false);
        new RecorderTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"");
    }

    private class RecorderTask extends AsyncTask<String, Long, String> {
        WavRecorder wavRecorder;
        long last_round_time=0;
        private ProgressDialog dialog=new ProgressDialog(MainActivity.this);
        long waitTime=2000;


        @Override
        protected String doInBackground(String... params) {
            dialog.setCancelable(false);
            TriggerNames.clear();
            TriggerTimesStart.clear();
            TriggerTimesEnd.clear();
            Long currentTime=System.currentTimeMillis();
            current_ts=currentTime.toString();
            wavRecorder = new WavRecorder(AudioRecordingsDir+"/"+android_id+"_"+current_ts+".wav");
            wavRecorder.startRecording();
            tStart = System.currentTimeMillis();
            long tElapsed=0;
            last_pressed=-2000;
            while(tElapsed<33000 && !minimized_flag){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                tElapsed=System.currentTimeMillis()-tStart;
                publishProgress(tElapsed);
            }
            return "";
        }

        @Override
        protected void onProgressUpdate(Long... values) {
            super.onProgressUpdate(values);
            long tElapsed = values[0];
            if (tElapsed - last_round_time > 1000) {
                last_round_time += 1000;
            }
            int t_s = 33 - (int) ((double) (last_round_time) / 1000.0);
            b_start.setText(Integer.toString(t_s));

            if(tElapsed<27000) {
                if (tElapsed - last_pressed < waitTime) {
                    b_word0.setEnabled(false);
                    dialog.setMessage("Please Wait " + Double.toString((waitTime - (tElapsed - last_pressed)) / 1000.0) + "seconds");
                    if (!dialog.isShowing()) {
                        dialog.show();
                        if(isRandom)
                            waitTime = (r.nextInt(9) * 1000) + 1000;
                        else
                            waitTime = (r.nextInt(3) * 1000) + 1000;
                    }
                } else {
                    b_word0.setEnabled(true);
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }
            }
            else{
                b_word0.setEnabled(false);
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }

        @Override
        protected void onPostExecute(String res) {
            if(!minimized_flag) {
                wavRecorder.stopRecording();
                save_triggers();
            }

            initialize_word();

            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            b_word0.setEnabled(false);
            b_start.setText("START");
            b_start.setEnabled(true);
            myMenu.findItem(R.id.upload).setEnabled(true);
            myMenu.findItem(R.id.change_user).setEnabled(true);
            myMenu.findItem(R.id.about).setEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        myMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.upload) {
            File toDelete = new File(destinationPath);
            if (toDelete.isDirectory()) {
                String[] children = toDelete.list();
                for (int i = 0; i < children.length; i++) {
                    new File(toDelete, children[i]).delete();
                }
                toDelete.delete();
            }
            if(isOnline()){
                FHelp.zip_files(path2save);
                myMenu.findItem(R.id.change_user).setEnabled(true);
            }
            else
                Toast.makeText(this, "Please check your INTERNET connection and try again", Toast.LENGTH_SHORT).show();
        }
        if (item.getItemId() == R.id.about) {
            AlertDialog.Builder aboutDialog = new AlertDialog.Builder(this);
            aboutDialog.setTitle("About");
            aboutDialog.setMessage("App Version: 1.4\n\nDeveloped by Sirena Technologies");
            aboutDialog.setIcon(R.drawable.app_launcher);
            aboutDialog.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            aboutDialog.show();
        }
        if(item.getItemId() == R.id.change_user){
            Intent userSelect = new Intent(this, user_select.class);
            startActivity(userSelect);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("Change User?");
        alertDialog.setMessage("Are you sure you want to change the user?");
        alertDialog.setIcon(R.drawable.app_launcher);
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent userSelect = new Intent(MainActivity.this, user_select.class);
                startActivity(userSelect);
                finish();
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        alertDialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    @Override
    public void onStop() {
        minimized_flag = true;
        super.onStop();
    }

    @Override
    public void onResume(){
        minimized_flag=false;
        super.onResume();
    }

    private class update_wordlist extends AsyncTask<String, String, String>{
        @Override
        public String doInBackground(String... params){
            try {
                Hotwords = new ArrayList<>();
                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/wordlist.txt";
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(words_file)));
                String line;
                line = reader.readLine();
                path2save = line;

                while (true) {
                    line = reader.readLine();
                    if (line == null)
                        break;
                    Hotwords.add(line);
                }

                numberOfWords = Hotwords.size();
                reader.close();

            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String str){
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < numberOfWords; i++) {
                sb.append(Hotwords.get(i)).append(",");
            }
            mEditor.putString("word_list", sb.toString());
            mEditor.commit();
            mEditor.putString("path2save",path2save);
            mEditor.commit();

            initialize_word();

            File toDelete1 = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/wordlist.txt");
            if (toDelete1.isDirectory()) {
                String[] children = toDelete1.list();
                for (int i = 0; i < children.length; i++) {
                    new File(toDelete1, children[i]).delete();
                }
            }
            toDelete1.delete();
        }
    }
}
