package com.example.demo.repository;

import com.example.demo.domain.SftpObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SftpObjectRepository extends JpaRepository<SftpObject, Long> {
    List<SftpObject> findByServer_Id(Long serverId);
    void deleteByServer_Id(Long serverId);
    List<SftpObject> findByServer_IdAndPathStartingWith(Long serverId, String path);
}