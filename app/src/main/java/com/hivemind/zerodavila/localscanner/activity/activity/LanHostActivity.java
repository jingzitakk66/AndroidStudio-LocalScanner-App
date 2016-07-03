package com.hivemind.zerodavila.localscanner.activity.activity;

import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.hivemind.zerodavila.localscanner.activity.network.Wireless;

public class LanHostActivity extends HostActivity {
    private Wireless wifi;
    private String hostName;
    private String hostIp;
    private String hostMac;

    /**
     * Activity created
     *
     * @param savedInstanceState Data from a saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.hostName = savedInstanceState.getString("hostName");
            this.hostIp = savedInstanceState.getString("hostIp");
            this.hostMac = savedInstanceState.getString("hostMac");
            //ports = savedInstanceState.getStringArrayList("ports");
        } else if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                return;
            }

            this.hostName = extras.getString("HOSTNAME");
            this.hostIp = extras.getString("IP");
            this.hostMac = extras.getString("MAC");
        }

        this.adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, ports);
        this.portList.setAdapter(adapter);
        this.wifi = new Wireless(this);
/*
        hostIpLabel.setText(this.hostName);
        hostMacLabel.setText(this.hostMac);

*/
    }

    /**
     * Save the state of the activity
     *
     * @param savedState Data to save
     */
    @Override
    public void onSaveInstanceState(Bundle savedState) {
        super.onSaveInstanceState(savedState);

        savedState.putString("hostName", this.hostName);
        savedState.putString("hostIp", this.hostIp);
        savedState.putString("hostMac", this.hostMac);
    }


    /**
     * Delegate to determine if the progress dialog should be dismissed or not
     *
     * @param output True if the dialog should be dismissed
     */
    @Override
    public void processFinish(boolean output) {
        if (output && this.scanProgressDialog != null && this.scanProgressDialog.isShowing()) {
            this.scanProgressDialog.dismiss();
            this.scanProgress = 0;
        }
        if (output && this.portRangeDialog != null && this.portRangeDialog.isShowing()) {
            this.portRangeDialog.dismiss();
        }
    }
}
