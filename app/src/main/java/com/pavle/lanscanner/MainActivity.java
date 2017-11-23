package com.pavle.lanscanner;

import android.content.SharedPreferences;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import jcifs.netbios.NbtAddress;

public class MainActivity extends AppCompatActivity
{

    private Button btnScan;
    private String defaultGateway = "";
    private int i = 0;

    DhcpInfo dhcp;
    WifiManager wifiManager;
    WifiInfo wifiInfo;
    ArrayList<String> ipAddressList = new ArrayList<String>();
    ArrayList<String> macAddressList = new ArrayList<String>();
    ArrayList<String> hostNameList = new ArrayList<String>();
    ArrayList<String> manufacturerList = new ArrayList<String>();

    private ImageView imgDevice;

    private PrintWriter printwriter;
    private String message;
    private Socket client;

    private ListView lvNetworkInfo;
    private NetworkInfoAdapter adapter;
    private List<NetworkInformation> mNetworkInfoList;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnScan = (Button) findViewById(R.id.readclient);
        lvNetworkInfo = (ListView) findViewById(R.id.lvNetworkInfo);

        mNetworkInfoList = new ArrayList<>();

        adapter = new NetworkInfoAdapter(getApplicationContext(), mNetworkInfoList);

        lvNetworkInfo.setAdapter(adapter);

        getRouterInfo();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("defaultGateway", defaultGateway);
        editor.apply();

        btnScan.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                pingIp();

                Runnable runnable = new IpMacTask();
                new Thread(runnable).start();

                getDeviceIPMac();

                addToListView();
            }
        });
    }

    private void getRouterInfo()
    {
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        dhcp = wifiManager.getDhcpInfo();
        defaultGateway = Formatter.formatIpAddress(dhcp.gateway);

        wifiInfo = wifiManager.getConnectionInfo();
        DhcpInfo dhcp = wifiManager.getDhcpInfo();
        String SSID = wifiInfo.getSSID().replace('\"', ' ').trim();
    }

    String pingCmd = "";

    public void pingIp()
    {
        String ip = "";
        int i = 0;

        while (i < 255)
        {
            ip = defaultGateway.substring(0, 9) + "." + i;
            //pingCmd = "ping -c 1 -w 1 " + ip;

            Runnable runnable = new ScanIpTask(ip);
            new Thread(runnable).start();

            i++;
        }
    }

    public class ScanIpTask implements Runnable
    {
        String ip;

        public ScanIpTask(String ip)
        {
            this.ip = ip;
        }

        public void run()
        {
            try
            {
                pingCmd = "ping -c 1 " + ip;
                BufferedReader in = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(pingCmd).getInputStream()));
                long timeS = System.nanoTime();

                while ((System.nanoTime() - timeS) / 1000000 < 2000)
                {
                    if (in.ready())
                    {
                        String inputLine = in.readLine();
                        if (inputLine == null || inputLine.contains("\u0000"))
                        {
                            break;
                        }
                        if (inputLine.contains("Destination Host Unreachable"))
                        {
                            break;
                        }
                        else if (inputLine.contains("100.0% packet loss"))
                        {
                            break;
                        }
                        else
                        {
                            //System.out.println("IP ADDRESS: " + ip);
                            //ipList.add(ip);

                            break;
                        }
                    }
                }
            }
            catch (UnknownHostException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void readIPMACAddress()
    {
        BufferedReader bufferedReader = null;

        try
        {
            bufferedReader = new BufferedReader(new FileReader("/proc/net/arp"));

            String line;
            while ((line = bufferedReader.readLine()) != null)
            {
                if(!line.contains("00:00:00:00:00:00"))
                {
                    String[] splitted = line.split("    ");

                    String ip = splitted[0].trim();
                    String mac = splitted[5].trim();

                    if(!ip.equals("") && !mac.equals(""))
                    {
                        ipAddressList.add(ip);
                        macAddressList.add(mac);
                    }
                    //System.out.println("IP Address: " + ip + " MAC Address: " + mac);
                }
            }

            bufferedReader.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private String getExternalIP()
    {
        try
        {
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet("http://whatismyip.akamai.com/");
            // HttpGet httpget = new HttpGet("http://ipecho.net/plain");
            HttpResponse response;

            response = httpclient.execute(httpget);

            //Log.i("externalip",response.getStatusLine().toString());
            HttpEntity entity = response.getEntity();
            if (entity != null)
            {
                long len = entity.getContentLength();
                if (len != -1 && len < 1024)
                {
                    String str= EntityUtils.toString(entity);
                    //Log.i("externalip",str);
                    return str;
                }
                else
                {
                    return "Response too long or error.";
                    //debug
                    //ip.setText("Response too long or error: "+EntityUtils.toString(entity));
                    //Log.i("externalip",EntityUtils.toString(entity));
                }
            }
            else
            {
                return "Null:" + response.getStatusLine().toString();
            }

        }
        catch (IOException e)
        {
            return "Error";
        }
    }

    private String getManufacturer()
    {
        String line = "";
        String output = "";
        List<String> macList = new ArrayList<String>();
        try
        {
            InputStream inputStream = getAssets().open("oui.txt");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = bufferedReader.readLine()) != null)
            {
                if(line.contains("(hex)"))
                {
                    line = line.replace("(hex)","/");
                    line = line.replace("-",":");
                    macList.add(line);
                    //System.out.println(line);
                }
            }

            bufferedReader.close();

            for(int i = 0; i < macAddressList.size(); i++)
            {
                if(macAddressList.get(i).substring(0, 7).equals(macList.get(i)))
                {
                    manufacturerList.add(macAddressList.get(i));
                    //macAddressList.set(i, macAddressList.get(i) + "/" + macList.get(i));
                }
            }
        }
        catch (IOException e)
        {
            Log.e("readMem", e.toString());
        }
        return output;
    }


    public String getDeviceIPMac()
    {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        String ipAddress = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
        String macAddress = wifiInfo.getMacAddress();
        System.out.println("Device ip and mac: " + ipAddress + " " + macAddress);

        return ipAddress + " " + macAddress;
    }

    private void getHostName()
    {
        try
        {
            for(int i = 0; i < ipAddressList.size(); i++)
            {
                if(!ipAddressList.get(i).equals("192.168.0.1") && !ipAddressList.get(i).equals("192.168.1.1"))
                {
                    NbtAddress[] nbts = NbtAddress.getAllByAddress(ipAddressList.get(i));
                    String netbiosname = nbts[0].getHostName();
                    hostNameList.add(netbiosname);
                }
                //System.out.println("ADDRESS: " + netbiosname);
            }


            /*InetAddress addr = NbtAddress.getByName( "OWNER-PC" ).getInetAddress();
            System.out.println("ADDRESS: " + addr);*/
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
    }

    public class IpMacTask implements Runnable
    {
        public IpMacTask()
        {

        }

        public void run()
        {
            readIPMACAddress();
            getHostName();
            getExternalIP();
            getManufacturer();

            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    addToListView();
                }
            });

        }
    }

    private void addToListView()
    {
        for(int i = 0; i < ipAddressList.size(); i++)
        {
            mNetworkInfoList.add(new NetworkInformation(i, ipAddressList.get(i), "", "", ""));
        }

        for(int i = 0; i < macAddressList.size(); i++)
        {
            mNetworkInfoList.get(i).setMacAddress(macAddressList.get(i));
        }

        for(int i = 0; i < hostNameList.size(); i++)
        {
            mNetworkInfoList.get(i).setHostName(hostNameList.get(i));
        }

        for(int i = 0; i < manufacturerList.size(); i++)
        {
            mNetworkInfoList.get(i).setManufacturer(manufacturerList.get(i));
        }

        if (adapter != null)
            adapter.notifyDataSetChanged();
    }
}
