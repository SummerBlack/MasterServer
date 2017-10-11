package com.lamost.datainfo;
/**
 *情景模式下电器的实体类
 * @author C.Feng
 * @version 
 * @date 2017-5-3 下午8:40:56
 * 
 */
public class SceneElectric {
	private int id;
	private int scene_index;
	private String electric_code;
	private String electric_order;
	private String order_info;

	public SceneElectric() {
		// TODO Auto-generated constructor stub
	}

	public SceneElectric(int id, int scene_index, String electric_code,
			String electric_order, String order_info) {
		super();
		this.id = id;
		this.scene_index = scene_index;
		this.electric_code = electric_code;
		this.electric_order = electric_order;
		this.order_info = order_info;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getScene_index() {
		return scene_index;
	}

	public void setScene_index(int scene_index) {
		this.scene_index = scene_index;
	}

	public String getElectric_code() {
		return electric_code;
	}

	public void setElectric_code(String electric_code) {
		this.electric_code = electric_code;
	}

	public String getElectric_order() {
		return electric_order;
	}

	public void setElectric_order(String electric_order) {
		this.electric_order = electric_order;
	}

	public String getOrder_info() {
		return order_info;
	}

	public void setOrder_info(String order_info) {
		this.order_info = order_info;
	}

	@Override
	public String toString() {
		return "SceneElectric [id=" + id + ", scene_index=" + scene_index
				+ ", electric_code=" + electric_code + ", electric_order="
				+ electric_order + ", order_info=" + order_info + "]";
	}
	
	

}
