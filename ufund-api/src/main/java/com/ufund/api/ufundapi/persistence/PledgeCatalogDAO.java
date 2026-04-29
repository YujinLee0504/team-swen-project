package com.ufund.api.ufundapi.persistence;

import java.io.IOException;

import com.ufund.api.ufundapi.model.Pledge;

public interface PledgeCatalogDAO {
    /**
     * Returns the next available pledge ID
     * @return
     */
    int nextId();

    /**
     * Returns the {@linkplain Pledge pledges} in memory as an array
     * @return All Pledge objects currently in the catalog's memory
     */
    Pledge[] pledgeArray();

    Pledge addPledgeFromArguments(int basketId, int needId, int amount, double money) throws IOException;

    /**
     * Adds a Pledge to the catalog, if a Pledge with the same ID does not already exist
     * @param pledge The Pledge to be added
     * @return {@code true} if the Pledge was added successfully, {@code false} otherwise
     * @throws IOException when file cannot be accessed or written to
     */
    boolean addPledge(Pledge pledge) throws IOException;

    /**
     * Retrieves a Pledge by id
     * @param pledgeId The pledge ID to look up
     * @return The Pledge with the given id, or null if not found
     */
    Pledge getPledge(int pledgeId);

    /**
     * Checks whether a Pledge with an ID matching a specific Pledge object's ID exists in memory
     * @param pledge The Pledge to check for. Note that it only compares the Pledge object's ID, it does not check for object equality.
     * @return {@code true} if a pledge exists, {@code false} otherwise
     */
    boolean hasPledge(Pledge pledge);

    /**
     * Checks whether a Pledge with a specific ID exists in memory
     * @param pledgeId The pledge ID to check for
     * @return {@code true} if a pledge with the ID exists, {@code false} otherwise
     */
    boolean hasPledge(int pledgeId);

    /**
     * Updates an existing Pledge in the catalog.
     * @param pledge The Pledge with updated data
     * @return {@code true} if the Pledge existed and was updated, {@code false} otherwise
     * @throws IOException when file cannot be accessed or written to
     */
    boolean updatePledge(Pledge pledge) throws IOException;

    /**
     * Removes a Pledge from the catalog that matches the specified ID
     * @param pledgeId The pledge ID to remove
     * @return {@code true} if a pledge with the ID exists and was removed successfully, {@code false} otherwise
     * @throws IOException when file cannot be accessed or written to
     */
    boolean removePledge(int pledgeId) throws IOException;
}