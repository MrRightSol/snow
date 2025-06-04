package com.example.demo.config;

import com.example.demo.domain.SftpCredential;
import com.example.demo.domain.SftpServerConfig;
import com.example.demo.repository.SftpCredentialRepository;
import com.example.demo.service.SftpService;
import org.apache.sshd.server.SshServer;
import com.example.demo.config.ProxySftpSubsystemFactory;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
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
        // Proxy SFTP subsystem to remote servers
        sshd.setSubsystemFactories(
            Collections.singletonList(new ProxySftpSubsystemFactory(credRepo, sftpService))
        );
        sshd.start();
        return sshd;
    }
}