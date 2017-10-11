package com.lamost.masterserver;

import com.lamost.update.UpdateService;
import com.lamost.utils.LogHelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

/**
 * 网络状态广播监听器。
 * 
 * @author <a href="http://www.xfyun.cn">讯飞开放平台</a>
 * @date 2016年7月23日 上午10:33:03 
 *
 */
public class NetworkStateReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(final Context context, Intent intent) {
		if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
			ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
	        NetworkInfo wifiInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI); 
	        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	        //如果wifi已经连接上
	        if (wifiInfo.isConnected() && wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
	        	LogHelper.d("网络连接成功");
	        	//启动主服务
	        	Intent serviceIntent = new Intent(SmartKitService.ACTION);
				serviceIntent.setPackage(context.getPackageName());
				context.startService(serviceIntent);
				//启动更新apk服务
	        	Intent updateIntent = new Intent(context, UpdateService.class);
				context.startService(updateIntent);
	        }
		}
	}
	
}
