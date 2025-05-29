package com.example.demo.config;

import com.example.demo.domain.SftpCredential;
import com.example.demo.domain.SftpServerConfig;
import com.example.demo.repository.SftpCredentialRepository;
import com.example.demo.service.SftpService;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;

/**
 * Embedded SFTP proxy using Apache MINA SSHD.
 */
@Configuration
public class SshdSftpProxyConfig {
    @Value("${sftp.proxy.port:2222}")
    private int port;

    private final SftpCredentialRepository credRepo;
    private final SftpService sftpService;

    public SshdSftpProxyConfig(SftpCredentialRepository credRepo, SftpService sftpService) {
        this.credRepo = credRepo;
        this.sftpService = sftpService;
    }

    @Bean
    public SshServer sshd() throws IOException {
        SshServer sshd = SshServer.setUpDefaultServer();
        sshd.setPort(port);
        sshd.setKeyPairProvider(
            new SimpleGeneratorHostKeyProvider(
                Paths.get("hostkey.ser")
            )
        );
        sshd.setPasswordAuthenticator(new PasswordAuthenticator() {
            @Override
            public boolean authenticate(String username, String password, ServerSession session) {
                return credRepo.findByUsername(username)
                        .filter(c -> c.getPassword().equals(password)
                                && c.getExpiresAt().isAfter(LocalDateTime.now()))
                        .isPresent();
            }
        });
        sshd.setSubsystemFactories(
            Collections.singletonList(new SftpSubsystemFactory())
        );
        VirtualFileSystemFactory vfs = new VirtualFileSystemFactory();
        credRepo.findAll().forEach(c ->
            vfs.setUserHomeDir(c.getUsername(), Paths.get(c.getServer().getBasePath()))
        );
        sshd.setFileSystemFactory(vfs);
        sshd.start();
        return sshd;
    }
}