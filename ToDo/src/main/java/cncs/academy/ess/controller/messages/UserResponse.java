package cncs.academy.ess.controller.messages;

public class UserResponse {
    public int userId;
    public String username;
    public UserResponse(int userId, String username) {
        this.userId = userId;
        this.username = username;
    }
}
