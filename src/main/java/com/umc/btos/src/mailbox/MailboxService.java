package com.umc.btos.src.mailbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MailboxService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final MailboxDao mailboxDao;

    @Autowired
    public MailboxService(MailboxDao mailboxDao) {
        this.mailboxDao = mailboxDao;
    }

}
