package cncs.academy.ess.controller.messages;

public class TodoListGetResponse {
    public int listId;
    public String name;
    public int userId;
    public TodoListGetResponse(int listId, String name, int userId) {
        this.listId = listId;
        this.name = name;
        this.userId = userId;
    }
}
