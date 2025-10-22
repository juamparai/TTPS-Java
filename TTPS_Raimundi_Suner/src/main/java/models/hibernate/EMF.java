package models.hibernate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;

public class EMF {
    private static EntityManagerFactory em = null;
    private static EntityManagerFactory testEmf = null;

    static {
        try {
            em = Persistence.createEntityManagerFactory("unlp");
        } catch (PersistenceException e) {
            System.err.println("Error al crear EntityManagerFactory: "+e.getMessage());
            e.printStackTrace();
        }
    }

    public static EntityManagerFactory getEMF(){
        return em;
    }

    public static EntityManagerFactory getTestEMF() {
        if (testEmf == null) {
            testEmf = Persistence.createEntityManagerFactory("test-pu");
        }
        return testEmf;
    }

    private EMF() {}
}