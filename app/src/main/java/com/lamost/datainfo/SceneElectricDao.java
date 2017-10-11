package com.lamost.datainfo;

import java.util.ArrayList;
import java.util.List;
import com.lamost.utils.LogHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 操作sceneelectric表的DAO类
 * @author C.Feng
 * @version 
 * @date 2017-5-3 下午8:46:36
 * 
 */
public class SceneElectricDao {
	private MyDatabaseHelper databaseHelper;
	
	public SceneElectricDao(Context context) {
		//仅仅执行这句是不会创建表的，第一次执行getReadableDatabase的时候会创建
		databaseHelper = new MyDatabaseHelper(context, "sceneelectric.db3", 1);
	}
	/**
	 * 添加一条记录
	 * @param sceneElectric
	 */
	public void save(SceneElectric sceneElectric){
		//1.得到连接
		SQLiteDatabase database = databaseHelper.getReadableDatabase();
		//2.执行insert
		ContentValues values = new ContentValues();
		values.put("scene_index", sceneElectric.getScene_index());
		values.put("electric_code", sceneElectric.getElectric_code());
		values.put("electric_order", sceneElectric.getElectric_order());
		values.put("order_info", sceneElectric.getOrder_info());
		long id = database.insert("sceneelectric", null, values);
		LogHelper.d("执行添加操作 id=" + id);
		//3.关闭
		database.close();
	}
	/**
	 * 根据传入参数删除一条记录
	 * @param scene_index
	 * @param electric_code
	 * @param order_info
	 */
	public void delete(int scene_index, String electric_code,
			 String order_info){
		//1.得到连接
		SQLiteDatabase database = databaseHelper.getReadableDatabase();
		//2.执行delect
		//delete from sceneelectric where scene_index=scene_index electric_code=electric_code and order_info=order_info
		int delectcount = database.delete("sceneelectric", "scene_index=? and electric_code=? and order_info=?", 
				new String[]{scene_index+"",electric_code,order_info});
		LogHelper.d("执行删除操作返回值：" + delectcount);
		//3.关闭
		database.close();
	}
	/**
	 * 删除scene_index的所有记录
	 * @param scene_index
	 */
	public void delete(int scene_index){
		//1.得到连接
		SQLiteDatabase database = databaseHelper.getReadableDatabase();
		//2.执行delect
		//delete from sceneelectric where scene_index=scene_index electric_code=electric_code and order_info=order_info
		int delectcount = database.delete("sceneelectric", "scene_index=?", 
				new String[]{scene_index+""});
		LogHelper.d("执行删除操作返回值：" + delectcount);
		//3.关闭
		database.close();
	}
	/**
	 * 更新一条记录
	 * @param sceneElectric
	 */
	public void update(SceneElectric sceneElectric){
		//1.得到连接
		SQLiteDatabase database = databaseHelper.getReadableDatabase();
		//2.执行update操作
		ContentValues values = new ContentValues();
		values.put("scene_index", sceneElectric.getScene_index());
		values.put("electric_code", sceneElectric.getElectric_code());
		values.put("electric_order", sceneElectric.getElectric_order());
		values.put("order_info", sceneElectric.getOrder_info());
		database.update("sceneelectric", values, "_id=" + sceneElectric.getId(), null);
		LogHelper.d("执行更新操作");
		//3.关闭
		database.close();
	}
	/**
	 * 查询所有记录封装成List<>
	 * @return
	 */
	public List<SceneElectric> getAll(){
		List<SceneElectric> list = new ArrayList<>();
		//1.得到连接
		SQLiteDatabase database = databaseHelper.getReadableDatabase();
		//2.执行query select *from sceneelectric
		Cursor cursor = database.query("sceneelectric", null, null, null, null, null, null);
		while(cursor.moveToNext()){
			//id
			int id = cursor.getInt(0);
			//scene_index
			int scene_index = cursor.getInt(1);
			//electric_code
			String electric_code = cursor.getString(2);
			//electric_order
			String electric_order = cursor.getString(3);
			//order_info
			String order_info = cursor.getString(4);
			
			list.add(new SceneElectric(id, scene_index, electric_code, electric_order, order_info));
		}
		//3.关闭
		cursor.close();
		database.close();
		
		return list;
	}
	/**
	 * 根据传入的scene_index，查询相应记录
	 * @param scene_index 情景模式编号
	 * @return
	 */
	public List<SceneElectric> getByScene_index(int scene_index){
		
		List<SceneElectric> list = new ArrayList<>();
		//1.得到连接
		SQLiteDatabase database = databaseHelper.getReadableDatabase();
		//2.执行query select *from sceneelectric
		Cursor cursor = database.query("sceneelectric", null, "scene_index=" + scene_index, null, null, null, null);
		while(cursor.moveToNext()){
			//id
			int id = cursor.getInt(0);
			//scene_index
			int sceneindex = cursor.getInt(1);
			//electric_code
			String electric_code = cursor.getString(2);
			//electric_order
			String electric_order = cursor.getString(3);
			//order_info
			String order_info = cursor.getString(4);
			
			list.add(new SceneElectric(id, sceneindex, electric_code, electric_order, order_info));
		}
		//3.关闭
		cursor.close();
		database.close();
		
		return list;
	}
	/**
	 * 根据传入的以下参数进行查找
	 * 该方法用于检测该条信息在表中是否有记录，将来可以根据该结果，如果有就更新，如果没有就保存
	 * @param scene_index
	 * @param electric_code
	 * @param order_info
	 * @return
	 */
	public List<SceneElectric> getByParams(int scene_index, String electric_code,
			 String order_info){
		List<SceneElectric> list = new ArrayList<>();
		//1.得到连接
		SQLiteDatabase database = databaseHelper.getReadableDatabase();
		//2.执行query select *from sceneelectric where scene_index=scene_index
		Cursor cursor = database.query("sceneelectric", null, "scene_index=? and electric_code=? and order_info=?", 
				new String[]{scene_index+"",electric_code,order_info}, null, null, null);
		
		while(cursor.moveToNext()){
			//id
			int id = cursor.getInt(0);
			//scene_index
			int sceneindex = cursor.getInt(1);
			//electric_code
			String electriccode = cursor.getString(2);
			//electric_order
			String electricorder = cursor.getString(3);
			//order_info
			String orderinfo = cursor.getString(4);
			
			list.add(new SceneElectric(id, sceneindex, electriccode, electricorder, orderinfo));
		}
		//3.关闭
		cursor.close();
		database.close();
		
		return list;
		
	}

}
