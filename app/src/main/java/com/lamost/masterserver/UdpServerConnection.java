package com.lamost.masterserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

import android.util.Log;

public class UdpServerConnection {
	private static final String TAG = "UdpConnection";
	//使用常量作为本程序的多点广播IP地址（224.0.0.0至239.255.255.255范围内即可）
	private static final String BROADCAST_IP = "230.0.0.255";
	//使用常量作为本程序的多点广播端口
	private static final int BROADCAST_PORT = 48899;
	//定义每个数据包的大小最大为1KB
	private static final int DATA_LEN = 1024;
	//定义本程序MulticastSocket实例
	private MulticastSocket udpSocket = null;
	private InetAddress broadcastAddress = null;
	//接收网络数据的字节数组
	private byte[] receiveBuff = new byte[DATA_LEN];
	//以指定字节数组创建准备接收数据的DatagramPacket对象
	private DatagramPacket receivePacket = new DatagramPacket(receiveBuff, receiveBuff.length);
	//定义一个用于发送的DatagramPacket对象
    private DatagramPacket sendPacket = null;

	public UdpServerConnection() throws IOException {
		// TODO Auto-generated constructor stub
		init();
	}
	
	private void init() throws IOException{
		//创建用于发送、接收数据的MulticastSocket对象
        //由于该MulticastSocket对象需要接收数据，所以有指定端口
		udpSocket = new MulticastSocket(BROADCAST_PORT);
		broadcastAddress = InetAddress.getByName(BROADCAST_IP);
		//将该socket加入到指定的多点广播地址
		udpSocket.joinGroup(broadcastAddress);
		//设置本MulticastSocket发送的数据报不会发送到本身
		udpSocket.setLoopbackMode(true);
	}
	/**
	 * 接收手机端的搜索信息并判断是不是约定格式
	 * @return
	 */
	public boolean udpServerReceiveSearch(){
		Arrays.fill(receiveBuff, (byte) 0);
		try {
			udpSocket.setSoTimeout(5000);
			// 该方法会阻塞线程，直到收到数据
			udpSocket.receive(receivePacket);
			// 如果收到信息，判断是否为搜索信息
			if(getSearchInfo(receivePacket.getData())){
				//当接收到搜索后，回复本机IP+","+MAC地址
				Log.e(TAG, receivePacket.getAddress().toString());
				Log.e(TAG, receivePacket.getPort()+"");
				return true;
	        } 
		}catch (IOException e) {
			//e.printStackTrace();
			Log.e(TAG, "uudpClientReceiveSearch outtime " ); 
    		return false;
		}
	
		return false;
	}
	/**
	 * 收到手机搜索信息后回复
	 * @param sendBuff
	 * @throws IOException
	 */
	public void udpServerReturnSearch(byte[] sendBuff) throws IOException{
		sendPacket = new DatagramPacket(new byte[0],0,receivePacket.getAddress(),receivePacket.getPort());
		sendPacket.setData(sendBuff);
		udpSocket.send(sendPacket);
	}
	
	public void udpServerClose(){
		
	}
	/**
     * 判断收到的是否是按照约定格式的广播
     * @param bufferBytesRegister
     * @return
     */
    private boolean getSearchInfo(byte[] bufferBytes){
    	String receiveString  = new String(bufferBytes,0,bufferBytes.length);
    	if(receiveString.contains("HF-A11ASSISTHREAD")){
    		return true;
    	}
		 return false;         
    }
}
