package com.ufund.api.ufundapi.services;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;

/**
 * One-time messages for donors (e.g. pledges removed when a need is archived).
 * Messages are drained when the client loads the basket.
 */
@Service
public class BasketNotificationService {

    private final ConcurrentHashMap<Integer, CopyOnWriteArrayList<String>> pendingByOwnerId = new ConcurrentHashMap<>();

    public void addArchivedNeedRemovedMessage(int ownerId, String needName) {
        String text = "The food bank removed \"" + needName
                + "\" from the cupboard. It was removed from your pledge basket.";
        pendingByOwnerId.computeIfAbsent(ownerId, k -> new CopyOnWriteArrayList<>()).add(text);
    }

    public void addArchivedNeedRemovedMessages(int[] ownerIds, String needName) {
        if (ownerIds == null) {
            return;
        }
        for (int ownerId : ownerIds) {
            addArchivedNeedRemovedMessage(ownerId, needName);
        }
    }

    /**
     * @return all pending messages for this owner and clears them
     */
    public String[] drainMessages(int ownerId) {
        List<String> list = pendingByOwnerId.remove(ownerId);
        if (list == null || list.isEmpty()) {
            return new String[0];
        }
        return list.toArray(new String[0]);
    }
}
