package cncs.academy.ess.repository.memory;

import cncs.academy.ess.repository.ListSharesRepository;

import java.util.HashSet;
import java.util.Set;

public class InMemoryListSharesRepository implements ListSharesRepository {
    private final Set<String> shares = new HashSet<>();

    private String key(int listId, int userId) {
        return listId + ":" + userId;
    }

    @Override
    public void save(int listId, int userId) {
        shares.add(key(listId, userId));
    }

    @Override
    public boolean isShared(int listId, int userId) {
        return shares.contains(key(listId, userId));
    }
}
