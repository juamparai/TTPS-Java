package models.hibernate;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;

public class EMF {
    private static EntityManagerFactory em = null;
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
    private EMF() {}
}
