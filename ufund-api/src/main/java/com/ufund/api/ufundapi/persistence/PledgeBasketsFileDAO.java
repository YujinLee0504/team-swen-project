package com.ufund.api.ufundapi.persistence;


import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufund.api.ufundapi.model.Pledge;
import com.ufund.api.ufundapi.model.PledgeBasket;


/**
 * File-based persistence for PledgeBaskets. Stores each basket as ownerId + pledgeIds;
 * pledge entities are resolved from {@link PledgeCatalogDAO}.
 *
 * @author 5E
 */
@Component
public class PledgeBasketsFileDAO implements PledgeBasketsDAO {


    private final String filename;
    private final ObjectMapper objectMapper;
    private final PledgeCatalogDAO pledgeCatalogDao;


    /** In-memory map: ownerId -> PledgeBasket */
    private Map<Integer, PledgeBasket> basketsByOwner = new LinkedHashMap<>();


    public PledgeBasketsFileDAO(
            @Value("${baskets.file}") String filename,
            ObjectMapper objectMapper,
            PledgeCatalogDAO pledgeCatalogDao) throws IOException {
        this.filename = filename;
        this.objectMapper = objectMapper;
        this.pledgeCatalogDao = pledgeCatalogDao;
        load();
    }
    

    private boolean load() throws IOException {
        File file = new File(filename);
        if (objectMapper == null || !file.exists()) {
            return false;
        }
        PledgeBasket[] pledgeBaskets = objectMapper.readValue(file, PledgeBasket[].class);

        for (PledgeBasket p : pledgeBaskets) {
            basketsByOwner.put(p.getOwnerId(), p);
        }
        return true;
    }

    private boolean save() throws IOException {
        File file = new File(filename);
        if (objectMapper == null || !file.exists()) return false;
        PledgeBasket[] pledgeBaskets = this.getPledgeBaskets();
        objectMapper.writeValue(file, pledgeBaskets);
        return true;
    }

    @Override
    public PledgeBasket[] getPledgeBaskets() {
        return basketsByOwner.values().toArray(new PledgeBasket[0]);
    }

    @Override
    public PledgeBasket getPledgeBasketByOwner(int ownerAccountId) {
        return basketsByOwner.get(ownerAccountId);
    }

    @Override
    public boolean addPledgeBasket(PledgeBasket basket) {
        if (basket == null) {
            return false;
        }

        int ownerId = basket.getOwnerId();//in getPledgeBasket branch

        // Do not overwrite an existing basket for this owner
        if (basketsByOwner.containsKey(ownerId)) {
            return false;
        }

        basketsByOwner.put(ownerId, basket);
        try {
            // Persist all baskets to file
            if (!save()) {
                basketsByOwner.remove(ownerId);
                return false;
            }
            return true;
        } catch (IOException e) {
            // Go back in-memory change if persistence fails
            basketsByOwner.remove(ownerId);
            return false;
        }
    }

    @Override
    public boolean hasPledgeBasketWithOwner(int ownerAccountId) {
        return basketsByOwner.containsKey(ownerAccountId);
    }
    
    @Override
    public boolean updatePledgeBasket(PledgeBasket basket) throws IOException {
        if (basket == null) {
            return false;
        }
        // The basket returned by getPledgeBasketByOwner is the same instance stored in the map, so we only need to verify it exists and then persist.
        if (!basketsByOwner.containsValue(basket)) {
            return false;
        }
        return save();
    }

    @Override
    public boolean removePledgeBasket(PledgeBasket basket) throws IOException {
        if (basket == null) return false;
        int ownerId = basket.getOwnerId();
        PledgeBasket removed = basketsByOwner.remove(ownerId);
        if (removed != null) {
            if (!save()) {
                basketsByOwner.put(ownerId, removed);
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean checkoutPledgeBasket(int pledgeBasketId, CupboardDAO cupboardDAO) throws IOException {
        if (!this.hasPledgeBasketWithOwner(pledgeBasketId)) return false;
        Pledge[] checkingOutPledges = this.getPledgeBasketByOwner(pledgeBasketId).checkout();
        cupboardDAO.distributeCheckout(checkingOutPledges);
        return save();
    }

    @Override
    public int[] removePledgesForNeed(int needId) throws IOException {
        synchronized (basketsByOwner) {
            Set<Integer> affectedOwners = new LinkedHashSet<>();
            boolean changed = false;
            for (PledgeBasket basket : basketsByOwner.values()) {
                Pledge[] snapshot = basket.pledgeArray();
                for (Pledge p : snapshot) {
                    if (p.getNeedId() == needId) {
                        basket.removePledge(p.getId());
                        pledgeCatalogDao.removePledge(p.getId());
                        affectedOwners.add(basket.getOwnerId());
                        changed = true;
                    }
                }
            }
            if (changed) {
                save();
            }
            return affectedOwners.stream().mapToInt(Integer::intValue).toArray();
        }
    }
}