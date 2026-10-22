package com.campusoverflow.identity.application;

public record RegisterUserCommand(String username, String displayName, String email, String password,
                                  String college, String className) {
}
