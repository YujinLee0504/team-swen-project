package com.ufund.api.ufundapi.services;

import com.ufund.api.ufundapi.exceptions.UsernameTakenException;
import com.ufund.api.ufundapi.model.PledgeBasket;
import com.ufund.api.ufundapi.model.User;
import com.ufund.api.ufundapi.model.User.AccountType;
import com.ufund.api.ufundapi.persistence.PledgeBasketsDAO;
import com.ufund.api.ufundapi.persistence.UserDAO;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserDAO userDao;
    private PledgeBasketsDAO pledgeBasketsDao;

    public UserService(UserDAO userDAO, PledgeBasketsDAO pledgeBasketsDao) {
        this.userDao = userDAO;
        this.pledgeBasketsDao = pledgeBasketsDao;
    }

    public User getUser(String username) throws IOException {
        if (userDao.findUsers(username).length != 0) {
            User user = userDao.findUsers(username)[0];
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null;
    }

    public User[] getWorldMap() throws IOException {
        User[] allUsers = userDao.findUsers("");
        return Arrays.stream(allUsers).filter(user -> user.getLatitude() != 0.0 || user.getLongitude() != 0.0).toArray(User[]::new);
    }

    public User createUser(String username, String password, String accountType, double latitude, double longitude) throws UsernameTakenException, IOException {
        if (getUser(username) != null) {
            throw new UsernameTakenException(username);
        }

        AccountType creationType = AccountType.User;
        if ("Manager".equals(accountType)) {
            creationType = AccountType.Manager;
        }

        User user = userDao.createUser(new User(0, username, password, creationType, latitude, longitude));
        pledgeBasketsDao.addPledgeBasket(new PledgeBasket(user.getId(), Collections.emptyList()));
        return user;
    }
}