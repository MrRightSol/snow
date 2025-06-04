package com.example.demo.config;

import com.example.demo.domain.SftpCredential;
import com.example.demo.domain.SftpServerConfig;
import com.example.demo.repository.SftpCredentialRepository;
import com.example.demo.service.SftpService;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.channel.ChannelSession;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * SFTP subsystem command that proxies between the client and a remote SFTP server.
 */
public class ProxySftpCommand implements Command {
    private final SftpCredentialRepository credRepo;
    private final SftpService sftpService;
    private InputStream in;
    private OutputStream out;
    @SuppressWarnings("unused")
    private OutputStream err;
    private ExitCallback callback;
    private Session remoteSession;
    private Channel remoteChannel;
    private Thread in2remote;
    private Thread remote2out;

    public ProxySftpCommand(SftpCredentialRepository credRepo, SftpService sftpService) {
        this.credRepo = credRepo;
        this.sftpService = sftpService;
    }

    @Override
    public void setInputStream(InputStream in) {
        this.in = in;
    }

    @Override
    public void setOutputStream(OutputStream out) {
        this.out = out;
    }

    @Override
    public void setErrorStream(OutputStream err) {
        this.err = err;
    }

    @Override
    public void setExitCallback(ExitCallback callback) {
        this.callback = callback;
    }


    @Override
    public void start(org.apache.sshd.server.channel.ChannelSession session, Environment env) throws IOException {
        String username = session.getServerSession().getUsername();
        SftpCredential cred = credRepo.findByUsername(username)
                .orElseThrow(() -> new IOException("Invalid SFTP credential for user: " + username));
        SftpServerConfig cfg = cred.getServer();
        try {
            // Open remote SFTP channel
            JSch jsch = new JSch();
            remoteSession = jsch.getSession(cfg.getUsername(), cfg.getHost(), cfg.getPort());
            remoteSession.setPassword(cfg.getPassword());
            Properties props = new Properties();
            props.put("StrictHostKeyChecking", "no");
            remoteSession.setConfig(props);
            if (cfg.getTimeout() != null) {
                remoteSession.connect(cfg.getTimeout());
            } else {
                remoteSession.connect();
            }
            // Use SFTP subsystem
            Channel channel = remoteSession.openChannel("sftp");
            if (cfg.getTimeout() != null) {
                channel.connect(cfg.getTimeout());
            } else {
                channel.connect();
            }
            remoteChannel = channel;
            InputStream remoteIn = remoteChannel.getInputStream();
            OutputStream remoteOut = remoteChannel.getOutputStream();

            // Pipe client -> remote
            in2remote = new Thread(() -> {
                byte[] buf = new byte[8192];
                int len;
                try {
                    while ((len = in.read(buf)) != -1) {
                        remoteOut.write(buf, 0, len);
                        remoteOut.flush();
                    }
                } catch (IOException ignored) {
                } finally {
                    cleanup();
                }
            }, "sftp-proxy-in");
            // Pipe remote -> client
            remote2out = new Thread(() -> {
                byte[] buf = new byte[8192];
                int len;
                try {
                    while ((len = remoteIn.read(buf)) != -1) {
                        out.write(buf, 0, len);
                        out.flush();
                    }
                } catch (IOException ignored) {
                } finally {
                    cleanup();
                }
            }, "sftp-proxy-out");
            in2remote.start();
            remote2out.start();
        } catch (Exception e) {
            cleanup();
            throw new IOException("Failed to start SFTP proxy: " + e.getMessage(), e);
        }
    }

    @Override
    public void destroy(ChannelSession session) {
        cleanup();
    }

    private synchronized void cleanup() {
        try {
            if (remoteChannel != null && remoteChannel.isConnected()) {
                remoteChannel.disconnect();
            }
            if (remoteSession != null && remoteSession.isConnected()) {
                remoteSession.disconnect();
            }
        } catch (Exception ignore) {
        }
        if (callback != null) {
            callback.onExit(0);
            callback = null;
        }
    }
}