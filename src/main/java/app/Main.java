package app;

import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.config.Populate;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        // Parse booleans properly
        boolean deployed     = Boolean.parseBoolean(System.getenv().getOrDefault("DEPLOYED", "false"));
        boolean seedIfEmpty  = Boolean.parseBoolean(System.getenv().getOrDefault("SEED_IF_EMPTY", "true"));

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

        // Seed only if DB is empty (safe to run in prod, idempotent)
        if (seedIfEmpty && isDbEmpty(emf)) {
            System.out.println("Seeding database (empty detected)...");
            Populate.seed(emf);
            System.out.println("Done seeding.");
        } else {
            System.out.printf("Skipping seed (DEPLOYED=%s, SEED_IF_EMPTY=%s).%n", deployed, seedIfEmpty);
        }

        ApplicationConfig.startServer(7076);
    }

    private static boolean isDbEmpty(EntityManagerFactory emf) {
        try (EntityManager em = emf.createEntityManager()) {
            Long count = (Long) em.createQuery("select count(s) from Song s").getSingleResult();
            return count == 0L;
        }
    }
}