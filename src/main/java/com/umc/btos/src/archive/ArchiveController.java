package com.umc.btos.src.archive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/archives")
public class ArchiveController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final ArchiveProvider archiveProvider;

    public ArchiveController(ArchiveProvider archiveProvider) {
        this.archiveProvider = archiveProvider;
    }

}
