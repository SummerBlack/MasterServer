package com.lamost.app;

import android.app.Application;

import com.lamost.utils.CrashCollector;
import com.lamost.utils.LogHelper;

/**
 * @author C.Feng
 * @version
 * @date 2017-3-9 下午9:40:05
 * 
 */
public class MasterServerApplication extends Application {
	private final static boolean SAVE_CRASHLOG = true;

	public MasterServerApplication() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// 在Application中注册
		if (SAVE_CRASHLOG) {
			CrashCollector collector = CrashCollector.getInstance();
			collector.init(getApplicationContext());
			LogHelper.d("注册崩溃日志收集器");
		}
	}

}
