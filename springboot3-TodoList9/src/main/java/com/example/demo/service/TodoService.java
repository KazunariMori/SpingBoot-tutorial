package com.example.demo.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import com.example.demo.common.Utils;
import com.example.demo.entity.Todo;
import com.example.demo.form.TodoData;
import com.example.demo.form.TodoQuery;
import com.example.demo.repository.TodoRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class TodoService {
	
	private final TodoRepository todoRepository;
	private final MessageSource messageSource;

	public boolean isValid(TodoData todoData , BindingResult result , String mode , Locale locale) {
	
		boolean ans = true;
		
		//件名は全角スペースだけで構成されていたらエラー
		String title = todoData.getTitle();
		if (title != null && !title.equals("")) {
			boolean isAllDoubleSpace = true;
			for (int i = 0; i < title.length(); i++) {
				if (title.charAt(i) != '　') {
					isAllDoubleSpace = false;
					break;
				}
			}
			if (isAllDoubleSpace) {
				FieldError fieldError = new FieldError(result.getObjectName(), 
														"title", 
														messageSource.getMessage("DoubleSpace.todoData.title",null,locale));
				result.addError(fieldError);
				ans = false;
			}
		}
		
		//期限過去日付ならエラー
		String deadline = todoData.getDeadline();
		if (!deadline.equals("") && mode.equals("create")) {
			//データ登録の場合のみ、期限をチェックする
			LocalDate today = LocalDate.now();
			LocalDate deadLineDate = null;
			try {
				deadLineDate = LocalDate.parse(deadline);
				if (deadLineDate.isBefore(today)) {
					FieldError fieldError = new FieldError(result.getObjectName(), 
																"deadline", 
																messageSource.getMessage("Past.todoData.deadline",null,locale));

					result.addError(fieldError);
					ans=false;
				}
			} catch (Exception e) {
				// TODO: handle exception
				FieldError fieldError = new FieldError(result.getObjectName(),
														"deadline", 
														messageSource.getMessage("InvalidFormat.todoData.deadline",null,locale));

				result.addError(fieldError);
				ans= false;
			}
		}
		
		return ans;
	}
	
	public boolean isValid(TodoQuery todoQuery , BindingResult result , Locale locale) {
		
		boolean ans = true;
				
		//期限：開始の形式チェック
		String deadLineFrom = todoQuery.getDeadLineFrom();
		if (!deadLineFrom.equals("")) {
			try {
				LocalDate.parse(deadLineFrom);
			} catch (Exception e) {
				// TODO: handle exception
				FieldError fieldError = new FieldError(result.getObjectName(), 
														"deadLineFrom", 
														messageSource.getMessage("InvalidFormat.todoQuery.deadlineFrom", null, locale));
				result.addError(fieldError);
				ans= false;
			}
		}
		
		//期限：終了の形式チェック
		String deadLineTo = todoQuery.getDeadLineTo();
		if (!deadLineTo.equals("")) {
			try {
				LocalDate.parse(deadLineTo);
			} catch (Exception e) {
				// TODO: handle exception
				FieldError fieldError = new FieldError(result.getObjectName(), 
														"deadLineTo", 
														messageSource.getMessage("InvalidFormat.todoQuery.deadlineTo", null, locale));
				result.addError(fieldError);
				ans= false;
			}
		}

		return ans;
	}
	
	public List<Todo> doQuery(TodoQuery todoQuery){
		
		List<Todo> todoList = null;
		
		if (todoQuery.getTitle().length() > 0) {
			//タイトル検索
			todoList = todoRepository.findByTitleLike("%" + todoQuery.getTitle() + "%");
		}else if (todoQuery.getImportance() != null && todoQuery.getImportance() != -1) {
			//重要度検索
			todoList = todoRepository.findByImportance(todoQuery.getImportance());
		}else if (todoQuery.getUrgency() != null && todoQuery.getUrgency() != -1) {
			//緊急度検索
			todoList = todoRepository.findByUrgency(todoQuery.getUrgency());
		}else if (!todoQuery.getDeadLineFrom().equals("") && todoQuery.getDeadLineTo().equals("")) {
			//期限　開始～
			todoList = todoRepository.findByDeadlineGreaterThanEqualOrderByDeadlineAsc(Utils.str2date(todoQuery.getDeadLineFrom()));
		}else if (todoQuery.getDeadLineFrom().equals("") && !todoQuery.getDeadLineTo().equals("")) {
			//～期限　終了
			todoList = todoRepository.findByDeadlineLessThanEqualOrderByDeadlineAsc(Utils.str2date(todoQuery.getDeadLineTo()));
		}else if (!todoQuery.getDeadLineFrom().equals("") && !todoQuery.getDeadLineTo().equals("")) {
			//期限開始～期限終了
			todoList = todoRepository.findByDeadlineBetweenOrderByDeadlineAsc(
					Utils.str2date(todoQuery.getDeadLineFrom()),
					Utils.str2date(todoQuery.getDeadLineTo()));
		}else if (todoQuery.getDone() != null && todoQuery.getDone().equals("Y")) {
			//完了で検索
			todoList = todoRepository.findByDone(todoQuery.getDone());
		}else {
			//検索条件がなければ全件検索
			todoList = todoRepository.findAll();
		}
		
		return todoList;
		
	}

	
	

}
