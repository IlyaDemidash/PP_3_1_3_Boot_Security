package ru.kata.spring.boot_security.demo.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.kata.spring.boot_security.demo.entities.Role;
import ru.kata.spring.boot_security.demo.entities.User;
import ru.kata.spring.boot_security.demo.repositories.UserRepo;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserServiceImpl;

import javax.annotation.PostConstruct;

/*
Создание конфигурации для Spring Security (юзеры, пароли, роли...)
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private final SuccessUserHandler successUserHandler;
    private UserServiceImpl userService;

    private UserRepo userRepo;

    private RoleService roleService;

    @Autowired
    public void setUserService(UserServiceImpl userService) {
        this.userService = userService;
    }

    public WebSecurityConfig(SuccessUserHandler successUserHandler, UserRepo userRepo, RoleService roleService) {
        this.successUserHandler = successUserHandler;
        this.userRepo = userRepo;
        this.roleService = roleService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/user/**").hasAnyRole("ADMIN", "USER")
                .anyRequest().authenticated()
                .and()
                .formLogin().successHandler(successUserHandler)
                .permitAll()
                .and()
                .logout()
                .permitAll();
    }

    /*
    Преобразование паролей (из текста в хэш)
     */
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
    Далее создаем authenticationProvider его задача сказать существует ли такой пользователь и если существует,
    то его нужно положить в SpringSecurityContext. Для этого есть метод:
    setUserDetailsService - будет предоставлять юзеров
     */
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setPasswordEncoder(passwordEncoder()); //указываем наш энкодер паролей
        authenticationProvider.setUserDetailsService(userService);
        return authenticationProvider;
    }

    /*
    Создаем пользователей в БД при старте приложения, пароли: 100
     */
    @PostConstruct
    public void addAdminInDB() {
        roleService.saveRole(new Role("ROLE_ADMIN"));
        roleService.saveRole(new Role("ROLE_USER"));

        String[] adminRoles = {"ROLE_ADMIN", "ROLE_USER"};
        String[] userRoles = {"ROLE_USER"};

        userService.saveUser(new User("admin", "adm", 33, "admin@mail.ru"), adminRoles, "111");
        userService.saveUser(new User("user", "usr", 44, "user@mail.ru"), userRoles, "111");
    }

}