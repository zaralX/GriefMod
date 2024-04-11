package ru.zaralx.griefmod.commands;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import ru.zaralx.griefmod.GriefMod;
import ru.zaralx.griefmod.config.MainConfig;
import ru.zaralx.griefmod.utils.GriefModManager;

import java.util.ArrayList;
import java.util.List;

public class GriefModCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.hasPermission("griefmod.admin")) return true;

        if (args.length == 0 || args[0].equalsIgnoreCase("info")) {
            sender.sendMessage("§8      -    §7[§6GriefMod§7] §fИнформация    §8-");
            sender.sendMessage("§7 -> §b/griefmod §ereload §8- §fПерезагрузить конфиг");
            sender.sendMessage("§7 -> §b/griefmod §eon §a[Игрок] §8- §fВключает игроку грифмод навсегда (Запрещая все опасные действия)");
            sender.sendMessage("§7 -> §b/griefmod §eoff §a[Игрок] §8- §fВыключает игроку грифмод навсегда (Разрешая все опасные действия)");
            sender.sendMessage("§7 -> §b/griefmod §eauto §a[Игрок] §8- §fУстанавливает игроку грифмод в зависимости от наиграного времени (Для обновления )");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            MainConfig.reload();
            GriefMod.getInstance().getPlayerListener().update();
        }

        if (args.length < 2) return true;

        Player player = Bukkit.getPlayer(args[1]);

        if (player == null) {
            sender.sendMessage("§8cИгрок не найден!");
            return true;
        }

        FileConfiguration config = MainConfig.get();

        if (args[0].equalsIgnoreCase("on")) {
            GriefModManager.enableGrief(player);
            sender.sendMessage(config.getString("messages.prefix")+" Выдан игроку навсегда §6"+player.getName());
        } else if (args[0].equalsIgnoreCase("off")) {
            GriefModManager.disableGrief(player);
            sender.sendMessage(config.getString("messages.prefix")+" Снят у игрока навсегда §6"+player.getName());
        } else if (args[0].equalsIgnoreCase("auto")) {
            GriefModManager.deleteGrief(player);
            sender.sendMessage(config.getString("messages.prefix")+" Теперь автоматический у игрока §6"+player.getName());
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            for (String option : new String[]{"reload", "info", "on", "off", "auto"}) {
                if (option.startsWith(args[0])) {
                    completions.add(option);
                }
            }
        } else if (args.length == 2 && (args[0].equals("on") || args[0].equals("off") || args[0].equals("auto"))) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().startsWith(args[1])) {
                    completions.add(player.getName());
                }
            }
        }
        return completions;
    }
}
