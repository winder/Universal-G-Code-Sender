/*
    Copyright 2020 Will Winder

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
package com.willwinder.ugs.nbp.joystick.action;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.willwinder.ugs.nbp.joystick.Settings;
import com.willwinder.ugs.nbp.joystick.model.JoystickControl;
import com.willwinder.ugs.nbp.lib.services.ActionReference;
import com.willwinder.ugs.nbp.lib.services.ActionRegistrationService;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.Lookup;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class ActionManager {

    private final ActionRegistrationService actionRegistrationService;
    private final Map<String, List<ActionReference>> customActions = new HashMap<>();
    private final Cache<String, Optional<ActionReference>> actionReferenceCache = CacheBuilder.newBuilder().build();

    public ActionManager() {
        this.actionRegistrationService = Lookup.getDefault().lookup(ActionRegistrationService.class);
    }

    public ActionManager(ActionRegistrationService actionRegistrationService) {
        this.actionRegistrationService = actionRegistrationService;
    }

    public Optional<ActionReference> getActionById(String actionId) {
        Optional<ActionReference> actionById = actionRegistrationService.getActionById(actionId);
        if (!actionById.isPresent()) {
            actionById = customActions.values()
                    .stream()
                    .flatMap(Collection::stream)
                    .filter(a -> StringUtils.equals(a.getId(), actionId))
                    .findFirst();
        }
        return actionById;
    }

    public Set<String> getCategories() {
        HashSet<String> result = new HashSet<>();
        result.addAll(actionRegistrationService.getCategoryActions().keySet());
        result.addAll(customActions.keySet());
        return result;
    }

    public List<ActionReference> getActionsByCategory(String key) {
        List<ActionReference> results = new ArrayList<>();
        results.addAll(customActions.getOrDefault(key, Collections.emptyList()));
        results.addAll(actionRegistrationService.getCategoryActions().getOrDefault(key, Collections.emptyList()));
        return results;
    }

    public Optional<ActionReference> getMappedAction(JoystickControl joystickButton) {
        try {
            return actionReferenceCache.get(joystickButton.name(), () -> {
                String actionMapping = Settings.getActionMapping(joystickButton);
                Optional<ActionReference> actionById = getActionById(actionMapping);

                // If it's a analog control bound to a digital action, wrap it in a DigitalActionAdapter
                if(joystickButton.isAnalog() && actionById.isPresent() && !(actionById.get().getAction() instanceof AnalogAction)) {
                    ActionReference actionReference = new ActionReference();
                    actionReference.setAction(new DigitalToAnalogActionAdapter(actionById.get().getAction()));
                    actionReference.setId(actionById.get().getId());
                    actionById =  Optional.of(actionReference);
                }

                return actionById;
            });
        } catch (ExecutionException e) {
            return Optional.empty();
        }
    }

    public void setMappedAction(JoystickControl joystickButton, ActionReference actionReference) {
        Settings.setActionMapping(joystickButton, actionReference.getId());
        actionReferenceCache.invalidateAll();
    }

    public void registerAction(String id, String category, Action action) {
        ActionReference actionReference = new ActionReference();
        actionReference.setId(id);
        actionReference.setAction(action);
        List<ActionReference> actionList = customActions.getOrDefault(category, new ArrayList<>());
        actionList.add(actionReference);
        customActions.put(category, actionList);
    }

    public void clearMappedAction(JoystickControl joystickButton) {
        Settings.setActionMapping(joystickButton, "");
        actionReferenceCache.invalidateAll();
    }
}
