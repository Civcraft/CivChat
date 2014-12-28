package com.untamedears.civchat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import vg.civcraft.mc.namelayer.group.Group;

/*
 * Coded by Rourke750 & ibbignerd
 */
public class ChatListener implements Listener {

    private ChatManager chat;

    public ChatListener(ChatManager instance) {
        chat = instance;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent PlayerDeathEvent) {
        PlayerDeathEvent.setDeathMessage(null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerKick(PlayerKickEvent event) {
        event.setLeaveMessage(null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void PlayerChatEvent(AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        String message = event.getMessage();
        Player player = event.getPlayer();
        String channel = chat.getChannel(player.getName());
        Group group = chat.getGroupTalk(player.getName());

        if (channel != null) {
            Player to = Bukkit.getPlayerExact(channel);

            if (to != null) {
                if(chat.isIgnoring(player.getName(),channel)){
                    player.sendMessage(ChatColor.YELLOW + channel + ChatColor.RED + " has muted you.");
                    return;
                }
                else{ 
                chat.sendPrivateMessage(player, to, message);
                return; }
            } else {
                chat.removeChannel(player.getName());
                player.sendMessage(ChatColor.GOLD + "The player you were chatting with has gone offline. You are now in regular chat.");
                return;
            }
        }
        if (group != null) {
            chat.GroupChat(group, message, player.getName());
            return;
        }
        
        chat.sendPlayerBroadcast(player, message, event.getRecipients());
    }
}
