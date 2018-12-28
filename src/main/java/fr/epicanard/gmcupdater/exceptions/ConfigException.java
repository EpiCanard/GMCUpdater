package fr.epicanard.gmcupdater.exceptions;

public class ConfigException extends Exception {

  static final long serialVersionUID = -7914157672976633808L;

  public ConfigException(String message) {
    super("[Configuration]" + message);
  }
}
