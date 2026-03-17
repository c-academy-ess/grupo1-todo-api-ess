package cncs.academy.ess.controller;

import cncs.academy.ess.controller.messages.ErrorMessage;
import cncs.academy.ess.controller.messages.TodoAddRequest;
import cncs.academy.ess.model.Todo;
import cncs.academy.ess.model.TodoList;
import cncs.academy.ess.service.TodoListsService;
import cncs.academy.ess.service.TodoService;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TodoController {
    private static final Logger log = LoggerFactory.getLogger(TodoController.class);

    private final TodoListsService todoListService;
    private final TodoService todoService;

    public TodoController(
            TodoService todoServive,
            TodoListsService toDoListService)
    {
        this.todoService = todoServive;
        this.todoListService = toDoListService;
    }

    public void createTodoItem(Context ctx) {
        TodoAddRequest request = ctx.bodyAsClass(TodoAddRequest.class);
        if (!checkOwnershipOfList(ctx, request.listId)) {
            return;
        }
        Todo todo = todoService.createTodoItem(request.description, request.listId);
        ctx.status(200).json(todo);
    }

    public void getTodoItem(Context ctx) {
        int todoId = Integer.parseInt(ctx.pathParam("taskId"));
        int listId = Integer.parseInt(ctx.pathParam("listId"));
        if (!checkAccessToList(ctx, listId)) {
            return;
        }
        Todo todo = todoService.getTodoItem(todoId);
        if (todo != null) {
            ctx.status(200).json(todo);
        } else {
            ctx.status(404).result("Todo not found");
        }
    }

    public void getAllTodoItems(Context ctx) {
        int listId = Integer.parseInt(ctx.pathParam("listId"));
        log.info("Getting all todo items for list {}", listId);
        if (!checkAccessToList(ctx, listId)) {
            return;
        }
        ctx.status(200).json(todoService.getAllTodoItemsByListId(listId));
    }

    private boolean checkOwnershipOfList(Context ctx, int listId) {
        int userId = ctx.attribute("userId");
        log.info("Checking ownership of list {} by user {}", listId, userId);
        TodoList list = todoListService.getTodoList(listId);
        if (list == null || list.getOwnerId() != userId) {
            log.error("User not owner of list");
            ctx.status(403).json(new ErrorMessage("User not owner of list"));
            return false;
        }
        return true;
    }

    private boolean checkAccessToList(Context ctx, int listId) {
        int userId = ctx.attribute("userId");
        log.info("Checking access to list {} by user {}", listId, userId);
        TodoList list = todoListService.getTodoList(listId);
        if (list == null) {
            ctx.status(403).json(new ErrorMessage("User not owner of list"));
            return false;
        }
        if (list.getOwnerId() == userId) {
            return true;
        }
        if (todoListService.isShared(listId, userId)) {
            return true;
        }
        ctx.status(403).json(new ErrorMessage("User not owner of list"));
        return false;
    }

    public void deleteTodoItem(Context context) {
        int todoId = Integer.parseInt(context.pathParam("taskId"));
        int listId = Integer.parseInt(context.pathParam("listId"));
        if (!checkOwnershipOfList(context, listId)) {
            return;
        }
        if (todoService.deleteTodoItem(todoId)) {
            context.status(203);
        } else {
            context.status(404).json(new ErrorMessage("Todo not found"));
        }
    }
}
