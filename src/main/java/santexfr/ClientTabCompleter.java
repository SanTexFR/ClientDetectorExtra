package santexfr;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class ClientTabCompleter implements org.bukkit.command.TabCompleter{
    @Override
    public@Nullable List<@NotNull String>onTabComplete(@NotNull CommandSender s,@NotNull Command c,@NotNull String l,@NotNull String[]args){
        if(args.length==1)return Arrays.asList("info","reload","gui");
        if(args.length==2&&args[0].equalsIgnoreCase("info"))return null;
        return Collections.emptyList();
    }
}