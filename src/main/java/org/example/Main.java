package org.example;

import io.javalin.Javalin;
import io.javalin.http.Context;

public class Main {
    public static void main(String[] args) {
        AuthService authService = new AuthService();

        Javalin app = Javalin.create(config -> {
            config.plugins.enableCors(cors -> {
                cors.add(it -> {
                    it.anyHost();
                });
            });
        }).start(7000);

        // Registration endpoint
        app.post("/register", ctx -> {
            String username = ctx.formParam("username");
            String password = ctx.formParam("password");
            String telephone = ctx.formParam("telephone");
            String address = ctx.formParam("address");

            if (username == null || password == null) {
                ctx.status(400).result("Username and password are required");
                return;
            }

            authService.register(username, password, telephone, address);
            ctx.status(201).result("User registered successfully");
        });

        // Login endpoint
        app.post("/login", ctx -> {
            String username = ctx.formParam("username");
            String password = ctx.formParam("password");

            if (username == null || password == null) {
                ctx.status(400).result("Username and password are required");
                return;
            }

            String token = authService.login(username, password);
            if (token != null) {
                ctx.result(token);
            } else {
                ctx.status(401).result("Invalid credentials");
            }
        });

        // Protected profile endpoint - ADD THIS
        app.get("/profile", ctx -> {
            String authHeader = ctx.header("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                ctx.status(401).result("Missing or invalid authorization header");
                return;
            }

            String token = authHeader.substring(7);
            String username = JwtUtil.validateTokenAndGetUsername(token);

            if (username != null) {
                // Return user profile data as JSON
                ctx.json(new ProfileResponse(username, "Profile data retrieved successfully"));
            } else {
                ctx.status(401).result("Invalid or expired token");
            }
        });

        // Health check endpoint
        app.get("/health", ctx -> {
            ctx.result("Server is running");
        });
    }

    // Simple profile response class
    public static class ProfileResponse {
        public String username;
        public String message;
        public long timestamp;

        public ProfileResponse(String username, String message) {
            this.username = username;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }
    }
}