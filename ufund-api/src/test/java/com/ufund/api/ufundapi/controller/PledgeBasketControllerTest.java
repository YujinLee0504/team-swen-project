package com.ufund.api.ufundapi.controller; 

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.ufund.api.ufundapi.model.Need;
import com.ufund.api.ufundapi.model.Pledge;
import com.ufund.api.ufundapi.model.PledgeBasket;
import com.ufund.api.ufundapi.persistence.CupboardDAO;
import com.ufund.api.ufundapi.persistence.PledgeBasketsDAO;
import com.ufund.api.ufundapi.persistence.PledgeCatalogDAO;
import com.ufund.api.ufundapi.services.BasketNotificationService;

/**
 * Test PledgeBasketController class.
 *
 * @author 5E
 */
@Tag("Controller-tier")
public class PledgeBasketControllerTest {
    private PledgeBasketController pledgeBasketController;
    private PledgeBasketsDAO mockPledgeBasketsDAO;
    private PledgeCatalogDAO mockPledgeCatalogDAO;
    private CupboardDAO mockCupboardDAO;
    private BasketNotificationService mockBasketNotificationService;

    @BeforeEach
    public void setupPledgeBasketController() {
        mockPledgeBasketsDAO = mock(PledgeBasketsDAO.class);
        mockPledgeCatalogDAO = mock(PledgeCatalogDAO.class);
        mockCupboardDAO = mock(CupboardDAO.class);
        mockBasketNotificationService = mock(BasketNotificationService.class);
        when(mockBasketNotificationService.drainMessages(anyInt())).thenReturn(new String[0]);
        pledgeBasketController = new PledgeBasketController(
                mockPledgeBasketsDAO, mockPledgeCatalogDAO, mockCupboardDAO, mockBasketNotificationService);
    }



     //--------------------------------------------------
     // update
     //------------------------------
    @Test
    void updatePledgeQuantitySuccess() throws IOException {
        int userId = 1;
        int pledgeId = 100;
        int newQuantity = 10;
        
        Pledge pledge = new Pledge(pledgeId, userId, 10, 5, 0.0);
        PledgeBasket basket = new PledgeBasket(userId, Collections.emptyList());
        basket.addPledge(pledge);

        when(mockPledgeBasketsDAO.getPledgeBasketByOwner(userId)).thenReturn(basket);
        when(mockPledgeCatalogDAO.addPledge(pledge)).thenReturn(true);
        when(mockPledgeCatalogDAO.getPledge(pledgeId)).thenReturn(pledge);
        when(mockPledgeCatalogDAO.updatePledge(any(Pledge.class))).thenReturn(true);

        ResponseEntity<Pledge> response = pledgeBasketController.updatePledgeQuantity(userId, pledgeId, newQuantity);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(newQuantity, response.getBody().getQuantity());
        verify(mockPledgeBasketsDAO).updatePledgeBasket(basket);
    }

    @Test
    void updatePledgeQuantityPledgeNotFound() throws IOException {

        when(mockPledgeCatalogDAO.getPledge(999)).thenReturn(null);

        ResponseEntity<Pledge> response = pledgeBasketController.updatePledgeQuantity(1, 999, 10);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ----------------------------------------------------------------
    // getUserBasket
    // ----------------------------------------------------------------

    @Test
    public void testGetUserBasket() {
        // Setup
        int userId = 1;
        PledgeBasket basket = new PledgeBasket(userId, Collections.emptyList());
        when(mockPledgeBasketsDAO.getPledgeBasketByOwner(userId)).thenReturn(basket);
        when(mockPledgeBasketsDAO.hasPledgeBasketWithOwner(userId)).thenReturn(true);

        // Invoke
        ResponseEntity<PledgeBasketController.UserBasketPayload> response = pledgeBasketController.getUserBasket(userId);
        // Analyze
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(basket.pledgeArray(), response.getBody().getPledges());
        verify(mockBasketNotificationService).drainMessages(userId);
    }

    @Test
    public void testGetUserBasketNoBasketReturnsEmptyPledges() {
        // Setup
        int userId = 99;
        when(mockPledgeBasketsDAO.hasPledgeBasketWithOwner(userId)).thenReturn(false);

        // Invoke
        ResponseEntity<PledgeBasketController.UserBasketPayload> response = pledgeBasketController.getUserBasket(userId);

        // Analyze
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().getPledges().length);
        verify(mockBasketNotificationService).drainMessages(userId);
    }

    @Test
    public void testGetUserBasketIncludesDrainedMessages() {
        int userId = 3;
        when(mockPledgeBasketsDAO.hasPledgeBasketWithOwner(userId)).thenReturn(true);
        PledgeBasket basket = new PledgeBasket(userId, Collections.emptyList());
        when(mockPledgeBasketsDAO.getPledgeBasketByOwner(userId)).thenReturn(basket);
        when(mockBasketNotificationService.drainMessages(userId)).thenReturn(new String[] { "Notice one" });

        ResponseEntity<PledgeBasketController.UserBasketPayload> response = pledgeBasketController.getUserBasket(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(new String[] { "Notice one" }, response.getBody().getMessages());
    }

    // ----------------------------------------------------------------
    // checkoutBasket
    // ----------------------------------------------------------------

    @Test
    public void testCheckoutUserBasketSuccess() throws IOException {
        int userId = 1;
        when(mockPledgeBasketsDAO.hasPledgeBasketWithOwner(userId)).thenReturn(true);
        when(mockPledgeBasketsDAO.checkoutPledgeBasket(userId, mockCupboardDAO)).thenReturn(true);

        ResponseEntity<Boolean> response = pledgeBasketController.checkoutUserBasket(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody());
    }

    @Test
    public void testCheckoutUserBasketNotFound() {
        int userId = 99;
        when(mockPledgeBasketsDAO.hasPledgeBasketWithOwner(userId)).thenReturn(false);

        ResponseEntity<Boolean> response = pledgeBasketController.checkoutUserBasket(userId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(false, response.getBody());
    }

    @Test
    public void testCheckoutUserBasketIOException() throws IOException {
        int userId = 1;
        when(mockPledgeBasketsDAO.hasPledgeBasketWithOwner(userId)).thenReturn(true);
        doThrow(new IOException()).when(mockPledgeBasketsDAO).checkoutPledgeBasket(userId, mockCupboardDAO);

        ResponseEntity<Boolean> response = pledgeBasketController.checkoutUserBasket(userId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(false, response.getBody());
    }
    // ----------------------------------------------------------------
    // removeNeedFromBasket
    // ----------------------------------------------------------------

    @Test
    public void testRemoveNeedFromBasketSuccess() throws IOException {
        // Setup
        int userId = 1;
        int needId = 100;
        int pledgeId = 10;

        PledgeBasket basket = mock(PledgeBasket.class);
        Pledge pledge = new Pledge(pledgeId, userId, needId, 1, 0.0);
        Need need = new Need(needId, 1, "admin", "Pasta", "", "🍎", 10.0, 30, "Food", 0.0);

        when(mockPledgeBasketsDAO.getPledgeBasketByOwner(userId)).thenReturn(basket);
        when(mockCupboardDAO.getNeedRecord(needId)).thenReturn(need);
        when(basket.pledgeArray()).thenReturn(new Pledge[] { pledge });
        when(basket.removePledge(pledgeId)).thenReturn(true);
        when(mockPledgeCatalogDAO.removePledge(pledgeId)).thenReturn(true);
        when(mockPledgeBasketsDAO.updatePledgeBasket(basket)).thenReturn(true);

        // Invoke
        ResponseEntity<Need> response = pledgeBasketController.removeNeedFromBasket(userId, needId);

        // Analyze
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(need, response.getBody());
        verify(mockPledgeBasketsDAO, times(1)).updatePledgeBasket(basket);
        verify(mockPledgeCatalogDAO, times(1)).removePledge(pledgeId);
    }

    @Test
    public void testRemoveNeedFromBasketUserNotFound() {
        // Setup
        int userId = 1;
        int needId = 100;
        when(mockPledgeBasketsDAO.getPledgeBasketByOwner(userId)).thenReturn(null);

        // Invoke
        ResponseEntity<Need> response = pledgeBasketController.removeNeedFromBasket(userId, needId);

        // Analyze
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testRemoveNeedFromBasketNeedNotInCupboard() throws IOException {
        // Setup
        int userId = 1;
        int needId = 100;
        PledgeBasket basket = mock(PledgeBasket.class);
        when(mockPledgeBasketsDAO.getPledgeBasketByOwner(userId)).thenReturn(basket);
        when(mockCupboardDAO.getNeed(needId)).thenReturn(null);

        // Invoke
        ResponseEntity<Need> response = pledgeBasketController.removeNeedFromBasket(userId, needId);

        // Analyze
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testRemoveNeedFromBasketNeedNotInBasket() throws IOException {
        // Setup
        int userId = 1;
        int needId = 100;
        PledgeBasket basket = mock(PledgeBasket.class);
        Need need = new Need(needId, 1, "admin", "Pasta", "", "🍎", 10.0, 30, "Food", 0.0);

        when(mockPledgeBasketsDAO.getPledgeBasketByOwner(userId)).thenReturn(basket);
        when(mockCupboardDAO.getNeed(needId)).thenReturn(need);
        when(basket.pledgeArray()).thenReturn(new Pledge[] { new Pledge(10, userId, 999, 1, 0.0) });

        // Invoke
        ResponseEntity<Need> response = pledgeBasketController.removeNeedFromBasket(userId, needId);

        // Analyze
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testDeletePledgeFromBasketSuccess() throws IOException {
        // Setup
        int userId = 1;
        int needId = 100;
        int pledgeId = 10;

        PledgeBasket basket = mock(PledgeBasket.class);
        Pledge pledge = new Pledge(pledgeId, userId, needId, 1, 0.0);
        Need need = new Need(needId, 1, "admin", "Pasta", "", "🍎", 10.0, 30, "Food", 0.0);
        when(mockPledgeBasketsDAO.getPledgeBasketByOwner(userId)).thenReturn(basket);
        when(mockCupboardDAO.getNeedRecord(needId)).thenReturn(need);
        when(basket.pledgeArray()).thenReturn(new Pledge[] { pledge });
        when(basket.removePledge(pledgeId)).thenReturn(true);
        when(mockPledgeCatalogDAO.removePledge(pledgeId)).thenReturn(true);
        when(mockPledgeBasketsDAO.updatePledgeBasket(basket)).thenReturn(true);

        // Invoke
        ResponseEntity<Need> response = pledgeBasketController.removeNeedFromBasket(userId, needId);

        // Analyze
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testDeletePledgeFromBasketNullPledge() throws IOException {
        // Setup
        int userId = 1;
        int needId = 100;

        PledgeBasket basket = mock(PledgeBasket.class);
        Need need = new Need(needId, 1, "admin", "Pasta", "", "🍎", 10.0, 30, "Food", 0.0);
        when(mockPledgeBasketsDAO.getPledgeBasketByOwner(userId)).thenReturn(basket);
        when(mockCupboardDAO.getNeedRecord(needId)).thenReturn(need);
        when(basket.pledgeArray()).thenReturn(new Pledge[] { new Pledge(10, userId, 999, 1, 0.0) });

        // Invoke
        ResponseEntity<Need> response = pledgeBasketController.removeNeedFromBasket(userId, needId);

        // Analyze
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
   
    @Test
    public void testDeletePledgeFromBasketIOException() throws IOException {
        // Setup
        int userId = 1;
        int needId = 100;
        int pledgeId = 10;

        PledgeBasket basket = mock(PledgeBasket.class);
        Pledge pledge = new Pledge(pledgeId, userId, needId, 1, 0.0);
        Need need = new Need(needId, 1, "admin", "Pasta", "", "🍎", 10.0, 30, "Food", 0.0);
        when(mockPledgeBasketsDAO.getPledgeBasketByOwner(userId)).thenReturn(basket);
        when(mockCupboardDAO.getNeedRecord(needId)).thenReturn(need);
        when(basket.pledgeArray()).thenReturn(new Pledge[] { pledge });
        when(basket.removePledge(pledgeId)).thenReturn(true);
        when(mockPledgeCatalogDAO.removePledge(pledgeId)).thenReturn(true);
        doThrow(new IOException()).when(mockPledgeBasketsDAO).updatePledgeBasket(basket);

        // Invoke
        ResponseEntity<Need> response = pledgeBasketController.removeNeedFromBasket(userId, needId);

        // Analyze
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

     //--------------------------------------------------
// addPledgeToBasket

    @Test
    void testAddPledgeToBasketSuccess() throws IOException{
        int userId = 1;
        int needId = 100;
        int qty = 10;
        double money = 9.99;
        PledgeBasket basket = mock(PledgeBasket.class);
        Pledge mockPledge = new Pledge(1, userId, needId, qty, money);
        when(mockPledgeCatalogDAO.addPledgeFromArguments(1, 100, qty, money)).thenReturn(mockPledge);
        when(mockPledgeBasketsDAO.getPledgeBasketByOwner(userId)).thenReturn(basket);

        ResponseEntity<Pledge> result = pledgeBasketController.addPledgeToBasket(userId, needId, qty, money);

        assertEquals(mockPledge, result.getBody());
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void testAddPledgeToBasketIOException() throws IOException {
        int userId = 1;
        int needId = 100;
        int qty = 10;
        double money = 9.99;
        when(mockPledgeCatalogDAO.addPledgeFromArguments(1, 100, qty, money)).thenThrow(new IOException());

        ResponseEntity<Pledge> result = pledgeBasketController.addPledgeToBasket(userId, needId, qty, money);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    }

    @Test
    void testAddPledgeToBasketNullPledge() throws IOException {
        int userId = 1;
        int needId = 100;
        int qty = 10;
        double money = 9.99;
        PledgeBasket basket = mock(PledgeBasket.class);
        when(mockPledgeCatalogDAO.addPledgeFromArguments(1, 100, qty, money)).thenReturn(null);
        when(mockPledgeBasketsDAO.getPledgeBasketByOwner(userId)).thenReturn(basket);

        ResponseEntity<Pledge> result = pledgeBasketController.addPledgeToBasket(userId, needId, qty, money);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    void testAddPledgeToBasketBasketNotFound() throws IOException {
        int userId = 1;
        int needId = 100;
        int qty = 10;
        double money = 9.99;
        Pledge mockPledge = new Pledge(1, userId, needId, qty, money);
        when(mockPledgeCatalogDAO.addPledgeFromArguments(1, 100, qty, money)).thenReturn(mockPledge);
        when(mockPledgeBasketsDAO.getPledgeBasketByOwner(userId)).thenReturn(null);

        ResponseEntity<Pledge> result = pledgeBasketController.addPledgeToBasket(userId, needId, qty, money);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }


    // ----------------------------------------------------------------
    // deletePledgeFromBasket (/user/{userId}/pledge)
    // ----------------------------------------------------------------

    @Test
    void deletePledgeFromBasketSuccess() throws IOException {
        int userId = 1;
        int pledgeId = 10;
        PledgeBasket basket = mock(PledgeBasket.class);
        Pledge pledge = new Pledge(pledgeId, userId, 100, 1, 0.0);

        when(mockPledgeCatalogDAO.getPledge(pledgeId)).thenReturn(pledge);
        when(mockPledgeBasketsDAO.getPledgeBasketByOwner(userId)).thenReturn(basket);
        when(mockPledgeBasketsDAO.updatePledgeBasket(basket)).thenReturn(true);

        ResponseEntity<Pledge> response = pledgeBasketController.deletePledgeFromBasket(userId, pledgeId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(pledge, response.getBody());
        verify(basket).removePledge(pledgeId);
        verify(mockPledgeBasketsDAO).updatePledgeBasket(basket);
    }

    @Test
    void deletePledgeFromBasketBasketNotFound() {
        int userId = 1;
        int pledgeId = 10;
        when(mockPledgeBasketsDAO.getPledgeBasketByOwner(userId)).thenReturn(null);

        ResponseEntity<Pledge> response = pledgeBasketController.deletePledgeFromBasket(userId, pledgeId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deletePledgeFromBasketIOException() throws IOException {
        int userId = 1;
        int pledgeId = 10;
        PledgeBasket basket = mock(PledgeBasket.class);
        when(mockPledgeBasketsDAO.getPledgeBasketByOwner(userId)).thenReturn(basket);
        doThrow(new IOException()).when(mockPledgeBasketsDAO).updatePledgeBasket(basket);

        ResponseEntity<Pledge> response = pledgeBasketController.deletePledgeFromBasket(userId, pledgeId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}