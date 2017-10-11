package com.lamost.masterserver;

import android.content.Context;
import android.content.Intent;

import com.lamost.utils.DaemonUtil;
import com.lamost.utils.LogHelper;
/**
 * 监测相应的Service是否正在运行，如果没有就重新启动
 * @author C.Feng
 * @version 
 * @date 2017-4-1 下午4:01:42
 * 
 */
public class AppDeamonThread extends Thread {
	//它所监听的DeamonService的属性值
	private static final String DEAMONSERVICEACTION = "com.lamost.deamonservice.action.DeamonClientService";
	private static final String DeamonServiceNAME = "com.lamost.deamonservice.DeamonClientService";
	private static final String DeamonServicePACKAGE = "com.lamost.deamonservice";
	
	private Context mContext = null;
	private boolean mStopRun = false;
	//默认检测周期为5秒钟
	private int mCheckIntervalSec = 5;
	
	public AppDeamonThread(Context mContext){
		this.mContext = mContext;
	}
	/**
	 * 准备两个服务， 互相判断是否存在， 如果不存在则重启对方。
	 * 开启一个守护进程
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();

        while (!mStopRun) {
        	//监测主控功能的程序SmartKitService是否正常运行，如果意外关闭则立刻重新启动
        	if (!DaemonUtil.isServiceAlive(mContext,DeamonServiceNAME)) {
                LogHelper.d("检测到服务DeamonClientService不存在.....");
                
                Intent serviceIntent = new Intent(DEAMONSERVICEACTION);
        		serviceIntent.setPackage(DeamonServicePACKAGE);
        		mContext.startService(serviceIntent);
            }
        	
            try {
                Thread.sleep(mCheckIntervalSec*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    
	}
	
	/**
	 * 暂停该线程
	 */
	public void stopRun() {
		mStopRun = true;
		interrupt();
	}
	
	

}
