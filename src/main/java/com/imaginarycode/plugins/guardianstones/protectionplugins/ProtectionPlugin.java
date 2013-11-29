package com.imaginarycode.plugins.guardianstones.protectionplugins;

import com.imaginarycode.plugins.guardianstones.GuardianStonesException;
import com.imaginarycode.plugins.guardianstones.ProtectionFlag;
import org.bukkit.Location;

/**
 * This class implements the guideline for an protection plugin to be supported by GuardianStones.
 *
 * @author tux-amd64
 */
public interface ProtectionPlugin {
    /**
     * Enable this protection plugin.
     * <p/>
     * The plugin's main class is available via GuardianStones.self.
     *
     * @return true if enabled; false if not
     */
    boolean enable();

    /**
     * Disable this protection plugin.
     * <p/>
     * The plugin's main class is available via GuardianStones.self.
     *
     * @return true if disabled; false if not
     */
    boolean disable();

    /**
     * Check if this Location has any protections (placed by GuardianStones)
     * whatsoever.
     *
     * @param loc
     * @param guardianStonesOnly
     * @return true if protected by GuardianStones
     */
    boolean isProtected(Location loc, boolean guardianStonesOnly);

    /**
     * Get the region of the protection.
     *
     * @param loc
     * @return region name
     */
    String getRegion(Location loc);

    /**
     * Add a member to a region.
     *
     * @param regionName
     * @param player
     * @return true if succeeded
     * @throws GuardianStonesException if the addition failed
     */
    boolean addMemberToRegion(String regionName, String player) throws GuardianStonesException;

    /**
     * Remove a member from a region.
     *
     * @param regionName
     * @param player
     * @return true if succeeded
     * @throws GuardianStonesException if the removal failed
     */
    boolean removeMemberFromRegion(String regionName, String player) throws GuardianStonesException;

    /**
     * Define a region, specified by a Location (the center of the protection),
     * an initial owner and a radius. The protection should have:
     * <ul>
     * <li>Creeper explosions denied
     * <li>TNT denied
     * <li>Redstone mechanism use denied
     * <li>PvP denied
     * <li>Mob damage and spawning denied
     * <li>No overlap with any other GuardianStones region
     * </ul>
     *
     * @param owner
     * @param center
     * @param radius
     * @return the name of the region
     * @throw GuardianStonesException if the creation failed
     */
    String defineRegion(String owner, Location center, int radius) throws GuardianStonesException;

    /**
     * Remove a region.
     *
     * @param name
     * @return true if region was removed
     * @throw GuardianStonesException if the removal failed
     */
    boolean removeRegion(String name) throws GuardianStonesException;

    /**
     * Toggle a flag.
     *
     * @param regionName
     * @param flag
     */
    void toggleFlag(String regionName, ProtectionFlag flag) throws GuardianStonesException;
}
