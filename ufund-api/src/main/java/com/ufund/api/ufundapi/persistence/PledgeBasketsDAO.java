package com.ufund.api.ufundapi.persistence;

import java.io.IOException;

import com.ufund.api.ufundapi.model.PledgeBasket;

public interface PledgeBasketsDAO {
    /**
     * Retrieves all {@link PledgeBasket} instances currently persisted.
     *
     * @return an array of {@link PledgeBasket} objects; may be empty but never {@code null}
     */
    PledgeBasket[] getPledgeBaskets();

    /**
     * Retrieves the {@link PledgeBasket} owned by ownerAccountId.
     * There should be at most one basket per owner.
     *
     * @param ownerAccountId the id of the donor / account owner
     * @return the {@link PledgeBasket} for that owner, or {@code null} if none exists
     */
    PledgeBasket getPledgeBasketByOwner(int ownerAccountId); // there should only be one pledge basket per owner

    /**
     * Creates and persists a new {@link PledgeBasket} for the given owner id.
     *
     * @param basket the basket to add; its {@code ownerId} is used as the key
     * @return {@code true} if the basket was added and saved, or {@code false}
     *         if a basket already exists for that owner or the input is {@code null}
     * @throws IOException if underlying storage cannot be accessed or written
     */
    boolean addPledgeBasket(PledgeBasket basket) throws IOException;

    /**
     * Whether a pledge basket exists for this account owner (baskets are keyed by owner id).
     */
    boolean hasPledgeBasketWithOwner(int ownerAccountId);

    /**
     * Updates and persists an existing pledge basket (e.g. after adding a pledge).
     * @param basket The basket to update
     * @return {@code true} if the basket was found and saved
     * @throws IOException if underlying storage cannot be accessed
     */
    boolean updatePledgeBasket(PledgeBasket basket) throws IOException;

    boolean removePledgeBasket(PledgeBasket basket) throws IOException;

    boolean checkoutPledgeBasket(int pledgeBasketId, CupboardDAO cupboardDao) throws IOException;

    /**
     * Removes every pledge (across all baskets) that targets {@code needId}, and removes those pledges from the catalog.
     *
     * @return distinct owner ids that had at least one pledge removed (may be empty, never {@code null})
     */
    int[] removePledgesForNeed(int needId) throws IOException;
}
