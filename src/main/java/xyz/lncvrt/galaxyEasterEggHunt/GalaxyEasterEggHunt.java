package xyz.lncvrt.galaxyEasterEggHunt;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;

public final class GalaxyEasterEggHunt extends JavaPlugin implements Listener {
    private File findingsFile;
    private FileConfiguration findingsConfig;
    HashMap<Player, Long> playerTimestamps = new HashMap<>();
    private final PlaceholderAPIExpansion placeholderAPIExpansion = new PlaceholderAPIExpansion(this);

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        findingsFile = new File(getDataFolder(), "findings.yml");
        if (!findingsFile.exists()) {
            try {
                findingsFile.getParentFile().mkdirs();
                findingsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        findingsConfig = YamlConfiguration.loadConfiguration(findingsFile);

        placeholderAPIExpansion.register();
    }

    @Override
    public void onDisable() {
        placeholderAPIExpansion.unregister();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String path = player.getName();
        if (!findingsConfig.contains(path)) {
            findingsConfig.set(path, new HashMap<String, Object>());
            try {
                findingsConfig.save(findingsFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onServerTickEndEvent(ServerTickEndEvent event) {
        for (Player player : getServer().getOnlinePlayers()) {
            if (getTotalFindings(player) == 12) return;
            Location loc = player.getLocation();
            int radius = 10;
            boolean found = false;
            for (int x = -radius; x <= radius && !found; x++) {
                for (int y = -radius; y <= radius && !found; y++) {
                    for (int z = -radius; z <= radius && !found; z++) {
                        Location checkLoc = loc.clone().add(x, y, z);
                        if (checkLoc.getBlock().getType() == Material.PLAYER_HEAD && !hasFound(player, checkLoc.getBlock().getLocation())) {
                            found = true;
                        }
                    }
                }
            }
            if (found) player.sendActionBar(MiniMessage.miniMessage().deserialize("<color:#A48FD1><b>CLOSE TO AN EASTER EGG"));
            else player.sendActionBar(MiniMessage.miniMessage().deserialize("<color:#D18FA4><b>NOT CLOSE TO AN EASTER EGG"));
        }
    }

    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        MiniMessage miniMessage = MiniMessage.miniMessage();
        if (block == null) return;
        if (block.getType() == Material.PLAYER_HEAD) {
            if (playerTimestamps.containsKey(player) && System.currentTimeMillis() - playerTimestamps.get(player) < 1000) return;
            playerTimestamps.put(player, System.currentTimeMillis());

            int totalFindings = getTotalFindings(player);

            if (!storeFinding(player, block.getLocation())) {
                player.sendMessage(miniMessage.deserialize("<red>You have already found that egg! (%s left)".formatted(12-totalFindings)));
                return;
            }
            totalFindings = totalFindings+1;
            player.sendMessage(miniMessage.deserialize("<color:#A48FD1><b>%s/12 eggs found, %s left to go!".formatted(totalFindings, 12-totalFindings)));
            if (totalFindings == 12) {
                player.sendMessage(miniMessage.deserialize("<color:#D18FA4><b>Congrats! You now have the <gradient:#FBC2EB:#A6C1EE>BUNNY</gradient> tag!"));
                getServer().dispatchCommand(getServer().getConsoleSender(), "lp user %s parent add bunny".formatted(player.getName()));
            }
        }
    }

    private boolean hasFound(Player player, Location location) {
        String encoded = encodeLocation(location);
        return findingsConfig.getBoolean(player.getName() + "." + encoded, false);
    }

    private boolean storeFinding(Player player, Location location) {
        String encoded = encodeLocation(location);
        String path = player.getName() + "." + encoded;

        if (findingsConfig.contains(path)) {
            return false;
        }

        findingsConfig.set(path, true);
        try {
            findingsConfig.save(findingsFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getTotalFindings(Player player) {
        int count = 0;
        ConfigurationSection section = findingsConfig.getConfigurationSection(player.getName());
        if (section == null) return 0;

        for (String key : section.getKeys(false)) {
            if (findingsConfig.getBoolean(player.getName() + "." + key, false)) count++;
        }

        return count;
    }

    private String encodeLocation(Location location) {
        String str = location.getX() + ":" + location.getY() + ":" + location.getZ() + ":" + location.getWorld().getName();
        return Base64.getEncoder().encodeToString(str.getBytes());
    }

    private Location decodeLocation(String encoded) {
        String decoded = new String(Base64.getDecoder().decode(encoded));
        String[] parts = decoded.split(":");
        return new Location(getServer().getWorld(parts[3]), Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
    }
}
