package cncs.academy.ess.repository.sql;

import cncs.academy.ess.repository.ListSharesRepository;

import java.sql.*;

import org.apache.commons.dbcp2.BasicDataSource;

public class SQLListSharesRepository implements ListSharesRepository {
    private final BasicDataSource dataSource;

    public SQLListSharesRepository(BasicDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(int listId, int userId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                     "INSERT INTO list_shares (list_id, user_id) VALUES (?, ?)")) {
            stmt.setInt(1, listId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save list share", e);
        }
    }

    @Override
    public boolean isShared(int listId, int userId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                     "SELECT 1 FROM list_shares WHERE list_id = ? AND user_id = ?")) {
            stmt.setInt(1, listId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check list share", e);
        }
    }
}
