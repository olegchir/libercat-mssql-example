/*
 */
package com.bellsoft;

import com.bellsoft.Persons;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Oleg Chirukhin
 */
@Path("data")
@Produces(MediaType.APPLICATION_JSON)
public class DBResource {

    @PersistenceUnit(unitName = "Tutorial")
    private EntityManagerFactory factory;

    @GET
    @Path("/persons")
    public String persons() {

        EntityManager entityManager = factory.createEntityManager();
        List<Persons> persons = entityManager.createQuery("SELECT p FROM Persons p").getResultList();
        String result = persons.stream()
                .map(Persons::toString)
                .collect(Collectors.joining(" | "));

        return result;
    }
}
