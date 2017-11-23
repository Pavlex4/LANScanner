package com.pavle.lanscanner;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by pavle on 20-Nov-17.
 */

public class NetworkInfoAdapter extends BaseAdapter
{
    private Context mContext;
    private List<NetworkInformation> mNetworkInfoList;

    public NetworkInfoAdapter(Context mContext, List<NetworkInformation> mDeviceList)
    {
        this.mContext = mContext;
        this.mNetworkInfoList = mDeviceList;
    }

    @Override
    public int getCount()
    {
        return mNetworkInfoList.size();
    }

    @Override
    public Object getItem(int position)
    {
        return mNetworkInfoList.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup)
    {
        View v = View.inflate(mContext, R.layout.custom_layout, null);

        TextView ipAddress = (TextView) v.findViewById(R.id.ipAddress);
        TextView macAddress = (TextView) v.findViewById(R.id.macAddress);
        TextView hostName = (TextView) v.findViewById(R.id.hostName);
        TextView manufacturer = (TextView) v.findViewById(R.id.manufacturer);

        ipAddress.setText(mNetworkInfoList.get(position).getIpAddress());
        macAddress.setText(mNetworkInfoList.get(position).getMacAddress());
        hostName.setText(mNetworkInfoList.get(position).getHostName());
        manufacturer.setText(mNetworkInfoList.get(position).getManufacturer());

        v.setTag(mNetworkInfoList.get(position).getId());

        return v;
    }
}
