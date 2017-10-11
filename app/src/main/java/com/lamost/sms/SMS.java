package com.lamost.sms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * @author C.Feng
 * @version
 * @date 2017-6-18 下午4:45:35
 * 
 */
public class SMS {
	public static void sendMessageToUser(int templateid, String mobiles,
			String params) throws IOException {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		String url = "https://api.netease.im/sms/sendtemplate.action";
		HttpPost httpPost = new HttpPost(url);

		String appKey = "d8bb4e3a81bee2e130bd4bb08bc4b631";
		String appSecret = "b381b8603e9c";
		String nonce = "12345";
		String curTime = String.valueOf((new Date()).getTime() / 1000L);
		String checkSum = CheckSumBuilder
				.getCheckSum(appSecret, nonce, curTime);// 参考 计算CheckSum的java代码

		// 设置请求的header
		httpPost.addHeader("AppKey", appKey);
		httpPost.addHeader("Nonce", nonce);
		httpPost.addHeader("CurTime", curTime);
		httpPost.addHeader("CheckSum", checkSum);
		httpPost.addHeader("Content-Type",
				"application/x-www-form-urlencoded;charset=utf-8");
		// 设置请求的参数
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("templateid", templateid + ""));
		nvps.add(new BasicNameValuePair("mobiles", mobiles));
		// nvps.add(new BasicNameValuePair("mobiles", "['15555173209']"));
		nvps.add(new BasicNameValuePair("params", params));
		httpPost.setEntity(new UrlEncodedFormEntity(nvps, "utf-8"));
		// 执行请求
		HttpResponse response = httpClient.execute(httpPost);
		// 打印执行结果
		System.out.println(EntityUtils.toString(response.getEntity(), "utf-8"));
	}

}
