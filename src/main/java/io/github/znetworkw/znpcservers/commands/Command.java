package io.github.znetworkw.znpcservers.commands;

import com.google.common.collect.Iterables;
import io.github.znetworkw.znpcservers.cache.CacheRegistry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.defaults.BukkitCommand;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Represents a command.
 *
 * <p>
 * To create a new sub command for a {@link Command}, create a method
 * with the parameters: ({@link CommandSender}, {@link Map<String>})
 * annotated as {@link CommandInformation}.
 * example:
 * <pre>
 * @CommandInformation(arguments = {"hello"}, name = "subCommandName", permission = "permission")
 * void exampleCommand(CommandSender sender, Map<String, String> args) {
 * // code
 * }
 * </pre>
 *
 * @see CommandInformation
 * @see CommandSender
 */
public class Command extends BukkitCommand {
    /**
     * A string whitespace.
     */
    private static final String WHITESPACE = " ";

    /**
     * The bukkit command map instance.
     */
    private static final CommandMap COMMAND_MAP;

    static {
        try {
            COMMAND_MAP = (CommandMap) CacheRegistry.BUKKIT_COMMAND_MAP.get(Bukkit.getServer());
        } catch (IllegalAccessException exception) {
            // should not happen....
            throw new IllegalStateException("can't access bukkit command map.");
        }
    }

    /**
     * A map that contains the subcommands for the current command.
     */
    private final Map<CommandInformation, CommandInvoker> subCommands;

    /**
     * Creates a new {@link Command} with the given {@code name}.
     */
    public Command(String name) {
        super(name);
        subCommands = new HashMap<>();
        load();
    }

    /**
     * Loads the command.
     */
    private void load() {
        // register the command
        COMMAND_MAP.register(getName(), this);
        // load sub commands
        for (Method method : getClass().getMethods()) {
            if (method.isAnnotationPresent(CommandInformation.class)) {
                CommandInformation cmdInfo = method.getAnnotation(CommandInformation.class);
                subCommands.put(cmdInfo, new CommandInvoker(this, method, cmdInfo.permission()));
            }
        }
    }

    /**
     * Converts the provided subcommand arguments to a map.
     *
     * @param subCommand The subcommand.
     * @param args       The subcommand arguments.
     * @return A map with the subcommand arguments for the provided values.
     */
    private Map<String, String> loadArgs(CommandInformation subCommand,
                                         Iterable<String> args) {
        int size = Iterables.size(args);
        int subCommandsSize = subCommand.arguments().length;
        Map<String, String> argsMap = new HashMap<>();
        if (size > 1) {
            if (subCommand.isMultiple()) {
                argsMap.put(Iterables.get(args, 1), String.join(WHITESPACE, Iterables.skip(args, 2)));
            } else {
                for (int i = 0; i < Math.min(subCommandsSize, size); i++) {
                    int fixedLength = i + 1;
                    if (size > fixedLength) {
                        String input = Iterables.get(args, fixedLength);
                        if (fixedLength == subCommandsSize) {
                            input = String.join(WHITESPACE, Iterables.skip(args, subCommandsSize));
                        }
                        argsMap.put(subCommand.arguments()[i], input);
                    }
                }
            }
        }
        return argsMap;
    }

    /**
     * Returns a set containing the subcommands on the map.
     *
     * @return A set containing the subcommands on the map.
     */
    public Set<CommandInformation> getCommands() {
        return subCommands.keySet();
    }

    @Override
    public boolean execute(org.bukkit.command.CommandSender sender, String commandLabel, String[] args) {
        Optional<Map.Entry<CommandInformation, CommandInvoker>> subCommandOptional =
            subCommands.entrySet().stream()
                .filter(command -> command.getKey().name().contentEquals(args.length > 0 ? args[0] : ""))
                .findFirst();

        if (!subCommandOptional.isPresent()) {
            sender.sendMessage(ChatColor.RED + "can't find command: " + commandLabel + ".");
            return false;
        }

        try {
            Map.Entry<CommandInformation, CommandInvoker> subCommand = subCommandOptional.get();
            subCommand.getValue().execute(new CommandSender(sender), loadArgs(subCommand.getKey(), Arrays.asList(args)));
        } catch (CommandExecuteException e) {
            sender.sendMessage(ChatColor.RED + "can't execute command.");
            // Logs enabled
            e.printStackTrace();
        } catch (CommandPermissionException e) {
            sender.sendMessage(ChatColor.RED + "no permission for run this command.");
        }
        return true;
    }
}
