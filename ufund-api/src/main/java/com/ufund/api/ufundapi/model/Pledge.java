package com.ufund.api.ufundapi.model;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Represents a Pledge entity
 * 
 * @author 5E
 */
public class Pledge {

    // Package private for tests
    static final String STRING_FORMAT = "Pledge [id=%d, ownerId=%s, needId=%d, quantityPledged=%d, moneyPledged=%f]";

    @JsonProperty("id") private int id;
    @JsonProperty("ownerId") private int ownerId; // account owner ID
    @JsonProperty("needId") private int needId; // ID of need the pledge is for
    @JsonProperty("quantityPledged") private int quantityPledged; // amount of items pledged
    @JsonProperty("moneyPledged") private double moneyPledged; // amount of money in dollars pledged


    /**
     * Create a pledge with the given id and owner
     * @param id The id of the pledge
     * @param ownerId The account owner ID
     * @param needId The ID of the need the pledge is for
     * @param quantityPledged The amount of items pledged
     * @param moneyPledged The amount of money in dollars pledged
     * 
     * {@literal @}JsonProperty is used in serialization and deserialization
     * of the JSON object to the Java object in mapping the fields.  If a field
     * is not provided in the JSON object, the Java field gets the default Java
     * value, i.e. 0 for int
     */
    public Pledge(int id, int ownerId, int needId, int quantityPledged, double moneyPledged) {
        this.id = id;
        this.ownerId = ownerId;
        this.needId = needId;
        this.quantityPledged = quantityPledged;
        this.moneyPledged = moneyPledged;
    }

    /**
     * Retrieves the id of the pledge
     * @return The id of the pledge
     */
    public int getId() { return this.id; }

    
    /**
     * Retrieves the id of the account owner of the pledge
     * @return The id of the account owner of the pledge
     */
    public int getOwnerId() { return this.ownerId; }

    
    /**
     * Retrieves the id of the need
     * @return The id of the need
     */
    public int getNeedId() { return this.needId; }

    /**
     * Retrieves the quantity pledged for the need
     * @return The quantity pledged for the need
     */
    public int getQuantity() { return this.quantityPledged; }

    /**
     * Sets the quantity pledged.
     * @param quantityPledged New quantity
     */
    public void setQuantity(int quantityPledged) { this.quantityPledged = quantityPledged; }

    /**
     * Retrieves the money pledged for the need
     * @return The money pledged for the need
     */
    public double getMoney() { return this.moneyPledged; } //Money is not in the MVP, but we can add it later if we have time
    
    /**
     * Sets the money pledged.
     * @param moneyPledged New dollar amount
     */
    public void setMoney(double moneyPledged) { this.moneyPledged = moneyPledged; }


    /**
     * Two pledges are equal when they share the same id.
     * This drives correct Set-membership behaviour inside {@link PledgeBasket}.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Pledge)) return false;
        return this.id == ((Pledge) obj).id;
    }

    @Override
    public int hashCode() { return Integer.hashCode(id); }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(STRING_FORMAT, id, ownerId, needId, quantityPledged, moneyPledged);
    }
}
