package org.jcoderz.mp3.intern.db;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

public class RepositoryDb
{
    private static EntityManagerFactory sEntityManagerFactory;
    
    public static void startup(File basedir)
    {
      //  Class.forName("org.hsqldb.jdbc.JDBCDriver");
//        Configuration cfg = new Configuration()
//            // .addClass(org.hibernate.auction.Item.class)
//            // .addClass(org.hibernate.auction.Bid.class)
//            .setProperty("hibernate.connection.driver_class", "org.hsqldb.jdbc.JDBCDriver")
//            .setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect")
//            .setProperty("hibernate.connection.url", "jdbc:hsqldb:file:" // /tmp/tools/var/db/hsqldb") 
//                + new File(basedir.getAbsolutePath(), "/tools/var/db/hsqldb/db").getAbsolutePath())  
//            .setProperty("hibernate.order_updates", "true")
//            .setProperty("hibernate.show_sql", "true")
//            .setProperty("hibernate.hbm2ddl.auto", "update")
//            .addAnnotatedClass(MediaFile.class);
        Logger.getLogger("org.hibernate").setLevel(Level.FINEST);
//        sSessionFactory = cfg.buildSessionFactory();
//        
//        Session session = getSession();
//        session.createSQLQuery("SET DATABASE DEFAULT TABLE TYPE CACHED;").executeUpdate();
//        session.close();

        sEntityManagerFactory 
            = Persistence.createEntityManagerFactory("org.jcoderz.mp3");
//        EntityManager em = getEntityManager();
////        em.getTransaction().begin();
////        Query query = em.createNativeQuery("SET DATABASE DEFAULT TABLE TYPE CACHED;");
////        query.executeUpdate();
////        em.getTransaction().commit();
//        em.close();
    }
    
    public static void main (String[] args)
    {
//        startup(new File("/tmp/"));
//        Session session = sSessionFactory.openSession();
//        session.beginTransaction();
//        List result = session.createQuery("from MediaFile").list();
//        for (MediaFile f : (List<MediaFile>) result)
//        {
//            System.out.println("Event (" + f.getLocation() + ") : ");
//        }
//        session.getTransaction().commit();
//        session.close();
    }

    public static void close ()
    {
        sEntityManagerFactory.close();
    }

    public static EntityManager getEntityManager ()
    {
        return sEntityManagerFactory.createEntityManager();
    }
}
