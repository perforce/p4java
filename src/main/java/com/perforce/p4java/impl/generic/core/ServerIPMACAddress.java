package com.perforce.p4java.impl.generic.core;

import com.perforce.p4java.core.IServerIPMACAddress;
import java.util.Map;

public class ServerIPMACAddress extends ServerResource implements IServerIPMACAddress {
    private String serverInterface;
    private String macAddress;
    private String ipv4Address;
    private String ipv6Address;

    public ServerIPMACAddress() {}

    public ServerIPMACAddress(Map<String, Object> map) {
        if(map.size() > 0 ) {
            this.serverInterface = String.valueOf(map.get("interface"));
            this.macAddress = String.valueOf(map.get("macAddress"));
            this.ipv4Address = String.valueOf(map.get("ipv4Address"));
            this.ipv6Address = String.valueOf(map.get("ipv6Address"));
        }
    }

    public String getServerInterface() {
        return serverInterface;
    }

    public String getMACAddress() { return macAddress; }

    public String getIPV4Address() {
        return ipv4Address;
    }

    public String getIPV6Address() {
        return ipv6Address;
    }
}
