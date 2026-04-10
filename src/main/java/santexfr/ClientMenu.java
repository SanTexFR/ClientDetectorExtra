package santexfr;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import santexfr.api.ClientDetectorAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"unused","UnusedReturnValue"})
public class ClientMenu implements Listener{
    //VARIABLES(STATICS)
    private static final int ITEMS_PER_PAGE=45;

    //UTILS
    private static@NotNull String getMsg(@NotNull String key) {
        return ClientDetectorExtra.getInstance().getRawMessage(key);
    }
    private static@NotNull ItemStack createItem(@NotNull Material mat,@NotNull String name){
        final ItemStack item=new ItemStack(mat);
        final ItemMeta meta=item.getItemMeta();
        if(meta!=null){
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static void addNavigation(@NotNull Inventory inv,int page,int totalItems,boolean isSubMenu){
        if(page>0)
            inv.setItem(45,createItem(Material.ARROW, getMsg("gui-previous-page")));
        
        if(isSubMenu)
            inv.setItem(49,createItem(Material.BARRIER, getMsg("gui-back-button")));
        
        if((page+1)*ITEMS_PER_PAGE<totalItems)
            inv.setItem(53,createItem(Material.ARROW,getMsg("gui-next-page")));
    }

    public static void openMainMenu(@NotNull Player player,int page) {
        final ClientDetectorAPI api=ClientDetectorExtra.getApi();
        final List<String> brands=new ArrayList<>(api.getActiveClientBrands());

        final String title=getMsg("gui-main-title").replace("%page%",String.valueOf(page+1));
        final Inventory inv=Bukkit.createInventory(null,54,title);

        int start=page*ITEMS_PER_PAGE;
        int end=Math.min(start+ITEMS_PER_PAGE,brands.size());

        for(int i=start;i<end;i++){
            final String brand=brands.get(i);
            final ItemStack item=new ItemStack(Material.BOOK);
            final ItemMeta meta=item.getItemMeta();
            meta.setDisplayName("§b"+brand);

            final  List<String>lore=ClientDetectorExtra.getInstance().getMessageList("gui-client-lore").stream()
                    .map(line->line.replace("%count%",String.valueOf(api.getPlayersByClientBrand(brand).size())))
                    .collect(Collectors.toList());

            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.addItem(item);
        }

        addNavigation(inv,page,brands.size(),false);
        player.openInventory(inv);
    }

    public static void openPlayerListMenu(@NotNull Player player,@NotNull String brand,int page) {
        final ClientDetectorAPI api=ClientDetectorExtra.getApi();
        final List<Player>players=api.getPlayersByClientBrand(brand);

        final String title=getMsg("gui-sub-title")
                .replace("%brand%",brand)
                .replace("%page%",String.valueOf(page+1));

        final Inventory inv=Bukkit.createInventory(null,54,title);

        final int start=page*ITEMS_PER_PAGE;
        final int end=Math.min(start+ITEMS_PER_PAGE,players.size());

        for(int i=start;i<end;i++){
            final Player target=players.get(i);
            final ItemStack head=new ItemStack(Material.PLAYER_HEAD);
            final SkullMeta meta=(SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(target);
            meta.setDisplayName("§e"+target.getName());

            final List<String>lore=ClientDetectorExtra.getInstance().getMessageList("gui-player-lore").stream()
                    .map(line->line.replace("%uuid%",target.getUniqueId().toString())
                            .replace("%is_bedrock%",api.isBedrock(target)?"Oui":"Non"))
                    .collect(Collectors.toList());

            meta.setLore(lore);
            head.setItemMeta(meta);
            inv.addItem(head);
        }

        addNavigation(inv,page,players.size(),true);
        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        final String title=e.getView().getTitle();
        final String mainRoot=getMsg("gui-main-title").split("%page%")[0];
        final String subRoot=getMsg("gui-sub-title").split("%brand%")[0];

        if(!title.startsWith(mainRoot)&&!title.startsWith(subRoot))return;

        e.setCancelled(true);
        final ItemStack current=e.getCurrentItem();
        if(current==null||!current.hasItemMeta())return;

        final Player player=(Player) e.getWhoClicked();
        final String name=current.getItemMeta().getDisplayName();

        int currentPage=0;
        try{
            final String[]parts=title.split("Page ");
            if(parts.length>1)currentPage=Integer.parseInt(parts[1].replaceAll("[^0-9]",""))-1;
        }catch(Exception ignored){}

        if(name.equals(getMsg("gui-previous-page"))){
            if (title.startsWith(mainRoot))openMainMenu(player,currentPage-1);
            else openPlayerListMenu(player,extractBrand(title,subRoot),currentPage-1);
        }
        else if(name.equals(getMsg("gui-next-page"))){
            if(title.startsWith(mainRoot))openMainMenu(player,currentPage+1);
            else openPlayerListMenu(player,extractBrand(title,subRoot),currentPage+1);
        }
        else if(name.equals(getMsg("gui-back-button"))){
            openMainMenu(player,0);
        }
        else if(title.startsWith(mainRoot)&&current.getType()==Material.BOOK){
            openPlayerListMenu(player,name.replace("§b",""),0);
        }
    }

    private@NotNull String extractBrand(@NotNull String title,@NotNull String root){
        return title.replace(root,"").split(" \\(")[0];
    }
}