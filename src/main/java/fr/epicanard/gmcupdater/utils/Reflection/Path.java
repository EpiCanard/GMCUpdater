package fr.epicanard.gmcupdater.utils.Reflection;

import org.bukkit.Bukkit;

public enum Path {
  BUKKIT("org.bukkit.craftbukkit" + version()),
  MINECRAFT_RESOURCES("net.minecraft.resources"),
  MINECRAFT_WORLD_ITEM("net.minecraft.world.item"),
  MINECRAFT_CORE_REGISTRIES("net.minecraft.core.registries"),
  MINECRAFT_CORE("net.minecraft.core");

  final String path;

  Path(String path) {
    this.path = path;
  }

  private static String version() {
    try {
      return "." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    } catch (ArrayIndexOutOfBoundsException e) {
      return "";
    }
  }

  public Class<?> getClass(String className) throws ClassNotFoundException {
    return Class.forName(this.getPath(className));
  }

  public String getPath(String className) {
    return this.path + "." + className;
  }
}
