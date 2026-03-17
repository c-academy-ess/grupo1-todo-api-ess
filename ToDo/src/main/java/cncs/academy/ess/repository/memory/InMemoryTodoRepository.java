package cncs.academy.ess.repository.memory;

import cncs.academy.ess.model.Todo;
import cncs.academy.ess.repository.TodoRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryTodoRepository implements TodoRepository {
    private Map<Integer, Todo> list = new HashMap<>();
    private final AtomicInteger currentId = new AtomicInteger(0);

    @Override
    public Todo findById(int todoId) {
        return list.get(todoId);
    }

    @Override
    public List<Todo> findAll() {
        return list.values().stream().toList();
    }

    @Override
    public List<Todo> findAllByListId(int listId) {
        return list.values().stream().filter(todo -> todo.getListId() == listId).toList();
    }

    public int save(Todo todo) {
        int id = todo.getId();
        if (id == 0) {
            todo.setId(id = currentId.incrementAndGet());
        }
        list.put(id, todo);
        return id;
    }

    @Override
    public void update(Todo todo) {
        list.put(todo.getId(), todo);
    }

    @Override
    public boolean deleteById(int todoId) {
        return list.remove(todoId) != null;
    }

    public List<Todo> findAll(int listId) {
        return list.values().stream().filter(todo -> todo.getListId() == listId).toList();
    }

}
