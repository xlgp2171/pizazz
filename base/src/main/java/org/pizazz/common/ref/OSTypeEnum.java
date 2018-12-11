package org.pizazz.common.ref;

/**
 * 操作系统类型枚举
 * 
 * @author xlgp2171
 * @version 1.0.181210
 */
public enum OSTypeEnum {
    /**
     * Windows
     */
	WINDOWS("windows", new String[] {
	        "cmd", "/c" }),
    /**
     * Linux
     */
	LINUX("linux", new String[] {
	        "/bin/bash", "-c" }),
    /**
     * Mac_OS
     */
	MAC_OS("mac", new String[] {
	        "/bin/bash", "-c" }),
    /**
     * Solaris
     */
	SOLARIS("solaris", new String[] {
	        "/bin/bash", "-c" }),
    /**
     * SunOS
     */
	SUN_OS("sun", new String[] {
	        "/bin/bash", "-c" }),
    /**
     * MPE_iX
     */
	MPE_IX("mpe", new String[] {
	        "/bin/bash", "-c" }),
    /**
     * HP_UX
     */
	HP_UX("hp", new String[] {
	        "/bin/bash", "-c" }),
    /**
     * AIX
     */
	AIX("aix", new String[] {
	        "/bin/bash", "-c" }),
    /**
     * OS_390
     */
	OS_390("os", new String[] {
	        "/bin/bash", "-c" }),
    /**
     * FreeBSD
     */
	FREE_BSD("free", new String[] {
	        "/bin/bash", "-c" }),
    /**
     * Irix
     */
	IRIX("irix", new String[] {
	        "/bin/bash", "-c" }),
    /**
     * Digital_Unix
     */
	DIGITAL_UNIX("digital", new String[] {
	        "/bin/bash", "-c" }),
    /**
     * OSF1
     */
	OSF1("osf1", new String[] {
	        "/bin/bash", "-c" }),
    /**
     * OpenVMS
     */
	OPEN_VMS("open", new String[] {
	        "/bin/bash", "-c" });

	private final String type;
	private final String[] environment;

	private OSTypeEnum(String type, String[] environment) {
		this.type = type;
		this.environment = environment;
	}

	public String[] getEnvironment() {
		return environment;
	}

	@Override
	public String toString() {
		return type;
	}
}
