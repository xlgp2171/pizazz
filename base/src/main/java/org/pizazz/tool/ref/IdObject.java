package org.pizazz.tool.ref;

/**
 * ID生成组件对象
 * 
 * @author xlgp2171
 * @version 1.0.190224
 */
public class IdObject {
	private final byte version;
	private final short custom;
	private short sequence;
	private long timestamp;

	public IdObject(byte version, short custom) {
		this.version = version;
		this.custom = custom;
	}

	public byte getVersion() {
		return version;
	}

	public short getCustom() {
		return custom;
	}

	public short getSequence() {
		return sequence;
	}

	public void setSequence(short sequence) {
		this.sequence = sequence;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		try {
			return org.pizazz.common.JSONUtils.toJSON(this);
		} catch (Exception e) {
			return new StringBuilder("version=").append(version).append(",timestamp=").append(timestamp)
					.append(",sequence=").append(sequence).append(",custom=").append(custom).toString();
		}
	}
}
