package com.badcompany.qrscannew;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by Donatas on 04/07/2018.
 */

public class ScannerActivity extends Activity implements ZXingScannerView.ResultHandler{

    private int Msg_Code=0;
    private ZXingScannerView zXingScannerView;
    private int CameraPermission = 17;
    private TextView txtUsername;
    private String id, clntID="0", clntName;


    @Override
    protected void onCreate(Bundle savedInstancestate){
        super.onCreate(savedInstancestate);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //setContentView(R.layout.activity_scan);
        zXingScannerView = new ZXingScannerView(getApplicationContext());
        setContentView(zXingScannerView);
        zXingScannerView.setResultHandler(this);
        if (ContextCompat.checkSelfPermission(ScannerActivity.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not grantedif (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,
            if (ActivityCompat.shouldShowRequestPermissionRationale(ScannerActivity.this,
                    Manifest.permission.CAMERA)) {
                new AlertDialog.Builder(this)
                        .setTitle("Permission needed")
                        .setMessage("Scanner requires camera permission to work..")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(ScannerActivity.this, new String[]{Manifest.permission.CAMERA}, CameraPermission);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CameraPermission);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        zXingScannerView.startCamera();
    }

    @Override
    public void handleResult(Result result){

        zXingScannerView.removeAllViews();
        zXingScannerView.stopCamera();
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result",result.getText());
        setResult(Activity.RESULT_OK,returnIntent);
        this.finish();
        Toast.makeText(getApplicationContext(), result.getText()+" Asynctask", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == CameraPermission){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else{
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(zXingScannerView!=null)
            zXingScannerView.stopCamera();
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

}
