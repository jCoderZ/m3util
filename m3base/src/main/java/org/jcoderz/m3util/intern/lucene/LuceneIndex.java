package org.jcoderz.m3util.intern.lucene;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * Deals with the Lucene index. Creates a new or opens an existing index.
 * Updates documents in the index.
 *
 * @author mrumpf
 */
public class LuceneIndex {

    private static final String CLASSNAME = LuceneIndex.class.getName();
    private static final Logger logger = Logger.getLogger(CLASSNAME);
    private IndexWriter writer = null;

    /**
     * Create a new index or open an existing one.
     *
     * @param indexPath The path of the index folder
     * @return true when index creation succeeded
     */
    public boolean open(File indexPath) {
        boolean result = false;
        try {
            logger.info("Opening Lucene index from " + indexPath);
            Directory dir = FSDirectory.open(indexPath);
            result = open(dir);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Could not open Lucene index", ex);
        }
        return result;
    }

    /**
     * Create a new index or open an existing one.
     *
     * @param indexPath The path of the index folder
     * @return true when index creation succeeded
     */
    public boolean open(Directory dir) {
        boolean result = false;
        try {
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_36,
                    analyzer);
            iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
            writer = new IndexWriter(dir, iwc);
            result = true;
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Could not create or open the Lucene index", ex);
        }
        return result;
    }

    /**
     * Updates the specified document.
     *
     * @param doc the document to add
     * @return true when successful, false otherwise
     */
    public boolean updateDocument(Document doc) {
        boolean result = false;
        try {
            Fieldable f = doc.getFieldable("uuid");
            if (f != null) {
                Term term = new Term("uuid", f.stringValue());
                writer.updateDocument(term, doc);
                result = true;
            }
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Could not update document " + doc, ex);
        }
        return result;
    }

    /**
     * Close the lucene index.
     *
     * @return true when successful, false otherwise
     */
    public boolean close() {
        boolean result = false;
        if (writer != null) {
            try {
                writer.close();
                result = true;
            } catch (IOException ex) {
                logger.log(Level.WARNING, "Could not close Lucene index", ex);
            }
        }
        return result;
    }
}
