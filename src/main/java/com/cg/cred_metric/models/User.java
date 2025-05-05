package com.cg.cred_metric.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@NoArgsConstructor
@Data
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long userId;

    private String name;
    private String email;
    private String password;

    @JsonIgnore
    private String token;
    private LocalDate dateOfBirth;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore // This hides loans in JSON response
    private List<Loan> loans;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<CreditCard> creditCards;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Repayment> repayments;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private CreditScore creditScore;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<CreditScoreSuggestion> suggestions;

    // Getters and Setters

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();  // Set createdAt when the entity is created
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();  // Set updatedAt when the entity is updated
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Return roles or authorities for the user
        return null; // For example, roles/permissions
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return this.password;
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return this.email;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true; // Logic for account expiration
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true; // Logic for account lock
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true; // Logic for credential expiration
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true; // Logic for account enabling/disabling (you can modify based on your requirements)
    }
}
