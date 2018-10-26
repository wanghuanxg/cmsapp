package com.contec.helper;

public class UserInfo {

	private String id;// 不为空，VARNUM(10) 用户序号
	private String uid;// 不为空，UID(20) 账号
	private String senderid;// （doctorId） 不为空，UID(15) 用户系统内部的唯一号
	private String pid;
	private String name;
	private String tel;
	private String notes;	
	private String area;
	private String areaid;
	private String createdate;
	private String hospitalid;
	private String hospitalname;
	private String transtype;
	private String hgroupid;
	private String hgroupname;
	private String sid;
	private String anotherlogininfo;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getSenderid() {
		return senderid;
	}
	public void setSenderid(String senderid) {
		this.senderid = senderid;
	}
	public String getPid() {
		return pid;
	}
	public void setPid(String pid) {
		this.pid = pid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTel() {
		return tel;
	}
	public void setTel(String tel) {
		this.tel = tel;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
	public String getAreaid() {
		return areaid;
	}
	public void setAreaid(String areaid) {
		this.areaid = areaid;
	}
	public String getCreatedate() {
		return createdate;
	}
	public void setCreatedate(String createdate) {
		this.createdate = createdate;
	}
	public String getHospitalid() {
		return hospitalid;
	}
	public void setHospitalid(String hospitalid) {
		this.hospitalid = hospitalid;
	}
	public String getHospitalname() {
		return hospitalname;
	}
	public void setHospitalname(String hospitalname) {
		this.hospitalname = hospitalname;
	}
	public String getTranstype() {
		return transtype;
	}
	public void setTranstype(String transtype) {
		this.transtype = transtype;
	}
	public String getHgroupid() {
		return hgroupid;
	}
	public void setHgroupid(String hgroupid) {
		this.hgroupid = hgroupid;
	}
	public String getHgroupname() {
		return hgroupname;
	}
	public void setHgroupname(String hgroupname) {
		this.hgroupname = hgroupname;
	}
	public String getSid() {
		return sid;
	}
	public void setSid(String sid) {
		this.sid = sid;
	}
	public String getAnotherlogininfo() {
		return anotherlogininfo;
	}
	public void setAnotherlogininfo(String anotherlogininfo) {
		this.anotherlogininfo = anotherlogininfo;
	}
 
}
