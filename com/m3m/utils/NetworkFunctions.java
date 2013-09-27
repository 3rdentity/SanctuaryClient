/**
 *
 * @author thunder
 */
package com.m3m.utils;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

public class NetworkFunctions {

    public static void main(String[] args) {
        // This for testing:
        getActiveNetworksInterfaces();
    }

    private NetworkFunctions() {
    }

    public void setProxy(String host, String port, String username, String password, boolean socks) {
        boolean useAuthentication = false;
        if ((username != null) && (password != null)) {
            useAuthentication = true;
        }

        System.getProperties().put("proxySet", "true");
        if (socks) {
            // SOCKS Proxy
            System.setProperty("socksProxyHost", host);
            System.setProperty("socksProxyPort", port);
            if (useAuthentication) {
                System.setProperty("java.net.socks.username", username);
                System.setProperty("java.net.socks.password", password);
                Authenticator.setDefault(new ProxyAuth(username, password));
            }
        } else {
            // HTTP Proxy:
            System.setProperty("http.proxyHost", host);
            System.setProperty("http.proxyPort", port);
            // HTTPS Proxy:
            System.setProperty("https.proxyHost", host);
            System.setProperty("https.proxyPort", port);
            if (useAuthentication) {
//                String encoded = new String(Base64.encodeBase64((username + ":" + password).getBytes()));
//                con.setRequestProperty("Proxy-Authorization", "Basic " + encoded);
                Authenticator.setDefault(new ProxyAuth(username, password));
            }
        }
    }

    public void disableAllProxies() {
        System.getProperties().put("socksProxySet", "false");
        System.setProperty("socksProxyHost", "");
        System.setProperty("socksProxyPort", "");

        System.getProperties().put("proxySet", "false");

        // HTTP Proxy:
        System.setProperty("http.proxyHost", "");
        System.setProperty("http.proxyPort", "");
        // HTTPS Proxy:
        System.setProperty("https.proxyHost", "");
        System.setProperty("https.proxyPort", "");
    }

    public static void setProxyAsSystem() {
        // Use System Proxy:
        System.setProperty("java.net.useSystemProxies", "true");
    }

    public void disableDefaultProxy() {
        System.getProperties().put("proxySet", "false");
        System.getProperties().put("proxyHost", "");
        System.getProperties().put("proxyPort", "");
    }

    // Download the file from provided URL and return the fileName:
    public static String downloadFile(String url) throws IOException {
        String fileName = getFileName(url);
        downloadFile(url, fileName);
        return fileName;
    }

    public static void downloadFile(String url, String fileName) throws IOException {
        URL link = new URL(url);
        File file = new File(fileName);
        FileUtils.copyURLToFile(link, file);             
    }

    private static String getFileName(String url) throws IOException {
        String fileName = null;
        URL linkURL = new URL(url);
        URLConnection connection = linkURL.openConnection();
        String contentDisposition = connection.getHeaderField("content-disposition");
        if (contentDisposition != null) {
            fileName = extractFileNameFromContentDisposition(contentDisposition);
        }
        if (fileName == null) {
            fileName = org.apache.commons.io.FilenameUtils.getName(url);
        }
        return fileName;
    }

    private static String extractFileNameFromContentDisposition(String contentDisposition) {
        String[] attributes = contentDisposition.split(";");
        for (String a : attributes) {
            if (a.toLowerCase().contains("filename")) {
                return a.substring(10);
            }
        }
        return null;
    }

    public static Collection<NetworkInterface> getActiveNetworksInterfaces() {
        ArrayList<NetworkInterface> networksInterfaces = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            while (nets.hasMoreElements()) {
                NetworkInterface network = nets.nextElement();
                if (network.isUp()) {
                    System.out.println(network.getName() + " " + network.getDisplayName());
                    networksInterfaces.add(network);
                }
            }
        } catch (SocketException ex) {
            Logger.getLogger(NetworkFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
        return networksInterfaces;
    }

    public static boolean isNetworkInterfaceUp(String interfaceName) {
        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            while (nets.hasMoreElements()) {
                NetworkInterface network = nets.nextElement();
                if (network.isUp() && network.getName().equalsIgnoreCase(interfaceName)) {
                    return true;
                }
            }
        } catch (SocketException ex) {
            Logger.getLogger(NetworkFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public static void displayInterfaceInformation(NetworkInterface netint) throws SocketException {
        System.out.println(String.format("Display name: %s", netint.getDisplayName()));
        System.out.println(String.format("Name: %s", netint.getName()));
        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
            System.out.println(String.format("InetAddress: %s", inetAddress));
        }
    }

    public class ProxyAuth extends Authenticator {

        private PasswordAuthentication auth;

        private ProxyAuth(String user, String password) {
            auth = new PasswordAuthentication(user, password == null ? new char[]{} : password.toCharArray());
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return auth;
        }
    }
}
