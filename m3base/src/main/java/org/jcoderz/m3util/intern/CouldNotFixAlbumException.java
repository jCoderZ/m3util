package org.jcoderz.m3util.intern;

public final class CouldNotFixAlbumException
        extends Exception {

    private static final long serialVersionUID = 1L;

    public CouldNotFixAlbumException(String message) {
        super(message);
    }

    public CouldNotFixAlbumException(String message, Throwable thr) {
        super(message, thr);
    }
}