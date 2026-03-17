package cncs.academy.ess.model;

public class TodoList {
    /** The id of the todo list */
    private int id;
    /** The name of the todo list */
    private String name;
    /** The id of the user that the todo list belongs to */
    private int ownerId;
    // constructor, getters, setters
    public TodoList(int id, String name, int ownerId) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
    }
    public TodoList(String name, int ownerId) {
        this.name = name;
        this.ownerId = ownerId;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getListId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getOwnerId() {
        return ownerId;
    }
}


