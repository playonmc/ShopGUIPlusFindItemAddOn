package mc.play.findshopitem;

import com.earth2me.essentials.Essentials;
import net.brcdev.shopgui.ShopGuiPlugin;
import net.brcdev.shopgui.ShopGuiPlusApi;
import net.brcdev.shopgui.exception.player.PlayerDataNotLoadedException;
import net.brcdev.shopgui.gui.gui.AmountSelectionGui;
import net.brcdev.shopgui.gui.gui.OpenGui;
import net.brcdev.shopgui.player.PlayerData;
import net.brcdev.shopgui.shop.Shop;
import net.brcdev.shopgui.shop.ShopManager;
import net.brcdev.shopgui.shop.item.ShopItem;
import net.brcdev.shopgui.shop.item.ShopItemType;
import net.brcdev.shopgui.util.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.ChatColor;

import java.util.Optional;

public class BuyItemCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /buyitem <block>");
            return true;
        }

        String blockName = args[0].toUpperCase();

        try {
            findAndOpenBuyGui(player, blockName);
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Invalid block type: " + blockName);
            return true;
        }

        return true;
    }

    private void findAndOpenBuyGui(Player player, String blockName) throws Exception {
        Essentials essentials = Essentials.getPlugin(Essentials.class);

        ItemStack itemStack = essentials.getItemDb().get(blockName);

        if (itemStack == null) {
            player.sendMessage(ChatColor.RED + "Invalid block name: " + blockName);
            return;
        }

        try {
            ShopGuiPlugin plugin = ShopGuiPlusApi.getPlugin();

            ShopItem shopItem = null;
            for (Shop shop : plugin.getShopManager().getShops()) {
                Optional<ShopItem> shopObj = shop.getShopItems()
                        .stream()
                        .filter(shopItemObj -> (shopItemObj.getItem().getType() == itemStack.getType()) && shopItemObj.getType() == ShopItemType.ITEM)
                        .findFirst();

                if (shopObj.isPresent()) {
                    shopItem = shopObj.get();
                    break;
                }
            }

            if(shopItem == null) {
                player.sendMessage(ChatColor.RED + "Item " + itemStack.getType().name() + " is not available for purchase in any shop!");
                return;
            }

            // Get or create PlayerData
            PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);

            // Create the ItemStack (clone from shop item)
            ItemStack shopStack = shopItem.getItem().clone();
            shopStack.setAmount(1); // Default amount

            // Create and open the AmountSelectionGui
            AmountSelectionGui amountGui = new AmountSelectionGui(
                    player,
                    playerData,
                    OpenGui.MenuType.AMOUNT_SELECTION,
                    shopItem.getShop(),
                    ShopManager.ShopAction.BUY,
                    shopItem,
                    shopStack
            );

            // Open the inventory using InventoryUtils
            InventoryUtils.openInventory(player, playerData, amountGui.getInventory(), amountGui);
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Error opening buy screen: " + e.getMessage());
        }
    }
}