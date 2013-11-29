package com.imaginarycode.plugins.guardianstones;

import com.google.common.base.Joiner;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.imaginarycode.plugins.guardianstones.protectionplugins.ProtectionPlugin;
import com.imaginarycode.plugins.guardianstones.protectionplugins.ProtectionPluginWorldGuard;

public class GuardianStones extends JavaPlugin implements Listener {
	public static GuardianStones self; // Stuck in a Pythonic World
	protected YamlConfiguration userToRegionMappings;
	/* Protection plugin classes */
	//private ProtectionPlugin[] pls = { new ProtectionPluginWorldGuard() };
	private ProtectionPlugin prot;

	public void onEnable() {
		// Detect plugins
		// Right now, we're hard-wired to use WorldGuard.
		self = this;
		prot = new ProtectionPluginWorldGuard();
		prot.enable();
		userToRegionMappings = GuardianStonesUtil.loadYaml("users.yml");
		userToRegionMappings.options().header("Do not modify this file! It is used internally by GuardianStones to identify owner-region-location relationships!");
    	getLogger().info("Loaded user to region mappings!");
    	getServer().getPluginManager().registerEvents(this, this);
	}
	
	public void onDisable() {
		prot.disable();
		GuardianStonesUtil.saveYaml(userToRegionMappings, "users.yml");
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
    	if(!(sender instanceof Player)) {
    		return true;
    	}
    	Player p = (Player)sender;
    	try {
    		if(args[0].equals("addmember")) {
    			String r = prot.getRegion(p.getLocation());
    			if(r != null) {
    				try {
    					prot.addMemberToRegion(r, args[1]);
    					sender.sendMessage(ChatColor.GREEN + "Added player successfully.");
    				} catch (GuardianStonesException e1) {
    					sender.sendMessage(ChatColor.RED + "An error occurred while we tried to add the player: " + e1.getMessage());
    				} catch (ArrayIndexOutOfBoundsException e2) {
    					sender.sendMessage(ChatColor.RED + "/gs addmember Player");
    				}
    			}
    		}
    		if(args[0].equals("removemember")) {
				String r2 = prot.getRegion(p.getLocation());
				if(r2 != null) {
					try {
						prot.removeMemberFromRegion(r2, args[1]);
						sender.sendMessage(ChatColor.GREEN + "Removed player successfully.");
					} catch (GuardianStonesException e1) {
						sender.sendMessage(ChatColor.RED + "An error occurred while we tried to remove the player: " + e1.getMessage());
					} catch (ArrayIndexOutOfBoundsException e2) {
						sender.sendMessage(ChatColor.RED + "/gs removemember Player");
					}
				}
    		}
            if(args[0].equals("toggleflag")) {
                String r2 = prot.getRegion(p.getLocation());
                if(r2 != null) {
                    try {
                        prot.toggleFlag(r2, ProtectionFlag.valueOf(args[1].toUpperCase()));
                        sender.sendMessage(ChatColor.GREEN + "Toggled flag successfully.");
                    } catch (GuardianStonesException e1) {
                        sender.sendMessage(ChatColor.RED + "An error occurred while we tried to remove the player: " + e1.getMessage());
                    } catch (ArrayIndexOutOfBoundsException e2) {
                        sender.sendMessage(ChatColor.RED + "/gs toggleflag FLAG");
                        sender.sendMessage(ChatColor.RED + "FLAG can be one of " + Joiner.on(", ").join(ProtectionFlag.values()) + ".");
                        sender.sendMessage(ChatColor.RED + "Upper and lower case letters don't matter.");
                    }
                }
            }
    	} catch (ArrayIndexOutOfBoundsException e2) {
    		sender.sendMessage(ChatColor.RED + "/gs addmember/removemember/toggleflag");
    	}
		return true;
    }
    
    @EventHandler
    public void onBlockPlacement(BlockPlaceEvent e) {
    	if(e.isCancelled()) return;
    	if(getConfig().contains("protection-blocks."+e.getBlockPlaced().getTypeId())) {
    		try {
				String region = prot.defineRegion(e.getPlayer().getName(), e.getBlockPlaced().getLocation(), getConfig().getInt("protection-blocks."+e.getBlockPlaced().getTypeId()+".radius"));
				userToRegionMappings.set(GuardianStonesUtil.serializedLoc(e.getBlockPlaced().getLocation()) + ".name", region);
				userToRegionMappings.set(GuardianStonesUtil.serializedLoc(e.getBlockPlaced().getLocation()) + ".owner", e.getPlayer().getName());
				e.getPlayer().sendMessage(ChatColor.GREEN + "Protection created successfully.");
			} catch (GuardianStonesException e1) {
				e.getPlayer().sendMessage(ChatColor.RED + "An error occured while we tried to create the protection: " + e1.getMessage());
				e.setCancelled(true);
			}
    	}
    }
    
    @EventHandler
    public void onBlockRemoval(BlockBreakEvent e) {
    	if(e.isCancelled()) return;
    	if(getConfig().contains("protection-blocks."+e.getBlock().getTypeId())) {
			// Identify the owner
    		try {
				if(userToRegionMappings.getString(GuardianStonesUtil.serializedLoc(e.getBlock().getLocation()) + ".owner").equals(e.getPlayer().getName())) {
					try {
						prot.removeRegion(userToRegionMappings.getString(GuardianStonesUtil.serializedLoc(e.getBlock().getLocation()) + ".name"));
						userToRegionMappings.set(GuardianStonesUtil.serializedLoc(e.getBlock().getLocation()), null);
						e.getPlayer().sendMessage(ChatColor.GREEN + "Protection removed successfully.");
					} catch (GuardianStonesException e1) {
						e.getPlayer().sendMessage(ChatColor.RED + "An error occured while we tried to remove the protection: " + e1.getMessage());
						e.setCancelled(true);
					}
				} else {
					e.getPlayer().sendMessage(ChatColor.RED + "You do not own this protection.");
					e.setCancelled(true);
				}
    		} catch (NullPointerException e2) {
    			// Ignore
    		}
    	}
    }
}
