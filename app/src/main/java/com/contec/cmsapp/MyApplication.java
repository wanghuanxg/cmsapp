/**
 * 
 */
package com.contec.cmsapp;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * @author WH
 * 
 */
public class MyApplication extends Application {

	/**
	 * 
	 */
	public static final String APP_DATA_DIR = "CMSAPP";
    public static final String UPDATE_XML_URL = "http://data2.contec365.com/updatesoftware/doctor_androidsoftware/5200/current.xml";
    public static final Integer SOCKET_PORT = 5188;
    private String username= "";//账号
	private String passwortd= "";//密码
	private String sessionId = "";
    private String Accountspath= "";
    private String name = "";
    private String caseid = "";
	private List<Activity> mList = new LinkedList<Activity>();  
	private static MyApplication instance;
    //实例化一次
    public synchronized static MyApplication getInstance(){
        if (null == instance) {
            instance = new MyApplication();
        }
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }
    // add Activity
    public void addActivity(Activity activity) {   
        mList.add(activity);   
    }   
    //关闭每一个list内的activity  
    public void exit() {   
        try {   
            for (Activity activity:mList) {   
                if (activity != null)   
                    activity.finish();   
            }   
        } catch (Exception e) {   
            e.printStackTrace();   
        } finally {   
            System.exit(0);   
        }   
    }

    public String getSharedPreferences(Context context,String key)
    {
        SharedPreferences sp;
        sp = context.getSharedPreferences("userInfo", Context.MODE_WORLD_READABLE);
        return sp.getString(key, "");
    }
    public void setSharedPreferences(Context context,String key,String value)
    {
        SharedPreferences sp;
        sp = context.getSharedPreferences("userInfo", Context.MODE_WORLD_READABLE);
        sp.edit().putString(key, value).commit();
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getCaseid() {
        return caseid.toUpperCase();
    }

    public void setCaseid(String caseid) {
        this.caseid = caseid;
    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswortd() {
        return passwortd;
    }

    public void setPasswortd(String passwortd) {
        this.passwortd = passwortd;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    public String getAccountspath() {
        return Accountspath;
    }

    public void setAccountspath(String accountspath) {
        Accountspath = accountspath;
    }

    @Override
	public void onLowMemory() {
		super.onLowMemory();
		System.gc();
	}

	/**
	 * 检查当前网络是否可用
	 * 
	 * @param context
	 * @return
	 */

	public boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (connectivityManager == null) {
			return false;
		} else {
			// 获取NetworkInfo对象
			NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();

			if (networkInfo != null && networkInfo.length > 0) {
				for (int i = 0; i < networkInfo.length; i++) {
					if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}

	
	public static void delete(String filePath){
		if (filePath.isEmpty()) {
			return;
		}
		final File file = new File(filePath);
		new Thread(new Runnable() {
			@Override
			public void run() {
				deleteFiles(file);
			}
		}).start();
	}
	private static void deleteFiles(File file) {
		if (file.isFile()) {
			file.delete();
			return;
		}

		if (file.isDirectory()) {
			File[] childFiles = file.listFiles();
			if (childFiles == null || childFiles.length == 0) {
				file.delete();
				return;
			}

			for (int i = 0; i < childFiles.length; i++) {
				deleteFiles(childFiles[i]);
			}
			file.delete();
		}
	}
	
	

}
