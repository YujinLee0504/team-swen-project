package com.ufund.api.ufundapi.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.ufund.api.ufundapi.model.Pledge;

/**
 * Test the PledgeCatalog File DAO class
 *
 * @author 5E
 */
@Tag("Persistence-tier")
class PledgeCatalogFileDAOTest {
    private PledgeCatalogFileDAO dao;
    private ObjectMapper mockMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockMapper = mock(ObjectMapper.class);

        when(mockMapper.readValue(any(File.class), any(Class.class))).thenReturn(new Pledge[0]);
        dao = new PledgeCatalogFileDAO("test.json", mockMapper);
    }

    // ----------------------------------------------------------------
    // Save/load
    // ----------------------------------------------------------------

    @Test
    void testSaveNullObjectMapperThrowsException() throws IOException {
        PledgeCatalogFileDAO nullMapperDAO = new PledgeCatalogFileDAO("test.json", null);
        assertThrows(IllegalStateException.class, () -> nullMapperDAO.addPledge(new Pledge(1, 1, 1, 1, 0.0)));
    }

    @Test
    void testLoadIdAssignment() throws IOException {
        // File must exist so load() runs the for-loop over persisted pledges
        Path pledgeFile = Files.createTempFile("pledges-load-id-test-", ".json");

        try {
            Pledge[] persisted = new Pledge[] {
                new Pledge(10, 2, 100, 1, 0.0),
                new Pledge(3, 1, 200, 2, 1.5)
            };
            ObjectMapper loadMapper = mock(ObjectMapper.class);
            when(loadMapper.readValue(any(File.class), eq(Pledge[].class))).thenReturn(persisted);

            PledgeCatalogFileDAO loaded = new PledgeCatalogFileDAO(pledgeFile.toString(), loadMapper);

            assertTrue(loaded.hasPledge(3));
            assertTrue(loaded.hasPledge(10));
            Pledge p3 = loaded.getPledge(3);
            assertNotNull(p3);
            assertEquals(3, p3.getId());
            assertEquals(1, p3.getOwnerId());
            assertEquals(200, p3.getNeedId());
            assertEquals(2, p3.getQuantity());
            assertEquals(1.5, p3.getMoney());
            Pledge p10 = loaded.getPledge(10);
            assertNotNull(p10);
            assertEquals(10, p10.getId());
            assertEquals(2, p10.getOwnerId());
            assertEquals(100, p10.getNeedId());
            assertEquals(1, p10.getQuantity());
            assertEquals(0.0, p10.getMoney());
            assertEquals(11, loaded.nextId());
            assertEquals(12, loaded.nextId());
        } finally {
            Files.deleteIfExists(pledgeFile);
        }
    }


    //-----------------------------------------
    // add
    // ----------------------------------------

    @Test
    public void addPledgeSuccess() throws IOException {
        Pledge pledge = new Pledge(1, 1, 1, 0, 0.0);
        
        boolean result = dao.addPledge(pledge);

        assertTrue(result);
        assertTrue(dao.hasPledge(pledge.getId()));
        verify(mockMapper).writeValue(any(File.class), any(Pledge[].class));

    }

    @Test
    public void addPledgeDuplicate() throws IOException{
        Pledge pledge = new Pledge(1, 1, 1, 0, 0.0);
        dao.addPledge(pledge);

        boolean result = dao.addPledge(pledge);

        assertFalse(result);
    }

    @Test
    public void addPledgeNull() throws IOException {
        boolean result = dao.addPledge(null);

        assertFalse(result);
    }

    @Test
    void addPledgeFromArgumentsSuccess() throws IOException {
        Pledge created = dao.addPledgeFromArguments(7, 42, 3, 12.5);

        assertNotNull(created);
        assertEquals(1, created.getId());
        assertEquals(7, created.getOwnerId());
        assertEquals(42, created.getNeedId());
        assertEquals(3, created.getQuantity());
        assertEquals(12.5, created.getMoney());
        assertTrue(dao.hasPledge(created.getId()));
        assertEquals(created, dao.getPledge(created.getId()));
        verify(mockMapper, times(1)).writeValue(any(File.class), any(Pledge[].class));
    }
  
    // ----------------------------------------------------------------
    // update
    // ----------------------------------------------------------------

    @Test
    void updatePledgeSuccess() throws IOException {
        // Setup
        Pledge original = new Pledge(10, 1, 100, 5, 0.0);
        Pledge updated = new Pledge(10, 1, 100, 15, 0.0);

        dao.addPledge(original);

        // Invoke
        boolean result = dao.updatePledge(updated);

        // Analyze
        assertTrue(result);
        verify(mockMapper, times(2)).writeValue(any(File.class), any(Pledge[].class)); // add + update
    }

    @Test
    void updatePledgeNotFoundReturnsFalse() throws IOException {
        // Setup
        Pledge updated = new Pledge(99, 1, 100, 15, 0.0);

        // Invoke
        boolean result = dao.updatePledge(updated);
        assertFalse(result);
        verify(mockMapper, times(0)).writeValue(any(File.class), any(Pledge[].class));
    }

    @Test
    void updatePledgeNullReturnsFalse() throws IOException {
        boolean result = dao.updatePledge(null);
        assertFalse(result);
        verify(mockMapper, never()).writeValue(any(File.class), any(Pledge[].class));
    }
  
    // ----------------------------------------------------------------
    // get/has
    // ----------------------------------------------------------------

    @Test
    void testNextIdIncrements() {
        int id1 = dao.nextId();
        int id2 = dao.nextId();

        assertEquals(id1 + 1, id2);
    }

    @Test
    void testPledgeArrayInitiallyEmpty() {
        Pledge[] pledges = dao.pledgeArray();
        assertNotNull(pledges);
        assertEquals(0, pledges.length);
    }

    @Test
    void testPledgeArrayAfterAddContainsPledge() throws IOException {
        Pledge pledge = new Pledge(10, 1, 100, 5, 0.0);
        dao.addPledge(pledge);

        Pledge[] pledges = dao.pledgeArray();
        assertEquals(1, pledges.length);
        assertEquals(pledge, pledges[0]);
    }

    @Test
    void testHasPledgeById() throws IOException {
        Pledge pledge = new Pledge(10, 1, 100, 5, 0.0);
        dao.addPledge(pledge);

        assertTrue(dao.hasPledge(10));
        assertFalse(dao.hasPledge(999));
    }

    @Test
    void testHasPledgeByObject() throws IOException {
        Pledge pledge = new Pledge(10, 1, 100, 5, 0.0);
        dao.addPledge(pledge);

        assertTrue(dao.hasPledge(new Pledge(10, 1, 100, 999, 0.0))); 
        assertFalse(dao.hasPledge(null));
        assertFalse(dao.hasPledge(new Pledge(11, 1, 100, 5, 0.0)));
    }

    @Test
    void testGetPledgeFound() throws IOException {
        Pledge pledge = new Pledge(10, 1, 100, 5, 0.0);
        dao.addPledge(pledge);
        Pledge found = dao.getPledge(10);
        assertEquals(pledge, found);
    }

    @Test
    void testGetPledgeNotFoundReturnsNull() throws IOException {
        assertNull(dao.getPledge(999));
    }

    // ----------------------------------------------------------------
    // removePledge
    // ----------------------------------------------------------------

    @Test
    void testRemovePledgeByIdSuccess() throws IOException {
        // Setup
        Pledge pledge = new Pledge(10, 1, 100, 5, 0.0);
        dao.addPledge(pledge);

        // Invoke
        boolean result = dao.removePledge(10);

        // Analyze
        assertTrue(result);
        assertFalse(dao.hasPledge(10));
        verify(mockMapper, times(2)).writeValue(any(File.class), any(Pledge[].class)); // add + remove
    }
    @Test
    void testRemovePledgeByIdNotFoundReturnsFalse() throws IOException {
        // Invoke
        boolean result = dao.removePledge(999);

        // Analyze
        assertFalse(result);
        verify(mockMapper, times(0)).writeValue(any(File.class), any(Pledge[].class));
    }

    @Test
    void testRemovePledgeByIdFromEquivalentObjectIdSuccess() throws IOException {
        // Setup
        Pledge pledge = new Pledge(10, 1, 100, 5, 0.0);
        dao.addPledge(pledge);

        // Invoke
        boolean result = dao.removePledge(10);

        // Analyze
        assertTrue(result);
        assertFalse(dao.hasPledge(10));
        verify(mockMapper, times(2)).writeValue(any(File.class), any(Pledge[].class)); 
    }

}