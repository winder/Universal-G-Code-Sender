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
package com.willwinder.universalgcodesender.services;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.LinkedHashSet;

import static org.junit.Assert.*;

public class LookupServiceTest {

    @Before
    public void setUp() throws Exception {
        resetLookupServiceState();
    }

    @After
    public void tearDown() throws Exception {
        resetLookupServiceState();
    }

    @Test
    public void registerShouldNotAddDuplicateTypes() {
        FirstTestService first = new FirstTestService();
        FirstTestService duplicate = new FirstTestService();

        LookupService.register(first);
        LookupService.register(duplicate);

        List<FirstTestService> results = LookupService.lookupAll(FirstTestService.class);
        assertEquals(1, results.size());
        assertSame(first, results.get(0));
    }

    @Test
    public void lookupAllShouldReturnOnlyMatchingInstances() {
        FirstTestService first = new FirstTestService();
        SecondTestService second = new SecondTestService();

        LookupService.register(first);
        LookupService.register(second);

        List<FirstTestService> results = LookupService.lookupAll(FirstTestService.class);
        assertEquals(1, results.size());
        assertSame(first, results.get(0));
    }

    @Test
    public void createAndRegisterShouldCreateInstanceAndStoreIt() {
        CreatedService created = LookupService.createAndRegister(CreatedService.class);

        assertNotNull(created);
        assertEquals(1, LookupService.lookupAll(CreatedService.class).size());
        assertSame(created, LookupService.lookupAll(CreatedService.class).get(0));
    }

    @Test
    public void removeShouldDeleteMatchingInstances() {
        LookupService.register(new FirstTestService());
        LookupService.register(new SecondTestService());

        LookupService.remove(FirstTestService.class);

        assertTrue(LookupService.lookupAll(FirstTestService.class).isEmpty());
        assertEquals(1, LookupService.lookupAll(SecondTestService.class).size());
    }

    @Test
    public void lookupShouldReturnAlreadyRegisteredInstance() {
        FirstTestService first = new FirstTestService();
        LookupService.register(first);

        FirstTestService result = LookupService.lookup(FirstTestService.class);

        assertSame(first, result);
        assertEquals(1, LookupService.lookupAll(FirstTestService.class).size());
    }

    @Test
    public void lookupShouldCreateAndRegisterMissingInstance() {
        MissingService result = LookupService.lookup(MissingService.class);

        assertNotNull(result);
        assertEquals(1, LookupService.lookupAll(MissingService.class).size());
        assertSame(result, LookupService.lookupAll(MissingService.class).get(0));
    }

    private static void resetLookupServiceState() throws Exception {
        Field registryField = LookupService.class.getDeclaredField("registry");
        registryField.setAccessible(true);
        @SuppressWarnings("unchecked")
        LinkedHashSet<Object> registry = (LinkedHashSet<Object>) registryField.get(null);
        registry.clear();

        Field initializedField = LookupService.class.getDeclaredField("isInitialized");
        initializedField.setAccessible(true);
        initializedField.setBoolean(null, false);
    }

    public static class FirstTestService {
    }

    public static class SecondTestService {
    }

    public static class CreatedService {
        public CreatedService() {
        }
    }

    public static class MissingService {
        public MissingService() {
        }
    }
}