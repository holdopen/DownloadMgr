package com.gionee.download.manager;

import java.io.File;

import com.gionee.download.core.DownloadInfo;

public interface IFileVerify {
    
    boolean onVerify(DownloadInfo info, File file);
}
