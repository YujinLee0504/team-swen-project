package com.ufund.api.ufundapi.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import com.ufund.api.ufundapi.model.Need;
import com.ufund.api.ufundapi.services.CupboardService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Test the UfundController class
 *
 * @author 5E
 */
@Tag("Controller-tier")
public class UfundControllerTest {
    private UfundController ufundController;
    private CupboardService mockCupboardService;

    @BeforeEach
    public void setupUfundController() {
        mockCupboardService = mock(CupboardService.class);
        ufundController = new UfundController(mockCupboardService);
    }

    // ----------------------------------------------------------------
    // getNeed
    // ----------------------------------------------------------------

    @Test
    public void testGetNeed() throws IOException {
        // Setup
        Need need = new Need(99, 1, "admin", "Blankets", "", "🍎", 10.00, 50, "Clothing", 0.0);
        when(mockCupboardService.getNeed(need.getId())).thenReturn(need);

        // Invoke
        ResponseEntity<Need> response = ufundController.getNeed(need.getId());

        // Analyze
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(need, response.getBody());
    }

    @Test
    public void testGetNeedNotFound() throws IOException {
        // Setup
        int needId = 99;
        when(mockCupboardService.getNeed(needId)).thenReturn(null);

        // Invoke
        ResponseEntity<Need> response = ufundController.getNeed(needId);

        // Analyze
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testGetNeedHandleException() throws IOException {
        // Setup
        int needId = 99;
        doThrow(new IOException()).when(mockCupboardService).getNeed(needId);

        // Invoke
        ResponseEntity<Need> response = ufundController.getNeed(needId);

        // Analyze
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testGetArchivedNeeds() throws IOException {
        // Setup
        Need[] needs = new Need[2];
        needs[0] = new Need(99,  "Blankets",    10.00, 50,  "Clothing");
        needs[1] = new Need(100, "Canned Food",  2.50, 200, "Food");
        when(mockCupboardService.getArchivedNeeds()).thenReturn(needs);

        // Invoke
        ResponseEntity<Need[]> response = ufundController.getArchivedNeeds();

        // Analyze
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(needs, response.getBody());
    }

    @Test
    public void testGetArchivedNeedsHandleException() throws IOException {
        // Setup
        doThrow(new IOException()).when(mockCupboardService).getArchivedNeeds();

        // Invoke
        ResponseEntity<Need[]> response = ufundController.getArchivedNeeds();

        // Analyze
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // ----------------------------------------------------------------
    // getNeeds
    // ----------------------------------------------------------------

    @Test
    public void testGetNeeds() throws IOException {
        // Setup
        Need[] needs = new Need[2];
        needs[0] = new Need(99, 1, "admin",  "Blankets", "", "🍎",    10.00, 50,  "Clothing", 0.0);
        needs[1] = new Need(100, 1, "admin", "Canned Food", "", "🍎",  2.50, 200, "Food", 0.0);
        when(mockCupboardService.getNeeds()).thenReturn(needs);

        // Invoke
        ResponseEntity<Need[]> response = ufundController.getNeeds();

        // Analyze
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(needs, response.getBody());
    }

    @Test
    public void testGetNeedsHandleException() throws IOException {
        // Setup
        doThrow(new IOException()).when(mockCupboardService).getNeeds();

        // Invoke
        ResponseEntity<Need[]> response = ufundController.getNeeds();

        // Analyze
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // ----------------------------------------------------------------
    // searchNeeds
    // ----------------------------------------------------------------

    @Test
    public void testSearchNeeds() throws IOException {
        // Setup
        String searchString = "food";
        Need[] needs = new Need[2];
        needs[0] = new Need(100, 1, "admin", "Canned Food", "", "🍎",  2.50, 200, "Food", 0.0);
        needs[1] = new Need(101, 1, "admin", "Food Voucher", "", "🍎", 5.00, 100, "Food", 0.0);
        when(mockCupboardService.findNeeds(searchString)).thenReturn(needs);

        // Invoke
        ResponseEntity<Need[]> response = ufundController.searchNeeds(searchString);

        // Analyze
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(needs, response.getBody());
    }

    @Test
    public void testSearchNeedsHandleException() throws IOException {
        // Setup
        String searchString = "food";
        doThrow(new IOException()).when(mockCupboardService).findNeeds(searchString);

        // Invoke
        ResponseEntity<Need[]> response = ufundController.searchNeeds(searchString);

        // Analyze
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // ----------------------------------------------------------------
    // restoreNeed
    // ----------------------------------------------------------------
    @Test
    public void testRestoreNeed() throws IOException {
        // Setup
        int id = 99;
        Need need = new Need(id, "Blankets", 10.00, 50, "Clothing");
        when(mockCupboardService.restoreNeed(id)).thenReturn(need);

        // Invoke
        ResponseEntity<Need> response = ufundController.restoreNeed(id);

        // Analyze
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(need, response.getBody());
    }

    @Test
    public void testRestoreNeedNotFound() throws IOException {
        // Setup
        int id = 99;
        when(mockCupboardService.restoreNeed(id)).thenReturn(null);

        // Invoke
        ResponseEntity<Need> response = ufundController.restoreNeed(id);

        // Analyze
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testRestoreNeedHandleException() throws IOException {
        // Setup
        int id = 99;
        doThrow(new IOException()).when(mockCupboardService).restoreNeed(id);

        // Invoke
        ResponseEntity<Need> response = ufundController.restoreNeed(id);

        // Analyze
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // ----------------------------------------------------------------
    // permanentlyDeleteNeed (archive)
    // ----------------------------------------------------------------

    @Test
    public void testPermanentlyDeleteNeed() throws IOException {
        int id = 5;
        Need gone = new Need(id, "Gone", 2, 2, "Food");
        when(mockCupboardService.permanentlyDeleteNeed(id)).thenReturn(gone);

        ResponseEntity<Need> response = ufundController.permanentlyDeleteNeed(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(gone, response.getBody());
    }

    @Test
    public void testPermanentlyDeleteNeedNotFound() throws IOException {
        int id = 5;
        when(mockCupboardService.permanentlyDeleteNeed(id)).thenReturn(null);

        ResponseEntity<Need> response = ufundController.permanentlyDeleteNeed(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testPermanentlyDeleteNeedHandleException() throws IOException {
        int id = 5;
        doThrow(new IOException()).when(mockCupboardService).permanentlyDeleteNeed(id);

        ResponseEntity<Need> response = ufundController.permanentlyDeleteNeed(id);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // ----------------------------------------------------------------
    // createNeed
    // ----------------------------------------------------------------

    @Test
    public void testCreateNeed() throws IOException {
        // Setup
        Need need = new Need(99, 1, "admin", "Blankets", "", "🍎", 10.00, 50, "Clothing", 0.0);
        when(mockCupboardService.createNeed(need)).thenReturn(need);

        // Invoke
        ResponseEntity<Need> response = ufundController.createNeed(need);

        // Analyze
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(need, response.getBody());
    }

    @Test
    public void testCreateNeedFailed() throws IOException {
        // Setup -- duplicate name; DAO returns null to signal conflict
        Need need = new Need(99, 1, "admin", "Blankets", "", "🍎", 10.00, 50, "Clothing", 0.0);
        when(mockCupboardService.createNeed(need)).thenReturn(null);

        // Invoke
        ResponseEntity<Need> response = ufundController.createNeed(need);

        // Analyze
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    public void testCreateNeedHandleException() throws IOException {
        // Setup
        Need need = new Need(99, 1, "admin", "Blankets", "", "🍎", 10.00, 50, "Clothing", 0.0);
        doThrow(new IOException()).when(mockCupboardService).createNeed(need);

        // Invoke
        ResponseEntity<Need> response = ufundController.createNeed(need);

        // Analyze
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // ----------------------------------------------------------------
    // updateNeed
    // ----------------------------------------------------------------

    @Test
    public void testUpdateNeed() throws IOException {
        // Setup
        Need need = new Need(99, 1, "admin", "Blankets", "", "🍎", 10.00, 50, "Clothing", 0.0);
        when(mockCupboardService.updateNeed(need)).thenReturn(need);
        ResponseEntity<Need> response = ufundController.updateNeed(need);
        need.setCost(15.00);

        // Invoke
        response = ufundController.updateNeed(need);

        // Analyze
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(need, response.getBody());
    }

    @Test
    public void testUpdateNeedFailed() throws IOException {
        // Setup -- DAO returns null when need does not exist
        Need need = new Need(99, 1, "admin", "Blankets", "🍎", "", 10.00, 50, "Clothing", 0.0);
        when(mockCupboardService.updateNeed(need)).thenReturn(null);

        // Invoke
        ResponseEntity<Need> response = ufundController.updateNeed(need);

        // Analyze
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testUpdateNeedHandleException() throws IOException {
        // Setup
        Need need = new Need(99, 1, "admin", "Blankets", "", "🍎", 10.00, 50, "Clothing", 0.0);
        doThrow(new IOException()).when(mockCupboardService).updateNeed(need);

        // Invoke
        ResponseEntity<Need> response = ufundController.updateNeed(need);

        // Analyze
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // ----------------------------------------------------------------
    // deleteNeed
    // ----------------------------------------------------------------

    @Test
    public void testDeleteNeed() throws IOException {
        // Setup
        int needId = 99;
        Need need = new Need(needId, 1, "admin", "Blankets", "", "🍎", 10.00, 50, "Clothing", 0.0);
        when(mockCupboardService.getNeed(needId)).thenReturn(need);
        when(mockCupboardService.deleteNeed(needId)).thenReturn(true);

        // Invoke
        ResponseEntity<Need> response = ufundController.deleteNeed(needId);

        // Analyze
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testDeleteNeedNotFound() throws IOException {
        // Setup
        int needId = 99;
        when(mockCupboardService.getNeed(needId)).thenReturn(null);
        when(mockCupboardService.deleteNeed(needId)).thenReturn(false);

        // Invoke
        ResponseEntity<Need> response = ufundController.deleteNeed(needId);

        // Analyze
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testDeleteNeedHandleException() throws IOException {
        // Setup -- exception thrown on getNeed, which is called first in deleteNeed
        int needId = 99;
        doThrow(new IOException()).when(mockCupboardService).getNeed(needId);

        // Invoke
        ResponseEntity<Need> response = ufundController.deleteNeed(needId);

        // Analyze
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}