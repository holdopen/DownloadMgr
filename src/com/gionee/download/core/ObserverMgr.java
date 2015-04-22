package com.gionee.download.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.util.Log;

import com.gionee.download.manager.DownloadMgr.Setting;
import com.gionee.download.manager.IDownloadObserver;

public class ObserverMgr extends DelaySyncExecutor implements
        IOnControllerDestroy {

    private static final int SPCE_TIME = 80;

    public static final int TYPE_PROGRESS = 1 << 0;
    public static final int TYPE_STATUS = 1 << 1;
    public static final int TYPE_DELETE = 1 << 2;

    private static final String TAG = "ObserverMgr";

    private List<IDownloadObserver> mObservers;
    private Map<DownloadInfo, Integer> mChangeRecord;

    public ObserverMgr() {
        mObservers = new ArrayList<IDownloadObserver>();
        mChangeRecord = new HashMap<DownloadInfo, Integer>();
    }

    public void registerObserver(IDownloadObserver observer) {
        if (mObservers.contains(observer)) {
            return;
        }

        mObservers.add(observer);
    }

    public void unregisterObserver(IDownloadObserver observer) {
        mObservers.remove(observer);
    }

    public void postChanged(DownloadInfo info, int type) {
        if (null == info) {
            return;
        }
        
        try {
            if (mObservers.size() == 0) {
                return;
            }

            if (mChangeRecord.size() == 0) {
                setDelayed(Setting.getObserverSpaceTime());
            }

            setType(info, type);

            if (TYPE_PROGRESS != type) {
                setDelayed(SPCE_TIME);
            }
        } catch (Exception e) {
            Log.d(TAG, "mChangeRecord=" + mChangeRecord + "  info=" + info);
        }
    }

    private void setType(DownloadInfo info, int type) {
        int oldType = 0;
        if (mChangeRecord.containsKey(info) && null != mChangeRecord.get(info)) {
            oldType = mChangeRecord.get(info);
        }

        if (oldType == TYPE_DELETE) {
            return;
        }

        if (type == TYPE_DELETE) {
            mChangeRecord.put(info, type);
        } else {
            mChangeRecord.put(info, oldType | type);
        }
    }

    @Override
    protected void onExecute() {
        if (mChangeRecord.size() == 0) {
            return;
        }

        Map<DownloadInfo, Integer> changeRecord = mChangeRecord;
        mChangeRecord = new HashMap<DownloadInfo, Integer>();

        notifyChange(changeRecord);
    }

    private void notifyChange(Map<DownloadInfo, Integer> changeRecord) {
        List<DownloadInfo> progressChanged = new ArrayList<DownloadInfo>();
        List<DownloadInfo> statusChanged = new ArrayList<DownloadInfo>();
        List<DownloadInfo> deleteChanged = new ArrayList<DownloadInfo>();

        Set<Entry<DownloadInfo, Integer>> changed = changeRecord.entrySet();
        for (Entry<DownloadInfo, Integer> entry : changed) {
            int type = entry.getValue();
            DownloadInfo info = entry.getKey();
            if ((type & TYPE_PROGRESS) == TYPE_PROGRESS) {
                progressChanged.add(info);
            }

            if ((type & TYPE_STATUS) == TYPE_STATUS) {
                statusChanged.add(info);
            }

            if ((type & TYPE_DELETE) == TYPE_DELETE) {
                deleteChanged.add(info);
            }
        }

        if (progressChanged.size() > 0) {
            onProgressChanged(progressChanged);
        }

        if (statusChanged.size() > 0) {
            onStatusChanged(statusChanged);
        }

        if (deleteChanged.size() > 0) {
            onDeleted(deleteChanged);
        }
    }

    private void onProgressChanged(List<DownloadInfo> changedInfos) {
        for (int i = 0; i < mObservers.size(); i++) {
            mObservers.get(i).onProgressChange(changedInfos);
        }
    }

    private void onStatusChanged(List<DownloadInfo> changedInfos) {
        for (int i = 0; i < mObservers.size(); i++) {
            mObservers.get(i).onStatusChange(changedInfos);
        }
    }

    private void onDeleted(List<DownloadInfo> deletedInfos) {
        for (int i = 0; i < mObservers.size(); i++) {
            mObservers.get(i).onDelete(deletedInfos);
        }
    }

    @Override
    public void onDestroy() {
        cancelTask();
    }
}
