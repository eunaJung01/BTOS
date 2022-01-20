package com.umc.btos.src.mailbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MailboxProvider {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final MailboxDao mailboxDao;

    @Autowired
    public MailboxProvider(MailboxDao mailboxDao) {
        this.mailboxDao = mailboxDao;
    }

}
