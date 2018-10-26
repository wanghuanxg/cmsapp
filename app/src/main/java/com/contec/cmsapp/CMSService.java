package com.contec.cmsapp;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Base64;
import android.util.Xml;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.contec.helper.IniFileIO;
import com.contec.helper.WhLogger;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import io.socket.client.IO;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.EngineIOException;

public class CMSService extends Service {
    private io.socket.client.Socket mIoSocket;
    private ServerSocket myCMSServer;
    private Socket myCMSSocket;
    private InputStream is = null ;
    private Boolean bExit = false;
    private String locationInfo = "";//坐标信息经纬度
    private String latitude = "";
    private String longitude ="";
    private String userInfo = "";
    private LocationService locationService;
    private Context mContext;
    private Boolean blObserverOnline = false;
    private String latestObserverSessionId = "";
    public CMSService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        try {
            JSONObject obj = new JSONObject();
            obj.put("type", "0");//数据源
            obj.put("id", MyApplication.getInstance().getUsername());
            obj.put("name", MyApplication.getInstance().getName());
            obj.put("caseid", MyApplication.getInstance().getCaseid());
            obj.put("sessionid", MyApplication.getInstance().getSessionId());
            userInfo = obj.toString();
            WhLogger.e("userInfo", userInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        initSocketClient();

        initTcpServer();

        locationService = new LocationService(getApplicationContext());
        locationService.registerListener(mLocationListener);
        locationService.start();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        locationService.unregisterListener(mLocationListener); //注销掉监听
        locationService.stop(); //停止定位服务
        if (mIoSocket != null) {
            mIoSocket.emit("OFFLINE", userInfo);
            mIoSocket.disconnect();
            mIoSocket.off(io.socket.client.Socket.EVENT_CONNECT);
            mIoSocket.off(io.socket.client.Socket.EVENT_DISCONNECT);
            mIoSocket.off(io.socket.client.Socket.EVENT_CONNECT_TIMEOUT);
            mIoSocket.off("ONLINE");
            mIoSocket.off("OFFLINE");
            mIoSocket.off("ERROR");
            mIoSocket.off("DATA");
        }
        bExit = true;
        try {
        //    WhLogger.e("cmsserver-onDestroy", "监护仪断开连接");
            //      EventBus.getDefault().post(new MessageEvent("10001", "监护仪断开连接"));
            if (myCMSServer != null) myCMSServer.close();
            if (myCMSSocket != null) myCMSSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 101:
                    mIoSocket.off(io.socket.client.Socket.EVENT_CONNECT);
                    mIoSocket.off(io.socket.client.Socket.EVENT_DISCONNECT);
                    mIoSocket.off(io.socket.client.Socket.EVENT_CONNECT_TIMEOUT);
                    mIoSocket.off("ONLINE");
                    mIoSocket.off("OFFLINE");
                    mIoSocket.off("ERROR");
                    mIoSocket.off("DATA");
                    initSocketClient();
                    break;
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public void initSocketClient(){
        String strUrl = "http://";
        try {
            IniFileIO iniFileIO = new IniFileIO(Environment.getExternalStorageDirectory() + "/" + MyApplication.APP_DATA_DIR + "/PhmsConfig.ini");
            String host = iniFileIO.getValue("SOCKETIO", "HOST");
            String port = iniFileIO.getValue("SOCKETIO", "PORT");
            String path = iniFileIO.getValue("SOCKETIO", "PATH");
            strUrl += host + ":" + port + path;
         //   strUrl = "http://47.97.238.209:3000";//阿里云
         //   strUrl = "http://45.62.106.37:3000";

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "PhmsConfig.ini配置文件异常", Toast.LENGTH_SHORT).show();
        }
        try {
            WhLogger.e("socketIO-address", strUrl);
            mIoSocket = IO.socket(strUrl);
            mIoSocket.on(io.socket.client.Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {  //连接成功
                    WhLogger.e("io.socket.client.Socket.EVENT_CONNECT", "EVENT_CONNECT");
                }
            }).on(io.socket.client.Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) { //连接断开
                 //   mHandler.obtainMessage(101).sendToTarget();
                    mIoSocket.off(io.socket.client.Socket.EVENT_DISCONNECT);
                    EventBus.getDefault().post(new MessageEvent("10011", "服务器连接已断开,请检查网络重试"));
                    WhLogger.ErrorLog(MyApplication.getInstance().getUsername(),"CMSService.java","服务器连接已断开---我要重新连","io.socket.client.Socket.EVENT_DISCONNECT");
                    WhLogger.e("io.socket.client.Socket.EVENT_DISCONNECT", "服务器连接已断开---我要重新连");
                }
            }).on(io.socket.client.Socket.EVENT_RECONNECT_FAILED, new Emitter.Listener() {
                @Override
                public void call(Object... args) { //连接失败
                    mIoSocket.off(io.socket.client.Socket.EVENT_RECONNECT_FAILED);
                    mIoSocket.disconnect();
                    EventBus.getDefault().post(new MessageEvent("10011", "服务器连接失败,请检查网络重试"));
                    WhLogger.e("io.socket.client.Socket.EVENT_RECONNECT_FAILED", "EVENT_RECONNECT_FAILED");
                }
            }).on(io.socket.client.Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) { //连接错误
                    mIoSocket.off(io.socket.client.Socket.EVENT_CONNECT_ERROR);
                    mIoSocket.disconnect();
                    EventBus.getDefault().post(new MessageEvent("10011", "网络异常,服务器连接错误"));
                    WhLogger.e("io.socket.client.Socket.EVENT_CONNECT_ERROR", "服务器连接错误");
                }
            }).on(io.socket.client.Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {//连接超时
                    mIoSocket.off(io.socket.client.Socket.EVENT_CONNECT_TIMEOUT);
                    EventBus.getDefault().post(new MessageEvent("10011", "服务器连接超时,请检查网络重试"));
                    WhLogger.e("io.socket.client.Socket.EVENT_CONNECT_TIMEOUT", "服务器连接超时");
                    WhLogger.ErrorLog(MyApplication.getInstance().getUsername(), "CMSService.java", "服务器连接超时", "io.socket.client.Socket.EVENT_CONNECT_TIMEOUT");
                }
            }).on("ONLINE", new Emitter.Listener() {//上线
                @Override
                public void call(Object... args) {
                    String str = (String) args[0];
                    WhLogger.e("io.socket.client.Socket.ONLINE", str.toString());
                    JSONObject obj = null;
                    try {
                        obj = new JSONObject(str);
                        String doctroid = MyApplication.getInstance().getSharedPreferences(mContext,"doctorid");
                        String remoteId = obj.getString("id");
                        if (remoteId.toUpperCase().equals(doctroid.toUpperCase())) {
                            EventBus.getDefault().post(new MessageEvent("20000", "观察者上线"));
                            latestObserverSessionId = obj.getString("sessionid");
                            blObserverOnline = true;
                            //观察者上线时需要断开一下app监听的socket，让监护仪重新连接发送开机包
                            try {
                                   if (myCMSServer!=null)myCMSServer.close();
                                   if (myCMSSocket != null)myCMSSocket.close();
                                   if (is != null) is.close();
                                   WhLogger.ErrorLog(MyApplication.getInstance().getUsername(),"CMSService.java","app主动断开socket重连","L-197");
                                   EventBus.getDefault().post(new MessageEvent("10001","app主动断开socket重连"));//创建socket失败通知页面
                                   initTcpServer();

                            } catch (IOException e) {
                                e.printStackTrace();
                                WhLogger.ErrorLog(MyApplication.getInstance().getUsername(),"CMSService.java","app主动断开socket重连时发生了异常","L-197");
                                EventBus.getDefault().post(new MessageEvent("10001","app主动断开socket重连时发生了异常"));//创建socket失败通知页面
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).on("OFFLINE", new Emitter.Listener() {//下线
                @Override
                public void call(Object... args) {
                    String str = (String) args[0];
                    WhLogger.e("io.socket.client.Socket.OFFLINE", str.toString());
                    JSONObject obj = null;
                    try {
                        obj = new JSONObject(str);
                        String doctroid = MyApplication.getInstance().getSharedPreferences(mContext,"doctorid");
                        String remoteId = obj.getString("id");
                        if (remoteId.toUpperCase().equals(doctroid.toUpperCase())) {
                            WhLogger.e(latestObserverSessionId,obj.getString("sessionid"));
                            if(obj.getString("sessionid").equals(latestObserverSessionId)){
                                blObserverOnline = false;
                                EventBus.getDefault().post(new MessageEvent("20001", "观察者离线"));
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).on("ERROR", new Emitter.Listener() {//错误信息
                @Override
                public void call(Object... args) {
                    String str = (String) args[0];
                //    WhLogger.e("io.socket.client.Socket.ERROR", str.toString());
                    JSONObject obj = null;
                    try {
                        obj = new JSONObject(str);//ERROR {"errorno":"0", "error":"success", "event":"ONLINE"}
                        if (obj.getString("event").toUpperCase().equals("ONLINE")) {
                            if (obj.getString("errorno").equals("0")) {//ONLINE 成功
                                EventBus.getDefault().post(new MessageEvent("10010", obj.getString("error")));
                                WhLogger.e("io.socket.client.Socket.ERROR", str.toString());
                            }
                        }
                        if (!obj.getString("errorno").equals("0") &&!obj.getString("errorno").equals("6")){
                            EventBus.getDefault().post(new MessageEvent("10011", obj.toString()));
                            WhLogger.ErrorLog(MyApplication.getInstance().getUsername(),"CMSService.java",obj.toString(),"L-156");
                            WhLogger.e("io.socket.client.Socket.ERROR", str.toString());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }).on("DATA", new Emitter.Listener() {//数据信息
                @Override
                public void call(Object... args) {
                    JSONObject obj = (JSONObject) args[0];
                    WhLogger.e("io.socket.client.Socket.DATA", obj.toString());
                }
            });
            mIoSocket.connect();
            mIoSocket.emit("ONLINE", userInfo);
        } catch (URISyntaxException e) {
            WhLogger.e("创建socketio失败",e.getMessage());
            WhLogger.ErrorLog(MyApplication.getInstance().getUsername(),"CMSService.java",e.getLocalizedMessage(),"L-143");
        }
    }
    public void initTcpServer() {

        new Thread(new Runnable() {
            @Override
            public void run() {

            //    BufferedReader br  = null ;
                try {
                    myCMSServer = new ServerSocket(MyApplication.SOCKET_PORT);
                    while (!bExit){
                        Boolean firstConnect = true;
                        WhLogger.e("!!!!!!!!!!!!!!!!!","11111111111111111");
                        myCMSSocket = myCMSServer.accept();
                        myCMSSocket.setSoTimeout(5000);
                        WhLogger.e("!!!!!!!!!!!!!!!!!",myCMSSocket.getInetAddress().toString());
                //        WhLogger.ErrorLog(MyApplication.getInstance().getUsername(),"CMSService.java",myCMSSocket.getInetAddress().toString(),"监护请求ip");
                        is = myCMSSocket.getInputStream();
                //        br = new BufferedReader(new InputStreamReader(is));
                        byte [] buff = new byte[1024];
                        String incomingMsg;
                        while (!bExit) {
                            Integer count =  is.read(buff);
                         //   WhLogger.e("count",String.valueOf(count));
                            if (count == -1)break;
                            if (count > 0) {
                                if (firstConnect) {
                                    firstConnect = false;
                                    WhLogger.e("cmsserver-socket:511---", "监护仪连接成功");
                                    EventBus.getDefault().post(new MessageEvent("10000", "监护仪连接成功"));
                                }
                                //   WhLogger.e("incomingMsg", incomingMsg);
                                incomingMsg = Base64.encodeToString(buff,0,count, Base64.NO_WRAP);//收到的监护仪数据
                                JSONObject obj = new JSONObject();
                                obj.put("monitor", incomingMsg);
                                obj.put("latitude", latitude);
                                obj.put("longitude", longitude);
                                latitude = "";longitude = "";
                             //   obj.put("gps", locationInfo);
                                String finalMessage = obj.toString()+'\n';

                                if (mIoSocket != null && blObserverOnline){
                                    mIoSocket.emit("DATA",finalMessage);
                                //    WhLogger.e("finalMessage", finalMessage);
                                }
                            }else continue;
                        }
                        if (myCMSSocket != null)myCMSSocket.close();
                    //    WhLogger.e("cmsserver-socket:511---", "监护仪断开连接");
                        EventBus.getDefault().post(new MessageEvent("10001", "监护仪断开连接"));
                    }
                } catch(SocketTimeoutException e){
                    e.printStackTrace();
                    WhLogger.e("cmsserver-socket:511---", "监护仪主动断开连接");
                    EventBus.getDefault().post(new MessageEvent("10001", "监护仪主动断开连接"));
                    try {
                        if (myCMSServer!=null)    myCMSServer.close();
                        if (myCMSSocket != null)  myCMSSocket.close();
                        if (is != null) is.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    initTcpServer();//重连吧
                } catch (IOException e) {
                    e.printStackTrace();
                    WhLogger.ErrorLog(MyApplication.getInstance().getUsername(),"CMSService.java",e.getLocalizedMessage(),"L-236");
                    EventBus.getDefault().post(new MessageEvent("10001", e.getMessage().toString()));//创建socket失败通知页面
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    if (is != null) is.close();
                 //   if (br != null)br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    WhLogger.ErrorLog(MyApplication.getInstance().getUsername(),"CMSService.java",e.getLocalizedMessage(),"L-246");
                    WhLogger.e("initTcpServer-si/br-colse-Exception", e.getLocalizedMessage());
                }
            }
        }).start();
    }
    /*****
     *
     * 定位结果回调，重写onReceiveLocation方法，可以直接拷贝如下代码到自己工程中修改
     *
     *
     */
    private BDAbstractLocationListener mLocationListener = new BDAbstractLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // TODO Auto-generated method stub
            if (null != location && location.getLocType() != BDLocation.TypeServerError) {
                latitude = String.valueOf(location.getLatitude());
                longitude = String.valueOf(location.getLongitude());
            }
        }
    };
    public class MessageEvent {
        public String id;
        public String msg;

        public MessageEvent(String id, String msg) {
            this.id = id;
            this.msg = msg;
        }

        public String getid() {
            return id;
        }

        public String getmsg() {
            return msg;
        }
    }


}
