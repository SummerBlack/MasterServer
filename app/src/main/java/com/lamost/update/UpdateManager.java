package com.lamost.update;

import java.util.Timer;
import java.util.TimerTask;
import com.lamost.update.DownloadUtil.UpdateDownloadListener;
import com.lamost.utils.LogHelper;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

/**
 * @author C.Feng
 * @version 
 * @date 2017-4-7 上午9:04:12
 * 
 */
public class UpdateManager {
	
	private static final int MSG_UPDATE_REQUEST = 1;
	private static final int MSG_UPDATE_DOWNLOAD = 2;
	
	public static final int UPDATE_NEED = 0;
	public static final int UPDATE_NONEED = 1;
	public static final int UPDATE_FAIL = 2;
	
	public static String URL = "http://101.201.211.87:8080/";
	
	private Context mContext;
	private static UpdateManager instance;
	
	//HandlerThread对象
	private HandlerThread mUpdateHandlerThread;
	//分线程，处理主线程抛出的事件
	private ResultHandler mResultHandler;
	//监听向服务器请求更新的回馈
	private UpdateListener mUpdateListener;
	// 监听下载进度
	private DownloadUtil.UpdateDownloadListener mDownloadListener;
	private UpdateWebService mWebService;
	private DownloadUtil mDownloadUtil;
	
	private Timer timer;

	private UpdateManager(Context context) {
		mContext = context;
		//registerReceiver();
		mUpdateHandlerThread = new HandlerThread("UpdateHandlerThread");
		mUpdateHandlerThread.start();
		mResultHandler = new ResultHandler(mUpdateHandlerThread.getLooper());
		mWebService = new UpdateWebService();
	}
	/**
	 * 获取单例
	 * @param context
	 * @return
	 */
	public static UpdateManager getInstance(Context context){
		if (instance == null) {
			instance = new UpdateManager(context);
		}
		return instance;
	}

	/**
	 * 向服务器发送更新请求
	 * @param appName 
	 * @param updateListener 结果回调
	 */
	public void autoUpdate(String appName, UpdateListener updateListener){
		if (appName == null || updateListener == null) {
			return;
		}
		
		mUpdateListener = updateListener;
		mResultHandler.removeMessages(MSG_UPDATE_REQUEST );
		//将向服务器发送更新请求的事件抛出去，在handleMessage中处理
		mResultHandler.obtainMessage(MSG_UPDATE_REQUEST, appName).sendToTarget();
	}
	/**
	 * 开始下载APK
	 * @param urlPath 下载地址
	 * @param targetFile 下载文件的保存路径
	 * @param threadNum 启动的下载线程数
	 * @param listener 下载回调
	 * @param time 单位为秒，最长等待时间，如果这么长时间还没下载好，就返回下载失败
	 */
	
	public void startDownload(String urlPath, String targetFile, UpdateDownloadListener listener, long time){
		if (urlPath == null || targetFile == null || listener == null  || time <= 0) {
			return;
		}
		mDownloadUtil = new DownloadUtil(urlPath, targetFile, 1);
		mDownloadListener = listener;
		mResultHandler.removeMessages(MSG_UPDATE_DOWNLOAD);
		//将向服务器发送更新请求的事件抛出去，在handleMessage中处理
		mResultHandler.obtainMessage(MSG_UPDATE_DOWNLOAD, null).sendToTarget();
		//如果下载超时
		timer = new Timer();
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				mDownloadListener.onFailure();
				LogHelper.d("下载超时");
				
			}
		}, time * 1000);
		
	}
	
	 /** 
     * 获取应用程序版本名称信息
     * @param context 
     * @return 当前应用的版本名称 
     */  
    private  String getVersionName(Context context){ 
		try {
			PackageManager packageManager = context.getPackageManager();  
			PackageInfo packageInfo = packageManager.getPackageInfo(  
			        context.getPackageName(), 0);
			return packageInfo.versionName; 
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
        return null;  
    }  
    
    public void destroy() {
    	
    	timer.cancel();
		if (null != mUpdateHandlerThread) {
			mUpdateHandlerThread.quit();
			mResultHandler = null;
		}
		mDownloadUtil.destroy();
		
		instance = null;
	}
    
	class ResultHandler extends Handler{
		
		public ResultHandler(Looper looper){
			super(looper);
		}
		
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_UPDATE_REQUEST:
			{
				String appName = (String) msg.obj;
				String result = mWebService.getAppVersionVoice(appName);
				String versionName = getVersionName(mContext);
				LogHelper.d("当前版本号：" + versionName);
				
				if (versionName != null && !TextUtils.isEmpty(result) && result != "-1" ) {
					//如果服务器的版本号和该应用的版本号相同,则不需要下载更新
					if (result.equals(versionName)) {
						mUpdateListener.onResult(UPDATE_NONEED, result);
					} else {
						//否则就下载
						mUpdateListener.onResult(UPDATE_NEED, result);
					}
				
				} else {
					//向服务器请求失败
					mUpdateListener.onResult(UPDATE_FAIL, null);
				}
			} break;
			case MSG_UPDATE_DOWNLOAD:
			{
				try {
					mDownloadUtil.download(mDownloadListener);
				} catch (Exception e) {
					e.printStackTrace();
					//如果下载有问题
					mDownloadListener.onFailure();
				}
			} break;

			default:
				break;
			}
		}
	}
}
