package com.umc.btos.src.mailbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/btos/mailbox")
public class MailboxController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final MailboxProvider mailboxProvider;
    @Autowired
    private final MailboxService mailboxService;

    public MailboxController(MailboxProvider mailboxProvider, MailboxService mailboxService) {
        this.mailboxProvider = mailboxProvider;
        this.mailboxService = mailboxService;
    }

}
