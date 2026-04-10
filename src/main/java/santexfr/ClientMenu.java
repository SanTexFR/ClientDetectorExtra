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
import santexfr.api.ClientDetectorAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClientMenu implements Listener {

    private static final int ITEMS_PER_PAGE = 45;

    private static String getMsg(String key) {
        return ClientDetectorExtra.getInstance().getRawMessage(key);
    }

    private static ItemStack createItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static void addNavigation(Inventory inv, int page, int totalItems, boolean isSubMenu) {
        // Bouton Précédent (Slot 45)
        if (page > 0) {
            inv.setItem(45, createItem(Material.ARROW, getMsg("gui-previous-page")));
        }

        // Bouton Retour (Slot 49)
        if (isSubMenu) {
            inv.setItem(49, createItem(Material.BARRIER, getMsg("gui-back-button")));
        }

        // Bouton Suivant (Slot 53)
        if ((page + 1) * ITEMS_PER_PAGE < totalItems) {
            inv.setItem(53, createItem(Material.ARROW, getMsg("gui-next-page")));
        }
    }

    public static void openMainMenu(Player player, int page) {
        ClientDetectorAPI api = ClientDetectorExtra.getApi();
        List<String> brands = new ArrayList<>(api.getActiveClientBrands());

        String title = getMsg("gui-main-title").replace("%page%", String.valueOf(page + 1));
        Inventory inv = Bukkit.createInventory(null, 54, title);

        int start = page * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, brands.size());

        for (int i = start; i < end; i++) {
            String brand = brands.get(i);
            ItemStack item = new ItemStack(Material.BOOK);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§b" + brand);

            List<String> lore = ClientDetectorExtra.getInstance().getMessageList("gui-client-lore").stream()
                    .map(line -> line.replace("%count%", String.valueOf(api.getPlayersByClientBrand(brand).size())))
                    .collect(Collectors.toList());

            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.addItem(item);
        }

        addNavigation(inv, page, brands.size(), false);
        player.openInventory(inv);
    }

    public static void openPlayerListMenu(Player player, String brand, int page) {
        ClientDetectorAPI api = ClientDetectorExtra.getApi();
        List<Player> players = api.getPlayersByClientBrand(brand);

        String title = getMsg("gui-sub-title")
                .replace("%brand%", brand)
                .replace("%page%", String.valueOf(page + 1));

        Inventory inv = Bukkit.createInventory(null, 54, title);

        int start = page * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, players.size());

        for (int i = start; i < end; i++) {
            Player target = players.get(i);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(target);
            meta.setDisplayName("§e" + target.getName());

            List<String> lore = ClientDetectorExtra.getInstance().getMessageList("gui-player-lore").stream()
                    .map(line -> line.replace("%uuid%", target.getUniqueId().toString())
                            .replace("%is_bedrock%", api.isBedrock(target) ? "Oui" : "Non"))
                    .collect(Collectors.toList());

            meta.setLore(lore);
            head.setItemMeta(meta);
            inv.addItem(head);
        }

        addNavigation(inv, page, players.size(), true);
        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        String title = e.getView().getTitle();
        // On vérifie si le titre correspond à l'un de nos menus (via la config)
        String mainRoot = getMsg("gui-main-title").split("%page%")[0];
        String subRoot = getMsg("gui-sub-title").split("%brand%")[0];

        if (!title.startsWith(mainRoot) && !title.startsWith(subRoot)) return;

        e.setCancelled(true);
        ItemStack current = e.getCurrentItem();
        if (current == null || !current.hasItemMeta()) return;

        Player player = (Player) e.getWhoClicked();
        String name = current.getItemMeta().getDisplayName();

        int currentPage = 0;
        try {
            // Extraction robuste de la page
            String[] parts = title.split("Page ");
            if (parts.length > 1) currentPage = Integer.parseInt(parts[1].replaceAll("[^0-9]", "")) - 1;
        } catch (Exception ignored) {}

        if (name.equals(getMsg("gui-previous-page"))) {
            if (title.startsWith(mainRoot)) openMainMenu(player, currentPage - 1);
            else openPlayerListMenu(player, extractBrand(title, subRoot), currentPage - 1);
        }
        else if (name.equals(getMsg("gui-next-page"))) {
            if (title.startsWith(mainRoot)) openMainMenu(player, currentPage + 1);
            else openPlayerListMenu(player, extractBrand(title, subRoot), currentPage + 1);
        }
        else if (name.equals(getMsg("gui-back-button"))) {
            openMainMenu(player, 0);
        }
        else if (title.startsWith(mainRoot) && current.getType() == Material.BOOK) {
            openPlayerListMenu(player, name.replace("§b", ""), 0);
        }
    }

    private String extractBrand(String title, String root) {
        return title.replace(root, "").split(" \\(")[0];
    }
}