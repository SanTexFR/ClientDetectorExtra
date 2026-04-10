package santexfr;

import com.cjcrafter.foliascheduler.FoliaCompatibility;
import com.cjcrafter.foliascheduler.ServerImplementation;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import santexfr.api.ClientDetectorAPI;
import santexfr.internal.ClientDetectorProvider;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"unused","UnusedReturnValue"})
public final class ClientDetectorExtra extends JavaPlugin{
    //VARIABLES(INSTANCES)
    private static ServerImplementation serverImplementation;

    private static ClientDetectorExtra instance;
    private static ClientDetectorAPI api;

    //VARIABLES(CONFIG)
    private boolean checkForUpdate;
    private boolean notifyConsole;
    private boolean notifyAdmin;
    private String adminPermission;
    private String cachedPrefix;
    private final@NotNull ConcurrentHashMap<@NotNull String,@NotNull String>msgCache=new ConcurrentHashMap<>();
    private final@NotNull ConcurrentHashMap<@NotNull String,@NotNull List<@NotNull String>>listMsgCache=new ConcurrentHashMap<>();

    //METHODS(INSTANCES)
    @Override
    public void onEnable(){
        instance=this;
        api=new ClientDetectorProvider();

        serverImplementation=new FoliaCompatibility(this).getServerImplementation();

        saveDefaultConfig();
        loadConfiguration();

        final var command=getCommand("clientdetector");
        if(command!=null){
            command.setExecutor(new ClientCommand());
            command.setTabCompleter(new ClientTabCompleter());
        }

        getServer().getPluginManager().registerEvents(new DetectorManager(),this);
        getServer().getPluginManager().registerEvents(new ClientMenu(),this);

        final Metrics metrics=new Metrics(this,30634);

        //UPDATER
        if(checkForUpdate)Updater.checkForUpdates();
    }

    @Override
    public void onDisable(){
        msgCache.clear();
    }

    //METHODS(STATICS)
    static@NotNull ClientDetectorExtra getInstance(){
        return instance;
    }
    static ServerImplementation getServerImplementation(){
        return serverImplementation;
    }

    //METHODS(CONFIG)
    public void loadConfiguration(){
        reloadConfig();
        this.checkForUpdate=getConfig().getBoolean("check-for-update",true);
        this.notifyConsole=getConfig().getBoolean("notify-console",true);
        this.notifyAdmin=getConfig().getBoolean("notify-admin",true);
        this.adminPermission=getConfig().getString("permission","clientdetector.admin");
        this.cachedPrefix=color(getConfig().getString("messages.prefix", "&8[&bClientDetector&8] &f"));

        msgCache.clear();
        listMsgCache.clear();
        final String prefix=getConfig().getString("messages.prefix","");

        final ConfigurationSection section=getConfig().getConfigurationSection("messages");
        if(section==null)return;

        for(final String key:section.getKeys(false)){
            if(key.equals("prefix"))continue;

            final String rawPath="messages."+key;
            final String rawMsg=getConfig().getString(rawPath,"");

            if(getConfig().isList(rawPath)){
                final List<String>coloredList=getConfig().getStringList(rawPath).stream()
                        .map(this::color)
                        .collect(java.util.stream.Collectors.toList());
                listMsgCache.put(key,coloredList);

                continue;
            }

            msgCache.put(key,color(rawMsg));
            msgCache.put(key+"_prefixed",color(prefix+rawMsg));
        }
    }
    private@NotNull String color(@NotNull String text){
        if(text.isEmpty())return"";

        try {
            return LegacyComponentSerializer.legacySection().serialize(
                    MiniMessage.miniMessage().deserialize(text)
            );
        }catch(Exception e){
            getLogger().warning("Erreur de format MiniMessage dans la config : "+text);
            return text;
        }
    }
    
    //API
    public@NotNull String getMessage(@NotNull String key){
        return msgCache.getOrDefault(key+"_prefixed","§cMissing msg: "+key);
    }
    public@NotNull String getRawMessage(@NotNull String key){
        return msgCache.getOrDefault(key,"§cMissing msg: "+key);
    }

    public@NotNull List<String>getMessageList(@NotNull String key){
        return listMsgCache.getOrDefault(key,java.util.Collections.singletonList("§cMissing list: "+key));
    }

    public@NotNull String getPrefix(){
        return this.cachedPrefix;
    }

    public boolean isNotifyConsole(){
        return this.notifyConsole;
    }
    public boolean isNotifyAdmin(){
        return this.notifyAdmin;
    }
    public@NotNull String getAdminPermission(){
        return this.adminPermission;
    }

    //METHODS(API)
    public static@NotNull ClientDetectorAPI getApi() {
        if(api==null)throw new IllegalStateException("ClientDetectorAPI isn't fully loaded!");
        return api;
    }
}