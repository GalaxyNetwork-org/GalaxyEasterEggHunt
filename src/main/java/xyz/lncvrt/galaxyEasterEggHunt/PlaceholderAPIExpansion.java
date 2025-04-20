package xyz.lncvrt.galaxyEasterEggHunt;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaceholderAPIExpansion extends PlaceholderExpansion {
    private final GalaxyEasterEggHunt plugin;

    public PlaceholderAPIExpansion(GalaxyEasterEggHunt plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public String getAuthor() {
        return "Lncvrt";
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "galaxyeasteregghunt";
    }

    @Override
    @NotNull
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        switch (params.toLowerCase()) {
            case "totalfound":
                return "%s".formatted(plugin.getTotalFindings(player));
            default:
                break;
        }
        return null;
    }
}