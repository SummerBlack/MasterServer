package com.lamost.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

/**
 * 网络相关的API
 * @author C.Feng
 * @version 
 * @date 2017-4-5 下午8:27:09
 * 
 */
public class NetworkUtil {
	public static final String NET_UNKNOWN = "none";

	// wifi, cmwap, ctwap, uniwap, cmnet, uninet, ctnet,3gnet,3gwap
	// 其中3gwap映射为uniwap
	public static final String NET_WIFI   = "wifi";
	public static final String NET_CMWAP  = "cmwap";
	public static final String NET_UNIWAP = "uniwap";
	public static final String NET_CTWAP  = "ctwap";
	public static final String NET_CTNET = "ctnet";

	public NetworkUtil() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * 判断网络是否连接
	 * @param context
	 * @return
	 */
	public static boolean isNetworkConnected(Context context) {
		 if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable() && mNetworkInfo.isConnected();
            }
        }
		 
        return false;
	}
	/**
	 * 判断wifi是否连接
	 * @param context
	 * @return
	 */
	 public static boolean isWifiConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWiFiNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (mWiFiNetworkInfo != null) {
                return mWiFiNetworkInfo.isConnected() && wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED;
            }
        }
        return false;
    }
	/**
	 * 判断当前网络是否为wifi网络
	 * @param context
	 * @return
	 */
	public static boolean isWifiConnect(Context context) {
		String netType = "";
		if(context != null)
		{
			ConnectivityManager  nmgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo active = nmgr.getActiveNetworkInfo();
			if(active == null) {
				
			} else {
				netType = NetworkUtil.getNetType(active);
			}
		}
		return NET_WIFI.equals(netType);
	}
	/**
	 * 获取网络类型参数，包括cmwap,uniwap,ctwap,wifi,cmnet,ctnet,uninet,3gnet
	 * 由于底层msc无法处理3gwap，3gwap映射为uniwap
	 * @param info
	 * @return
	 */
	public static String getNetType(NetworkInfo info)
	{
		if(info == null)
			return NET_UNKNOWN;

		try {
			if(info.getType() == ConnectivityManager.TYPE_WIFI)
				return NET_WIFI;
			else
			{
				String extra = info.getExtraInfo().toLowerCase();
				if(TextUtils.isEmpty(extra))
					return NET_UNKNOWN;
				// 3gwap由于底层msc兼容不了，转换为uniwap
				if(extra.startsWith("3gwap") || extra.startsWith(NET_UNIWAP))
				{
					return NET_UNIWAP;
				}else if(extra.startsWith(NET_CMWAP))
				{
					return NET_CMWAP;
				}else if(extra.startsWith(NET_CTWAP))
				{
					return NET_CTWAP;
				}else if(extra.startsWith( NET_CTNET )){
					return NET_CTNET;
				}else
					return extra;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return NET_UNKNOWN;
	}
	/**
	 * 获取网络类型详细信息，包括EDGE、CDMA-EvDo rev.A、HSDPA等
	 * @param info
	 * @return 以字符串加int类型进行组合，如EDGE;2
	 */
	public static String getNetSubType(NetworkInfo info)
	{
		if(info == null)
			return NET_UNKNOWN;
		try {
			if(info.getType() == ConnectivityManager.TYPE_WIFI)
				return NET_UNKNOWN;
			else
			{
				String subtype = "";
				subtype += info.getSubtypeName();
				subtype += ";" + info.getSubtype();
				return subtype;
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}
		return NET_UNKNOWN;
	}


	/**
	 * 判断网络是否可用
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context) {
		if (checkPermission(context, "android.permission.INTERNET")) {
			ConnectivityManager cManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = cManager.getActiveNetworkInfo();
			if (info != null && info.isAvailable()) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * 检查APP权限是否开通
	 * 
	 * @param context
	 * @param str
	 * @return
	 */
	private static boolean checkPermission(Context context, String str) {
		return context.checkCallingOrSelfPermission(str) == PackageManager.PERMISSION_GRANTED;
	}

}
