/*
    Copyright 2018 Will Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.universalgcodesender;

import com.willwinder.universalgcodesender.model.Axis;

import java.util.HashSet;
import java.util.Set;

/**
 * Stores all capabilities supported by the implementation of the {@link IController}.
 *
 * @author wwinder
 * @author Joacim Breiler
 */
public class Capabilities {
    /**
     * The capabilities available for the current hardware
     */
    private Set<String> capabilities = new HashSet<>();

    /**
     * Merge capabilities from another Capabilities object into a new one.
     *
     * @param other the capabilities to merge.
     */
    public Capabilities merge(Capabilities other) {
        Capabilities result = new Capabilities();
        result.capabilities.addAll(other.capabilities);
        result.capabilities.addAll(capabilities);
        return result;
    }

    /**
     * Adds a capability that is either defined in {@link CapabilitiesConstants} or a
     * special capability for the controller.
     *
     * @param capability the capability to add. Either defined in {@link CapabilitiesConstants}
     *                   or a specific defined by the controller.
     */
    public void addCapability(String capability) {
        capabilities.add(capability);
    }

    /**
     * Removes a capability that is either defined in {@link CapabilitiesConstants} or a
     * special capability for the controller.
     *
     * @param capability the capability to remove
     */
    public void removeCapability(String capability) {
        capabilities.remove(capability);
    }

    /**
     * Checks if the controller has the given capability is available.
     *
     * @param capability the capability as a string that we want to check. You may define
     *                   your own values, the generic capabilities are defined in
     *                   {@link CapabilitiesConstants}
     * @return returns true if the capability is available
     */
    public boolean hasCapability(String capability) {
        return capabilities.contains(capability);
    }

    /**
     * Returns if the hardware have support for jogging. The capabilitiy is defined
     * by the capability {@link CapabilitiesConstants#JOGGING}. Jogging may be
     * emulated by the controller using GCode-commands.
     *
     * @return true if jogging is supported
     */
    public boolean hasJogging() {
        return hasCapability(CapabilitiesConstants.JOGGING);
    }

    /**
     * Returns if the hardware have support for overrides. The capabilitiy is defined
     * by the capability {@link CapabilitiesConstants#OVERRIDES}.
     *
     * @return true if overrides is supported
     */
    public boolean hasOverrides() {
        return hasCapability(CapabilitiesConstants.OVERRIDES);
    }

    /**
     * Returns if the hardware have support for continuous jogging. The capabilitiy
     * is defined by the capability {@link CapabilitiesConstants#CONTINUOUS_JOGGING}
     *
     * @return true if continuous jogging is supported
     */
    public boolean hasContinuousJogging() {
        return hasCapability(CapabilitiesConstants.CONTINUOUS_JOGGING);
    }

    /**
     * Returns if the hardware have support for homing. The capability
     * is defined by the capability {@link CapabilitiesConstants#HOMING}
     *
     * @return true if homing is supported
     */
    public boolean hasHoming() {
        return hasCapability(CapabilitiesConstants.HOMING);
    }

    /**
     * Returns if the hardware have support for hard limit switches. The capability
     * is defined by the capability {@link CapabilitiesConstants#HARD_LIMITS}
     *
     * @return true if hard limits are supported
     */
    public boolean hasHardLimits() {
        return hasCapability(CapabilitiesConstants.HARD_LIMITS);
    }

    /**
     * Returns if the hardware have support for soft limits. The capability
     * is defined by the capability {@link CapabilitiesConstants#SOFT_LIMITS}
     *
     * @return true if hard limits are supported
     */
    public boolean hasSoftLimits() {
        return hasCapability(CapabilitiesConstants.SOFT_LIMITS);
    }

    /**
     * Returns if the hardware have support for the setup wizard. The capability
     * is defined by the capability {@link CapabilitiesConstants#SETUP_WIZARD}
     *
     * @return true if the setup wizard is supported
     */
    public boolean hasSetupWizardSupport() {
        return hasCapability(CapabilitiesConstants.SETUP_WIZARD);
    }

    /**
     * Returns if the hardware have support for check mode. The capability
     * is defined by the capability {@link CapabilitiesConstants#CHECK_MODE}
     *
     * @return true if check mode is available in the hardware
     */
    public boolean hasCheckMode() {
        return hasCapability(CapabilitiesConstants.CHECK_MODE);
    }

    /**
     * Returns if the firmware has support for settings. The capability
     * is defined by the capability {@link CapabilitiesConstants#FIRMWARE_SETTINGS}
     *
     * @return true if the firmware has support for settings
     */
    public boolean hasFirmwareSettings() {
        return hasCapability(CapabilitiesConstants.FIRMWARE_SETTINGS);
    }

    /**
     * Returns if the hardware has support for returning to zero. The capability
     * is defined in by the capability {@link CapabilitiesConstants#RETURN_TO_ZERO}
     *
     * @return true if return to zero function is available in the hardware
     */
    public boolean hasReturnToZero() {
        return hasCapability(CapabilitiesConstants.RETURN_TO_ZERO);
    }


    /**
     * Returns if the controller has support for the given axis
     *
     * @param axis - the axis to check support for
     * @return true if the axis is supported
     */
    public boolean hasAxis(Axis axis) {
        switch (axis) {
            case X:
                return hasCapability(CapabilitiesConstants.X_AXIS);
            case Y:
                return hasCapability(CapabilitiesConstants.Y_AXIS);
            case Z:
                return hasCapability(CapabilitiesConstants.Z_AXIS);
            case A:
                return hasCapability(CapabilitiesConstants.A_AXIS);
            case B:
                return hasCapability(CapabilitiesConstants.B_AXIS);
            case C:
                return hasCapability(CapabilitiesConstants.C_AXIS);
            default:
                return false;
        }
    }

    /**
     * Returns if the hardware has the capabilities for opening the door
     *
     * @return true if open door function is available in the hardware
     */
    public boolean hasOpenDoor() {
        return hasCapability(CapabilitiesConstants.OPEN_DOOR);
    }
}
