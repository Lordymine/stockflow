package com.stockflow.modules.users.domain.model;

import jakarta.persistence.*;

/**
 * Role entity representing a system role.
 *
 * <p>Roles are pre-defined in the database (ADMIN, MANAGER, STAFF)
 * and are associated with users through the UserRole junction table.</p>
 *
 * <p>Roles are global across all tenants but their assignment is tenant-scoped.</p>
 *
 * @see RoleEnum
 * @see UserRole
 */
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true)
    private RoleEnum name;

    /**
     * Default constructor for JPA.
     */
    protected Role() {
    }

    /**
     * Constructor for creating a role.
     *
     * @param name the role enum value
     */
    public Role(RoleEnum name) {
        this.name = name;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RoleEnum getName() {
        return name;
    }

    public void setName(RoleEnum name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role)) return false;
        Role role = (Role) o;
        return name == role.name;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return String.format("Role[id=%d, name=%s]", id, name);
    }
}
