package com.example.rfidprint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.TcpConnection;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String PROFILE1 = "Sacnner" ;
    private EditText id;
    private Button print;
    AsyncTask<Void, String, String> task = null;
    IntentFilter filter = new IntentFilter();
    private Handler handler = new Handler(){

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch(msg.what){
                case 0x11:
                    CustomToast.showToast(MainActivity.this,"ip错误！",1500);
                    break;



            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        id = (EditText) findViewById(R.id.edit_query);
        print =(Button)findViewById(R.id.buttonPanel);
                filter.addAction(Datawedeentity.ACTION_RESULT_DATAWEDGE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.addAction(Datawedeentity.ACTIVITY_INTENT_FILTER_ACTION);  // The filtered action must match the "Intent action" specified in the DW Profile's Intent output configuration
        String Code128Value = "true";
        String EAN13Value = "false";
        CreateProfile(PROFILE1, Code128Value, EAN13Value);
        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFastClick()){
                    if (NetWorkUtils.getAPNType(MainActivity.this) == 1){
                        Mytask match = new Mytask();
                        match.execute();
                    }else {
                        CustomToast.showToast(MainActivity.this,"请连接WIFi！",1500);
                    }
                }else{
                    CustomToast.showToast(MainActivity.this,"点击太快了！",1500);

                }

            }
        });
    }

    private class Mytask extends AsyncTask<Void, String, String>{

        @Override
        protected String doInBackground(Void... voids) {
            try {
                sendZplOverTcp(nowIp());
            } catch (ConnectionException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public String nowIp(){
        SharedPreferences pref = getSharedPreferences("ipData", MODE_PRIVATE);
        return pref.getString("ip", "");
    }
    private static final int MIN_CLICK_DELAY_TIME = 2000;
    private static long lastClickTime;
    public static boolean isFastClick() {
        boolean flag = false;
        long curClickTime = System.currentTimeMillis();
        if ((curClickTime - lastClickTime) >= MIN_CLICK_DELAY_TIME) {
            flag = true;
        }
        lastClickTime = curClickTime;
        return flag;
    }
    private void sendZplOverTcp(String theIpAddress) throws ConnectionException {
        // Instantiate connection for ZPL TCP port at given address
        Connection thePrinterConn = new TcpConnection(theIpAddress, TcpConnection.DEFAULT_ZPL_TCP_PORT);

        try {
            thePrinterConn.open();
          //  String zplData = "^XA^FO50,20^A0N,25,25^FD11111111.^FS^XZ";
            String zplData = "^XA\n" +
                    "^RS,,,3,N,,,2\n" +
                    "^RR3\n" +
                    "^XZ\n" +
                    "^XA\n" +
                    "^SZ2^JMA\n" +
                    "^MCY^PMN\n" +
                    "^PW800\n" +
                    "~JSN\n" +
                    "~SD25^MD0\n" +
                    "^JZY\n" +
                    "^LH0,0^LRN\n" +
                    "^XZ\n" +
                    "^XA\n" +
                    "^FT150,152\n" +
                    "^CI0\n" +
                    "^AAN,27,15^FDAAAABBBB1001^FS\n" +
                    "^RFW,H,1,2,1^FD1C00^FS\n" +
                    "^RFW,H,2,6,1^FDAAAABBBB1001^FS\n" +
                    "^PQ1,0,1,Y\n" +
                    "^XZ\n";

            thePrinterConn.write(zplData.getBytes());
        } catch (ConnectionException e) {
            //e.printStackTrace();
            handler.sendEmptyMessage(0x11);
        } finally {
            thePrinterConn.close();
        }
    }

    private void printConfigLabelUsingDnsName(String dnsName) throws ConnectionException {
        Connection connection = new TcpConnection(dnsName, 9100);
        try {
            connection.open();
            ZebraPrinter p = ZebraPrinterFactory.getInstance(connection);
            p.printConfigurationLabel();
        } catch (ConnectionException e) {
            e.printStackTrace();
        } catch (ZebraPrinterLanguageUnknownException e) {
            e.printStackTrace();
        } finally {
            // Close the connection to release resources.
            connection.close();
        }

    }
    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(Broadcast, filter);
        Datawedeentity.sendDataWedgeIntentWithExtra(getApplicationContext(),
                Datawedeentity.ACTION_DATAWEDGE, Datawedeentity. EXTRA_GET_ACTIVE_PROFILE,
                Datawedeentity.EXTRA_EMPTY);
    }

    //创建选项菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.meum_ipseeting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_settings) {
            Intent showTastList = new Intent(getApplicationContext(),IpActivity.class);
            startActivity(showTastList);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onStop() {
        super.onStop();
        unregisterReceiver(Broadcast);
    }
    private BroadcastReceiver Broadcast = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Datawedeentity.EXTRA_RESULT_GET_ACTIVE_PROFILE)){
                if (intent.hasExtra(Datawedeentity.EXTRA_RESULT_GET_ACTIVE_PROFILE)) {
                    String activeProfile = intent.getStringExtra(Datawedeentity.EXTRA_RESULT_GET_ACTIVE_PROFILE);
                }

            }


            if (action.equals(Datawedeentity.ACTIVITY_INTENT_FILTER_ACTION)) {
                //  Received a barcode scan
                try {

                    displayScanResult(intent, "via Broadcast");
                } catch (Exception e) {
                    //  Catch if the UI does not exist when we receive the broadcast
                    Toast.makeText(getApplicationContext(), "Error; " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

        }
    };
    // Display scanned data
    private void displayScanResult(Intent initiatingIntent, String howDataReceived)
    {
        //Id
        String decodedData = initiatingIntent.getStringExtra(Datawedeentity.DATAWEDGE_INTENT_KEY_DATA);
        //类型
        String decodedDecoder = initiatingIntent.getStringExtra(Datawedeentity.DATAWEDGE_INTENT_KEY_DECODER);
        id.setText(decodedData);
    }

    private void CreateProfile (String profileName, String code128Value, String ean13Value){

        // Configure profile to apply to this app
        Bundle bMain = new Bundle();
        bMain.putString("PROFILE_NAME", profileName);
        bMain.putString("PROFILE_ENABLED", "true");
        bMain.putString("CONFIG_MODE", "CREATE_IF_NOT_EXIST");  // Create profile if it does not exist

        // Configure barcode input plugin
        Bundle bConfigBarcode = new Bundle();
        bConfigBarcode.putString("PLUGIN_NAME", "BARCODE");
        bConfigBarcode.putString("RESET_CONFIG", "true"); //  This is the default

        // PARAM_LIST bundle properties
        Bundle bParamsBarcode = new Bundle();
        bParamsBarcode.putString("scanner_selection", "auto");
        bParamsBarcode.putString("scanner_input_enabled", "true");
        bParamsBarcode.putString("decoder_code128", code128Value);
        bParamsBarcode.putString("decoder_ean13", ean13Value);

        bConfigBarcode.putBundle("PARAM_LIST", bParamsBarcode);

        // Associate appropriate activity to profile
        String activityName = new String();
        Bundle appConfig = new Bundle();
        appConfig.putString("PACKAGE_NAME", getPackageName());
        if (profileName.equals(PROFILE1))
        {
            activityName = MainActivity.class.getSimpleName();
        }
        String activityPackageName = getPackageName() + "." + activityName;
        appConfig.putStringArray("ACTIVITY_LIST", new String[] {activityPackageName});
        bMain.putParcelableArray("APP_LIST", new Bundle[]{appConfig});

        // Configure intent output for captured data to be sent to this app
        Bundle bConfigIntent = new Bundle();
        bConfigIntent.putString("PLUGIN_NAME", "INTENT");
        bConfigIntent.putString("RESET_CONFIG", "true");
        // Set params for intent output
        Bundle bParamsIntent = new Bundle();
        bParamsIntent.putString("intent_output_enabled", "true");
        bParamsIntent.putString("intent_action", Datawedeentity.ACTIVITY_INTENT_FILTER_ACTION);
        bParamsIntent.putString("intent_delivery", "2");
        // Bundle "bParamsIntent" within bundle "bConfigIntent"
        bConfigIntent.putBundle("PARAM_LIST", bParamsIntent);
        //KEYSTROKE
//        Bundle bConfigkeystroke = new Bundle();
//        bConfigkeystroke.putString("PLUGIN_NAME", "KEYSTROKE");
//        Bundle bParamsKEYSTROKE = new Bundle();
//        bParamsKEYSTROKE.putString("keystroke_output_enabled","false");
//        bConfigkeystroke.putBundle("PARAM_LIST",bParamsKEYSTROKE);

        // Place both "bConfigBarcode" and "bConfigIntent" bundles into arraylist bundle
        ArrayList<Bundle> bundlePluginConfig = new ArrayList<>();
        bundlePluginConfig.add(bConfigBarcode);
        bundlePluginConfig.add(bConfigIntent);

        // Place bundle arraylist into "bMain" bundle
        bMain.putParcelableArrayList("PLUGIN_CONFIG", bundlePluginConfig);

        Datawedeentity.sendDataWedgeIntentWithExtra(getApplicationContext(),
                Datawedeentity.ACTION_DATAWEDGE, Datawedeentity.EXTRA_SET_CONFIG, bMain);

       // Toast.makeText(getApplicationContext(), "Created profiles.  Check DataWedge app UI.", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}