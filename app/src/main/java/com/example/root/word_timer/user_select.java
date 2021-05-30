package com.example.root.word_timer;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class user_select extends AppCompatActivity {

    ListView userList;
    List<String> UserNames;
    List<String> UserIDs;
    List<String> UserGender;
    List<String> UserAge;
    List<String> UserCountries;
    List<String> active_user_details;
    String u_names, u_ids, u_gender, u_age,u_countries;
    setDeviceList setIt;
    Button new_user;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_select);  //A32330

        userList = findViewById(R.id.list);
        new_user = findViewById(R.id.add_user);
        final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipelayout);

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},PackageManager.PERMISSION_GRANTED);
        }

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mEditor = mPreferences.edit();

        u_names = mPreferences.getString("user_names","");
        UserNames = new ArrayList<>(Arrays.asList(u_names.split(",")));

        u_ids = mPreferences.getString("user_IDs","");
        UserIDs = new ArrayList<>(Arrays.asList(u_ids.split(",")));

        u_gender = mPreferences.getString("user_gender","");
        UserGender = new ArrayList<>(Arrays.asList(u_gender.split(",")));

        u_age = mPreferences.getString("user_age","");
        UserAge = new ArrayList<>(Arrays.asList(u_age.split(",")));

        u_countries = mPreferences.getString("user_countries","");
        UserCountries = new ArrayList<>(Arrays.asList(u_countries.split(",")));

        setIt = new setDeviceList();
        userList.setAdapter(setIt);

        userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent mainActivity = new Intent(user_select.this,MainActivity.class);
                Bundle info = new Bundle();
                info.putString("u_name",UserNames.get(i));
                info.putString("u_ID",UserIDs.get(i));
                info.putString("u_gender",UserGender.get(i));
                info.putString("u_age",UserAge.get(i));
                info.putString("u_country",UserCountries.get(i));
                mainActivity.putExtras(info);
                startActivity(mainActivity);
                finish();
            }
        });

        new_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userDetails = new Intent(user_select.this, UserDetails.class);
                Bundle info = new Bundle();
                info.putString("u_names",u_names);
                info.putString("u_IDs",u_ids);
                info.putString("u_genders",u_gender);
                info.putString("u_ages",u_age);
                info.putString("u_countries",u_countries);
                userDetails.putExtras(info);
                startActivity(userDetails);
                finish();
            }
        });

        userList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(user_select.this);
                alertDialog.setTitle("Delete User?");
                alertDialog.setMessage("Are you sure you want to Delete this user?");
                alertDialog.setIcon(R.drawable.app_launcher);
                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        UserNames.remove(position);
                        UserIDs.remove(position);
                        UserGender.remove(position);
                        UserAge.remove(position);
                        UserCountries.remove(position);
                        userList.setAdapter(null);
                        userList.setAdapter(setIt);

                        mEditor.putString("user_names",TextUtils.join(",",UserNames)).commit();
                        mEditor.putString("user_IDs",TextUtils.join(",",UserIDs)).commit();
                        mEditor.putString("user_gender",TextUtils.join(",",UserGender)).commit();
                        mEditor.putString("user_age",TextUtils.join(",",UserAge)).commit();
                        mEditor.putString("user_countries",TextUtils.join(",",UserCountries)).commit();
                    }
                });
                alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                alertDialog.show();
                return true;
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);

                refresh();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                },500);
            }
        });
    }

    class setDeviceList extends BaseAdapter {

        @Override
        public int getCount() {
            if(UserNames.size() < 1 || UserNames.get(0).equals("")) {
                return 0;
            }
            return UserNames.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = getLayoutInflater().inflate(R.layout.list_layout, null);

            TextView name = view.findViewById(R.id.tv_name);
            TextView ip = view.findViewById(R.id.tv_ip);

            name.setText(UserNames.get(i));
            ip.setText(UserIDs.get(i));

            return view;
        }
    }

    @Override
    public void onBackPressed(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Leave Application?");
        alertDialog.setMessage("Are you sure you want to leave the application?");
        alertDialog.setIcon(R.drawable.app_launcher);
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
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

    public void refresh(){
        UserNames.clear();
        UserIDs.clear();
        UserGender.clear();
        UserAge.clear();
        UserCountries.clear();

        u_names = mPreferences.getString("user_names","");
        UserNames = new ArrayList<>(Arrays.asList(u_names.split(",")));

        u_ids = mPreferences.getString("user_IDs","");
        UserIDs = new ArrayList<>(Arrays.asList(u_ids.split(",")));

        u_gender = mPreferences.getString("user_gender","");
        UserGender = new ArrayList<>(Arrays.asList(u_gender.split(",")));

        u_age = mPreferences.getString("user_age","");
        UserAge = new ArrayList<>(Arrays.asList(u_age.split(",")));

        u_countries = mPreferences.getString("user_countries","");
        UserCountries = new ArrayList<>(Arrays.asList(u_countries.split(",")));

        userList.setAdapter(null);
        userList.setAdapter(setIt);
    }
}
