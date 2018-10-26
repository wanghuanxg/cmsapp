package com.contec.helper;

public class Versioninfo {

	private String fname;//文件名
	private String path;//文件路径
	private String version;//版本号
	private String type;//升级类型



    private Long size;
	/**
	 * @return fname
	 */
	public String getFname() {
		return fname;
	}
	/**
	 * @param fname 要设置的 fname
	 */
	public void setFname(String fname) {
		this.fname = fname;
	}
	/**
	 * @return path
	 */
	public String getPath() {
		return path;
	}
	/**
	 * @param path 要设置的 path
	 */
	public void setPath(String path) {
		this.path = path;
	}
	/**
	 * @return version
	 */
	public String getVersion() {
		return version;
	}
	/**
	 * @param version 要设置的 version
	 */
	public void setVersion(String version) {
		this.version = version;
	}
	/**
	 * @return type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type 要设置的 type
	 */
	public void setType(String type) {
		this.type = type;
	}

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }
}
