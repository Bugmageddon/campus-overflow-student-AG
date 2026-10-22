package com.campusoverflow.identity.infrastructure.persistence;

import com.campusoverflow.shared.security.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;

/** JPA 持久化对象（与领域对象 User 分离，见 ADR-008）。邮箱以 AES-GCM 密文 + HMAC 摘要存储。 */
@Entity
@Table(name = "id_user")
public class UserJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String username;

    @Column(name = "display_name", nullable = false, unique = true, length = 20)
    private String displayName;

    @Column(name = "email_cipher", nullable = false, length = 255)
    private String emailCipher;

    @Column(name = "email_hash", nullable = false, unique = true, length = 64)
    private String emailHash;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Role role;

    @Column(nullable = false, length = 16)
    private String status;

    @Column(nullable = false)
    private boolean verified;

    @Column(length = 50)
    private String college;

    @Column(name = "class_name", length = 50)
    private String className;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Version
    private long version;

    protected UserJpaEntity() {
    }

    public UserJpaEntity(String username, Instant createdAt) {
        this.username = username;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getEmailCipher() { return emailCipher; }
    public void setEmailCipher(String emailCipher) { this.emailCipher = emailCipher; }
    public String getEmailHash() { return emailHash; }
    public void setEmailHash(String emailHash) { this.emailHash = emailHash; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public Instant getCreatedAt() { return createdAt; }
    public long getVersion() { return version; }
}
