package cncs.academy.ess.controller.messages;

public class TodoListAddResponse {
    public String name;
    public int userId;
    public int listId;
    public TodoListAddResponse(int listId, String name, int userId) {
        this.listId = listId;
        this.name = name;
        this.userId = userId;
    }
}
