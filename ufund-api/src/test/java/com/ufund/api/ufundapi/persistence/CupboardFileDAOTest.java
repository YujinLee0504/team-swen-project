package com.ufund.api.ufundapi.persistence;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufund.api.ufundapi.model.Need;
import com.ufund.api.ufundapi.model.Pledge;

/**
 * Test the Cupboard File DAO class
 *
 * @author 5E
 */
@Tag("Persistence-tier")
public class CupboardFileDAOTest {
    CupboardFileDAO cupboardFileDAO;
    Need[] testNeeds;
    ObjectMapper mockObjectMapper;

    /**
     * Before each test, we will create and inject a Mock Object Mapper to
     * isolate the tests from the underlying file
     *
     * @throws IOException
     */
    @BeforeEach
    public void setupCupboardFileDAO() throws IOException {
        mockObjectMapper = mock(ObjectMapper.class);
        testNeeds = new Need[3];
        testNeeds[0] = new Need(99, 1, "admin",  "Blankets", "", "🍎",      10.00, 50,  "Clothing", 0.0);
        testNeeds[1] = new Need(100, 1, "admin", "Canned Food", "", "🍎",    2.50, 200, "Food", 0.0);
        testNeeds[2] = new Need(101, 1, "admin", "Food Voucher", "", "🍎",   5.00, 100, "Food", 0.0);

        // When the object mapper is supposed to read from the file
        // the mock object mapper will return the needs array above
        when(mockObjectMapper
            .readValue(new File("doesnt_matter.txt"), Need[].class))
                .thenReturn(testNeeds);
        cupboardFileDAO = new CupboardFileDAO("doesnt_matter.txt", mockObjectMapper);
    }

    @Test
    public void testGetNeeds() throws IOException {
        // Invoke
        Need[] needs = cupboardFileDAO.getNeeds();

        // Analyze
        assertEquals(testNeeds.length, needs.length);
        for (int i = 0; i < testNeeds.length; ++i)
            assertEquals(testNeeds[i], needs[i]);
    }

    @Test
    public void testGetNeedRecord() throws IOException {
        // Invoke
        Need need = cupboardFileDAO.getNeedRecord(99);

        // Analyze
        assertEquals(testNeeds[0], need);
    }

    @Test
    public void testGetArchivedNeedsWhenNothingArchived() throws IOException {
        assertEquals(0, cupboardFileDAO.getArchivedNeeds().length);
    }

    @Test
    public void testFindNeeds() throws IOException {
        // Invoke -- "food" matches "Canned Food" and "Food Voucher"
        Need[] needs = cupboardFileDAO.findNeeds("food");

        // Analyze
        assertEquals(2, needs.length);
        assertEquals(testNeeds[1], needs[0]);
        assertEquals(testNeeds[2], needs[1]);
    }

    @Test
    public void testGetNeed() throws IOException {
        // Invoke
        Need need = cupboardFileDAO.getNeed(99);

        // Analyze
        assertEquals(testNeeds[0], need);
    }

    @Test
    public void testGetNeedNotFound() throws IOException {
        // Invoke
        Need need = cupboardFileDAO.getNeed(98);

        // Analyze
        assertNull(need);
    }

    @Test
    public void testGetNeedsByIds() throws IOException {
        // Invoke
        Need[] needs = cupboardFileDAO.getNeedsByIds(new int[]{99, 101});
        // Analyze
        assertEquals(2, needs.length);
        assertEquals(testNeeds[0], needs[0]);
        assertEquals(testNeeds[2], needs[1]);
    }

    @Test
    public void testDeleteNeed() {
        // Invoke
        boolean result = assertDoesNotThrow(() -> cupboardFileDAO.deleteNeed(99),
                "Unexpected exception thrown");

        // Analyze
        assertEquals(true, result);
        assertEquals(testNeeds.length, cupboardFileDAO.needs.size());
        assertEquals(true, cupboardFileDAO.needs.get(99).isDeleted());
        assertEquals(2, assertDoesNotThrow(() -> cupboardFileDAO.getNeeds().length));
        assertNull(assertDoesNotThrow(() -> cupboardFileDAO.getNeed(99)));
        Need[] archived = assertDoesNotThrow(() -> cupboardFileDAO.getArchivedNeeds());
        assertEquals(1, archived.length);
        assertEquals(99, archived[0].getId());
    }

    @Test
    public void testDeleteNeedNotFound() {
        // Invoke
        boolean result = assertDoesNotThrow(() -> cupboardFileDAO.deleteNeed(98),
                "Unexpected exception thrown");

        // Analyze
        assertEquals(false, result);
        // Map size should be unchanged
        assertEquals(testNeeds.length, cupboardFileDAO.needs.size());
    }

    @Test
    public void testPermanentlyDeletNeed() throws IOException {
        // Setup -- delete need 99 so it can be permanently deleted
        cupboardFileDAO.deleteNeed(99);

        // Invoke
        // Invoke
        Need result = assertDoesNotThrow(() -> cupboardFileDAO.permanentlyDeleteNeed(99),
                "Unexpected exception thrown");

        // Analyze
        // Analyze
        assertEquals(testNeeds[0], result);
        assertEquals(testNeeds.length - 1, cupboardFileDAO.needs.size());
        assertNull(cupboardFileDAO.needs.get(99));
        assertNull(cupboardFileDAO.getNeedRecord(99));
    }

    @Test
    public void testPermanentlyDeleteNeedWhenNotArchivedReturnsNull() throws IOException {
        Need result = assertDoesNotThrow(() -> cupboardFileDAO.permanentlyDeleteNeed(99),
                "Unexpected exception thrown");

        assertNull(result);
        assertEquals(testNeeds.length, cupboardFileDAO.needs.size());
        assertNotNull(cupboardFileDAO.getNeed(99));
    }

    @Test
    public void testRestoreNeed() throws IOException {
        cupboardFileDAO.deleteNeed(99);

        boolean restored = assertDoesNotThrow(() -> cupboardFileDAO.restoreNeed(99),
                "Unexpected exception thrown");

        assertTrue(restored);
        Need n = cupboardFileDAO.needs.get(99);
        assertFalse(n.isDeleted());
        assertEquals(0L, n.getTimeDeleted());
        assertNotNull(cupboardFileDAO.getNeed(99));
        assertEquals(0, cupboardFileDAO.getArchivedNeeds().length);
        assertEquals(testNeeds.length, cupboardFileDAO.getNeeds().length);
    }

    @Test
    public void testRestoreNeedWhenNotArchivedReturnsFalse() throws IOException {
        boolean restored = assertDoesNotThrow(() -> cupboardFileDAO.restoreNeed(100),
                "Unexpected exception thrown");

        assertFalse(restored);
        assertNotNull(cupboardFileDAO.getNeed(100));
    }

    @Test
    public void testRestoreNeedWhenMissingReturnsFalse() throws IOException {
        boolean restored = assertDoesNotThrow(() -> cupboardFileDAO.restoreNeed(98),
                "Unexpected exception thrown");

        assertFalse(restored);
    }

    @Test
    public void testRestoreNeedAfterPermanentDeleteReturnsFalse() throws IOException {
        cupboardFileDAO.deleteNeed(99);
        cupboardFileDAO.permanentlyDeleteNeed(99);

        boolean restored = assertDoesNotThrow(() -> cupboardFileDAO.restoreNeed(99),
                "Unexpected exception thrown");

        assertFalse(restored);
        assertNull(cupboardFileDAO.getNeedRecord(99));
    }

    @Test
    public void testCreateNeed() {
        // Setup
        Need need = new Need(102, 1, "admin", "Winter Coats", "", "🍎", 25.00, 30, "Clothing", 0.0);

        // Invoke
        Need result = assertDoesNotThrow(() -> cupboardFileDAO.createNeed(need),
                "Unexpected exception thrown");

        // Analyze
        assertNotNull(result);
        Need actual = assertDoesNotThrow(() -> cupboardFileDAO.getNeed(result.getId()),
                "Unexpected exception thrown");
        assertNotNull(actual);
        assertEquals(result.getName(),     actual.getName());
        assertEquals(result.getCost(),     actual.getCost());
        assertEquals(result.getQuantity(), actual.getQuantity());
        assertEquals(result.getType(),     actual.getType());
    }

    @Test
    public void testCreateNeedDuplicate() {
        // Setup -- "Blankets" already exists in testNeeds
        Need duplicate = new Need(102, 1, "admin", "Blankets", "", "🍎", 12.00, 10, "Clothing", 0.0);

        // Invoke
        Need result = assertDoesNotThrow(() -> cupboardFileDAO.createNeed(duplicate),
                "Unexpected exception thrown");

        // Analyze -- null signals 409 CONFLICT
        assertNull(result);
        // Map size should be unchanged
        assertEquals(testNeeds.length, cupboardFileDAO.needs.size());
    }

    @Test
    public void testUpdateNeed() {
        // Setup -- update the cost and quantity of an existing need
        Need updated = new Need(99, 1, "admin", "Blankets", "", "🍎", 15.00, 75, "Clothing", 0.0);

        // Invoke
        Need result = assertDoesNotThrow(() -> cupboardFileDAO.updateNeed(updated),
                "Unexpected exception thrown");

        // Analyze
        assertNotNull(result);
        Need actual = assertDoesNotThrow(() -> cupboardFileDAO.getNeed(99),
                "Unexpected exception thrown");
        assertEquals(updated, actual);
    }

    @Test
    public void testUpdateNeedNotFound() {
        // Setup -- id 98 does not exist
        Need need = new Need(98, 1, "admin", "Mystery Item", "", "🍎", 1.00, 1, "Misc", 0.0);

        // Invoke
        Need result = assertDoesNotThrow(() -> cupboardFileDAO.updateNeed(need),
                "Unexpected exception thrown");

        // Analyze
        assertNull(result);
    }

    @Test
    public void testSaveException() throws IOException {
        doThrow(new IOException())
            .when(mockObjectMapper)
                .writeValue(any(File.class), any(Need[].class));

        Need need = new Need(102, 1, "admin", "New Item", "", "🍎", 5.00, 10, "Misc", 0.0);

        assertThrows(IOException.class,
                () -> cupboardFileDAO.createNeed(need),
                "IOException not thrown");
    }

    @Test
    public void testConstructorException() throws IOException {
        // Setup
        ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        // Simulate IOException during JSON deserialization in the load() method
        doThrow(new IOException())
            .when(mockObjectMapper)
                .readValue(new File("doesnt_matter.txt"), Need[].class);

        // Invoke & Analyze
        assertThrows(IOException.class,
                () -> new CupboardFileDAO("doesnt_matter.txt", mockObjectMapper),
                "IOException not thrown");
    }

    @Test
    public void testDistributeCheckoutSuccess() throws IOException {
        Pledge[] checkingOutPledges = new Pledge[] {
            new Pledge(5001, 1, 99, 2, 0.0),
            new Pledge(5002, 1, 100, 4, 3.0)
        };

        assertDoesNotThrow(() -> cupboardFileDAO.distributeCheckout(checkingOutPledges),
                "exception thrown");

        //test if need 99 updated
        assertEquals(2, cupboardFileDAO.getNeed(99).getQuantityFulfilled(), 0.001);

        //test if need 100 updated
        double effectiveQuantity = 4 + 3.0 / cupboardFileDAO.getNeed(100).getCost(); // 4 items plus however many 3$ is worth
        assertEquals(effectiveQuantity, cupboardFileDAO.getNeed(100).getQuantityFulfilled(), 0.001);
    }

    @Test
    public void testDistributeCheckoutIgnoresMissingNeedIds() throws IOException {
        Pledge[] checkingOutPledges = new Pledge[] {
            new Pledge(6001, 1, 99, 1, 0.0),
            new Pledge(6002, 1, 9999, 1, 0.0) // no matching need
        };

        assertDoesNotThrow(() -> cupboardFileDAO.distributeCheckout(checkingOutPledges),
                "exception thrown");

        assertNull(cupboardFileDAO.getNeed(9999));
    }

    @Test
    public void testDistributeCheckoutSaveIOException() throws IOException {
        doThrow(new IOException())
            .when(mockObjectMapper)
                .writeValue(any(File.class), any(Need[].class));

        Pledge[] checkingOutPledges = new Pledge[] {
            new Pledge(7001, 1, 99, 1, 0.0)
        };

        assertThrows(IOException.class,
                () -> cupboardFileDAO.distributeCheckout(checkingOutPledges),
                "IOException not thrown");
    }
}