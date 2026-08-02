/*
    Copyright 2026 Damian Nikodem

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
package com.willwinder.ugs.designer.logic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.willwinder.ugs.designer.model.toollibrary.DefaultToolSeeds;
import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;
import com.willwinder.ugs.designer.model.toollibrary.ToolLibraryFile;
import com.willwinder.ugs.designer.model.toollibrary.ToolLibraryMigrator;
import com.willwinder.universalgcodesender.services.LookupServiceProvider;
import com.willwinder.universalgcodesender.utils.SettingsFactory;

import javax.swing.SwingUtilities;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@LookupServiceProvider
public class ToolLibraryService {
    private static final Logger LOGGER = Logger.getLogger(ToolLibraryService.class.getName());
    private static final String LIBRARY_FILENAME = "tool-library.json";
    private static final long WRITE_DEBOUNCE_MS = 250;

    private final Object mutationLock = new Object();
    private final Set<ToolLibraryListener> listeners = new CopyOnWriteArraySet<>();
    private final Map<String, ToolDefinition> tools = new LinkedHashMap<>();
    private final Path libraryPath;
    private final ScheduledExecutorService saveExecutor;
    private ScheduledFuture<?> pendingSave;
    private volatile boolean disableWrites;

    public ToolLibraryService() {
        this(new File(SettingsFactory.getSettingsDirectory(), LIBRARY_FILENAME).toPath());
    }

    public ToolLibraryService(Path libraryPath) {
        this.libraryPath = libraryPath;
        this.saveExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ToolLibrary-writer");
            t.setDaemon(true);
            return t;
        });
        loadOrSeed();
    }

    private void loadOrSeed() {
        synchronized (mutationLock) {
            ToolLibraryFile file = readFile();
            if (file == null) {
                LOGGER.info("No tool library found — seeding defaults at " + libraryPath);
                seedDefaults();
                scheduleSave();
                return;
            }
            tools.clear();
            Set<Integer> claimedToolNumbers = new HashSet<>();
            for (ToolDefinition tool : file.getTools()) {
                if (tool == null) continue;
                if (tool.getId() == null || tool.getId().isBlank()) {
                    tool.setId(UUID.randomUUID().toString());
                }
                if (tool.hasToolNumber() && !claimedToolNumbers.add(tool.getToolNumber())) {
                    LOGGER.warning("Tool number " + tool.getToolNumber() + " claimed more than once, "
                            + "clearing it on " + tool.getName());
                    tool.setToolNumber(ToolDefinition.UNASSIGNED_TOOL_NUMBER);
                }
                tools.put(tool.getId(), tool);
            }
        }
    }

    private ToolLibraryFile readFile() {
        if (!Files.exists(libraryPath)) {
            return null;
        }
        try {
            String json = Files.readString(libraryPath, StandardCharsets.UTF_8);
            ToolLibraryFile loaded = createGson().fromJson(json, ToolLibraryFile.class);
            return ToolLibraryMigrator.migrate(loaded);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Could not read tool library, backing up and reseeding", e);
            backupCorruptFile();
            return null;
        }
    }

    private void backupCorruptFile() {
        try {
            Path backup = libraryPath.resolveSibling(LIBRARY_FILENAME + ".corrupt-" + System.currentTimeMillis());
            Files.move(libraryPath, backup, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
            // best effort
        }
    }

    private void seedDefaults() {
        tools.clear();
        for (ToolDefinition seed : DefaultToolSeeds.create()) {
            tools.put(seed.getId(), seed);
        }
    }

    public Path getLibraryPath() {
        return libraryPath;
    }

    public List<ToolDefinition> getTools() {
        synchronized (mutationLock) {
            List<ToolDefinition> copy = new ArrayList<>(tools.size());
            for (ToolDefinition t : tools.values()) {
                copy.add(new ToolDefinition(t));
            }
            return Collections.unmodifiableList(copy);
        }
    }

    public Optional<ToolDefinition> getById(String id) {
        if (id == null) return Optional.empty();
        synchronized (mutationLock) {
            ToolDefinition t = tools.get(id);
            return t == null ? Optional.empty() : Optional.of(new ToolDefinition(t));
        }
    }

    /**
     * Finds the tool occupying a physical slot, for resolving a T word back to a tool.
     */
    public Optional<ToolDefinition> getByToolNumber(int toolNumber) {
        if (toolNumber <= ToolDefinition.UNASSIGNED_TOOL_NUMBER) return Optional.empty();
        synchronized (mutationLock) {
            return tools.values().stream()
                    .filter(t -> t.getToolNumber() == toolNumber)
                    .findFirst()
                    .map(ToolDefinition::new);
        }
    }

    /**
     * The lowest slot number not already claimed by another tool.
     */
    public int nextAvailableToolNumber() {
        synchronized (mutationLock) {
            Set<Integer> claimed = collectToolNumbers(null);
            int candidate = 1;
            while (claimed.contains(candidate)) {
                candidate++;
            }
            return candidate;
        }
    }

    private Set<Integer> collectToolNumbers(String excludedId) {
        Set<Integer> claimed = new HashSet<>();
        for (ToolDefinition tool : tools.values()) {
            if (tool.hasToolNumber() && !tool.getId().equals(excludedId)) {
                claimed.add(tool.getToolNumber());
            }
        }
        return claimed;
    }

    private void requireFreeToolNumber(ToolDefinition tool) {
        if (!tool.hasToolNumber()) {
            return;
        }
        tools.values().stream()
                .filter(other -> !other.getId().equals(tool.getId()))
                .filter(other -> other.getToolNumber() == tool.getToolNumber())
                .findFirst()
                .ifPresent(clash -> {
                    throw new IllegalArgumentException("Tool number " + tool.getToolNumber()
                            + " is already used by \"" + clash.getName() + "\"");
                });
    }

    public ToolDefinition addTool(ToolDefinition tool) {
        Objects.requireNonNull(tool, "tool");
        synchronized (mutationLock) {
            ToolDefinition copy = new ToolDefinition(tool);
            if (copy.getId() == null || copy.getId().isBlank()) {
                copy.setId(UUID.randomUUID().toString());
            }
            if (tools.containsKey(copy.getId())) {
                throw new IllegalArgumentException("Duplicate tool id: " + copy.getId());
            }
            requireFreeToolNumber(copy);
            tools.put(copy.getId(), copy);
            scheduleSave();
            notifyListeners();
            return new ToolDefinition(copy);
        }
    }

    public ToolDefinition updateTool(ToolDefinition tool) {
        Objects.requireNonNull(tool, "tool");
        Objects.requireNonNull(tool.getId(), "tool.id");
        synchronized (mutationLock) {
            if (!tools.containsKey(tool.getId())) {
                throw new IllegalArgumentException("Unknown tool id: " + tool.getId());
            }
            ToolDefinition copy = new ToolDefinition(tool);
            requireFreeToolNumber(copy);
            tools.put(copy.getId(), copy);
            scheduleSave();
            notifyListeners();
            return new ToolDefinition(copy);
        }
    }

    public void deleteTool(String id) {
        Objects.requireNonNull(id, "id");
        synchronized (mutationLock) {
            ToolDefinition existing = tools.get(id);
            if (existing == null) {
                return;
            }
            tools.remove(id);
            scheduleSave();
            notifyListeners();
        }
    }

    public ToolDefinition duplicate(String id) {
        Objects.requireNonNull(id, "id");
        synchronized (mutationLock) {
            ToolDefinition source = tools.get(id);
            if (source == null) {
                throw new IllegalArgumentException("Unknown tool id: " + id);
            }
            ToolDefinition copy = new ToolDefinition(source);
            copy.setId(UUID.randomUUID().toString());
            copy.setBuiltIn(false);
            copy.setCustomSentinel(false);
            copy.setName(suffixCopy(source.getName()));
            // Two tools cannot sit in the same physical slot — the copy starts out unassigned.
            copy.setToolNumber(ToolDefinition.UNASSIGNED_TOOL_NUMBER);
            tools.put(copy.getId(), copy);
            scheduleSave();
            notifyListeners();
            return new ToolDefinition(copy);
        }
    }

    private String suffixCopy(String name) {
        if (name == null || name.isBlank()) return "Copy";
        return name + " (copy)";
    }

    /**
     * Built-in only: restores the seed values while keeping the user-chosen name intact.
     */
    public ToolDefinition revertToDefault(String id) {
        Objects.requireNonNull(id, "id");
        synchronized (mutationLock) {
            ToolDefinition current = tools.get(id);
            if (current == null || !current.isBuiltIn()) {
                throw new IllegalStateException("Revert is only valid for built-in tools");
            }
            ToolDefinition seed = DefaultToolSeeds.create().stream()
                    .filter(s -> id.equals(s.getId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Built-in seed missing: " + id));
            seed.setName(current.getName());
            seed.setToolNumber(current.getToolNumber());
            tools.put(id, seed);
            scheduleSave();
            notifyListeners();
            return new ToolDefinition(seed);
        }
    }

    /**
     * Imports a tool that came from a project file. If the id already exists and differs,
     * a new id is assigned so user data isn't clobbered. A tool number that is already claimed
     * by a library tool is dropped rather than rejected — the project describes someone else's
     * machine, and the local slot assignment wins.
     */
    public ToolDefinition importFromProject(ToolDefinition tool) {
        Objects.requireNonNull(tool, "tool");
        synchronized (mutationLock) {
            ToolDefinition copy = new ToolDefinition(tool);
            copy.setBuiltIn(false);
            copy.setCustomSentinel(false);
            if (copy.getId() == null || copy.getId().isBlank() || tools.containsKey(copy.getId())) {
                copy.setId(UUID.randomUUID().toString());
            }
            if (collectToolNumbers(copy.getId()).contains(copy.getToolNumber())) {
                copy.setToolNumber(ToolDefinition.UNASSIGNED_TOOL_NUMBER);
            }
            tools.put(copy.getId(), copy);
            scheduleSave();
            notifyListeners();
            return new ToolDefinition(copy);
        }
    }

    public void addListener(ToolLibraryListener listener) {
        if (listener != null) listeners.add(listener);
    }

    public void removeListener(ToolLibraryListener listener) {
        if (listener != null) listeners.remove(listener);
    }

    private void notifyListeners() {
        Set<ToolLibraryListener> snapshot = new HashSet<>(listeners);
        SwingUtilities.invokeLater(() -> snapshot.forEach(l -> {
            try {
                l.onToolLibraryChanged();
            } catch (RuntimeException e) {
                LOGGER.log(Level.WARNING, "Tool library listener threw", e);
            }
        }));
    }

    private void scheduleSave() {
        if (disableWrites) return;
        if (pendingSave != null && !pendingSave.isDone()) {
            pendingSave.cancel(false);
        }
        pendingSave = saveExecutor.schedule(this::writeNow, WRITE_DEBOUNCE_MS, TimeUnit.MILLISECONDS);
    }

    private void writeNow() {
        ToolLibraryFile snapshot;
        synchronized (mutationLock) {
            snapshot = new ToolLibraryFile(new ArrayList<>(tools.values()));
        }
        try {
            Path parent = libraryPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Path tmp = libraryPath.resolveSibling(libraryPath.getFileName().toString() + ".tmp");
            String json = createGson().toJson(snapshot);
            Files.writeString(tmp, json, StandardCharsets.UTF_8);
            try {
                Files.move(tmp, libraryPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException notAtomic) {
                // Some CI filesystems (overlayfs, certain tmpfs configs) reject atomic replace.
                // Fall back to a plain move — non-atomic but still reliable.
                Files.move(tmp, libraryPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to write tool library to " + libraryPath, e);
        }
    }

    /**
     * Test hook — waits for any pending write to complete.
     */
    public void flushForTesting() {
        if (pendingSave == null) return;
        try {
            pendingSave.get(5, TimeUnit.SECONDS);
        } catch (Exception ignored) {
            // test-only path
        }
    }

    private static Gson createGson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create();
    }
}
