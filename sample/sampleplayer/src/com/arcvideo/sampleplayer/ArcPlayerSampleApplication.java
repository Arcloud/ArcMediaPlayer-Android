package com.arcvideo.sampleplayer;

import java.io.File;
import java.util.ArrayList;

import com.arcvideo.MediaPlayer.ModuleManager;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.util.Log;

public class ArcPlayerSampleApplication extends Application {

	private static String TAG = "ArcPlayerSampleApplication";

	private static String _strFileDir = null;

	private static String _strLibsDir = null;
	
	private static String _strArm64LibsDir = null;
	
	private static boolean _isArm64 = false;

	private static boolean _bOuputLog = true;

	private static void outputLog(String strTAG, String strInfo) {
		if (_bOuputLog)
			Log.i(strTAG, strInfo);
	}

	/**
	 * 获取当前APP安装路径
	 */
	private String getCurAppDir() {
		ApplicationInfo applicationInfo = getApplicationInfo();
		outputLog(TAG, "cur data dir:" + this.getApplicationInfo().dataDir);
		outputLog(TAG, "cur file dir:"
				+ getBaseContext().getFilesDir().getAbsolutePath());

		_strLibsDir = applicationInfo.dataDir;
		_strFileDir = getBaseContext().getFilesDir().getAbsolutePath();
		
		_strArm64LibsDir = applicationInfo.nativeLibraryDir;
		_isArm64 = _strArm64LibsDir.endsWith("arm64");
		_strArm64LibsDir += "/";

		if (!_strLibsDir.endsWith("/")) {
			_strLibsDir = _strLibsDir + "/";
		}
		_strLibsDir = _strLibsDir + "lib/";
		outputLog(TAG, "cur libs dir:" + _strLibsDir);

		if (!_strFileDir.endsWith("/"))
			_strFileDir = _strFileDir + "/";

		if (!_strFileDir.endsWith("/"))
			_strFileDir = _strFileDir + "/";

		outputLog(TAG, "cur file dir:" + _strFileDir);
		return this.getApplicationInfo().dataDir;
	}

	private void copyPlayerIni() {
		getCurAppDir();

		ArrayList<Integer> codecList = new ArrayList<Integer>();
		//WARNING: DO NOT USE CODEC_SUBTYPE_ALL if you expect smaller config file size
		codecList.add(ModuleManager.CODEC_SUBTYPE_ALL);

        ArrayList<Integer> parserList = new ArrayList<Integer>();
		parserList.add(ModuleManager.FILE_PARSER_SUBTYPE_ALL);

		ModuleManager mgr = new ModuleManager(null, codecList, parserList);
		ArrayList<String> modList = mgr.QueryRequiredModules();
		outputLog(TAG, "module list(" + modList.size() + ": " + modList);
		
		File dirFile = new File(_strFileDir);
		if (!dirFile.exists()) {
			if (!dirFile.mkdirs()) {
				return;
			}
		}
		
		

		if(_isArm64)
		{
			mgr.GenerateConfigFile(_strArm64LibsDir, _strFileDir+"MV3Plugin.ini");
		}
		else
		{
			mgr.GenerateConfigFile(_strLibsDir, _strFileDir+"MV3Plugin.ini");
		}
		
		//String amr64Dir = "/data/app/com.arcvideo.sampleplayer-1/lib/arm64/";
		//mgr.GenerateConfigFile(amr64Dir, _strFileDir+"MV3Plugin.ini");
		//mgr.GenerateConfigFile(_strLibsDir, _strFileDir+"MV3Plugin.ini");
	}

	
	
	
	private static void LoadLibrarayArm64() {
		try {
			outputLog(TAG, "LoadLibraray : load libmv3_platform.so");
		
			System.loadLibrary("mv3_platform");
		} catch (java.lang.UnsatisfiedLinkError ex) {
			Log.d(TAG, "load Arm64 libmv3_platform.so failed," + ex.getMessage());
		}
		
		try {
			outputLog(TAG, "LoadLibraray : load libmv3_common.so");
			//System.load(_strLibsDir + "libmv3_common.so");
			System.loadLibrary("mv3_common");
		} catch (java.lang.UnsatisfiedLinkError ex) {
			Log.d(TAG, "load libmv3_common.so failed," + ex.getMessage());
		}
		
		try {
			outputLog(TAG, "LoadLibraray : load libmv3_mpplat.so");
			//System.load(_strLibsDir + "libmv3_mpplat.so");
			System.loadLibrary("mv3_mpplat");
		} catch (java.lang.UnsatisfiedLinkError ex) {
			Log.d(TAG, "load libmv3_mpplat.so failed," + ex.getMessage());
		}
		
		try {
			outputLog(TAG, "LoadLibraray : load libmv3_playerbase.so");
			//System.load(_strLibsDir + "libmv3_playerbase.so");
			System.loadLibrary("mv3_playerbase");
		} catch (java.lang.UnsatisfiedLinkError ex) {
			Log.d(TAG, "load libmv3_playerbase.so failed," + ex.getMessage());
		}
		
		try {
			Log.d(TAG, "LoadLibraray : load libmv3_jni.so");
			//System.load(_strLibsDir + "libmv3_jni.so");
			System.loadLibrary("mv3_jni");
		} catch (java.lang.UnsatisfiedLinkError ex) {
			Log.d(TAG, "load libmv3_jni.so failed, " + ex.getMessage());
		}
	}
	
	
	
	
	/**
	 * 该函数主要用来加载程序运行所需要的共享库
	 */
	private static void LoadLibraray() {
		try {
			outputLog(TAG, "LoadLibraray : load libmv3_platform.so");
			
			System.load(_strLibsDir + "libmv3_platform.so");
			
		} catch (java.lang.UnsatisfiedLinkError ex) {
			Log.d(TAG, "load libmv3_platform.so failed," + ex.getMessage());
		}
		
		try {
			outputLog(TAG, "LoadLibraray : load libmv3_common.so");
			System.load(_strLibsDir + "libmv3_common.so");
		} catch (java.lang.UnsatisfiedLinkError ex) {
			Log.d(TAG, "load libmv3_common.so failed," + ex.getMessage());
		}
		
		try {
			outputLog(TAG, "LoadLibraray : load libmv3_mpplat.so");
			System.load(_strLibsDir + "libmv3_mpplat.so");
		} catch (java.lang.UnsatisfiedLinkError ex) {
			Log.d(TAG, "load libmv3_mpplat.so failed," + ex.getMessage());
		}
		
		try {
			outputLog(TAG, "LoadLibraray : load libmv3_playerbase.so");
			System.load(_strLibsDir + "libmv3_playerbase.so");
		} catch (java.lang.UnsatisfiedLinkError ex) {
			Log.d(TAG, "load libmv3_playerbase.so failed," + ex.getMessage());
		}
		

		

		

		
		int apiVersion = android.os.Build.VERSION.SDK_INT;
		if (apiVersion >= 14) {
			try {
				Log.d(TAG, "LoadLibraray : load libmv3_jni_4.0.so");
				System.load(_strLibsDir + "libmv3_jni_4.0.so");
			} catch (java.lang.UnsatisfiedLinkError ex) {
				Log.d(TAG, "load libmv3_jni_4.0.so failed, " + ex.getMessage());
				}
		}else {
			try {
				Log.d(TAG, "LoadLibraray : load libmv3_jni.so");
				System.load(_strLibsDir + "libmv3_jni.so");
			} catch (java.lang.UnsatisfiedLinkError ex) {
				Log.d(TAG, "load libmv3_jni.so failed, " + ex.getMessage());
			}
		}
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		outputLog(TAG, "ArcPlayerApplication");
		copyPlayerIni();
		//LoadLibrarayArm64();
		if(_isArm64)
		{
			LoadLibrarayArm64();
		}
		else
		{
			LoadLibraray();
		}
		
		super.onCreate();

	}

}
