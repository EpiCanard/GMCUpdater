package fr.epicanard.gmcupdater.utils;

import org.bukkit.inventory.ItemStack;

import fr.epicanard.gmcupdater.utils.Reflection.VersionSupportUtils;

/**
 * Utility class for ItemStacks
 */
public final class ItemStackUtils {

  /**
   * Get the itemstack from the specify key
   *
   * @param name Minecraft item name (ex: minecraft:chest)
   * @return ItemStack created from minecraft key
   */
  public static ItemStack getItemStack(String name) {
    if (name == null)
      return null;

    String[] spec = name.split("/");

    ItemStack item = VersionSupportUtils.getInstance().getItemStack(spec[0]);

    if (item != null) {
      if (spec.length > 1)
        item.setDurability(Short.parseShort(spec[1]));
    }

    return item;
  }

  /**
   * Get minecraft key from item
   *
   * @param item ItemStack
   * @return minecraft key in string
   */
  public static String getMinecraftKey(ItemStack item) {
    return VersionSupportUtils.getInstance().getMinecraftKey(item);
  }
}