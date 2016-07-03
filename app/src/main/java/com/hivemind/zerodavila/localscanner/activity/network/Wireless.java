package com.hivemind.zerodavila.localscanner.activity.network;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.Enumeration;

public class Wireless {

    private Activity activity;

//------------------- Constructor to set the activity for context ------------------------
    public Wireless(Activity activity) {
        this.activity = activity;
    }

//------------------------ Gets the MAC address of the device ----------------------------
    public String getMacAddress() {
        String address = this.getWifiInfo().getMacAddress(); //Won't work on Android 6+ https://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-hardware-id
        if (!"02:00:00:00:00:00".equals(address)) {
            return address;
        }

        //This should get us the device's MAC address on Android 6+
        try {
            NetworkInterface iface = NetworkInterface.getByInetAddress(this.getWifiInetAddress());
            if (iface == null) {
                return "Unknown";
            }

            byte[] mac = iface.getHardwareAddress();
            if (mac == null) {
                return "Unknown";
            }

            StringBuilder buf = new StringBuilder();
            for (byte aMac : mac) {
                buf.append(String.format("%02x:", aMac));
            }

            if (buf.length() > 0) {
                buf.deleteCharAt(buf.length() - 1);
            }

            return buf.toString();
        } catch (SocketException ex) {
            return "Unknown";
        }
    }

//-------------------- Wireless address --------------------------------
     private InetAddress getWifiInetAddress() {
        String ipAddress = this.getInternalWifiIpAddress();
        try {
            return InetAddress.getByName(ipAddress);
        } catch (UnknownHostException e) {
            return null;
        }
    }

//---------------------- Signal strength --------------------------------
    public int getSignalStrength() {
        return this.getWifiInfo().getRssi();
    }

//---------------- Local WiFi network LAN IP address ----------------------
    public String getInternalWifiIpAddress() {
        int ip = this.getWifiInfo().getIpAddress();
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ip = Integer.reverseBytes(ip);
        }
        byte[] ipByteArray = BigInteger.valueOf(ip).toByteArray();
        try {
            return InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            return null;
        }
    }

//-------------- Local cellular network LAN IP address -----------------
    public String getInternalMobileIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            return "Unknown";
        }
        return "Unknown";
    }

//-------------------- Wireless link speed --------------------
    public int getLinkSpeed() {
        return this.getWifiInfo().getLinkSpeed();
    }

//---- Determines if the device is connected to a WiFi network or not ----
    public boolean isConnectedWifi() {
        NetworkInfo info = this.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return info != null && info.isConnected();
    }

//-------------------------- WifiManager -----------------------
    public WifiManager getWifiManager() {
        return (WifiManager) this.activity.getSystemService(Context.WIFI_SERVICE);
    }

//------------------------ WiFi information --------------------
    private WifiInfo getWifiInfo() {
        return this.getWifiManager().getConnectionInfo();
    }

//---------------------- Connectivity manager -------------------
     private ConnectivityManager getConnectivityManager() {
        return (ConnectivityManager) this.activity.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

//----------------------  Network information --------------------
    private NetworkInfo getNetworkInfo(int type) {
        return this.getConnectivityManager().getNetworkInfo(type);
    }

}
