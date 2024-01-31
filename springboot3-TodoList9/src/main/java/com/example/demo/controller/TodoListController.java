package com.example.demo.controller;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.common.OpMsg;
import com.example.demo.dao.TodoDaoImpl;
import com.example.demo.entity.Todo;
import com.example.demo.form.TodoData;
import com.example.demo.form.TodoQuery;
import com.example.demo.repository.TodoRepository;
import com.example.demo.service.TodoService;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TodoListController {
	
	private final TodoRepository todoRepository;
	private final TodoService todoService;
	private final HttpSession session;
	
	@PersistenceContext
	private EntityManager entityManager;
	private TodoDaoImpl todoDaoImpl;
	
	private final MessageSource messageSource;
	
	@PostConstruct
	public void init() {
		todoDaoImpl = new TodoDaoImpl(entityManager);
	}
	
	@GetMapping("/todo")
	public ModelAndView showTodoList(ModelAndView mv , @PageableDefault(page = 0 , size = 5 , sort = "id") Pageable pageable){
		
		//一覧を検索して表示する
		mv.setViewName("todoList");
		
        // sessionから前回の検索条件を取得
        TodoQuery todoQuery = (TodoQuery)session.getAttribute("todoQuery");
        if (todoQuery == null) {
            //なければ初期値を使う
            todoQuery = new TodoQuery();
            session.setAttribute("todoQuery", todoQuery);
        }
        
        // sessionから前回のpageableを取得
        Pageable prevPageable = (Pageable)session.getAttribute("prevPageable");
        if (prevPageable == null) {
            //なければ@PageableDefaultを使う
            prevPageable = pageable;
            session.setAttribute("prevPageable", prevPageable);
        }

		Page<Todo> todoPage = todoDaoImpl.findByCriteria(todoQuery, prevPageable);
		mv.addObject("todoPage", todoPage);
		mv.addObject("todoList", todoPage.getContent());
		mv.addObject("todoQuery", new TodoQuery());
		
		return  mv;
	}
	
	// Todoの検索処理
	@PostMapping("/todo/query")
	public ModelAndView queryTodo(@ModelAttribute TodoQuery todoQuery , 
												BindingResult result , 
												ModelAndView mv,
												@PageableDefault(page=0,size=5) Pageable pageable,
												Locale locale) {
		
		mv.setViewName("todoList");
		Page<Todo> todoPage = null;
		
		if (todoService.isValid(todoQuery, result,locale)) {
//			todoList = todoService.doQuery(todoQuery);
//			todoPage = todoDaoImpl.findByJPQL(todoQuery , pageable));
			todoPage = todoDaoImpl.findByCriteria(todoQuery, pageable);
			
			//該当はなかったらメッセージを表示
			if (todoPage.getContent().size() == 0) {
				String msg = messageSource.getMessage("msg.w.todo_not_found", null, locale);
				mv.addObject("msg", new OpMsg("W", msg));
			}
			
			session.setAttribute("todoQuery", todoQuery);
			mv.addObject("todoPage", todoPage);
			mv.addObject("todoList", todoPage.getContent());
		}else {
			mv.addObject("todoPage", null);
			mv.addObject("todoList", null);
			
			//検索条件エラーあり
			String msg = messageSource.getMessage("msg.e.input_something_wrong", null, locale);
			mv.addObject("msg", new OpMsg("E", msg));
		}
		
		return mv;
	}
	
    // ページリンク押下時
	@GetMapping("/todo/query")
	public ModelAndView queryTodo(@PageableDefault(page=0,size=5) Pageable pageable ,ModelAndView mv) {
		
        // 現在のページ位置を保存
        session.setAttribute("prevPageable", pageable);

		mv.setViewName("todoList");
		
		TodoQuery todoQuery = (TodoQuery)session.getAttribute("todoQuery");
		Page<Todo> todoPage = todoDaoImpl.findByCriteria(todoQuery, pageable);
		
		mv.addObject("todoQuery", todoQuery);// 検索条件表示用
		mv.addObject("todoPage", todoPage);// page情報
		mv.addObject("todoList", todoPage.getContent());// 検索結果
		
		return mv;
	}

	
	@PostMapping("/todo/create/form")
	public ModelAndView createTodo(ModelAndView mv){
		//Todo一覧画面で「新規追加」リンクをクリックされたとき
		mv.setViewName("todoForm");
		mv.addObject("todoData", new TodoData());
		session.setAttribute("mode","create");
		return  mv;
	}
	
	@PostMapping("/todo/create/do")
	public String createTodo(@ModelAttribute @Validated TodoData todoData , 
								BindingResult result, 
								Model model, 
								RedirectAttributes redirectAttributes,
								Locale locale){
		
		//Todo登録画面で「登録」リンクをクリックされたとき
		boolean isValid = todoService.isValid(todoData, result , session.getAttribute("mode").toString(),locale);
		if (!result.hasErrors() && isValid) {
			Todo todo = todoData.toEntity();
			todoRepository.saveAndFlush(todo);
			
			//登録完了メッセージをセットしてリダイレクト
			String msg = messageSource.getMessage("msg.i.todo_created", null, locale);
			redirectAttributes.addFlashAttribute("msg", new OpMsg("I", msg));

			return "redirect:/todo";
		}else {
			String msg = messageSource.getMessage("msg.e.input_something_wrong", null, locale);
			model.addAttribute("msg", new OpMsg("E",msg));
			return "todoForm";
		}
	}
	
	@PostMapping("/todo/cancel")
	public String cancel(){
		//Todo登録画面で「キャンセル登録」リンクをクリックされたとき
		return  "redirect:/todo";
	}
	
	@GetMapping("/todo/{id}")
	public ModelAndView todoById(@PathVariable(name="id") int id , ModelAndView mv) {
		mv.setViewName("todoForm");
		Todo todo = todoRepository.findById(id).get();
		mv.addObject("todoData", todo);
		session.setAttribute("mode","update");
		return mv;
	}
	
	@PostMapping("/todo/update")
	public String updateTodo(@ModelAttribute @Validated TodoData todoData, 
								BindingResult result, 
								Model model, 
								RedirectAttributes redirectAttributes,
								Locale locale){
		
		//Todo更新画面で「更新」リンクをクリックされたとき
		boolean isValid = todoService.isValid(todoData, result, session.getAttribute("mode").toString(),locale);
		if (!result.hasErrors() && isValid) {
			Todo todo = todoData.toEntity();
			todoRepository.saveAndFlush(todo);
			
			//更新完了メッセージをセットしてリダイレクト
			String msg = messageSource.getMessage("msg.i.todo_updated", null, locale);
			redirectAttributes.addFlashAttribute("msg", new OpMsg("I", msg));
			
			return "redirect:/todo";
		}else {
			
			//検索条件エラーあり
			String msg = messageSource.getMessage("msg.e.input_something_wrong", null, locale);
			model.addAttribute("msg", new OpMsg("E", msg));
			
			return "todoForm";
		}
	}
	
	@PostMapping("/todo/delete")
	public String deleteTodo(@ModelAttribute @Validated TodoData todoData, 
								RedirectAttributes redirectAttributes, 
								Locale locale){
		//Todo更新画面で「削除」リンクをクリックされたとき
		todoRepository.deleteById(todoData.getId());
		
		//登録完了メッセージをセットしてリダイレクト
		String msg = messageSource.getMessage("msg.i.todo_deleted", null, locale);
		redirectAttributes.addFlashAttribute("msg", new OpMsg("I", msg));
		
		return "redirect:/todo";
	}
	
	
}