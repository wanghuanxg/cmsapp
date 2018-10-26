package com.contec.helper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.R.integer;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ApplicationInfo;
import android.os.Environment;
import android.util.Log;

import com.contec.cmsapp.MyApplication;


/**
 * lsj add
 */  
public class WhLogger {

	public WhLogger() {   
        super();   
    } 
	public static final String APP_DATA_DIR = MyApplication.APP_DATA_DIR;
	public static Boolean DEBUG = true;

    public static boolean isApkInDebug(Context context) {
        try {
            ApplicationInfo info = context.getApplicationInfo();
            boolean bl =  (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
            WhLogger.DEBUG = bl;
            return  bl;
        } catch (Exception e) {
            return false;
        }
    }
    public static void d(String tag, String msg){  
        if(DEBUG) Log.d(tag, msg)  ;
    }  

    public static void e(String tag, String msg){  
        if(DEBUG) Log.e(tag, msg)  ;
    }  
	
	public static void alert(Context context, String Title, String Msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		if (Title.equals(""))
			Title = "提示";
		builder.setTitle(Title);
		builder.setMessage(Msg);
		builder.setCancelable(false);
		builder.setPositiveButton("ok", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.create().show();
	}
	
	/**  
     * 功能：错误日志
     * @param stringFilePath  保存日志文件路径 
     * @param stringClassName 类名  
     * @param stringMsg   错误信息 
     */  
    public static void ErrorLog(String AccountNumber, String stringClassName, String stringMsg, String stringline){
    	
    	SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM");
		Calendar curr = Calendar.getInstance();
		Date date = curr.getTime();
		String stringDate = fm.format(date)+ ".txt";
		
		String stringLogPath = Environment.getExternalStorageDirectory() + "//" + APP_DATA_DIR +  "//" + "ErrorLog//" ;
		
		File LogFilePath = new File(stringLogPath);
		if (!LogFilePath.exists()) {
			LogFilePath.mkdirs();
		}
		
		File saveFile = new File(stringLogPath + stringDate);   
		if (!saveFile.exists()) {
            String month = stringDate.substring(5, 7);
            String year = stringDate.substring(0, 4);
            String oldFileName ="";
            if ("01".equals(String.valueOf(month))) {
                oldFileName = String.valueOf(Integer.parseInt(year)-1)+"-12.txt";
            }else if ("02".equals(String.valueOf(month))) {
                oldFileName = year+"-01.txt";
            }else if ("03".equals(String.valueOf(month))) {
                oldFileName = year+"-02.txt";
            }else if ("04".equals(String.valueOf(month))) {
                oldFileName = year+"-03.txt";
            }else if ("05".equals(String.valueOf(month))) {
                oldFileName = year+"-04.txt";
            }else if ("06".equals(String.valueOf(month))) {
                oldFileName = year+"-05.txt";
            }else if ("07".equals(String.valueOf(month))) {
                oldFileName = year+"-06.txt";
            }else if ("08".equals(String.valueOf(month))) {
                oldFileName = year+"-07.txt";
            }else if ("09".equals(String.valueOf(month))) {
                oldFileName = year+"-08.txt";
            }else if ("10".equals(String.valueOf(month))) {
                oldFileName = year+"-09.txt";
            }else if ("11".equals(String.valueOf(month))) {
                oldFileName = year+"-10.txt";
            }else if ("12".equals(String.valueOf(month))) {
                oldFileName = year+"-11.txt";
            }

            MyApplication.delete(stringLogPath+oldFileName);
			try {
				saveFile.createNewFile();
			} catch (IOException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
		Date nowtime = new Date();  
        String stringTimeNow = sdf.format(nowtime); 
        
        FileWriter fileWriter; 
        try {
        	fileWriter = new FileWriter(saveFile, true); 
    		fileWriter.write(stringTimeNow + "\t\t");  
    		fileWriter.write(stringClassName+ "\t\t");
    		fileWriter.write(stringMsg+ "\t\t");
    		fileWriter.write(stringline+ "\t\t");
    		fileWriter.write("\r\n--------------------------------------\r\n");
    		fileWriter.close(); 
			
		} catch (IOException e)  
        {  
            e.printStackTrace();  
        }  
    }
    
    /**  
     * 功能：操作日志
     * @param stringFilePath  保存日志文件路径 
     * @param stringClassName 类名  
     * @param stringContent   操作内容
     */  
    public static void OperateLog(String AccountNumber, String stringClassName, String stringContent,String stringline){
    	
    	SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM");
		Calendar curr = Calendar.getInstance();
		Date date = curr.getTime();
		String stringDate = fm.format(date)+ ".txt";
		
		String stringLogPath = Environment.getExternalStorageDirectory() + "//" + APP_DATA_DIR+ "//" + "OperateLog//" ;
		
		File LogFilePath = new File(stringLogPath);
		if (!LogFilePath.exists()) {
			LogFilePath.mkdirs();
		}
		
		File saveFile = new File(stringLogPath + stringDate);   
		if (!saveFile.exists()) {
			String month = stringDate.substring(5, 7);
			String year = stringDate.substring(0, 4);
			String oldFileName ="";
			if ("01".equals(String.valueOf(month))) {
				oldFileName = String.valueOf(Integer.parseInt(year)-1)+"-12.txt";
			}else if ("02".equals(String.valueOf(month))) {
				oldFileName = year+"-01.txt";
			}else if ("03".equals(String.valueOf(month))) {
				oldFileName = year+"-02.txt";
			}else if ("04".equals(String.valueOf(month))) {
				oldFileName = year+"-03.txt";
			}else if ("05".equals(String.valueOf(month))) {
				oldFileName = year+"-04.txt";
			}else if ("06".equals(String.valueOf(month))) {
				oldFileName = year+"-05.txt";
			}else if ("07".equals(String.valueOf(month))) {
				oldFileName = year+"-06.txt";
			}else if ("08".equals(String.valueOf(month))) {
				oldFileName = year+"-07.txt";
			}else if ("09".equals(String.valueOf(month))) {
				oldFileName = year+"-08.txt";
			}else if ("10".equals(String.valueOf(month))) {
				oldFileName = year+"-09.txt";
			}else if ("11".equals(String.valueOf(month))) {
				oldFileName = year+"-10.txt";
			}else if ("12".equals(String.valueOf(month))) {
				oldFileName = year+"-11.txt";
			}
			
			MyApplication.delete(stringLogPath+oldFileName);
		//	LogFilePath.mkdirs();
			try {
				saveFile.createNewFile();
				Log.e("fileexit", String.valueOf(saveFile.exists()));
			} catch (IOException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
		Date nowtime = new Date();  
        String stringTimeNow = sdf.format(nowtime); 
        
        FileWriter fileWriter; 
        try {
        	fileWriter = new FileWriter(saveFile, true); 
    		fileWriter.write(stringTimeNow + "\t\t");  
    		fileWriter.write(stringClassName+ "\t\t");
    		fileWriter.write(stringContent+ "\t\t");
    		fileWriter.write(stringline+ "\t\t");
    		fileWriter.write("\r\n-------------------------------------\r\n");
    		fileWriter.close(); 
			
		} catch (IOException e)  
        {  
            e.printStackTrace();  
        }  
    }
}
  