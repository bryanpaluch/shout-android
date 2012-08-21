package com.comcastshoutapp;

import java.net.URI;
import java.net.URISyntaxException;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;



public class WebsocketService extends Service{
	 private static final String TAG = "websocket-service";
	 private WebSocketClient cc;
	 final static String WS_MSG = "WS_MSG";
	 final static int MSG_REGISTER_CLIENT = 0;
	 final static int MSG_DEREGISTER_CLIENT = 1;
	 static final int MSG_SEND_TO_WSS = 2;
	 static final int MSG_SEND_TO_ACT = 3;
	
	 Messenger client;
	 final Messenger mMessenger = new Messenger(new IncomingHandler());
     @Override
     public void onCreate() {
           super.onCreate();
            Log.i(TAG, "service created starting websocket conection");
       	try {
       	// cc = new ChatClient(new URI(uriField.getText()), area, ( Draft ) draft.getSelectedItem() );
       	cc = new WebSocketClient( new URI( "ws://10.255.132.175:8080/" ), (Draft) new Draft_17() ) {
       		

       	@Override
       	public void onMessage( String message ) {
       		sendToActivity(message);
       		Log.i(TAG, message);
       	}

       	@Override
       	public void onOpen( ServerHandshake handshake ) {
       		Log.i(TAG, "websocket connected");
       		
       		
       		
       	}

       	@Override
       	public void onClose( int code, String reason, boolean remote ) {
       		Log.i(TAG, "websocket disconnected");
       	}

       	@Override
       	public void onError( Exception ex ) {
       		Log.i(TAG, ex.toString());
       	}
       	};

       	Log.i(TAG, "trying to connect");
       	cc.connect();
       	} catch ( URISyntaxException ex ) {
       		Log.i(TAG,ex.toString());
       	}
     }
   
     private void sendToActivity(String message){
    	if(client != null){
    	 try {
			client.send(Message.obtain(null, WebsocketService.MSG_SEND_TO_ACT, message));
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}}else{
			Log.i(TAG,"No connected client");
		}
			
    	     	 
     }
      class IncomingHandler extends Handler {
         @Override
         public void handleMessage(Message msg) {
             switch (msg.what) {
             case MSG_REGISTER_CLIENT:
            	 client = msg.replyTo;
            	 Log.i(TAG, "client registered");
            	 break;
             case MSG_DEREGISTER_CLIENT:
            	 Log.i(TAG, "client deregistered");
            	 client = null;
            	 break;
             case MSG_SEND_TO_WSS:
                     cc.send((String) msg.obj);
                     break;
             default:
                     super.handleMessage(msg);
             }
         }
     }
     
     
     @Override
     public IBinder onBind(Intent arg0) {
    	 	
           return mMessenger.getBinder();
     }
     @Override
     public void onDestroy() {
           super.onDestroy();
           cc.close();
           Toast.makeText(this, "Service destroyed ...", Toast.LENGTH_LONG).show();
     }
}
