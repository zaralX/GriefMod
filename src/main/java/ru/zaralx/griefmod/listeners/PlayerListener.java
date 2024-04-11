package ru.zaralx.griefmod.listeners;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import ru.zaralx.griefmod.GriefMod;
import ru.zaralx.griefmod.config.MainConfig;
import ru.zaralx.griefmod.utils.GriefModManager;

import java.util.Set;
import java.util.stream.Collectors;

public class PlayerListener implements Listener {
    private final MiniMessage miniMessage;
    private FileConfiguration config;
    private final BukkitAudiences adventure;
    private Set<Material> disabledPlaceMaterials;
    private Set<Material> disabledDestroyMaterials;
    private Set<Material> disabledInteractUseMaterials;
    public PlayerListener() {
        this.miniMessage = MiniMessage.miniMessage();
        this.adventure = GriefMod.getInstance().getAdventure();
        update();
        Bukkit.getPluginManager().registerEvents(this, GriefMod.getInstance());
    }

    public void update() {
        this.config = MainConfig.get();
        disabledPlaceMaterials = config.getStringList("grief-disabled.place")
                .stream()
                .map(String::toUpperCase)
                .map(Material::valueOf)
                .collect(Collectors.toSet());

        disabledDestroyMaterials = config.getStringList("grief-disabled.destroy")
                .stream()
                .map(String::toUpperCase)
                .map(Material::valueOf)
                .collect(Collectors.toSet());

        disabledInteractUseMaterials = config.getStringList("grief-disabled.interact.use")
                .stream()
                .map(String::toUpperCase)
                .map(Material::valueOf)
                .collect(Collectors.toSet());
    }

    private boolean isGriefMod(Player player) {
        if (GriefModManager.canGrief(player)) {
            return true;
        }
        Component message = miniMessage.deserialize(config.getString("messages.disabled-actionbar"));
        adventure.player(player).sendActionBar(message);
        return false;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        GriefModManager.updateGrief(player);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Material material = event.getBlock().getType();
        if (disabledPlaceMaterials.contains(material)) {
            Player player = event.getPlayer();
            boolean isGrief = isGriefMod(player);
            event.setCancelled(!isGrief);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Material material = event.getBlock().getType();
        if (disabledDestroyMaterials.contains(material)) {
            Player player = event.getPlayer();
            boolean isGrief = isGriefMod(player);
            event.setCancelled(!isGrief);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (disabledInteractUseMaterials.contains(event.getMaterial())) {
            Player player = event.getPlayer();
            boolean isGrief = isGriefMod(player);
            event.setCancelled(!isGrief);
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Material bucket = event.getBucket();
        boolean lavaGriefDisabled = config.getBoolean("grief-disabled.bucket.empty.lava");
        boolean waterGriefDisabled = config.getBoolean("grief-disabled.bucket.empty.water");

        if (
                (bucket == Material.LAVA_BUCKET && lavaGriefDisabled) ||
                (bucket == Material.WATER_BUCKET && waterGriefDisabled)
        ) {
            if (event.getBlock().getType().equals(Material.AIR)) {
                Player player = event.getPlayer();
                boolean isGrief = isGriefMod(player);
                event.setCancelled(!isGrief);
            }
        }
    }

    @EventHandler
    public void onPlayerDamagePlayer(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            if (config.getBoolean("grief-disabled.disable-pvp")) {
                Player player = (Player) event.getDamager();
                Player target = (Player) event.getEntity();

                boolean isGrief = isGriefMod(player);
                event.setCancelled(!isGrief);
            }
        }
    }
}
