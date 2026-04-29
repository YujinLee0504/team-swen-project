package com.ufund.api.ufundapi.services;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ufund.api.ufundapi.model.Need;
import com.ufund.api.ufundapi.persistence.CupboardDAO;
import com.ufund.api.ufundapi.persistence.PledgeBasketsDAO;

@Tag("Controller-tier")
public class CupboardServiceTest {
    private CupboardDAO mockCupboardDAO;
    private PledgeBasketsDAO mockPledgeBasketsDAO;
    private BasketNotificationService mockBasketNotificationService;
    private CupboardService cupboardService;

    @BeforeEach
    public void initializeCupboardService() {
        mockCupboardDAO = mock(CupboardDAO.class);
        mockPledgeBasketsDAO = mock(PledgeBasketsDAO.class);
        mockBasketNotificationService = mock(BasketNotificationService.class);
        cupboardService = new CupboardService(mockCupboardDAO, mockPledgeBasketsDAO, mockBasketNotificationService);
    }

    // getNeed

    @Test
    public void testGetNeed() throws IOException {
        Need need = new Need(10, 1, "admin", "Hot Dogs", "", "🍎", 6.99, 5, "Food", 0.0);
        when(mockCupboardDAO.getNeed(need.getId())).thenReturn(need);

        Need response = cupboardService.getNeed(need.getId());

        assertEquals(need, response);
    }

    @Test
    public void testGetNeedNotFound() throws IOException {
        int testId = 100;
        when(mockCupboardDAO.getNeed(testId)).thenReturn(null);

        Need response = cupboardService.getNeed(testId);

        assertEquals(null, response);
    }

    @Test
    public void testGetNeedException() throws IOException {
        int testId = 100;
        doThrow(new IOException()).when(mockCupboardDAO).getNeed(testId);
        assertThrows(IOException.class, () -> cupboardService.getNeed(testId));
    }

    // getNeeds

    @Test
    public void testGetNeeds() throws IOException {
        Need[] needs = new Need[2];
        needs[0] = new Need(99, 1, "admin",  "Blankets", "", "🍎",    10.00, 50,  "Clothing", 0.0);
        needs[1] = new Need(100, 1, "admin", "Canned Food", "", "🍎",  2.50, 200, "Food", 0.0);
        when(mockCupboardDAO.getNeeds()).thenReturn(needs);

        Need[] response = cupboardService.getNeeds();

        assertArrayEquals(needs, response);
    }

    @Test
    public void testGetNeedsByIds() throws IOException {
        Need[] needs = new Need[2];
        needs[0] = new Need(99, 1, "admin",  "Blankets", null, "🍎",    10.00, 50,  "Clothing", 0.0);
        needs[1] = new Need(100, 1, "admin", "Canned Food", null, "🍎",  2.50, 200, "Food", 0.0);
        when(mockCupboardDAO.getNeedsByIds(new int[]{99, 100})).thenReturn(needs);
        Need[] response = cupboardService.getNeedsByIds(new int[]{99, 100});
        assertArrayEquals(needs, response);
    }
    public void testGetArchivedNeeds() throws IOException {
        Need[] needs = new Need[2];
        needs[0] = new Need(99,  "Blankets",    10.00, 50,  "Clothing");
        needs[1] = new Need(100, "Canned Food",  2.50, 200, "Food");
        when(mockCupboardDAO.getArchivedNeeds()).thenReturn(needs);

        Need[] response = cupboardService.getArchivedNeeds();

        assertArrayEquals(needs, response);
     }

     @Test
     public void testGetArchivedNeedsException() throws IOException {
         doThrow(new IOException()).when(mockCupboardDAO).getArchivedNeeds();
         assertThrows(IOException.class, () -> cupboardService.getArchivedNeeds());
     }

     @Test
     public void testRestoreNeed() throws IOException {
         int id = 99;
         Need need = new Need(id, "Blankets", 10.00, 50, "Clothing");
         when(mockCupboardDAO.restoreNeed(id)).thenReturn(true);
         when(mockCupboardDAO.getNeed(id)).thenReturn(need);

         Need response = cupboardService.restoreNeed(id);

         assertEquals(need, response);
     }

     @Test
     public void testRestoreNeedNotFound() throws IOException {
         int id = 99;
         when(mockCupboardDAO.restoreNeed(id)).thenReturn(false);

         Need response = cupboardService.restoreNeed(id);

         assertNull(response);
     }

     @Test
     public void testRestoreNeedWhenGetNeedNullAfterRestore() throws IOException {
         int id = 99;
         when(mockCupboardDAO.restoreNeed(id)).thenReturn(true);
         when(mockCupboardDAO.getNeed(id)).thenReturn(null);

         Need response = cupboardService.restoreNeed(id);

         assertNull(response);
     }

     @Test
     public void testRestoreNeedException() throws IOException {
         int id = 99;
         doThrow(new IOException()).when(mockCupboardDAO).restoreNeed(id);

         assertThrows(IOException.class, () -> cupboardService.restoreNeed(id));
     }

    @Test
    public void testGetNeedsException() throws IOException {
        doThrow(new IOException()).when(mockCupboardDAO).getNeeds();
        assertThrows(IOException.class, () -> cupboardService.getNeeds());
    }

    // findNeeds

    @Test
    public void testFindNeeds() throws IOException {
        String searchString = "food";
        Need[] needs = new Need[2];
        needs[0] = new Need(100, 1, "admin", "Canned Food", "", "🍎",  2.50, 200, "Food", 0.0);
        needs[1] = new Need(101, 1, "admin", "Food Voucher", "", "🍎", 5.00, 100, "Food", 0.0);
        when(mockCupboardDAO.findNeeds(searchString)).thenReturn(needs);

        Need[] response = cupboardService.findNeeds(searchString);

        assertEquals(needs, response);
    }

    @Test
    public void testFindNeedsException() throws IOException {
        String query = "Can";
        doThrow(new IOException()).when(mockCupboardDAO).findNeeds(query);
        assertThrows(IOException.class, () -> cupboardService.findNeeds(query));
    }

    // createNeed

    @Test
    public void testCreateNeed() throws IOException {
        Need need = new Need(10, 1, "admin", "Hot Dogs", "", "🍎", 6.99, 5, "Food", 0.0);
        when(mockCupboardDAO.createNeed(need)).thenReturn(need);

        Need response = cupboardService.createNeed(need);

        assertEquals(need, response);
    }

    @Test
    public void testCreateNeedFail() throws IOException {
        // assume this need already exists in database
        Need existingNeed = new Need(10, 1, "admin", "Hot Dogs", "🍎", "", 6.99, 5, "Food", 0.0);
        when(mockCupboardDAO.createNeed(existingNeed)).thenReturn(null);

        Need response = cupboardService.createNeed(existingNeed);

        assertEquals(null, response);
    }

    @Test
    public void testCreateNeedException() throws IOException {
        Need need = new Need(10, 1, "admin", "Hot Dogs", "", "🍎", 6.99, 5, "Food", 0.0);
        doThrow(new IOException()).when(mockCupboardDAO).createNeed(need);
        assertThrows(IOException.class, () -> cupboardService.createNeed(need));
    }

    // deleteNeed

    @Test
    public void testDeleteNeed() throws IOException {
        int id = 999;
        Need need = new Need(id, "Soup", 1, 1, "Food");
        when(mockCupboardDAO.getNeed(id)).thenReturn(need);
        when(mockCupboardDAO.deleteNeed(id)).thenReturn(true);
        when(mockPledgeBasketsDAO.removePledgesForNeed(id)).thenReturn(new int[] { 2, 3 });

        boolean response = cupboardService.deleteNeed(id);

        assertTrue(response);
        verify(mockBasketNotificationService).addArchivedNeedRemovedMessages(
                argThat(owners -> owners.length == 2 && owners[0] == 2 && owners[1] == 3),
                eq("Soup"));
    }

    @Test
    public void testDeleteNeedNoPledgesNoNotification() throws IOException {
        int id = 999;
        Need need = new Need(id, "Soup", 1, 1, "Food");
        when(mockCupboardDAO.getNeed(id)).thenReturn(need);
        when(mockCupboardDAO.deleteNeed(id)).thenReturn(true);
        when(mockPledgeBasketsDAO.removePledgesForNeed(id)).thenReturn(new int[0]);

        assertTrue(cupboardService.deleteNeed(id));

        verify(mockBasketNotificationService, never()).addArchivedNeedRemovedMessages(any(), anyString());
    }

    @Test
    public void testDeleteNeedNotFound() throws IOException {
        int id = 999;
        when(mockCupboardDAO.getNeed(id)).thenReturn(null);

        boolean response = cupboardService.deleteNeed(id);

        assertFalse(response);
        verify(mockCupboardDAO, never()).deleteNeed(anyInt());
    }

    @Test
    public void testDeleteNeedDaoFails() throws IOException {
        int id = 999;
        Need need = new Need(id, "Soup", 1, 1, "Food");
        when(mockCupboardDAO.getNeed(id)).thenReturn(need);
        when(mockCupboardDAO.deleteNeed(id)).thenReturn(false);

        boolean response = cupboardService.deleteNeed(id);

        assertFalse(response);
        verify(mockPledgeBasketsDAO, never()).removePledgesForNeed(anyInt());
    }

    @Test
    public void testDeleteNeedException() throws IOException {
        int id = 999;
        Need need = new Need(id, "Soup", 1, 1, "Food");
        when(mockCupboardDAO.getNeed(id)).thenReturn(need);
        doThrow(new IOException()).when(mockCupboardDAO).deleteNeed(id);
        assertThrows(IOException.class, () -> cupboardService.deleteNeed(id));
    }

    @Test
    public void testPermanentlyDeleteNeed() throws IOException {
        int id = 999;
        Need need = new Need(id, "Soup", 1, 1, "Food");
        when(mockCupboardDAO.permanentlyDeleteNeed(id)).thenReturn(need);

        Need response = cupboardService.permanentlyDeleteNeed(id);

        assertEquals(need, response);
    }

    @Test
    public void testPermanentlyDeleteNeedReturnsNull() throws IOException {
        int id = 999;
        when(mockCupboardDAO.permanentlyDeleteNeed(id)).thenReturn(null);

        assertNull(cupboardService.permanentlyDeleteNeed(id));
    }

    @Test
    public void testPermanentlyDeleteNeedException() throws IOException {
        int id = 999;
        doThrow(new IOException()).when(mockCupboardDAO).permanentlyDeleteNeed(id);

        assertThrows(IOException.class, () -> cupboardService.permanentlyDeleteNeed(id));
    }

    @Test
    public void testDeleteNeedIOExceptionFromRemovePledgesForNeed() throws IOException {
        int id = 999;
        Need need = new Need(id, "Soup", 1, 1, "Food");
        when(mockCupboardDAO.getNeed(id)).thenReturn(need);
        when(mockCupboardDAO.deleteNeed(id)).thenReturn(true);
        doThrow(new IOException()).when(mockPledgeBasketsDAO).removePledgesForNeed(id);

        assertThrows(IOException.class, () -> cupboardService.deleteNeed(id));
    }

    // updateNeed

    @Test
    public void testUpdateNeed() throws IOException {
        Need need = new Need(10, 1, "admin", "Hot Dogs", "", "🍎", 6.99, 5, "Food", 0.0);
        when(mockCupboardDAO.updateNeed(need)).thenReturn(need);

        Need response = cupboardService.updateNeed(need);

        assertEquals(need, response);
    }

    @Test
    public void testUpdateNeedFail() throws IOException {
        Need need = new Need(10, 1, "admin", "Hot Dogs", "", "🍎", 6.99, 5, "Food", 0.0);
        when(mockCupboardDAO.updateNeed(need)).thenReturn(null);

        Need response = cupboardService.updateNeed(need);

        assertEquals(null, response);
    }

    @Test
    public void testUpdateNeedException() throws IOException {
        Need need = new Need(10, 1, "admin", "Hot Dogs", "", "🍎", 6.99, 5, "Food", 0.0);
        doThrow(new IOException()).when(mockCupboardDAO).updateNeed(need);
        assertThrows(IOException.class, () -> cupboardService.updateNeed(need));
    }
}
