package com.example.demo.service;

import com.example.demo.domain.Protocol;
import com.example.demo.domain.SftpServerConfig;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.stereotype.Service;

/**
 * SFTP/FTP client operations (test + listing).
 */
@Service
public class SftpService {
    /**
     * Tests connectivity for the given server config.
     */
    public boolean testConnection(SftpServerConfig cfg) {
        if (cfg.getProtocol() == Protocol.FTP || cfg.getProtocol() == Protocol.FTPS) {
            return testFtp(cfg);
        }
        return testSftp(cfg);
    }

    private boolean testFtp(SftpServerConfig cfg) {
        FTPClient ftp = new FTPClient();
        try {
            if (cfg.getTimeout() != null) {
                ftp.setDefaultTimeout(cfg.getTimeout());
            }
            ftp.connect(cfg.getHost(), cfg.getPort());
            boolean success = ftp.login(cfg.getUsername(), cfg.getPassword());
            ftp.logout();
            ftp.disconnect();
            return success;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean testSftp(SftpServerConfig cfg) {
        Session session = null;
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(cfg.getUsername(), cfg.getHost(), cfg.getPort());
            session.setPassword(cfg.getPassword());
            java.util.Properties props = new java.util.Properties();
            props.put("StrictHostKeyChecking", "no");
            session.setConfig(props);
            if (cfg.getTimeout() != null) {
                session.connect(cfg.getTimeout());
            } else {
                session.connect();
            }
            session.disconnect();
            return true;
        } catch (Exception e) {
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
            return false;
        }
    }
    /**
     * Lists files and directories at the given path (non-recursive).
     */
    public java.util.List<com.example.demo.dto.SftpObjectDto> listDirectory(
            SftpServerConfig cfg, String path) throws Exception {
        java.util.List<com.example.demo.dto.SftpObjectDto> result = new java.util.ArrayList<>();
        if (cfg.getProtocol() == Protocol.FTP || cfg.getProtocol() == Protocol.FTPS) {
            // FTP
            org.apache.commons.net.ftp.FTPClient ftp = new org.apache.commons.net.ftp.FTPClient();
            try {
                if (cfg.getTimeout() != null) ftp.setDefaultTimeout(cfg.getTimeout());
                ftp.connect(cfg.getHost(), cfg.getPort());
                ftp.login(cfg.getUsername(), cfg.getPassword());
                org.apache.commons.net.ftp.FTPFile[] files = ftp.listFiles(path);
                for (org.apache.commons.net.ftp.FTPFile f : files) {
                    com.example.demo.dto.SftpObjectDto dto = new com.example.demo.dto.SftpObjectDto();
                    dto.setPath(path + "/" + f.getName());
                    dto.setName(f.getName());
                    dto.setObjectType(f.isDirectory() ? "DIR" : "FILE");
                    dto.setSize(f.getSize());
                    dto.setLastModified(java.time.LocalDateTime.ofInstant(
                            f.getTimestamp().getTime().toInstant(),
                            java.time.ZoneId.systemDefault()));
                    dto.setPermissions(f.getRawListing());
                    result.add(dto);
                }
                ftp.logout();
                ftp.disconnect();
                return result;
            } finally {
                if (ftp.isConnected()) ftp.disconnect();
            }
        } else {
            // SFTP
            com.jcraft.jsch.Session session = null;
            try {
                com.jcraft.jsch.JSch jsch = new com.jcraft.jsch.JSch();
                session = jsch.getSession(cfg.getUsername(), cfg.getHost(), cfg.getPort());
                session.setPassword(cfg.getPassword());
                java.util.Properties props = new java.util.Properties();
                props.put("StrictHostKeyChecking", "no");
                session.setConfig(props);
                if (cfg.getTimeout() != null) session.connect(cfg.getTimeout());
                else session.connect();
                com.jcraft.jsch.Channel channel = session.openChannel("sftp");
                channel.connect();
                com.jcraft.jsch.ChannelSftp sftp = (com.jcraft.jsch.ChannelSftp) channel;
                @SuppressWarnings("unchecked")
                java.util.Vector<com.jcraft.jsch.ChannelSftp.LsEntry> entries =
                        sftp.ls(path);
                for (com.jcraft.jsch.ChannelSftp.LsEntry e : entries) {
                    String name = e.getFilename();
                    if (".".equals(name) || "..".equals(name)) continue;
                    com.example.demo.dto.SftpObjectDto dto = new com.example.demo.dto.SftpObjectDto();
                    String full = path.endsWith("/") ? path + name : path + "/" + name;
                    dto.setPath(full);
                    dto.setName(name);
                    boolean isDir = e.getAttrs().isDir();
                    dto.setObjectType(isDir ? "DIR" : "FILE");
                    dto.setSize(isDir ? null : e.getAttrs().getSize());
                    dto.setLastModified(java.time.LocalDateTime.ofInstant(
                            java.util.Date.from(
                                java.time.Instant.ofEpochMilli(e.getAttrs().getMTime() * 1000L)
                            ).toInstant(),
                            java.time.ZoneId.systemDefault()));
                    dto.setPermissions(e.getAttrs().getPermissionsString());
                    result.add(dto);
                }
                sftp.exit();
                session.disconnect();
                return result;
            } finally {
                if (session != null && session.isConnected()) session.disconnect();
            }
        }
    }
    /**
     * Download the file at the given path into the provided OutputStream.
     */
    public void downloadTo(SftpServerConfig cfg, String path, java.io.OutputStream out) throws Exception {
        if (cfg.getProtocol() == Protocol.FTP || cfg.getProtocol() == Protocol.FTPS) {
            org.apache.commons.net.ftp.FTPClient ftp = new org.apache.commons.net.ftp.FTPClient();
            try (java.io.InputStream in = connectAndGetFtpStream(ftp, cfg, path)) {
                byte[] buf = new byte[8192];
                int r;
                while ((r = in.read(buf)) != -1) {
                    out.write(buf, 0, r);
                }
                ftp.completePendingCommand();
            } finally {
                ftp.logout();
                ftp.disconnect();
            }
        } else {
            com.jcraft.jsch.Session session = null;
            com.jcraft.jsch.ChannelSftp sftp = null;
            try {
                com.jcraft.jsch.JSch jsch = new com.jcraft.jsch.JSch();
                session = jsch.getSession(cfg.getUsername(), cfg.getHost(), cfg.getPort());
                session.setPassword(cfg.getPassword());
                java.util.Properties props = new java.util.Properties();
                props.put("StrictHostKeyChecking", "no");
                session.setConfig(props);
                if (cfg.getTimeout() != null) session.connect(cfg.getTimeout()); else session.connect();
                com.jcraft.jsch.Channel channel = session.openChannel("sftp");
                channel.connect();
                sftp = (com.jcraft.jsch.ChannelSftp) channel;
                try (java.io.InputStream in = sftp.get(path)) {
                    byte[] buf = new byte[8192];
                    int r;
                    while ((r = in.read(buf)) != -1) {
                        out.write(buf, 0, r);
                    }
                }
            } finally {
                if (sftp != null) sftp.exit();
                if (session != null && session.isConnected()) session.disconnect();
            }
        }
    }

    private java.io.InputStream connectAndGetFtpStream(org.apache.commons.net.ftp.FTPClient ftp,
                                                      SftpServerConfig cfg,
                                                      String path) throws java.io.IOException {
        if (cfg.getTimeout() != null) ftp.setDefaultTimeout(cfg.getTimeout());
        ftp.connect(cfg.getHost(), cfg.getPort());
        ftp.login(cfg.getUsername(), cfg.getPassword());
        return ftp.retrieveFileStream(path);
    }

    /**
     * Upload data from the provided InputStream to the given path.
     */
    public void uploadFrom(SftpServerConfig cfg, String path, java.io.InputStream in) throws Exception {
        if (cfg.getProtocol() == Protocol.FTP || cfg.getProtocol() == Protocol.FTPS) {
            org.apache.commons.net.ftp.FTPClient ftp = new org.apache.commons.net.ftp.FTPClient();
            try {
                if (cfg.getTimeout() != null) ftp.setDefaultTimeout(cfg.getTimeout());
                ftp.connect(cfg.getHost(), cfg.getPort());
                ftp.login(cfg.getUsername(), cfg.getPassword());
                ftp.storeFile(path, in);
            } finally {
                ftp.logout();
                ftp.disconnect();
            }
        } else {
            com.jcraft.jsch.Session session = null;
            com.jcraft.jsch.ChannelSftp sftp = null;
            try {
                com.jcraft.jsch.JSch jsch = new com.jcraft.jsch.JSch();
                session = jsch.getSession(cfg.getUsername(), cfg.getHost(), cfg.getPort());
                session.setPassword(cfg.getPassword());
                java.util.Properties props = new java.util.Properties();
                props.put("StrictHostKeyChecking", "no");
                session.setConfig(props);
                if (cfg.getTimeout() != null) session.connect(cfg.getTimeout()); else session.connect();
                com.jcraft.jsch.Channel channel = session.openChannel("sftp");
                channel.connect();
                sftp = (com.jcraft.jsch.ChannelSftp) channel;
                sftp.put(in, path);
            } finally {
                if (sftp != null) sftp.exit();
                if (session != null && session.isConnected()) session.disconnect();
            }
        }
    }
}