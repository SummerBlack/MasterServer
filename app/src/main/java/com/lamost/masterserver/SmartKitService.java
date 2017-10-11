package com.lamost.masterserver;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

import com.iflytek.aiui.uartkit.UARTAgent;
import com.iflytek.aiui.uartkit.util.PacketBuilder;
import com.lamost.datainfo.SceneElectric;
import com.lamost.datainfo.SceneElectricDao;
import com.lamost.utils.LogHelper;
import com.lamost.webservice.WebService;

/**
 * @author C.Feng
 * @version
 * @date 2017-2-18 下午4:18:58
 */
public class SmartKitService extends Service {
	private static final String TAG = "SmartKitServer";
	// 该Service的ACTION属性值
	public static final String ACTION = "com.lamost.masterserver.action.SmartKitService";

	public static final int NOTIFICATION_ID = 2;

	private UdpServerConnection mUdpConnection = null;
	private TcpServerConnection mTcpServerConnection = null;
	// 保存本机的IP+MAC
	private WifiManager mWifiManager = null;
	private WifiInfo mWifiInfo = null;
	private AppDeamonThread mAppDeamonThread = null;

	private SceneElectricDao sceneElectricDao = null;

	private SharedPreferences.Editor mSensorEditor = null;

	public SmartKitService() {

	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		try {
			mUdpConnection = new UdpServerConnection();
			mTcpServerConnection = new TcpServerConnection(SmartKitService.this);
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*// 取得WifiManager对象
		mWifiManager = (WifiManager) SmartKitService.this
				.getSystemService(Context.WIFI_SERVICE);
		// 取得WifiInfo对象
		mWifiInfo = mWifiManager.getConnectionInfo();*/

		sceneElectricDao = mTcpServerConnection.getSceneElectricDao();
		/**
		 * 开启三个线程，分别作为接受tcp连接与交互，udp连接与交互，读取远程控制指令
		 */
		tcpReceive();
		udpReceiveSearch();
		masterReadElecticOrder();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		android.os.Debug.waitForDebugger();
		LogHelper.d("前台运行");
		startForeground();
		// 开启守护
		startDeamon();
		return super.onStartCommand(intent, Service.START_STICKY, startId);
	}

	/**
	 * 准备两个服务， 互相判断是否存在， 如果不存在则重启对方 开启一个守护进程
	 */
	public void startDeamon() {
		if (mAppDeamonThread == null || !mAppDeamonThread.isAlive()) {
			mAppDeamonThread = new AppDeamonThread(SmartKitService.this);
			mAppDeamonThread.start();
		}
	}

	/**
	 * 开启一个新的线程，TCP服务器接收客户端的连接
	 */
	public void tcpReceive() {
		Log.e(TAG, "Master sceneControl====>");
		new Thread(new Runnable() {

			@Override
			public void run() {
				mTcpServerConnection.tcpServer();
			}
		}).start();
	}

	/**
	 * 开启一个新的线程，一直在监听有没有客户端通过UDP广播来搜索该设备的信息，如果有就回复(包括：ip+mac+masterCode)
	 */
	public void udpReceiveSearch() {

		Log.e(TAG, "udpReceiveSearch====>");
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					// 如果接收到搜索
					if (mUdpConnection.udpServerReceiveSearch()) {
						Log.e(TAG, "接收到客户端的搜索信息");
						// 当接收到搜索后，回复本机IP+","+MAC地址
						//取得WifiManager对象
						mWifiManager = (WifiManager) SmartKitService.this
								.getSystemService(Context.WIFI_SERVICE);
						// 取得WifiInfo对象
						mWifiInfo = mWifiManager.getConnectionInfo();
						String IPAddress = ipToString(mWifiInfo.getIpAddress());
						String MACAddress = mWifiInfo.getMacAddress();
						String sendBuff = IPAddress + "," + MACAddress + ","
								+ mTcpServerConnection.getMasterCode() + ",\n";
						try {
							mUdpConnection.udpServerReturnSearch(sendBuff
									.getBytes());
							Log.e(TAG, "发送回复：" + sendBuff);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();

		Log.e(TAG, "<===udpReceiveSearch");
	}

	/**
	 * 开启一个新的线程，每隔一段时间就询问一下阿里云服务器，看看该主机下的电器设备有没有被远程控制，如果有，就做出相应动作
	 * 暂时定为程序运行后5s之后，每隔2s询问一次。5s是考虑到程序运行时，查询主控的编号需要一些时间，
	 * 将来可以考虑第一次的时候查询好编号后，sp保存起来，以后就不用查了，因为一个主控的编号时唯一的，不会改变
	 * 
	 * 不设置5s也没关系，如果没有主机编号，就不会去读取，因为是每隔2s读一次，等到什么时候有主机编号了就什么时候开始读
	 */
	public void masterReadElecticOrder() {
		Log.e(TAG, "masterReadElecticOrder====>");
		new Thread(new Runnable() {

			@Override
			public void run() {
				new Timer().schedule(new TimerTask() {
					@Override
					public void run() {
						String masterCode = mTcpServerConnection
								.getMasterCode();
						if (masterCode != null) {
							// 从服务器读取远程控制指令
							String orderResult = "none";
							WebService mService = mTcpServerConnection
									.getmWebService();
							orderResult = mService
									.masterReadElecticOrder(mTcpServerConnection
											.getMasterCode().substring(1, 9));
							Log.e(TAG, "读取服务器" + masterCode.substring(1, 9)
									+ "的远程控制指令为:" + orderResult);
							if (!orderResult.equals("none")
									&& !orderResult.equals("-1")) {
								// 说明有电器需要控制，首先需要解析收到的控制指令，然后将控制指令通过串口发送给cc2530
								// 读取服务器AA00FF88的远程控制指令为:<07006962XI**********00>|<07006962XH64********00>|<07006962XH64********00>
								String[] orders = orderResult.split("\\|");

								// UARTAgent mAgent = UARTAgent.getUARTAgent();
								for (String ord : orders) {
									try {
										handleInstructions(ord);
									} catch (NumberFormatException e) {
										LogHelper.d("该字符串不能转换为数字");
									}

								}
							} else {
								// 调试时使用，将来可以去掉
								// Log.e(TAG,
								// "没有读取到服务器"+mTcpServerConnection.getMasterCode().substring(1,9)+"的远程控制指令");
							}
						}

					}
				}, 5000, 2000);
			}
		}).start();
		Log.e(TAG, "<===masterReadElecticOrder");
	}

	/**
	 * 解析从服务器读取到的远程指令并处理 对于远程指令分为以下几类： 1.在手机端添加、删除、更改情景模式及对应的电器，此时会收到形如：
	 * 将010032FA电器的第1按键(01)对应的电器打开状态(H)添加到第0组情景<010032FASH010*******00>
	 * 将该电器从第0组删除<010032FASR010*******00>
	 * 2.情景开关和传感器与情景模式关联时，会收到<0A001467SH.....>
	 * ,这个时候我们只需要转发给主节点，由主节点发送给相应的情景开关完成配置即可 3.T表示与情景模式相关的远程控制信息
	 * 4.其他如<07006962XH**********00>，就是普通的远程控制电器指令
	 * 
	 * @param ord
	 *            从服务器收到的远程指令
	 */
	private void handleInstructions(String ord) throws NumberFormatException {
		String instr_Type = ord.substring(9, 10);

		String electric_Type = ord.substring(1, 3);// 电器类型
		String electric_code = ord.substring(1, 9);// 第1-8位
		String order_info = ord.substring(11, 13);// 11-12位
		String electric_order = ord.substring(10, 11);// 第10位

		switch (instr_Type) {
		case "S":// 情景模式中电器的增删改查
			int scene_index = Integer.parseInt(ord.substring(13, 14));// 第13位
			if ("0A".equals(electric_Type)) {// 情景开关
				// 发送给情景开关
				UARTAgent mAgent = UARTAgent.getUARTAgent();
				mAgent.sendMessage(PacketBuilder.obtainCustomPacket(ord
						.getBytes()));
				Log.e(TAG, "发送控制指令" + ord);
			} else if ("0D".equals(electric_Type)) {// 传感器
				mSensorEditor = mTcpServerConnection.getmSensorEditor();
				mSensorEditor.putInt(electric_code, scene_index);
				mSensorEditor.commit();
			} else {
				if ("R".equals(electric_order)) {// 删除情景模式下的电器
					// 删除相应情景模式下的电器指令
					sceneElectricDao.delete(scene_index, electric_code,
							order_info);
				} else {
					// 判定情景模式下的电器指令，如果表中已经存在则更新，反之则保存
					List<SceneElectric> list = sceneElectricDao.getByParams(
							scene_index, electric_code, order_info);
					if (list != null && !list.isEmpty()) {
						// 如果在表中已经存在，则更新
						for (SceneElectric elec : list) {
							if (elec != null) {
								elec.setElectric_order("X" + electric_order);
								sceneElectricDao.update(elec);
							}
						}
					} else {
						// 如果在表中不存在就保存
						sceneElectricDao.save(new SceneElectric(-1,
								scene_index, electric_code, "X"
										+ electric_order, order_info));
					}
				}
			}

			break;
		case "T":// 对于整个情景模式的操作
			// 情景模式的类型
			String sceneType = ord.substring(10, 11);
			// 提取出情景模式的编号
			int sceneIndex = Integer.parseInt(ord.substring(13, 14));// 第13位

			if ("H".equals(sceneType)) {// 执行情景模式
				// 获取该编号下的指令集，并通过串口发送
				mTcpServerConnection.sendSceneOrder(sceneIndex + "");
			} else if ("G".equals(sceneType)) {// 失效模式

			} else if ("R".equals(sceneType)) {// 删除某个情景模式
				sceneElectricDao.delete(sceneIndex);
			}

			break;

		case "X":// 电器控制
			// 普通的控制指令
			UARTAgent mAgent = UARTAgent.getUARTAgent();
			mAgent.sendMessage(PacketBuilder.obtainCustomPacket(ord.getBytes()));
			Log.e(TAG, "发送控制指令" + ord);
			break;
		default:

			break;

		}
	}

	/**
	 * 将IP地址转换成字符串(将32位数字转换为字符串)
	 * 
	 * @param ipInt
	 * @return
	 */
	private String ipToString(int ipInt) {
		String serverip = String.valueOf(new StringBuilder()
				.append((ipInt & 0xff)).append('.').append((ipInt >> 8) & 0xff)
				.append('.').append((ipInt >> 16) & 0xff).append('.')
				.append(((ipInt >> 24) & 0xff)).toString());
		return serverip;
	}

	/**
	 * 在手机休眠一段时间后（1-2小时），后台运行的服务被强行kill掉，有可能是系统回收内存的一种机制
	 * 要想避免这种情况可以通过startForeground让服务前台运行，当stopservice的时候通过stopForeground去掉
	 */
	private void startForeground() {
		// 创建一个启动Service的Intent，从程序看，启动的是自己...
		Intent serviceIntent = new Intent();
		serviceIntent.setAction(ACTION);
		serviceIntent.setPackage(getPackageName());
		// 显示在手机状态栏的通知
		PendingIntent pendingIntent = PendingIntent.getService(
				SmartKitService.this, 0, serviceIntent, 0);
		Notification notification = new Notification.Builder(
				SmartKitService.this)
		// 设置显示在状态栏的通知提示消息
				.setTicker("SmartKit is running.")
				// 设置通知内容的标题
				.setContentTitle("SmartKit")
				// 设置通知内容
				.setContentText("Hello, SmartKit!")
				// 设置通知将要启动程序的Intent
				.setContentIntent(pendingIntent).build();
		// 使此服务在前台运行
		startForeground(NOTIFICATION_ID, notification);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		LogHelper.d("SmartKitService onDestory!");
		mUdpConnection = null;
		mTcpServerConnection = null;
		mWifiManager = null;
		mWifiInfo = null;

		mAppDeamonThread.stopRun();
		mAppDeamonThread = null;
		// 停止前台运行
		stopForeground(true);

		LogHelper.d("SmartKitService onDestory!");
	}

}
