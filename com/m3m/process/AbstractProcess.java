package com.m3m.process;

import com.m3m.utils.EmaLogger;
import com.m3m.utils.Utils;
import java.io.*;
import com.m3m.utils.Configuration;

// Example shutdown hook class
public abstract class AbstractProcess {

  private boolean working;
  private EmaLogger logger;
  private String configFolder = "C:/INServices/config/";
  private String configFilename = "config.xml";
  protected Configuration config;

  public abstract void doStart();

  public void doStop() {
  }

  public void start() {
    start(null);
  }

  public void start(String[] args) {
    // Load Configuration XML-File:

    try {
      config = new Configuration(getConfigFilename());
    }
    catch (IOException ex) {
      halt(ex.getMessage());
    }

    // Enable/Disable Logging, Tracing:
    logger = new EmaLogger(AbstractProcess.class, config.get("Process_Name"));
    logger.setLogging(true);

    if (config.get("Tracing").equalsIgnoreCase("YES")) {
      logger.setTracing(true);
    }

    logger.log(String.format("Starting Process: [%s], Arguments: [%s]", config.get("Process_Name"), args));

    logger.log("\t-->Logger Created");
    logger.log("\t-->Configuration XML-File Loaded");

    // Checking Arguments:
    logger.log("\t-->Checking CMD-Arguments");
    extractCmdAgruments(args);

    // Add Shutdown Hook:
    logger.log("\t-->Adding Shutdown-Hook");
    addShutdownHook();

    logger.log("\t-->Create Process Objects");
    // Process Status:
    setWorking(true);

    // Start the Process:
    doStart();

    // Process Started
    logger.log("\t-->Process Started");
  }

  public void stop() {

    // Left to each Process:
    doStop();

    // Process Status:
    setWorking(false);
  }

  public AbstractProcess() {
    initVariales();
  }

  private void initVariales() {
    // Set the Config-files Folder based on the operating system:
    if (Utils.isUnix()) {
      configFolder = "/home/INServices/config/";
    }
    else if (Utils.isWindows()) {
      configFolder = "C:/INServices/config/";
    }
  }

  protected void extractCmdAgruments(String[] args) {
    // Checking Arguments:
    int argsCount = config.getInt("CMDArguments_Count");

    // Arguments names are based on 'CMDArguments_Names':
    String[] tokens = Utils.tokenize(config.get("CMDArguments_Names"), " ");

    if (argsCount > 0) {
      if (args == null || args.length != argsCount) {
        String msg = "Error in Command-Line Parameters count...";
        halt(msg);
      }
      else if (tokens == null || tokens.length != argsCount) {
        String msg = String.format("Error in Configuration file: %s @ Nodes: 'CMDArguments_Count' & CMDArguments_Names",
                                   config.getFileName());
        halt(msg);
      }
      else {
        // Getting CMD Arguments:
        for (int i = 0; i < config.getInt("CMDArguments_Count"); i++) {
          config.add(tokens[i], args[i]);
        }
      }
    }
  }

  protected void halt(String msg) {
    System.err.println(msg);
    if (logger != null) {
      logger.log(msg);
    }
    System.exit(1);
  }

  public String getConfigFilename() {
    return configFilename;
  }

  public Configuration getConfig() {
    return config;
  }

  public boolean isWorking() {
    return working;
  }

  public void setConfigFilename(String configFilename, boolean locatedInConfigFolder) {
    if (locatedInConfigFolder) {
      this.configFilename = configFolder + configFilename;
    }
    else {
      this.configFilename = configFilename;
    }
  }

  public void setWorking(boolean working) {
    this.working = working;
  }

  public EmaLogger getLogger() {
    return logger;
  }

  public String getConfigFolder() {
    return configFolder;
  }

  public final void sleep(Object obj, long sleepPeriod) {
    sleep(obj.toString(), sleepPeriod);
  }

  public final void sleep(String message, long sleepPeriod) {
    String msg = String.format("Msg: -%s- Sleep (%s seconds)", message, sleepPeriod / 1000);
    getLogger().log(msg);
    try {
      Thread.sleep(sleepPeriod);
    }
    catch (InterruptedException ie) {
      ie.printStackTrace();
    }
  }

  protected void addShutdownHook() {
    ShutdownHook hook = new ShutdownHook(this);
    Runtime.getRuntime().addShutdownHook(hook);
  }
}
class ShutdownHook extends Thread {

  private AbstractProcess inProcess;

  public ShutdownHook(AbstractProcess inProcess) {
    this.inProcess = inProcess;
  }

  public void run() {
    inProcess.getLogger().log("ShutdownHook Called...");
    inProcess.setWorking(false);
    inProcess.stop();
  }
}
