package com.contec.cmsapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.contec.helper.CustomAlertDialog;
import com.contec.helper.IpUtil;
import com.contec.helper.MyProgressDialog;
import com.contec.helper.UpdateUtil;
import com.contec.helper.Versioninfo;
import com.contec.helper.WhLogger;
import com.contec.helper.XMLParse;
import com.contec.phmsnet.UserNetInterface;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by HP on 2018/5/29.
 */
public class MainActivity extends BaseActivity {

    @BindView(R.id.userid)
    EditText useridText;
    @BindView(R.id.doctorid)
    EditText doctoridText;
    @BindView(R.id.startBtn)
    Button startBtn;
    @BindView(R.id.cms_device_state)
    TextView cmsDeviceState;
    @BindView(R.id.cms_observe_state)
    TextView cmsObserveState;
    @BindView(R.id.text_hint_info)
    TextView textHintInfo;
    @BindView(R.id.startVideo)
    Button startVideo;
    @BindView(R.id.exit_system)
    ImageView exitSystem;
    @BindView(R.id.main_title_view)
    LinearLayout mainTitleView;
    @BindView(R.id.data_source_state)
    TextView dataSourceState;
    @BindView(R.id.main_rect_one)
    LinearLayout mainRectOne;
    @BindView(R.id.main_rect_two)
    LinearLayout mainRectTwo;

    private Context mContext;
    private Intent cmsIntent = null;
    private Boolean indexState = false;//服务状态  false 空闲 true 开启
    private Dialog mProgressDialog;
    private String apkURL = "";
    private Boolean observerOnline = false;
    private Boolean cmsOnline = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     //   requestWindowFeature(Window.FEATURE_NO_TITLE);
        mContext = this;
        setContentView(R.layout.main_activity_layout);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ButterKnife.bind(this);
        //   EventBus.getDefault().register(this);
        useridText.setText(MyApplication.getInstance().getUsername());
        doctoridText.setText(MyApplication.getInstance().getSharedPreferences(mContext, "doctorid"));
        cmsIntent = new Intent(this, CMSService.class);
        checkNeedUpdate(mContext);
        WhLogger.isApkInDebug(mContext);
        startBtn.setText("启动服务");
        String ipStr = IpUtil.getIPAddress(this);
        textHintInfo.setText(textHintInfo.getText() + "本机当前IP地址:" + ipStr);
        setEditTextReadOnly(useridText);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int heigthDynamic = dm.heightPixels;
        int weightDynamic = dm.widthPixels;

        heigthDynamic = (heigthDynamic-150)/3;
        weightDynamic = (weightDynamic-20)/ 2;
        heigthDynamic = heigthDynamic > weightDynamic? weightDynamic:heigthDynamic;
        WhLogger.e("heigthDynamic",heigthDynamic+"dp");
        LinearLayout.LayoutParams linearParams =(LinearLayout.LayoutParams) mainTitleView.getLayoutParams();
        linearParams.height = heigthDynamic;
        linearParams.height = heigthDynamic-50;
        mainTitleView.setLayoutParams(linearParams);
        LinearLayout.LayoutParams  linearParamsOne =(LinearLayout.LayoutParams) mainRectOne.getLayoutParams();
        linearParamsOne.height = heigthDynamic;
        mainRectOne.setLayoutParams(linearParamsOne);

        LinearLayout.LayoutParams  linearParamsTwo =(LinearLayout.LayoutParams) mainRectOne.getLayoutParams();
        linearParamsTwo.height = heigthDynamic;
        mainRectTwo.setLayoutParams(linearParamsTwo);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //关闭视频后,更新下主界面信息
        if (requestCode == 1 && resultCode == 2) {
            Integer observerState = data.getIntExtra("observer", -1);//观察者状态
            Integer cmsState = data.getIntExtra("cms", -1);//监护仪状态
            Integer serviceState = data.getIntExtra("service", -1);//服务器状态

           if(observerState == 1) observerOnline = true;
           if(observerState == 0) observerOnline = false;
           if(cmsState == 0) cmsOnline = false;
           if(cmsState == 1) cmsOnline = true;

            if (observerState == 1) {
                obvserOnLine(true);
            } else if (observerState == 0) {
                obvserOnLine(false);
            }
            if (cmsState == 1) {
                cmsDeviceState.setText("在线");
                cmsDeviceState.setTextColor(getResources().getColor(R.color.onlineGreen));
            } else if (cmsState == 0) {
                cmsDeviceState.setText("离线");
                cmsDeviceState.setTextColor(getResources().getColor(R.color.offlineRed));

                dataSourceState.setText("离线");
                dataSourceState.setTextColor(getResources().getColor(R.color.offlineRed));
            }

            if (cmsOnline &&!observerOnline){
                dataSourceState.setText("就绪...");
                dataSourceState.setTextColor(getResources().getColor(R.color.jiuxuYellow));
            }
            if (cmsOnline && observerOnline){
                dataSourceState.setText("数据传输中...");
                dataSourceState.setTextColor(getResources().getColor(R.color.onlineGreen));
            }

            if (serviceState == 0) {
                switchBtnState(false);
                obvserOnLine(false);
                cmsDeviceState.setText("离线");
                cmsDeviceState.setTextColor(getResources().getColor(R.color.offlineRed));
                dataSourceState.setText("离线");
                dataSourceState.setTextColor(getResources().getColor(R.color.offlineRed));
                if (cmsIntent != null) stopService(cmsIntent);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        setPlayPrepare();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    //    WhLogger.e("mainactivity_stop","---stop");
    }

    @OnClick({R.id.startBtn, R.id.startVideo})
    public void onViewClicked(Button button) {
        switch (button.getId()) {
            case R.id.startBtn:
                String useridValue = useridText.getText().toString().trim();
                String doctoridValue = doctoridText.getText().toString().trim();

                if (useridValue == null || "".equals(useridValue)) {
                    Toast.makeText(mContext, "机构号不能为空！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (doctoridValue == null || "".equals(doctoridValue)) {
                    Toast.makeText(mContext, "专家号不能为空！", Toast.LENGTH_SHORT).show();
                    return;
                }
                MyApplication.getInstance().setSharedPreferences(mContext, "doctorid", doctoridValue);
                MyApplication.getInstance().setCaseid(useridValue + doctoridValue);

                indexState = !indexState;
                switchBtnState(indexState);
                if (indexState) {
                    if (mProgressDialog == null) {
                        mProgressDialog = MyProgressDialog.createLoadingDialog(mContext, "正在执行...");
                    }
                    mProgressDialog.show();
                    startBtn.setText("服务正在启动");
                    startService(cmsIntent);
                } else {
                    stopService(cmsIntent);
                    if (mProgressDialog == null) {
                        mProgressDialog = MyProgressDialog.createLoadingDialog(mContext, "正在执行...");
                    }
                    mProgressDialog.show();
                    startBtn.setText("服务正在关闭");
                    startBtn.setEnabled(false);
                    obvserOnLine(false);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                                mHandler.obtainMessage(101).sendToTarget();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
                break;
            case R.id.startVideo:

                String name = MyApplication.getInstance().getSharedPreferences(mContext, "room");
                if (!name.isEmpty()) {
                    Intent intent = new Intent(MainActivity.this, VideoChatViewActivity.class);
                    startActivityForResult(intent, 1);
                } else {
                    final EditText et = new EditText(this);
                    new AlertDialog.Builder(this).setTitle("请输入视频通话房间名称")
                            .setIcon(R.drawable.login_title)
                            .setView(et)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //按下确定键后的事件
                                    String roomName = et.getText().toString().trim();
                                    if (roomName.isEmpty())
                                        Toast.makeText(mContext, "房间名称/房间号不能为空", Toast.LENGTH_SHORT).show();
                                    else {
                                        MyApplication.getInstance().setSharedPreferences(mContext, "room", roomName);
                                        Intent intent = new Intent(MainActivity.this, VideoChatViewActivity.class);
                                        startActivityForResult(intent, 1);
                                    }
                                }
                            }).setNegativeButton("取消", null).show();
                }
                break;
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 101:
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }
                    startBtn.setEnabled(true);
                    startBtn.setText("启动服务");
                    break;
            }
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CMSService.MessageEvent event) {
        //    if (mProgressDialog != null && mProgressDialog.isShowing()) {
        //       mProgressDialog.dismiss();
        //   }
        if (event.getid().equals("10010")) {//连接服务器成功ONLINE
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            switchBtnState(true);
        }
        if (event.getid().equals("10011")) {//连接服务器失败
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            switchBtnState(false);
            obvserOnLine(false);
            if (cmsIntent != null) stopService(cmsIntent);
            String msg = event.getmsg();
            final CustomAlertDialog dg = new CustomAlertDialog(mContext);
            dg.setTitle("系统提示");
            dg.setMessage(msg);
            dg.setPositiveButton("确定", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dg.dismiss();
                }
            });

        }
        if (event.getid().equals("10000")) {//监护仪上线
            switchBtnState(true);
            cmsOnline = true;
            cmsDeviceState.setText("在线");
            cmsDeviceState.setTextColor(getResources().getColor(R.color.onlineGreen));
            if (observerOnline){
                dataSourceState.setText("数据传输中...");
                dataSourceState.setTextColor(getResources().getColor(R.color.onlineGreen));
            }else {
                dataSourceState.setText("就绪...");
                dataSourceState.setTextColor(getResources().getColor(R.color.jiuxuYellow));
            }
        }
        if (event.getid().equals("10001")) {//socket异常/监护仪下线
            cmsOnline = false;
            cmsDeviceState.setText("离线");
            cmsDeviceState.setTextColor(getResources().getColor(R.color.offlineRed));

            dataSourceState.setText("离线");
            dataSourceState.setTextColor(getResources().getColor(R.color.offlineRed));

            WhLogger.e("MainActivity-onMessageEvent", event.getmsg());
        }
        if (event.getid().equals("20000")) {//观察者上线
            //   WhLogger.e(event.getid(),event.getmsg());
            obvserOnLine(true);
        }
        if (event.getid().equals("20001")) {//观察者下线
            obvserOnLine(false);
            if (cmsOnline){
                dataSourceState.setText("就绪...");
                dataSourceState.setTextColor(getResources().getColor(R.color.jiuxuYellow));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //   EventBus.getDefault().unregister(this);
        if (cmsIntent != null) stopService(cmsIntent);
    }

    public void switchBtnState(Boolean index) {
        indexState = index;
        if (index) {//服务状态
            startBtn.setText("服务已启动");
            startBtn.setBackgroundResource(R.drawable.main_child_bg_blue);
        } else {//空闲状态
            startBtn.setText("开启服务");
            startBtn.setBackgroundResource(R.drawable.main_child_bg_green);
        }
    }

    public void obvserOnLine(Boolean bl) {
        observerOnline = bl;
        if (bl) {
            cmsObserveState.setText("在线");
            cmsObserveState.setTextColor(getResources().getColor(R.color.onlineGreen));//#0281D0 #35C643
        } else {
            cmsObserveState.setText("离线");
            cmsObserveState.setTextColor(getResources().getColor(R.color.offlineRed));
        }
    }

    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(mContext, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                final CustomAlertDialog ad = new CustomAlertDialog(mContext);
                ad.setTitle("系统提示");
                ad.setMessage("您是否要退出程序？");
                ad.setPositiveButton("是", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (cmsIntent != null) stopService(cmsIntent);
                        ad.dismiss();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                UserNetInterface.Logout();
                            }
                        }).start();
                        finish();
                        System.exit(0);
                    }
                });
                ad.setNegativeButton("否", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ad.dismiss();
                    }
                });
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public static void setEditTextReadOnly(TextView view) {
        view.setTextColor(Color.parseColor("#666666"));   //设置只读时的文字颜色
        if (view instanceof EditText) {
            view.setCursorVisible(false);      //设置输入框中的光标不可见
            view.setFocusable(false);           //无焦点
            view.setFocusableInTouchMode(false);     //触摸时也得不到焦点
        }
    }

    public void checkNeedUpdate(final Context context) {
        String path = Environment.getExternalStorageDirectory() + "/";
        final String xmlPath = path + MyApplication.APP_DATA_DIR + "/Update";
        File fileDir = new File(xmlPath);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        UpdateUtil.getInstance().download(MyApplication.UPDATE_XML_URL, xmlPath, "version.xml", new UpdateUtil.OnDownloadListener() {
            @Override
            public void onDownloadSuccess(String path) {
                File file = new File(xmlPath, "version.xml");
                if (!file.exists()) return;
                XMLParse m_xmlparser = new XMLParse();
                Versioninfo versioninfo = new Versioninfo();
                try {
                    versioninfo = m_xmlparser.parseVersionXml(xmlPath + "/version.xml");
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                if (versioninfo != null) {
                    int serviceCode = Integer.valueOf(versioninfo.getVersion());
                    String apkUrl = "http://data2.contec365.com" + versioninfo.getPath() + "/" + versioninfo.getFname();
                    final String filename = versioninfo.getFname();
                    if (serviceCode > UpdateUtil.getInstance().getVersionCode(context)) {
                        UpdateUtil.getInstance().download(apkUrl, xmlPath, filename, new UpdateUtil.OnDownloadListener() {
                            @Override
                            public void onDownloadSuccess(String path) {
                                WhLogger.e("升级文件下载完成", filename);
                            }

                            @Override
                            public void onDownloading(int progress) {
                                WhLogger.e("升级文件下载进度...", progress + "");
                            }

                            @Override
                            public void onDownloadFailed() {
                                WhLogger.e("APk升级文件下载失败", "APk升级文件下载失败");
                            }
                        });
                    } else {
                        return;//不需要下载
                    }
                }
            }

            @Override
            public void onDownloading(int progress) {

            }

            @Override
            public void onDownloadFailed() {
                WhLogger.e("XML升级文件下载失败", "XML升级文件下载失败");
            }
        });
    }

    @OnClick(R.id.exit_system)
    public void onViewClicked() {
        final CustomAlertDialog ad = new CustomAlertDialog(mContext);
        ad.setTitle("系统提示");
        ad.setMessage("您是否要退出程序？");
        ad.setPositiveButton("是", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cmsIntent != null) stopService(cmsIntent);
                ad.dismiss();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        UserNetInterface.Logout();
                    }
                }).start();
                finish();
                System.exit(0);
            }
        });
        ad.setNegativeButton("否", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ad.dismiss();
            }
        });
    }

}
