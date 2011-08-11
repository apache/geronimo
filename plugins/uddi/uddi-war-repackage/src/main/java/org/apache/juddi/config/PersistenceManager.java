package org.apache.juddi.config;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PersistenceManager
{
  private static Log log = LogFactory.getLog(PersistenceManager.class);
  public static final String PERSISTENCE_UNIT_NAME = "juddiDatabase";
  private static EntityManagerFactory emf;

  public static EntityManager getEntityManager()
  {
    try
    {
      if (emf == null)
        AppConfig.getInstance();
    }
    catch (ConfigurationException e) {
      log.error("Error initializing config in PersistenceManager", e);
      throw new ExceptionInInitializerError(e);
    }

    return emf.createEntityManager();
  }

  public static void closeEntityManager() {
    if (emf.isOpen())
      emf.close();
  }

  protected static void initializeEntityManagerFactory(String persistenceUnitName) {
    try {
      if (emf == null)
        emf = Persistence.createEntityManagerFactory(persistenceUnitName, System.getProperties());
    }
    catch (Throwable t) {
      log.error("entityManagerFactory creation failed", t);
      throw new ExceptionInInitializerError(t);
    }
  }
}
