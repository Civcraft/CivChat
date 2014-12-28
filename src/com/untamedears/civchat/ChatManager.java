package com.untamedears.civchat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;

/*
 * Coded by ibbignerd and Rourke750
 */
public class ChatManager {
	private FileConfiguration config;
	private CivChat plugin;
	public double chatmax;
	public boolean garbleEnabled;
	private Random random = new Random();
	private double chatDist1;
	private int garble1;
	private double chatDist2;
	private int garble2;
	private int garblevar;
	private boolean greyscale;
	private String defaultcolor;
	private String color1;
	private String color2;
	public boolean yvar;
	private int ynogarb;
	public boolean shout;
	public String shoutChar;
	public int shoutDist;
	public boolean whisper;
	public String whisperChar;
	public int whisperDist;
	private String whisperColor;
	private HashMap<String, Group> groupchat = new HashMap<String, Group>();
	private HashMap<String, String> channels = new HashMap<String, String>();
	private String replacement = "abcdefghijklmnopqrstuvwxyz";
	private HashMap<Player, Long> shoutList = new HashMap<Player, Long>();
	public long shoutCool;
	private int shoutHunger;
	private String shoutColor;
	private HashMap<String, List<String>> ignoreList = new HashMap<String, List<String>>();
	private Map<String, List<String>> allowedGroupList = new HashMap<String, List<String>>();

	public ChatManager(CivChat pluginInstance, FileConfiguration config) {
		plugin = pluginInstance;
		this.config = config;
		chatmax = config.getDouble("chat.range.maxrange");
		garbleEnabled = config.getBoolean("chat.range.garbleEnabled");
		chatDist1 = chatmax - config.getDouble("chat.range.1.distance");
		garble1 = config.getInt("chat.range.1.garble");
		chatDist2 = chatmax - config.getDouble("chat.range.2.distance");
		garble2 = config.getInt("chat.range.2.garble");
		garblevar = config.getInt("chat.garblevariation");
		greyscale = config.getBoolean("chat.greyscale");
		defaultcolor = config.getString("chat.defaultcolor");
		color1 = config.getString("chat.range.1.color");
		color2 = config.getString("chat.range.2.color");
		yvar = config.getBoolean("chat.yvariation.enabled");
		ynogarb = config.getInt("chat.yvariation.noGarbLevel");
		shout = config.getBoolean("chat.shout.enabled");
		shoutChar = config.getString("chat.shout.char");
		shoutDist = config.getInt("chat.shout.distanceAdded");
		whisper = config.getBoolean("chat.whisper.enabled");
		whisperChar = config.getString("chat.whisper.char");
		whisperDist = config.getInt("chat.whisper.distance");
		whisperColor = config.getString("chat.whisper.color");
		shoutCool = config.getLong("chat.shout.cooldown") * 1000;
		shoutHunger = config.getInt("chat.shout.hungerreduced");
		shoutColor = config.getString("chat.shout.color");
	}

	public void sendPrivateMessage(Player from, Player to, String message) {
		if (isIgnoring(to.getName(), from.getName())) {
			from.sendMessage(ChatColor.YELLOW + to.getName() + ChatColor.RED
					+ " has muted you.");
			return;
		}
		Commands.replyList.put(from.getName(), to.getName());
		Commands.replyList.put(to.getName(), from.getName());
		from.sendMessage(ChatColor.LIGHT_PURPLE + "To " + to.getName() + ": "
				+ message);
		to.sendMessage(ChatColor.LIGHT_PURPLE + "From " + from.getName() + ": "
				+ message);
		SaveChat(from, "P Message",
				"To " + to.getName() + ": " + message.toString());
	}

	public void sendPlayerBroadcast(Player player, String message,
			Set<Player> receivers) {
		SaveChat(player, "Broadcast", message);

		Location location = player.getLocation();
		int x = location.getBlockX();
		int y = location.getBlockY();
		int z = location.getBlockZ();
		double chatdist = 0;
		boolean whispering = false;
		double chatrange = chatmax;
		double added = 0;
		boolean shouting = false;
		if (yvar && !message.startsWith(whisperChar) && y > ynogarb) {
			added = Math.pow(1.1, (y - ynogarb) / 14) * (y - ynogarb);
			chatrange += added;
		}

		if (shout && message.startsWith(shoutChar)) {
			if (shoutList.get(player) != null
					&& System.currentTimeMillis() - shoutList.get(player) >= shoutCool) {
				shoutList.remove(player);
			}

			if (shoutList.get(player) == null) {
				Float sat = player.getSaturation();
				if (sat > 0) {
					sat -= shoutHunger;
					if (sat <= 0) {
						player.setSaturation(0);
						player.setFoodLevel(player.getFoodLevel()
								- Integer.parseInt(sat + ""));
					} else {
						player.setSaturation(player.getSaturation() - sat);
					}
				} else {
					int food = player.getFoodLevel() - shoutHunger;
					if (food < 0) {
						food = 0;
					}
					player.setFoodLevel(food);
				}
				chatrange += shoutDist;
				shoutList.put(player, System.currentTimeMillis());
				shouting = true;
			} else {
				player.sendMessage(ChatColor.RED
						+ "Shout under cooldown, please wait "
						+ ((((shoutList.get(player) - System
								.currentTimeMillis()) + shoutCool) / 1000) + 1)
						+ " seconds");
			}
		}

		for (Player receiver : receivers) {
			double garble = 0;
			String chat = message;
			double randGarble = 0;
			ChatColor color = ChatColor.valueOf(defaultcolor);

			int rx = receiver.getLocation().getBlockX();
			int ry = receiver.getLocation().getBlockY();
			int rz = receiver.getLocation().getBlockZ();

			chatdist = Math.sqrt(Math.pow(x - rx, 2) + Math.pow(y - ry, 2)
					+ Math.pow(z - rz, 2));

			whispering = whisper && message.startsWith(whisperChar);
			if (whispering) {
				chatrange = whisperDist;
				color = ChatColor.valueOf(whisperColor);
				randGarble = 0;
			} else if (chatdist <= chatDist2 && chatdist > chatDist1) {
				if (garbleEnabled) {
					randGarble = random.nextInt(garblevar) + garble1;
				}
				if (greyscale) {
					color = ChatColor.valueOf(color1);
				}
			} else if (chatdist <= chatrange && chatdist > chatDist2) {
				if (garbleEnabled) {
					randGarble = random.nextInt(garblevar) + garble2;
				}
				if (greyscale) {
					color = ChatColor.valueOf(color2);
				}
			}
			if (garbleEnabled) {
				garble = chat.length() * (randGarble / 100.0F);
				chat = shuffle(chat, garble);
			} else {
				chat = message;
			}
			if (chatdist <= chatrange) {
				if (whispering) {
					receiver.sendMessage(color + player.getDisplayName()
							+ " whispered: " + chat.substring(1));
				} else if (shouting) {
					color = ChatColor.valueOf(shoutColor);
					receiver.sendMessage(color + player.getDisplayName()
							+ " shouted: " + chat.substring(1));
				} else {

					if (receiver.getWorld() != player.getWorld()) {
						continue;
					} else {
						receiver.sendMessage(color + player.getDisplayName()
								+ ": " + chat);
					}
				}
			}
		}
	}

	private String shuffle(String input, double a) {
		int times = (int) a;
		StringBuilder sb = new StringBuilder(input);
		for (int i = 0; i < times; i++) {
			int rand = random.nextInt(input.length());
			int replaceRand = random.nextInt(replacement.length());
			switch (sb.charAt(rand)) {
			case ' ':
			case '.':
			case ',':
			case '!':
				continue;
			}
			sb.setCharAt(rand, replacement.charAt(replaceRand));
		}
		String output = new String(sb);
		return output;
	}

	public void addChannel(String player1, String player2) {
		if (getChannel(player1) != null) {
			channels.put(player1, player2);
		} else {
			channels.put(player1, player2);
		}
	}

	public String getChannel(String player) {
		return channels.get(player);
	}

	public void removeChannel(String player) {
		if (channels.containsKey(player)) {
			channels.remove(player);
		}
	}

	public void GroupChat(Group group, String message, String player) {
		Player player1 = Bukkit.getPlayer(player);
		List<Player> players = new ArrayList<Player>();
		for(Player p : Bukkit.getOnlinePlayers()){
			players.add(p);
		}
		String chat = message;
		if (!isGroupAllowed(player, group.getName())){
			player1.sendMessage(ChatColor.RED + "Error: you need to /gallow this group to speak or hear from it.");
			return;
		}
		player1.sendMessage(ChatColor.GRAY + "[" + group.getName() + "] "
				+ player + ": " + ChatColor.WHITE + chat);
		for (Player reciever : players) {
			if ((!group.isMember(reciever.getUniqueId())) ||
					!isGroupAllowed(reciever.getName(), group.getName())) {
				continue;
			}
			if (isIgnoring(reciever.getName(), player)) {
				continue;
			}
			if (reciever.getName().equals(player1.getName())) {
				continue;
			} else {
				reciever.sendMessage(ChatColor.GRAY + "[" + group.getName()
						+ "] " + player + ": " + ChatColor.WHITE + chat);
			}

		}
		SaveChat(player1, "GroupChat", group.getName() + " -> " + message);
	}

	public void addGroupTalk(String player, Group group) {
		if (getGroupTalk(player) != null) {
			groupchat.put(player, group);
		} else {
			groupchat.put(player, group);
		}

	}

	public Group getGroupTalk(String player) {
		return groupchat.get(player);
	}

	public boolean isGroupTalk(String player) {
		if (groupchat.containsKey(player)) {
			return true;
		}
		return false;
	}

	public void removeGroupTalk(String player) {
		if (groupchat.containsKey(player)) {
			groupchat.remove(player);
		}
	}

	public void SaveChat(Player sender, String type, String message) {
		String date = new SimpleDateFormat("dd-MM HH:mm:ss").format(new Date());
		String name = sender.getName();
		String loc = (int) sender.getLocation().getX() + ", "
				+ (int) sender.getLocation().getY() + ", "
				+ (int) sender.getLocation().getZ();
		String textLine = "[" + date + "] [" + loc + "] [" + type + "] ["
				+ name + "] " + message;

		try {
			plugin.writer.write(textLine);
			plugin.writer.newLine();
			plugin.writer.flush();
		} catch (IOException ex) {
			Logger.getLogger(CivChat.class.getName()).log(Level.SEVERE, null,
					ex);
		}
	}

	public String playerCheck(String player) {
		List<Player> onlineList = new ArrayList<Player>();
		for(Player p : Bukkit.getOnlinePlayers()){
			onlineList.add(p);
		}
		for (Player check : onlineList) {
			if (check.getName().startsWith(player)) {
				return check.getName();
			}
		}
		return player;
	}

	public boolean isIgnoring(String muter, String muted) {
		List<String> ignorelist;
		try {
			if (ignoreList.containsKey(muter)) {
				ignorelist = ignoreList.get(muter);
				if (ignorelist.contains(muted)) {
					return true;
				}
			} else {
				return false;
			}
		} catch (NullPointerException e) {
			return false;
		}

		return false;
	}
	
	public boolean isGroupAllowed(String player, String group){
		if (allowedGroupList.containsKey(player))
			if (allowedGroupList.get(player).contains(group.toLowerCase())) return true;
		return false;
	}

	public void setIgnoreList(String player, String reciever) {
		List<String> recievers = new ArrayList<String>();
		if (ignoreList.get(player) != null) {
			recievers = ignoreList.get(player);
			recievers.add(reciever);
			ignoreList.put(player, recievers);
		} else {
			recievers.add(reciever);
			ignoreList.put(player, recievers);
		}
	}

	public List<String> getIgnoreList(String player) {
		return ignoreList.get(player);
	}

	public void removeIgnore(String player, String reciever) {
		List<String> removeplayers = new ArrayList<String>();
		for (String x : ignoreList.get(player)) {
			if (x.equals(reciever)) {
				continue;
			} else {
				removeplayers.add(x);
			}
		}
		ignoreList.put(player, removeplayers);
	}

	public void load(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		String line;
		while ((line = br.readLine()) != null) {
			String parts[] = line.split(" ");
			String owner = parts[0];
			List<String> participants = new ArrayList<>();
			for (int x = 1; x < parts.length; x++) {
				participants.add(parts[x]);
			}
			ignoreList.put(owner, participants);
		}
		br.close();
		fis.close();
	}

	public void save(File file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		BufferedWriter br = new BufferedWriter(new OutputStreamWriter(fos));
		Set<String> main = ignoreList.keySet();
		for (String z : main) {
			br.append(z);
			for (String x : ignoreList.get(z)) {
				br.append(" ");
				br.append(x);
			}
			br.append("\n");
		}
		br.flush();
		fos.close();
	}

	public void saveGroupAllowed(File file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		BufferedWriter br = new BufferedWriter(new OutputStreamWriter(fos));
		Set<String> players = allowedGroupList.keySet();
		for (String player : players) {
			br.append(player);
			for (String ignored : allowedGroupList.get(player))
				br.append(" " + ignored);
			br.append("\n");
		}
		br.flush();
		br.close();
	}

	public void loadGroupAllowed(File file) throws FileNotFoundException {
		FileInputStream fis = new FileInputStream(file);
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		String line;
		try {
			while ((line = br.readLine()) != null) {
				String parts[] = line.split(" ");
				String owner = parts[0];
				List<String> participants = new ArrayList<>();
				UUID uuid = Bukkit.getOfflinePlayer(owner).getUniqueId();
				Group group = null;
				for (int x = 1; x < parts.length; x++) {
					group = NameAPI.getGroupManager().getGroup(parts[x]);
					if (group == null){
						String log = "The group: " + parts[x] + " was not found.";
						Bukkit.getLogger().log(Level.INFO, log);
						continue;
					}
					if (group.isMember(uuid))
						participants.add(parts[x]);
				}
				allowedGroupList.put(owner, participants);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			try {
				br.close();
				fis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public boolean addOrRemoveGroup(String name, String group){
		List<String> stored = allowedGroupList.get(name);
		if (stored == null){
			List<String> groups = new ArrayList<String>();
			groups.add(group.toLowerCase());
			allowedGroupList.put(name, groups);
			return true;
		}
		else if (stored.contains(group)){
			allowedGroupList.get(name).remove(group.toLowerCase());
			return false;
		}
		else{
			stored.add(group.toLowerCase());
			allowedGroupList.put(name, stored);
			return true;
		}
	}
}