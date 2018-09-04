package com.badcompany.qrscannew;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

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

public class ShowActivity extends Activity {
    ImageView image;
    private TextView username;
    private int mInterval = 7000; // 2 seconds by default, can be changed later
    private Handler mHandler;
    String text2QR = "HELLO", clntID, clntName, ownID, ownName, compName,  amount, service;
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
        setContentView(R.layout.activity_show);

        username =  findViewById(R.id.usr2);
        Intent intent = getIntent();
        Bundle bd = intent.getExtras();
        if (bd != null) {
            clntName = (String) bd.get("clnt_name");
            username.setText(clntName);
            text2QR  = (String) bd.get("code");
            clntID = (String) bd.get("clnt_id");
        }


        image = findViewById(R.id.image);
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(text2QR, BarcodeFormat.QR_CODE, 200, 200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            image.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }


        mHandler = new Handler();
        startRepeatingTask();
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {

                String[] details = new String[]{clntID};
                System.out.println("STEP 1");
                new Authenticate().execute(details);
                System.out.println("STEP 2");
            } catch (Exception e) {
                Toast.makeText(ShowActivity.this, "Could not authenticate", Toast.LENGTH_SHORT).show();
                System.out.println("STEP -1");
                e.printStackTrace();

                // updateStatus(); //this function can change value of mInterval.
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
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
            out.println("34:" + details[0] + "\n"); //used to be VERF
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
                    if (vars[1].equals("14"))
                        Scs_Msg_Code = 14;
                    Err_Msg_Code = 0;
                    ownID = vars[2];
                    ownName = vars[3];
                    compName = vars[4];
                    service = vars[5];
                    amount = vars[6];
                } else {
                    System.out.println("UNEXPECTED RESULT 65");
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
            System.out.println("Post Exe");
            if (Err_Msg_Code == 0) {
                if (Scs_Msg_Code == 14) {
                    stopRepeatingTask();
                    Intent intent = new Intent(getApplicationContext(), ConfirmActivity.class);
                    intent.putExtra("clnt_name", clntName);
                    intent.putExtra("own_id", ownID);
                    intent.putExtra("clnt_id", clntID);
                    intent.putExtra("comp_name", compName);
                    intent.putExtra("service", service);
                    intent.putExtra("amount", amount);
                    startActivity(intent);
                }
            } else {
                System.out.println("Post Exe ERR" + Err_Msg_Code);
                switch (Err_Msg_Code) {
                    case 1: Toast.makeText(ShowActivity.this, "Email address already taken!", Toast.LENGTH_SHORT).show();
                        break;
                    case 2: Toast.makeText(ShowActivity.this, "User not found!", Toast.LENGTH_SHORT).show();
                        break;
                    case 3: Toast.makeText(ShowActivity.this, "User not found!", Toast.LENGTH_SHORT).show();
                        break;
                    case 4: Toast.makeText(ShowActivity.this, "User does not Have Sell permissions!", Toast.LENGTH_SHORT).show();
                        break;
                    case 5: Toast.makeText(ShowActivity.this, "User does not Have Buy permissions!", Toast.LENGTH_SHORT).show();
                        break;
                    case 11: Toast.makeText(ShowActivity.this, "No records yet..", Toast.LENGTH_SHORT).show();
                        break;
                    default: Toast.makeText(ShowActivity.this, "Unknown Error code: " + Err_Msg_Code, Toast.LENGTH_SHORT).show();
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
