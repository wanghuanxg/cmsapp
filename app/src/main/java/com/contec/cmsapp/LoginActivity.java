package com.contec.cmsapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.contec.helper.CustomAlertDialog;
import com.contec.helper.IniFileIO;
import com.contec.helper.MyProgressDialog;
import com.contec.helper.UpdateUtil;
import com.contec.helper.UserInfo;
import com.contec.helper.Versioninfo;
import com.contec.helper.WhLogger;
import com.contec.helper.XMLParse;
import com.contec.phmsnet.CommonNetInterface;
import com.contec.phmsnet.UserNetInterface;

import org.xmlpull.v1.XmlPullParserException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by HP on 2018/5/28.
 */
public class LoginActivity extends Activity {

    @BindView(R.id.server_setting)
    TextView serverSettingBtn;
    @BindView(R.id.login)
    Button loginBtn;
    @BindView(R.id.username)
    EditText username;
    @BindView(R.id.password)
    EditText password;
    private Context mContext;

    private String Accountspath;
    private Boolean loginstart = false;
    private Dialog mDialog;
    private long timeout = 0;
    private long threadId ;


    private final int SDK_PERMISSION_REQUEST = 127;
    private String permissionInfo = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        mContext = this;
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        if (InitPhmsNetObj(this) == 1) {
            //	Log.e("InitDoctorNetObj", "InitDoctorNetObj ====1");
        } else {
            Toast.makeText(mContext, "初始化失败，请稍后重试!", Toast.LENGTH_SHORT).show();
            return;
        }

        username.setText(MyApplication.getInstance().getSharedPreferences(mContext,"username"));
        password.setText(MyApplication.getInstance().getSharedPreferences(mContext,"password"));

        getPersimmions();
        //检查是否有升级文件
        checkUpdate();

    }
    @TargetApi(23)
    private void getPersimmions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissions = new ArrayList<String>();
            /***
             * 定位权限为必须权限，用户如果禁止，则每次进入都会申请
             */
            // 定位精确位置
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
			/*
			 * 读写权限和电话状态权限非必要权限(建议授予)只会申请一次，用户同意或者禁止，只会弹一次
			 */
            // 读写权限
            if (addPermission(permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissionInfo += "Manifest.permission.WRITE_EXTERNAL_STORAGE Deny \n";
            }
            // 读取电话状态权限
            if (addPermission(permissions, Manifest.permission.READ_PHONE_STATE)) {
                permissionInfo += "Manifest.permission.READ_PHONE_STATE Deny \n";
            }

            if (addPermission(permissions, Manifest.permission.RECORD_AUDIO)) {
                permissionInfo += "Manifest.permission.RECORD_AUDIO Deny \n";
            }
            if (addPermission(permissions, Manifest.permission.CAMERA)) {
                permissionInfo += "Manifest.permission.CAMERA Deny \n";
            }
            if(!permissionInfo.isEmpty())WhLogger.e("permissionInfo",permissionInfo);
            if (permissions.size() > 0) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), SDK_PERMISSION_REQUEST);
            }
        }
    }

    @TargetApi(23)
    private boolean addPermission(ArrayList<String> permissionsList, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) { // 如果应用没有获得对应权限,则添加到列表中,准备批量申请
            if (shouldShowRequestPermissionRationale(permission)){
                return true;
            }else{
                permissionsList.add(permission);
                return false;
            }

        }else{
            return true;
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // TODO Auto-generated method stub
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }
    @OnClick({R.id.server_setting, R.id.login})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.server_setting:
                Intent intentnetset = new Intent(LoginActivity.this, SetNetSetActivity.class);
                startActivity(intentnetset);
                break;
            case R.id.login:
                String userNameValue = username.getText().toString();
                String passwordValue = password.getText().toString();

                if (userNameValue == null || "".equals(userNameValue)) {
                    Toast.makeText(mContext, "用户名不能为空！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (passwordValue == null || "".equals(passwordValue)) {
                    Toast.makeText(mContext, "密码不能为空！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!MyApplication.getInstance().isNetworkAvailable(mContext)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setTitle("提示");
                    builder.setMessage("网络不可用!");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                            startActivity(intent);
                        }
                    });
                    builder.show();
                } else {
                    loginstart = true;
                    mHandler.obtainMessage(MSG_LOGIN_START, "登录中，请稍后...").sendToTarget();
                    Login(userNameValue, passwordValue);
                }
                break;
        }
    }
    private void Login(final String username, final String password) {
        new Thread() {
            @Override
            public void run() {
                // TODO 自动生成的方法存根
                super.run();
                if (MyApplication.getInstance().isNetworkAvailable(mContext)) {
                    StringBuffer ip = new StringBuffer();
                    String loginEC ="";
                    String path = Environment.getExternalStorageDirectory() + "/";
                    File file = new File(path, MyApplication.APP_DATA_DIR + "/Accounts/" + username + "/Tmp");
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    Accountspath = file.getPath() + "/";
                    String localPathString = Accountspath + "LoginInfo.XML";
              //      WhLogger.e("ttttttttt",localPathString);
                    threadId = CommonNetInterface.GetThreadself();
                    String[] ips = new String[]{""};
                    int ErrorCode = UserNetInterface.UserLogin(localPathString,username,password,"1","", ips);
                    for(int i = 0; i< ips.length; i++)
                    {
                        ip.append(ips[i]);
                    }
                    loginEC = String.valueOf(ErrorCode);
                    // 登录成功
                    WhLogger.e("登录接口=====loginEC返回码:", String.valueOf(ErrorCode));
                    if ("100000".equals(loginEC)) {
                        try {
                            IniFileIO iniFileIO = new IniFileIO(Environment.getExternalStorageDirectory() + "/"+MyApplication.APP_DATA_DIR+"/PhmsConfig.ini");
                            iniFileIO.putValue("NET","IP",ip.toString());
                            iniFileIO.commit();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mHandler.obtainMessage(MSG_LOGIN_SUCCESS).sendToTarget();
                    }
                    else if ("100201".equals(loginEC)) {
                        mHandler.obtainMessage(MSG_LOGIN_FAIL_OTHER_REASON, "参数校验错误").sendToTarget();
                    }
                    else if ("100202".equals(loginEC)) {
                        mHandler.obtainMessage(MSG_LOGIN_FAIL_OTHER_REASON, "数据库操作错误").sendToTarget();
                    }
                    else if ("100203".equals(loginEC)) {
                        mHandler.obtainMessage(MSG_LOGIN_FAIL_OTHER_REASON, "用户名不存在").sendToTarget();
                    }
                    else if ("100204".equals(loginEC)) {
                        mHandler.obtainMessage(MSG_LOGIN_FAIL_OTHER_REASON, "密码不正确").sendToTarget();
                    }
                    else if ("100205".equals(loginEC)) {
                        mHandler.obtainMessage(MSG_LOGIN_FAIL_OTHER_REASON, "账号已停用").sendToTarget();
                    }
                    else {
                        mHandler.obtainMessage(MSG_LOGIN_FAIL_OTHER_REASON, "登录异常，未知错误").sendToTarget();
                    }
                }
            }

        }.start();

    }
    private static final int MSG_LOGIN_START = 0X00001001;// 开始登录操作
    private static final int MSG_LOGIN_SUCCESS = 0X00001002;// 登录成功
    private static final int MSG_LOGIN_FAIL = 0X00001003;// 通过返回码判断失败原因
    private static final int MSG_LOGIN_FAIL_OTHER_REASON = 0X00001004;// 直接返回失败原因
    private static final int MSG_LOGIN_FAIL_NO_SIGN = 200101;// 直接返回失败原因
    private static final int MSG_MANUAL_STOP = 200102;// 手动终止登陆
    private static final int MSG_GO_TO_MAIN_ACTIVITY = 200103;// 跳转页面
    private Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_GO_TO_MAIN_ACTIVITY:
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    if (mDialog != null && mDialog.isShowing()) {
                        mDialog.dismiss();
                    }
                    finish();
                    break;
                case MSG_LOGIN_START:
                    if (mDialog == null) {
                        mDialog = MyProgressDialog.createLoadingDialog(mContext, msg.obj.toString());
                    }
                    mDialog.show();
                    new Thread(){
                        @Override
                        public void run() {
                            // TODO 自动生成的方法存根
                            while (loginstart) {
                                try {
                                    sleep(1000);
                                    timeout+= 1000;
                                    if (timeout >= 20000) {
                                        mHandler.obtainMessage(MSG_MANUAL_STOP, "Timeout").sendToTarget();
                                    }
                                } catch (InterruptedException e) {
                                    // TODO 自动生成的 catch 块
                                    e.printStackTrace();
                                }
                            }
                            super.run();
                        }
                    }.start();
                    break;
                case MSG_LOGIN_FAIL_NO_SIGN:
                    if (mDialog != null && mDialog.isShowing()) {
                        mDialog.dismiss();
                    }
                    Toast.makeText(mContext, "签名文件验证失败!", Toast.LENGTH_SHORT).show();
                    break;
                case MSG_LOGIN_SUCCESS:
                    loginstart = false;
                    Toast.makeText(mContext, "登录成功！", Toast.LENGTH_SHORT).show();
                    MyApplication.getInstance().setUsername(username.getText().toString());
                    MyApplication.getInstance().setPasswortd(password.getText().toString());
                    MyApplication.getInstance().setSharedPreferences(mContext,"username",username.getText().toString());
                    MyApplication.getInstance().setSharedPreferences(mContext,"password",password.getText().toString());
                    MyApplication.getInstance().setAccountspath(Accountspath);
                    XMLParse m_xmlparser = new XMLParse();
                    UserInfo user = new UserInfo();
                    try {
                        user = m_xmlparser.parseUser(Accountspath + "LoginInfo.XML");
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    MyApplication.getInstance().setName(user.getName());
                    new Thread(){
                        @Override
                        public void run() {
                            // TODO 自动生成的方法存根
                            String[] session = {""};
                            StringBuffer sessionId = new StringBuffer();
                            int errorCode = CommonNetInterface.GetSessionId(session, 512);
                            if (errorCode == 100000) {
                                for (int i = 0; i < session.length; i++) {
                                    sessionId.append(session[i]);
                                }
                                MyApplication.getInstance().setSessionId(sessionId.toString());
                                mHandler.obtainMessage(MSG_GO_TO_MAIN_ACTIVITY, "跳转页面").sendToTarget();
                            }
                            super.run();
                        }
                    }.start();
                    break;
                case MSG_LOGIN_FAIL:
                    String errorCode = msg.obj.toString();
                    Toast.makeText(mContext, errorCode, Toast.LENGTH_SHORT).show();
                    if (mDialog != null && mDialog.isShowing()) {
                        mDialog.dismiss();
                    }
                    break;
                case MSG_LOGIN_FAIL_OTHER_REASON:
                    if (mDialog != null && mDialog.isShowing()) {
                        mDialog.dismiss();
                    }
                    if (msg.obj != null) {
                        Toast.makeText(mContext, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case MSG_MANUAL_STOP:
                    if (mDialog != null && mDialog.isShowing()) {
                        mDialog.dismiss();
                    }
                    loginstart = false;
                    Toast.makeText(mContext, "服务器连接失败", Toast.LENGTH_SHORT).show();
                    new Thread(){
                        @Override
                        public void run() {
                            // TODO 自动生成的方法存根
                            CommonNetInterface.AbortSessionByThreadId(threadId);
                            timeout = 0;
                            super.run();
                        }
                    }.start();
                    break;
                default:
                    break;
            }
        }
    };
    public int InitPhmsNetObj(Context context) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            String path = Environment.getExternalStorageDirectory() + "/";
            File file = new File(path, MyApplication.APP_DATA_DIR);
            if (!file.exists()) {
                file.mkdir();
            }// 判断是否有配置文件 如果没有 写入配置文件
            File configFile = new File(file, "PhmsConfig.ini");
            if (!configFile.exists()) {

                boolean configFileCopysuccessful = copyAssetFileToPath(context, "PhmsConfig.ini", file.getAbsolutePath());
            }
            int ErrorCode = CommonNetInterface.SetSDcardDir(file.getAbsolutePath() + "/");//sd卡软件目录
            ErrorCode = CommonNetInterface.SetTerminalType("contec-ctype7/1.0");//android设备终端类型
            if (ErrorCode == 100000) {
                new Thread() {
                    public void run() {
                        int Error = CommonNetInterface.InitializeNetLibObject();
                    }

                    ;
                }.start();
            }
            return 1;
        } else {
            return 0;

        }
    }

    public static boolean copyAssetFileToPath(Context context, String fileName, String path) {

        boolean isSuccessful = false;
        DataInputStream dis = null;
        DataOutputStream dos = null;
        try {
            InputStream is = context.getResources().getAssets().open(fileName);
            dis = new DataInputStream(is);
            byte[] dst = new byte[dis.available()];
            dis.readFully(dst);
            dos = new DataOutputStream(new FileOutputStream(new File(path, fileName)));
            dos.write(dst);
            isSuccessful = true;
        } catch (IOException e1) {
            e1.printStackTrace();
            isSuccessful = false;
        } finally {
            try {
                if (dis != null) {
                    dis.close();
                    dis = null;
                }
                if (dos != null) {
                    dos.close();
                    dos = null;
                }
            } catch (IOException e1) {
            }
        }

        return isSuccessful;
    }

    public void checkUpdate(){
        final String xmlPath = Environment.getExternalStorageDirectory() + "/" + MyApplication.APP_DATA_DIR + "/Update";
        File fileDir = new File(xmlPath);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        File xmlFile = new File(xmlPath,"version.xml");
        if (xmlFile.exists()){
            XMLParse xmlParse = new XMLParse();
            try {
                Versioninfo versioninfo = xmlParse.parseVersionXml(xmlFile.getPath());
                if (versioninfo != null){
                    int serviceCode = Integer.valueOf(versioninfo.getVersion());
                    int localCode = UpdateUtil.getInstance().getVersionCode(mContext);
                    WhLogger.e("serviceCode:localCode",serviceCode+":"+localCode);
                    if (serviceCode > localCode){
                        final File apkPath = new File(xmlPath,versioninfo.getFname());
                        if (!apkPath.exists()){
                            WhLogger.e("升级包文件不存在",apkPath.getPath());
                            return;
                        }
                        Long size = versioninfo.getSize();
                        long apkSize = UpdateUtil.getInstance().getFileSize(apkPath);
                        if (size == apkSize){
                            //有升级包，可以升级
                            final CustomAlertDialog ad = new CustomAlertDialog(mContext);
                            ad.setTitle("系统升级");
                            ad.setMessage("有新版本，请立即更新！");
                            ad.setPositiveButton("确定", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ad.dismiss();
                                    Intent i = new Intent(Intent.ACTION_VIEW);
                                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    i.setDataAndType(Uri.parse("file://" + apkPath.getAbsolutePath()), "application/vnd.android.package-archive");
                                    mContext.startActivity(i);
                                }
                            });
                        }
                    }else {
                        File fl = new File(xmlPath,versioninfo.getFname());
                        if (fl.exists()){
                            WhLogger.e("已经是最新版本删除本地升级包",fl.getAbsolutePath());
                            MyApplication.delete(fl.getAbsolutePath());
                        }
                    }
                }
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
