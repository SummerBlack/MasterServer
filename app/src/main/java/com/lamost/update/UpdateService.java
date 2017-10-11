package com.lamost.update;

import com.lamost.masterserver.R;
import com.lamost.update.DownloadUtil.UpdateDownloadListener;
import com.lamost.utils.LogHelper;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;

/**
 * @author C.Feng
 * @version 
 * @date 2017-4-7 上午9:03:21
 * 
 */
public class UpdateService extends Service {
	private UpdateManager mUpdateManager;

	private UpdateDownloadListener listener = new UpdateDownloadListener(){
		@Override
		public void onStarted() {
			// TODO Auto-generated method stub
			downloadStartBroadcast();
			LogHelper.d("开始下载");
		}

		@Override
		public void onProgressChanged(int progress) {
			// TODO Auto-generated method stub
			downloadChangeBroadcast(progress);
			LogHelper.d("下载进度"+progress);
		}

		@Override
		public void onFinished() {
			// TODO Auto-generated method stub
			downloadFinishBroadcast();
			LogHelper.d("下载完成");
			stopSelf();
		}

		@Override
		public void onFailure() {
			// TODO Auto-generated method stub
			downloadFailureBroadcast();
			LogHelper.d("下载失败!!!");
			stopSelf();
		}
	};

	public UpdateService() {
		
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		LogHelper.d("onCreate");
		super.onCreate();
		mUpdateManager = UpdateManager.getInstance(getApplicationContext());
		final String appName = getString(R.string.app_name);
		mUpdateManager.autoUpdate(appName, new UpdateListener() {
			
			@Override
			public void onResult(int resultCode, String result) {
				// TODO Auto-generated method stub
				if (resultCode == UpdateManager.UPDATE_NEED) {
					String targetFile = Environment.getExternalStorageDirectory() + "/AutoInstallPackage/"
							+ appName+ ".apk";
					String urlPath = UpdateManager.URL + appName +"-" + result + ".apk";
					mUpdateManager.startDownload(urlPath, targetFile, listener, 60);
				} else if (resultCode == UpdateManager.UPDATE_NONEED){
					LogHelper.d("已是最新版本，不要用更新");
				} else {
					LogHelper.d("更新请求失败！！！");
				}
			}
		});
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		LogHelper.d("onStartCommand");
		
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * 发送开始下载的广播
	 */
	private void downloadStartBroadcast(){
		Intent startIntent = new Intent();
		startIntent.setAction(UpdateConstant.ACTION_UPDATESTART);
		sendBroadcast(startIntent);
	}
	/**
	 * 发送下载进度广播
	 */
	private void downloadChangeBroadcast(int progress){
		Intent changeIntent = new Intent();
		changeIntent.setAction(UpdateConstant.ACTION_UPDATESTATUS);
		changeIntent.putExtra("PROGRESS", progress);
		sendBroadcast(changeIntent);
	}
	/**
	 * 发送下载完成广播
	 */
	private void downloadFinishBroadcast(){
		Intent finishIntent = new Intent();
		finishIntent.setAction(UpdateConstant.ACTION_UPDATEFINISH);
		finishIntent.putExtra("appName", getString(R.string.app_name));
		finishIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
		sendBroadcast(finishIntent);
		LogHelper.d("下载完成！");
	}
	/**
	 * 发送下载失败广播
	 */
	private void downloadFailureBroadcast(){
		Intent failureIntent = new Intent();
		failureIntent.setAction(UpdateConstant.ACTION_UPDATEFAILURE);
		failureIntent.putExtra("appName", getString(R.string.app_name));
		sendBroadcast(failureIntent);
		LogHelper.d("下载失败！");
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mUpdateManager.destroy();
		LogHelper.d("onDestroy");
	}

}
