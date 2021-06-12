package fr.epicanard.gmcupdater.utils.Reflection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class VersionSupportUtils {

  enum Path {
    BUKKIT("org.bukkit.craftbukkit"),
    MINECRAFT_SERVER("net.minecraft.server"),
    MINECRAFT_RESOURCES("net.minecraft.resources"),
    MINECRAFT_WORLD_ITEM("net.minecraft.world.item"),
    MINECRAFT_CORE("net.minecraft.core");

    String path;

    Path(String path) {
      this.path = path;
    }
  }

  private String version;
  private static VersionSupportUtils INSTANCE;

  /**
   * Private constructor VersionSupportUtils
   */
  private VersionSupportUtils() {
    this.version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
  }

  /**
   * Singleton, method to get the instance of this class
   */
  public static VersionSupportUtils getInstance() {
    if (INSTANCE == null)
      INSTANCE = new VersionSupportUtils();
    return INSTANCE;
  }


  // ======== TOOLS ============

  /**
   * Get a class from his path
   *
   * @param basePath Base of the pathe to define if it is a bukkit ou minecraft
   * @param path Name of the classe
   * @return return the class with the path
   * @throws ClassNotFoundException
   */
  private Class<?> getClassFromPath(Path basePath, String path) throws ClassNotFoundException {
    return Class.forName(String.format("%s.%s", basePath.path, path));
  }

  private Class<?> getClassFromPathWithVersion(Path basePath, String path) throws ClassNotFoundException {
    return Class.forName(String.format("%s.%s.%s", basePath.path, this.version, path));
  }

  /**
   * Transform an object array into a class array
   *
   * @param args Object array
   * @return return an array of class from each object
   */
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

  /**
   * Invoke the method of an object
   *
   * @param object the object on which call the method
   * @param method the method name to calle
   * @param args all the arguments that must be send to the method
   * @return return the object return by the method
   */
  private Object invokeMethod(Object object, String method, Object ...args) {
    try {
      return object.getClass().getMethod(method, fromObjectToClass(args)).invoke(object, args);
    } catch(InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
      e.printStackTrace();
    }
    return null;
  }

  private Object newInstance(String path, Object ...args) {
    try {
      return getClassFromPath(Path.MINECRAFT_SERVER, path).getConstructor(fromObjectToClass(args)).newInstance(args);
    } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
    }
    return null;
  }


  /**
   * Get the static object Item.REGISTRY
   *
   * @return return the static object
   */
  private Object getRegistry() {
    try {
        return getClassFromPath(Path.MINECRAFT_CORE, "IRegistry").getField("Z").get(null);
    } catch(ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
      System.out.println(e);
    }
    return null;
  }


  // ======= SPECIFIC METHOD ===========

  /**
   * Get an itemStack with is minecraft key
   *
   * @param name
   * @return
   */
  public ItemStack getItemStack(String name) {
    try {
      Class<?> minecraftKeyClass = getClassFromPath(Path.MINECRAFT_RESOURCES, "MinecraftKey");
      Object minecraftKey = minecraftKeyClass.getConstructor(String.class).newInstance(name);

      Object registry = getRegistry();
      Object item = registry.getClass().getMethod("get", minecraftKeyClass).invoke(registry, minecraftKey);
      if (item == null)
        return null;

      Method asNewCraftStack = getClassFromPathWithVersion(Path.BUKKIT, "inventory.CraftItemStack").getMethod("asNewCraftStack", getClassFromPath(Path.MINECRAFT_WORLD_ITEM, "Item"));
      ItemStack itemStack = (ItemStack)asNewCraftStack.invoke(null, item);

      return itemStack;

    } catch(ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
      e.printStackTrace();
    }

    return null;
  }


  /**
   * Get the minecraftkey in string. Format minecraft:dirt
   *
   * @param item
   *          ItemStack
   * @return return the string minecraft key
   */
  public String getMinecraftKey(ItemStack itemStack) {
    try {
      Method asNMSCopy = getClassFromPathWithVersion(Path.BUKKIT, "inventory.CraftItemStack").getDeclaredMethod("asNMSCopy", ItemStack.class);
      Object nmsItemStack = asNMSCopy.invoke(null, itemStack);

      Object registry = getRegistry();
      Object minecraftKey;
      try {
        minecraftKey = registry.getClass().getMethod("getKey", Object.class).invoke(registry, invokeMethod(nmsItemStack, "getItem"));
      } catch(NoSuchMethodException e) {
        return null;
      }

      return invokeMethod(minecraftKey, "getNamespace").toString() + ":" + invokeMethod(minecraftKey, "getKey").toString();
    } catch(ClassNotFoundException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Update the inventory name update by the player
   *
   * @param title The new inventory Name
   * @param player The player
   */
  public void updateInventoryName(String title, Player player) {
    try {
      Object entityPlayer = invokeMethod(player, "getHandle");
      Object chatMessage = newInstance("ChatMessage", title, new Object[]{});
      VersionField activeContainerVF = VersionField.from(entityPlayer).get("activeContainer");
      Object windowId = activeContainerVF.get("windowId").value();

      Class<?> iChat = getClassFromPath(Path.MINECRAFT_SERVER, "IChatBaseComponent");

      Object packet = getClassFromPath(Path.MINECRAFT_SERVER, "PacketPlayOutOpenWindow").getConstructor(Integer.TYPE, String.class, iChat, Integer.TYPE)
        .newInstance( windowId, "minecraft:chest", iChat.cast(chatMessage), player.getOpenInventory().getTopInventory().getSize());


      Object playerConnection = entityPlayer.getClass().getDeclaredField("playerConnection").get(entityPlayer);

      playerConnection.getClass().getMethod("sendPacket", getClassFromPath(Path.MINECRAFT_SERVER, "Packet")).invoke(playerConnection, packet);
      entityPlayer.getClass().getMethod("updateInventory", getClassFromPath(Path.MINECRAFT_SERVER, "Container")).invoke(entityPlayer, activeContainerVF.value());

    } catch(NoSuchFieldException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException  e) {
      e.printStackTrace();
    }


  }

}
