package cncs.academy.ess;

import cncs.academy.ess.controller.AuthorizationMiddleware;
import cncs.academy.ess.controller.TodoController;
import cncs.academy.ess.controller.TodoListController;
import cncs.academy.ess.controller.UserController;
import cncs.academy.ess.repository.ListSharesRepository;
import cncs.academy.ess.repository.TodoListsRepository;
import cncs.academy.ess.repository.TodoRepository;
import cncs.academy.ess.repository.UserRepository;
import cncs.academy.ess.repository.memory.InMemoryListSharesRepository;
import cncs.academy.ess.repository.memory.InMemoryTodoListsRepository;
import cncs.academy.ess.repository.memory.InMemoryTodoRepository;
import cncs.academy.ess.repository.memory.InMemoryUserRepository;
import cncs.academy.ess.repository.sql.SQLListSharesRepository;
import cncs.academy.ess.repository.sql.SQLTodoRepository;
import cncs.academy.ess.repository.sql.SQLTodoListsRepository;
import cncs.academy.ess.repository.sql.SQLUserRepository;
import cncs.academy.ess.service.TodoListsService;
import cncs.academy.ess.service.TodoUserService;
import cncs.academy.ess.service.TodoService;
import io.javalin.Javalin;
import io.javalin.community.ssl.SslPlugin;
import org.apache.commons.dbcp2.BasicDataSource;

import org.casbin.jcasbin.main.Enforcer;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class App {
    public static void main(String[] args) throws Exception {
        // Generate HMAC secret key for JWT signing/verification
        byte[] hmacSecret = new byte[32]; // 256 bits
        new SecureRandom().nextBytes(hmacSecret);
        // Configuração HTTPS com certificado auto-assinado
        SslPlugin sslPlugin = new SslPlugin(conf -> {
            conf.keystoreFromClasspath("keystore.jks", "changeit");
            conf.insecurePort = 7100;      // HTTP port (opcional, para redirecionamento)
            conf.securePort = 7443;        // HTTPS port
            conf.sniHostCheck = false;     // Desativar verificação SNI para desenvolvimento
        });

        Javalin app = Javalin.create(config -> {
            config.registerPlugin(sslPlugin);
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> {
                    it.anyHost();
                });
            });
        }).start();

        // Initialize repositories based on REPO_MODE environment variable
        //REPO_MODE=memory -> InMemory repositories (no database needed)
        // REPO_MODE=sql (or unset) -> SQL repositories (PostgreSQL)
        //String repoMode = "sql";
        String repoMode = "memory";

        UserRepository userRepository;
        TodoListsRepository listsRepository;
        ListSharesRepository listSharesRepository;
        TodoRepository todoRepository;

        if ("memory".equalsIgnoreCase(repoMode)) {
            System.out.println(">>> Using MEMORY repositories");
            userRepository = new InMemoryUserRepository();
            listsRepository = new InMemoryTodoListsRepository();
            listSharesRepository = new InMemoryListSharesRepository();
            todoRepository = new InMemoryTodoRepository();
        } else {
            System.out.println(">>> Using SQL repositories (PostgreSQL)");
            BasicDataSource ds = new BasicDataSource();
            ds.setDriverClassName("org.postgresql.Driver");
            String connectURI = String.format("jdbc:postgresql://%s:%s/%s?user=%s&password=%s", "10.58.129.152", "5433", "Todo", "postgres", "qwer_123");
            ds.setUrl(connectURI);

            userRepository = new SQLUserRepository(ds);
            listsRepository = new SQLTodoListsRepository(ds);
            listSharesRepository = new SQLListSharesRepository(ds);
            todoRepository = new SQLTodoRepository(ds);
        }

        TodoUserService userService = new TodoUserService(userRepository, hmacSecret);
        UserController userController = new UserController(userService);

        // Pre-create admin user in memory mode
        if ("memory".equalsIgnoreCase(repoMode)) {
            userService.addUser("admin", "penca");
            System.out.println(">>> Admin user created (admin/penca)");
        }

        TodoListsService toDoListService = new TodoListsService(listsRepository, listSharesRepository);
        TodoListController todoListController = new TodoListController(toDoListService);

        TodoService todoService = new TodoService(todoRepository, listsRepository);
        TodoController todoController = new TodoController(todoService, toDoListService);

        // Initialize Casbin RBAC enforcer
        // Copy classpath resources to temp files (required when running from a fat JAR)
        Path modelTemp = Files.createTempFile("model", ".conf");
        Path policyTemp = Files.createTempFile("policy", ".csv");
        modelTemp.toFile().deleteOnExit();
        policyTemp.toFile().deleteOnExit();
        try (InputStream ms = App.class.getClassLoader().getResourceAsStream("model.conf");
             InputStream ps = App.class.getClassLoader().getResourceAsStream("policy.csv")) {
            Files.copy(ms, modelTemp, StandardCopyOption.REPLACE_EXISTING);
            Files.copy(ps, policyTemp, StandardCopyOption.REPLACE_EXISTING);
        }
        Enforcer enforcer = new Enforcer(modelTemp.toString(), policyTemp.toString());

        AuthorizationMiddleware authMiddleware = new AuthorizationMiddleware(hmacSecret, enforcer);

        // CORS
        app.before(ctx -> {
            ctx.header("Access-Control-Allow-Origin", "*");
            ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            ctx.header("Access-Control-Allow-Headers", "*");
        });
        // Authorization middleware
        app.before(authMiddleware::handle);

        // User management
        app.post("/user", userController::createUser);
        app.get("/user/{userId}", userController::getUser);
        app.delete("/user/{userId}", userController::deleteUser);
        app.post("/login", userController::loginUser);

        // "To do" lists management
        /* POST /todolist
          {
              "listName": "Shopping list"
          }
         */
        app.post("/todolist", todoListController::createTodoList);
        app.get("/todolist", todoListController::getAllTodoLists);
        app.get("/todolist/{listId}", todoListController::getTodoList);
        app.post("/todolist/{listId}/share", todoListController::shareTodoList);

        // "To do" list items management
        /* POST /todo/item
          {
              "description": "Buy milk",
              "listId": 1
          }
         */
        app.post("/todo/item", todoController::createTodoItem);
        /* GET /todo/1/tasks */
        app.get("/todo/{listId}/tasks", todoController::getAllTodoItems);
        /* GET /todo/1/tasks/1 */
        app.get("/todo/{listId}/tasks/{taskId}", todoController::getTodoItem);
        /* DELETE /todo/1/tasks/1 */
        app.delete("/todo/{listId}/tasks/{taskId}", todoController::deleteTodoItem);

        //fillDummyData(userService, toDoListService, todoService);
    }

    private static void fillDummyData(
            TodoUserService userService,
            TodoListsService toDoListService,
            TodoService todoService) throws NoSuchAlgorithmException {
        userService.addUser("user1", "password1");
        userService.addUser("user2", "password2");
        toDoListService.createTodoListItem("Shopping list", 1);
        toDoListService.createTodoListItem( "Other", 1);
        todoService.createTodoItem("Bread", 1);
        todoService.createTodoItem("Milk", 1);
        todoService.createTodoItem("Eggs", 1);
        todoService.createTodoItem("Cheese", 1);
        todoService.createTodoItem("Butter", 1);
    }
}
