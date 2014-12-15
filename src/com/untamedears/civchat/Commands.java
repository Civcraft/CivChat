package com.untamedears.civchat;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.kitteh.vanish.VanishManager;
import org.kitteh.vanish.staticaccess.VanishNoPacket;
import org.kitteh.vanish.staticaccess.VanishNotLoadedException;

import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;

/*
 * Coded by Rourke750 & ibbignerd
 */
public class Commands implements CommandExecutor {

	private CivChat civ;
	private ChatManager chatManager;
	public static HashMap<String, String> replyList = new HashMap<String, String>();

	public Commands(ChatManager chatManagerInstance, CivChat instance) {
		chatManager = chatManagerInstance;
		civ = instance;
	}

	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		if (command.getName().equals("tell")) {
			return tell(sender, command, label, args);
		}

		if (command.getName().equals("reply")) {
			return reply(sender, command, label, args);
		}

		if (command.getName().equals("exit")) {
			return exit(sender, command, label, args);
		}

		if (command.getName().equals("civchat")) {
			return civchat(sender, command, label, args);
		}
		if (command.getName().equals("groupchat")) {
			return groupchat(sender, command, label, args);
		}

		if (command.getName().equals("ignore")) {
			return ignore(sender, command, label, args);
		}
		
		if (command.getName().equals("gallow")){
			return gAllow(sender, command, label, args);
		}

		if (command.getName().equals("chat")) {
			String chatPrefix = ChatColor.DARK_RED + "===" + ChatColor.YELLOW
					+ "CivChat" + ChatColor.DARK_RED
					+ "=========================\n";
			if (args.length == 0) {
				String help = "/chat range\n /chat groupchat\n /chat tell\n";
				if (chatManager.shout) {
					help += " /chat shout\n";
				}
				if (chatManager.whisper) {
					help += " /chat whisper\n";
				}
				if (chatManager.yvar) {
					help += " /chat height\n";
				}
				help += " /chat alias\n /chat ignore\n /chat gallow\n /chat info";

				sender.sendMessage(chatPrefix + " " + ChatColor.WHITE + help);
			} else if (args.length > 0) {
				if (args[0].equalsIgnoreCase("shout") && chatManager.shout) {
					sender.sendMessage(chatPrefix + ChatColor.WHITE
							+ " By putting a \"" + chatManager.shoutChar
							+ "\" in front of your message, chat range\n"
							+ "   is extended " + chatManager.shoutDist + "m\n"
							+ " There is a " + chatManager.shoutCool / 1000
							+ " second cooldown");
				} else if (args[0].equalsIgnoreCase("whisper")
						&& chatManager.whisper) {
					sender.sendMessage(chatPrefix + ChatColor.WHITE
							+ " By putting a \"" + chatManager.whisperChar
							+ "\" in front of your message, chat range\n"
							+ "   is reduced to " + chatManager.whisperDist
							+ "m\n");
				} else if (args[0].equalsIgnoreCase("height")
						&& chatManager.yvar) {
					sender.sendMessage(chatPrefix
							+ ChatColor.WHITE
							+ " The higher you are the farther your messages can be heard");
				} else if (args[0].equalsIgnoreCase("range")) {
					sender.sendMessage(chatPrefix
							+ ChatColor.WHITE
							+ " The default chat range is "
							+ (int) chatManager.chatmax
							+ "m\n"
							+ " This can be extended by using shout or climbing a mountain\n");
				} else if (args[0].equalsIgnoreCase("garble")
						&& chatManager.garbleEnabled) {
					sender.sendMessage(chatPrefix
							+ ChatColor.WHITE
							+ " Depending on the range, chat will be more garbled");
				} else if (args[0].equalsIgnoreCase("groupchat")) {
					sender.sendMessage(chatPrefix
							+ ChatColor.WHITE
							+ " /groupchat <citadel group> <message>\n"
							+ " Send a message to everyone in the citadel group");
				} else if (args[0].equalsIgnoreCase("alias")) {
					sender.sendMessage(chatPrefix + ChatColor.WHITE
							+ " /groupchat [g, group]\n"
							+ " /tell [message, msg, m, pm]\n"
							+ " /chat [chathelp, ch]\n" + " /reply [r]\n");
				} else if (args[0].equalsIgnoreCase("tell")) {
					sender.sendMessage(chatPrefix
							+ ChatColor.WHITE
							+ " /tell <player> <message>\n"
							+ "   Send one message to the player\n"
							+ " /tell <player>\n"
							+ "   Create a channel with player. All regular chat will\n"
							+ "     go to player"
							+ " /exit\n"
							+ "   Stop the channel with player. All regular chat will\n"
							+ "     go to regular chat");
				} else if (args[0].equalsIgnoreCase("info")) {
					sender.sendMessage(chatPrefix + ChatColor.WHITE
							+ " Version 1.33 \n"
							+ " Coded by: Rourke750 and ibbignerd");
				} else if (args[0].equalsIgnoreCase("ignore")) {
					sender.sendMessage(chatPrefix
							+ ChatColor.WHITE
							+ " /ignore <player>\n"
							+ " Stop receiving personal messages from player\n"
							+ " Running /ignore <player> again, will allow personal\n"
							+ "   messages from player again");
				} else if (args[0].equalsIgnoreCase("gallow")){
					sender.sendMessage(ChatColor.WHITE + "/gallow <group>\n"
							+ "Start receiving messages from groups.\n"
							+ "Running the command again ignores the group.");
				}else {
					sender.sendMessage(ChatColor.RED + args[0]
							+ " is not a valid argument");
				}
				return true;
			}
		}
		return true;
	}
	
	public boolean gAllow(CommandSender sender, Command command, String label, String[] args){
		if (!(sender instanceof Player)) {
			sender.sendMessage("You have to be a player to use that command!");
			return true;
		}
		if (args.length == 0) {
			sender.sendMessage("/gallow <group>");
			return true;
		}
		else if (args.length > 0){
			Group group = NameAPI.getGroupManager().getGroup(args[0]);
			boolean isGroup = group != null;
			UUID uuid = ((Player) sender).getUniqueId();
			if (!isGroup){
				sender.sendMessage(ChatColor.RED + "Group: "+args[0]+" is not a real group!");
			}
			else if (!(group.isMember(uuid))){
				sender.sendMessage(ChatColor.RED + "You cannot join that group you are not on it.");
			}
			else {
				boolean value = chatManager.addOrRemoveGroup(sender.getName(), args[0]);
				if (value) sender.sendMessage(ChatColor.RED + "Allowing messages from "+args[0] + "!");
				else sender.sendMessage(ChatColor.RED + "Blocking messages from "+args[0] + "!");
			}
		}
		return true;
	}

	public Boolean tell(CommandSender sender, Command command, String label,
			String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("You have to be a player to use that command!");
			return true;
		}
		VanishManager vanish = null;
		try {
			if (Bukkit.getPluginManager().getPlugin("VanishNoPacket") !=  null)
				vanish = VanishNoPacket.getManager();
		} catch (VanishNotLoadedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Player player = (Player) sender;

		if (args.length < 1) {
			if (chatManager.getChannel(player.getName()) == null) {
				player.sendMessage(ChatColor.RED + "Usage: /tell <player>");
			}

			else {
				chatManager.removeChannel(player.getName());
				chatManager.removeGroupTalk(player.getName());
				player.sendMessage(ChatColor.RED
						+ "You have moved to regular chat.");
			}
			return true;
		} else if (args.length == 1) {
			Player receiver = Bukkit
					.getPlayer(chatManager.playerCheck(args[0]));
			if (receiver == null || (vanish != null && vanish.isVanished(receiver))) {
				player.sendMessage(ChatColor.RED
						+ "Error: Player is offline.");
				return true;
			} else if(player.getName().equals(receiver.getName())){
				player.sendMessage(ChatColor.RED + "Error: You cannot send a message to yourself.");
				return true;
			}
			else {
				if (chatManager.getGroupTalk(player.getName()) != null) {
					sender.sendMessage(ChatColor.RED
							+ "You were removed from Group Chat.");
					chatManager.removeGroupTalk(player.getName());
				}
				chatManager.addChannel(player.getName(), receiver.getName());
				player.sendMessage(ChatColor.RED + "You are now chatting with "
						+ receiver.getName() + ".");
				return true;
			}
		}

		if (args.length > 1) {
			Player receiver = Bukkit
					.getPlayer(chatManager.playerCheck(args[0]));
			if (receiver == null ||(vanish != null && vanish.isVanished(receiver))) {
				player.sendMessage(ChatColor.RED
						+ "Error: Player is offline.");
				return true;
			} else {
				StringBuilder message = new StringBuilder();

				for (int i = 1; i < args.length; i++) {
					message.append(args[i]);

					if (i < args.length - 1) {
						message.append(" ");
					}
				}
				chatManager.sendPrivateMessage(player, receiver,
						message.toString());
				return true;
			}
		}
		return true;

	}

	public Boolean reply(CommandSender sender, Command command, String label,
			String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("You have to be a player to use that command!");
			return true;
		}

		String player = chatManager.playerCheck(sender.getName());
		if (replyList.containsKey(player)) {
			if (Bukkit.getPlayerExact(replyList.get(player)) == null) {
				sender.sendMessage(ChatColor.RED + "Error: Player is offline.");
				replyList.remove(player);
				return true;
			} else {
				if (args.length > 0) {
					String receiver = replyList.get(player);
					if (chatManager.isIgnoring(player, receiver)) {
						sender.sendMessage(ChatColor.YELLOW + receiver
								+ ChatColor.RED + " has muted you.");
						return true;
					}
					StringBuilder message = new StringBuilder();

					for (int i = 0; i < args.length; i++) {
						message.append(args[i]);

						if (i < args.length - 1) {
							message.append(" ");
						}
					}
					chatManager.sendPrivateMessage(Bukkit.getPlayer(player), Bukkit.getPlayer(receiver), message.toString());
				} else {
					String player2 = replyList.get(player);
					Bukkit.getPlayer(player).sendMessage(
							ChatColor.LIGHT_PURPLE + "You will message "
									+ ChatColor.YELLOW + player2);
					chatManager.removeChannel(player);
					chatManager.removeGroupTalk(player);
					chatManager.addChannel(player, player2);
				}
			}
		} else {
			Bukkit.getPlayer(player).sendMessage(
					ChatColor.RED + "There is no one to reply to");
		}
		return true;
	}

	public Boolean exit(CommandSender sender, Command command, String label,
			String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("You have to be a player to use that command!");
			return true;
		}

		Player player = (Player) sender;
		if (chatManager.getChannel(player.getName()) != null) {
			chatManager.removeChannel(player.getName());
			player.sendMessage(ChatColor.RED
					+ "You have been moved to regular chat.");
			return true;
		}
		if (chatManager.getGroupTalk(player.getName()) != null) {
			chatManager.removeGroupTalk(player.getName());
			player.sendMessage(ChatColor.RED
					+ "You have been moved to regular chat.");
			return true;
		} else {
			player.sendMessage(ChatColor.RED
					+ "You are not in a private chat or group chat");
			return true;
		}
	}

	public Boolean civchat(CommandSender sender, Command command, String label,
			String[] args) {
		if (sender.hasPermission("civchat.admin")) {
			if (args.length < 1) {
				sender.sendMessage(ChatColor.RED
						+ "Usage: /civchat <save/reload>");
				return true;
			}

			if (args[0].equalsIgnoreCase("save")) {
				sender.sendMessage("Saved configuration.");
				civ.saveConfig();

				return true;
			}
			if (args[0].equalsIgnoreCase("reload")) {
				sender.sendMessage("Reloaded configuration.");
				civ.reloadConfig();

				return true;
			}
		} else {
			sender.sendMessage(ChatColor.RED
					+ "You Do not have Permissions civchat.admin");
			return true;
		}
		return true;
	}

	public Boolean groupchat(CommandSender sender, Command command,
			String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Must be a player to use that.");
			return true;
		}
		Player player = (Player) sender;
		if (args.length < 1) {
			if (chatManager.isGroupTalk(player.getName())) {
				sender.sendMessage(ChatColor.RED
						+ "You have been moved to normal chat.");
				chatManager.removeGroupTalk(player.getName());
				return true;
			} else {
				sender.sendMessage(ChatColor.RED
						+ "Usage: /groupchat [group name] <message>");
				return true;
			}
		}
		StringBuilder message = new StringBuilder();
		Group group = NameAPI.getGroupManager().getGroup(args[0]);
		if (group == null) {
			sender.sendMessage(ChatColor.RED + "Error: Not a valid group name.");
			return true;
		}
		if (!group.isMember(player.getUniqueId())) {
			sender.sendMessage(ChatColor.RED + "You are not in that group.");
			return true;
		}

		if (args.length == 1) {
			if (chatManager.getGroupTalk(player.getName()) == null) {
				if (chatManager.getChannel(player.getName()) != null) {
					sender.sendMessage(ChatColor.RED
							+ "You were removed from private chat.");
					chatManager.removeChannel(player.getName());
				}
				sender.sendMessage(ChatColor.RED
						+ "You have moved to group chat in the group: "
						+ group.getName() + ".");
				chatManager.addGroupTalk(sender.getName(), group);
				return true;
			} else {
				sender.sendMessage(ChatColor.RED
						+ "You have been switched to group: " + group.getName());
				chatManager.removeGroupTalk(player.getName());
				chatManager.addGroupTalk(player.getName(), group);
				return true;
			}
		}
		if (args.length > 1) {

			for (int i = 1; i < args.length; i++) {
				message.append(args[i]);

				if (i < args.length - 1) {
					message.append(" ");
				}
			}
			chatManager.GroupChat(group, message.toString(), sender.getName());
			return true;
		}
		return true;
	}

	public Boolean ignore(CommandSender sender, Command command, String label,
			String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("You have to be a player to use that command!");
			return true;
		}
		if (args.length == 0) {
			sender.sendMessage("/ignore <player>");
			return true;
		}

		else if (args.length >= 1) {
			String player = sender.getName();
			String reciever = args[0];
			if (Bukkit.getPlayer(reciever) == null) {
				sender.sendMessage("That player is not online.");
				return true;
			} else {
				int i = 0;
				if (chatManager.getIgnoreList(player) == null) {
					Bukkit.getPlayerExact(player).sendMessage(
							"Added player " + reciever + " to ignore list.");
					chatManager.setIgnoreList(player, reciever);
					return true;
				} else {
					for (String ignored : chatManager.getIgnoreList(player)) {
						if (ignored.equals(reciever)) {
							i++;
							Bukkit.getPlayerExact(player).sendMessage(
									"Removed player " + reciever
											+ " from ignore list.");
							chatManager
									.removeIgnore(sender.getName(), reciever);

							return true;
						} else {
							continue;
						}

					}
					if (i == 0) {
						Bukkit.getPlayerExact(player)
								.sendMessage(
										"Added player " + reciever
												+ " to ignore list.");
						chatManager.setIgnoreList(player, reciever);
						return true;
					}
				}
			}
		}
		return true;
	}

}
