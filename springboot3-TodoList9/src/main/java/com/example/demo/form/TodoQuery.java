package com.example.demo.form;

import lombok.Data;

@Data
public class TodoQuery {
	
	private String title;
	private Integer importance;
	private Integer urgency;
	private String deadLineFrom;
	private String deadLineTo;
	private String done;
	
	public TodoQuery() {
		title="";
		importance = -1;
		urgency=-1;
		deadLineFrom = "";
		deadLineTo = "";
		done="";
	}

}
