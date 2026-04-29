package com.ufund.api.ufundapi.persistence;


import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties.Io;
import org.springframework.stereotype.Component;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufund.api.ufundapi.model.Pledge;


@Component
public class PledgeCatalogFileDAO implements PledgeCatalogDAO {
    private Map<Integer, Pledge> pledgeMap;
    private ObjectMapper objectMapper;
    private static int nextId;
    private String filename;

    public static PledgeCatalogFileDAO INSTANCE; // TODO band-aid solution, I will figure out something more permanent to allow reference to this from PledgeBasket

    public PledgeCatalogFileDAO(@Value("${pledges.file}") String filename, ObjectMapper objectMapper) throws IOException {
        this.filename = filename;
        this.objectMapper = objectMapper;
        this.load();

        INSTANCE = this; // TODO band-aid solution, I will figure out something more permanent to allow reference to this from PledgeBasket
    }

    /**
     ** {@inheritDoc}
     */
    public int nextId() { return nextId++; }

    /**
     ** {@inheritDoc}
     */
    public Pledge[] pledgeArray() { return this.pledgeMap.values().toArray(new Pledge[0]); }

    /**
     * Saves {@linkplain Pledge pledges} in memory to JSON file storage
     * @return {@code true} if save was successful
     * @throws IOException when file cannot be accessed or written to
     */
    private boolean save() throws IOException {
        if (this.objectMapper == null) {
            throw new IllegalStateException("ObjectMapper is not initialized");
        }
        Pledge[] pledges = this.pledgeArray();
        objectMapper.writeValue(new File(filename), pledges);
        return true;
    }

    /**
     * Loads {@linkplain Pledge pledges} into memory from JSON file storage
     * <br>
     * Sets the next ID to one greater than the maximum found ID
     * @return {@code true} if load was successful
     * @throws IOException when file cannot be accessed or written to
     */
    private boolean load() throws IOException {
        this.pledgeMap = new LinkedHashMap<>();
        nextId = 1;

        File file = new File(filename);
        Pledge[] pledges;

        if (file.exists()) {
            pledges = objectMapper.readValue(new File(filename), Pledge[].class);
        } else {
            pledges = new Pledge[0];
        }

        for (Pledge p : pledges) {
            if (p.getId() >= nextId) nextId = p.getId() + 1;
            pledgeMap.put(p.getId(), p);
        }

        return true;
    }

    /**
     ** {@inheritDoc}
     */
    public Pledge addPledgeFromArguments(int basketId, int needId, int amount, double money) throws IOException {
        Pledge pledge = new Pledge(this.nextId(), basketId, needId, amount, money);
        this.addPledge(pledge);
        return pledge;
    }

    /**
     ** {@inheritDoc}
     */
    public boolean addPledge(Pledge pledge) throws IOException {
        if (pledge == null || this.hasPledge(pledge.getId())) return false;
        this.pledgeMap.put(pledge.getId(), pledge);
        this.save();
        return true;
    }

    /**
     ** {@inheritDoc}
     */
    @Override
    public Pledge getPledge(int pledgeId) {
        if (!this.hasPledge(pledgeId)) return null;
        return pledgeMap.get(pledgeId);
    }

    /**
     ** {@inheritDoc}
     */
    public boolean hasPledge(Pledge pledge) {
        return pledge != null && pledgeMap.containsKey(pledge.getId());
    }

    /**
     ** {@inheritDoc}
     */
    public boolean hasPledge(int pledgeId) {
        return pledgeMap.containsKey(pledgeId);
    }

    /**
     ** {@inheritDoc}
     */
    @Override
    public boolean updatePledge(Pledge pledge) throws IOException {
        if (pledge == null || !this.hasPledge(pledge.getId())) {
            return false; // doesn't exist
        }
        this.pledgeMap.put(pledge.getId(), pledge);
        this.save(); // pledges.json
        return true;
}
    /**
     ** {@inheritDoc}
     */
    public boolean removePledge(int pledgeId) throws IOException {
        if (this.pledgeMap.remove(pledgeId) != null) {
            this.save();
            return true;
        };
        return false;
    }
}