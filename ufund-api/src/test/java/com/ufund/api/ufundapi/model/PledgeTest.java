package com.ufund.api.ufundapi.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for Pledge.class
 */
@Tag("Model-tier")
public class PledgeTest {

    @Test
    public void testConstructor() {
        int id = 1;
        int ownerId = 100;
        int needId = 50;
        int quantity = 10;
        double money = 25.50;

        Pledge pledge = new Pledge(id, ownerId, needId, quantity, money);

        assertEquals(id, pledge.getId());
        assertEquals(ownerId, pledge.getOwnerId());
        assertEquals(needId, pledge.getNeedId());
        assertEquals(quantity, pledge.getQuantity());
        assertEquals(money, pledge.getMoney());
    }

    @Test
    public void testSetQuantity() {
        Pledge pledge = new Pledge(1, 100, 50, 5, 0.0);
        int newQuantity = 15;

        pledge.setQuantity(newQuantity);

        assertEquals(newQuantity, pledge.getQuantity());
    }

    @Test
    public void testSetMoney() {
        Pledge pledge = new Pledge(1, 100, 50, 0, 10.0);
        double newMoney = 100.0;

        pledge.setMoney(newMoney);

        assertEquals(newMoney, pledge.getMoney());
    }

    @Test
    public void testEqualsSameId() {
        Pledge pledge1 = new Pledge(1, 100, 50, 10, 5.0);
        Pledge pledge2 = new Pledge(1, 200, 60, 20, 10.0);

        assertEquals(pledge1, pledge2);
        assertEquals(pledge1.hashCode(), pledge2.hashCode());
    }

    @Test
    public void testNotEqualDifferentId() {
        Pledge pledge1 = new Pledge(1, 100, 50, 10, 5.0);
        Pledge pledge2 = new Pledge(2, 100, 50, 10, 5.0);

        assertNotEquals(pledge1, pledge2);
        assertNotEquals(pledge1.hashCode(), pledge2.hashCode());
    }

    @Test
    public void testEqualsNullAndOtherClass() {
        Pledge pledge = new Pledge(1, 100, 50, 10, 5.0);

        assertNotEquals(null, pledge);
        assertNotEquals("Not a pledge", pledge);
    }

    @Test
    public void testToString() {
        int id = 1;
        int ownerId = 100;
        int needId = 50;
        int quantity = 10;
        double money = 25.50;
        Pledge pledge = new Pledge(id, ownerId, needId, quantity, money);

        String expectedString = String.format(Pledge.STRING_FORMAT, id, ownerId, needId, quantity, money);

        assertEquals(expectedString, pledge.toString());
    }
}