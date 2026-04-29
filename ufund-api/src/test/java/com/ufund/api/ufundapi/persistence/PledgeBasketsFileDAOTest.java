package com.ufund.api.ufundapi.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.ufund.api.ufundapi.model.Pledge;
import com.ufund.api.ufundapi.model.PledgeBasket;

/**
 * Test the PledgeBaskets File DAO class
 *
 * @author 5E
 */
@Tag("Persistence-tier")
class PledgeBasketsFileDAOTest {
    private PledgeBasketsFileDAO dao;
    private ObjectMapper mockMapper;
    private PledgeCatalogDAO mockCatalog;
    private Path existingFilePath;

    @BeforeEach
    public void initializeTest() throws Exception{
        mockMapper = mock(ObjectMapper.class);
        mockCatalog = mock(PledgeCatalogDAO.class);
        existingFilePath = Files.createTempFile("pledge-baskets-dao-test-", ".json");
        when(mockMapper.readValue(any(File.class), eq(PledgeBasket[].class))).thenReturn(new PledgeBasket[0]);
        dao = new PledgeBasketsFileDAO(existingFilePath.toString(), mockMapper, mockCatalog);
    }

    // ------------------------------
    // load/save
    // ------------------------------

    @Test
    void testSaveNullObjectMapperThrowsException() throws IOException {
        PledgeBasketsFileDAO nullMapperDAO = new PledgeBasketsFileDAO(existingFilePath.toString(), null, mockCatalog);
        assertFalse(nullMapperDAO.addPledgeBasket(new PledgeBasket(1, Collections.emptyList())));
    }

    @Test
    void testLoadWhenFileExistsReadsBaskets() throws IOException {
        PledgeBasket[] loaded = new PledgeBasket[] {
                new PledgeBasket(9, Collections.emptyList())
        };
        ObjectMapper loadMapper = mock(ObjectMapper.class);
        when(loadMapper.readValue(any(File.class), eq(PledgeBasket[].class))).thenReturn(loaded);

        PledgeBasketsFileDAO loadedDao = new PledgeBasketsFileDAO(existingFilePath.toString(), loadMapper, mockCatalog);

        assertTrue(loadedDao.hasPledgeBasketWithOwner(9));
        verify(loadMapper, times(1)).readValue(any(File.class), eq(PledgeBasket[].class));
    }

    @Test
    void testSaveWhenFileMissingReturnsFalse() throws IOException {
        String missingPath = existingFilePath.toString() + "-missing2";
        PledgeBasketsFileDAO missingFileDao = new PledgeBasketsFileDAO(missingPath, mockMapper, mockCatalog);

        boolean result = missingFileDao.addPledgeBasket(new PledgeBasket(55, Collections.emptyList()));
        assertFalse(result);
        assertFalse(missingFileDao.hasPledgeBasketWithOwner(55));
    }

    // ------------------------------
    // add
    // ------------------------------

    @Test
    public void testAddPledgeBasketNullMapperFailure() throws IOException {
        PledgeBasketsFileDAO nullMapperDAO = new PledgeBasketsFileDAO(existingFilePath.toString(), null, mockCatalog);
        boolean result = nullMapperDAO.addPledgeBasket(null);

        assertFalse(result);
    }

    @Test
    public void testAddPledgeBasketNewOwnerSuccess() throws IOException {
        PledgeBasket basket = new PledgeBasket(5, Collections.emptyList());
        
        boolean result = dao.addPledgeBasket(basket);

        assertTrue(result);
        assertTrue(dao.hasPledgeBasketWithOwner(5));
        verify(mockMapper).writeValue(any(File.class), any(PledgeBasket[].class));

    }

    @Test
    void testAddPledgeBasketIOException() throws IOException {
        ObjectMapper throwingMapper = mock(ObjectMapper.class);
        when(throwingMapper.readValue(any(File.class), eq(PledgeBasket[].class))).thenReturn(new PledgeBasket[0]);
        doThrow(new IOException("write failed")).when(throwingMapper).writeValue(any(File.class), any(PledgeBasket[].class));

        PledgeBasketsFileDAO failingDao = new PledgeBasketsFileDAO(existingFilePath.toString(), throwingMapper, mockCatalog);
        PledgeBasket basket = new PledgeBasket(77, Collections.emptyList());

        boolean result = failingDao.addPledgeBasket(basket);

        assertFalse(result);
        assertFalse(failingDao.hasPledgeBasketWithOwner(77));
    }

    @Test
    public void testAddPledgeBasketExistingOwnerFailure() throws IOException{
        PledgeBasket basket = new PledgeBasket(5, Collections.emptyList());
        dao.addPledgeBasket(basket);

        boolean result = dao.addPledgeBasket(new PledgeBasket(5, Collections.emptyList()));

        assertFalse(result);
    }

        
    // ----------------------------------------------------------------
    // getPledgeBaskets
    // ----------------------------------------------------------------

    @Test
    void testGetPledgeBasketsEmpty() {
        // Invoke
        PledgeBasket[] baskets = dao.getPledgeBaskets();

        // Analyze
        assertNotNull(baskets);
        assertEquals(0, baskets.length);
    }

    @Test
    void testGetPledgeBasketByOwnerFound() {
        dao.addPledgeBasket(new PledgeBasket(1, Collections.emptyList()));
        // Invoke
        PledgeBasket basket = dao.getPledgeBasketByOwner(1);

        // Analyze
        assertNotNull(basket);
        assertEquals(1, basket.getOwnerId());
    }

    @Test
    void testGetPledgeBasketByOwnerNotFound() {
        // Invoke
        PledgeBasket basket = dao.getPledgeBasketByOwner(999);

        // Analyze
        assertNull(basket);
    }

    // ----------------------------------------------------------------
    // updatePledgeBasket
    // ----------------------------------------------------------------
   
    @Test
    void testUpdatePledgeBasketNullFailure() throws IOException {
        boolean result = dao.updatePledgeBasket(null);
        assertFalse(result);
    }

    @Test
    void testUpdatePledgeBasketNotInMapReturnsFalse() throws IOException {
        dao.addPledgeBasket(new PledgeBasket(3, Collections.emptyList()));
        PledgeBasket basket = new PledgeBasket(99, Collections.emptyList());

        boolean result = dao.updatePledgeBasket(basket);

        assertFalse(result);
        verify(mockMapper, times(1)).writeValue(any(File.class), any(PledgeBasket[].class));
    }

    @Test
    void testUpdatePledgeBasketSuccessPersists() throws IOException {
        int ownerId = 5;
        PledgeBasket basket = new PledgeBasket(ownerId, Collections.emptyList());
        assertTrue(dao.addPledgeBasket(basket));

        boolean result = dao.updatePledgeBasket(basket);

        assertTrue(result);
        verify(mockMapper, times(2)).writeValue(any(File.class), any(PledgeBasket[].class));
    }

    // ----------------------------------------------------------------
    // removePledgeBasket
    // ----------------------------------------------------------------

    @Test 
    void testRemovePledgeBasketNullFailure() throws IOException {
        boolean result = dao.removePledgeBasket(null);
        assertFalse(result);
    }

    @Test
    void testRemovePledgeBasketSuccess() throws IOException {
        dao.addPledgeBasket(new PledgeBasket(1, Collections.emptyList()));
        PledgeBasket basket = dao.getPledgeBasketByOwner(1);

        boolean result = dao.removePledgeBasket(basket);

        assertTrue(result);
        assertNull(dao.getPledgeBasketByOwner(1));
        verify(mockMapper, times(2)).writeValue(any(File.class), any(PledgeBasket[].class));
    }

    @Test
    void testRemovePledgeBasketNotFoundReturnsFalse() throws IOException {
        // Setup: basket not present
        PledgeBasket basket = new PledgeBasket(999, Collections.emptyList());

        // Invoke
        boolean result = dao.removePledgeBasket(basket);

        // Analyze
        assertFalse(result);
        verify(mockMapper, times(0)).writeValue(any(File.class), any(PledgeBasket[].class));
    }

    @Test
    void testRemovePledgeBasketSaveFailure() throws IOException {
        PledgeBasket basket = new PledgeBasket(42, Collections.emptyList());
        assertTrue(dao.addPledgeBasket(basket));
        PledgeBasket stored = dao.getPledgeBasketByOwner(42);
        assertNotNull(stored);

        Files.delete(existingFilePath);

        boolean result = dao.removePledgeBasket(stored);

        assertFalse(result);
        assertTrue(dao.hasPledgeBasketWithOwner(42));
        assertSame(stored, dao.getPledgeBasketByOwner(42));
        verify(mockMapper, times(1)).writeValue(any(File.class), any(PledgeBasket[].class));
    }


    @Test
    void removePledgesForNeedWhenNoResolvedPledgesReturnsEmpty() throws IOException {
        dao.addPledgeBasket(new PledgeBasket(3, Collections.emptyList()));
        dao.addPledgeBasket(new PledgeBasket(2, Collections.emptyList()));

        int[] affectedOwners = dao.removePledgesForNeed(123);

        assertArrayEquals(new int[0], affectedOwners);
        verify(mockCatalog, never()).removePledge(anyInt());
    }

    @Test
    void testRemovePledgesForNeed() throws IOException {
        PledgeCatalogFileDAO previousInstance = PledgeCatalogFileDAO.INSTANCE;
        Path pledgesFile = Files.createTempFile("pledges-rpfn-", ".json");
        Path basketsFile = Files.createTempFile("baskets-rpfn-", ".json");
        Files.writeString(pledgesFile, "[]", StandardCharsets.UTF_8);
        Files.writeString(basketsFile, "[]", StandardCharsets.UTF_8);

        try {
            ObjectMapper mapper = new ObjectMapper();
            PledgeCatalogFileDAO catalog = new PledgeCatalogFileDAO(pledgesFile.toString(), mapper);
            int needId = 30;
            assertTrue(catalog.addPledge(new Pledge(10, 1, needId, 2, 0.0)));
            assertTrue(catalog.addPledge(new Pledge(11, 1, needId, 1, 0.0)));
            assertTrue(catalog.addPledge(new Pledge(12, 2, needId, 1, 0.0)));

            PledgeBasketsFileDAO basketsDao = new PledgeBasketsFileDAO(basketsFile.toString(), mapper, catalog);
            assertTrue(basketsDao.addPledgeBasket(new PledgeBasket(1, List.of(10, 11))));
            assertTrue(basketsDao.addPledgeBasket(new PledgeBasket(2, List.of(12))));

            int[] affectedOwners = basketsDao.removePledgesForNeed(needId);

            assertArrayEquals(new int[] { 1, 2 }, affectedOwners);
            assertFalse(catalog.hasPledge(10));
            assertFalse(catalog.hasPledge(11));
            assertFalse(catalog.hasPledge(12));
            assertTrue(basketsDao.getPledgeBasketByOwner(1).getPledges().isEmpty());
            assertTrue(basketsDao.getPledgeBasketByOwner(2).getPledges().isEmpty());
        } finally {
            PledgeCatalogFileDAO.INSTANCE = previousInstance;
            Files.deleteIfExists(pledgesFile);
            Files.deleteIfExists(basketsFile);
        }
    }



    // ----------------------------------------------------------------
    // checkout
    // ----------------------------------------------------------------

    @Test   
    void testCheckoutPledgeBasketNoPledgeBasket() throws IOException {
        CupboardDAO cupboardDao = mock(CupboardDAO.class);
        boolean result = dao.checkoutPledgeBasket(999, cupboardDao);
        assertFalse(result);
        verify(cupboardDao, never()).distributeCheckout(any(Pledge[].class));
    }

    @Test
    void testCheckoutPledgeBasketSuccess() throws IOException {
        CupboardDAO cupboardDao = mock(CupboardDAO.class);
        PledgeBasket basket = new PledgeBasket(7, Collections.emptyList());
        assertTrue(dao.addPledgeBasket(basket));

        boolean result = dao.checkoutPledgeBasket(7, cupboardDao);

        assertTrue(result);
        verify(cupboardDao, times(1)).distributeCheckout(any(Pledge[].class));
        verify(mockMapper, times(2)).writeValue(any(File.class), any(PledgeBasket[].class)); // add + checkout save
    }

    @Test
    void testCheckoutPledgeBasketSaveFailureReturnsFalse() throws IOException {
        CupboardDAO cupboardDao = mock(CupboardDAO.class);
        PledgeBasket basket = new PledgeBasket(8, Collections.emptyList());
        assertTrue(dao.addPledgeBasket(basket));

        Files.delete(existingFilePath); // force save() to return false
        boolean result = dao.checkoutPledgeBasket(8, cupboardDao);

        assertFalse(result);
        verify(cupboardDao, times(1)).distributeCheckout(any(Pledge[].class));
    }

}