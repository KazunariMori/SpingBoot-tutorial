package com.example.demo.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.demo.entity.Todo;
import com.example.demo.form.TodoQuery;

public interface TodoDao {
	
	List<Todo> findByJPQL(TodoQuery todoQuery ,Pageable pageable);
	Page<Todo> findByCriteria(TodoQuery todoQuery ,Pageable pageable);

}
