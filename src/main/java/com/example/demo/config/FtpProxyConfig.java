package com.example.demo.config;

import com.example.demo.domain.SftpCredential;
import com.example.demo.repository.SftpCredentialRepository;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.apache.ftpserver.usermanager.PasswordEncryptor;
// import org.apache.ftpserver.usermanager.impl.ClearTextPasswordEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Optional;

/**
 * Embedded FTP proxy using Apache FtpServer.
 */
@Configuration
public class FtpProxyConfig {
    @Value("${ftp.proxy.port:2121}")
    private int port;

    private final SftpCredentialRepository credRepo;

    public FtpProxyConfig(SftpCredentialRepository credRepo) {
        this.credRepo = credRepo;
    }

    @Bean
    public UserManager userManager() {
        return new UserManager() {
            PasswordEncryptor encryptor = new PasswordEncryptor() {
                @Override
                public String encrypt(String password) {
                    return password;
                }

                @Override
                public boolean matches(String inputPassword, String storedPassword) {
                    return inputPassword != null && inputPassword.equals(storedPassword);
                }
            };

            @Override
            public BaseUser getUserByName(String username) throws FtpException {
                Optional<SftpCredential> opt = credRepo.findByUsername(username);
                if (opt.isEmpty()) {
                    return null;
                }
                SftpCredential cred = opt.get();
                BaseUser user = new BaseUser();
                user.setName(username);
                user.setPassword(cred.getPassword());
                user.setAuthorities(List.of(new WritePermission()));
                user.setHomeDirectory("/");
                return user;
            }

            @Override
            public String[] getAllUserNames() throws FtpException {
                return credRepo.findAll().stream()
                        .map(SftpCredential::getUsername)
                        .toArray(String[]::new);
            }

            @Override
            public void delete(String username) throws FtpException {
                throw new UnsupportedOperationException();
            }

            @Override
            public void save(org.apache.ftpserver.ftplet.User user) throws FtpException {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean doesExist(String username) throws FtpException {
                return credRepo.findByUsername(username).isPresent();
            }

            @Override
            public org.apache.ftpserver.ftplet.User authenticate(Authentication authentication) throws AuthenticationFailedException {
                if (authentication instanceof UsernamePasswordAuthentication up) {
                    try {
                        BaseUser user = getUserByName(up.getUsername());
                        if (user != null && encryptor.matches(up.getPassword(), user.getPassword())) {
                            return user;
                        }
                    } catch (FtpException e) {
                        throw new AuthenticationFailedException("Error retrieving user", e);
                    }
                }
                throw new AuthenticationFailedException("Invalid credentials");
            }

            @Override
            public String getAdminName() throws FtpException {
                return null;
            }

            @Override
            public boolean isAdmin(String username) throws FtpException {
                return false;
            }
        };
    }

    @Bean
    public FtpServer ftpServer(UserManager userManager) throws FtpException {
        FtpServerFactory factory = new FtpServerFactory();
        factory.setUserManager(userManager);
        ListenerFactory listener = new ListenerFactory();
        listener.setPort(port);
        factory.addListener("default", listener.createListener());
        FtpServer server = factory.createServer();
        server.start();
        return server;
    }
}