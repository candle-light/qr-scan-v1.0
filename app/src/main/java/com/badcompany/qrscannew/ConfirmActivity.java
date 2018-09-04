package com.badcompany.qrscannew;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
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

/**
 * Created by Donatas on 07/07/2018.
 */

public class ConfirmActivity extends Activity {
    private TextView txtClntName, txtOwnName, txtCompName, txtAmount, txtService;
    private Button btnAccept, btnDecline;
    String clntID = "", clntName = "", ownID = "", ownName = "", compName = "", amount, service;
    private Socket socket;
    private static final int SERVER_PORT = 2222;

    //private static final String  SERVER_IP = "10.0.2.2";
    private static final String SERVER_IP = "62.75.189.139";
    private int Msg_Code = 0, Err_Msg_Code = 0, Scs_Msg_Code = 0;


    @Override
    protected void onCreate(Bundle savedInstancestate) {
        super.onCreate(savedInstancestate);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_confirm);

        txtClntName =  findViewById(R.id.clnt_name);
        txtOwnName =  findViewById(R.id.usr3);
        txtCompName = findViewById(R.id.cname);
        txtAmount =  findViewById(R.id.amnt1);
        txtService =  findViewById(R.id.srvc1);

        btnAccept =  findViewById(R.id.btn_confirm);
        btnDecline =  findViewById(R.id.btn_decline);
        Intent intent = getIntent();
        Bundle bd = intent.getExtras();
        if (bd != null) {
            clntName = (String) bd.get("clnt_name");
            txtClntName.setText(clntName);
            clntID = (String) bd.get("clnt_id");
            ownID = (String) bd.get("own_id");
            compName = (String) bd.get("comp_name");
            txtCompName.setText(compName);
            amount = (String) bd.get("amount");
            txtAmount.setText(amount);
            service = (String) bd.get("service");
            txtService.setText(service);
        }
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    btnAccept.setEnabled(false);
                    System.out.println("Step C1 - Initializing details");
                    String[] details = new String[]{"36", ownID, clntID, ownName, compName, service, amount};
                    new Authenticate().execute(details);
                    System.out.println("Step C2 - after authenticate called");
                } catch (Exception e) {
                    Toast.makeText(ConfirmActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                    System.out.println("STEP -1");
                    e.printStackTrace();
                }
            }
        });

        btnDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    btnDecline.setEnabled(false);
                    System.out.println("Step C1 - Initializing details");
                    String[] details = new String[]{"37", ownID, clntID, ownName, compName, service, amount};
                    new Authenticate().execute(details);
                    System.out.println("Step C2 - after authenticate called");
                } catch (Exception e) {
                    Toast.makeText(ConfirmActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                    System.out.println("STEP -1");
                    e.printStackTrace();
                }
            }
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
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
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                socket = new Socket(serverAddr, SERVER_PORT);
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            details = params[0];

            out.println(details[0] + ":" + details[1] + ":" + details[2] + ":" + details[3] + ":" + details[4] + "\n");
            out.flush();
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
                    if (vars[1].equals("11")) Err_Msg_Code = 11;
                } else if (vars[0].equals("Scs")) {
                    if (vars[1].equals("18")) Scs_Msg_Code = 18;
                    else if (vars[1].equals("19")) Scs_Msg_Code = 18;
                    else System.out.println("UNEXPECTED RESULT 65");
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
            if (Err_Msg_Code == 0) {
                if (Scs_Msg_Code == 10) {

                }
            } else {
                System.out.println("Post Exe ERR" + Err_Msg_Code);
                switch (Err_Msg_Code) {
                    case 1:
                        Toast.makeText(ConfirmActivity.this, "Email address already taken!", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(ConfirmActivity.this, "User not found!", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        Toast.makeText(ConfirmActivity.this, "Username already taken!", Toast.LENGTH_SHORT).show();
                        break;
                    case 4:
                        Toast.makeText(ConfirmActivity.this, "User does not Have Sell permissions!", Toast.LENGTH_SHORT).show();
                        break;
                    case 5:
                        Toast.makeText(ConfirmActivity.this, "User does not Have Buy permissions!", Toast.LENGTH_SHORT).show();
                        break;
                    case 11:
                        Toast.makeText(ConfirmActivity.this, "No records yet..", Toast.LENGTH_SHORT).show();
                        break;
                    default:Toast.makeText(ConfirmActivity.this, "Unknown Error code: " + Err_Msg_Code, Toast.LENGTH_SHORT).show();
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

