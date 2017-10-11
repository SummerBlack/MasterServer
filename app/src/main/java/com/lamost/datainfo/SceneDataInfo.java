package com.lamost.datainfo;

import java.util.ArrayList;

/**
 * @author C.Feng
 * @version 
 * @date 2017-4-27 上午8:36:54
 * 
 */
public class SceneDataInfo {
	
	 //private String accountCode;
     //private String masterCode;
    //情景模式的名称，如回家、离家...
	private String sceneName;
	//情景模式的编号，如回家对应0，离家对应1...
	private String sceneIndex;
	//情景模式下对应的控制指令集
	private ArrayList<String> sceneOrderInfos;
	 

	public SceneDataInfo() {
		// TODO Auto-generated constructor stub
	}
	

	public String getSceneName() {
		return sceneName;
	}

	public void setSceneName(String sceneName) {
		this.sceneName = sceneName;
	}

	public String getSceneIndex() {
		return sceneIndex;
	}

	public void setSceneIndex(String sceneIndex) {
		this.sceneIndex = sceneIndex;
	}

	public ArrayList<String> getSceneOrderInfos() {
		return sceneOrderInfos;
	}

	public void setSceneOrderInfos(ArrayList<String> sceneOrderInfos) {
		this.sceneOrderInfos = sceneOrderInfos;
	}


	@Override
	public String toString() {
		return "SceneDataInfo [sceneName=" + sceneName + ", sceneIndex="
				+ sceneIndex + ", sceneOrderInfos=" + sceneOrderInfos + "]";
	}

	
	

	
}
