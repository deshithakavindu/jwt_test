package org.example;

import io.javalin.Javalin;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {
    public static void main(String[] args) {
        AuthService authService = new AuthService();

        Javalin app = Javalin.create().start(7000);
        ObjectMapper mapper = new ObjectMapper();

        app.post("/register", ctx -> {
            User user = mapper.readValue(ctx.body(), User.class);

            // Call your register method to save in database
            authService.register(user.getUsername(), user.getPassword());

            ctx.status(201).result("User registered: " + user.getUsername());
        });
        app.post("/login", ctx -> {
            String username = ctx.formParam("username");
            String password = ctx.formParam("password");
            String token = authService.login(username, password);
            if (token != null) {
                ctx.json(token);
            } else {
                ctx.status(401).result("Invalid credentials");
            }
        });

        // Protected endpoint
        app.get("/profile", ctx -> {
            String authHeader = ctx.header("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String username = JwtUtil.validateTokenAndGetUsername(token);
                if (username != null) {
                    ctx.json("Hello " + username + ", welcome to your profile!");
                    return;
                }
            }
            ctx.status(401).result("Unauthorized");
        });
    }
}
