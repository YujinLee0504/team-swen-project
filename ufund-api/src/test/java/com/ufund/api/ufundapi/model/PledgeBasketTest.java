package com.ufund.api.ufundapi.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.ufund.api.ufundapi.persistence.PledgeCatalogFileDAO;

/**
 * Unit tests for the PledgeBasket model.
 * 
 * @author 5E
 */
@Tag("Model-tier")
class PledgeBasketTest {

    @Test
    void addPledgeAddsToInternalMap() {
        PledgeBasket basket = new PledgeBasket(1, Collections.emptyList());
        Pledge pledge = new Pledge(100, 1, 10, 5, 0.0);
        when(PledgeCatalogFileDAO.INSTANCE.hasPledge(100)).thenReturn(true);
        when(PledgeCatalogFileDAO.INSTANCE.getPledge(100)).thenReturn(pledge);
        boolean result = basket.addPledge(pledge);

        assertTrue(result);
        assertEquals(1, basket.getPledges().size());
        assertTrue(basket.hasPledge(100));
    }

    @Test
    void addPledgeNullReturnsFalse() {
        PledgeBasket basket = new PledgeBasket(1, Collections.emptyList());
        assertFalse(basket.addPledge(null));
    }

    @Test
    void addPledgeDupIdReturnsFalse() {
        PledgeBasket basket = new PledgeBasket(1, Collections.emptyList());
        Pledge p1 = new Pledge(100, 1, 10, 5, 0.0);
        Pledge p2 = new Pledge(100, 1, 11, 2, 0.0);
        when(PledgeCatalogFileDAO.INSTANCE.hasPledge(100)).thenReturn(true);
        when(PledgeCatalogFileDAO.INSTANCE.getPledge(100)).thenReturn(p1);
        basket.addPledge(p1);
        boolean result = basket.addPledge(p2);

        assertFalse(result);
        assertEquals(1, basket.getPledges().size());
    }

    @Test
    void updatePledgeSuccess() throws IOException {
        PledgeBasket basket = new PledgeBasket(1, Collections.emptyList());
        Pledge originalPledge = new Pledge(10, 1, 100, 5, 0.0);
        when(PledgeCatalogFileDAO.INSTANCE.hasPledge(10)).thenReturn(true);
        when(PledgeCatalogFileDAO.INSTANCE.getPledge(10)).thenReturn(originalPledge);
        basket.addPledge(originalPledge);
        Pledge updatedPledge = new Pledge(10, 1, 100, 15, 0.0); // Quant changed to 15
        when(PledgeCatalogFileDAO.INSTANCE.getPledge(10)).thenReturn(updatedPledge);

        boolean result = basket.updatePledge(updatedPledge);

        assertTrue(result);
        assertEquals(15, basket.getPledge(10).getQuantity());
    }

    @Test
    void updatePledgeNotFoundReturnsFalse() {
        PledgeBasket basket = new PledgeBasket(1, Collections.emptyList());
        Pledge nonExistentPledge = new Pledge(99, 1, 100, 5, 0.0);

        assertFalse(basket.updatePledge(nonExistentPledge));
    }

    @Test
    void testHasPledgesuccess() {
        PledgeBasket basket = new PledgeBasket(1, Collections.emptyList());
        Pledge probe = new Pledge(10, 1, 100, 5, 0.0);
        assertFalse(basket.hasPledge(10));
        assertFalse(basket.hasPledge(probe));

        Pledge pledge = new Pledge(10, 1, 100, 5, 0.0);
        when(PledgeCatalogFileDAO.INSTANCE.hasPledge(10)).thenReturn(true);
        when(PledgeCatalogFileDAO.INSTANCE.getPledge(10)).thenReturn(pledge);
        basket.addPledge(pledge);

        assertTrue(basket.hasPledge(10));
        assertTrue(basket.hasPledge(probe));
    }

    @Test
    void testGetOwnerId() {
        PledgeBasket basket = new PledgeBasket(42, Collections.emptyList());
        assertEquals(42, basket.getOwnerId());
    }

    @Test
    void testHasPledgeByIdAndObject() {
        PledgeBasket basket = new PledgeBasket(1, Collections.emptyList());
        Pledge pledge = new Pledge(10, 1, 100, 5, 0.0);

        assertFalse(basket.hasPledge(10));
        assertFalse(basket.hasPledge(pledge));

        assertTrue(basket.addPledge(pledge));
        assertTrue(basket.hasPledge(10));
        assertTrue(basket.hasPledge(new Pledge(10, 1, 100, 999, 0.0))); // same id
        assertFalse(basket.hasPledge(11));
    }

    @Test
    void testHasPledgeNullObjectReturnsFalse() {
        PledgeBasket basket = new PledgeBasket(1, Collections.emptyList());
        assertFalse(basket.hasPledge((Pledge) null));
    }

    @Test
    void testHasPledgeReturnsFalseAfterRemove() {
        PledgeBasket basket = new PledgeBasket(1, Collections.emptyList());
        Pledge pledge = new Pledge(10, 1, 100, 5, 0.0);
        assertTrue(basket.addPledge(pledge));
        assertTrue(basket.hasPledge(10));

        assertTrue(basket.removePledge(10));
        assertFalse(basket.hasPledge(10));
        assertFalse(basket.hasPledge(pledge));
    }

    @Test
    void testPledgeArrayResolvesFromCatalog() {
        PledgeCatalogFileDAO.INSTANCE = mock(PledgeCatalogFileDAO.class);
        PledgeBasket basket = new PledgeBasket(1, Collections.emptyList());
        Pledge pledge = new Pledge(10, 1, 100, 5, 0.0);
        assertTrue(basket.addPledge(pledge));

        when(PledgeCatalogFileDAO.INSTANCE.hasPledge(10)).thenReturn(true);
        when(PledgeCatalogFileDAO.INSTANCE.getPledge(10)).thenReturn(pledge);

        Pledge[] pledges = basket.pledgeArray();
        assertNotNull(pledges);
        assertEquals(1, pledges.length);
        assertEquals(pledge, pledges[0]);
    }

    @Test
    void testPledgeArraySkipsMissingCatalogEntries() {
        PledgeCatalogFileDAO.INSTANCE = mock(PledgeCatalogFileDAO.class);

        PledgeBasket basket = new PledgeBasket(1, Collections.emptyList());
        Pledge pledge = new Pledge(10, 1, 100, 5, 0.0);
        assertTrue(basket.addPledge(pledge));

        when(PledgeCatalogFileDAO.INSTANCE.hasPledge(10)).thenReturn(false);

        Pledge[] pledges = basket.pledgeArray();
        assertNotNull(pledges);
        assertEquals(0, pledges.length);
    }

    @Test
    void testGetPledgesResolvesFromCatalog() {
        PledgeCatalogFileDAO.INSTANCE = mock(PledgeCatalogFileDAO.class);

        PledgeBasket basket = new PledgeBasket(1, Collections.emptyList());
        Pledge pledge = new Pledge(10, 1, 100, 5, 0.0);
        assertTrue(basket.addPledge(pledge));

        when(PledgeCatalogFileDAO.INSTANCE.hasPledge(10)).thenReturn(true);
        when(PledgeCatalogFileDAO.INSTANCE.getPledge(10)).thenReturn(pledge);

        Collection<Pledge> pledges = basket.getPledges();
        assertNotNull(pledges);
        assertEquals(1, pledges.size());
        assertEquals(pledge, pledges.iterator().next());
    }

    // ----------------------------------------------------------------
    // removePledge / removeAllPledges
    // ----------------------------------------------------------------

    @Test
    void testRemovePledgeById() {
        PledgeBasket basket = new PledgeBasket(1, Collections.emptyList());
        Pledge pledge = new Pledge(10, 1, 100, 5, 0.0);
        assertTrue(basket.addPledge(pledge));

        assertTrue(basket.removePledge(10));
        assertFalse(basket.hasPledge(10));
        assertFalse(basket.removePledge(10)); // already removed
    }

    @Test
    void testRemovePledgeByIdEquivalent() {
        PledgeBasket basket = new PledgeBasket(1, Collections.emptyList());
        Pledge pledge = new Pledge(10, 1, 100, 5, 0.0);
        assertTrue(basket.addPledge(pledge));

        assertTrue(basket.removePledge(10));
        assertFalse(basket.hasPledge(10));
        assertFalse(basket.removePledge(10)); // already removed
    }

    @Test
    void testRemoveAllPledgesClearsBasket() {
        PledgeBasket basket = new PledgeBasket(1, Collections.emptyList());
        assertTrue(basket.addPledge(new Pledge(10, 1, 100, 5, 0.0)));
        assertTrue(basket.addPledge(new Pledge(11, 1, 101, 5, 0.0)));
        assertTrue(basket.hasPledge(10));
        assertTrue(basket.hasPledge(11));

        basket.removeAllPledges();

        assertFalse(basket.hasPledge(10));
        assertFalse(basket.hasPledge(11));
    }

    @Test
    void testAddPledgeWrongOwnerReturnsFalse() {
        PledgeBasket basket = new PledgeBasket(1, Collections.emptyList());
        Pledge intruderPledge = new Pledge(100, 99, 10, 5, 0.0);

        assertFalse(basket.addPledge(intruderPledge), "Should not add pledge belonging to another user");
        assertEquals(0, basket.pledgeArray().length);
    }

    @Test
    void testEqualsAndHashCode() {
        PledgeBasket basket1 = new PledgeBasket(1, List.of(10, 20));
        PledgeBasket basket2 = new PledgeBasket(1, List.of(10, 20));
        PledgeBasket basket3 = new PledgeBasket(2, List.of(10, 20)); // Different owner

        assertEquals(basket1, basket2);
        assertEquals(basket1.hashCode(), basket2.hashCode());
        assertNotEquals(basket1, basket3);
    }

    @Test
    void testCheckoutClearsBasket() {
        PledgeCatalogFileDAO.INSTANCE = mock(PledgeCatalogFileDAO.class);
        PledgeBasket basket = new PledgeBasket(1, Collections.emptyList());
        Pledge p1 = new Pledge(10, 1, 100, 5, 0.0);

        when(PledgeCatalogFileDAO.INSTANCE.hasPledge(10)).thenReturn(true);
        when(PledgeCatalogFileDAO.INSTANCE.getPledge(10)).thenReturn(p1);
        basket.addPledge(p1);

        Pledge[] results = basket.checkout();

        assertEquals(1, results.length);
        assertEquals(0, basket.pledgeArray().length, "Basket should be empty after checkout");
    }

    @Test
    void testConstructorWithNullPledgeIds() {
        assertDoesNotThrow(() -> {
            PledgeBasket basket = new PledgeBasket(1, null);
            assertNotNull(basket.pledgeArray());
        }, "Constructor should handle null pledgeIds gracefully");
    }

    // ----------------------------------------------------------------
    // equals, toString
    // ----------------------------------------------------------------

    @Test
    void testEqualsSameObject() {
        PledgeBasket basket = new PledgeBasket(1, Collections.emptyList());
        PledgeBasket sameRef = basket;
        assertEquals(basket, sameRef);
    }

    @Test
    void testEqualsNull() {
        PledgeBasket basket = new PledgeBasket(1, Collections.emptyList());
        assertNotEquals(basket, null);
    }

    @Test
    void testEqualsDifferentClass() {
        PledgeBasket basket = new PledgeBasket(1, Collections.emptyList());
        assertNotEquals(basket, "not a basket");
    }

    @Test
    void testEqualsDifferentPledgeIds() {
        PledgeBasket left = new PledgeBasket(1, List.of(10, 20));
        PledgeBasket right = new PledgeBasket(1, List.of(10, 30));
        assertNotEquals(left, right);
    }

    @Test
    void testToString() {
        PledgeBasket basket = new PledgeBasket(1, List.of(10, 20));
        String str = basket.toString();
        assertNotNull(str);
        assertTrue(str.contains("1"));
        assertTrue(str.contains("10, 20"));
    }
}