package cn.aezo.utils.base;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class HttpU {
    /**
     * 获取一个合理的ipv4(使用VPN/虚拟机后优先取VPN/虚拟机的内网)
     * @return
     */
    public String getIP() {
        try {
            // 根据hostname找ip
            InetAddress address = InetAddress.getLocalHost();
            if (address.isLoopbackAddress()) {
                Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
                while (allNetInterfaces.hasMoreElements()) {
                    NetworkInterface netInterface = allNetInterfaces.nextElement();
                    Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress ip = addresses.nextElement();
                        if (!ip.isLinkLocalAddress() && !ip.isLoopbackAddress() && ip instanceof Inet4Address) {
                            return ip.getHostAddress();
                        }
                    }
                }
            }
            return address.getHostAddress();
        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取所有网络接口
     * @param onlyIPv4 是否仅获取Ipv4地址
     * @return
     */
    public List<String> getAllIP(boolean onlyIPv4) {
        List<String> ips = new ArrayList<>();

        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = allNetInterfaces.nextElement();

                // 去除回环接口，子接口，未运行和接口
                if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress ip = addresses.nextElement();
                    if (ip != null) {
                        if(onlyIPv4) {
                            // ipv4
                            if (ip instanceof Inet4Address) {
                                ips.add(ip.getHostAddress());

                                //
                            }
                        } else {
                            ips.add(ip.getHostAddress());
                        }
                    }
                }
            }
        } catch (SocketException e) {
            System.err.println("Error when getting host ip address"+ e.getMessage());
        }

        return ips;
    }
}
