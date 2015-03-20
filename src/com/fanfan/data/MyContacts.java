package com.fanfan.data;

public class MyContacts {
	private long id;
	private String name;
	private String number;
	private boolean isSelect;
	private String fristName; // 用于分组

	public String getFristName() {
		return fristName;
	}

	public void setFristName(String fristName) {
		this.fristName = fristName;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public boolean isSelect() {
		return isSelect;
	}

	public void setSelect(boolean isSelect) {
		this.isSelect = isSelect;
	}

	public MyContacts() {
		super();
	}

	public MyContacts(String name, String number) {
		super();
		this.name = name;
		this.number = number;
	}

	public MyContacts(int id, String name, String number) {
		super();
		this.id = id;
		this.name = name;
		this.number = number;
	}

	@Override
	public String toString() {
		return "MyContacts [id=" + id + ", name=" + name + ", number=" + number
				+ ", isSelect=" + isSelect + ", fristName=" + fristName + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof MyContacts)) {
			return false;
		}
		if (o == this) {
			return true;
		}
		MyContacts contacts = (MyContacts) o;
		return this.number.equals(contacts.getNumber());
	}
}
