package com.ufund.api.ufundapi.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;

import com.fasterxml.jackson.databind.ObjectMapper;

public class NeedsListTests {
    private NeedsList needsList;
    private Need mockNeed;

    @BeforeEach
    public void setup() {
        needsList = new NeedsList();
        mockNeed = mock(Need.class); // base need to add to list

        when(mockNeed.getId()).thenReturn(1);
        when(mockNeed.getName()).thenReturn("Canned Soup");
    }

    @Test
    public void testGetNeed() {
        needsList.addNeed(mockNeed);
        Need firstResult = needsList.getNeed(1);
        Need secondResult = needsList.getNeed(2);

        assertNotNull(firstResult, "Getting need should return Need with ID added to list");
        assertNull(secondResult, "Getting need should return null if no ID exists");
    }

    @Test
    public void testAddNeedSuccess() {
        boolean result = needsList.addNeed(mockNeed);

        assertTrue(result, "Adding unique need should return true");
        assertEquals(mockNeed, needsList.getNeed(1), "Adding unique need should show up in getNeed");
    }

    @Test
    public void testAddNeedDuplicate() {
        needsList.addNeed(mockNeed); // setup to ensure list has need
        boolean result = needsList.addNeed(mockNeed);

        assertFalse(result, "Adding duplicate need should return false");
    }

    @Test
    public void testRemoveNeed() {
        needsList.addNeed(mockNeed); // setup to ensure list has need
        boolean result = needsList.removeNeed(1);

        assertTrue(result, "Successful remove should return true");
        assertFalse(needsList.hasNeed(1), "Successful remove should not show up in hasNeed");
    }

    @Test
    public void testGetNeeds() {
        needsList.addNeed(mockNeed);
        Collection<Need> allNeeds = needsList.getNeeds();

        assertEquals(1, allNeeds.size(), "Get all needs should have size 1 if one need was added");
        assertTrue(allNeeds.contains(mockNeed), "Get all needs should have all needs added");
    }

    @Test
    public void testSearchNeeds() {
        Need mockNeed2 = mock(Need.class); // setup for second need to exclude from search
        when(mockNeed2.getId()).thenReturn(2);
        when(mockNeed2.getName()).thenReturn("Fresh Fruit");

        needsList.addNeed(mockNeed);
        needsList.addNeed(mockNeed2);

        ArrayList<Need> searchResult = needsList.searchNeeds("Soup");

        assertEquals(1, searchResult.size(), "Searching should have size 1 if only 1 result matches");
        assertEquals("Canned Soup", searchResult.get(0).getName(), "Searching 'Soup' should return Canned Soup");
    }
}