package com.milo.widget;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.SQLException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

/**
 * @author t00297405
 *
 */
public class NetUtil {
	private static final int WIFI = 1;
	private static final int CMWAP = 2;
	private static final int CMNET = 3;
	private static final String TAG = "NetUtil";
	/**
	
	     * @author sky
	
	     * Email milotian1988@gmail.com
	
	     * 获取当前的网络状态  -1：没有网络  1：WIFI网络2：wap网络3：net网络
	
	     * @param context
	
	     * @return
	
	     */ 
	
	    public static String getAPNType(Context context){ 
	
	        String netType = "NoNet";  
	
	        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE); 
	
	        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo(); 
	
	         
	
	        if(networkInfo==null){ 
	
	            return netType; 
	
	        } 
	
	        int nType = networkInfo.getType(); 
	
	        if(nType==ConnectivityManager.TYPE_MOBILE){ 
	
	            Log.e("networkInfo.getExtraInfo()", "networkInfo.getExtraInfo() is "+networkInfo.getExtraInfo()); 
	
	            if(networkInfo.getExtraInfo().toLowerCase().endsWith("net")){ 
	            	//移动cmnet
	                netType = "Net_APN"; 
	            } 
	            else{ 
	                netType = "Wap_APN"; 
	            } 
	
	        } 
	
	        else if(nType==ConnectivityManager.TYPE_WIFI){ 
	
	            netType = "Wifi"; 
	
	        } 
	
	        return netType; 
	
	    }
	    
	    
	    
	    
	/**
	 * 切换wifi开关
	 * @param context
	 */
	public static void changeWifi(Context context) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		if (wifiManager.isWifiEnabled()) {
			wifiManager.setWifiEnabled(false);
		} else {
			wifiManager.setWifiEnabled(true);
		}
	}
	
	
	
	
	
    /**
     * 当开启移动网络时调用setMobileDataStatus(context,true)，
     * 关闭调用setMobileDataStatus(context,false)
     * 移动数据开启和关闭
     * @param context
     * @param enabled
     */
    public static void setMobileDataStatus(Context context,boolean enabled)
    {
    ConnectivityManager conMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

    //ConnectivityManager类

    Class<?> conMgrClass = null;

      //ConnectivityManager类中的字段
      Field iConMgrField = null;
      //IConnectivityManager类的引用
      Object iConMgr = null;
      //IConnectivityManager类
      Class<?> iConMgrClass = null;
      //setMobileDataEnabled方法
       Method setMobileDataEnabledMethod = null;
    try
      {

       //取得ConnectivityManager类
       conMgrClass = Class.forName(conMgr.getClass().getName());
       //取得ConnectivityManager类中的对象Mservice
       iConMgrField = conMgrClass.getDeclaredField("mService");
       //设置mService可访问
       iConMgrField.setAccessible(true);
       //取得mService的实例化类IConnectivityManager
       iConMgr = iConMgrField.get(conMgr);
       //取得IConnectivityManager类
    iConMgrClass = Class.forName(iConMgr.getClass().getName());

       //取得IConnectivityManager类中的setMobileDataEnabled(boolean)方法
    setMobileDataEnabledMethod = iConMgrClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);

       //设置setMobileDataEnabled方法是否可访问   
       setMobileDataEnabledMethod.setAccessible(true);
          //调用setMobileDataEnabled方法
          setMobileDataEnabledMethod.invoke(iConMgr, enabled);

    }

    catch(Exception e)
      {

    e.printStackTrace();
      }

    }


	public static void setMobileDataState(Context cxt, boolean mobileDataEnabled) {
		TelephonyManager telephonyService = (TelephonyManager) cxt.getSystemService(Context.TELEPHONY_SERVICE);
		try {
			Method setMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("setDataEnabled", boolean.class);
			if (null != setMobileDataEnabledMethod) {
				setMobileDataEnabledMethod.invoke(telephonyService,mobileDataEnabled);
			}
		} catch (Exception e) {
			Log.v(TAG,"Error setting"+ ((InvocationTargetException) e).getTargetException() + telephonyService);
		}
	}

	public static boolean getMobileDataState(Context cxt) {
		TelephonyManager telephonyService = (TelephonyManager) cxt.getSystemService(Context.TELEPHONY_SERVICE);
		try {
			Method getMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("getDataEnabled");
			if (null != getMobileDataEnabledMethod) {
				boolean mobileDataEnabled = (Boolean) getMobileDataEnabledMethod.invoke(telephonyService);
				return mobileDataEnabled;
			}
		} catch (Exception e) {
			Log.v(TAG,"Error getting"+ ((InvocationTargetException) e).getTargetException() + telephonyService);
		}
		return false;
	}
    
    
    /**
     * 切换手机数据流量开关
     * @param context
     */
    public static void switchMobileData(Context context){
    	int version = Integer.valueOf(android.os.Build.VERSION.SDK);
//    	android5.0 以下
    	if(version < 21 ){
    		setMobileDataStatus(context,!getMobileDataState(context));
    	}else{
    		setMobileDataState(context,!getMobileDataState(context));
    	}	
    }
    
    
    
   
    // 当前的APN 
    public static final Uri PRE_APN_URI = Uri.parse("content://telephony/carriers/preferapn"); 
    //取得current=1的apn列表
    public static final Uri CURRENT_APN_URI = Uri.parse("content://telephony/carriers/current"); 
    // 所有的APN配配置信息位置
    public static final Uri APN_LIST_URI = Uri.parse("content://telephony/carriers"); 
    
    private static String[] projection = { "_id", "apn", "type", "current", "proxy", "port" };
    
    
    /**
     * 切换APN接入点
     * @param context
     * @return
     */
    public static boolean switchApn(Context context){
    	boolean result = false;
    	String currentApnName = getCurApnName(context);
    	if(currentApnName.endsWith("wap")){
    		result = updateCurrentAPN(context.getContentResolver(),"3gnet");
    	}else if(currentApnName.endsWith("net")){
    		result = updateCurrentAPN(context.getContentResolver(),"3gwap");
    	}else{
    		Toast.makeText(context, "get APN point ERROR！" + currentApnName, Toast.LENGTH_SHORT).show();
    	}
    	
    	return result;
    }
    
    
    
    public static String getCurrentAPNFromSetting(ContentResolver resolver) { 
        Cursor cursor = null; 
        try { 
            cursor = resolver.query(PRE_APN_URI, null, null, null, null); 
            while (cursor != null && cursor.moveToNext()) { 
                String curApnId = cursor.getString(cursor.getColumnIndex("_id")); 
                String curApnName = cursor.getString(cursor.getColumnIndex("apn")); 
            	Log.i(TAG, "Prefer: " + curApnId + " " + curApnName);
            } 
            cursor.close(); 
            //find apn name from apn list 
			cursor = resolver.query(APN_LIST_URI, null, null, null, null);
			while (cursor != null && cursor.moveToNext()) {
				String apnId = cursor.getString(cursor.getColumnIndex("_id"));
				String apnName = cursor.getString(cursor.getColumnIndex("apn"));
				Log.i(TAG, "All: " + apnId + " " + apnName);
			}
			
			cursor = resolver.query(CURRENT_APN_URI, null, null, null, null);
			while (cursor != null && cursor.moveToNext()) {
				String apnId = cursor.getString(cursor.getColumnIndex("_id"));
				String apnName = cursor.getString(cursor.getColumnIndex("apn"));
				Log.i(TAG, "Current: " + apnId + " " + apnName);
			}
            
        } catch (SQLException e) { 
            e.printStackTrace();
        } finally { 
            if (cursor != null) { 
                cursor.close(); 
            } 
        } 
        
        return null; 
} 
    
	// 获取当前APN
	public static String getCurApnName(Context context) {
		ContentResolver resoler = context.getContentResolver();
		// String[] projection = new String[] { "_id" };
		Cursor cur = resoler.query(PRE_APN_URI, projection, null, null, null);
		String apnName = "";
		// cur为空则表示默认情况下一个都没有选中
		if (cur != null && cur.moveToFirst()) {
			// apnId = cur.getString(cur.getColumnIndex("_id"));
			apnName = cur.getString(cur.getColumnIndex("apn"));
		}
		Log.i("xml", "getCurApnId:" + apnName);
		return apnName;
	}
    
    
    public static boolean updateCurrentAPN(ContentResolver resolver, String newAPN) { 
    	boolean changeResult = false;
        Cursor cursor = null; 
        try { 
            //get new apn id from list 
            cursor = resolver.query(APN_LIST_URI, null, " apn = ? and current = 1", new String[]{newAPN.toLowerCase()}, null); 
            String apnId = null; 
            if (cursor != null && cursor.moveToFirst()) { 
                apnId = cursor.getString(cursor.getColumnIndex("_id")); 
            } 
            cursor.close(); 
            
            //set new apn id as chosen one 
            if (apnId != null) { 
                ContentValues values = new ContentValues(); 
                values.put("apn_id", apnId); 
                resolver.update(PRE_APN_URI, values, null, null); 
                changeResult = true;
            } else { 
                //apn id not found, return 0. 
            } 
        } catch (Exception e) { 
        	e.printStackTrace();
        } finally { 
            if (cursor != null) { 
                cursor.close(); 
            } 
        } 
        
        return changeResult; 
} 
}

