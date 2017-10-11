package com.lamost.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.text.TextUtils;

/**
 * 文件工具类。
 * 
 * @author <a href="http://www.xfyun.cn">讯飞开放平台</a>
 * @date 2016年7月23日 上午10:31:30
 *
 */
public class FileUtil {
	public final static String SURFFIX_PCM = ".pcm";//后缀.pcm
	
	public final static String SURFFIX_TXT = ".txt";
	
	public final static String SURFFIX_CFG = ".cfg";
	
	/**
	 * 数据文件类，用于读写文件。
	 * 
	 * @author hj
	 * @date 2016年5月11日 下午2:18:47 
	 *
	 */
	public static class DataFileHelper {
		//默认的文件路径
		private String FILE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() 
										+ "/Files/";
		//缓冲流(字节输出流)
		private BufferedOutputStream mFos;
		//文件输入流(字节流)，用于读取文件
		private FileInputStream mFis;
		//缓冲流(字符输出流)
		private BufferedWriter  mWriter;
		
		public DataFileHelper(String fileDir) {
			FILE_DIR = fileDir;
		}
		
		public boolean openFile(String filePath) {
			return openFile(filePath, true);
		}
		/**
		 * 打开一个文件，用于文件的读取
		 * @param filePath
		 * @param inCurrentDir
		 * @return
		 */
		public boolean openFile(String filePath, boolean inCurrentDir) {
			File file = null;
			if (inCurrentDir) {
				file = new File(FILE_DIR + filePath);
			} else {
				file = new File(filePath);
			}
			
			try {
				mFis = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				mFis = null;
				return false;
			}
			
			return true;
		}
		/**
		 * 获取输入流中可读取的字节数
		 * @return
		 */
		public int getAvailabe() {
			if (null != mFis) {
				try {
					return mFis.available();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			return -1;
		}
		/**
		 * 读取文件输入流中的数据并保存的buffer中，最多读取buffer.length个字节的数据
		 * @param buffer
		 * @return 返回实际读取的字节
		 */
		public int read(byte[] buffer) {
			if (null != mFis) {
				try {
					return mFis.read(buffer);
				} catch (IOException e) {
					e.printStackTrace();
					closeReadFile();
					return 0;
				}
			}
			
			return -1;
		}
		/**
		 * 关闭输入流，结束文件的读取
		 */
		public void closeReadFile() {
			if (null != mFis) {
				try {
					mFis.close();
					mFis = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		/**
		 * 创建一个可追加写入的文件
		 * @param filename 文件名，如果为空，则以当前时间命名
		 * @param suffix 文件后缀
		 */
		public void createAppendableFile(String filename, String suffix) {
			File dir = new File(FILE_DIR);
			if (!dir.exists()) {//判断File对象是否存在对应的目录（或文件）
				dir.mkdirs();//试图创建一个File对象所对应的目录
			}
			
			if (null != mWriter) {
				return;
			}
			
			if (TextUtils.isEmpty(filename)) {
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA);
				filename = df.format(new Date());//文件名就是目前的时间如：2017-03-11-10-06-48
			}
			
			String filePath = FILE_DIR + filename + suffix;
			File file = new File(filePath);
			
			try {
				if (!file.exists()) {
					file.createNewFile();//当此File对象所对应的文件不存在时，该方法将新建一个该File对象所指定的新文件
				}
				//构造一个写入文件的FileOutputStream。 如果append为true并且文件已经存在，它将被追加到该文件后面
				//否则将被截断。 如果文件不存在，将创建该文件。
				mWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		/**
		 * 向文件后面追加数据
		 * @param s
		 */
		public void append(String s) {
			if (null != mWriter) {
				try {
					mWriter.write(s);
					mWriter.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		/**
		 * 关闭追加文件输出流
		 */
		public void closeAppendableFile() {
			if (null != mWriter) {
				try {
					mWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				mWriter = null;
			}
		}
		/**
		 * 创建一个文件，用于保存数据
		 * @param filename 文件名，如果没有为filename赋值，则以当前时间为文件名称
		 * @param suffix 后缀
		 * @param append 是否可追加
		 */
		public void createFile(String filename, String suffix, boolean append) {
			File dir = new File(FILE_DIR);
			if (!dir.exists()) {
				dir.mkdirs();
			}

			if (null != mFos) {
				return;
			}
			//如果没有为filename赋值，则以当前时间为文件名称
			if (TextUtils.isEmpty(filename)) {
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA);
				filename = df.format(new Date());
			}
			
			String filePath = FILE_DIR + filename + suffix;
			
			File file = new File(filePath);
			try {
				mFos = new BufferedOutputStream(
								new FileOutputStream(file, append));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		/**
		 * 主要用于保存崩溃日志时，创建相应的文件
		 * @param appName 应用的名字，用于标识是哪个应用
		 * 最终的文件名如：xxx-2017-04-01-10-31-06.txt
		 * xxx表示为xxx这个应用，2017-04-01-10-31-06表示发生崩溃的时间
		 */
		public void createTxtFileforCrashLog(String appName){
			String fileName = null;
			if(TextUtils.isEmpty(appName)){
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA);
				fileName = df.format(new Date());
			}else{
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA);
				fileName = appName+"-"+df.format(new Date());
			}
			
			createFile(fileName, SURFFIX_TXT, false);
		}
		
		/**
		 * 创建一个.pcm为后缀的文件
		 * @param filename
		 */
		public void createPcmFile(String filename) {
			if (TextUtils.isEmpty(filename)) {
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA);
				filename = df.format(new Date());
			}
			
			createFile(filename, SURFFIX_PCM, false);
		}
		
		public void write(byte[] data, boolean flush) {
			synchronized (DataFileHelper.this) {
				if (null != mFos) {
					try {
						mFos.write(data);
						if (flush) {
							mFos.flush();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		public void write(byte[] data, int offset, int len, boolean flush) {
			synchronized (DataFileHelper.this) {
				if (null != mFos) {
					try {
						mFos.write(data, offset, len);
						if (flush) {
							mFos.flush();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		public void closeWriteFile() {
			synchronized (DataFileHelper.this) {
				if (null != mFos) {
					try {
						mFos.flush();
						mFos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					mFos = null;
				}
			}
		}
	}
	
	/**
	 * 目录大小守护线程，当目录大小超出限制时，按时间先后顺序删除子目录，以保持目录大小。
	 * 
	 * @author <a href="http://www.xfyun.cn">讯飞开放平台</a>
	 * @date 2016年5月11日 下午2:17:16 
	 *
	 */
	public static class DirSizeDeamonThread extends Thread {
		//private static final String TAG = "DirSizeDeamonThread";
		//文件目录
		private File mDir;
		//目录大小的上限
		private long mDirSizeBytesLimit;
		
		private long mDelSizeBytes;
		//检测周期
		private int mCheckIntervalSec = 10 * 60;
		
		private boolean mStopRun = false;
		
		public DirSizeDeamonThread(String dirPath, long dirSizeBytesLimit, 
				long delSizeBytes, int checkIntervalSec) {
			mDir = new File(dirPath);
			mDirSizeBytesLimit = dirSizeBytesLimit;
			mDelSizeBytes = delSizeBytes;
			mCheckIntervalSec = checkIntervalSec;
			
			// 检查间隔至少为10分钟
			if (mCheckIntervalSec < 10 * 60) {
				mCheckIntervalSec = 10 * 60;
			}
		}
		
		public void setDirPath(String dirPath) {
			mDir = new File(dirPath);
		}
		
		// 比较器，将文件按修改时间排序
		private Comparator<File> mTimeComparator = new Comparator<File>() {

			@Override
			public int compare(File file1, File file2) {
				long time1 = file1.lastModified();
				long time2 = file2.lastModified();
				
				if (time1 < time2) {
					return -1;
				} else if (time1 > time2) {
					return 1;
				}
				
				return 0;
			}
		};
		
		public void stopRun() {
			mStopRun = true;
			interrupt();
		}
		
		// 从oriFiles中按顺序找到总和刚好大于totalSize的文件
		private List<File> getFilesBySize(List<File> oriFiles, Map<File, Long> details,
				double totalSize) {
			if (null == oriFiles || null == details || totalSize <= 0) {
				return null;
			}
			
			List<File> files = new ArrayList<File>();
			double curSize = 0;
			
			for (File oriFile : oriFiles) {
				curSize += details.get(oriFile);
				files.add(oriFile);
				
				if (curSize >= totalSize) {
					break;
				}
			}
			
			return files.size() == 0 ? null : files;
		}
		
		// 删除文件
		private void delFlies(List<File> files) {
			if (null == files) {
				return;
			}
			
			for (File file : files) {
				try {
					//DebugLog.LogD(TAG, "delFile:" + file.getName());
					
					FileUtil.delFile(file);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		@Override
		public void run() {
			super.run();
			
			while (!mStopRun) {
				Map<File, Long> details = new HashMap<File, Long>();
				
				long time = System.currentTimeMillis();
				
				double totalSizeBytes = FileUtil.getFileSizeBytes(mDir, details, true);
				
				long spent = System.currentTimeMillis() - time;
				
				LogHelper.d("getFileSize, spent=" + spent);
						
				if (totalSizeBytes - mDirSizeBytesLimit > 0) {
					List<File> subFileList = new ArrayList<File>(details.keySet());
					Collections.sort(subFileList, mTimeComparator);
					
					List<File> delFileList = getFilesBySize(subFileList, details, mDelSizeBytes);
					
					long time2 = System.currentTimeMillis();
					
					delFlies(delFileList);
					
					long spent2 = System.currentTimeMillis() - time2;
					
					LogHelper.d("delFlies, spent=" + spent2);
				}
				
				try {
					sleep(mCheckIntervalSec * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	/**
	 * 创建一个FileHelper
	 * 
	 * @param fileDir 文件目录
	 * @return
	 */
	public static DataFileHelper createFileHelper(String fileDir) {
		return new DataFileHelper(fileDir);
	}
	
	/**
	 * 检测文件是否存在
	 * 
	 * @param path 文件全路径
	 * @return 是否存在
	 */
	public static boolean exist(String path) {
		File file = new File(path);
		
		return file.exists();
	}
	
	/**
	 * 删除文件（夹）。
	 * 
	 * @param file
	 * @return 是否成功
	 */
	public static boolean delFile(File file) {
		if (null == file) {
			return false;
		}
		
		if (file.isFile()) {
			return file.delete();
		}
		
		boolean deleted = true;
		
		File[] subFiles = file.listFiles();
		if (null != subFiles) {
			for (File subFile : subFiles) {
				deleted = delFile(subFile);
				
				if (!deleted) {
					return deleted;
				}
			}
		}
		
		return deleted;
	}
	
	/**
	 * 获取文件（夹）的大小，单位：字节。
	 * 
	 * @param file 文件（夹）对象
	 * @param details 用于存储文件夹下文件夹大小信息
	 * @param onlyDirDetail 是否只需要子文件夹信息
	 * @return
	 */
	public static long getFileSizeBytes(File file, Map<File, Long> details, 
			boolean onlyDirDetail) {
		if (!file.exists()) {
			return 0;
		}
		//如果File对象是一个文件，则返回文件的大小
		if (file.isFile()) {
			return file.length();
		}
		
		long totalSizeBytes = 0;
		//如果File对象是一个目录，则列出该对象的所有子文件和路径
		File[] subFiles = file.listFiles();
		if (null != subFiles) {
			for (File subFile : subFiles) {
				//采用递归方法
				long subFileSizeBytes = getFileSizeBytes(subFile, null, onlyDirDetail);
				
				if (null != details) {
					if (onlyDirDetail) {
						if (subFile.isDirectory()) {
							details.put(subFile.getAbsoluteFile(), subFileSizeBytes);
						}
					} else {
						details.put(subFile.getAbsoluteFile(), subFileSizeBytes);
					}
				}
				
				totalSizeBytes += subFileSizeBytes;
			}
		}
		
		return totalSizeBytes;
	}
	
	/**
	 * 从assets目录读取字符文件。
	 * 
	 * @param filePath 文件路径
	 * @return 文件内容
	 */
	public static String readAssetsFile(Context context, String filePath) {
		String content = "";
		
		AssetManager assetManager = context.getResources().getAssets();
		try {
			InputStream ins = assetManager.open(filePath);
			byte[] buffer = new byte[ins.available()];
			
			ins.read(buffer);
			ins.close();
			
			content = new String(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return content;
	}
	
}
