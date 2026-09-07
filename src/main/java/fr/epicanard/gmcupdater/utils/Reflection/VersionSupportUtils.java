package fr.epicanard.gmcupdater.utils.Reflection;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import fr.epicanard.gmcupdater.utils.Reflection.Path;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Optional;
import fr.epicanard.gmcupdater.GMCUpdater;

public class VersionSupportUtils {

  private final NamespacedKey gmcTagNamespaceKey = new NamespacedKey(GMCUpdater.plugin, "GMCItem");

  private static VersionSupportUtils INSTANCE;

  private VersionSupportUtils() {
  }

  public static VersionSupportUtils getInstance() {
    if (INSTANCE == null)
      INSTANCE = new VersionSupportUtils();
    return INSTANCE;
  }


  // ======== TOOLS ============

  private Class<?>[] fromObjectToClass(Object[] args) {
    Class<?>[] classes = new Class<?>[0];

    if (args != null) {
      classes = new Class<?>[args.length];
      for (int i = 0; i < args.length; i++) {
        classes[i] = args[i].getClass();
      }
    }
    return classes;
  }

  private Object invokeMethod(Object object, String method, Object ...args) {
    try {
      return object.getClass().getMethod(method, fromObjectToClass(args)).invoke(object, args);
    } catch(InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
      e.printStackTrace();
    }
    return null;
  }

  private Optional<Field> findParametrizedField(Class<?> main, Class<?> type, Class<?> generic) {
    return Arrays
      .stream(main.getFields())
      .filter(f -> f.getType().isAssignableFrom(type)
          && ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0].equals(generic))
      .findFirst();

  }

  private Object getRegistry() {
    try {
      final Class<?> registryBlockClass = Path.MINECRAFT_CORE.getClass("RegistryBlocks");
      final Class<?> itemClass = Path.MINECRAFT_WORLD_ITEM.getClass("Item");
      final Class<?> registryClass = Path.MINECRAFT_CORE_REGISTRIES.getClass("BuiltInRegistries");
      final Optional<Field> maybeRegistryField = findParametrizedField(registryClass, registryBlockClass, itemClass);

      if (!maybeRegistryField.isPresent()) {
        throw new NoSuchFieldException("Can't find item Registry.");
      }
      return maybeRegistryField.get().get(null);
    } catch (Exception e) {
        return null;
    }
  }


  // ======= SPECIFIC METHOD ===========

  public ItemStack getItemStack(String name) {
    String key = name.substring(10);
    Material material = Registry.MATERIAL.get(NamespacedKey.minecraft(key));
    return this.setTag(new ItemStack(material, 1));
  }

  public boolean hasTag(ItemStack itemStack) {
    if (itemStack == null)
      return false;
    PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
    return container.has(this.gmcTagNamespaceKey, PersistentDataType.BYTE);
  }

  public ItemStack setTag(ItemStack itemStack) {
    if (itemStack == null || hasTag(itemStack))
      return itemStack;

    ItemMeta meta = itemStack.getItemMeta();

    PersistentDataContainer container = meta.getPersistentDataContainer();
    container.set(this.gmcTagNamespaceKey, PersistentDataType.BYTE, (byte) 0x01);

    itemStack.setItemMeta(meta);

    return itemStack;
  }

  public String getMinecraftKey(ItemStack itemStack) {
    NamespacedKey key = itemStack.getType().getKey();
    if (key != null)
      return key.getNamespace() + ":" + key.getKey();
    return null;
  }

}
