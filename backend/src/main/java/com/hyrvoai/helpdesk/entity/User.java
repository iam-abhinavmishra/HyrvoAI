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

    /*
     * OAuth-only users do not need a local password.
     * Local accounts will still have one.
     */
    @Column(nullable = true)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String role;

    @Column
    private String department;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "company_id", nullable = true)
    private Company company;

    @Column(name = "auth_provider", nullable = false)
    private String authProvider = "LOCAL";

    @Column(name = "provider_user_id")
    private String providerUserId;

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
        this.authProvider = "LOCAL";
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
                department == null || department.isBlank()
                        ? "GENERAL"
                        : department;

        this.authProvider = "LOCAL";
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDepartment() {
        if (department == null || department.isBlank()) {
            return "GENERAL";
        }

        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getAuthProvider() {
        return authProvider;
    }

    public void setAuthProvider(String authProvider) {
        this.authProvider = authProvider;
    }

    public String getProviderUserId() {
        return providerUserId;
    }

    public void setProviderUserId(String providerUserId) {
        this.providerUserId = providerUserId;
    }
}