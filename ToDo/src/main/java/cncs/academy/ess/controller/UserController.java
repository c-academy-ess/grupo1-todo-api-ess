package cncs.academy.ess.controller;

import cncs.academy.ess.controller.messages.ErrorMessage;
import cncs.academy.ess.controller.messages.UserAddRequest;
import cncs.academy.ess.controller.messages.UserLoginRequest;
import cncs.academy.ess.controller.messages.UserResponse;
import cncs.academy.ess.model.User;
import cncs.academy.ess.service.TodoUserService;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;

public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final TodoUserService userService;

    public UserController(TodoUserService userService) {
        this.userService = userService;
    }

    public void createUser(Context ctx) throws NoSuchAlgorithmException {
        UserAddRequest userRequest = ctx.bodyAsClass(UserAddRequest.class);
        log.info("Create user: {}", userRequest.username);
        User user = userService.addUser(userRequest.username, userRequest.password);
        UserResponse response = new UserResponse(user.getId(), user.getUsername());
        ctx.status(201).json(response);
    }

    public void getUser(Context ctx) {
        int userId = Integer.parseInt(ctx.pathParam("userId"));
        User user = userService.getUser(userId);
        if (user != null) {
            UserResponse response = new UserResponse(user.getId(), user.getUsername());
            ctx.status(200).json(response);
        } else {
            ctx.status(404).json(new ErrorMessage("User not found"));
        }
    }

    public void deleteUser(Context ctx) {
        int userId = Integer.parseInt(ctx.pathParam("userId"));
        userService.deleteUser(userId);
        ctx.status(204);
    }

    public void loginUser(Context ctx) throws NoSuchAlgorithmException {
        UserLoginRequest userRequest = ctx.bodyAsClass(UserLoginRequest.class);
        log.info("Login user: {}", userRequest.username);
        String token = userService.login(userRequest.username, userRequest.password);
        if (token != null) {
            ctx.status(200).json(token);
        } else {
            ctx.status(401).json(new ErrorMessage("Invalid username or password"));
        }
    }
}

