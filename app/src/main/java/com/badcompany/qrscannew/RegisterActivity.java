package com.badcompany.qrscannew;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

/**
 * Created by Donatas on 06/07/2018.
 */

public class RegisterActivity extends Activity {
    private Socket socket;
    private static final int SERVER_PORT = 2222;
    //private static final String  SERVER_IP = "10.0.2.2";
    private static final String SERVER_IP = "62.75.189.139";

    private static final String LOG_TAG = "Barcode Scanner API";

    private TextView txtUserName, txtName, txtSurname, txtEmail, txtCompanyName, txtPassword, txtPassword2;
    private TextView txtUserNameLabel, txtNameLabel, txtSurnameLabel, txtEmailLabel, txtCompanyNameLabel, txtPasswordLabel, txtPassword2Label;
    private String UName, Name, Surname, Email, CName, Password, Password2;

    //private Checkbox ckb_buyer, ckb_seller;
    //private String OwnID, ClntID, Bill;
    private Uri imageUri;
    private Button btn_Register;
    private static final String SAVED_INSTANCE_URI = "uri";
    private static final String SAVED_INSTANCE_RESULT = "result";
    private int Msg_Code = 0, Err_Msg_Code = 0, Scs_Msg_Code = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register);

        btn_Register =  findViewById(R.id.btn_register);
        txtUserName =  findViewById(R.id.reg_uname);
        txtName =  findViewById(R.id.reg_name);
        txtSurname =  findViewById(R.id.reg_surname);
        txtEmail =  findViewById(R.id.reg_email);
        txtCompanyName =findViewById(R.id.reg_cname);
        txtPassword =  findViewById(R.id.reg_password1);
        txtPassword2 =  findViewById(R.id.reg_password2);

        txtUserNameLabel =  findViewById(R.id.label_uname);
        txtNameLabel = findViewById(R.id.label_name);
        txtSurnameLabel =  findViewById(R.id.label_surname);
        txtEmailLabel =  findViewById(R.id.label_email);
        txtCompanyNameLabel =  findViewById(R.id.label_cname);
        txtPasswordLabel =findViewById(R.id.label_password);
        txtPassword2Label =  findViewById(R.id.label_password2);


        UName = txtUserName.getText().toString();
        Name = txtName.getText().toString();
        Surname = txtSurname.getText().toString();
        Email = txtEmail.getText().toString();
        CName = txtCompanyName.getText().toString();
        Password = txtPassword.getText().toString();
        Password2 = txtPassword2.getText().toString();

        txtUserName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    Name = txtName.getText().toString();
                    if (!LettersOnly(Name)) {
                        txtName.setText("");
                        txtNameLabel.setVisibility(View.VISIBLE);
                    } else txtNameLabel.setVisibility(View.INVISIBLE);
                }
            }
        });
        txtName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    UName = txtUserName.getText().toString();
                    if (!LettersOnly(UName)) {
                        txtUserName.setText("");
                        txtUserNameLabel.setVisibility(View.VISIBLE);
                    } else txtUserNameLabel.setVisibility(View.INVISIBLE);
                }
            }
        });
        txtSurname.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    Surname = txtSurname.getText().toString();
                    if (!LettersOnly(Surname)) {
                        txtSurname.setText("");
                        txtSurnameLabel.setVisibility(View.VISIBLE);
                    } else txtSurnameLabel.setVisibility(View.INVISIBLE);
                }
            }
        });
        txtEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    Email = txtEmail.getText().toString();
                    if (!isEmail(Email)) {
                        txtEmail.setText("");
                        txtEmailLabel.setVisibility(View.VISIBLE);
                    } else txtEmailLabel.setVisibility(View.INVISIBLE);
                }
            }
        });
        txtCompanyName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    CName = txtCompanyName.getText().toString();
                    if (!LettersOnly(CName)) {
                        txtCompanyName.setText("");
                        txtCompanyNameLabel.setVisibility(View.VISIBLE);
                    } else txtCompanyNameLabel.setVisibility(View.INVISIBLE);
                }
            }
        });
        txtPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    Password = txtPassword.getText().toString();
                    if (!isValidPassword(Password)) {
                        txtPassword.setText("");
                        txtPasswordLabel.setVisibility(View.VISIBLE);
                    } else txtPasswordLabel.setVisibility(View.INVISIBLE);
                }
            }
        });
        txtPassword2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    Password2 = txtPassword2.getText().toString();
                    if (!Password2.equals(Password)) {
                        txtPassword2.setText("");
                        txtPassword.setText("");
                        txtPassword2Label.setVisibility(View.VISIBLE);
                    } else txtPassword2Label.setVisibility(View.INVISIBLE);
                }
            }
        });


        /*if (savedInstanceState != null) {
            imageUri = Uri.parse(savedInstanceState.getString(SAVED_INSTANCE_URI));
            scanResults.setText(savedInstanceState.getString(SAVED_INSTANCE_RESULT));
        }*/



        /*Ownerscan.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(MainActivity.this, new
                        String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION_OWNER);
            }
        });
        Clieantscan.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(MainActivity.this, new
                        String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION_CLIENT);
            }
        });*/

        /*public void onRadioButtonClicked(View view) {
            // Is the button now checked?
            boolean checked = ((RadioButton) view).isChecked();

            // Check which radio button was clicked
            switch(view.getId()) {
                case R.id.radio_pirates:
                    if (checked)
                        // Pirates are the best
                        break;
                case R.id.radio_ninjas:
                    if (checked)
                        // Ninjas rule
                        break;
            }
        }*/

        btn_Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //if(!allValid()){Toast.makeText(RegisterActivity.this,"You must fill in all fields first" , Toast.LENGTH_SHORT).show();} else


                try {
                    // Toast.makeText(RegisterActivity.this,"ALL GOOD" , Toast.LENGTH_SHORT).show();
                    btn_Register.setEnabled(false);
                    System.out.println("TRY " + Name + "  " + Surname + "  " + Email + "  " + UName + "   " + Password + "   " + CName + " \n");
                    String[] details = new String[]{Name, Surname, Email, UName, Password, CName};
                    System.out.println("STEP 1");
                    new Authenticate().execute(details);
                    System.out.println("STEP 2");
                } catch (Exception e) {
                    System.out.println("STEP -1");
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


    private class Authenticate extends AsyncTask<String[], Void, String> {
        private PrintWriter out;
        private BufferedReader in;
        private String[] details;

        @Override
        protected String doInBackground(String[]... params) {
            try {
                System.out.println("REGISTER 1");
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                socket = new Socket(serverAddr, SERVER_PORT);
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
                System.out.println("TOAST 2");
            } catch (IOException e1) {
                System.out.println("TOAST 3");
                e1.printStackTrace();
            }
            details = params[0];
            System.out.println("REG  A" + details[0] + "B" + details[1] + "C" + details[2] + "D" + details[3] + "E" + details[4] + "F" + details[5] + "G\n");
            out.println("38:" + details[0] + ":" + details[1] + ":" + details[2] + ":" + details[3] + ":" + details[4] + ":" + details[5] + "\n"); // used to be REG
            out.flush();

            System.out.print(details[2]);
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
                    if (vars[1].equals("1")) {
                        Scs_Msg_Code = 1;
                    } else  {
                        Scs_Msg_Code = 2;
                    }
                }


            }

            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e)
                {
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
            btn_Register.setEnabled(true);
        /*ListAdapter listAdapter = new CharacterListAdapater(MainActivity.this,
                char_names.toArray(new String[char_names.size()]),
                char_lvls.toArray(new Integer[char_lvls.size()]),
                char_classes.toArray(new String[char_classes.size()]));
        CharList.setAdapter(listAdapter);*/

           /* Intent intent = new Intent(getApplicationContext(),ScanActivity.class);
            startActivity(intent);*/
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }


    private boolean LettersOnly(String s) {
        String regex = "^[a-zA-Z]+$";//.*[a-zA-Z]+.*
        if (Pattern.matches(regex, s))
            return true;
        else
            return false;


    }

    private boolean isEmail(String s) {
        String regex = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x2" +
                "3-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z" +
                "0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z" +
                "0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
        if (Pattern.matches(regex, s))
            return true;
        else
            return false;


    }

    private boolean isValidPassword(String s) {
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$";
        if (Pattern.matches(regex, s))
            return true;
        else
            return false;


    }

    private boolean allValid() {
        if (UName == null || UName.isEmpty() || !LettersOnly(UName)) return false;
       /* if( Name != null && !Name.isEmpty() && !LettersOnly(Name)) return false;
        if(Surname != null && !Surname.isEmpty() && !LettersOnly(Surname)) return false;
        if(Email != null && !Email.isEmpty() && !isEmail(Email)) return false;
        if( CName != null && !CName.isEmpty() && !LettersOnly(CName)) return false;
        if(Password != null && !Password.isEmpty() && !isValidPassword(Password)) return false;
        if(Password2 != null && !Password2.equals(Password)) return false;*/
        return true;
    }


    private int allValids() {
        if (UName != null && !UName.isEmpty() && !LettersOnly(UName)) return 1;
        if (Name != null && !Name.isEmpty() && !LettersOnly(Name)) return 2;
        if (Surname != null && !Surname.isEmpty() && !LettersOnly(Surname)) return 3;
        if (Email != null && !Email.isEmpty() && !isEmail(Email)) return 4;
        if (CName != null && !CName.isEmpty() && !LettersOnly(CName)) return 5;
        if (Password != null && !Password.isEmpty() && !isValidPassword(Password)) return 6;
        if (Password2 != null && !Password2.equals(Password)) return 7;
        return 0;
    }
}