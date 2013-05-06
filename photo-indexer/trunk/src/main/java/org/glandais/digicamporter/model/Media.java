package org.glandais.digicamporter.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class Media implements Comparable<Media> {

	@Id
	@GeneratedValue
	private Long id;

	private String fullPath;

	@Temporal(TemporalType.TIMESTAMP)
	private Date date;

	private String hash;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFullPath() {
		return fullPath;
	}

	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((fullPath == null) ? 0 : fullPath.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Media other = (Media) obj;
		if (fullPath == null) {
			if (other.fullPath != null)
				return false;
		} else if (!fullPath.equals(other.fullPath))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Media [hash=" + hash + ", date=" + date + ", fullPath="
				+ fullPath + "]";
	}

	public int compareTo(Media o) {
		long thisVal = this.date.getTime();
		long anotherVal = o.date.getTime();
		return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
	}
}
