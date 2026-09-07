package fr.epicanard.gmcupdater;

import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.plugin.java.JavaPlugin;

import fr.epicanard.gmcupdater.commands.CommandHandler;
import fr.epicanard.gmcupdater.configuration.CategoryHandler;
import fr.epicanard.gmcupdater.configuration.ConfigLoader;
import fr.epicanard.gmcupdater.exceptions.CantLoadConfigException;
import fr.epicanard.gmcupdater.utils.MinecraftVersion;



public class GMCUpdater extends JavaPlugin {
  public static GMCUpdater plugin;
  public final ConfigLoader configLoader;
  public CategoryHandler catHandler;
  public MinecraftVersion minecraftVersion;

  public GMCUpdater() {
    // Initialization of loader
    this.configLoader = new ConfigLoader();
    this.minecraftVersion = MinecraftVersion.parse(this.getServer().getBukkitVersion());
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
