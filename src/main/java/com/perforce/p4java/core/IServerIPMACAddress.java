package com.perforce.p4java.core;

public interface IServerIPMACAddress extends IServerResource {

    String getServerInterface();

    String getMACAddress();

    String getIPV4Address();

    String getIPV6Address();
}
