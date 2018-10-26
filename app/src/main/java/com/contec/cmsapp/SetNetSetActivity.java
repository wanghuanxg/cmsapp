package com.contec.cmsapp;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.contec.helper.IniFileIO;


public class SetNetSetActivity extends Activity {
    private EditText txt_Address, txt_Port, txt_WebIo_Address, txt_WebIo_Port,txt_video_name;
    private Button btnSave, btnBack;
    private String strAddressValue, strPortValue, strWebIoAddressValue, strWebIoPortValue;
    private String strIniPath;
    private IniFileIO inifile;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.set_netset);
        context = this;
        LoadValue();
        InitCtrl();
    }

    public void LoadValue() {

        strIniPath = Environment.getExternalStorageDirectory() + "/" + MyApplication.APP_DATA_DIR + "/PhmsConfig.ini";

        inifile = null;
        try {
            inifile = new IniFileIO(strIniPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        strAddressValue = inifile.getValue("NET", "SERVER_NAME");
        strPortValue = inifile.getValue("NET", "PORT");
        strWebIoAddressValue = inifile.getValue("SOCKETIO", "HOST");
        strWebIoPortValue = inifile.getValue("SOCKETIO", "PORT");
    }

    public void InitCtrl() {

        txt_Address = (EditText) findViewById(R.id.edit_address);
        txt_Port = (EditText) findViewById(R.id.edit_port);
        txt_Address.setText(strAddressValue);
        txt_Port.setText(strPortValue);

        txt_WebIo_Address = (EditText) findViewById(R.id.webid_edit_address);
        txt_WebIo_Port = (EditText) findViewById(R.id.webid_edit_port);
        txt_WebIo_Address.setText(strWebIoAddressValue);
        txt_WebIo_Port.setText(strWebIoPortValue);

        btnSave = (Button) findViewById(R.id.set_save_button);
        btnBack = (Button) findViewById(R.id.set_back_button);
        btnSave.setOnClickListener(new ButtonListener());
        btnBack.setOnClickListener(new ButtonListener());

        txt_video_name= (EditText) findViewById(R.id.video_name);
        txt_video_name.setText(MyApplication.getInstance().getSharedPreferences(context,"room"));
    }

    public void SaveValue() {

        MyApplication.getInstance().setSharedPreferences(context,"room",txt_video_name.getText().toString().trim());
        strAddressValue = txt_Address.getText().toString();
        strPortValue = txt_Port.getText().toString();
        strWebIoAddressValue = txt_WebIo_Address.getText().toString();
        strWebIoPortValue = txt_WebIo_Port.getText().toString();
        if (strPortValue.equals("443")) {
            inifile.putValue("NET", "SSL", "1");
        } else if (strPortValue.equals("80")) {
            inifile.putValue("NET", "SSL", "0");
        }
        inifile.putValue("NET", "SERVER_NAME", strAddressValue);
        inifile.putValue("NET", "PORT", strPortValue);
        inifile.putValue("NET", "IP", "");
        inifile.putValue("SOCKETIO", "HOST", strWebIoAddressValue);
        inifile.putValue("SOCKETIO", "PORT", strWebIoPortValue);
        try {
            inifile.commit();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "保存设置失败！", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(context, "保存设置成功！", Toast.LENGTH_SHORT).show();
        finish();
    }

    private class ButtonListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.set_save_button:
                    SaveValue();
                    break;
                case R.id.set_back_button:
                    finish();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
