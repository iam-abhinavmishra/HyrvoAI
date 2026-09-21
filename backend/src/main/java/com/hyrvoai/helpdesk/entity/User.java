package com.hyrvoai.helpdesk.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String role;

    @Column
    private String department;

    public User() {
    }

    public User(
            String email,
            String password,
            String name,
            String role) {

        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
        this.department = "GENERAL";
    }

    public User(
            String email,
            String password,
            String name,
            String role,
            String department) {

        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
        this.department =
                department == null
                        || department.isBlank()
                        ? "GENERAL"
                        : department;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(
            String email) {

        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(
            String password) {

        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(
            String name) {

        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(
            String role) {

        this.role = role;
    }

    public String getDepartment() {

        if (department == null
                || department.isBlank()) {

            return "GENERAL";
        }

        return department;
    }

    public void setDepartment(
            String department) {

        this.department = department;
    }
}