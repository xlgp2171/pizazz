package org.pizazz2.tool.ref;

import org.pizazz2.PizContext;

/**
 * ID生成组件对象
 * 
 * @author xlgp2171
 * @version 2.0.210201
 */
public class IdObject {
	/**
	 * piz版本{@link PizContext#VERSION}
	 */
	private final byte version;
	/**
	 * 自定义序号
	 */
	private final short custom;
	/**
	 * 时间戳顺序号
	 */
	private short sequence;
	/**
	 * 时间戳
	 */
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
			return org.pizazz2.common.JSONUtils.toJSON(this);
		} catch (Exception e) {
			return new StringBuilder("version=").append(version).append(",timestamp=").append(timestamp)
					.append(",sequence=").append(sequence).append(",custom=").append(custom).toString();
		}
	}
}
