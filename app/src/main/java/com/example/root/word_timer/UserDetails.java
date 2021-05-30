package com.example.root.word_timer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class UserDetails extends AppCompatActivity  implements AdapterView.OnItemSelectedListener  {

    private SharedPreferences.Editor mEditor;
    private SharedPreferences mPreferences;
    ArrayList<String> UserNames;
    ArrayList<String> UserIDs;
    ArrayList<String> UserGenders;
    ArrayList<String> UserAges;
    ArrayList<String> UserCountries;
    EditText username, userage, userID;
    String u_name, u_age, u_ID, u_country;
    RadioGroup gen;
    RadioButton u_gender;
    Button done;
    Spinner user_country;
    String[] countries = {"--", "Argentina", "Australia", "Austria", "Bangladesh", "Belgium", "Brazil", "Canada", "China", "Croatia", "Czech Republic",
            "Denmark", "Egypt", "France", "Germany", "Greece", "Hong Kong", "Hungary", "India", "Indonesia", "Iran", "Iraq", "Italy", "Japan", "Malaysia",
            "Mexico", "Nepal", "Poland", "Saudi Arabia", "Singapore", "South Africa", "Spain", "Sri Lanka", "Sweden", "Switzerland", "Thailand", "Ukraine", "U.A.E",
            "United Kingdom", "U.S.A", "Vietnam"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        username = findViewById(R.id.et_name);
        userage = findViewById(R.id.et_age);
        userID = findViewById(R.id.et_email);
        gen = findViewById(R.id.radioGroup);
        done = findViewById(R.id.done);
        user_country = findViewById(R.id.sp_country);

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, countries);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        user_country.setAdapter(adapter);
        user_country.setOnItemSelectedListener(this);
        ArrayAdapter user_countryAdapter = (ArrayAdapter) user_country.getAdapter();

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mEditor = mPreferences.edit();

        Bundle info = getIntent().getExtras();
        UserNames = new ArrayList<>(Arrays.asList(info.getString("u_names").split(",")));
        UserIDs = new ArrayList<>(Arrays.asList(info.getString("u_IDs").split(",")));
        UserGenders = new ArrayList<>(Arrays.asList(info.getString("u_genders").split(",")));
        UserAges = new ArrayList<>(Arrays.asList(info.getString("u_ages").split(",")));
        UserCountries = new ArrayList<>(Arrays.asList(info.getString("u_countries").split(",")));

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                u_name = username.getText().toString();
                u_age = userage.getText().toString();
                u_ID = userID.getText().toString();

                if(gen.getCheckedRadioButtonId() == -1){
                    Toast.makeText(UserDetails.this, "Please select a gender", Toast.LENGTH_SHORT).show();
                }
                else if((u_name.length()<1) || !(isAlpha(u_name))){
                    Toast.makeText(UserDetails.this, "Given name is invalid", Toast.LENGTH_SHORT).show();
                    username.setText("");
                }
                else if((u_age.length() > 2) || (u_age.length() < 1)){
                    Toast.makeText(UserDetails.this, "Given age is invalid", Toast.LENGTH_SHORT).show();
                    userage.setText("");
                }
                else if(!(u_ID.contains("@") && u_ID.contains("."))){
                    Toast.makeText(UserDetails.this, "Given email ID is invalid", Toast.LENGTH_SHORT).show();
                    userID.setText("");
                }
                else if(UserNames.contains(u_name) || UserIDs.contains(u_ID)){
                    Toast.makeText(UserDetails.this, "Given email ID or User name alredy exists, please check", Toast.LENGTH_SHORT).show();
                    userID.setText("");
                    username.setText("");
                }
                else if(u_country.equals("--")){
                    Toast.makeText(UserDetails.this, "Please select your country", Toast.LENGTH_SHORT).show();
                }
                else{
                    if(UserNames.get(0).equals("")){
                        UserNames.set(0,u_name);
                        UserGenders.set(0,u_gender.getText().toString());
                        UserAges.set(0,u_age);
                        UserIDs.set(0,u_ID);
                        UserCountries.set(0,u_country);
                    }
                    else {
                        UserNames.add(u_name);
                        UserGenders.add(u_gender.getText().toString());
                        UserAges.add(u_age);
                        UserIDs.add(u_ID);
                        UserCountries.add(u_country);
                    }

                    mEditor.putString("user_names",TextUtils.join(",",UserNames)).commit();
                    mEditor.putString("user_IDs",TextUtils.join(",",UserIDs)).commit();
                    mEditor.putString("user_gender",TextUtils.join(",",UserGenders)).commit();
                    mEditor.putString("user_age",TextUtils.join(",",UserAges)).commit();
                    mEditor.putString("user_countries",TextUtils.join(",",UserCountries)).commit();

                    Intent mainActivity = new Intent(UserDetails.this, MainActivity.class);
                    Bundle info = new Bundle();
                    info.putString("u_name",u_name);
                    info.putString("u_ID",u_ID);
                    info.putString("u_gender",u_gender.getText().toString());
                    info.putString("u_age",u_age);
                    info.putString("u_country",u_country);
                    mainActivity.putExtras(info);
                    startActivity(mainActivity);
                    finish();
                }
            }
        });
    }

    public void checkButton(View v){
        int radioID = gen.getCheckedRadioButtonId();
        u_gender = findViewById(radioID);
    }

    @Override
    public void onBackPressed(){
        Intent userSelect = new Intent(UserDetails.this, user_select.class);
        startActivity(userSelect);
        finish();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        u_country  = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    public boolean isAlpha(String name) {
        char[] chars = name.toCharArray();

        for (char c : chars) {
            if(!Character.isLetter(c) && !Character.isSpaceChar(c)) {
                return false;
            }
        }
        return true;
    }
}


