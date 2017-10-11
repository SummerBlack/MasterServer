package com.lamost.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;

import com.lamost.utils.FileUtil.DataFileHelper;
/**
 * @author C.Feng
 * @version 
 * @date 2017-3-9 下午8:42:48
 * 崩溃日志收集器
 */
public class CrashCollector implements UncaughtExceptionHandler {
	//private static final String TAG = "CrashCollector";
	
	private static final String CRASH_LOG_DIR = Environment.getExternalStorageDirectory().getAbsolutePath()
			+ "/CrashLog/";
	private static final String APPNAME = "MasterServer";
	//系统默认的UncaughtException处理类,如果我们没有处理就交给他处理
	private Thread.UncaughtExceptionHandler mDefaultHandler = null;
	//使用一个类变量来缓存曾经创建的实例
	private static CrashCollector instance = null;
	//用于保存崩溃日志
	private DataFileHelper mCrashLogHelper;
	//程序的Context对象  
    private Context mContext;  
    //用来存储设备信息和异常信息  
    private Map<String, String> infos = new HashMap<String, String>();  
    //用于格式化日期,作为日志文件名的一部分  
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA); 
	
	private CrashCollector() {
		
	}
	/**
	 * 类方法，用于获取单例
	 * @return
	 */
	public static CrashCollector getInstance(){
		if(instance == null){
			instance = new CrashCollector();
		}
		return instance;
	}
	/**
	 * 在Application中调用注册
	 */
	public void init(Context context){
		mContext = context;
		//获取系统默认的UncaughtException处理类
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		//设置该CrashCollector为程序的默认处理器 
		Thread.setDefaultUncaughtExceptionHandler(CrashCollector.this);
		mCrashLogHelper = FileUtil.createFileHelper(CRASH_LOG_DIR);
	}
	/**
	 * 重写uncaughtException方法，当程序出现未处理的异常时会执行该方法
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		// TODO Auto-generated method stub
		if (!myHandleException(ex) && mDefaultHandler != null) {  
            //如果用户没有处理则让系统默认的异常处理器来处理  
            mDefaultHandler.uncaughtException(thread, ex);  
        } else {  
            try {  
                Thread.sleep(3000);  
            } catch (InterruptedException e) {  
                LogHelper.d("error : "+ e);
            }  
            //退出程序  
            //必须加上这段，这段stopService会调用SmartKitService的ondestory方法结束前台运行。
            //测试发现，即使在ondestory方法没有加stopForeground(true);程序也会退出前台显示
            //总之，调用stopService会使该服务退出，这样守护进程可以监测到已经退出了，然后重启，否则监测不到退出，不能重启！！！
            LogHelper.d("未捕获的异常，需要退出程序");
            Intent serviceIntent = new Intent("com.lamost.masterserver.action.SmartKitService");
    		serviceIntent.setPackage("com.lamost.masterserver");
            mContext.stopService(serviceIntent);
            
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);  
        }  

	}
	/**
	 * 我们自己个性化处理异常的操作
	 * @param ex
	 * @return
	 */
	private boolean myHandleException(Throwable ex) {
		if (ex == null) {  
            return false;  
        }  
		//此处就是我们自己的处理
		saveCrashLog(ex);
		return true;
	}
	/**
	 * 保存崩溃日志
	 * @param ex
	 */
	private void saveCrashLog(Throwable ex) {
		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();
		
		while (null != cause) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		
		String crashLog = writer.toString();
		printWriter.close();
		//将未捕获异常保存到sd卡的/MasterServer/crash/目录下，文件名为:当前时间.txt
		//mCrashLogHelper.createFile("", FileUtil.SURFFIX_TXT, false);
		String AppInfo = getAppInfo(mContext);
		mCrashLogHelper.createTxtFileforCrashLog(AppInfo);
		mCrashLogHelper.write(crashLog.getBytes(), true);
		mCrashLogHelper.closeWriteFile();
	}
	/**
	 * 获取APP应用有关信息
	 * @param context
	 * @return
	 */
	public String getAppInfo(Context context){
		PackageManager pm = context.getPackageManager();
		try {
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			if (pi != null) {
				int labelRes = pi.applicationInfo.labelRes;
				String appName = context.getResources().getString(labelRes);
				String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                return appName+"-"+versionName+"-"+versionCode;
			}
			
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return APPNAME;
		}
		return APPNAME;
	}
	 /** 
     * 收集设备参数信息 
     * @param ctx 
     */  
    public void collectDeviceInfo(Context ctx) {  
        try {  
            PackageManager pm = ctx.getPackageManager();  
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);  
            if (pi != null) {  
                String versionName = pi.versionName == null ? "null" : pi.versionName;  
                String versionCode = pi.versionCode + "";  
                infos.put("versionName", versionName);  
                infos.put("versionCode", versionCode);  
            }  
        } catch (NameNotFoundException e) {  
            LogHelper.e( "an error occured when collect package info:"+ e);  
        }  
        java.lang.reflect.Field[] fields = Build.class.getDeclaredFields();  // 反射机制
        for (java.lang.reflect.Field field : fields) {  
            try {  
                field.setAccessible(true); 
                infos.put(field.getName(), field.get(null).toString());  
                LogHelper.d(field.getName() + " : " + field.get(null));  
            } catch (Exception e) {  
            	LogHelper.e("an error occured when collect crash info:"+ e);  
            }  
        }  
    }  
    /** 
     * 保存错误信息到文件中 
     *  
     * @param ex 
     * @return  返回文件名称,便于将文件传送到服务器 
     */  
    public String saveCrashInfo2File(Throwable ex) {  
          
        StringBuffer sb = new StringBuffer();  
        for (Map.Entry<String, String> entry : infos.entrySet()) {  
            String key = entry.getKey();  
            String value = entry.getValue();  
            sb.append(key + "=" + value + "\n");  
        }  
          
        Writer writer = new StringWriter();  
        PrintWriter printWriter = new PrintWriter(writer);  
        ex.printStackTrace(printWriter);  
        Throwable cause = ex.getCause();  
        while (cause != null) {  
            cause.printStackTrace(printWriter);  
            cause = cause.getCause();  
        }  
        printWriter.close();  
        String result = writer.toString();  
        sb.append(result);  
        try {  
            long timestamp = System.currentTimeMillis();  
            String time = formatter.format(new Date());  
            String fileName = "crash-" + time + "-" + timestamp + ".log";  
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {  
                String path = "//sdcard//crash//";  
                File dir = new File(path);  
                if (!dir.exists()) {  
                    dir.mkdirs();  
                }  
                FileOutputStream fos = new FileOutputStream(path + fileName);  
                fos.write(sb.toString().getBytes());  
                fos.close();  
            }  
            return fileName;  
        } catch (Exception e) {  
        	LogHelper.e( "an error occured while writing file...:"+e);  
        }  
        return null;  
    }  
	

}
