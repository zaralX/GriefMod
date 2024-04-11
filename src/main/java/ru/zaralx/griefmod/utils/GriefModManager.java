package ru.zaralx.griefmod.utils;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import org.bukkit.Statistic;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import ru.zaralx.griefmod.GriefMod;
import ru.zaralx.griefmod.config.MainConfig;

import java.util.concurrent.ExecutionException;

public class GriefModManager {
    private static final LuckPerms luckPermsApi = GriefMod.getInstance().getLuckPermsApi();
    private static final FileConfiguration config = MainConfig.get();
    private static final BukkitAudiences adventure = GriefMod.getInstance().getAdventure();
    public static void updateGrief(Player player) {
        if (player.getStatistic(Statistic.PLAY_ONE_MINUTE)/20/60 >= config.getInt("grief-disable-minutes")) {
            if (emptyGriefPermission(player)) {
                enableGrief(player);
            }
        }
    }

    private static boolean emptyGriefPermission(Player player) {
        try {
            return luckPermsApi.getUserManager().loadUser(player.getUniqueId()).thenApplyAsync(user -> user.getNodes(NodeType.PERMISSION).stream()
                    .noneMatch(permissionNode -> permissionNode.getPermission().equals("griefmod.player"))).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean canGrief(Player player) {
        try {
            return luckPermsApi.getUserManager().loadUser(player.getUniqueId()).thenApplyAsync(user -> {
                if (emptyGriefPermission(player)) {
                    return false;
                } else {
                    return player.hasPermission("griefmod.player");
                }
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void enableGrief(Player player) {
        luckPermsApi.getUserManager().modifyUser(player.getUniqueId(), user -> {
            user.data().add(Node.builder("griefmod.player").value(true).build());
            adventure.player(player).sendMessage(
                    message("grief-change.enabled-message", player)
            );
        });
    }

    public static void disableGrief(Player player) {
        luckPermsApi.getUserManager().modifyUser(player.getUniqueId(), user -> {
            user.data().add(Node.builder("griefmod.player").value(false).build());
        });
        adventure.player(player).sendMessage(
                message("grief-change.disabled-message", player)
        );
    }

    public static void deleteGrief(Player player) {
        luckPermsApi.getUserManager().modifyUser(player.getUniqueId(), user -> {
            user.data().remove(Node.builder("griefmod.player").build());
        });
    }

    private static Component message(String messagePath, Player player) {
        return Component.text(config.getString("messages.prefix")+" ").append(MiniMessage.miniMessage().deserialize(config.getString("messages."+messagePath).replaceAll("%PLAYER%", player.getName())));
    }
}
