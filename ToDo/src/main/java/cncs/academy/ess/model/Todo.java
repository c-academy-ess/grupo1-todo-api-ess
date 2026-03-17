package cncs.academy.ess.model;

public class Todo {
    /** The id of the todo item */
    private int id;
    /** The description of the todo item */
    private String description;
    /** The completion status of the todo item */
    private boolean completed;
    /** The id of the list that the todo item belongs to */
    private int listId;

    public Todo(int id, String description, boolean completed, int listId) {
        this.id = id;
        this.description = description;
        this.completed = completed;
        this.listId = listId;
    }
    public Todo(String description, int listId) {
        this.description = description;
        this.completed = false;
        this.listId = listId;
    }
    public void setIsCompleted(boolean completed) {
        this.completed = completed;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public int getListId() {
        return listId;
    }
}
