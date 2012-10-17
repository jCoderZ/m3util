package org.jcoderz.mp3.intern.db;

import java.io.File;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jcoderz.mp3.intern.util.Environment;

public class RepositoryDb {

    private static EntityManagerFactory sEntityManagerFactory;

    public static void startup(File basedir) {
        Properties p = new Properties();
        p.setProperty("hibernate.connection.url", "jdbc:derby:" + Environment.getDbFolder().getAbsolutePath() + File.separatorChar + "m3db;create=true");
        p.setProperty("hibernate.connection.driver_class", "org.apache.derby.jdbc.EmbeddedDriver");
        p.setProperty("hibernate.dialect", "org.hibernate.dialect.DerbyDialect");
        p.setProperty("hibernate.connection.username", "");
        p.setProperty("hibernate.connection.password", "");
        p.setProperty("hibernate.hbm2ddl.auto", "create");


        Logger.getLogger("org.hibernate").setLevel(Level.FINEST);
        sEntityManagerFactory = Persistence.createEntityManagerFactory("org.jcoderz.m3", p);
    }

    public static void close() {
        sEntityManagerFactory.close();
    }

    public static EntityManager getEntityManager() {
        return sEntityManagerFactory.createEntityManager();
    }
}
