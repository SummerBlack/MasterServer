package com.lamost.update;

import java.io.IOException;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

/**
 * Created by Jia on 2016/4/6.
 */
public class UpdateWebService {
	private static final String TAG = "WebService";
	// 命名空间
	private final static String SERVICE_NS = "http://ws.smarthome.zfznjj.com/";
	// 阿里云
	private final static String SERVICE_URL = "http://101.201.211.87:8080/zfzn02/services/smarthome?wsdl=SmarthomeWs.wsdl";
	// SOAP Action
	private static String soapAction = "";
	// 调用的方法名称
	private static String methodName = "";
	private HttpTransportSE ht;
	private SoapSerializationEnvelope envelope;
	private SoapObject soapObject;
	private SoapObject result;

	public UpdateWebService() {
		ht = new HttpTransportSE(SERVICE_URL); // ①
		ht.debug = true;
	}

	public String getAppVersionVoice(String appName) {
		ht = new HttpTransportSE(SERVICE_URL);
		ht.debug = true;

		methodName = "getAppVersionVoice";
		soapAction = SERVICE_NS + methodName;// 通常为命名空间 + 调用的方法名称

		// 使用SOAP1.1协议创建Envelop对象
		envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11); // ②
		// 实例化SoapObject对象
		soapObject = new SoapObject(SERVICE_NS, methodName); // ③
		// 将soapObject对象设置为 SoapSerializationEnvelope对象的传出SOAP消息
		envelope.bodyOut = soapObject; // ⑤
		envelope.dotNet = true;
		envelope.setOutputSoapObject(soapObject);
		soapObject.addProperty("appName", appName);

		try {
			// System.out.println("测试1");
			ht.call(soapAction, envelope);
			// System.out.println("测试2");
			// 根据测试发现，运行这行代码时有时会抛出空指针异常，使用加了一句进行处理
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
			resetParam();
		}
		return -1 + "";
	}

	private void resetParam() {
		envelope = null;
		soapObject = null;
		result = null;
	}

}
