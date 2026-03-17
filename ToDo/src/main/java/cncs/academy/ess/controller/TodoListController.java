package cncs.academy.ess.controller;

import cncs.academy.ess.controller.messages.ErrorMessage;
import cncs.academy.ess.controller.messages.ShareListRequest;
import cncs.academy.ess.controller.messages.TodoListAddRequest;
import cncs.academy.ess.controller.messages.TodoListAddResponse;
import cncs.academy.ess.controller.messages.TodoListGetResponse;
import cncs.academy.ess.model.TodoList;
import cncs.academy.ess.service.TodoListsService;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TodoListController {
    private static final Logger logger = LoggerFactory.getLogger(TodoListController.class);
    private final TodoListsService todoListService;

    public TodoListController(TodoListsService todoListService) {
        this.todoListService = todoListService;
    }

    public void createTodoList(Context ctx) {
        logger.info("Create todo list item");
        TodoListAddRequest request = ctx.bodyAsClass(TodoListAddRequest.class);
        TodoList list = todoListService.createTodoListItem(request.listName, ctx.attribute("userId"));
        TodoListAddResponse response = new TodoListAddResponse(
            list.getListId(),
            list.getName(),
            list.getOwnerId()
        );
        ctx.status(201).json(response);
    }

    public void getTodoList(Context ctx) {
        logger.info("Get todo list");
        int listId = Integer.parseInt(ctx.pathParam("listId"));
        int userId = ctx.attribute("userId");
        TodoList list = todoListService.getTodoList(listId);
        if (list == null) {
            ctx.status(404).json(new ErrorMessage("List not found"));
            return;
        }
        if (list.getOwnerId() != userId) {
            ctx.status(403).json(new ErrorMessage("Owner of list does not match user"));
            return;
        }
        TodoListGetResponse response = new TodoListGetResponse(
            list.getListId(),
            list.getName(),
            list.getOwnerId()
        );
        ctx.status(200).json(response);
    }

    public void getAllTodoLists(Context ctx) {
        logger.info("Get all todo lists");
        int userId = ctx.attribute("userId");
        ctx.status(200).json(todoListService.getAllTodoLists(userId));
    }

    public void shareTodoList(Context ctx) {
        logger.info("Share todo list");
        int listId = Integer.parseInt(ctx.pathParam("listId"));
        int userId = ctx.attribute("userId");
        ShareListRequest request = ctx.bodyAsClass(ShareListRequest.class);
        TodoList list = todoListService.getTodoList(listId);
        if (list == null) {
            ctx.status(404).json(new ErrorMessage("List not found"));
            return;
        }
        if (list.getOwnerId() != userId) {
            ctx.status(403).json(new ErrorMessage("Only the owner can share this list"));
            return;
        }
        todoListService.shareList(listId, request.userId);
        ctx.status(201).result("List shared successfully");
    }
}
