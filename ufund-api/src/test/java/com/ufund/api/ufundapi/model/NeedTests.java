package com.ufund.api.ufundapi.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("Model-tier")
public class NeedTests {

    Need need1;
    Need need2;

    @BeforeEach
    public void setupNeed() {
        need1 = new Need(1, 1, "admin", "Penne", "", "🍎", 0.5, 100, "Pasta", 0.0);
        need2 = new Need(2, 1, "admin", "Ritz", "", "🍎", 1.0, 50, "Cracker", 0.0);
    }

    @Test
    public void testConstructor() {

        assertEquals(1, need1.getId(), "Need ID not constructed properly or getter not functional");
        assertEquals("Penne", need1.getName(), "Need Name not constructed properly or getter not functional");
        assertEquals(0.5, need1.getCost(), "Need Cost not constructed properly or getter not functional");
        assertEquals(100, need1.getQuantity(), "Need Quantity not constructed properly or getter not functional");
        assertEquals("Pasta", need1.getType(), "Need Type not constructed properly or getter not functional");
    }

    @Test
    public void testSetters() {

        need1.setName("Garlic");
        need1.setCost(2.0);
        need1.setQuantity(10);
        need1.setType("Vampire Repellant");
        need1.setQuantityFulfilled(5.6);

        assertEquals("Garlic", need1.getName(), "Need Name setter did not set properly");
        assertEquals(2.0, need1.getCost(), "Need Cost setter did not set properly");
        assertEquals(10, need1.getQuantity(), "Need Quantity setter did not set properly");
        assertEquals("Vampire Repellant", need1.getType(), "Need Type setter did not set properly");
    }

    @Test
    public void testToString() {
        Need need = new Need(1, 1, "admin", "Radishes", "", "🍎", 2.50, 10, "Vegetable", 0.0);
        String testingString = String.format(Need.STRING_FORMAT, 1, "Radishes", 2.50, 10, "Vegetable");

        String actualString = need.toString();

        assertEquals(testingString, actualString, "The object string should string correctly"); // :P
    }

    @Test
    public void testCompletionPercentage() {
        assertEquals(0, need2.completionPercentage(), 0.0001);
    }

    @Test
    public void testCompletionPercentageEdgeCase() {
        Need chicken = new Need(3, 1, "admin", "Chicken", "", "🍎", 3, 0, "Meat", 20.0);
        assertEquals(100, chicken.completionPercentage(), 0.0001);
    }

    @Test
    public void testIsComplete() {
        Need notCompletedNeed = new Need(3, 1, "admin", "Chicken", "", "🍎", 3, 5, "Meat", 2.3);
        Need completedNeed = new Need(4, 1, "admin", "Beef", "", "🍎", 3, 5, "Meat", 5);
        Need overCompletedNeed = new Need(5, 1, "admin", "Pork", "", "🍎", 3, 5, "Meat", 10.865);
        assertFalse(notCompletedNeed.isComplete());
        assertTrue(completedNeed.isComplete());
        assertTrue(overCompletedNeed.isComplete());
    }
    

    public void testArchiveMarkDeletedClearDeletedAndTimeDeleted() {
        assertFalse(need1.isDeleted());
        assertEquals(0L, need1.getTimeDeleted());

        need1.markDeleted();
        assertTrue(need1.isDeleted());
        assertTrue(need1.getTimeDeleted() > 0L);

        need1.clearDeleted();
        assertFalse(need1.isDeleted());
        assertEquals(0L, need1.getTimeDeleted());

        need1.setDeleted(true);
        need1.setTimeDeleted(42L);
        assertTrue(need1.isDeleted());
        assertEquals(42L, need1.getTimeDeleted());
    }

}