package ru.zaralx.griefmod;

import lombok.Getter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.zaralx.griefmod.commands.GriefModCommand;
import ru.zaralx.griefmod.config.MainConfig;
import ru.zaralx.griefmod.listeners.PlayerListener;
import ru.zaralx.griefmod.utils.GriefModManager;

public final class GriefMod extends JavaPlugin {
    @Getter
    private static GriefMod instance;
    @Getter
    private LuckPerms luckPermsApi;
    @Getter
    private BukkitAudiences adventure;
    @Getter
    private PlayerListener playerListener;
    @Override
    public void onEnable() {
        instance = this;
        this.adventure = BukkitAudiences.create(this);

        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPermsApi = provider.getProvider();
        }

        MainConfig.setup();

        getCommand("griefmod").setExecutor(new GriefModCommand());

        playerListener = new PlayerListener();

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    GriefModManager.updateGrief(player);
                }
            }
        }.runTaskTimer(this, 0, (long) MainConfig.get().getInt("grief-auto-check") * 20 * 60);
    }

    @Override
    public void onDisable() {
        if(this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
    }
}
