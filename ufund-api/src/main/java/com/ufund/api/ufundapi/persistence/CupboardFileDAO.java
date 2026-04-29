package com.ufund.api.ufundapi.persistence;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufund.api.ufundapi.model.Need;
import com.ufund.api.ufundapi.model.Pledge;

/**
 * Implements the functionality for JSON file-based persistence for Needs
 * 
 * {@literal @}Component Spring annotation instantiates a single instance of this
 * class and injects the instance into other classes as needed
 * 
 * @author 5E
 */
@Component
public class CupboardFileDAO implements CupboardDAO {
    private static final Logger LOG = Logger.getLogger(CupboardFileDAO.class.getName());

    Map<Integer, Need> needs;          // Local cache — avoids reading the file every time
    private ObjectMapper objectMapper; // Handles conversion between Need objects and JSON
    private static int nextId;         // The next id to assign to a new Need
    private String filename;           // Filename to read from and write to

    /**
     * Creates a Cupboard File Data Access Object
     *
     * @param filename     Filename to read from and write to
     * @param objectMapper Provides JSON Object to/from Java Object serialization and deserialization
     *
     * @throws IOException when file cannot be accessed or read from
     */
    public CupboardFileDAO(@Value("${needs.file}") String filename, ObjectMapper objectMapper) throws IOException {
        this.filename     = filename;
        this.objectMapper = objectMapper;
        load(); // load the needs from the file
    }

    /**
     * Generates the next id for a new {@linkplain Need need}
     *
     * @return The next id
     */
    private synchronized static int nextId() {
        int id = nextId;
        ++nextId;
        return id;
    }

    private Need[] allNeedsOrderedById() {
        return needs.values().toArray(new Need[0]);
    }

    /**
     * Active (non-archived) needs, optional name substring filter when {@code containsText} is non-null.
     */
    private Need[] getActiveNeedsArray(String containsText) {
        ArrayList<Need> needArrayList = new ArrayList<>();

        for (Need need : needs.values()) {
            if (need.isDeleted()) {
                continue;
            }
            if (containsText == null || need.getName().toLowerCase().contains(containsText.toLowerCase().strip())) {
                needArrayList.add(need);
            }
        }

        Need[] needArray = new Need[needArrayList.size()];
        needArrayList.toArray(needArray);
        return needArray;
    }

    private Need[] getArchivedNeedsArray() {
        List<Need> list = new ArrayList<>();
        for (Need need : needs.values()) {
            if (need.isDeleted()) {
                list.add(need);
            }
        }
        list.sort(Comparator.comparingLong(Need::getTimeDeleted).reversed());
        return list.toArray(new Need[0]);
    }

    /**
     * Saves the {@linkplain Need needs} from the map into the file as an array of JSON objects
     *
     * @return true if the {@link Need needs} were written successfully
     *
     * @throws IOException when file cannot be accessed or written to
     */
    private boolean save() throws IOException {
        Need[] needArray = allNeedsOrderedById();

        // Serializes the Java Objects to JSON objects into the file.
        // writeValue will throw an IOException if there is an issue
        // with the file or writing to the file.
        objectMapper.writeValue(new File(filename), needArray);
        return true;
    }

    /**
     * Loads {@linkplain Need needs} from the JSON file into the map
     * <br>
     * Also sets next id to one more than the greatest id found in the file
     *
     * @return true if the file was read successfully
     *
     * @throws IOException when file cannot be accessed or read from
     */
    private boolean load() throws IOException {
        needs  = new TreeMap<>();
        nextId = 0;

        // Deserializes the JSON objects from the file into an array of needs.
        // readValue will throw an IOException if there's an issue with the file
        // or reading from the file.
        Need[] needArray = objectMapper.readValue(new File(filename), Need[].class);

        // Add each need to the tree map and keep track of the greatest id
        for (Need need : needArray) {
            needs.put(need.getId(), need);
            if (need.getId() > nextId)
                nextId = need.getId();
        }
        // Make the next id one greater than the maximum from the file
        ++nextId;
        return true;
    }

    /**
     ** {@inheritDoc}
     */
    @Override
    public Need[] getNeeds() throws IOException {
        synchronized (needs) {
            return getActiveNeedsArray(null);
        }
    }

    @Override
    public Need[] getNeedsByIds(int[] ids) {
        synchronized (needs) {
            Set<Integer> idSet = Arrays.stream(ids).boxed().collect(Collectors.toCollection(LinkedHashSet::new));
            return Arrays.stream(getActiveNeedsArray(null)).filter(need -> idSet.contains(need.getId())).toArray(Need[]::new);
        }
    }

    /**
     ** {@inheritDoc}
     */
    @Override
    public Need[] findNeeds(String containsText) throws IOException {
        synchronized (needs) {
            return getActiveNeedsArray(containsText);
        }
    }

    /**
     ** {@inheritDoc}
     */
    @Override
    public Need getNeed(int id) throws IOException {
        synchronized (needs) {
            Need n = needs.get(id);
            if (n == null || n.isDeleted()) {
                return null;
            }
            return n;
        }
    }


    @Override
    public Need getNeedRecord(int id) throws IOException {
        synchronized (needs) {
            return needs.get(id);
        }
    }


    /**
     ** {@inheritDoc}
     */
    @Override
    public Need createNeed(Need need) throws IOException {
        synchronized (needs) {
            for (Need existingNeed : needs.values()){
                if (existingNeed.isDeleted()) {
                    continue;
                }
                if (existingNeed.getCreatorId() == need.getCreatorId() && existingNeed.getName().equalsIgnoreCase(need.getName())){
                    return null;
                }
            }


            // We create a new Need object because the id field is immutable
            // and we need to assign the next unique id
            Need newNeed = new Need(nextId(), need.getCreatorId(), need.getCreatorName(), need.getName(), need.getDesc(), need.getIcon(), need.getCost(), need.getQuantity(), need.getType(), need.isDeleted(), need.getTimeDeleted()); //just a stub of field for Need, can be changed later on
            needs.put(newNeed.getId(), newNeed);
            save();


            // return newNeed to signal 201 CREATED to the controller
            return newNeed;
        }
    }


    /**
     ** {@inheritDoc}
     */
    @Override
    public Need updateNeed(Need need) throws IOException {
        synchronized (needs) {
            Need existing = needs.get(need.getId());
            if (existing == null || existing.isDeleted()) {
                return null;
            }


            need.clearDeleted();


            needs.put(need.getId(), need);
            save(); // may throw an IOException
            return need;
        }
    }


    /**
     ** {@inheritDoc}
     */
    @Override
    public boolean deleteNeed(int id) throws IOException {
        synchronized (needs) {
            Need n = needs.get(id);
            if (n == null || n.isDeleted()) {
                return false;
            }
            n.markDeleted();
            return save();
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Need[] getArchivedNeeds() throws IOException {
        synchronized (needs) {
            return getArchivedNeedsArray();
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean restoreNeed(int id) throws IOException {
        synchronized (needs) {
            Need n = needs.get(id);
            if (n == null || !n.isDeleted()) {
                return false;
            }
            n.clearDeleted();
            return save();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Need permanentlyDeleteNeed(int id) throws IOException {
        synchronized (needs) {
            Need n = needs.get(id);
            if (n == null || !n.isDeleted()) {
                return null;
            }
            needs.remove(id);
            save();
            return n;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void distributeCheckout(Pledge[] checkingOutPledges) throws IOException {
        for (Pledge p : checkingOutPledges) {
            Need n = needs.get(p.getNeedId());
            if (n == null || n.isDeleted()) {
                continue;
            }
            n.fulfillQuantity(p.getQuantity());
            n.fulfillMoney(p.getMoney());
        }
        save();
    }
}
