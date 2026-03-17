package cncs.academy.ess.repository.sql;

import cncs.academy.ess.model.Todo;
import cncs.academy.ess.repository.TodoRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

public class SQLTodoRepository implements TodoRepository {
    private final BasicDataSource dataSource;

    public SQLTodoRepository(BasicDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Todo findById(int todoId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement("SELECT * FROM todos WHERE id = ?")) {
            stmt.setInt(1, todoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTodo(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find todo by ID", e);
        }
        return null;
    }

    @Override
    public List<Todo> findAll() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement("SELECT * FROM todos");
             ResultSet rs = stmt.executeQuery()) {
            List<Todo> todos = new ArrayList<>();
            while (rs.next()) {
                todos.add(mapResultSetToTodo(rs));
            }
            return todos;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all todos", e);
        }
    }

    @Override
    public List<Todo> findAllByListId(int listId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement("SELECT * FROM todos WHERE list_id = ?")) {
            stmt.setInt(1, listId);
            try (ResultSet rs = stmt.executeQuery()) {
                List<Todo> todos = new ArrayList<>();
                while (rs.next()) {
                    todos.add(mapResultSetToTodo(rs));
                }
                return todos;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find todos by list ID", e);
        }
    }

    @Override
    public int save(Todo todo) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                     "INSERT INTO todos (description, completed, list_id) VALUES (?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, todo.getDescription());
            stmt.setBoolean(2, todo.isCompleted());
            stmt.setInt(3, todo.getListId());
            stmt.executeUpdate();
            int generatedId = 0;
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    generatedId = rs.getInt(1);
                }
            }
            return generatedId;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save todo", e);
        }
    }

    @Override
    public void update(Todo todo) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                     "UPDATE todos SET description = ?, completed = ?, list_id = ? WHERE id = ?")) {
            stmt.setString(1, todo.getDescription());
            stmt.setBoolean(2, todo.isCompleted());
            stmt.setInt(3, todo.getListId());
            stmt.setInt(4, todo.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update todo", e);
        }
    }

    @Override
    public boolean deleteById(int todoId) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement("DELETE FROM todos WHERE id = ?")) {
            stmt.setInt(1, todoId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete todo", e);
        }
    }

    private Todo mapResultSetToTodo(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String description = rs.getString("description");
        boolean completed = rs.getBoolean("completed");
        int listId = rs.getInt("list_id");
        return new Todo(id, description, completed, listId);
    }
}
