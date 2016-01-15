package com.milo.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;  
import android.appwidget.AppWidgetProvider;  
import android.content.Context;  
import android.content.Intent;  
import android.os.Bundle;
import android.os.Vibrator;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.widget.RemoteViews;
import android.widget.Toast;
import android.util.Log;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;




public class ExampleAppWidgetProvider extends AppWidgetProvider {
	private static final String TAG = "ExampleAppWidgetProvider";

	private boolean DEBUG = false; 
	//网络状态发生变化
	public static final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    // 启动ExampleAppWidgetService服务对应的action
    private final Intent EXAMPLE_SERVICE_INTENT = new Intent("android.appwidget.action.EXAMPLE_APP_WIDGET_SERVICE");
    // 更新 widget 的广播对应的action
	private final String ACTION_UPDATE_ALL = "com.skywang.widget.UPDATE_ALL";
    // 保存 widget 的id的HashSet，每新建一个 widget 都会为该 widget 分配一个 id。
	private static Set idsSet = new HashSet();
	// 按钮信息
    private static final int BUTTON_WIFI = 1;
    private static final int BUTTON_MOBILE = 2;
    private static final int BUTTON_APN = 3;

    
	// onUpdate() 在更新 widget 时，被执行，
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.d(TAG, "onUpdate(): appWidgetIds.length="+appWidgetIds.length);

		// 每次 widget 被创建时，对应的将widget的id添加到set中
		for (int appWidgetId : appWidgetIds) {
			idsSet.add(Integer.valueOf(appWidgetId));
		}
		
		String netStatu = NetUtil.getAPNType(context);
//		String netStatu = "NetUtil";
		updateAllAppWidgets(context, AppWidgetManager.getInstance(context), idsSet, netStatu);
		prtSet();
	}
	
    // 当 widget 被初次添加 或者 当 widget 的大小被改变时，被调用 
    @Override  
    public void onAppWidgetOptionsChanged(Context context,  
            AppWidgetManager appWidgetManager, int appWidgetId,  
            Bundle newOptions) {
    	Log.d(TAG, "onAppWidgetOptionsChanged");
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId,  newOptions);  
    }  
    
    // widget被删除时调用  
    @Override  
    public void onDeleted(Context context, int[] appWidgetIds) {  
		Log.d(TAG, "onDeleted(): appWidgetIds.length="+appWidgetIds.length);

		// 当 widget 被删除时，对应的删除set中保存的widget的id
		for (int appWidgetId : appWidgetIds) {
			idsSet.remove(Integer.valueOf(appWidgetId));
		}
		prtSet();
		
        super.onDeleted(context, appWidgetIds);  
    }

    // 第一个widget被创建时调用  
    @Override  
    public void onEnabled(Context context) {  
    	Log.d(TAG, "onEnabled");
    	// 在第一个 widget 被创建时，开启服务
//    	context.startService(EXAMPLE_SERVICE_INTENT);
    	
        super.onEnabled(context);  
    }  
    
    // 最后一个widget被删除时调用  
    @Override  
    public void onDisabled(Context context) {  
    	Log.d(TAG, "onDisabled");

    	// 在最后一个 widget 被删除时，终止服务
//    	context.stopService(EXAMPLE_SERVICE_INTENT);

        super.onDisabled(context);  
    }
    
    
    // 接收广播的回调函数
    @Override  
    public void onReceive(Context context, Intent intent) {  

        final String action = intent.getAction();
        Log.d(TAG, "OnReceive:Action: " + action);
        if (ACTION_UPDATE_ALL.equals(action)) {
        	// “更新”广播
        	String NetStau = intent.getStringExtra("NetStau");
        	updateAllAppWidgets(context, AppWidgetManager.getInstance(context), idsSet, NetStau);
	    } else if (intent.hasCategory(Intent.CATEGORY_ALTERNATIVE)) {
	    	// “按钮点击”广播
	        Uri data = intent.getData();
	        int buttonId = Integer.parseInt(data.getSchemeSpecificPart());
	        if (buttonId == BUTTON_WIFI) {
//	        	Log.d(TAG, "Button wifi clicked");
//	        	Toast.makeText(context, "wifi Button Clicked", Toast.LENGTH_SHORT).show();
	        	NetUtil.changeWifi(context);
	        }else if(buttonId == BUTTON_MOBILE) {
//	        	Log.d(TAG, "Button mobile clicked");
//	        	Toast.makeText(context, "moblie Button Clicked", Toast.LENGTH_SHORT).show();
	        	NetUtil.switchMobileData(context);
	        	}
	        else if(buttonId == BUTTON_APN) {
		        	Log.d(TAG, "Button apn clicked");
//		        	Toast.makeText(context, "apn Button Clicked", Toast.LENGTH_SHORT).show();
		        	NetUtil.switchApn(context);
		        	NetUtil.getCurrentAPNFromSetting(context.getContentResolver());
		        }
	     }else if(action.equals(CONNECTIVITY_CHANGE_ACTION)){//网络变化的时候会发送通知
           //xml里面静态注册
	    	String Apnname = NetUtil.getAPNType(context);
            Toast.makeText(context, "NetWork Changed！ " + Apnname, Toast.LENGTH_SHORT).show();
            if(Apnname.toLowerCase().endsWith("net")){
            	//切换到NET接入点了，关闭网络，震动
                if(NetUtil.getMobileDataState(context)){
                    NetUtil.switchMobileData(context);
                }
            	Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                long [] pattern = {100,400,100,400};   // 停止 开启 停止 开启 
                vibrator.vibrate(pattern,3);           //重复两次上面的pattern 如果只想震动一次，index设为-1 
            }
            updateAllAppWidgets(context, AppWidgetManager.getInstance(context), idsSet, NetUtil.getAPNType(context));
        }
        
        super.onReceive(context, intent);  
    }  

    // 更新所有的 widget 
    private void updateAllAppWidgets(Context context, AppWidgetManager appWidgetManager, Set set,String netStue) {

		Log.d(TAG, "updateAllAppWidgets(): size="+set.size());
		
		// widget 的id
    	int appID;
    	// 迭代器，用于遍历所有保存的widget的id
    	Iterator it = set.iterator();

    	while (it.hasNext()) {
    		appID = ((Integer)it.next()).intValue();    
    		// 获取 example_appwidget.xml 对应的RemoteViews    		
    		RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.example_appwidget);
    		
//    		remoteView.sette
    		// 设置点击按钮对应的PendingIntent：即点击按钮时，发送广播。
    		remoteView.setOnClickPendingIntent(R.id.btn_wifi, getPendingIntent(context,BUTTON_WIFI));
    		remoteView.setOnClickPendingIntent(R.id.btn_mobile, getPendingIntent(context,BUTTON_MOBILE));
    		remoteView.setOnClickPendingIntent(R.id.btn_apn, getPendingIntent(context,BUTTON_APN));
    		
    		//更新wifi mobile apn状态
    		if(NetUtil.getMobileDataState(context)){
    			remoteView.setTextViewText(R.id.text_moblie, "ON");
    		}else{
    			remoteView.setTextViewText(R.id.text_moblie, "OFF");
    		}
    		
    		if(((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).isWifiEnabled()){
    			remoteView.setTextViewText(R.id.text_wifi, "ON");
    		}else{
    			remoteView.setTextViewText(R.id.text_wifi, "OFF");
    		}
    		
    		remoteView.setTextViewText(R.id.text_apn, NetUtil.getCurApnName(context));

    		// 更新 widget
    		appWidgetManager.updateAppWidget(appID, remoteView);		
    	}    	
	}

    private PendingIntent getPendingIntent(Context context, int buttonId) {
        Intent intent = new Intent();
        intent.setClass(context, ExampleAppWidgetProvider.class);
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        intent.setData(Uri.parse("custom:" + buttonId));
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0 );
        return pi;
    }

    // 调试用：遍历set
    private void prtSet() {
    	if (DEBUG) {
	    	int index = 0;
	    	int size = idsSet.size();
	    	Iterator it = idsSet.iterator();
	    	Log.d(TAG, "total:"+size);
	    	while (it.hasNext()) {
	    		Log.d(TAG, index + " -- " + ((Integer)it.next()).intValue());
	    	}
    	}
    }
}
