package com.winthier.featherweight;

import java.util.HashMap;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class FeatherweightPlugin extends JavaPlugin implements Listener {
    final ItemStack featherItem = new ItemStack(Material.FEATHER);
    
    @Override public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        final Player player = event.getPlayer();
        if (!(event.getRightClicked() instanceof LivingEntity)) return;
        final LivingEntity entity = (LivingEntity)event.getRightClicked();
        if (player.getPassenger() == entity) {
            event.setCancelled(true);
            player.eject();
            return;
        }
        final ItemStack item = player.getItemInHand();
        if (!featherItem.isSimilar(item)) return;
        // Check entity type
        switch (entity.getType()) {
        case ARMOR_STAND:
        case PLAYER:
        case ENDER_DRAGON:
            return;
        default:
            break;
        }
        // Check passengers
        if (player.getPassenger() != null) return;
        if (entity.getPassenger() != null) return;
        if (player.isInsideVehicle()) return;
        if (entity.isInsideVehicle()) return;
        // Check tamer
        if (entity instanceof Tameable) {
            final Tameable tamed = (Tameable)entity;
            if (tamed.isTamed() && tamed.getOwner() != null && !tamed.getOwner().equals(player)) {
                return;
            }
        }
        // Fake damage
        @SuppressWarnings("deprecation")
            final EntityDamageByEntityEvent fakeEvent = new EntityDamageByEntityEvent(player, entity, EntityDamageEvent.DamageCause.CUSTOM, 0.0);
        getServer().getPluginManager().callEvent(fakeEvent);
        if (fakeEvent.isCancelled()) return;
        // Cancel event
        event.setCancelled(true);
        // Reduce feather
        if (player.getGameMode() != GameMode.CREATIVE) {
            int amount = item.getAmount() - 1;
            if (amount <= 0) {
                player.setItemInHand(null);
            } else {
                item.setAmount(amount);
            }
        }
        // Mount
        player.setPassenger(entity);
        player.getWorld().playSound(entity.getLocation(), Sound.HORSE_ARMOR, 1, 1);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        final Player player = event.getPlayer();
        if (player.isInsideVehicle()) return;
        if (player.getPassenger() == null) return;
        player.eject();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        final Player player = (Player)event.getEntity();
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (player.getPassenger() == null) return;
        if (player.getPassenger().getType() != EntityType.CHICKEN) return;
        event.setCancelled(true);
    }
}
