package com.ufund.api.ufundapi.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Acts as a holder class for the cupboard's collection of Needs
 * 
 * @author Group 5E
 */
public class NeedsList {
    private Map<Integer, Need> needs;

    /**
     * Creates an empty NeedsList
     */
    public NeedsList() {
        this.needs = new HashMap<>();
    }

    /**
     * Gets a Need with a specific id from the NeedsList, if it exists
     * @param id The id of the Need to return
     * @return The Need with the specified id, or null if it is not in the NeedsList
     */
    public Need getNeed(int id) {
        if (!this.hasNeed(id)) return null;

        return this.needs.get(id);
    }

    /**
     * Gets all Needs in the NeedsList
     * @return A Collection containing all Needs in the NeedsList
     */
    public Collection<Need> getNeeds() {
        return this.needs.values();
    }

    /**
     * Checks for the existence of a Need in the NeedsList with a specified id
     * @param id The id of the Need to check for
     * @return true if the Need exists in the NeedsList, otherwise false
     */
    public boolean hasNeed(int id) {
        return this.needs.containsKey(id);
    }

    /**
     * Adds a Need to the NeedsList, if a Need with the same id does not yet exist
     * @param need The Need to add to the NeedsList
     * @return true if the Need was added successfully, otherwise false if a Need with that id already exists in the NeedsList
     */
    public boolean addNeed(Need need) {
        if (this.hasNeed(need.getId())) return false;

        this.needs.put(need.getId(), need);
        return true;
    }

    /**
     * Removes a Need with a specified id from the NeedsList, if it exists
     * @param id The Need id to remove from the NeedsList
     * @return true if the Need was removed successfully, otherwise false if there was no Need in the NeedsList with the specified id
     */
    public boolean removeNeed(int id) {
        if (!this.needs.containsKey(id)) return false;
        this.needs.remove(id);
        return true;
    }

    /**
     * Returns a list of all Needs with 
     * @param name The name or part of a name to query for
     * @return A list of all Needs 
     */
    public ArrayList<Need> searchNeeds(String name) {
        ArrayList<Need> list = new ArrayList<Need>();
        for (Need n : needs.values()) {
            if (n.getName().contains(name)) list.add(n);
        }
        return list;
    }
}
