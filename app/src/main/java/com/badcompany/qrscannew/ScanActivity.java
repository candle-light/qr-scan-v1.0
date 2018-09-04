package com.badcompany.qrscannew;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by Donatas on 04/07/2018.
 */

public class ScanActivity extends Activity {

    private Socket socket;
    private static final int SERVER_PORT = 2222;
    //private static final String  SERVER_IP = "10.0.2.2";
    private static final String SERVER_IP = "62.75.189.139";

    private int mInterval = 2000; // 2 seconds by default, can be changed later
    private Handler mHandler;
    private int Msg_Code = 0, Err_Msg_Code = 0, Scs_Msg_Code = 0;
    private ZXingScannerView zXingScannerView;
    private int CameraPermission = 17;
    private TextView txtUsername, txtClient, txtService, txtAmount;
    private String ownID = "", ownName = "", compName = "", clntID = "0", clntName = "";
    private Button btnSend;


    @Override
    protected void onCreate(Bundle savedInstancestate) {

        super.onCreate(savedInstancestate);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_scan);

        txtUsername =  findViewById(R.id.usr);
        txtService =  findViewById(R.id.srvc);
        txtAmount =  findViewById(R.id.amnt);
        Intent intent = getIntent();
        Bundle bd = intent.getExtras();
        if (bd != null) {
            ownName = (String) bd.get("own_name");
            txtUsername.setText(ownName);
            ownID = (String) bd.get("own_id");
            compName = (String) bd.get("comp_name");

        }
        txtClient =  findViewById(R.id.clnt);
        btnSend = findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if (isNetworkAvailable()){
                // if(isURLReachable(getApplicationContext(), SERVER_IP)){
                if (!clntID.equals("0")) {
                    try {
                        btnSend.setEnabled(false);
                        String srvc = txtService.getText().toString();
                        srvc.replaceAll("\\s+", "");
                        String amnt = txtAmount.getText().toString();
                        amnt.replaceAll("\\s+", "");
                        String[] details = new String[]{ownID, clntID, ownName, compName, srvc, amnt};
                        System.out.println("STEP 1");
                        new Authenticate().execute(details);
                        System.out.println("STEP 2");
                    } catch (Exception e) {
                        Toast.makeText(ScanActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                        System.out.println("STEP -1");
                        e.printStackTrace();
                    }
                }
                //} else  Toast.makeText(MainActivity.this, "No Network Access..!", Toast.LENGTH_SHORT).show();
            }
        });
        // mHandler = new Handler();
        // startRepeatingTask();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                String result = data.getStringExtra("result");
                String[] detail = new String[]{ownID, result};
                new Authenticate().execute(detail);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
                Toast.makeText(this, "No data returned ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void scan(View view) {
        Intent i = new Intent(this, ScannerActivity.class);
        startActivityForResult(i, 1);
    }

    @Override
    protected void onPause() {
        super.onPause();
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
        private PrintWriter out1;
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
                out1 = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            details = params[0];
            if (details.length == 2) {
                out1.println("33:" + details[0] + ":" + details[1] + "\n"); //used to be SCAN
                out1.flush();
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
                        if (vars[1].equals("9"))
                            Scs_Msg_Code = 9;
                        Err_Msg_Code = 0;
                        clntName = vars[2] + " " + vars[3];
                        clntID = vars[4];
                    }
                }
                if (socket != null && !socket.isClosed()) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace(System.err);
                    }
                }

            } else if (details.length == 4) {
                out1.println("35:" + details[0] + ":" + details[1] + ":" + details[2] + ":" + details[3] + "\n");
                out1.flush();
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
                        Toast.makeText(ScanActivity.this, "Could not update", Toast.LENGTH_SHORT).show();
                        if (vars[1].equals("1")) Err_Msg_Code = 1;
                        if (vars[1].equals("2")) Err_Msg_Code = 2;
                        if (vars[1].equals("3")) Err_Msg_Code = 3;
                    } else if (vars[0].equals("Scs")) {
                        //Toast.makeText(ScanActivity.this,"Waiting for client ... code:"+vars[1], Toast.LENGTH_SHORT).show();
                        if (vars[1].equals("10")) {
                            Scs_Msg_Code = 10;
                        }
                    } else {
                        System.out.println("UNEXPECTED EVENT 17");
                    }
                }
                if (socket != null && !socket.isClosed()) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace(System.err);
                    }
                }
            }
            /*if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e)
                {
                    e.printStackTrace(System.err);
                }
            }*/
            return "Finished";
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();



           /* if(isURLReachable(SERVER_IP))
                System.out.println("Url Reachable");
            else {
                System.out.println("Url is not reachable");
                cancel(true);
            }*/

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (Err_Msg_Code == 0) {

                System.out.println(" CLIENT NAME ....  :" + clntName);
                if (Scs_Msg_Code == 9) {
                    System.out.println(" CLIENT NAME ....  :" + clntName);
                    txtClient.setText(clntName);
                } else if (Scs_Msg_Code == 10) {
                    Toast.makeText(ScanActivity.this, "Sent Information Success", Toast.LENGTH_SHORT).show();
                    System.out.println("Sent information succesfully");

                }
               /* if(radioChoice.equals("Seller")){

                    Intent intent = new Intent(getApplicationContext(),ScanActivity.class);
                    intent.putExtra("name",username);
                    startActivity(intent);
                }
                else if(radioChoice.equals("Buyer")){
                    Intent intent = new Intent(getApplicationContext(),ShowActivity.class);
                    intent.putExtra("code",code);
                    intent.putExtra("name",username);
                    startActivity(intent);}
                else {Toast.makeText(MainActivity.this,"Choose between buyer or seller" , Toast.LENGTH_SHORT).show();}*/
            }  else {
                System.out.println("Post Exe ERR" + Err_Msg_Code);
                switch (Err_Msg_Code) {
                    case 1: Toast.makeText(ScanActivity.this, "Email address already taken!", Toast.LENGTH_SHORT).show();
                        break;
                    case 2: Toast.makeText(ScanActivity.this, "User not found!", Toast.LENGTH_SHORT).show();
                        break;
                    case 3: Toast.makeText(ScanActivity.this, "User not found!", Toast.LENGTH_SHORT).show();
                        break;
                    case 4: Toast.makeText(ScanActivity.this, "User does not Have Sell permissions!", Toast.LENGTH_SHORT).show();
                        break;
                    case 5: Toast.makeText(ScanActivity.this, "User does not Have Buy permissions!", Toast.LENGTH_SHORT).show();
                        break;
                    case 11: Toast.makeText(ScanActivity.this, "No records yet..", Toast.LENGTH_SHORT).show();
                        break;
                    default: Toast.makeText(ScanActivity.this, "Unknown Error code: " + Err_Msg_Code, Toast.LENGTH_SHORT).show();
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
