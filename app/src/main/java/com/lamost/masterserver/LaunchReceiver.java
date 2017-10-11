package com.lamost.masterserver;

import com.lamost.utils.NetworkUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LaunchReceiver extends BroadcastReceiver {
	//public static final String ACTION = "com.lamost.masterserver.action.SmartKitService";
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e("LaunchReceiver", "开机自动启动");
		//对于该应用，由于对网络的依赖很大，必须等待wifi连接上了以后再启动，因此开机就启动是有问题的；
		//为此添加一项判断，如果wifi连接上就启动，没有连接暂时不启动
		if (NetworkUtil.isWifiConnected(context)) {
			Intent serviceIntent = new Intent(SmartKitService.ACTION);
			serviceIntent.setPackage(context.getPackageName());
			context.startService(serviceIntent);
		}
		
	}


}
