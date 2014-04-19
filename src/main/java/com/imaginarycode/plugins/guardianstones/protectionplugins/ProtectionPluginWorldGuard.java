package com.imaginarycode.plugins.guardianstones.protectionplugins;

import com.google.common.collect.ImmutableList;
import com.imaginarycode.plugins.guardianstones.GuardianStones;
import com.imaginarycode.plugins.guardianstones.GuardianStonesException;
import com.imaginarycode.plugins.guardianstones.GuardianStonesUtil;
import com.imaginarycode.plugins.guardianstones.ProtectionFlag;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.UnsupportedIntersectionException;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class ProtectionPluginWorldGuard implements ProtectionPlugin {

    private WorldGuardPlugin wgplugin = null;
    private YamlConfiguration regionToWorldMappings = null;

    public boolean enable() {
        Plugin wgTmpPlugin = GuardianStones.self.getServer().getPluginManager().getPlugin("WorldGuard");
        if (wgTmpPlugin == null) {
            GuardianStones.self.getLogger().info("WorldGuard was not found. GuardianStones requires WorldGuard to work properly.");
            GuardianStones.self.getServer().getPluginManager().disablePlugin(GuardianStones.self);
            return false;
        } else {
            wgplugin = (WorldGuardPlugin) wgTmpPlugin;
        }
        regionToWorldMappings = GuardianStonesUtil.loadYaml("wgregions.yml");
        regionToWorldMappings.options().header("Do not modify this file -- it is used internally by GuardianStones's WorldGuard integration to determine world-region relationships!");
        return true;
    }

    public boolean disable() {
        GuardianStonesUtil.saveYaml(regionToWorldMappings, "wgregions.yml");
        return true;
    }

    private void sanityCheck(World desired) throws GuardianStonesException {
        if (wgplugin == null) {
            throw new GuardianStonesException("Did not find WorldGuard!");
        }
        if (wgplugin.getRegionManager(desired) == null) {
            throw new GuardianStonesException("Region support is not enabled for this world.");
        }
    }

    private ProtectedRegion getWorldGuardRegion(Location c) {
        if (wgplugin != null) {
            RegionManager regionManager = wgplugin.getRegionManager(c.getWorld());
            if (regionManager != null) {
                ApplicableRegionSet set = regionManager.getApplicableRegions(c);
                if (set.size() == 0)
                    return null;

                return set.iterator().next();
            }
        }
        return null;
    }

    private ProtectedRegion getBestGuardianStonesRegion(Location p) {
        if (wgplugin != null) {
            RegionManager regionManager = wgplugin.getRegionManager(p.getWorld());
            if (regionManager != null) {
                ApplicableRegionSet set = regionManager.getApplicableRegions(p);
                for (ProtectedRegion region : set) {
                    if (region.getId().startsWith("gs_")) {
                        return region;
                    }
                }
            }
        }
        return null;
    }
    /*
    private ProtectedRegion getBestGuardianStonesRegion(Player p) {
		if(wgplugin != null) {
			RegionManager regionManager = wgplugin.getRegionManager(p.getWorld());
			if(regionManager != null) {
    			ApplicableRegionSet set = regionManager.getApplicableRegions(p.getLocation());
    			for (ProtectedRegion region : set) {
    				if(region.getId().startsWith("gs_" + p.getName())) {
    					return region;
    				}
    			}
			}
		}
		return null;
    }*/

    @Override
    public boolean isProtected(Location loc, boolean guardianStonesOnly) {
        try {
            sanityCheck(loc.getWorld());
        } catch (GuardianStonesException e) {
            return false;
        }
        if (guardianStonesOnly) {
            return getBestGuardianStonesRegion(loc) != null;
        } else {
            return getWorldGuardRegion(loc) != null;
        }
    }

    @Override
    public boolean addMemberToRegion(String regionName, String player)
            throws GuardianStonesException {
        sanityCheck(Bukkit.getWorld(regionToWorldMappings.getString(regionName)));
        RegionManager regionManager = wgplugin.getRegionManager(Bukkit.getWorld(regionToWorldMappings.getString(regionName)));
        ProtectedRegion r = regionManager.getRegion(regionName);
        if (r != null) {
            DefaultDomain s = r.getMembers();
            s.addPlayer(player);
            r.setMembers(s);
            regionManager.removeRegion(regionName);
            regionManager.addRegion(r);
            try {
                regionManager.save();
            } catch (ProtectionDatabaseException e) {
                // TODO Auto-generated catch block
                throw new GuardianStonesException("Unable to commit region to WorldGuard!");
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean removeMemberFromRegion(String regionName, String player)
            throws GuardianStonesException {
        sanityCheck(Bukkit.getWorld(regionToWorldMappings.getString(regionName)));
        RegionManager regionManager = wgplugin.getRegionManager(Bukkit.getWorld(regionToWorldMappings.getString(regionName)));
        ProtectedRegion r = regionManager.getRegion(regionName);
        if (r != null) {
            DefaultDomain s = r.getMembers();
            s.removePlayer(player);
            r.setMembers(s);
            regionManager.removeRegion(regionName);
            regionManager.addRegion(r);
            try {
                regionManager.save();
            } catch (ProtectionDatabaseException e) {
                // TODO Auto-generated catch block
                throw new GuardianStonesException("Unable to commit region to WorldGuard!");
            }
            return true;
        }
        return false;
    }

    @Override
    public String defineRegion(String owner, Location center, int radius)
            throws GuardianStonesException {
        sanityCheck(center.getWorld());
        RegionManager regionManager = wgplugin.getRegionManager(center.getWorld());
        BlockVector v = new BlockVector(center.getBlockX(), center.getBlockY(), center.getBlockZ()).add(radius, 0, radius).setY(255).toBlockVector();
        BlockVector v2 = new BlockVector(center.getBlockX(), center.getBlockY(), center.getBlockZ()).subtract(radius, 0, radius).setY(0).toBlockVector();
        String name = "gs_" + GuardianStonesUtil.getLocationAsStringGuid(center);
        ProtectedCuboidRegion re = new ProtectedCuboidRegion(name, v2, v);
        // Verify that no other GuardianStones region overlaps this region.
        // If one does, we will decline to create a protection there.
        // TODO: This looks like a hack.
        try {
            // Oh, this is nice.
            List<ProtectedRegion> res = re.getIntersectingRegions(ImmutableList.copyOf(regionManager.getRegions().values()));
            if (res != null) {
                for (ProtectedRegion region : res) {
                    if (region.getId().startsWith("gs_")) {
                        // GuardianStones-protected!
                        // Try to identify it.
                        // Perhaps they want to expand the protection?
                        throw new GuardianStonesException("Proposed protection would overlap another existing protection!");
                    }
                }
            }
        } catch (UnsupportedIntersectionException e) {
            // TODO Auto-generated catch block
            throw new GuardianStonesException("Internal WorldGuard error");
        }
        // Set some flags
        // These will be quite aggressive for a PvP server
        re.setFlag(DefaultFlag.CREEPER_EXPLOSION, StateFlag.State.DENY);
        re.setFlag(DefaultFlag.TNT, StateFlag.State.DENY);
        re.setFlag(DefaultFlag.USE, StateFlag.State.DENY);
        re.setFlag(DefaultFlag.PVP, StateFlag.State.DENY);
        re.setFlag(DefaultFlag.MOB_DAMAGE, StateFlag.State.DENY);
        re.setFlag(DefaultFlag.MOB_SPAWNING, StateFlag.State.DENY);
        // Add the owner
        DefaultDomain d = new DefaultDomain();
        d.addPlayer(owner);
        re.setOwners(d);
        regionManager.addRegion(re);
        try {
            regionManager.save();
        } catch (ProtectionDatabaseException e) {
            throw new GuardianStonesException("Unable to commit changes to WorldGuard!");
        }
        // Now let's set the region's world
        this.regionToWorldMappings.set(name, center.getWorld().getName());
        return name;
    }

    @Override
    public boolean removeRegion(String name) throws GuardianStonesException {
        sanityCheck(Bukkit.getWorld(regionToWorldMappings.getString(name)));
        RegionManager regionManager = wgplugin.getRegionManager(Bukkit.getWorld(regionToWorldMappings.getString(name)));
        regionManager.removeRegion(name);
        try {
            regionManager.save();
        } catch (ProtectionDatabaseException e) {
            throw new GuardianStonesException("Unable to commit changes to WorldGuard!");
        }
        this.regionToWorldMappings.set(name, null);
        return true;
    }

    @Override
    public void toggleFlag(String regionName, ProtectionFlag flag) throws GuardianStonesException {
        sanityCheck(Bukkit.getWorld(regionToWorldMappings.getString(regionName)));
        RegionManager regionManager = wgplugin.getRegionManager(Bukkit.getWorld(regionToWorldMappings.getString(regionName)));
        ProtectedRegion pr = regionManager.getRegion(regionName);
        switch (flag) {
            case PVP:
                if (pr.getFlag(DefaultFlag.PVP) == StateFlag.State.DENY) {
                    pr.setFlag(DefaultFlag.PVP, StateFlag.State.ALLOW);
                } else {
                    pr.setFlag(DefaultFlag.PVP, StateFlag.State.DENY);
                }
                break;
            case TNT:
                if (pr.getFlag(DefaultFlag.TNT) == StateFlag.State.DENY) {
                    pr.setFlag(DefaultFlag.TNT, StateFlag.State.ALLOW);
                } else {
                    pr.setFlag(DefaultFlag.TNT, StateFlag.State.DENY);
                }
                break;
            case INTERACTION:
                if (pr.getFlag(DefaultFlag.USE) == StateFlag.State.DENY) {
                    pr.setFlag(DefaultFlag.USE, StateFlag.State.ALLOW);
                } else {
                    pr.setFlag(DefaultFlag.USE, StateFlag.State.DENY);
                }
                break;
            case MOBS:
                if (pr.getFlag(DefaultFlag.MOB_DAMAGE) == StateFlag.State.DENY) {
                    pr.setFlag(DefaultFlag.MOB_DAMAGE, StateFlag.State.ALLOW);
                } else {
                    pr.setFlag(DefaultFlag.MOB_DAMAGE, StateFlag.State.DENY);
                }
                if (pr.getFlag(DefaultFlag.MOB_SPAWNING) == StateFlag.State.DENY) {
                    pr.setFlag(DefaultFlag.MOB_SPAWNING, StateFlag.State.ALLOW);
                } else {
                    pr.setFlag(DefaultFlag.MOB_SPAWNING, StateFlag.State.DENY);
                }
                break;
        }
        try {
            regionManager.save();
        } catch (ProtectionDatabaseException e) {
            throw new GuardianStonesException("Unable to commit changes to WorldGuard!");
        }
    }

    @Override
    public String getRegion(Location loc) {
        try {
            sanityCheck(loc.getWorld());
        } catch (GuardianStonesException e) {
            return null;
        }
        return getWorldGuardRegion(loc).getId();
    }

}
