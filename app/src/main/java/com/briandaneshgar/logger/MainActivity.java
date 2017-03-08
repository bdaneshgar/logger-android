package com.briandaneshgar.logger;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private int masterCount = 0;

    List<String> logs = new ArrayList<>();
    private String prevLog = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //update
        Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(2000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String newLog = imsi() + ", " + mcc() + ", " + mnc() + ", " + carrier() + ", " + radioTech() + ", " + status();
                                if(!prevLog.equals(newLog)){
                                    prevLog = newLog;
                                    masterCount++;
                                    Log.wtf("mastercount", Integer.toString(masterCount));
                                    logs.add(timestamp() + ": " + newLog);
                                    update();
                                }
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        t.start();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String text = "";

                TableLayout ll = (TableLayout) findViewById(R.id.displayLinear);

                for(int i = 0; i < masterCount; i++) {
                    TableRow tableRow = (TableRow) ll.getChildAt(i);
                    TextView textView = (TextView) tableRow.getChildAt(0);
                    text += (String) textView.getText() + "\n";
                }

                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{});
                i.putExtra(Intent.EXTRA_SUBJECT, "Testing Log Report");
                i.putExtra(Intent.EXTRA_TEXT   , text);
                try {
                    startActivity(Intent.createChooser(i, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void update(){
        TableLayout ll = (TableLayout) findViewById(R.id.displayLinear);
        ll.removeAllViews();


        for (int i = 0; i < masterCount; i++) {

            TableRow row= new TableRow(this);
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(lp);
            TextView label = new TextView(this);
            label.setTextSize(10);
            label.setText(logs.get(i));
            row.addView(label);
            ll.addView(row,i);
        }
    }

    public String timestamp(){
        Date d = new Date();
        CharSequence formattedDate = DateFormat.format("yyyy-MM-dd hh:mm:ss", d.getTime());
        return (String) formattedDate;
    }

    public String imsi(){
        TelephonyManager tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String imsi = tel.getSubscriberId();

        if (imsi != null && !imsi.isEmpty()) {
            return imsi;
        } else {
            return "N/A";
        }
    }


    private String mcc(){
        TelephonyManager tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String networkOperator = tel.getNetworkOperator();
        int mcc = 0;
        if (!TextUtils.isEmpty(networkOperator)) {
            mcc = Integer.parseInt(networkOperator.substring(0, 3));
        }
        return Integer.toString(mcc);
    }

    private String mnc(){
        TelephonyManager tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String networkOperator = tel.getNetworkOperator();
        int mnc = 0;
        if (!TextUtils.isEmpty(networkOperator)) {
            mnc = Integer.parseInt(networkOperator.substring(3));
        }
        return Integer.toString(mnc);
    }


    public String carrier() {
        TelephonyManager tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String carrier;
        PackageManager pm = this.getPackageManager();

        if (pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY) && tel.getNetworkOperatorName() != "") {

            carrier = tel.getNetworkOperatorName();

        } else {
            carrier = "N/A";
        }

        return carrier;
    }


    public String radioTech(){
        TelephonyManager tel =
                (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = tel.getNetworkType();

        String result = "";

        switch (networkType)
        {
            case 7:
                result = "1xRTT";
                break;
            case 4:
                result = "CDMA";
                break;
            case 2:
                result = "EDGE";
                break;
            case 14:
                result = "eHRPD";
                break;
            case 5:
                result = "EVDO rev. 0";
                break;
            case 6:
                result = "EVDO rev. A";
                break;
            case 12:
                result = "EVDO rev. B";
                break;
            case 1:
                result = "GPRS";
                break;
            case 8:
                result = "HSDPA";
                break;
            case 10:
                result = "HSPA";
                break;
            case 15:
                result = "HSPA+";
                break;
            case 9:
                result = "HSUPA";
                break;
            case 11:
                result = "iDen";
                break;
            case 13:
                result = "LTE";
                break;
            case 3:
                result = "UMTS";
                break;
            case 0:
                result = "Unknown";
                break;
        }
        return result;
    }

    public String status(){
        if(mcc().equals("0") && mnc().equals("0")){
            return "Emergency Calls Only";
        }if(!radioTech().equals("N/A")){
            return "Registered";
        }  else
            return "No Network";
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
