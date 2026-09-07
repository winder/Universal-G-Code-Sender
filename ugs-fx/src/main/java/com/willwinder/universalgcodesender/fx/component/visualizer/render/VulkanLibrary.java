/*
   Copyright 2026 Joacim Breiler

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
package com.willwinder.universalgcodesender.fx.component.visualizer.render;

import club.doki7.ffm.library.ILibraryLoader;
import club.doki7.ffm.library.ISharedLibrary;
import club.doki7.vulkan.command.VulkanLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Opens the platform's Vulkan library.
 *
 * <p>vulkan4j resolves a library name by building {@code lib<name>.so} and handing that to
 * {@code dlopen}. That unversioned name is a development symlink: Debian, Ubuntu and Raspberry
 * Pi OS only install it with {@code libvulkan-dev}, while the runtime package ships nothing but
 * the soname {@code libvulkan.so.1}. Linux therefore looks for the soname in the distributions'
 * library directories and opens it by absolute path.
 *
 * <p>On macOS {@code libvulkan.so} never exists at all. macOS has no native Vulkan driver;
 * Vulkan is reached through MoltenVK, which translates it to Metal and ships as
 * {@code libMoltenVK.dylib}. Names that already start with a slash are passed to {@code dlopen}
 * verbatim, so the macOS candidates are searched as absolute paths instead.
 *
 * <p>Set {@code -Dugs.vulkan.library=/path/to/library} to override the search on any platform.
 */
final class VulkanLibrary {
    private static final Logger LOGGER = Logger.getLogger(VulkanLibrary.class.getName());

    static final String LIBRARY_PROPERTY = "ugs.vulkan.library";

    /**
     * MoltenVK first: it is what the native build bundles, and loading it directly avoids
     * depending on a Khronos loader that a user's machine may not have at all.
     */
    private static final List<String> MACOS_LIBRARY_NAMES = List.of(
            "libMoltenVK.dylib",
            "libvulkan.1.dylib",
            "libvulkan.dylib");

    /**
     * The Khronos loader's soname, which every Linux runtime package installs.
     */
    private static final String LINUX_SONAME = "libvulkan.so.1";

    /**
     * Debian multiarch directory names by {@code os.arch}. Fedora, Arch and friends keep their
     * libraries directly in {@code /usr/lib64} or {@code /usr/lib}, which are searched as well.
     */
    private static final Map<String, String> DEBIAN_MULTIARCH = Map.of(
            "aarch64", "aarch64-linux-gnu",
            "amd64", "x86_64-linux-gnu",
            "x86_64", "x86_64-linux-gnu",
            "arm", "arm-linux-gnueabihf",
            "riscv64", "riscv64-linux-gnu");

    private VulkanLibrary() {
    }

    static ISharedLibrary load() {
        String configured = System.getProperty(LIBRARY_PROPERTY, "").trim();
        if (!configured.isEmpty()) {
            Path library = Path.of(configured);
            if (Files.isReadable(library)) {
                LOGGER.log(Level.INFO, "Loading the Vulkan library from {0}", library);
                return open(library);
            }
            LOGGER.log(Level.WARNING, "No Vulkan library at {0}, searching the default locations", library);
        }

        if (isMacOs()) {
            return loadMoltenVk();
        }
        if (isLinux()) {
            return loadLinux();
        }
        return VulkanLoader.loadVulkanLibrary();
    }

    /**
     * Prefers the soname and only falls back to vulkan4j's {@code libvulkan.so} for systems that
     * keep their libraries somewhere unexpected but have the development symlink on the linker's
     * search path.
     */
    private static ISharedLibrary loadLinux() {
        List<Path> candidates = linuxCandidates();
        for (Path candidate : candidates) {
            if (!Files.isReadable(candidate)) {
                continue;
            }

            try {
                ISharedLibrary library = open(candidate);
                LOGGER.log(Level.INFO, "Loaded the Vulkan library from {0}", candidate);
                return library;
            } catch (UnsatisfiedLinkError e) {
                LOGGER.log(Level.FINE, e, () -> "Could not load " + candidate);
            }
        }

        try {
            return VulkanLoader.loadVulkanLibrary();
        } catch (UnsatisfiedLinkError e) {
            throw new VulkanException("No Vulkan library was found. Install the Vulkan loader "
                    + "(for example 'apt install libvulkan1 mesa-vulkan-drivers') or point "
                    + "-D" + LIBRARY_PROPERTY + " at it. Searched " + candidates
                    + " and the linker's own search path: " + e.getMessage(), e);
        }
    }

    private static List<Path> linuxCandidates() {
        Set<Path> directories = new LinkedHashSet<>();
        for (String entry : System.getProperty("java.library.path", "").split(java.io.File.pathSeparator)) {
            if (!entry.isBlank()) {
                directories.add(Path.of(entry));
            }
        }

        String vulkanSdk = System.getenv("VULKAN_SDK");
        if (vulkanSdk != null && !vulkanSdk.isBlank()) {
            directories.add(Path.of(vulkanSdk, "lib"));
        }

        String multiarch = DEBIAN_MULTIARCH.get(System.getProperty("os.arch", ""));
        if (multiarch != null) {
            directories.add(Path.of("/usr/lib", multiarch));
            directories.add(Path.of("/lib", multiarch));
        }
        directories.add(Path.of("/usr/local/lib"));
        directories.add(Path.of("/usr/lib64"));
        directories.add(Path.of("/usr/lib"));
        directories.add(Path.of("/lib64"));
        directories.add(Path.of("/lib"));

        List<Path> candidates = new ArrayList<>();
        for (Path directory : directories) {
            candidates.add(directory.resolve(LINUX_SONAME));
        }
        return candidates;
    }

    private static ISharedLibrary loadMoltenVk() {
        List<Path> candidates = macOsCandidates();
        for (Path candidate : candidates) {
            if (!Files.isReadable(candidate)) {
                continue;
            }

            try {
                ISharedLibrary library = open(candidate);
                LOGGER.log(Level.INFO, "Loaded the Vulkan library from {0}", candidate);
                return library;
            } catch (UnsatisfiedLinkError e) {
                LOGGER.log(Level.FINE, e, () -> "Could not load " + candidate);
            }
        }

        throw new VulkanException("No Vulkan library was found. macOS reaches Vulkan through "
                + "MoltenVK, which the application bundle normally ships. Searched: " + candidates);
    }

    /**
     * The bundled library comes first through {@code java.library.path}, which the native build
     * points at the application directory, so a MoltenVK installed system wide can never shadow
     * the version the application was tested against.
     */
    private static List<Path> macOsCandidates() {
        Set<Path> directories = new LinkedHashSet<>();
        for (String entry : System.getProperty("java.library.path", "").split(java.io.File.pathSeparator)) {
            if (!entry.isBlank()) {
                directories.add(Path.of(entry));
            }
        }

        String vulkanSdk = System.getenv("VULKAN_SDK");
        if (vulkanSdk != null && !vulkanSdk.isBlank()) {
            directories.add(Path.of(vulkanSdk, "lib"));
        }

        directories.add(Path.of("/usr/local/lib"));
        directories.add(Path.of("/opt/homebrew/lib"));

        List<Path> candidates = new ArrayList<>();
        for (Path directory : directories) {
            for (String name : MACOS_LIBRARY_NAMES) {
                candidates.add(directory.resolve(name));
            }
        }
        return candidates;
    }

    private static ISharedLibrary open(Path library) {
        return ILibraryLoader.platformLoader().loadLibrary(library.toAbsolutePath().toString());
    }

    private static boolean isMacOs() {
        return System.getProperty("os.name", "").toLowerCase().contains("mac");
    }

    private static boolean isLinux() {
        return System.getProperty("os.name", "").toLowerCase().contains("linux");
    }
}
