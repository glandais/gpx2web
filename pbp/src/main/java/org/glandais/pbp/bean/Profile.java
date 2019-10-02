package org.glandais.pbp.bean;

import java.util.List;

import lombok.Data;

@Data
public class Profile {

	private String name;
	
	private String bib;
	
	private String pid;
	
	private List<Split> splits;

}
