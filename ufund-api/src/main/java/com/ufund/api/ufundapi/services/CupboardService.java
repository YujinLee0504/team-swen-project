package com.ufund.api.ufundapi.services;
import com.ufund.api.ufundapi.model.Need;
import com.ufund.api.ufundapi.persistence.CupboardDAO;
import com.ufund.api.ufundapi.persistence.PledgeBasketsDAO;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

@Service
public class CupboardService {

    private final CupboardDAO cupboardDao;
    private final PledgeBasketsDAO pledgeBasketsDao;
    private final BasketNotificationService basketNotificationService;

        /**
     * Creates an API service to mediate controller and cupboard data
     *
     * @param cupboardDao The {@link CupboardDAO Cupboard Data Access Object} to
     *                    perform CRUD operations
     *                    <br>
     *                    This dependency is injected by the Spring Framework
     */
    public CupboardService(
            CupboardDAO cupboardDao,
            PledgeBasketsDAO pledgeBasketsDao,
            BasketNotificationService basketNotificationService) {
        this.cupboardDao = cupboardDao;
        this.pledgeBasketsDao = pledgeBasketsDao;
        this.basketNotificationService = basketNotificationService;
    }

    public Need getNeed(@PathVariable int id) throws IOException{
        return cupboardDao.getNeed(id);
    }

    public Need[] getNeeds() throws IOException {
        return cupboardDao.getNeeds();
    }

    public Need[] getNeedsByIds(int[] ids) throws IOException {
        return cupboardDao.getNeedsByIds(ids);
    }

    public Need[] findNeeds(String query) throws IOException {
        return cupboardDao.findNeeds(query);
    }

    public Need createNeed(Need need) throws IOException {
        return cupboardDao.createNeed(need);
    }

    public boolean deleteNeed(int id) throws IOException {
        Need active = cupboardDao.getNeed(id);
        if (active == null) {
            return false;
        }
        String needName = active.getName();
        if (!cupboardDao.deleteNeed(id)) {
            return false;
        }
        int[] affectedOwners = pledgeBasketsDao.removePledgesForNeed(id);
        if (affectedOwners.length > 0) {
            basketNotificationService.addArchivedNeedRemovedMessages(affectedOwners, needName);
        }
        return true;
    }

    public Need[] getArchivedNeeds() throws IOException {
        return cupboardDao.getArchivedNeeds();
    }

    public Need restoreNeed(int id) throws IOException {
        if (cupboardDao.restoreNeed(id)) {
            return cupboardDao.getNeed(id);
        }
        return null;
    }

    public Need permanentlyDeleteNeed(int id) throws IOException {
        return cupboardDao.permanentlyDeleteNeed(id);
    }

    public Need updateNeed(Need need) throws IOException {
        return cupboardDao.updateNeed(need);
    }
}
