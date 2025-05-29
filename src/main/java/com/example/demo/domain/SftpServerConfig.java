package com.example.demo.domain;

import jakarta.persistence.*;

/**
 * Configuration for an external FTP/SFTP server.
 */
@Entity
@Table(name = "GW_sftp_server")
public class SftpServerConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Protocol protocol;

    @Column(nullable = false)
    private String host;

    @Column(nullable = false)
    private int port;

    @Column(name = "base_path")
    private String basePath;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "timeout_millis")
    private Integer timeout;

    @Column(name = "passive_mode")
    private Boolean passiveMode;

    // Getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Protocol getProtocol() { return protocol; }
    public void setProtocol(Protocol protocol) { this.protocol = protocol; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getBasePath() { return basePath; }
    public void setBasePath(String basePath) { this.basePath = basePath; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Integer getTimeout() { return timeout; }
    public void setTimeout(Integer timeout) { this.timeout = timeout; }

    public Boolean getPassiveMode() { return passiveMode; }
    public void setPassiveMode(Boolean passiveMode) { this.passiveMode = passiveMode; }
}