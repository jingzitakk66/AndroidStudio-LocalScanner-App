package com.hivemind.zerodavila.localscanner.activity.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hivemind.zerodavila.localscanner.R;
import com.hivemind.zerodavila.localscanner.activity.network.Discovery;
import com.hivemind.zerodavila.localscanner.activity.network.Wireless;
import com.hivemind.zerodavila.localscanner.activity.response.MainAsyncResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity implements MainAsyncResponse {

    private Wireless wifi;
    private Discovery discovery = new Discovery();
    private ListView hostList;
    private ProgressDialog scanProgressDialog;
    private Handler mHandler = new Handler();
    private BroadcastReceiver receiver;
    private IntentFilter intentFilter = new IntentFilter();
    private ArrayAdapter hostsAdapter;
    private List<Map<String, String>> hosts = new ArrayList<>();

    //savedInstanceState Data from a saved state
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.hostList = (ListView) findViewById(R.id.hostList);
        this.wifi = new Wireless(this);
        this.setupHostsAdapter();
        this.setupReceivers();
        this.setupHostDiscovery();
    }

    private void setupHostsAdapter() {
        this.hostsAdapter = new ArrayAdapter<Map<String, String>>(this, android.R.layout.simple_list_item_2, android.R.id.text1, this.hosts) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                text2.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.grey));
                text1.setText(hosts.get(position).get("First Line"));
                text2.setText(hosts.get(position).get("Second Line"));
                return view;
            }
        };
        this.hostList.setAdapter(this.hostsAdapter);
    }

    private void setupHostDiscovery() {
        Button discoverHosts = (Button) findViewById(R.id.discoverHosts);
        discoverHosts.setOnClickListener(new View.OnClickListener() {

            //Click handler to perform host discovery
            @Override
            public void onClick(View v) {
                if (!wifi.isConnectedWifi()) {
                    Toast.makeText(getApplicationContext(), "You're not connected to a WiFi network!", Toast.LENGTH_SHORT).show();
                    return;
                }

                hosts.clear();
                hostsAdapter.notifyDataSetChanged();
                scanProgressDialog = new ProgressDialog(MainActivity.this, R.style.DialogTheme);
                scanProgressDialog.setCancelable(false);
                scanProgressDialog.setTitle("Scanning For Hosts");
                scanProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                scanProgressDialog.setProgress(0);
                scanProgressDialog.setMax(255);
                scanProgressDialog.show();
                discovery.scanHosts(wifi.getInternalWifiIpAddress(), MainActivity.this);
            }
        });
    }

    private void setupReceivers() {
        this.receiver = new BroadcastReceiver() {

            //Detect if a network connection has been lost
            @Override
            public void onReceive(Context context, Intent intent) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info == null) {
                    mHandler.removeCallbacksAndMessages(null);
                }
            }
        };
        this.intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(receiver, this.intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (this.scanProgressDialog != null && this.scanProgressDialog.isShowing()) {
            this.scanProgressDialog.dismiss();
        }
        this.scanProgressDialog = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (this.receiver != null) {
            unregisterReceiver(this.receiver);
        }
    }

    @Override
    public void onRestart() {
        super.onRestart();
        registerReceiver(this.receiver, this.intentFilter);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onRestoreInstanceState(Bundle savedState) {
        super.onRestoreInstanceState(savedState);

        this.hosts = (ArrayList<Map<String, String>>) savedState.getSerializable("hosts");
        if (this.hosts != null) {
            this.setupHostsAdapter();
        }
    }

    @Override
    public void processFinish(final Map<String, String> output) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                synchronized (hosts) {
                    if (!hosts.contains(output)) {
                        hosts.add(output);
                    } else {
                        hosts.set(hosts.indexOf(output), output);
                    }
                    Collections.sort(hosts, new Comparator<Map<String, String>>() {
                        @Override
                        public int compare(Map<String, String> lhs, Map<String, String> rhs) {
                            int left = Integer.parseInt(lhs.get("Second Line").substring(lhs.get("Second Line").lastIndexOf(".") + 1, lhs.get("Second Line").indexOf("[") - 1));
                            int right = Integer.parseInt(rhs.get("Second Line").substring(rhs.get("Second Line").lastIndexOf(".") + 1, rhs.get("Second Line").indexOf("[") - 1));
                            return left - right;
                        }
                    });
                    hostsAdapter.notifyDataSetChanged();
                }
                 if (scanProgressDialog != null && scanProgressDialog.isShowing()) {
                    scanProgressDialog.dismiss();
                }
            }
        });
    }

    @Override
    public void processFinish(int output) {
        if (this.scanProgressDialog != null && this.scanProgressDialog.isShowing()) {
            this.scanProgressDialog.incrementProgressBy(output);
        }
    }
}
