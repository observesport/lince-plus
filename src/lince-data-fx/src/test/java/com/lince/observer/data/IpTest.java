package com.lince.observer.data;

import com.lince.observer.data.util.SystemNetworkHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * com.lince.observer.data
 * Class IpTest
 * 26/10/2019
 *
 * @author berto (alberto.soto@gmail.com)
 */
public class IpTest {

    @Test
    public void getHostName() {
        String name = SystemNetworkHelper.getHostName();
        Assertions.assertNotNull(name);
    }

    @Test
    public void getHostIp() {
        String ip = SystemNetworkHelper.getStandardIp();
        Assertions.assertNotNull(ip);
    }

    @Test
    public void testExternalIp() {
        String ip = SystemNetworkHelper.getAccessibleIp();
        Assertions.assertNotNull(ip);
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
