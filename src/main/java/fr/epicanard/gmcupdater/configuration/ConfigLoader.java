package fr.epicanard.gmcupdater.configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import fr.epicanard.gmcupdater.GMCUpdater;
import fr.epicanard.gmcupdater.exceptions.CantLoadConfigException;

public class ConfigLoader {

  public YamlConfiguration categories;

  public ConfigLoader() {
    this.categories =  null;
  }

  private YamlConfiguration loadOneFile(String fileName, boolean newFile) throws CantLoadConfigException {
    if (!fileName.substring(fileName.length() - 4).equals(".yml"))
      fileName += ".yml";

    File confFile = new File(GMCUpdater.plugin.getDataFolder(), fileName);

    if (!confFile.exists()) {
      confFile.getParentFile().mkdirs();
      if (!newFile) {
        GMCUpdater.plugin.saveResource(fileName, false);
      } else {
        try {
          confFile.createNewFile();
        } catch (IOException e) {}
      }
    }

    YamlConfiguration conf = new YamlConfiguration();
    try {
      conf.load(confFile);
      return conf;
    } catch (IOException | IllegalArgumentException | InvalidConfigurationException e) {
      throw new CantLoadConfigException(fileName);
    }
  }

  public YamlConfiguration loadResource(String filename) {
    try {
      InputStream res = GMCUpdater.plugin.getResource(filename);
      return YamlConfiguration.loadConfiguration(new InputStreamReader(res));
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  public YamlConfiguration recreateFile(String filename) {
    File confFile = new File(GMCUpdater.plugin.getDataFolder(), filename);
    confFile.delete();
    try {
      return loadOneFile(filename, true);
    } catch (CantLoadConfigException e) {
      return null;
    }
  }

  public void loadFiles()  throws CantLoadConfigException {
    this.categories = this.loadOneFile("categories.yml", false);
  }
}
