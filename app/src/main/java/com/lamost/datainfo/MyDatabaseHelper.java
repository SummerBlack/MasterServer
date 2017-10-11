package com.lamost.datainfo;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库帮助类，用于创建一个数据库保存情景模式的电器指令等信息
 * @author C.Feng
 * @version 
 * @date 2017-5-3 下午5:02:13
 * 
 */
public class MyDatabaseHelper extends SQLiteOpenHelper {
	private final String CREATE_TABLE_SQL = "create table sceneelectric(" +
       "_id integer primary key autoincrement," +
       "scene_index int," +
       "electric_code varchar," +      
       "electric_order varchar," +       
       "order_info varchar" +
       ")";

	public MyDatabaseHelper(Context context, String name,
			 int version) {
		super(context, name, null, version);
	}

	public MyDatabaseHelper(Context context, String name,
			CursorFactory factory, int version,
			DatabaseErrorHandler errorHandler) {
		super(context, name, factory, version, errorHandler);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		//第一次使用数据库是自动建表
		db.execSQL(CREATE_TABLE_SQL);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// 当传入的数据库版本号大于数据库版本号时调用

	}

}
