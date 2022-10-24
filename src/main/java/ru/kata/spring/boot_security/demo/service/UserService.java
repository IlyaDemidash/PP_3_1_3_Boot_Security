package ru.kata.spring.boot_security.demo.service;

import ru.kata.spring.boot_security.demo.entities.Role;
import ru.kata.spring.boot_security.demo.entities.User;

import java.util.List;

public interface UserService {
    List<User> findAllUsers();

    User findById(Long id);

    void saveUser(User user, String[] roles, String password);

    void deleteUser(Long id);

    User findByEmail(String email);

    void updateUser(User updUser, String[] updRoles);

    List<Role> roleList();
}
