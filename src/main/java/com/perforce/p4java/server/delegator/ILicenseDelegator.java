package com.perforce.p4java.server.delegator;

import com.perforce.p4java.core.ILicense;
import com.perforce.p4java.core.ILicenseLimits;
import com.perforce.p4java.core.IServerIPMACAddress;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.server.IServer;

import java.util.List;

public interface ILicenseDelegator {

    ILicenseLimits getLimits() throws P4JavaException;

    ILicense getLicense() throws P4JavaException;

    String updateLicense(final ILicense license) throws P4JavaException;

    List<IServerIPMACAddress> getValidServerIPMACAddress() throws P4JavaException;
}
