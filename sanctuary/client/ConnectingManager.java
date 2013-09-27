/**
 *
 * @author Thunder
 */
package sanctuary.client;

import com.m3m.utils.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.exec.*;
import org.apache.commons.io.FilenameUtils;

public final class ConnectingManager {
    
    private Configuration conf;
    private EmaLogger eLogger;
    private volatile boolean alive;
    private final ExecutorService taskExecutor;
    private final LoginTask loginTask;
    private final UpdateTask updateTask;
    private final StatusTask statusTask;
    protected NetworkEventsListener listener;
    private File encryptedNetConfigFile;
    private File decryptedNetConfigFile;
    
    public final boolean setNetConfigFile(File encryptedNetConfigFile) {
        if (Encryption.isApplicationFile(encryptedNetConfigFile)) {
            this.encryptedNetConfigFile = encryptedNetConfigFile;
            return true;
        }
        return false;
    }
    
    public final boolean isNetConfigFileSet() {
        return (encryptedNetConfigFile != null && encryptedNetConfigFile.exists());
    }
    
    public ConnectingManager(Configuration conf, EmaLogger eLogger) {
        this.conf = conf;
        this.eLogger = eLogger;
        loginTask = new LoginTask();
        updateTask = new UpdateTask();
        statusTask = new StatusTask();
        taskExecutor = Executors.newFixedThreadPool(conf.getInt("Threads_Count"));
        setNetConfigFile(new File(conf.get("VPN_ConfigFile")));
    }
    
    public void setListener(NetworkEventsListener listener) {
        this.listener = listener;
    }
    
    public void abort() {
        if (alive) {
            doAbort();
        }
    }
    
    public void shutdownTasks() {
        if (!taskExecutor.isShutdown()) {
            taskExecutor.shutdown();
        }
    }
    
    public void login() {
        if (encryptedNetConfigFile == null) {
            return;
        }
        taskExecutor.execute(loginTask);
    }
    
    private void doLogin() {
        eLogger.log("Login Started");
        try {
            // Prepare execution:
            DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
            Executor processExecutor = new DefaultExecutor();
            processExecutor.setStreamHandler(new PumpStreamHandler(System.out, System.err, System.in));

            // TOR connection:
            eLogger.log(conf.get("Command_OBFS_ON"));
            
            CommandLine cmdLine = CommandLine.parse(conf.get("Command_OBFS_ON"));
            processExecutor.execute(cmdLine, resultHandler);
            
            long sleepPeriod = Math.round(conf.getDouble("TimeToWaitBetweenObfsAndVPN") * 1000);
            Thread.sleep(sleepPeriod);
            
            String decryptedNetConfigFileName = Utils.getDate("HHmmss") + "_" + FilenameUtils.removeExtension(encryptedNetConfigFile.getName());
            // Check the Memory Device /tmpfs if it's available to avoid writing to harddrive.
            String linuxTMPFS = "/run/shm/";
            Path path = Paths.get(linuxTMPFS);
            
            if (conf.getInt("USE_TMPFS") == 1 && Files.isDirectory(path) && Files.isWritable(path)) {
                decryptedNetConfigFileName = linuxTMPFS + decryptedNetConfigFileName;
            }

            // Decrypt the Net-Config file:
            Encryption.decrypt(encryptedNetConfigFile.getName(), decryptedNetConfigFileName);

            // Create decrypted file:
            decryptedNetConfigFile = new File(decryptedNetConfigFileName);
            decryptedNetConfigFile.deleteOnExit();

            // OBFS connection:
            String vpnCmd = String.format(conf.get("Command_VPN_ON"), decryptedNetConfigFile.getAbsolutePath());
            eLogger.log(vpnCmd);
            cmdLine = CommandLine.parse(vpnCmd);
            processExecutor.execute(cmdLine, resultHandler);
            
        } catch (Exception ex) {
            eLogger.log(ex);
        }
        eLogger.log("Login Finished");

        // Run the status checker thread:
        alive = true;
        taskExecutor.execute(statusTask);
    }
    
    private void doAbort() {
        eLogger.log("Abort Started");
        try {
            // Prepare execution:
            DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
            Executor processExecutor = new DefaultExecutor();
            processExecutor.setStreamHandler(new PumpStreamHandler(System.out, System.err, System.in));

            // VPN connection:
            eLogger.log(conf.get("Command_VPN_OFF"));
            CommandLine cmdLine = CommandLine.parse(conf.get("Command_VPN_OFF"));
            processExecutor.execute(cmdLine, resultHandler);

            // OBFS connection:
            eLogger.log(conf.get("Command_OBFS_OFF"));
            cmdLine = CommandLine.parse(conf.get("Command_OBFS_OFF"));
            processExecutor.execute(cmdLine, resultHandler);
        } catch (IOException ex) {
            eLogger.log(ex);
        }
        eLogger.log("Abort Finished");
    }
    
    public boolean isConnected() {
        return NetworkFunctions.isNetworkInterfaceUp(conf.get("VPN_Interface"));
    }
    
    public String getStatus() {
        return isConnected() ? "Connected" : "Disconnected !!";
    }
    
    public void update() {
        taskExecutor.execute(updateTask);
    }
    
    private void doUpdate() {
        boolean successfulUpdate = false;
        // Get Mirrors Links:
        eLogger.log("Update Started");
        String[] links = conf.get("Updates_Mirrors").split(" ");
        for (String link : links) {
            try {
                String fileName = NetworkFunctions.downloadFile(link);
                if (setNetConfigFile(new File(fileName))) {
                    successfulUpdate = true;
                    break;
                }
            } catch (IOException ex) {
                eLogger.log(ex);
            }
        }

        // Inform Listeners:
        listener.updateCompleted(successfulUpdate);
    }
    
    public void openWebsite() {
        BrowserLauncher.openURL(conf.get("WebsiteUrl"));
    }
    
    class LoginTask implements Runnable {
        
        @Override
        public void run() {
            doLogin();
        }
    }
    
    class UpdateTask implements Runnable {
        
        @Override
        public void run() {
            doUpdate();
        }
    }
    
    class StatusTask implements Runnable {
        
        @Override
        public void run() {
            boolean previousState = false, currentState;
            long sleepPeriod = Math.round(conf.getDouble("TimeIntervalForCheckStatus") * 1000);
            
            while (alive) {
                try {
                    currentState = isConnected();
                    if (currentState == previousState) {
                        Thread.sleep(sleepPeriod);
                    } else {
                        previousState = currentState;
                        // Inform Listeners Connection Initialized:
                        if (currentState) {
                            listener.loginCompleted();
                        } else {
                            listener.abortCompleted();
                            alive = false;
                        }
                    }

                    // Delete the plain Net-Config file from LoginTask
                    if (decryptedNetConfigFile != null) {
                        decryptedNetConfigFile.delete();
                    }
                    
                } catch (InterruptedException ex) {
                    eLogger.log(ex);
                }
            }
        }
    }
}
