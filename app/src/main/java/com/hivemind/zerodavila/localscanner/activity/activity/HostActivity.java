package com.hivemind.zerodavila.localscanner.activity.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.hivemind.zerodavila.localscanner.activity.response.LanHostAsyncResponse;

import java.util.ArrayList;
import java.util.Map;

public abstract class HostActivity extends AppCompatActivity implements LanHostAsyncResponse {
    protected ArrayAdapter<String> adapter;
    protected ListView portList;
    protected ArrayList<String> ports = new ArrayList<>();
    protected ProgressDialog scanProgressDialog;
    protected Dialog portRangeDialog;
    protected int scanProgress;

    @Override
    public void processFinish(final int output) {
    }

    @Override
    public void processFinish(Map<Integer, String> output) {
    }
}
