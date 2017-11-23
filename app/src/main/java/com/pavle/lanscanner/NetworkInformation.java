package com.pavle.lanscanner;

/**
 * Created by pavle on 20-Nov-17.
 */

public class NetworkInformation
{
    private int id;
    private String ipAddress, macAddress, hostName, manufacturer;

    public NetworkInformation(int id, String ipAddress, String macAddress, String hostName, String manufacturer)
    {
        this.id = id;
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.hostName = hostName;
        this.manufacturer = manufacturer;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getIpAddress()
    {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress)
    {
        this.ipAddress = ipAddress;
    }

    public String getMacAddress()
    {
        return macAddress;
    }

    public void setMacAddress(String macAddress)
    {
        this.macAddress = macAddress;
    }

    public String getHostName()
    {
        return hostName;
    }

    public void setHostName(String hostName)
    {
        this.hostName = hostName;
    }

    public String getManufacturer()
    {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer)
    {
        this.manufacturer = manufacturer;
    }
}
