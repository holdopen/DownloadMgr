package com.gionee.download.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import com.gionee.download.utils.StorageUtils;
import com.gionee.download.utils.Utils;

public class SystemStateHolder extends BroadcastReceiver implements IOnControllerDestroy {

    private MsgSender mMsgSender;
    private Context mContext;

    public SystemStateHolder(Context context, MsgSender msgSender) {
        this.mContext = context;
        this.mMsgSender = msgSender;
        register(context);
        
    }

    private void register(Context context) {
        IntentFilter filter4Net = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(this, filter4Net);
        
        IntentFilter filter4SdCard = new IntentFilter(Intent.ACTION_MEDIA_EJECT);
        filter4SdCard.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter4SdCard.addDataScheme("file");
        context.registerReceiver(this, filter4SdCard);
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            onNetStateChange();
            
        } else if (Intent.ACTION_MEDIA_EJECT.equals(action)) {
            mMsgSender.send(MsgSender.MSG_SYS_MEDIA_EJECT);
            
        } else if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
            mMsgSender.send(MsgSender.MSG_SYS_MEDIA_MOUNTED);
        }
    }
    
    private void onNetStateChange() {
        if (false == hasNetwork()) {
            mMsgSender.send(MsgSender.MSG_SYS_NO_NET);
            return;
        }
        
        if (isWifiNet()) {
            mMsgSender.send(MsgSender.MSG_SYS_WIFI_NET);
            return;
        }
        
        if (isMobileNet()) {
            mMsgSender.send(MsgSender.MSG_SYS_MOBLIE_NET);
        }
    }

    public boolean isSDCardMounted() {
        return StorageUtils.isSDCardMounted();
    }

    private boolean hasNetwork() {
        return Utils.hasNetwork(mContext);
    }

    private boolean isWifiNet() {
        return Utils.isWifiNet(mContext);
    }

    public boolean isMobileNet() {
        return Utils.isMobileNet(mContext);
    }

    
    public boolean isReady(boolean isAllowMobileNet) {
        return isNetReady(isAllowMobileNet) && isSDCardMounted();
    }

    public boolean isNetReady(boolean isAllowMobileNet) {
        return isWifiNet() || (isMobileNet() && isAllowMobileNet);
    }
    
    @Override
    public void onDestroy() {
        mContext.unregisterReceiver(this);
        mContext = null;
        mMsgSender = null;
    }
}
