package com.anysou.as_receiptnotice;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import androidx.core.app.ActivityCompat;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static android.content.Context.TELEPHONY_SERVICE;

/**
 * 获取设备唯一标识完美解决方案  https://blog.csdn.net/appdevzhang/article/details/52636575
 * **/

public class AndroidUniqueID {

    private Context mContext;

    public AndroidUniqueID(Context context) {
        this.mContext = context;
    }

    /**
     * The IMEI: 仅仅只对Android手机有效
     * 采用此种方法，需要在 AndroidManifest.xml中加入一个许可：android.permission.READ_PHONE_STATE，
     * 并且用户应当允许安装此应用。作为手机来讲，IMEI是唯一的，它应该类似于 359881030314356
     * （除非你有一个没有量产的手机（水货）它可能有无效的IMEI，如：0000000000000）。
     * Build.getSerial()
     */
    public String getIMEI() {
        TelephonyManager TelephonyMgr = (TelephonyManager) mContext.getSystemService(TELEPHONY_SERVICE);
        // 检测是否获得 READ_PHONE_STATE 权限； 动态请求权限
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            //下面这句是动态获取权限
            //ActivityCompat.requestPermissions(this, new String[]  {Manifest.permission.READ_PHONE_STATE},1);
            /* 另位要写，动态申请READ_PHONE_STATE权限操作后的回调函数：
            @Override
            public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
                switch (requestCode) {
                    case REQUEST_READ_PHONE_STATE:
                        if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                            // 用户授权了
                        } else {
                            // 用户拒绝了
                        }
                        break;

                    default:
                        break;
                }
            }
            * */
            return "";
        } else {
            String szImei = TelephonyMgr.getDeviceId();
            return szImei;
        }
    }

    /**
     * Pseudo-Unique ID, 这个在任何 Android手机中都有效
     * 有一些特殊的情况，一些如平板电脑的设置没有通话功能，或者你不愿加入 READ_PHONE_STATE许可，就用这方法。
     * 这时你可以通过取出ROM版本、制造商、CPU型号、以及其他硬件信息来实现这一点。这样计算出来的ID不是唯一的（因为如果两个手机应用了同样的硬件以及Rom 镜像）。
     * 但应当明白的是，出现类似情况的可能性基本可以忽略。大多数的 Build成员都是字符串形式的，我们只取他们的长度信息。
     * 我们取到13个数字，并在前面加上“35”。这样这个ID看起来就和 15位IMEI一样了。
     */
    public String getPesudoUniqueID() {
        String m_szDevIDShort = "35" + //帮助我们看上去像是 IMEI
                Build.BOARD.length() % 10 +
                Build.BRAND.length() % 10 +
                Build.CPU_ABI.length() % 10 +
                Build.DEVICE.length() % 10 +
                Build.DISPLAY.length() % 10 +
                Build.HOST.length() % 10 +
                Build.ID.length() % 10 +
                Build.MANUFACTURER.length() % 10 +
                Build.MODEL.length() % 10 +
                Build.PRODUCT.length() % 10 +
                Build.TAGS.length() % 10 +
                Build.TYPE.length() % 10 +
                Build.USER.length() % 10; //13 digits
        return m_szDevIDShort;
    }

    /**
     * The Android ID
     * 通常被认为不可信，因为它有时为null。开发文档中说明了：这个ID会改变如果进行了出厂设置。
     * 并且，如果某个Andorid手机被Root过的话，这个ID也可以被任意改变。无需任何许可。
     */
    public String getAndroidID() {
        String m_szAndroidID = Settings.Secure.getString(mContext.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return m_szAndroidID;
    }

    /** 网卡 MAC 地址（唯一）
     * The WLAN MAC Address string是另一个唯一ID。
     * 但是你需要为你的工程加入 android.permission.ACCESS_WIFI_STATE 权限，否则这个地址会为 null。
     * Returns: 00:11:22:33:44:55 (这不是一个真实的地址。而且这个地址能轻易地被伪造。).
     * WLan不必打开, 就可读取些值。
     */
    public String getWLANMACAddress() {
        WifiManager wm = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        String m_szWLANMAC = wm.getConnectionInfo().getMacAddress();
        return m_szWLANMAC;
    }

    /** 蓝牙地址（唯一，但要打开蓝牙才能读取）
     * 只在有蓝牙的设备上运行。并且要加入 android.permission.BLUETOOTH 权限.Returns: 43:25:78:50:93:38 .
     * 蓝牙没有必要打开，也能读取。
     */
    public String getBTMACAddress() {
        BluetoothAdapter m_BluetoothAdapter = null; // Local Bluetooth adapter
        m_BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        String m_szBTMAC = m_BluetoothAdapter.getAddress();
        return m_szBTMAC;
    }

    /**
     * Combined Device ID
     * 综上所述，我们一共有五种方式取得设备的唯一标识。它们中的一些可能会返回null，或者由于硬件缺失、权限问题等
     * 获取失败。但你总能获得至少一个能用。所以，最好的方法就是通过拼接，或者拼接后的计算出的 MD5值来产生一个结果。
     * 通过算法，可产生32位的 16进制数据:9DDDF85AFF0A87974CE4541BD94D5F55
     */
    public String getUniqueID() {
        // String m_szLongID = getIMEI() + getPesudoUniqueID() + getAndroidID() + getWLANMACAddress() + getBTMACAddress();
        String m_szLongID = getPesudoUniqueID() + getWLANMACAddress() + getBTMACAddress();
        // compute md5
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        m.update(m_szLongID.getBytes(), 0, m_szLongID.length());
        // get md5 bytes
        byte p_md5Data[] = m.digest();
        // create a hex string
        String m_szUniqueID = new String();
        for (int i = 0; i < p_md5Data.length; i++) {
            int b = (0xFF & p_md5Data[i]);
            // if it is a single digit, make sure it have 0 in front (proper padding)
            if (b <= 0xF)
                m_szUniqueID += "0";
            // add number to string
            m_szUniqueID += Integer.toHexString(b);
        }   // hex string to uppercase
        m_szUniqueID = m_szUniqueID.toUpperCase();
        return m_szUniqueID;
    }

}
