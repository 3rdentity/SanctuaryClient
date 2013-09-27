/**
 * @author Thunder
 */
package com.m3m.utils;

import java.lang.reflect.Method;
import javax.swing.JOptionPane;

public class BrowserLauncher {

    private BrowserLauncher() {
    }
    private static final String errMsg = "Error attempting to launch Web Browser";

    public static void openURL(String url) {
        String osName = System.getProperty("os.name");
        try {
            if (osName.startsWith("Mac OS")) {
                Class fileMgr = Class.forName("com.apple.eio.FileManager");
                Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[]{String.class});
                openURL.invoke(null, new Object[]{url});
            } else if (osName.startsWith("Windows")) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else { // assume Unix or Linux:
                String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape", "iceweasel"};
                String browser = null;
                for (int i = 0; i < browsers.length && browser == null; i++) {
                    if (Runtime.getRuntime().exec(new String[]{"which", browsers[i]}).waitFor() == 0) {
                        browser = browsers[i];
                    }
                }
                if (browser == null) {
                    throw new Exception("Could not find web browser");
                } else {
                    // Run the browser as normal user:
//                    String browserCommand = String.format("su - $(logname) -c '%s %s'", browser, url);
//                    String browserCommand = String.format("su - virtus -c '%s %s'", browser, url);
//                    Runtime.getRuntime().exec(browserCommand);
//                    System.out.println("browserCommand: " + browserCommand);
                    Runtime.getRuntime().exec(new String[]{browser, url});
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, errMsg + ":\n" + e.getLocalizedMessage());
        }
    }
}
