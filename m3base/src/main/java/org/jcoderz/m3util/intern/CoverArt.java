package org.jcoderz.m3util.intern;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.DateUtils;
import org.jcoderz.commons.util.IoUtil;
import org.jcoderz.commons.util.StringUtil;

public class CoverArt {

    public static final String CLASSNAME = CoverArt.class.getName();
    public static final Logger logger = Logger.getLogger(CLASSNAME);
    private final HttpClient mClient = new DefaultHttpClient();
    private final File mCacheDir;
    private final String[] mServers = {
        "http://ec2.images-amazon.com/images/P/<ASIN>.03.LZZZZZZZ.jpg", // DE
        "http://ec1.images-amazon.com/images/P/<ASIN>.02.LZZZZZZZ.jpg", // UK
        "http://ec1.images-amazon.com/images/P/<ASIN>.01.LZZZZZZZ.jpg" // US
    };
    private Map<String, ImageData> mCache = new HashMap<>();

    @Deprecated
    public CoverArt() {
        throw new RuntimeException("No global asin cache available use repository cache!");
        // mCacheDir = new File("c:/tmp/asin/");
    }

    public CoverArt(File cacheDir) {
        mCacheDir = cacheDir;
    }

    public ImageData getImage(String asin) {
        ImageData result = null;

        if (mCache.containsKey(asin)) {
            result = mCache.get(asin);
            logger.info("Cache HIT " + (result == null ? "x " : "  ") + asin);
        } else {
            result = readCache(asin);
            if (result == null) {
                logger.info("Try for " + asin);
                if (asin.length() == 36) {
                    result = getImageByMbId(asin);
                } else {
                    for (String url : mServers) {
                        result = getImage(asin, url);
                        if (result != null) {
                            break;
                        }
                    }
                }
                if (result == null) {
                    writeCache(asin);
                }
                mCache.put(asin, result);
            }
        }
        return result == null ? null : (result.getImage().length == 0 ? null : result);
    }

//    private ImageData getImage (String asin, String url)
//    {
//        ImageData result = null;
//        try
//        {
//            final URL theUrl = new URL(url.replace("<ASIN>", asin));
//            URLConnection openConnection = theUrl.openConnection();
//            InputStream is = null;
//            try
//            {
//                openConnection.setReadTimeout(30000);
//                is = openConnection.getInputStream();
//                byte[] data = IoUtil.readFully(is);
//                if (data.length < 1024)
//                {
//                    result = null;
//                    throw new IOException("Could not get " + theUrl + " Image to small " + data.length);
//                }
//                long lastModified
//                    = openConnection.getHeaderFieldDate("Last-Modified", System.currentTimeMillis());
//                result = new ImageData(data, "image/jpeg", asin, lastModified);
//                writeCache(result);
//                Thread.sleep(500);
//            }
//            catch (IOException ex)
//            {
//                logger.info("Could not get " + theUrl + " got " + ex);
//            }
//            catch (InterruptedException e)
//            {
//                logger.info("Interrupted" +  e);
//            }
//            finally
//            {
//                IoUtil.close(is);
//            }
//        }
//        catch (IOException ex)
//        {
//            logger.info("Could not get data for " + asin + " got " + ex);
//        }
//
//        return result;
//    }
    private ImageData getImage(String asin, String url) {
        ImageData result = null;
        final String theUrl = url.replace("<ASIN>", asin);
        final HttpGet get = new HttpGet(theUrl);
        InputStream content = null;
        try {
            HttpResponse rsp = mClient.execute(get);
            content = rsp.getEntity().getContent();
            byte[] data = IoUtil.readFully(content);
            if (data.length < 1024) {
                result = null;
                throw new IOException("Could not get " + theUrl + " Image to small " + data.length);
            }
//            final Header lastMo = method.getResponseHeader("Last-Modified");
            long lastModified = System.currentTimeMillis();
//                Long.parseLong(lastMo.getValue());
            result = new ImageData(data, "image/jpeg", asin, lastModified);
            writeCache(result);
//                Thread.sleep(500);
        } catch (IOException ex) {
            logger.info("Could not get " + theUrl + " got " + ex);
        } finally {
            IoUtil.close(content);
        }

        return result;
    }

    private void writeCache(ImageData image) {
        if (!mCacheDir.exists()) {
            mCacheDir.mkdirs();
        }
        final File file = new File(mCacheDir, image.getAsin() + ".jpg");
        if (!file.exists()) {
            OutputStream out = null;
            try {
                out = new FileOutputStream(file);
                out.write(image.getImage());
            } catch (IOException ex) {
                logger.info("Could not cache data for " + image.getAsin() + " got " + ex);
            } finally {
                IoUtil.close(out);
            }
            file.setLastModified(image.getLastModified());
        }
    }

    private void writeCache(String asin) {
        if (!mCacheDir.exists()) {
            mCacheDir.mkdirs();
        }
        final File file = new File(mCacheDir, asin + ".jpg");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                logger.info("Could not empty cache data for " + asin + " got " + ex);
            }
        }
    }

    private ImageData readCache(String asin) {
        ImageData result = null;
        if (!mCacheDir.exists()) {
            mCacheDir.mkdirs();
        }
        final File file = new File(mCacheDir, asin + ".jpg");
        if (file.exists()) {
            InputStream in = null;
            try {
                in = new FileInputStream(file);
                byte[] data = IoUtil.readFully(in);
                result = new ImageData(data, "image/jpg", asin, file.lastModified());
            } catch (IOException ex) {
                logger.info("Could not read cache data for " + asin + " got " + ex);
            }
        }
        return result;
    }

    public static void main(String[] args) {
        CoverArt ca = new CoverArt(new File("C:/tmp/asin"));

        ImageData image = ca.getImage("76df3287-6cda-33eb-8e9a-044b5e15ffdd"); // B00000JP3Y");

        System.out.println("Image size: " + image.getImage().length);
    }

    private ImageData getImageByMbId(String mbId)
    {
       ImageData result = null;
        final String theUrl
                = "http://coverartarchive.org/release/<mb-id>/front".replace("<mb-id>", mbId);
        final HttpGet get = new HttpGet(theUrl);

        InputStream content = null;
        try {
            HttpResponse rsp = mClient.execute(get);
            content = rsp.getEntity().getContent();
            byte[] data = IoUtil.readFully(content);
            if (data.length < 1024) {
                result = null;
                throw new IOException(
                        "Could not get " + theUrl + " Image to small " + data.length
                        + " result " + rsp.getStatusLine() + "  text:" + new String(data) + " ");
            }

            String mimetype = rsp.getLastHeader("Content-Type").getValue();
            if (StringUtil.isEmptyOrNull(mimetype)) {
                mimetype = "image/jpeg";
            }
            if (!mimetype.equals("image/jpeg")) {
                throw new RuntimeException("Unsupported mimetype " + mimetype);
            }
            long lastModified;
            try {
                lastModified =
                        DateUtils.parseDate(
                            rsp.getLastHeader("last-modified").getValue()).getTime();
            } catch (Exception ex) {
                logger.log(Level.INFO, "Could not parse last modified header " + ex, ex);
                lastModified = System.currentTimeMillis();
            }

            result = new ImageData(data, mimetype, mbId, lastModified);
            writeCache(result);
//                Thread.sleep(500);
        } catch (IOException ex) {
            logger.info("Could not get mbid " + theUrl + " got " + ex);
        } finally {
            IoUtil.close(content);
        }

        return result;
    }

    static class ImageData {

        private final byte[] mImage;
        private final String mMimeType;
        private final String mAsin;
        private final long mLastModified;

        public ImageData(byte[] image, String mimeType, String asin, long lastModified) {
            mImage = image;
            mMimeType = mimeType;
            mAsin = asin;
            mLastModified = lastModified;
        }

        /**
         * @return the image
         */
        public byte[] getImage() {
            return mImage;
        }

        /**
         * @return the mimeType
         */
        public String getMimeType() {
            return mMimeType;
        }

        /**
         * @return the asin
         */
        public String getAsin() {
            return mAsin;
        }

        /**
         * @return the mlastModified
         */
        public long getLastModified() {
            return mLastModified;
        }
    }
}
