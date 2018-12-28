package fr.epicanard.gmcupdater.commands;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_13_R2.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.epicanard.gmcupdater.GMCUpdater;
import fr.epicanard.gmcupdater.utils.ItemStackUtils;
import net.minecraft.server.v1_13_R2.CreativeModeTab;
import net.minecraft.server.v1_13_R2.Item;

public class CommandHandler implements CommandExecutor {

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String msg, String[] args) {
    if (sender != null) {
      Map<String, List<String>> result = detectWrongConfig();
      if (sender instanceof Player) {
        printList(result, (Player)sender);
      } else {
        printList(result, null);
      }
      saveFile(result);
      return false;
    }
    return true;
  }

  private Map<String, List<String>> detectWrongConfig() {
    Map<String, List<String>> result = new HashMap<>();
    List<String> configItems = Arrays.asList(GMCUpdater.plugin.catHandler.getItems("!"));
    List<String> materialItems;

    materialItems = Arrays.asList(Material.values()).stream().map(mat -> {
      ItemStack item = new ItemStack(mat);
      return ItemStackUtils.getMinecraftKey(item);
    }).filter(mat -> {
      return !mat.equals("minecraft:air");
    }).collect(Collectors.toList());

    result.put("To delete", getToDelete(materialItems, configItems));

    List<String> uncategorized = getUncategorized(materialItems, configItems);
    convertItemsToCategories(result, uncategorized);

    return result;
  }

  private List<String> getUncategorized(List<String> materials, List<String> items) {
    return materials.stream().filter(mat -> !items.contains(mat)).collect(Collectors.toList());
  }

  private List<String> getToDelete(List<String> materials, List<String> items) {
    return items.stream().filter(item -> !materials.contains(item)).collect(Collectors.toList());
  }

  private void printList(Map<String, List<String>> map, Player player) {
    map.forEach((key, lst) -> {
      print(key + " :", player);
      lst.forEach(value -> { print(" - " + value, player); });
    });
  }

  private void print(String msg, Player pl) {
    if (pl == null) {
      GMCUpdater.plugin.getLogger().info(msg);
    } else {
      pl.sendMessage(" - " + msg);
    }
  }

  private void saveFile(Map<String, List<String>> result) {
    YamlConfiguration resultConfig = GMCUpdater.plugin.configLoader.recreateFile("result.yml");
    result.forEach((key, lst) -> {
      resultConfig.set(key, lst);
    });
    try {
      resultConfig.save(GMCUpdater.plugin.getDataFolder() + "/result.yml");
    } catch (IOException e) {}
  }

  private void convertItemsToCategories(Map<String, List<String>> result, List<String> uncategorized) {
    uncategorized = uncategorized.stream().filter(mk -> {
      ItemStack item = ItemStackUtils.getItemStack(mk);
      String category = getCategoryByMystery(item.getType());
      if (category == null) {
        return true;
      }
      List<String> catLst = result.get(category);
      if (catLst == null) {
        catLst = new ArrayList<>();
      }
      catLst.add(mk);
      result.put(category, catLst);
      return false;
    }).collect(Collectors.toList());
    result.put("Uncategorized", uncategorized);
  }

  private String getCategoryByMystery(Material item) {
    Item it = CraftMagicNumbers.getItem(item);

    try {
      Field[] lst = FieldUtils.getAllFields(it.getClass());
      for (Field a : lst) {
        if (a.getType() == CreativeModeTab.class) {
          CreativeModeTab ret = (CreativeModeTab) FieldUtils.readField(a, it, true);
          if (ret != null) {
            return ret.c();
          }
        }
      }
    } catch (IllegalArgumentException | IllegalAccessException e) {
      System.out.println("Vilain !");
    }
    return null;
  }

}
