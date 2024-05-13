package com.deicos.lince.data;

import com.deicos.lince.data.util.SystemNetworkHelper;
import org.junit.Assert;
import org.junit.Test;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * com.deicos.lince.data
 * Class IpTest
 * 26/10/2019
 *
 * @author berto (alberto.soto@gmail.com)
 */
public class IpTest {

    @Test
    public void getHostName() {
        String name = SystemNetworkHelper.getHostName();
        Assert.assertNotNull(name);
    }

    @Test
    public void getHostIp() {
        String ip = SystemNetworkHelper.getStandardIp();
        Assert.assertNotNull(ip);
    }

    @Test
    public void testExternalIp() {
        String ip = SystemNetworkHelper.getAccessibleIp();
        Assert.assertNotNull(ip);
    }

    @Test
    public void testExternalIpMac() {
        try {
            System.out.println(SystemNetworkHelper.getMacAccessibleIp());
        } catch (Exception e) {
            System.out.println("Exception:" + e);
        }
    }
}
