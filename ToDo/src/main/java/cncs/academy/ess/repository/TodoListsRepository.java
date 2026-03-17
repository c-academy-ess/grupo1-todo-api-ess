package cncs.academy.ess.repository;
import cncs.academy.ess.model.TodoList;

import java.util.List;

public interface TodoListsRepository {
    TodoList findById(int listId);
    List<TodoList> findAll();
    List<TodoList> findAllByUserId(int userId);
    int save(TodoList todoList);
    void update(TodoList todoList);
    boolean deleteById(int listId);
}
