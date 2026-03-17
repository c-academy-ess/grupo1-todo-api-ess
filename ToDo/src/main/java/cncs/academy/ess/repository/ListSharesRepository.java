package cncs.academy.ess.repository;

public interface ListSharesRepository {
    void save(int listId, int userId);
    boolean isShared(int listId, int userId);
}
