package com.kectech.android.wyslink.miscellaneous;

import android.support.annotation.Nullable;

import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Paul on 01/03/2016.
 * for upload progress
 */
public class VideoDownloadHttpEntity extends HttpEntityWrapper {

    private ProgressListener progressListener;

    public VideoDownloadHttpEntity(final HttpEntity entity, final ProgressListener progressListener) {
        super(entity);
        this.progressListener = progressListener;
    }

    @Override
    public void writeTo(OutputStream outStream) throws IOException {
        this.wrappedEntity.writeTo(outStream instanceof ProgressOutputStream ? outStream :
                new ProgressOutputStream(outStream, this.progressListener,
                        this.getContentLength()));
    }

    public interface ProgressListener {
        void transferred(float progress);
    }

    public static class ProgressOutputStream extends FilterOutputStream {

        private final ProgressListener progressListener;

        private long transferred;

        private long total;

        public ProgressOutputStream(final OutputStream outputStream,
                                    final ProgressListener progressListener,
                                    long total) {

            super(outputStream);
            this.progressListener = progressListener;
            this.transferred = 0;
            this.total = total;
        }

        @Override
        public void write(@Nullable byte[] buffer, int offset, int length) throws IOException {
            if (buffer != null) {
                out.write(buffer, offset, length);
                this.transferred += length;
                this.progressListener.transferred(this._getCurrentProgress());
            }
        }

        @Override
        public void write(@Nullable byte[] buffer) throws IOException {
            if (buffer != null) {
                out.write(buffer);
                this.transferred++;
                this.progressListener.transferred(this._getCurrentProgress());
            }
        }

        private float _getCurrentProgress() {
            return ((float) this.transferred / this.total) * 100;
        }
    }
}
