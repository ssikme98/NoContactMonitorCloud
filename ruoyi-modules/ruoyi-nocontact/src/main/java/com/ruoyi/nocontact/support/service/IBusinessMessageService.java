package com.ruoyi.nocontact.support.service;

import com.ruoyi.nocontact.support.domain.BusinessMessage;
import java.util.List;

public interface IBusinessMessageService
{
    List<BusinessMessage> selectCurrentUserMessages(BusinessMessage query);

    int markRead(Long messageId);

    int countUnreadCurrentUser();

    int createMessage(BusinessMessage message);
}
