package fr.epicanard.gmcupdater;

import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

import fr.epicanard.gmcupdater.commands.CommandHandler;
import fr.epicanard.gmcupdater.configuration.CategoryHandler;
import fr.epicanard.gmcupdater.configuration.ConfigLoader;
import fr.epicanard.gmcupdater.exceptions.CantLoadConfigException;



public class GMCUpdater extends JavaPlugin {
  public static GMCUpdater plugin;
  public final ConfigLoader configLoader;
  public CategoryHandler catHandler;

  public GMCUpdater() {
    // Initialization of loader
    this.configLoader = new ConfigLoader();
  }

  @Override
  public void onEnable() {
    GMCUpdater.plugin = this;

    try {
      this.configLoader.loadFiles();
      this.catHandler = new CategoryHandler(GMCUpdater.plugin.configLoader.categories);
    } catch(CantLoadConfigException e) {}
    
    
    getCommand("GMCUpdater").setExecutor(new CommandHandler());
  }

  @Override
  public void onDisable() {
    this.disable();
  }

  public void disable() {
    this.getLogger().log(Level.WARNING, "Plugin GMCUpdater disabled");
    this.setEnabled(false);
  }
}
