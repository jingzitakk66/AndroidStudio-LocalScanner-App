package com.hivemind.zerodavila.localscanner.activity.network;

import com.hivemind.zerodavila.localscanner.activity.async.ScanHostsAsyncTask;
import com.hivemind.zerodavila.localscanner.activity.response.MainAsyncResponse;

public class Discovery {

    //Starts the host scanning
    public void scanHosts(String ip, MainAsyncResponse delegate) {
        new ScanHostsAsyncTask(delegate).execute(ip);
    }
}
