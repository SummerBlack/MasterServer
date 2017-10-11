package com.lamost.utils;

import java.util.List;
import android.app.ActivityManager;
import android.content.Context;

/**
 * @author C.Feng
 * @version 
 * @date 2017-3-4 下午4:06:45
 * 
 */
public class DaemonUtil {

	/**
	 * 判断服务是否开启
	 * @param context
	 * @param serviceClassName
	 * @return
	 */
    public static boolean isServiceAlive(Context context,
            String serviceClassName) {
	    if (context != null) {
   		 ActivityManager manager = (ActivityManager) context
   	                .getSystemService(Context.ACTIVITY_SERVICE);
   	        List<ActivityManager.RunningServiceInfo> running = manager
   	                .getRunningServices(30);
   	 
   	        for (int i = 0; i < running.size(); i++) {
   	            if (serviceClassName.equals(running.get(i).service.getClassName())) {
   	                return true;
   	            }
   	        }
	    }
      
        return false;
 
    }

}
