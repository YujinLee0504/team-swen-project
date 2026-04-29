package com.ufund.api.ufundapi.model;

import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a Need entity
 * 
 * @author SWEN Faculty
 */
public class Need {

    // Package private for tests
    static final String STRING_FORMAT = "Need [id=%d, name=%s, cost=%.2f, quantity=%d, type=%s]";

    @JsonProperty("id") private int id;
    @JsonProperty("creatorId") private int creatorId;
    @JsonProperty("creatorName") private String creatorName;
    @JsonProperty("name") private String name;
    @JsonProperty("desc") private String desc;
    @JsonProperty("icon") private String icon;
    @JsonProperty("cost") private double cost;
    @JsonProperty("quantity") private int quantity;
    @JsonProperty("quantityFulfilled") private double quantityFulfilled; // This is a double bc if it costs 3$ and they pledge 1$, this will go up by 0.333
    @JsonProperty("type") private String type;
    @JsonProperty("pledgeIds") private Set<Integer> pledgeIds;
    @JsonProperty("is_deleted") private boolean deleted;
    @JsonProperty("time_deleted") private long timeDeleted;

    /**
     * Create a need with the given id and name
     * @param id The id of the need
     * @param creatorId The id of the account that created the Need
     * @param creatorName The name of the account that created the Need
     * @param desc The optional description of the Need
     * @param name The name of the need
     * @param cost     The funding cost of the Need
     * @param quantity The number of units needed
     * @param type     The category/type of the Need eg. Canned, Fresh, etc.
     * @param isDeleted Whether this need has been deleted (archived)
     * @param timeDeletedMillis When this was deleted
     * 
     * {@literal @}JsonProperty is used in serialization and deserialization
     * of the JSON object to the Java object in mapping the fields.  If a field
     * is not provided in the JSON object, the Java field gets the default Java
     * value, i.e. 0 for int
     */
    @JsonCreator()
    public Need(@JsonProperty("id") int id, @JsonProperty("creatorId") int creatorId, @JsonProperty("creatorName") String creatorName, @JsonProperty("name") String name, @JsonProperty("desc") String desc, @JsonProperty("icon") String icon, @JsonProperty("cost") double cost, @JsonProperty("quantity") int quantity, @JsonProperty("type") String type,
            @JsonProperty("is_deleted") Boolean isDeleted, @JsonProperty("time_deleted") Long timeDeletedMillis) {
        this.id = id;
        this.creatorId = creatorId;
        this.creatorName = creatorName;
        this.name = name;
        this.desc = desc;
        this.icon = icon;
        this.cost = cost;
        this.quantity = quantity;
        this.type = type;
        this.quantityFulfilled = 0;
        this.deleted = Boolean.TRUE.equals(isDeleted);
        this.timeDeleted = timeDeletedMillis != null ? timeDeletedMillis : 0L;
    }

    /**
     * Create a need with the given id and name
     * @param id The id of the need
     * @param creatorId The id of the account that created the Need
     * @param creatorName The name of the account that created the Need
     * @param name The name of the need
     * @param desc The optional description of the Need
     * @param cost     The funding cost of the Need
     * @param quantity The number of units needed
     * @param type     The category/type of the Need eg. Canned, Fresh, etc.
     * @param quantityFulfilled The number of units that have been donated already (includes monetary and physical donations)
     * 
     * {@literal @}JsonProperty is used in serialization and deserialization
     * of the JSON object to the Java object in mapping the fields.  If a field
     * is not provided in the JSON object, the Java field gets the default Java
     * value, i.e. 0 for int
     */
    public Need(@JsonProperty("id") int id, @JsonProperty("creatorId") int creatorId, @JsonProperty("creatorName") String creatorName, @JsonProperty("name") String name, @JsonProperty("desc") String desc, @JsonProperty("icon") String icon, @JsonProperty("cost") double cost, @JsonProperty("quantity") int quantity , @JsonProperty("type") String type, @JsonProperty("quantityFulfilled") double quantityFulfilled) {
        this.id = id;
        this.creatorId = creatorId;
        this.creatorName = creatorName;
        this.name = name;
        this.desc = desc;
        this.icon = icon;
        this.cost = cost;
        this.quantity = quantity;
        this.type = type;
        this.quantityFulfilled = quantityFulfilled;
        this.pledgeIds = new LinkedHashSet<>();
        this.deleted = false;
        this.timeDeleted = 0L;
    }
    /**
     * Convenience constructor for tests and in-memory creation (not archived).
     */
    public Need(int id, String name, double cost, int quantity, String type) {
        this.id = id;
        this.name = name;
        this.creatorId = 0;
        this.creatorName = "doesnotexistgoawayplease";
        this.desc = "Placeholder";
        this.icon = "🍎";
        this.cost = cost;
        this.quantity = quantity;
        this.type = type;
        this.pledgeIds = new LinkedHashSet<>();
        this.quantityFulfilled = 0;
        this.deleted = false;
        this.timeDeleted = 0L;
    }

    public void markDeleted() {
        this.deleted = true;
        this.timeDeleted = System.currentTimeMillis();
    }

    public void clearDeleted() {
        this.deleted = false;
        this.timeDeleted = 0L;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public long getTimeDeleted() {
        return timeDeleted;
    }

    public void setTimeDeleted(long timeDeleted) {
        this.timeDeleted = timeDeleted;
    }

    public void setDesc(String desc) { this.desc = desc; }

    public void setIcon(String desc) { this.icon = icon; }

    /**
     * Adds a specified number of items as working to fulfill this need.
     * @param amount The number of items donated to help fulfill this need
     * @return true if the process was successful, false if it would have made the quantity fulfilled negative
     */
    public boolean fulfillQuantity( int amount ) {
        if (this.quantityFulfilled + amount < 0)
            return false;
        this.quantityFulfilled += amount;
        return true;
    }

    /**
     * Adds a specified amount of money as working to fulfill this need.
     * This amount can be negative, but it can't bring the total quantity fulfilled to be negative.
     * @param amount The amount of money donated to help fulfill this need
     * @return true if the process was successful, false if it would have made the quantity fulfilled negative
     */
    public boolean fulfillMoney( double amount ) {
        double effectiveQuantity = amount/this.cost;
        if (this.quantityFulfilled + effectiveQuantity < 0)
            return false;
        this.quantityFulfilled += effectiveQuantity;
        return true;
    }

    /**
     * Checks whether a Need has been fulfilled
     * @return Whether or not the Need's pledges sum up to fulfilling the Need
     */
    public boolean isComplete() {
        return quantityFulfilled >= quantity;
    }

    /**
     * Checks the completion percentage of the Need, based on its quantity and cost per item values
     * @return The completion percentage. Can be greater than 100.0
     */
    @JsonProperty("completionPercentage")
    public double completionPercentage() {
        if (quantity == 0)
            return 100;
        return (quantityFulfilled / quantity) * 100.0;
    }


    // GETTERS AND SETTERS:

    /**
     * Retrieves the id of the need
     * @return The id of the need
     */
    public int getId() {return id;}

    /**
     * Retrieves the id of the need
     * @return The id of the need
     */
    public int getCreatorId() {return this.creatorId;}

    public void setCreatorId(int creatorId) {this.creatorId = creatorId;}

    /**
     * Retrieves the name of account associated with the need
     * @return The id of the need
     */
    public String getCreatorName() {return this.creatorName;}

    public void setCreatorName(String creatorName) {this.creatorName = creatorName;}

    /**
     * Retrieves the name of the need
     * @return The name of the need
    */
   public String getName() {return name;}

   /**
     * Retrieves the description of the need
     * @return The description of the need
    */
   public String getDesc() {return desc;}

   /**
     * Retrieves the icon string of the need
     * @return The description of the need
    */
   public String getIcon() {return icon;}

   /**
    * Sets the name of the need - necessary for JSON object to Java object deserialization
    * @param name The name of the need
    */
   public void setName(String name) {this.name = name;}
   
   /**
    * Retrieves the cost of the Need
    * @return The cost of the Need
   */
    public double getCost() { return cost; }

    /**
     * Sets the cost of the Need
     * @param cost The cost of the Need
     */
    public void setCost(double cost) { this.cost = cost; }

    /**
     * Retrieves the quantity of the Need
     * @return The quantity of the Need
     */
    public int getQuantity() { return quantity; }

    /**
     * Sets the quantity of the Need
     * @param quantity The quantity of the Need
     */
    public void setQuantity(int quantity) { this.quantity = quantity; }

    /**
     * Retrieves the quantity fulfilled of the Need
     * @return The quantity of the Need
     */
    public double getQuantityFulfilled() { return quantityFulfilled; }

    /**
     * Sets the quantity fulfilled of the Need
     * @param quantity The quantity of the Need
     */
    public void setQuantityFulfilled(double quantityFulfilled) { this.quantityFulfilled = quantityFulfilled; }

    /**
     * Retrieves the type/category of the Need
     * @return The type of the Need
     */
    public String getType() { return type; }

    /**
     * Sets the type/category of the Need
     * @param type The type of the Need
     */
    public void setType(String type) { this.type = type; }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(STRING_FORMAT, id, name, cost, quantity, type);
    }
}