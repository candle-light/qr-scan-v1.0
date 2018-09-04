package com.badcompany.qrscannew;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Socket socket;
    private static final int SERVER_PORT = 2222;
    //private static final String  SERVER_IP = "10.0.2.2";
    private static final String SERVER_IP = "62.75.189.139";

    private TextView txtlogin, txtpassword;
    private String radioChoice = "Buyer", username, compname, code, id, def_lang = "en";
    private Uri imageUri;
    private Button loginbtn, create;
    private ImageButton changeLanguage, LangEng, LangPl;
    private RadioGroup radioGroup;
    private int Msg_Code = 0, Err_Msg_Code = 0, Scs_Msg_Code = 0;

    private static final String SAVED_INSTANCE_URI = "uri";
    private static final String SAVED_INSTANCE_RESULT = "result";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        /*requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        changeLanguage =  findViewById(R.id.chlang);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (def_lang.equals("pl"))
                changeLanguage.setImageDrawable(getResources().getDrawable(R.drawable.poland1, getApplicationContext().getTheme()));
            else
                changeLanguage.setImageDrawable(getResources().getDrawable(R.drawable.english1, getApplicationContext().getTheme()));
        } else {
            if (def_lang.equals("pl"))
                changeLanguage.setImageDrawable(getResources().getDrawable(R.drawable.poland1));
            else changeLanguage.setImageDrawable(getResources().getDrawable(R.drawable.english1));
        }

        loginbtn =  findViewById(R.id.btn_login);
        create = findViewById(R.id.btn_signup);
        txtlogin =  findViewById(R.id.input_username);
        txtpassword =  findViewById(R.id.input_password);
        radioGroup =  findViewById(R.id.radio_group);


        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb =  findViewById(checkedId);
                radioChoice = rb.getText().toString();
            }
        });
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if (isNetworkAvailable()){
                // if(isURLReachable(getApplicationContext(), SERVER_IP)){
                try {
                    loginbtn.setEnabled(false);
                    String user = txtlogin.getText().toString();
                    String pass = txtpassword.getText().toString();
                    String[] details = new String[]{radioChoice, user, pass};
                    System.out.println("STEP 1");
                    new Authenticate().execute(details);
                    System.out.println("STEP 2");
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                    System.out.println("STEP -1");
                    e.printStackTrace();
                }
                //} else  Toast.makeText(MainActivity.this, "No Network Access..!", Toast.LENGTH_SHORT).show();
            }
        });
        changeLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    final AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
                    //mBuilder.setTitle("Choose Language..");
                    View mView = getLayoutInflater().inflate(R.layout.dialog_change_language, null);
                    LangEng =  mView.findViewById(R.id.lang_eng);
                    LangPl =  mView.findViewById(R.id.lang_pl);
                    mBuilder.setView(mView);
                    final AlertDialog dialog = mBuilder.create();
                    LangEng.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                setLocale("en");
                                recreate();
                                dialog.dismiss();

                            } catch (Exception e) {
                                Toast.makeText(MainActivity.this, "Couldn't change language", Toast.LENGTH_SHORT).show();
                                System.out.println("Couldn't change language");
                                e.printStackTrace();
                            }
                        }
                    });
                    LangPl.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                setLocale("pl");
                                recreate();
                                dialog.dismiss();

                            } catch (Exception e) {
                                Toast.makeText(MainActivity.this, "Couldn't change language", Toast.LENGTH_SHORT).show();
                                System.out.println("Couldn't change language");
                                e.printStackTrace();
                            }
                        }
                    });
                    dialog.show();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Couldn't change language", Toast.LENGTH_SHORT).show();
                    System.out.println("Couldn't change language");
                    e.printStackTrace();
                }
                //} else  Toast.makeText(MainActivity.this, "No Network Access..!", Toast.LENGTH_SHORT).show();
            }
        });
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    create.setEnabled(false);
                    Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    System.out.println("STEP 7");
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (imageUri != null) {
            outState.putString(SAVED_INSTANCE_URI, imageUri.toString());
            //outState.putString(SAVED_INSTANCE_RESULT, scanResults.getText().toString());
        }
        super.onSaveInstanceState(outState);
    }

    protected void setLocale(String s) {
        Locale locale = new Locale(s);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        SharedPreferences.Editor editor = getSharedPreferences("settings", MODE_PRIVATE).edit();
        editor.putString("def_lang", s);
        editor.apply();

    }

    protected void loadLocale() {
        SharedPreferences prefs = getSharedPreferences("settings", Activity.MODE_PRIVATE);
        String lang = prefs.getString("def_lang", "");
        setLocale(lang);
        def_lang = lang;

    }

    private boolean isURLReachable(String s) {
        ///ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (isNetworkAvailable()) {
            //netInfo != null && netInfo.isConnected()) {
            try {
                URL url = new URL("http://" + s);   // "http://192.168.1.13"   Change to "http://google.com" for www  test.
                HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                urlc.setConnectTimeout(10 * 1000);          // 10 s.
                urlc.connect();
                if (urlc.getResponseCode() == 200) {        // 200 = "OK" code (http connection is fine).
                    Log.wtf("Connection", "Success !");
                    System.out.println("Server is running!");
                    return true;
                } else {
                    return false;
                }
            } catch (MalformedURLException e1) {
                return false;
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private class Authenticate extends AsyncTask<String[], Void, String> {
        private PrintWriter out;
        private BufferedReader in;
        private String[] details;

        @Override
        protected String doInBackground(String[]... params) {

            if (isNetworkAvailable()) {
                //netInfo != null && netInfo.isConnected()) {
                try {
                    URL url = new URL("http://" + SERVER_IP);   // "http://192.168.1.13"   Change to "http://google.com" for www  test.
                    HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                    urlc.setConnectTimeout(10 * 1000);          // 10 s.
                    urlc.connect();
                    if (urlc.getResponseCode() == 200) {        // 200 = "OK" code (http connection is fine).
                        Log.wtf("Connection", "Success !");
                        System.out.println("Server is running!");
                        //return true;
                    } else {
                        System.out.println("Server is NOT running!");
                        // return false;
                    }
                } catch (MalformedURLException e1) {
                    e1.printStackTrace();
                    //return false;
                } catch (IOException e) {
                    e.printStackTrace();
                    // return false;
                }
            }


            try {
                System.out.println("TOAST 1");
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                socket = new Socket(serverAddr, SERVER_PORT);

                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            } catch (UnknownHostException e1) {
                e1.printStackTrace();
                System.out.println("TOAST 2");
                System.out.println("STEP 2.1");
            } catch (IOException e1) {
                System.out.println("TOAST 3");
                System.out.println("STEP 2.2");
                e1.printStackTrace();
            }
            details = params[0];
            if (details[0].equals("Seller") && details[0].equals(radioChoice)) {
                System.out.println("31:" + details[0] + ":" + details[1] + ":" + details[2] + "\n");
                out.println("31:" + details[1] + ":" + details[2] + "\n");
                out.flush();

            } else if (details[0].equals("Buyer") && details[0].equals(radioChoice)) {
                System.out.println("32:" + details[0] + ":" + details[1] + ":" + details[2] + "\n");
                out.println("32:" + details[1] + ":" + details[2] + "\n");
                out.flush();


            } else {
                System.out.println("Something is wrong with sending login query");
            }

            System.out.println("Sent");
            String s = "", total = null;
            while (total == null) {
                System.out.println("Waiting");
                try {
                    s = in.readLine();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                total += s;
            }

            if (!s.equals("")) {
                String[] vars = s.split("\\s+");
                System.out.println(s);

                if (vars[0].equals("Err")) {
                    if (vars[1].equals("1")) Err_Msg_Code = 1;
                    if (vars[1].equals("2")) Err_Msg_Code = 2;
                    if (vars[1].equals("3")) Err_Msg_Code = 3;
                } else if (vars[0].equals("Scs")) {
                    Err_Msg_Code = 0;
                    id = vars[2];
                    username = vars[3] + " " + vars[4];
                    System.out.println("USERNAME >> " + username);

                    if (vars[1].equals("7")) { //seller
                        Scs_Msg_Code = 7;
                        compname = vars[5];
                    } else if (vars[1].equals("8")) { //buyer
                        code = vars[5];
                        System.out.println(" code :" + code);
                        Scs_Msg_Code = 8;
                    }
                }


            }
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace(System.err);
                }
            }
            return "Finished";
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            loginbtn.setEnabled(true);
            if (Err_Msg_Code == 0) {
                if (radioChoice.equals("Seller")) {
                    Intent intent = new Intent(getApplicationContext(), ScanActivity.class);
                    intent.putExtra("own_name", username);
                    intent.putExtra("comp_name", compname);
                    intent.putExtra("own_id", id);
                    startActivity(intent);
                } else if (radioChoice.equals("Buyer")) {
                    Intent intent = new Intent(getApplicationContext(), ShowActivity.class);
                    intent.putExtra("code", code);
                    intent.putExtra("clnt_name", username);
                    intent.putExtra("clnt_id", id);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "Choose between buyer or seller", Toast.LENGTH_SHORT).show();
                }
            } else {
                System.out.println("Post Exe ERR" + Err_Msg_Code);
                switch (Err_Msg_Code) {
                    case 1:
                        Toast.makeText(MainActivity.this, "Email address already taken!", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(MainActivity.this, "User not found!", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        Toast.makeText(MainActivity.this, "Username already taken!", Toast.LENGTH_SHORT).show();
                        break;
                    case 4:
                        Toast.makeText(MainActivity.this, "User does not Have Sell permissions!", Toast.LENGTH_SHORT).show();
                        break;
                    case 5:
                        Toast.makeText(MainActivity.this, "User does not Have Buy permissions!", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(MainActivity.this, "Unknown Error code: " + Err_Msg_Code, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }


        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

    }
}