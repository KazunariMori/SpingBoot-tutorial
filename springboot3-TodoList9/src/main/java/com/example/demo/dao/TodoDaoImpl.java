package com.example.demo.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.example.demo.common.Utils;
import com.example.demo.entity.Todo;
import com.example.demo.form.TodoQuery;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TodoDaoImpl implements TodoDao {
	
	private final EntityManager entityManager;

	@Override
	public List<Todo> findByJPQL(TodoQuery todoQuery , Pageable pageable) {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder("Select t from Todo t where 1 = 1");
		List<Object> params = new ArrayList<>();
		int pos = 0;
		
		//実行するSQLの組み立て
		//件名
		if (todoQuery.getTitle().length()>0) {
			sb.append(" and t.title like ?" + (++pos));
			params.add("%" + todoQuery.getTitle() + "%");
		}
		
		//重要度
		if (todoQuery.getImportance() != -1) {
			sb.append(" and t.importance = ?" + (++pos));
			params.add(todoQuery.getImportance());
		}
		
		//緊急度
		if (todoQuery.getUrgency() != -1) {
			sb.append(" and t.urgency = ?" + (++pos));
			params.add(todoQuery.getUrgency());
		}
		
		//期限：開始	
		if (!todoQuery.getDeadLineFrom().equals("")) {
			sb.append(" and t.deadline >= ?" + (++pos));
			params.add(Utils.str2date(todoQuery.getDeadLineFrom()));
		}
		
		//期限：終了	
		if (!todoQuery.getDeadLineTo().equals("")) {
			sb.append(" and t.deadline <= ?" + (++pos));
			params.add(Utils.str2date(todoQuery.getDeadLineTo()));
		}
		
		//完成
		if (todoQuery.getDone() != null && !todoQuery.getDone().equals("Y")) {
			sb.append(" and t.done = ?" + (++pos));
			params.add(todoQuery.getDone());
		}
		
		//order
		sb.append("order by id");
		
		Query query = entityManager.createQuery(sb.toString());
		for (int i=0 ; i<params.size(); ++i) {
			query = query.setParameter(i+1, params.get(i));
		}
				
		@SuppressWarnings("unchecked")
		List<Todo> list = query.getResultList();
		
		return list;
		
	}

	@Override
	public Page<Todo> findByCriteria(TodoQuery todoQuery , Pageable pageable) {
		// TODO Auto-generated method stub
		
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Todo> query = builder.createQuery(Todo.class);
		Root<Todo> root = query.from(Todo.class);
		List<Predicate> predicates = new ArrayList<>();
		
		//全件検索
		//List<Todo> todoList = entityManager.createQuery(query.select(root)).getResultList();
		
		//例えば：重要度＝１の検索
//		query = query.select(root).where(builder.equal(root.get("importance"),1));
//		List<Todo> todoList = entityManager.createQuery(query).getResultList();
		
		//複数条件の場合、例えば：重要度=1 and 緊急度=1
//		query.select(root).where(
//				builder.equal(root.get("importance"),1),
//				builder.and(builder.equal(root.get("urgency"),1))
//				);
//		List<Todo> todoList = entityManager.createQuery(query).getResultList();
		
		//複数条件の場合、例えば：重要度=1 and 緊急度=1 and 期限<=2020-01-01
//		query.select(root).where(
//				builder.equal(root.get("importance"),1),
//				builder.and(builder.equal(root.get("urgency"),1)),
//				builder.and(builder.lessThanOrEqualTo(root.get("deadLine"),Utils.str2date("2020-01-01")))
//				);
//		List<Todo> todoList = entityManager.createQuery(query).getResultList();
		
//		//動的生成
//		List<Predicate> predicates = new ArrayList<>();
//		//検索条件を追加
//		predicates.add(builder.equal(root.get("importance"),1));
//		predicates.add(builder.and(builder.equal(root.get("urgency"),1)));
//		predicates.add(builder.and(builder.lessThanOrEqualTo(root.get("deadLine"),Utils.str2date("2020-01-01"))));
//		
//		//List配列に変換
//		Predicate[] predArray = new Predicate[predicates.size()];
//		predicates.toArray(predArray);
//		
//		//配列を可変長引数としてWhereに設定
//		query = query.select(root).where(predArray);
		//検索結果の並べ替え
//		query = query.select(root).where(predArray).orderBy(builder.asc(root.get("id")));
//		List<Todo> todoList = entityManager.createQuery(query).getResultList();
		
		//件名
		String title="";
		if (todoQuery.getTitle().length() > 0) {
			title = "%" + todoQuery.getTitle() + "%";
		}else {
			title = "%";
		}
		predicates.add(builder.like(root.get("title") , title));
		
		//重要度
		if (todoQuery.getImportance() != -1) {
			predicates.add(builder.and(builder.equal(root.get("importance") , todoQuery.getImportance())));
		}

		//緊急度
		if (todoQuery.getUrgency() != -1) {
			predicates.add(builder.and(builder.equal(root.get("urgency") , todoQuery.getUrgency())));
		}

		//期限：開始
		if (!todoQuery.getDeadLineFrom().equals("")) {
			predicates.add(builder.and(builder.greaterThanOrEqualTo(root.get("deadline") , Utils.str2date(todoQuery.getDeadLineFrom()))));
		}
		
		//期限：終了
		if (!todoQuery.getDeadLineTo().equals("")) {
			predicates.add(builder.and(builder.lessThanOrEqualTo(root.get("deadline") , Utils.str2date(todoQuery.getDeadLineTo()))));
		}
		
		//完成
		if (todoQuery.getDone() != null && todoQuery.getDone().equals("Y")) {
			predicates.add(builder.and(builder.equal(root.get("done") , todoQuery.getDone())));
		}
		
		//SELECT作成
		Predicate[] predArray = new Predicate[predicates.size()];
		predicates.toArray(predArray);
		query = query.select(root).where(predArray).orderBy(builder.asc(root.get("id")));
		
		//クエリ生成
		TypedQuery<Todo> typedQuery = entityManager.createQuery(query);
		//レコード数を取得
		int totalRows = typedQuery.getResultList().size();
		//先頭レコードの位置設定
		typedQuery.setFirstResult(pageable.getPageNumber()*pageable.getPageSize());
		//１ページあたりページ数
		typedQuery.setMaxResults(pageable.getPageSize());
		
		Page<Todo> pageTodo = new PageImpl<Todo>(typedQuery.getResultList() , pageable , totalRows);
		
//		//検索実施
//		List<Todo> todoList = entityManager.createQuery(query).getResultList();
		

		return pageTodo;
	}

}
