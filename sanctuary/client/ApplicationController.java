/**
 *
 * @author Thunder
 */
package sanctuary.client;

import com.m3m.utils.*;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class ApplicationController {

    private Configuration conf;
    private EmaLogger eLogger;
    private ApplicationWindow clientGUI;
    private ConnectingManager connectionManager;

    public ApplicationController() {
        this("Application.properties");
    }

    public ApplicationController(String configFile) {
        // 0. Allow one instance only:
        System.out.println("Check Single-Instance Condition");
        if (!SingleInstanceApplication.registerInstance()) {
            System.out.println("There's another instance that's running");
        }

        // 1. Load configuration from properities file:
        try {
            System.out.println("Configuration file: " + configFile);

            // Load Encrypted properties file using JASYPT:
            Properties properties = Encryption.loadEncryptedProperties(configFile);
            conf = new Configuration(configFile, properties);
        } catch (IOException ex) {
            System.err.println(ex);
            System.exit(1);
        }

        // 2. Create Logger:
        eLogger = new EmaLogger(ApplicationController.class, conf.get("Application"));
    }

    public void start() {
        // 3. Create Connection Manager:
        eLogger.log("Create Connection Manager");
        connectionManager = new ConnectingManager(conf, eLogger);

        // 4. StartGUI:
        eLogger.log("Create GUI");
        createGUI();

        // 5. Set Events Listeners:
        eLogger.log("Set Events Listeners");
        connectionManager.setListener(new NetworkEventsListener() {
            @Override
            public void loginCompleted() {
                clientGUI.toggleGuiControls(true);
            }

            @Override
            public void abortCompleted() {
                clientGUI.toggleGuiControls(false);
            }

            @Override
            public void updateCompleted(boolean successfulUpdate) {
                clientGUI.showUpdateResult(successfulUpdate);
            }
        });

        // 6. Update on Startup:
        if (conf.getInt("Update_on_Startup") == 1) {
            update();
        }
    }

    private void createGUI() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                clientGUI = new ApplicationWindow(ApplicationController.this, conf, eLogger);
                // Prepare frame title:
                clientGUI.setTitle(String.format(".: %s %s :.", conf.get("Project"), conf.get("Application")));
                // Show the GUI:
                clientGUI.setVisible(true);
            }
        };
        java.awt.EventQueue.invokeLater(r);
    }

    public String getTrayIconMessage() {
        return String.format("%s %s v%s - %s", conf.get("Project"), conf.get("Application"),
                conf.get("Version"), connectionManager.getStatus());
    }

    public void login() {
        if (connectionManager.isNetConfigFileSet()) {
            clientGUI.setEnabledForButtons(false);
            connectionManager.login();
        } else {
            clientGUI.showNetConfigFileSetOptions();
        }
    }

    void abort() {
        clientGUI.setEnabledForButtons(true);
        connectionManager.abort();
    }

    public void update() {
        while (clientGUI == null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                eLogger.log(ex);
            }
        }
        clientGUI.showUpdateStarted();
        clientGUI.disableUpdateButtons();
        connectionManager.update();
    }

    public void exit() {
        // Abort any connection:
        connectionManager.abort();
        connectionManager.shutdownTasks();

        // Exit Application
        System.exit(0);
    }

    ConnectingManager getConnectionManager() {
        return connectionManager;
    }

    void restore() {
        clientGUI.setVisible(true);
        clientGUI.setState(Frame.NORMAL);
    }

    void gototray() {
        clientGUI.setVisible(false);
        clientGUI.setState(Frame.ICONIFIED);
    }

    void loadNetConfigFile() {
        File selectedFile = clientGUI.browseForNetConfig();
        if (selectedFile == null) {
            return;
        }
        boolean result = connectionManager.setNetConfigFile(selectedFile);
        clientGUI.showLoadNetConfigResult(result);
    }

    public void checkAndFixNetConfigFile() {
        // Check and fix:
        if (!connectionManager.isNetConfigFileSet()) {
            clientGUI.showNetConfigFileSetOptions();
        }
    }

    public Configuration getConf() {
        return conf;
    }
}
