package org.jcoderz.mp3.intern.db.types;


import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


@Entity
@Table(name = "MEDIA_FILES")
public class MediaFile
{
    private String mSha1;

    private String mLocation;

    private String mUuId;

    private Date mLastModified;

    private String mMbId;

    private int mFileSize;

    private Date mUpdateRun;

    
    @Id
    @Column(name = "ID", length = 36, nullable = false, columnDefinition = "char(36)")
    public String getUuId ()
    {
        return mUuId;
    }

    @Column(name = "LOCATION", length = 1024, nullable = false, unique = true)
    public String getLocation ()
    {
        return mLocation;
    }

    @Column(name = "SHA1", length = 40, nullable = false, columnDefinition = "char(40)")
    public String getSha1 ()
    {
        return mSha1;
    }

    @Column(name = "LAST_MODIFIED")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getLastModified()
    {
        return mLastModified;
    }

    @Column(name = "FILE_SIZE")
    public int getFileSize()
    {
        return mFileSize;
    }

    @Column(name = "UPDATE_RUN")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getUpdateRun ()
    {
        return mUpdateRun;
    }

    @Column(name = "MBID", length = 36, columnDefinition = "char(36)")
    public String getMbId ()
    {
        return mMbId;
    }

    public void setMbId (String mbId)
    {
        mMbId = mbId;
    }

    public void setLastModified (Date lastModified)
    {
        mLastModified = lastModified;
    }

    public void setSha1 (String sha1)
    {
        mSha1 = sha1;
    }

    public void setLocation (String location)
    {
        mLocation = location;
    }

    public void setUuId (String uuid)
    {
        mUuId = uuid;
    }

    public void setFileSize (int fileSize)
    {
        mFileSize = fileSize;
    }

    public void setUpdateRun (Date updateRun)
    {
        mUpdateRun = updateRun;
    }

}
