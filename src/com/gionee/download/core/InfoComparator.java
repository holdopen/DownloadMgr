package com.gionee.download.core;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class InfoComparator implements Comparator<DownloadInfo> {

	@Override
	public int compare(DownloadInfo lhs, DownloadInfo rhs) {
		return (int) (lhs.getSucceedTime() - rhs.getSucceedTime());
	}

	public static void test(List<DownloadInfo> list){
		Collections.sort(list, new InfoComparator());
		Collections.reverse(list);
	}
}
