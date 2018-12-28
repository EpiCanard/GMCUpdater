package fr.epicanard.gmcupdater.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;


public class CategoryHandler {
  YamlConfiguration config;
  public Set<String> categories;

  public CategoryHandler(YamlConfiguration conf) {
    this.config = conf;
    this.categories = this.config.getKeys(false);
  }

  private void addListItems(List<String> lst, String category) {
    final List<String> items = this.config.getStringList(category + ".Items");
    if (items != null)
      lst.addAll(items);
  }

  public String[] getItems(String category) {
    final List<String> lst = new ArrayList<String>();

    // When we want items from a specific category
    if (category != "!") {
      this.addListItems(lst, category);
      return (lst != null) ? lst.toArray(new String[0]) : null;
    }

    // When we want to get uncategorized items
    for (String cat : this.categories) {
      if (cat != "!")
        this.addListItems(lst, cat);
    }
    return lst.toArray(new String[0]);
  }

  public String getDisplayName(String category) {
    if (category == "!")
      return "Uncategorized";
    return this.config.getString(category + ".DisplayName");
  }

  public String getCategory(ItemStack item) {
    return "Unclassified";
  }

  public Boolean isCategory(String category) {
    if (this.categories == null)
      return false;
    return this.categories.contains(category);
  }
}
