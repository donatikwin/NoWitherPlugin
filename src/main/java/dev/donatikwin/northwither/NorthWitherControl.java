package dev.donatikwin.northwither;

import java.util.List;
import java.util.Locale;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class NorthWitherControl extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {

    private static final String ADMIN_PERMISSION = "northwither.admin";
    private static final List<String> COMMAND_ARGUMENTS = List.of("true", "false", "reload");

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);

        var command = getCommand("nowither");
        if (command == null) {
            throw new IllegalStateException("Команда nowither не объявлена в plugin.yml.");
        }

        command.setExecutor(this);
        command.setTabCompleter(this);
        getLogger().info("NorthWitherControl успешно запущен.");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getEntityType() == EntityType.WITHER
                && getConfig().getBoolean("wither-disabled", true)) {
            event.setCancelled(true);
        }
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(getMsg("no-permission"));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(getMsg("usage"));
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "true" -> {
                getConfig().set("wither-disabled", true);
                saveConfig();
                sender.sendMessage(getMsg("blocked"));
            }
            case "false" -> {
                getConfig().set("wither-disabled", false);
                saveConfig();
                sender.sendMessage(getMsg("unblocked"));
            }
            case "reload" -> {
                reloadConfig();
                sender.sendMessage(getMsg("reloaded"));
            }
            default -> sender.sendMessage(getMsg("usage"));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(
            CommandSender sender,
            Command command,
            String alias,
            String[] args
    ) {
        if (!sender.hasPermission(ADMIN_PERMISSION) || args.length != 1) {
            return List.of();
        }

        String input = args[0].toLowerCase(Locale.ROOT);
        return COMMAND_ARGUMENTS.stream()
                .filter(argument -> argument.startsWith(input))
                .toList();
    }

    public String getMsg(String path) {
        String prefix = getConfig().getString("messages.prefix", "");
        String message = getConfig().getString("messages." + path, "");
        return ChatColor.translateAlternateColorCodes('&', prefix + message);
    }
}
