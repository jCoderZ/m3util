package org.jcoderz.mp3.intern.db;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jcoderz.mp3.intern.db.types.MediaFile;

public class UploadCleaner
{
    public static void main (String[] args)
    {
        RepositoryDb.startup(null);
        EntityManager entityManager = RepositoryDb.getEntityManager();
        entityManager.getTransaction().begin();
        final Query mfQuery = entityManager.createQuery("from MediaFile where LOCATION LIKE 'upload/%'");
        List<MediaFile> resultList = mfQuery.getResultList();
        for (MediaFile mf : resultList)
        {
            final Query sha1q = entityManager.createQuery("select count (*) from MediaFile where sha1 = :sha1");
            sha1q.setParameter("sha1", mf.getSha1());
            int cnt = ((Long) (sha1q.getSingleResult())).intValue();
            if (cnt > 1)
            {
                dumpValues(entityManager, mf.getSha1());
            }
        }
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    private static void dumpValues (EntityManager entityManager, String sha1)
    {
        final Query sha1q = entityManager.createQuery("from MediaFile where sha1 = :sha1");
        sha1q.setParameter("sha1", sha1);
        List<MediaFile> resultList = sha1q.getResultList();
        System.out.println("************** SHA1 Dupes *************");
        for (MediaFile mf : resultList)
        {
            System.out.println(mf.getLocation());
        }
        
    }
}
