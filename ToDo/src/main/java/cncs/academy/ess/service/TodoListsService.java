package cncs.academy.ess.service;

import cncs.academy.ess.model.TodoList;
import cncs.academy.ess.repository.ListSharesRepository;
import cncs.academy.ess.repository.TodoListsRepository;

import java.util.Collection;

public class TodoListsService {
    TodoListsRepository todoListsRepository;
    ListSharesRepository listSharesRepository;

    public TodoListsService(TodoListsRepository todoListsRepository, ListSharesRepository listSharesRepository) {
        this.todoListsRepository = todoListsRepository;
        this.listSharesRepository = listSharesRepository;
    }

    public TodoList createTodoListItem(String listName, int ownerId) {
        TodoList list = new TodoList(listName, ownerId);
        int listId = todoListsRepository.save(list);
        list.setId(listId);
        return list;
    }
    public TodoList getTodoList(int listId) {
        return todoListsRepository.findById(listId);
    }
    public Collection<TodoList> getAllTodoLists(int userId) {
        return todoListsRepository.findAllByUserId(userId);
    }

    public void shareList(int listId, int userId) {
        listSharesRepository.save(listId, userId);
    }

    public boolean isShared(int listId, int userId) {
        return listSharesRepository.isShared(listId, userId);
    }
}
