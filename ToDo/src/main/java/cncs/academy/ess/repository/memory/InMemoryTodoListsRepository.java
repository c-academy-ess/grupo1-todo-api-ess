package cncs.academy.ess.repository.memory;

import cncs.academy.ess.model.TodoList;
import cncs.academy.ess.repository.TodoListsRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryTodoListsRepository implements TodoListsRepository {
    private final ConcurrentHashMap<Integer, TodoList> allLists = new ConcurrentHashMap<>();
    private final AtomicInteger currentId = new AtomicInteger(0);

    @Override
    public TodoList findById(int listId) {
        return allLists.get(listId);
    }

    @Override
    public List<TodoList> findAll() {
        return new ArrayList<>(allLists.values());
    }

    @Override
    public List<TodoList> findAllByUserId(int userId) {
        return allLists.values().stream()
                .filter(todoList -> todoList.getOwnerId() == userId)
                .toList();
    }

    @Override
    public int save(TodoList todoList) {
        int id = todoList.getListId();
        if (id == 0) {
            todoList.setId(id=currentId.incrementAndGet());
        }
        allLists.put(id, todoList);
        return id;
    }

    @Override
    public void update(TodoList todoList) {
        allLists.put(todoList.getListId(), todoList);
    }

    @Override
    public boolean deleteById(int listId) {
        return allLists.remove(listId) != null;
    }
}