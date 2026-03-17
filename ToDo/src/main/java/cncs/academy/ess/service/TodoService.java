package cncs.academy.ess.service;

import cncs.academy.ess.model.Todo;
import cncs.academy.ess.repository.TodoListsRepository;
import cncs.academy.ess.repository.TodoRepository;

import java.util.List;

public class TodoService {
    private TodoRepository repoTodoRepository;
    private TodoListsRepository repoTodoListRepository;

    public TodoService(
            TodoRepository todoRepository,
            TodoListsRepository repoTodoListRepository)
    {
        this.repoTodoRepository = todoRepository;
        this.repoTodoListRepository = repoTodoListRepository;
    }

    public Todo createTodoItem(String description, int listId) {
        if (repoTodoListRepository.findById(listId) == null) {
            throw new IllegalArgumentException("List not found");
        }
        Todo todo = new Todo(description, listId);
        int id = repoTodoRepository.save(todo);
        todo.setId(id);
        return todo;
    }

    public Todo getTodoItem(int todoId) {
        return repoTodoRepository.findById(todoId);
    }

    public List<Todo> getAllTodoItemsByListId(int listId) {
        return repoTodoRepository.findAllByListId(listId);
    }

    public boolean deleteTodoItem(int todoId) {
        return repoTodoRepository.deleteById(todoId);
    }
}
