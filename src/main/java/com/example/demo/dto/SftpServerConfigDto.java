package com.example.demo.dto;

import com.example.demo.domain.Protocol;

/**
 * DTO representing an SFTP/FTP server config.
 */
public class SftpServerConfigDto {
    private Long id;
    private String name;
    private Protocol protocol;
    private String host;
    private int port;
    private String basePath;
    private String username;
    private Integer timeout;
    private Boolean passiveMode;

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

    public Integer getTimeout() { return timeout; }
    public void setTimeout(Integer timeout) { this.timeout = timeout; }

    public Boolean getPassiveMode() { return passiveMode; }
    public void setPassiveMode(Boolean passiveMode) { this.passiveMode = passiveMode; }
}