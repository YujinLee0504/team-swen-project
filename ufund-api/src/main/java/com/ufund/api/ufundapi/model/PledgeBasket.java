package com.ufund.api.ufundapi.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ufund.api.ufundapi.persistence.PledgeCatalogDAO;
import com.ufund.api.ufundapi.persistence.PledgeCatalogFileDAO;

/**
 * Acts as a holder class for the PledgeBasket's collection of Pledges
 * 
 * @author Group 5E
 */
public class PledgeBasket {

    // Package private for tests
    static final String STRING_FORMAT = "PledgeBasket [owner=%s, pledges=%s]";

    private PledgeCatalogDAO catalog = PledgeCatalogFileDAO.INSTANCE; // TODO band-aid solution, I will figure out something more permanent to allow reference to this from PledgeBasket
    @JsonProperty("ownerId") private int ownerId; // account owner ID
    @JsonProperty("pledgeIds") private Set<Integer> pledgeIds;
    
    public PledgeBasket(@JsonProperty("ownerId") int ownerId,
                        @JsonProperty("pledgeIds") Collection<Integer> pledgeIds) {
        this.ownerId = ownerId;
        this.pledgeIds = new LinkedHashSet<>();
        if (pledgeIds != null && pledgeIds.size() != 0) {
            for (int p : pledgeIds) this.pledgeIds.add(p);
        }
    }

    public Pledge getPledge(int pledgeId) {
        if (!this.hasPledge(pledgeId)) return null;
        return this.catalog.getPledge(pledgeId);
    }

    public Pledge[] pledgeArray() {
        ArrayList<Pledge> pledgeArrayList = new ArrayList<>(pledgeIds.size());
        for (int pledgeId : pledgeIds) {
            if (this.catalog.hasPledge(pledgeId)) pledgeArrayList.add(this.catalog.getPledge(pledgeId));
        }
        return pledgeArrayList.toArray(new Pledge[0]);
    }

    /**
     * Adds a {@link Pledge} to this basket.
     *
     * Rejects the pledge if it is {@code null}, belongs to a different owner,
     * or a pledge with the same id is already present.
     *
     * @param pledge The pledge to add
     * @return {@code true} if the pledge was added; {@code false} otherwise
     */
    public boolean addPledge(Pledge pledge) {
        if (pledge == null || pledge.getOwnerId() != this.ownerId) return false;
        return this.pledgeIds.add(pledge.getId());
    }

    /**
     * @param pledge The pledge to check
     * @return {@code true} if this basket contains a pledge with the same id
     */
    public boolean hasPledge(Pledge pledge) {
        return pledge != null && pledgeIds.contains(pledge.getId());
    }

    /**
     * @param pledgeId The id of the pledge to check
     * @return {@code true} if this basket contains a pledge with the given id
     */
    public boolean hasPledge(int pledgeId) {
        return pledgeIds.contains(pledgeId);
    }

        /**
     * Updates an existing pledge in the basket.
     * * @param pledge The pledge with updated data
     * @return true if the pledge existed and was updated
     */
    public boolean updatePledge(Pledge pledge) {
        if (pledge == null || pledge.getOwnerId() != this.ownerId) {
            return false;
        }
        if (this.pledgeIds.remove(pledge.getId())) {
            return this.addPledge(pledge);
        }
        return false;
    }

    /**
     * Removes the pledge with the given id from this basket.
     *
     * @param pledgeId The id of the pledge to remove
     * @return {@code true} if a matching pledge was found and removed
     */
    public boolean removePledge(int pledgeId) {
        return pledgeIds.remove(pledgeId);
    }

    public Pledge[] checkout() {
        Pledge[] pledges = this.pledgeArray();
        this.removeAllPledges();
        return pledges;
    }

    /**
     * Removes all pledges from this basket (e.g., after checkout).
     */
    public void removeAllPledges() {
        pledgeIds.clear();
    }

    /**
     * @return The account owner ID of this basket
     */
    public int getOwnerId() {
        return ownerId;
    }

    /**
     * @return The pledges in this basket (cannot be modified by the caller)
     */
    @JsonIgnore
    public Collection<Pledge> getPledges() {
        List<Pledge> pledgeList = new ArrayList<>(pledgeIds.size());
        for (int pledgeId : pledgeIds) {
            if (catalog.hasPledge(pledgeId)) {
                Pledge pledge = catalog.getPledge(pledgeId);
                if (pledge != null) pledgeList.add(pledge);
            }
        }
        return Collections.unmodifiableList(pledgeList);
    }

    /** Two baskets are equal when they have the same ownerId and identical pledge maps. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PledgeBasket that = (PledgeBasket) o;
        return ownerId == that.ownerId && pledgeIds.equals(that.pledgeIds);
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(ownerId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(STRING_FORMAT, ownerId, pledgeIds);
    }
}
