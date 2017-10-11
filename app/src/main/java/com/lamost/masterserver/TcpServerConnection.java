package com.lamost.masterserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.iflytek.aiui.uartkit.UARTAgent;
import com.iflytek.aiui.uartkit.constant.UARTConstant;
import com.iflytek.aiui.uartkit.entity.CustomPacket;
import com.iflytek.aiui.uartkit.entity.MsgPacket;
import com.iflytek.aiui.uartkit.listener.EventListener;
import com.iflytek.aiui.uartkit.listener.UARTEvent;
import com.iflytek.aiui.uartkit.util.PacketBuilder;
import com.lamost.datainfo.SceneElectric;
import com.lamost.datainfo.SceneElectricDao;
import com.lamost.sms.SMS;
import com.lamost.utils.LogHelper;
import com.lamost.webservice.WebService;

public class TcpServerConnection {
	private static final int TCP_PORT = 8899;
	private ServerSocket mServerSocket = null;
	private Context mContext = null;
	// 串口通信
	private UARTAgent mAgent = null;
	// 定义保存所有Socket的HashMap，并将其包装为线程安全的
	private List<Socket> socketList = Collections
			.synchronizedList(new ArrayList<Socket>());

	private String masterCode = null;
	// 与服务器的通信
	private WebService mWebService = null;
	// 用于保存主控的编号
	private SharedPreferences mMasterCodePreferences = null;
	private SharedPreferences.Editor mMasterCodeEditor = null;
	// 保存传感器的配置信息，即每个对应eleCode的传感器，关联哪一组情景模式
	private SharedPreferences mSensorPreferences = null;
	private SharedPreferences.Editor mSensorEditor = null;
	// 保存传感器触发的时间，即
	private SharedPreferences mSensorTimePreferences = null;
	private SharedPreferences.Editor mSensorTimeEditor = null;
	// 保存和编辑情景模式相关数据
	private SceneElectricDao sceneElectricDao = null;
	// 保存传感器的触发时间，用于发送广播
	private Map<String, Long> lastTimeList = new HashMap<>();

	public SceneElectricDao getSceneElectricDao() {
		return sceneElectricDao;
	}

	private void setSceneElectricDao(SceneElectricDao sceneElectricDao) {
		this.sceneElectricDao = sceneElectricDao;
	}

	public WebService getmWebService() {
		return mWebService;
	}

	public TcpServerConnection(Context mContext) throws IOException {
		this.mContext = mContext;
		// 创建一个ServerSocket,用于监听客户端socket的连接请求
		mServerSocket = new ServerSocket(TCP_PORT);
		mWebService = new WebService();
		// 获取只能被本应用程序读写的SharedPreferences对象
		mMasterCodePreferences = mContext.getSharedPreferences("masterCode",
				Context.MODE_PRIVATE);
		mMasterCodeEditor = mMasterCodePreferences.edit();

		mSensorPreferences = mContext.getSharedPreferences("sensor",
				Context.MODE_PRIVATE);
		mSensorEditor = mSensorPreferences.edit();

		mSensorTimePreferences = mContext.getSharedPreferences("sensorTime",
				Context.MODE_PRIVATE);
		mSensorTimeEditor = mSensorTimePreferences.edit();

		setSceneElectricDao(new SceneElectricDao(mContext));

		mAgent = UARTAgent.createAgent("/dev/ttyS2", 115200,
				new EventListener() {
					@Override
					public void onEvent(UARTEvent event) {
						switch (event.eventType) {
						case UARTConstant.EVENT_INIT_SUCCESS:// 串口初始化成功且握手成功
							LogHelper.d("Init UART Success");

							// 初始化成功以后就去询问向2530该主控的编号
							String masterCode = mMasterCodePreferences
									.getString("masterCode", null);
							if (masterCode != null) {
								setMasterCode(masterCode);
								LogHelper.d("直接回复保存的主控编号");
							} else {
								byte[] searchMasterData = "<00000000U0**********00>"
										.getBytes();
								mAgent.sendMessage(PacketBuilder
										.obtainCustomPacket(searchMasterData));
								LogHelper.d("询问主控编号");
							}
							break;

						case UARTConstant.EVENT_INIT_FAILED:
							LogHelper.d("Init UART Failed");
							break;
						// 接收到串口发来的数据包
						case UARTConstant.EVENT_MSG:
							MsgPacket recvPacket = (MsgPacket) event.data;
							try {
								processPacket(recvPacket);
							} catch (Exception e) {
								LogHelper.d("处理出错");
							}
							break;
						// 串口发送失败
						case UARTConstant.EVENT_SEND_FAILED:
							MsgPacket sendPacket = (MsgPacket) event.data;
							mAgent.sendMessage(sendPacket);
							LogHelper.d("串口发送失败");
							break;
						default:
							break;
						}
					}
				});
	}

	/**
	 * 对串口接收到的数据包做出处理
	 * 
	 * @param packet
	 */
	private void processPacket(final MsgPacket packet) throws Exception {
		switch (packet.getMsgType()) {

		case MsgPacket.CUSTOM_PACKET_TYPE:

			LogHelper.d("recv aiui custom data"
					+ bytes2hex(((CustomPacket) packet).customData));
			
			// 判断收到的信息是不是设备的编号
			if (((CustomPacket) packet).customData[9] == 'U') {
				setMasterCode(new String(((CustomPacket) packet).customData));
				// 保存主控编号
				mMasterCodeEditor.putString("masterCode", masterCode);
				mMasterCodeEditor.commit();

				LogHelper.d("收到设备编号信息:"
						+ new String(((CustomPacket) packet).customData));
			}
			
			// 判断收到的信息是不是子节点状态改变，子节点的状态信息需要向阿里云服务器更新，同时发送给连接的每一个手机客户端
			if (((CustomPacket) packet).customData[9] == 'Z') {
				if (masterCode != null) {
					new Thread(new Runnable() {

						@Override
						public void run() {
							// 电器的编号如：03002A4E
							String electricCode = new String(
									((CustomPacket) packet).customData, 1, 8);
							String electicState = new String(
									((CustomPacket) packet).customData, 9, 2);
							String stateInfo = new String(
									((CustomPacket) packet).customData, 11, 10);

							mWebService.updateElectricState(
									masterCode.substring(1, 9), electricCode,
									electicState, stateInfo);
							LogHelper.d("成功更新子节点状态为："
									+ masterCode.substring(1, 9) + ":"
									+ electricCode + ":" + electicState + ":"
									+ stateInfo);

							String electricId = new String(
									((CustomPacket) packet).customData, 1, 2);
							// 如果是情景开关，还需要控制相应设备;
							if ("0A".equals(electricId)) {
								String sceneIndex = new String(((CustomPacket) packet).customData, 10, 1);
								// 发送控制指令
								sendSceneOrder(sceneIndex);
							} else if ("0D".equals(electricId)) {
								// 如果是传感器，报警后也需要控制相应关联设备
								int sceneIndex = mSensorPreferences.getInt(electricCode, -1);
								if (sceneIndex >= 0) {
									sendSceneOrder(sceneIndex + "");
								}
								// 同时对于燃气、水浸等要发送短信
								String sceneType = new String(((CustomPacket) packet).customData, 1, 4);// 传感器类型
								switch (sceneType) {
								case "ODA1"://温度
									String tempstr = new String(
											((CustomPacket) packet).customData, 11, 2);
									try{
										int temp = Integer.parseInt(tempstr);
										if (temp >= 35) {
											long time = System.currentTimeMillis();
											Long lastTime = lastTimeList.get(electricCode);
											if (lastTime == null || (time - lastTime) > 8*1000L) {
												lastTimeList.put(electricCode, time);
												//发送报警广播，语音接收播报
												Intent intent = new Intent();
												intent.setAction("com.iflytek.aiuiproduct.receiver.action.SENSOR");
												Bundle value = new Bundle();
												value.putString("sensorType", sceneType);
												value.putString("sensorState", electicState);
												intent.putExtra("sensor", value);
												mContext.sendBroadcast(intent);
											}
										}
									} catch(NumberFormatException e){
										
									}
									break;
									
								case "0D73":// 水浸

									//break;
								case "0D41":// 燃气
									if ("Z1".equals(electicState)) {// 触发报警
										// 当前时间
										long time = System.currentTimeMillis();
										// 传感器上次触发的时间
										long scensorTime = mSensorTimePreferences.getLong(electricCode, -1);
										// 如果是第一次触发，或者触发时间和上次间隔30分钟或以上
										if (time == -1 || (time - scensorTime) >= (1 * 60 * 1000L)) {
											LogHelper.d("触发新一轮报警：" + electricCode);
											// 更新传感器的触发时间
											mSensorTimeEditor.putLong(electricCode, time);
											mSensorTimeEditor.commit();
											// 发送短信给用户
											List<String> phoneNumList = mWebService
													.getAccountCodesByMaster(masterCode.substring(1, 9));
											if (phoneNumList != null && phoneNumList.size() > 0) {
												StringBuilder mobiles = new StringBuilder();
												mobiles.append("[");

												for (int i = 0, size = phoneNumList.size(); i < size - 1; i++) {
													mobiles.append("'" + phoneNumList.get(i) + "',");
												}
												
												mobiles.append("'"+ phoneNumList.get(phoneNumList.size() - 1)+ "']");
												
												
												try {
													SMS.sendMessageToUser(3059865, mobiles.toString(), "['燃气']");
												} catch (IOException e) {
													e.printStackTrace();
												}
											}
										}
										//long time = System.currentTimeMillis();
										Long lastTime = lastTimeList.get(electricCode);
										if (lastTime == null || (time - lastTime) > 8*1000L) {
											lastTimeList.put(electricCode, time);
											//发送报警广播，语音接收播报
											Intent intent = new Intent();
											intent.setAction("com.iflytek.aiuiproduct.receiver.action.SENSOR");
											Bundle value = new Bundle();
											value.putString("sensorType", sceneType);
											value.putString("sensorState", electicState);
											intent.putExtra("sensor", value);
											mContext.sendBroadcast(intent);
										}	
									} else if ("Z0".equals(electicState)) {// 解除报警
										// 发送短信，因为解除报警只有在触发报警后，解除之后发一次，频率较低，不加时间间隔限制
										LogHelper.d("解除报警：" + electricCode);
										/*try {
											SMS.sendMessageToUser(3053702, mobiles.toString(), "['燃气']");
										} catch (IOException e) {
											e.printStackTrace();
										}*/
										Intent intent = new Intent();
										intent.setAction("com.iflytek.aiuiproduct.receiver.action.SENSOR");
										Bundle value = new Bundle();
										value.putString("sensorType", sceneType);
										value.putString("sensorState", electicState);
										intent.putExtra("sensor", value);
										mContext.sendBroadcast(intent);
									}
									
									break;
								
								default:
									break;
								}
							}
						}
					}, "updateElectricState").start();
				}
			}
			// 向所有与之连接的客户端广播
			String s1 = new String(((CustomPacket) packet).customData);
			for (Socket s : socketList) {
				s.getOutputStream().write((s1 + "\r\n").getBytes());
				LogHelper.d("发送给连接的客户端:" + s.getInetAddress().toString() + "数据"
						+ s1 + "\r\n");
			}

			break;
		default:
			break;
		}
	}

	/**
	 * 将字符型转换成16进制
	 * 
	 * @param bytes
	 * @return
	 */
	private String bytes2hex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		String tmp = null;
		sb.append("[");
		for (byte b : bytes) {
			// 将每个字节与0xFF进行与运算，然后转化为10进制，然后借助于Integer再转化为16进制
			tmp = Integer.toHexString(0xFF & b);
			if (tmp.length() == 1)// 每个字节8为，转为16进制标志，2个16进制位
			{
				tmp = "0" + tmp;
			}
			sb.append(" " + tmp.toUpperCase());
		}
		sb.append("]");
		return sb.toString();
	}

	/**
	 * 使用阻塞式的Socket长连接，服务器每接收到一个客户端，就会开启一个新的线程进行处理该客户端的数据输入输出
	 * 该方法的弊端就是当有大量客户端接入时需要开启大量新线程，会导致程序崩溃，但是我们的一个主控，连接它的客
	 * 户端即手机数量非常有限，故目前就使用这种方法，将来可以考虑MINA非阻塞式网络架构
	 */
	public void tcpServer() {
		Socket clientSocket = null;
		try {
			while (true) {
				LogHelper.d("等待手机客户端请求tcp连接");
				clientSocket = mServerSocket.accept();

				LogHelper.d("收到手机客户端请求tcp连接");
				LogHelper.d("客户端IP为："
						+ clientSocket.getInetAddress().toString());
				socketList.add(clientSocket);
				new Thread(new ServerThread(clientSocket), clientSocket
						.getInetAddress().toString()).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getMasterCode() {
		return masterCode;
	}

	public void setMasterCode(String masterCode) {
		this.masterCode = masterCode;
	}

	/**
	 * 通过串口发送情景模式控制指令
	 * 
	 * @param sceneIndex
	 *            情景模式的编号
	 */
	public void sendSceneOrder(String sceneIndex) {
		try {
			int scene_index = Integer.parseInt(sceneIndex);
			// 根据情景模式的编号查询相应指令
			List<SceneElectric> list = sceneElectricDao
					.getByScene_index(scene_index);
			if (list != null && !list.isEmpty()) {

				for (SceneElectric ele : list) {
					if (ele != null) {
						String order = "<" + ele.getElectric_code()
								+ ele.getElectric_order() + ele.getOrder_info()
								+ "********00>";
						mAgent.sendMessage(PacketBuilder
								.obtainCustomPacket(order.getBytes()));
						// 两条指令中加入500毫秒的间隔
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						LogHelper.d("已通过串口发送情景模式--" + sceneIndex + "--控制指令数据包");

					}
				}
			}
		} catch (NumberFormatException e) {
			LogHelper.d("该字符串不能转换为数字");
		}
	}

	class ServerThread implements Runnable {
		private Socket clientSocket = null;
		private BufferedReader br = null;
		private OutputStream os = null;

		public ServerThread(Socket clientSocket) throws IOException {
			this.clientSocket = clientSocket;
			br = new BufferedReader(new InputStreamReader(
					this.clientSocket.getInputStream()));
			os = this.clientSocket.getOutputStream();
		}

		@Override
		public void run() {
			try {
				String content = null;
				// 如果读取返回null，表明客户端与服务器连接断开，否则一直处于循环中
				while ((content = readFromClient()) != null) {
					// Log.e(TAG, "收到数据:"+content);
					LogHelper.d("收到数据:" + content);
					tcpRecProcess(content);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				// 在所有删除断开的连接中，目前只有这句是起作用的
				socketList.remove(clientSocket);
				LogHelper.d("连接已经断开，删除掉1"
						+ clientSocket.getInetAddress().toString());
				try {
					if (!clientSocket.isClosed()) {
						clientSocket.close();
						LogHelper.d("关掉相应网络资源");
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		/**
		 * 读取客户端发送过来的数据，如果客户端与服务器断开连接会返回null
		 * 
		 * @return
		 */
		private String readFromClient() {
			if (clientSocket.isConnected()) {
				try {
					return br.readLine();
				} catch (IOException e) {
					return null;
				}
			} else {
				return null;
			}
		}

		/**
		 * 对接收到的客户端的数据做出处理，一般服务器接收到客户端的数据包括几类： 1
		 * 客户端搜索主控的编号，如：<00000000U0**********00> 2
		 * 客户端添加电器设备，如：<03000000Y0**********00>,希望添加3键开灯 3
		 * 在本地连接时，客户端向主控发送控制指令，如<03002A4EXH**********00>
		 * 
		 * @param content
		 * @throws IOException
		 */
		private void tcpRecProcess(String content) throws IOException {
			// 提取出标志位
			String flag = content.substring(9, 10);
			switch (flag) {
			// 搜索主控的编号
			case "U": {
				// 需要通过串口去询问主机的编号
				String masterCode = mMasterCodePreferences.getString(
						"masterCode", null);
				if (masterCode != null) {
					// 直接发送主控的编号
					os.write((masterCode + "\r\n").getBytes());
					LogHelper.d("直接回复保存的主控编号");
				} else {
					// 重新询问一下主控的编号
					byte[] searchMasterData = "<00000000U0**********00>"
							.getBytes();
					mAgent.sendMessage(PacketBuilder
							.obtainCustomPacket(searchMasterData));
					LogHelper.d("已通过串口发送搜索主控编号数据包");
				}
				break;
			}
			// 添加电器设备
			case "Y": {
				// 通过串口往下发送消息，允许设备入网，当子节点加入以后会通过串口往上发送，AIUI收到后tcp发送至手机端
				byte[] addElecData = content.getBytes();
				mAgent.sendMessage(PacketBuilder
						.obtainCustomPacket(addElecData));
				LogHelper.d("已通过串口发送允许子节点入网数据包");
				break;
			}
			// 接收到控制指令
			case "X": {
				// 通过串口往下发送消息，该消息用于控制相应子节点的动作
				byte[] controlData = content.getBytes();
				mAgent.sendMessage(PacketBuilder
						.obtainCustomPacket(controlData));
				LogHelper.d("已通过串口发送控制指令数据包");
				break;
			}
			// 接收到执行情景模式指令,需要通过串口发送一连串的的控制指令
			case "T": {
				// 提取出情景模式的编号
				String sceneIndex = content.substring(13, 14);// 第13位
				// 获取该编号下的指令集，并通过串口发送
				sendSceneOrder(sceneIndex);
				break;
			}

			default:

			}
		}
	}

	public SharedPreferences getmSensorPreferences() {
		return mSensorPreferences;
	}

	public SharedPreferences.Editor getmSensorEditor() {
		return mSensorEditor;
	}
}