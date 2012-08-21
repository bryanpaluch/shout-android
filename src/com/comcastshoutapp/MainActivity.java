package com.comcastshoutapp;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	private static final String TAG = "websocket-activity";
	final static String SEND_MSG = "SEND_MSG";
	boolean mIsBound;
	static final int MSG_SEND_TO_WSS = 1;
	Messenger mService = null;
	private String sessionId = "4000";
	static final int MODE_GET = 0;
	static final int MODE_UPLOAD = 1;
	static final int NO_MODE = 2;
	int mode = this.NO_MODE;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button upload = (Button)findViewById(R.id.upload_btn);
        upload.setOnClickListener(uploadListener);
        Button get = (Button)findViewById(R.id.get_btn);
        get.setOnClickListener(getListener);
        
        Log.i(TAG, "starting service from activity");
      //  startService(new Intent(MainActivity.this,WebsocketService.class));
        doBindService();
        
        
    }
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WebsocketService.MSG_SEND_TO_ACT:
                    onJSONEvent((String) msg.obj);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            mService = new Messenger(service);
            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                Message msg = Message.obtain(null,
                        WebsocketService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);

            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }

            // As part of the sample, tell the user what happened.
            Log.i(TAG,"service binded");
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            Log.i(TAG, "service disconnected");

            // As part of the sample, tell the user what happened.
          
        }
    };
    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        bindService(new Intent(this, 
                WebsocketService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
      
    }
    void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null,
                            WebsocketService.MSG_DEREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }
            }

            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
          Log.i(TAG,"service is unbinded");
        }
    }
    public void onJSONEvent(String jsonString){
    	try{
    	JSONObject jsonObj = new JSONObject(jsonString);
    	String action = jsonObj.getString("action");
    	Log.i(TAG,"action:" + action);
    	}catch (JSONException e){
    		Log.i(TAG,e.toString());
    		
    	}
    }
    @Override
    public void onResume() {
    
       
      super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    private OnClickListener uploadListener = new OnClickListener() {
        public void onClick(View v){
        	Log.i(TAG, "Upload Clicked");
        	
        }                
   };
   private OnClickListener getListener = new OnClickListener() {
       public void onClick(View v){
       	Log.i(TAG, "Clicked");
       	JSONObject obj = new JSONObject();
       	try {
			obj.put("action", "newGetSession");
		
       	obj.put("sessionId",  sessionId);
       	Message msg = Message.obtain(null,
                WebsocketService.MSG_SEND_TO_WSS, obj.toString());
       	mService.send(msg);
       	mode = MODE_GET;
       	} catch (JSONException e) {
		
			e.printStackTrace();
		}
       	catch (RemoteException e) {
		
			e.printStackTrace();
		}
       }                
  };
   @Override
   protected void onStop() {
    // TODO Auto-generated method stub

    Log.i(TAG,"Activity stop");
    super.onStop();
   }
   @Override
   protected void onDestroy() {
    // TODO Auto-generated method stub
 //   unregisterReceiver(eventReceiver);
    stopService(new Intent(MainActivity.this,WebsocketService.class));
    super.onDestroy();
   }
   

}
