package com.lamost.webservice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

/**
 * Created by Jia on 2016/4/6.
 */
public class WebService {
	private static final String TAG = "WebService";
	// 命名空间
	private final static String SERVICE_NS = "http://ws.smarthome.zfznjj.com/";
	// EndPoint
	// private final static String SERVICE_URL =
	// "http://192.168.2.106:8080/zfzn02/services/smarthome?wsdl=SmarthomeWs.wsdl";

	// 阿里云
	private final static String SERVICE_URL = "http://101.201.211.87:8080/zfzn02/services/smarthome?wsdl=SmarthomeWs.wsdl";
	// lv
	// private final static String SERVICE_URL =
	// "http://192.168.0.104:8080/ZFZNJJ_WS/services/samrthome?wsdl=SmarthomeWs.wsdl";
	// SOAP Action
	//private static String soapAction = "";
	// 调用的方法名称
	//private static String methodName = "";
	private HttpTransportSE ht;
	//private SoapSerializationEnvelope envelope;
	//private SoapObject soapObject;
	//private SoapObject result;

	public WebService() {
		ht = new HttpTransportSE(SERVICE_URL); // ①
		ht.debug = true;
		// mDC = DataControl.getInstance();
	}

	/**
	 * 当主控收到子节点发送的状态改变信息时，调用该方法，向服务器更新相应的电器状态
	 * 
	 * @param masterCode
	 *            主控编号
	 * @param electricCode
	 *            电器编号
	 * @param electicState
	 *            电器状态
	 * @param stateInfo
	 *            状态的参数信息，如窗帘打开的比例
	 */
	public void updateElectricState(String masterCode, String electricCode,
			String electricState, String stateInfo) {
		// HttpTransportSE ht = new HttpTransportSE(SERVICE_URL) ;
		HttpTransportSE ht = new HttpTransportSE(SERVICE_URL) ;
        ht.debug = true;
        SoapSerializationEnvelope envelope;
        SoapObject soapObject;
        //SoapObject result;

		String methodName = "updateElectricState";
		String soapAction = SERVICE_NS + methodName;// 通常为命名空间 + 调用的方法名称

		// 使用SOAP1.1协议创建Envelop对象
		envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11); // ②
		// 实例化SoapObject对象
		soapObject = new SoapObject(SERVICE_NS, methodName); // ③
		// 将soapObject对象设置为 SoapSerializationEnvelope对象的传出SOAP消息
		envelope.bodyOut = soapObject; // ⑤
		envelope.dotNet = true;
		envelope.setOutputSoapObject(soapObject);
		soapObject.addProperty("masterCode", masterCode);
		soapObject.addProperty("electricCode", electricCode);
		soapObject.addProperty("electricState", electricState);
		soapObject.addProperty("stateInfo", stateInfo);
		try {
			System.out.println("$$$$$$$$$$$$$$$$$$$$$$");
			ht.call(soapAction, envelope);
			System.out.println("##############################");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} finally {
			//resetParam();
		}
	}

	/**
	 * 根据传入的主控编号，返回有没有该主控下的电器设备状态改变
	 * 
	 * @param masterCode
	 *            主控编号
	 */
	public String masterReadElecticOrder(String masterCode) {

		/*ht = new HttpTransportSE(SERVICE_URL);
		ht.debug = true;*/
		HttpTransportSE ht = new HttpTransportSE(SERVICE_URL) ;
        ht.debug = true;
        SoapSerializationEnvelope envelope;
        SoapObject soapObject;
        SoapObject result;

		String methodName = "masterReadElecticOrder";
		String soapAction = SERVICE_NS + methodName;// 通常为命名空间 + 调用的方法名称

		// 使用SOAP1.1协议创建Envelop对象
		envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11); // ②
		// 实例化SoapObject对象
		soapObject = new SoapObject(SERVICE_NS, methodName); // ③
		// 将soapObject对象设置为 SoapSerializationEnvelope对象的传出SOAP消息
		envelope.bodyOut = soapObject; // ⑤
		envelope.dotNet = true;
		envelope.setOutputSoapObject(soapObject);
		soapObject.addProperty("masterCode", masterCode);
		try {
			// System.out.println("测试1");
			ht.call(soapAction, envelope);
			// System.out.println("测试2");
			// 根据测试发现，运行这行代码时有事会抛出空指针异常，使用加了一句进行处理
			if (envelope != null && envelope.getResponse() != null) {
				// 获取服务器响应返回的SOAP消息
				// System.out.println("测试3");
				result = (SoapObject) envelope.bodyIn; // ⑦
				// 接下来就是从SoapObject对象中解析响应数据的过程了
				// System.out.println("测试4");
				String flag = result.getProperty(0).toString();
				Log.e(TAG, "*********Webservice masterReadElecticOrder 服务器返回值："
						+ flag);
				return flag;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} finally {
			//resetParam();
		}
		return -1 + "";
	}

	/**
	 * 读取该账户的所有的用户号码
	 * 
	 * @param masterCode
	 */
	public List<String> getAccountCodesByMaster(String masterCode) {
		SoapSerializationEnvelope envelope;
		SoapObject soapObject;
		SoapObject result;
		HttpTransportSE ht = new HttpTransportSE(SERVICE_URL);
		ht.debug = true;

		String methodName = "getAccountCodesByMaster";
		String soapAction = SERVICE_NS + methodName;

		// 使用SOAP1.1协议创建Envelop对象
		envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11); // ②
	
		// 实例化SoapObject对象
		soapObject = new SoapObject(SERVICE_NS, methodName); // ③
		// 将soapObject对象设置为 SoapSerializationEnvelope对象的传出SOAP消息
		envelope.bodyOut = soapObject; // ⑤
		envelope.dotNet = true;
		envelope.setOutputSoapObject(soapObject);
		soapObject.addProperty("masterCode", masterCode);
		try {
			System.out.println("$$$$$$$$$$$$$$$$$$$$$");
			ht.call(soapAction, envelope);
			System.out.println("##############################");
			if (envelope.getResponse() != null) {
				// 获取服务器响应返回的SOAP消息
				result = (SoapObject) envelope.bodyIn; // ⑦
				List<String> phoneNumList = new ArrayList<String>();
				// 解析数据
				for (int i = 0; i < result.getPropertyCount(); i++) {
					String phone = result.getProperty(i).toString();
					phoneNumList.add(phone);
				}
				
				return phoneNumList;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} finally {
			/*envelope = null;
			soapObject = null;
			result = null;*/
		}
		return null;
	}

	/*private void resetParam() {
		envelope = null;
		soapObject = null;
		result = null;
	}*/

}
