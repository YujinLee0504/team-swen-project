package com.ufund.api.ufundapi.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ufund.api.ufundapi.model.Need;
import com.ufund.api.ufundapi.services.CupboardService;
/**
 * Handles the REST API requests for the Cupboard (Need) resource
 * <p>
 * {@literal @}RestController Spring annotation identifies this class as a REST API
 * method handler to the Spring framework
 *
 * @author 5E
 */
@RestController
@RequestMapping("cupboard")
public class UfundController {
private static final Logger LOG = Logger.getLogger(UfundController.class.getName());
private CupboardService cupboardService;

    /**
     * Creates a REST API controller to respond to requests
     *
     * @param cupboardService The {@link cupboardService Cupboard Data Access Object} to
     *                    perform CRUD operations
     *                    <br>
     *                    This dependency is injected by the Spring Framework
     */
    public UfundController(CupboardService cupboardService) {
        this.cupboardService = cupboardService;
    }

    /**
     * Lists archived needs, most recently deleted first.
     */
    @GetMapping("/archive")
    public ResponseEntity<Need[]> getArchivedNeeds() {
        try {
            Need[] archived = cupboardService.getArchivedNeeds();
            return new ResponseEntity<>(archived, HttpStatus.OK);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getLocalizedMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Restores an archived need to the active cupboard.
     */
    @PostMapping("/archive/{id}/restore")
    public ResponseEntity<Need> restoreNeed(@PathVariable int id) {
        try {
            Need restored = cupboardService.restoreNeed(id);
            if (restored != null) {
                return new ResponseEntity<>(restored, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getLocalizedMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Permanently removes an archived need.
     */
    @DeleteMapping("/archive/{id}")
    public ResponseEntity<Need> permanentlyDeleteNeed(@PathVariable int id) {
        try {
            Need removed = cupboardService.permanentlyDeleteNeed(id);
            if (removed != null) {
                return new ResponseEntity<>(removed, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getLocalizedMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Responds to the GET request with a {@linkplain Need need} currently in the cupboard
     * 
     * @param id The id used to locate the {@link Need need}
     * 
     * @return ResponseEntity with {@link Need need} object and HTTP status of OK if found
     * ResponseEntity with HTTP status of NOT_FOUND if not found
     * ResponseEntity with HTTP status of INTERNAL_SERVER_ERROR otherwise
     */
    @GetMapping("/{id}")
    public ResponseEntity<Need> getNeed(@PathVariable int id) {
        try {
            Need need = cupboardService.getNeed(id);
            if (need != null){
                return new ResponseEntity<>(need, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getLocalizedMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Responds to the GET request with all {@linkplain Need needs} currently in the cupboard
     * 
     * @return ResponseEntity with array of {@link Need needs} objects (may be empty) and
     * HTTP status of OK<br>
     * ResponseEntity with HTTP status of INTERNAL_SERVER_ERROR otherwise
     */
    @GetMapping("")
    public ResponseEntity<Need[]> getNeeds() {
        LOG.info("GET /cupboard/need");
        try {
            Need[] needs = cupboardService.getNeeds();
            return new ResponseEntity<>(needs, HttpStatus.OK);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getLocalizedMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/multiple")
    public ResponseEntity<Need[]> getNeedsByIds(@RequestParam String commaSeparatedIds) {
        LOG.info("GET /cupboard/multiple?commaSeparatedIds="+commaSeparatedIds);
        int[] intArray = Arrays.stream(commaSeparatedIds.split(",")).map(String::trim).mapToInt(Integer::parseInt).toArray();
        try {
            Need[] needs = cupboardService.getNeedsByIds(intArray);
            return new ResponseEntity<>(needs, HttpStatus.OK);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getLocalizedMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Responds to the GET request for all {@linkplain Need needs} whose name contains
     * the text in name
     * 
     * @param name The name parameter which contains the text used to find the {@link Need needs}
     * 
     * @return ResponseEntity with array of {@link Need needs} objects (may be empty) and
     * HTTP status of OK<br>
     * ResponseEntity with HTTP status of INTERNAL_SERVER_ERROR otherwise
     * 
     * <p>
     * Example: Find all needs that contain the text "ca"
     * GET http://localhost:8080/cupboard/need/?name=ca
     * </p>
     */
    @GetMapping("/")
    public ResponseEntity<Need[]> searchNeeds(@RequestParam String name) {
        LOG.info("GET /cupboard/need/?name="+name);
        try {
            Need[] needs = cupboardService.findNeeds(name);
            return new ResponseEntity<>(needs, HttpStatus.OK);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getLocalizedMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Creates a {@linkplain Need need} with the provided need object
     *
     * @param need The {@link Need need} to create
     *
     * @return ResponseEntity with created {@link Need need} object and HTTP status of CREATED<br>
     * ResponseEntity with HTTP status of CONFLICT if a {@link Need need} with the same name already exists<br>
     * ResponseEntity with HTTP status of INTERNAL_SERVER_ERROR otherwise
     */
    @PostMapping("/need")
    public ResponseEntity<Need> createNeed(@RequestBody Need need) {
        LOG.info("POST /cupboard/need " + need);
        try {
            Need newNeed = cupboardService.createNeed(need);
            if (newNeed != null)
                return new ResponseEntity<>(newNeed, HttpStatus.CREATED);
            else
                return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getLocalizedMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Updates a {@linkplain Need need} with provided {@linkplain Need need} object, if the need exists
     * 
     * @param need the {@linkplain Need need} to update
     * 
     * @Return ResponseEntity with updated {@linkplain Need need} object and status code of OK if successful
     * ResponseEntity with status code of NOT_FOUND if the need does not exist
     * ResponseEntity with status code of INTERNAL_SERVER_ERROR otherwise
     */
    @PutMapping("")
    public ResponseEntity<Need> updateNeed(@RequestBody Need need) {
        try {
            Need updNeed = cupboardService.updateNeed(need);
            if (updNeed != null) {
                return new ResponseEntity<>(updNeed, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes a single {@linkplain Need need} with a specific ID from the cupboard if it exists.
     * 
     * @param id The id used to locate the {@link Need need} to be deleted
     * 
     * @return ResponseEntity with HTTP status of OK if deleted
     * ResponseEntity with HTTP status of NOT_FOUND if the need is not found
     * ResponseEntity with HTTP status of INTERNAL_SERVER_ERROR if the fileDAO produced an error
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Need> deleteNeed(@PathVariable int id) {
        try {
            Need toBeDeleted = cupboardService.getNeed(id);
            if (toBeDeleted != null) {
                cupboardService.deleteNeed(id);
                return new ResponseEntity<Need>(toBeDeleted, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}