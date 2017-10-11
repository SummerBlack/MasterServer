package com.lamost.update;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import com.lamost.utils.LogHelper;

/**
 * 多线程下载
 * @author C.Feng
 * @version 
 * @date 2017-4-6 下午4:54:43
 * 
 */
public class DownloadUtil {
	//定义下载资源的路径
	private String urlPath;
	//指定所下载文件的保存位置
	private String targetFile;
	//定义需要使用多少条线程下载资源
	private int threadNum;
	//定义下载文件的总大小
	private int fileSize;
	//定义下载的线程对象
	private DownloadThread[] threads;
	//下载监听器
	private UpdateDownloadListener listener;
	//已经下载的进度
	private int mDownloadStatus;
	//定时器，每隔500毫秒检测一次下载状态
	private Timer timer;
	
	public DownloadUtil(String urlPath, String targetFile, int threadNum) {
		this.urlPath = urlPath;
		this.targetFile = targetFile;
		this.threadNum = threadNum;
		this.threads = new DownloadThread[threadNum];
	}
	/**
	 * 下载
	 * @throws Exception
	 */
	public void download(UpdateDownloadListener downloadListener) throws Exception{
		listener = downloadListener;
		
		URL url = new URL(urlPath);
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.setConnectTimeout(5000);
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Connection", "Keep-Alive");
		//得到文件大小
		fileSize = connection.getContentLength();
		LogHelper.d("文件大小:"+fileSize);
		connection.disconnect();
		int currentPartSize = fileSize / threadNum + 1;
		//检测该文件的目录是否存在，如果不存在就创建,否则会报错
		checkLocalFileDir(targetFile);
		
		File files = new File(targetFile);
		if (files.exists()) {
			files.delete();
			LogHelper.d("删除文件"+files);
		}
		
		RandomAccessFile file = new RandomAccessFile(targetFile, "rw");
		//设置本地文件的大小
		file.setLength(fileSize);
		file.close();
		for (int i = 0; i < threadNum; i++) {
			//计算每一条线程下载的开始位置
			int startPosition = i * currentPartSize;
			//每条线程使用一个RandomAccessFile进行下载
			RandomAccessFile currentFile = new RandomAccessFile(targetFile, "rw");
			currentFile.seek(startPosition);
			threads[i] = new DownloadThread(startPosition, currentPartSize, currentFile);
			//启动下载线程
			threads[i].start();
			LogHelper.d("启动线程"+i);
		}
		//开始下载
		listener.onStarted();
		//每隔500毫秒检测一次下载状态
		timer = new Timer();
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				//获取下载任务的完成情况
				double completeRate = getCompleteRate();
				mDownloadStatus = (int)(completeRate * 100);
				listener.onProgressChanged(mDownloadStatus);
				//LogHelper.d("下载完成"+mDownloadStatus);
				if (mDownloadStatus >= 100) {
					//发送下载完成广播
					listener.onFinished();
					if (timer != null) {
						timer.cancel();
					}
				}
			}
		}, 1000, 500);
		
	}
	/**
	 * 检测该文件的目录是否存在，如果不存在就创建
	 * @param path
	 */
	 private void checkLocalFileDir(String path){
        File dir = new File(path.substring(0, path.lastIndexOf("/") + 1));
        if (!dir.exists()) {
            dir.mkdir();
        }
    }
	/**
	 * 获取下载完成的百分比
	 * @return
	 */
	private double getCompleteRate(){
		//统计多条线程已经下载的总大小
		int sumSize = 0;
		for (int i = 0; i < threadNum; i++) {
			sumSize += threads[i].length;
		}
		return sumSize * 1.0 / fileSize;
	}
	
	public interface UpdateDownloadListener {

	    /**
	     * 下载开始回调
	     */
	    void onStarted();

	    /**
	     * 进度更新回调
	     * @param progerss
	     */
	    void onProgressChanged(int progress);

	    /**
	     * 下载完成回调
	     * @param completeSize
	     * @param downloadUrl
	     */
	    //void onFinished(int completeSize, String downloadUrl);
	    void onFinished();

	    /**
	     * 下载失败回调
	     */
	    void onFailure();
	}
	/**
	 * 正真的下载线程
	 * @author User
	 *
	 */
	private class DownloadThread extends Thread{
		//当前线程的下载位置
		private int startPosition;
		//定义当前线程负责下载的文件大小
		private int currentPartSize;
		//当前程序需要下载的文件块
		private RandomAccessFile currentFile;
		//定义该线程已经下载的字节数
		public int length;
		//停止运行标志位
		private boolean mStopRun = false;
		
		public DownloadThread(int startPosition, int currentPartSize, RandomAccessFile currentFile){
			this.startPosition = startPosition;
			this.currentPartSize = currentPartSize;
			this.currentFile = currentFile;
		}

		@Override
		public void run() {
			InputStream inStream = null;
			try {
				URL url = new URL(urlPath);
				HttpURLConnection connection = (HttpURLConnection)url.openConnection();
				connection.setConnectTimeout(5000);
				connection.setRequestMethod("GET");
				connection.setRequestProperty("Connection", "Keep-Alive");
				inStream = connection.getInputStream();
				skipFully(inStream,this.startPosition);
				byte[] buffer = new byte[1024];
				int hasRead = 0;
				
				while (!mStopRun && length <currentPartSize && (hasRead = inStream.read(buffer)) >0) {
					currentFile.write(buffer, 0, hasRead);
					//累计该线程下载的总大小
					length += hasRead;
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
				//下载失败
				listener.onFailure();
			} catch (IOException e) {
				e.printStackTrace();
				//下载失败
				listener.onFailure();
			} finally{
				try {
					if (currentFile != null && inStream != null) {
						currentFile.close();
						inStream.close();
					}
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
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
	/**
	 * 定义一个为InputStream跳过bytes字节的方法
	 * @param ins
	 * @param bytes
	 * @throws IOException
	 */
	private void skipFully(InputStream ins, long bytes) throws IOException{
		long remainning = bytes;
		long len = 0;
		while (remainning > 0) {
			len = ins.skip(remainning);
			remainning -= len;
		}
	}
	
	/**
	 * 暂停所有下载线程
	 */
	private void stopRun() {
		for (int i = 0; i < threadNum; i++) {
			threads[i].stopRun();
		}
	}
	/**
	 * 停止所有下载进程
	 */
	public void destroy(){
		stopRun();
		timer.cancel();
		timer = null;
		threads = null;
		urlPath = null;
		targetFile = null;
		listener = null;
		LogHelper.d("停止所有下载进程");
	}
	
}
