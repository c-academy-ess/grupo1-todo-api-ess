package cncs.academy.ess.repository;

import cncs.academy.ess.model.Todo;

import java.util.List;

public interface TodoRepository {
    Todo findById(int todoId);
    List<Todo> findAll();
    List<Todo> findAllByListId(int listId);
    int save(Todo todo);
    void update(Todo todo);
    boolean deleteById(int todoId);
}
