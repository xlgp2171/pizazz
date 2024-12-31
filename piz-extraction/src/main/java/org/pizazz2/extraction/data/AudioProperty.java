package org.pizazz2.extraction.data;

public class AudioProperty extends FileProperty {
    /** 持续时长 */
    private float duration = -1F;
    /**  */
    private int bits;
    /** 通道 */
    private int channels;
    /** 采样率 */
    private int sampleRate;

    public AudioProperty(long length, String type, String suffix) {
        super(length, type, suffix);
    }

    public float getDuration() {
        return duration;
    }

    public AudioProperty setDuration(float duration) {
        this.duration = duration;
        return this;
    }

    public int getBits() {
        return bits;
    }

    public AudioProperty setBits(int bits) {
        this.bits = bits;
        return this;
    }

    public int getChannels() {
        return channels;
    }

    public AudioProperty setChannels(int channels) {
        this.channels = channels;
        return this;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public AudioProperty setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
        return this;
    }
}
