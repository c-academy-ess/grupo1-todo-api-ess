package cncs.academy.ess.repository.memory;

import cncs.academy.ess.model.User;
import cncs.academy.ess.repository.UserRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryUserRepository implements UserRepository {
    private final Map<Integer, User> users = new HashMap<>();
    private final AtomicInteger nextId = new AtomicInteger(0);

    @Override
    public User findById(int userId) {
        return users.get(userId);
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public int save(User user) {
        int id = user.getId();
        if (id == 0) {
            id = nextId.incrementAndGet();
        }
        users.put(id, user);
        return id;
    }

    @Override
    public void deleteById(int userId) {
        users.remove(userId);
    }

    @Override
    public User findByUsername(String username) {
        return users.values().stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }
}
