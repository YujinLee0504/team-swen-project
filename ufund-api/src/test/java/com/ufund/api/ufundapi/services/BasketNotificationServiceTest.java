package com.ufund.api.ufundapi.services;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("Service-tier")
class BasketNotificationServiceTest {

    private BasketNotificationService service;

    @BeforeEach
    void setUp() {
        service = new BasketNotificationService();
    }

    @Test
    void drainMessagesWhenEmptyReturnsEmptyArray() {
        assertArrayEquals(new String[0], service.drainMessages(1));
    }

    @Test
    void addArchivedNeedRemovedMessageThenDrainReturnsFormattedText() {
        service.addArchivedNeedRemovedMessage(7, "Soup");

        String[] out = service.drainMessages(7);

        assertEquals(1, out.length);
        assertEquals(
                "The food bank removed \"Soup\" from the cupboard. It was removed from your pledge basket.",
                out[0]);
    }

    @Test
    void drainClearsQueueSecondDrainIsEmpty() {
        service.addArchivedNeedRemovedMessage(1, "A");

        assertEquals(1, service.drainMessages(1).length);
        assertArrayEquals(new String[0], service.drainMessages(1));
    }

    @Test
    void multipleMessagesForSameOwnerDrainInOrder() {
        service.addArchivedNeedRemovedMessage(2, "One");
        service.addArchivedNeedRemovedMessage(2, "Two");

        String[] out = service.drainMessages(2);

        assertEquals(2, out.length);
        assertTrue(out[0].contains("One"));
        assertTrue(out[1].contains("Two"));
    }

    @Test
    void addArchivedNeedRemovedMessagesNullIsNoOp() {
        service.addArchivedNeedRemovedMessages(null, "X");

        assertArrayEquals(new String[0], service.drainMessages(1));
    }

    @Test
    void addArchivedNeedRemovedMessagesEmptyArrayIsNoOp() {
        service.addArchivedNeedRemovedMessages(new int[0], "X");

        assertArrayEquals(new String[0], service.drainMessages(1));
    }

    @Test
    void addArchivedNeedRemovedMessagesNotifiesEachOwner() {
        service.addArchivedNeedRemovedMessages(new int[] { 10, 20 }, "Rice");

        String[] a = service.drainMessages(10);
        String[] b = service.drainMessages(20);

        assertEquals(1, a.length);
        assertEquals(1, b.length);
        assertTrue(a[0].contains("Rice"));
        assertTrue(b[0].contains("Rice"));
        assertArrayEquals(new String[0], service.drainMessages(10));
    }

    @Test
    void ownersAreIndependent() {
        service.addArchivedNeedRemovedMessage(1, "OnlyOne");

        assertArrayEquals(new String[0], service.drainMessages(99));
        assertEquals(1, service.drainMessages(1).length);
    }
}
