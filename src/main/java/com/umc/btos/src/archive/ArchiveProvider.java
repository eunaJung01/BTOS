package com.umc.btos.src.archive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ArchiveProvider {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ArchiveDao archiveDao;

    @Autowired
    public ArchiveProvider(ArchiveDao archiveDao) {
        this.archiveDao = archiveDao;
    }

}
