package me.lucacicada.spigot.vanillasort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * VanillaSort java plugin
 */
public class Plugin extends JavaPlugin implements Listener {

    /**
     * The custom name of the sorter chest.
     */
    private static final String CUSTOM_SORTER_NAME = "Sorter";

    private static final Logger LOGGER = Logger.getLogger("VanillaSort");

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        LOGGER.info("VanillaSort Enabled");
    }

    @Override
    public void onDisable() {
        LOGGER.info("VanillaSort Disabled");
    }

    /**
     * Called when a player closes an inventory.
     * <p>
     *
     * Check for:
     * <ul>
     * <li> The inventory is a trapped chest</li>
     * <li> The trapped chest has a custom name</li>
     * <li> Find all chests and barrels in a 16x16x5 area around the chest</li>
     * </ul>
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = event.getPlayer() instanceof Player ? (Player) event.getPlayer() : null;

        if (player == null) {
            return;
        }

        Inventory inventory = event.getInventory();

        if (inventory.isEmpty()) {
            return;
        }

        InventoryHolder holder = inventory.getHolder();

        Chest chest = holder instanceof Chest ? (Chest) holder : null;

        if (chest == null) {
            return;
        }

        // Only allow trapped chests
        if (chest.getBlock().getType() != Material.TRAPPED_CHEST) {
            return;
        }

        // Only allow chests with a special custom name
        if (!CUSTOM_SORTER_NAME.equals(chest.getCustomName())) {
            return;
        }

        Location chestLocation = chest.getLocation();
        World world = chestLocation.getWorld();

        if (world == null) {
            return;
        }

        HashSet<Integer> targetContainersHashSet = new HashSet<>();
        List<TargetContainer> targetContainers = new ArrayList<>();

        // Find all the blocks in an area around the chest
        final int RADIUS_XZ = 16;
        final int RADIUS_Y = 5;

        for (int x = -RADIUS_XZ; x <= RADIUS_XZ; x++) {
            for (int z = -RADIUS_XZ; z <= RADIUS_XZ; z++) {
                for (int y = -RADIUS_Y; y <= RADIUS_Y; y++) {
                    Location location = chestLocation.clone().add(x, y, z);
                    Block block = world.getBlockAt(location);
                    BlockState blockState = block.getState();

                    Container container = blockState instanceof Container ? (Container) blockState : null;

                    if (container == null) {
                        continue;
                    }

                    // Only allow chests and barrels
                    if (container.getType() != Material.CHEST && container.getType() != Material.BARREL) {
                        continue;
                    }

                    // Ignore duplicates
                    if (!targetContainersHashSet.add(container.hashCode())) {
                        continue;
                    }

                    targetContainers.add(new TargetContainer(container));
                }
            }
        }

        /*
        for (ItemFrame frame : world.getEntitiesByClass(ItemFrame.class)) {
            Block targetBlock = frame.getLocation().getBlock().getRelative(frame.getAttachedFace());

            if (targetBlock == null) {
                continue;
            }

            BlockState targetBlockState = targetBlock.getState();

            Container targetContainer = targetBlockState instanceof Container ? (Container) targetBlockState : null;

            if (targetContainer == null) {
                continue;
            }

            // Only allow chests and barrels
            if (targetContainer.getType() != Material.CHEST && targetContainer.getType() != Material.BARREL) {
                continue;
            }

            Location targetLocation = targetContainer.getLocation();

            // Max vertical distance of 5 blocks
            if (Math.abs(targetLocation.getY() - chestLocation.getY()) >= 5.0) {
                continue;
            }

            // Max horizontal distance of 16 blocks
            if (Math.abs(targetLocation.getX() - chestLocation.getX()) >= 16.0
                    || Math.abs(targetLocation.getZ() - chestLocation.getZ()) >= 16.0) {
                continue;
            }

            // Ignore duplicates
            if (!targetContainersHashSet.add(targetContainer.hashCode())) {
                continue;
            }

            targetContainers.add(new TargetContainer(targetContainer));
        } */
        // There is nowhere to move the items
        if (targetContainers.isEmpty()) {
            return;
        }

        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];

            if (item == null || item.getType().isAir()) {
                continue;
            }

            Material itemType = item.getType();

            Collections.sort(targetContainers, (TargetContainer a, TargetContainer b) -> {
                int materialAmountComparison = Long.compare(
                        b.getMaterialAmount(itemType),
                        a.getMaterialAmount(itemType)
                );

                // Get the one that have the most material on the item type
                if (materialAmountComparison != 0) {
                    return materialAmountComparison;
                }

                int occupiedSlotsComparison = Long.compare(
                        b.getOccupiedSlots(),
                        a.getOccupiedSlots()
                );

                // get the one that have the most occupied slots
                if (occupiedSlotsComparison != 0) {
                    return occupiedSlotsComparison;
                }

                int itemsAmountCountComparison = Long.compare(
                        b.getItemsAmountCount(),
                        a.getItemsAmountCount()
                );

                // Get the one that have the most item amount count
                if (itemsAmountCountComparison != 0) {
                    return itemsAmountCountComparison;
                }

                // Get the closet one to the sorter chest
                return Double.compare(
                        a.getContainer().getLocation().distanceSquared(chestLocation),
                        b.getContainer().getLocation().distanceSquared(chestLocation)
                );
            });

            for (TargetContainer targetContainer : targetContainers) {
                HashMap<Integer, ItemStack> overflow = targetContainer.getContainer().getInventory().addItem(item);

                // We have moved all the items
                if (overflow.isEmpty()) {
                    inventory.clear(i);
                    // player.playSound(targetContainer.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
                    break;
                }

                // Update the inventory slot with the remaining items
                // NOTE: overflow always contains exactly 1 item at this point
                for (ItemStack overflowItem : overflow.values()) {
                    inventory.setItem(i, overflowItem);
                }
            }
        }
    }

    private static final class TargetContainer {

        @Nonnull
        private final Container container;

        public TargetContainer(@Nonnull Container container) {
            this.container = container;
        }

        @Nonnull
        public Container getContainer() {
            return this.container;
        }

        /**
         * Returns the number of occupied slots in the container.
         */
        public long getOccupiedSlots() {
            return Arrays.stream(this.container.getInventory().getContents())
                    .filter(item -> item != null && !item.getType().isAir())
                    .count();
        }

        /**
         * Return a map of item material and the amounts in the container.
         *
         * @return
         */
        public Integer getMaterialAmount(Material material) {
            return Arrays.stream(this.container.getInventory().getContents())
                    .filter(item -> item != null && item.getType() == material)
                    .mapToInt(ItemStack::getAmount)
                    .sum();
        }

        /**
         * Return a map of item material and the amounts in the container.
         *
         * @return
         */
        public Integer getItemsAmountCount() {
            return Arrays.stream(this.container.getInventory().getContents())
                    .filter(item -> item != null)
                    .mapToInt(ItemStack::getAmount)
                    .sum();
        }
    }
}
