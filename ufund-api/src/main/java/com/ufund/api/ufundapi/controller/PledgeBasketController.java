package com.ufund.api.ufundapi.controller;


import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.ufund.api.ufundapi.model.Need;
import com.ufund.api.ufundapi.model.Pledge;
import com.ufund.api.ufundapi.model.PledgeBasket;
import com.ufund.api.ufundapi.persistence.CupboardDAO;
import com.ufund.api.ufundapi.persistence.PledgeBasketsDAO;
import com.ufund.api.ufundapi.persistence.PledgeCatalogDAO;
import com.ufund.api.ufundapi.persistence.PledgeCatalogFileDAO;
import com.ufund.api.ufundapi.services.BasketNotificationService;

import org.springframework.web.bind.annotation.RequestParam;



/**
 * REST API for PledgeBasket operations (add need to a donor's basket).
 * <p>
 * {@literal @}RestController Spring annotation identifies this class as a REST API
 * method handler to the Spring framework
 * 
 * @author 5E
 */
@RestController
@RequestMapping("pledgeBasket")
public class PledgeBasketController {
    private static final Logger LOG = Logger.getLogger(PledgeBasketController.class.getName());

    private final PledgeBasketsDAO pledgeBasketsDao;
    private final PledgeCatalogDAO pledgeCatalogDao;
    private final CupboardDAO cupboardDao;
    private final BasketNotificationService basketNotificationService;

    public PledgeBasketController(
            PledgeBasketsDAO pledgeBasketsDao,
            PledgeCatalogDAO pledgeCatalogDao,
            CupboardDAO cupboardDao,
            BasketNotificationService basketNotificationService) {
        this.pledgeBasketsDao = pledgeBasketsDao;
        this.pledgeCatalogDao = pledgeCatalogDao;
        this.cupboardDao = cupboardDao;
        this.basketNotificationService = basketNotificationService;
    }

    /**
     * Basket contents plus one-time messages for the donor (e.g. removed pledges).
     */
    public static class UserBasketPayload {
        private final Pledge[] pledges;
        private final String[] messages;

        public UserBasketPayload(Pledge[] pledges, String[] messages) {
            this.pledges = pledges != null ? pledges : new Pledge[0];
            this.messages = messages != null ? messages : new String[0];
        }

        public Pledge[] getPledges() {
            return pledges;
        }

        public String[] getMessages() {
            return messages;
        }
    }

        /**
     * Request body for adding a need to a user's pledge basket.
     */
    public static class AddNeedRequest {
        private int userId;
        private Need need;


        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }
        public Need getNeed() { return need; }
        public void setNeed(Need need) { this.need = need; }
    }


    /**
     * Response body: userId and the need that was added.
     */
    public static class AddNeedResponse {
        private final int userId;
        private final Need need;


        public AddNeedResponse(int userId, Need need) {
            this.userId = userId;
            this.need = need;
        }
        public int getUserId() { return userId; }
        public Need getNeed() { return need; }
    }

    @GetMapping("/user/{userId}/need/")
    public ResponseEntity<UserBasketPayload> getUserBasket(@PathVariable int userId) {
        System.out.println("Pledge basket request for acct: " + userId);

        String[] messages = basketNotificationService.drainMessages(userId);
        if (!pledgeBasketsDao.hasPledgeBasketWithOwner(userId)) {
            return new ResponseEntity<>(new UserBasketPayload(new Pledge[0], messages), HttpStatus.OK);
        }
        Pledge[] pledges = pledgeBasketsDao.getPledgeBasketByOwner(userId).pledgeArray();
        return new ResponseEntity<>(new UserBasketPayload(pledges, messages), HttpStatus.OK);
    }

    @GetMapping("/user/{userId}/checkout/")
    public ResponseEntity<Boolean> checkoutUserBasket(@PathVariable int userId) {
        System.out.println("Pledge basket request for acct: " + userId);

        if (!pledgeBasketsDao.hasPledgeBasketWithOwner(userId)) {
            return new ResponseEntity<Boolean>(false, HttpStatus.NOT_FOUND);
        }
        try {
            return new ResponseEntity<Boolean>(pledgeBasketsDao.checkoutPledgeBasket(userId, cupboardDao), HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<Boolean>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user/{userId}/pledge")
    public ResponseEntity<Pledge> addPledgeToBasket(@PathVariable int userId, @RequestParam int needId, @RequestParam int amount, @RequestParam double money) {
        try {
            Pledge pledge = pledgeCatalogDao.addPledgeFromArguments(userId, needId, amount, money);
            PledgeBasket basket = pledgeBasketsDao.getPledgeBasketByOwner(userId);

            if (basket == null) return new ResponseEntity<Pledge>(HttpStatus.NOT_FOUND);

            basket.addPledge(pledge);
            pledgeBasketsDao.updatePledgeBasket(basket);
            return new ResponseEntity<Pledge>(pledge, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<Pledge>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/user/{userId}/pledge")
    public ResponseEntity<Pledge> deletePledgeFromBasket(@PathVariable int userId, @RequestParam int pledgeId) {
        try {
            Pledge pledge = pledgeCatalogDao.getPledge(pledgeId);
            PledgeBasket basket = pledgeBasketsDao.getPledgeBasketByOwner(userId);

            if (basket == null) return new ResponseEntity<Pledge>(HttpStatus.NOT_FOUND);

            basket.removePledge(pledgeId);
            pledgeBasketsDao.updatePledgeBasket(basket);
            return new ResponseEntity<Pledge>(pledge, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<Pledge>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    



    /**
     * Updates the quantity of a pledge in a user's basket.
     * * @param userId The ID of the user owning the basket
     * @param pledgeId The ID of the pledge to update
     * @param newQuantity The new quantity to set
     * @return 200 OK with the updated pledge, or 404 if not found
     */
    @PutMapping("/{userId}/pledge/{pledgeId}")
    public ResponseEntity<Pledge> updatePledgeQuantity(
            @PathVariable int userId,
            @PathVariable int pledgeId,
            @RequestBody int newQuantity) {
        try {
            PledgeBasket basket = pledgeBasketsDao.getPledgeBasketByOwner(userId);
            Pledge pledge = pledgeCatalogDao.getPledge(pledgeId);
            if (basket == null || pledge == null || !basket.hasPledge(pledgeId)) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            pledge.setQuantity(newQuantity);
            pledgeCatalogDao.updatePledge(pledge);
            pledgeBasketsDao.updatePledgeBasket(basket);


            return new ResponseEntity<>(pledge, HttpStatus.OK);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getLocalizedMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Removes a specific need from a donor's pledge basket.
     *
     * @param userId donor/user id
     * @param needId need id to remove
     * @return 200 OK if removed, 404 NOT_FOUND if basket or need not found in basket (or need not in cupboard)
     */
    @DeleteMapping("/user/{userId}/need/{needId}")
    public ResponseEntity<Need> removeNeedFromBasket(@PathVariable int userId, @PathVariable int needId) {
        LOG.info("DELETE /pledgeBasket/user/" + userId + "/need/" + needId);
        try {
            PledgeBasket basket = pledgeBasketsDao.getPledgeBasketByOwner(userId);
            if (basket == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            Need need = cupboardDao.getNeedRecord(needId);
            if (need == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            int pledgeIdToRemove = -1;
            for (Pledge pledge : basket.pledgeArray()) {
                if (pledge.getNeedId() == needId) {
                    pledgeIdToRemove = pledge.getId();
                    break;
                }
            }
            if (pledgeIdToRemove == -1) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            // Remove from basket first, then remove from catalog, then persist basket
            if (!basket.removePledge(pledgeIdToRemove)) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            pledgeCatalogDao.removePledge(pledgeIdToRemove);
            pledgeBasketsDao.updatePledgeBasket(basket);// error function in updatePledgeBasket branch

            return new ResponseEntity<>(need, HttpStatus.OK);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getLocalizedMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}