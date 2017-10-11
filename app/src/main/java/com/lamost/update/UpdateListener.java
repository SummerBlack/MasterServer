package com.lamost.update;


/**
 * @author C.Feng
 * @version 
 * @date 2017-4-7 上午9:59:45
 * 
 */
public interface UpdateListener {
	/*void onResult(int errorcode, UpdateInfo result);*/
	void onResult(int resultCode, String result);
}
