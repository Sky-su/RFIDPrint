package com.example.rfidprint;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class IpActivity extends AppCompatActivity {
    TextView showIp;
    EditText editip;
    Button ApplyButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ip);
        initialize();
        String ip = nowIp();
        showIp.setText("目前的IP:"+ip);
        ApplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intoIp();
            }
        });
    }
    private void initialize(){
        showIp = (TextView) findViewById(R.id.ipshow);
        editip = (EditText) findViewById(R.id.ipedit);
        editip.setInputType(InputType.TYPE_CLASS_NUMBER);
        String digits = "0123456789.";
        editip.setKeyListener(DigitsKeyListener.getInstance(digits));
        ApplyButton = findViewById(R.id.buttonip);
    }
    public void intoIp(){
        SharedPreferences.Editor editor = getSharedPreferences("ipData", MODE_PRIVATE).edit();
        editor.putString("ip", editip.getText().toString().trim());
       // Toast.makeText(this, editip.getText().toString().trim(), Toast.LENGTH_SHORT).show();
        editor.commit();
    }
    public String nowIp(){
        SharedPreferences pref = getSharedPreferences("ipData", MODE_PRIVATE);
        String state = pref.getString("ip", "");
        if(state.isEmpty()) return "未设置IP";
        return state;
    }
}