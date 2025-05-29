package com.example.demo.dto;

import com.example.demo.domain.Protocol;

/**
 * DTO for creating/updating an SFTP/FTP server config.
 */
public class CreateSftpServerRequest {
    private String name;
    private Protocol protocol;
    private String host;
    private Integer port;
    private String basePath;
    private String username;
    private String password;
    private Integer timeout;
    private Boolean passiveMode;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Protocol getProtocol() { return protocol; }
    public void setProtocol(Protocol protocol) { this.protocol = protocol; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }

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