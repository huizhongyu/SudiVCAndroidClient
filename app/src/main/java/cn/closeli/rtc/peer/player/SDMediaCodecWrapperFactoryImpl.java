package cn.closeli.rtc.peer.player;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCrypto;
import android.media.MediaFormat;
import android.media.MediaCodec.BufferInfo;
import android.os.Bundle;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

interface SDMediaCodecWrapperFactory {
    SDMediaCodecWrapper createByCodecName(String var1) throws IOException;
}

public class SDMediaCodecWrapperFactoryImpl implements SDMediaCodecWrapperFactory {
    public SDMediaCodecWrapperFactoryImpl() {
    }

    public SDMediaCodecWrapper createByCodecName(String name) throws IOException {
        return new SDMediaCodecWrapperFactoryImpl.MediaCodecWrapperImpl(MediaCodec.createByCodecName(name));
    }

    private static class MediaCodecWrapperImpl implements SDMediaCodecWrapper {
        private final MediaCodec mediaCodec;

        public MediaCodecWrapperImpl(MediaCodec mediaCodec) {
            this.mediaCodec = mediaCodec;
        }

        public void configure(MediaFormat format, Surface surface, MediaCrypto crypto, int flags) {
            this.mediaCodec.configure(format, surface, crypto, flags);
        }

        public void start() {
            this.mediaCodec.start();
        }

        public void flush() {
            this.mediaCodec.flush();
        }

        public void stop() {
            this.mediaCodec.stop();
        }

        public void release() {
            this.mediaCodec.release();
        }

        public int dequeueInputBuffer(long timeoutUs) {
            return this.mediaCodec.dequeueInputBuffer(timeoutUs);
        }

        public void queueInputBuffer(int index, int offset, int size, long presentationTimeUs, int flags) {
            this.mediaCodec.queueInputBuffer(index, offset, size, presentationTimeUs, flags);
        }

        public int dequeueOutputBuffer(BufferInfo info, long timeoutUs) {
            return this.mediaCodec.dequeueOutputBuffer(info, timeoutUs);
        }

        public void releaseOutputBuffer(int index, boolean render) {
            this.mediaCodec.releaseOutputBuffer(index, render);
        }

        public MediaFormat getOutputFormat() {
            return this.mediaCodec.getOutputFormat();
        }

        public ByteBuffer[] getInputBuffers() {
            return this.mediaCodec.getInputBuffers();
        }

        public ByteBuffer[] getOutputBuffers() {
            return this.mediaCodec.getOutputBuffers();
        }

        @TargetApi(18)
        public Surface createInputSurface() {
            return this.mediaCodec.createInputSurface();
        }

        @TargetApi(19)
        public void setParameters(Bundle params) {
            this.mediaCodec.setParameters(params);
        }
    }
}