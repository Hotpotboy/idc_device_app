package com.zhanghang.idcdevice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.ViewGroup;

import com.zhanghang.idcdevice.adbsocket.AdbSocketService;
import com.zhanghang.self.utils.PopupWindowUtils;

/**
 * Created by hangzhang209526 on 2016/5/20.
 */
public class USBBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent.getAction().equals("android.hardware.usb.action.USB_STATE")) {
            if (intent.getExtras().getBoolean("connected")) {
                // usb 插入
                if(!AdbSocketService.isConnectionPc()){
                    Intent showDilagActivityIntent = new Intent(context, DialogActivity.class);
                    showDilagActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    showDilagActivityIntent.putExtra(Const.INTENT_KEY_DIALOG_ACTIVITY_SHOW,"插入USB，正与PC助手通信，请稍候...");
                    context.startActivity(showDilagActivityIntent);
                    AsyncTask<Void,Void,Void> task = new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            while (!AdbSocketService.isConnectionPc()){
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void result){
                            Intent dismissDilagActivityIntent = new Intent(context, DialogActivity.class);
                            dismissDilagActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            context.startActivity(dismissDilagActivityIntent);
                        }
                    };
                    task.execute();
                }
            }
        }
    }
}
