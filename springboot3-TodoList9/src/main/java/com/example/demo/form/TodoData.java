package com.example.demo.form;

import java.sql.Date;
import java.text.SimpleDateFormat;

import com.example.demo.entity.Todo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TodoData {
	
	private Integer id;
	
	@NotBlank
	private String title;
	
	@NotNull
	private Integer importance;
	
	@Min(value = 0)
	private Integer urgency;
	
	private String deadline;
	private String done;
	
	public Todo toEntity(){
		
		Todo todo = new Todo();
		todo.setId(id);
		todo.setImportance(importance);
		todo.setUrgency(urgency);
		todo.setTitle(title);
		
		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
		long ms;
		
		try {
			ms = sdFormat.parse(deadline).getTime();
			todo.setDeadline(new Date(ms));
		} catch (Exception e) {
			// TODO: handle exception
			todo.setDeadline(null);
		}
		todo.setDone(done);
		
		return todo;
	}
}

