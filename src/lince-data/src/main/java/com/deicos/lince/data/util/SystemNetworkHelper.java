package com.deicos.lince.data.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;

/**
 * com.deicos.lince.data.util
 * Class SystemNetworkHelper
 * 26/10/2019
 * Helper for getting internal ip or private Ip in a network
 *
 * @author berto (alberto.soto@gmail.com)
 */
public class SystemNetworkHelper {

    protected static final Logger log = LoggerFactory.getLogger(SystemNetworkHelper.class);

    /**
     * Gets hostName
     *
     * @return hostname
     */
    public static String getHostName() {
        InetAddress ip;
        String hostname = StringUtils.EMPTY;
        try {
            ip = InetAddress.getLocalHost();
            hostname = ip.getHostName();
        } catch (Exception e) {
            log.error("getting hostname", e);
        }
        return hostname;
    }

    /**
     * Gets first accessible IP
     * If virtual networks
     *
     * @return ip
     */
    public static String getStandardIp() {
        InetAddress ip;
        try {
            ip = InetAddress.getLocalHost();
            return ip.getHostAddress();
        } catch (Exception e) {
            log.error("getting ip", e);
            return null;
        }
    }

    public static String getAccessibleIp() {
        String ip = StringUtils.EMPTY;
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            ip = socket.getLocalAddress().getHostAddress();
            log.info("local address is:" + ip);
        } catch (Exception e) {
            log.error("getting ip", e);
        }
        return ip;
    }

    public static String getMacAccessibleIp(){
        try{
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("google.com", 80));
            return socket.getLocalAddress().toString();
        }catch (Exception e){
            log.error("Getting ip",e);
            return null;
        }
    }

    public static String getAccessibleIPStandard() {
        InetAddress ip;
        String ipValue = StringUtils.EMPTY;
        try {
            ip = InetAddress.getLocalHost();
            System.out.println("Current IP address : " + ip.getHostAddress());
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            byte[] mac = network.getHardwareAddress();
            System.out.print("Current MAC address : ");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            System.out.println(sb.toString());
        } catch (Exception e) {
            log.error("getting ip", e);
        }
        return ipValue;
    }

}
