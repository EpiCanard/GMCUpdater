package fr.epicanard.gmcupdater.utils;

import java.util.logging.Level;

import fr.epicanard.gmcupdater.GMCUpdater;

public final class LoggerUtils {
  public static void warn(String msg) {
    GMCUpdater.plugin.getLogger().log(Level.WARNING, msg);
  }

  public static void info(String msg) {
    GMCUpdater.plugin.getLogger().log(Level.INFO, msg);
  }
}