package com.example.demo.config;

import com.example.demo.repository.SftpCredentialRepository;
import com.example.demo.service.SftpService;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.subsystem.SubsystemFactory;

/**
 * Factory for proxying SFTP subsystem commands to remote servers via SftpService.
 */
public class ProxySftpSubsystemFactory implements NamedFactory<SubsystemFactory>, SubsystemFactory {
    private final SftpCredentialRepository credRepo;
    private final SftpService sftpService;

    public ProxySftpSubsystemFactory(SftpCredentialRepository credRepo, SftpService sftpService) {
        this.credRepo = credRepo;
        this.sftpService = sftpService;
    }

    @Override
    public String getName() {
        return "sftp";
    }

    @Override
    public SubsystemFactory create() {
        return this;
    }
    @Override
    public Command createSubsystem(ChannelSession session) {
        return new ProxySftpCommand(credRepo, sftpService);
    }
}