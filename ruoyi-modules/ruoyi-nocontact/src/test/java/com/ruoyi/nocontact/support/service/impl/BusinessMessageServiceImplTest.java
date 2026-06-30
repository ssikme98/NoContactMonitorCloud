package com.ruoyi.nocontact.support.service.impl;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.context.SecurityContextHolder;
import com.ruoyi.nocontact.support.domain.BusinessMessage;
import com.ruoyi.nocontact.support.mapper.BusinessMessageMapper;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessMessageServiceImplTest
{
    private BusinessMessageServiceImpl service;

    @Mock
    private BusinessMessageMapper messageMapper;

    @BeforeEach
    void setUp()
    {
        service = new BusinessMessageServiceImpl();
        ReflectionTestUtils.setField(service, "messageMapper", messageMapper);
    }

    @AfterEach
    void tearDown()
    {
        SecurityContextHolder.remove();
    }

    @Test
    void markCurrentUserMessageReadUpdatesReadStatus()
    {
        SecurityContextHolder.setUserName("admin");
        BusinessMessage message = new BusinessMessage();
        message.setMessageId(3001L);
        message.setReceiverUserName("admin");
        when(messageMapper.selectMessageById(3001L)).thenReturn(message);
        when(messageMapper.updateReadStatus(any(BusinessMessage.class))).thenReturn(1);

        int rows = service.markRead(3001L);

        assertEquals(1, rows);
        verify(messageMapper).updateReadStatus(argThat(update -> update != null
                && Long.valueOf(3001L).equals(update.getMessageId())
                && "1".equals(update.getReadStatus())
                && "admin".equals(update.getUpdateBy())));
    }

    @Test
    void readingAnotherUsersMessageIsRejected()
    {
        SecurityContextHolder.setUserName("admin");
        BusinessMessage message = new BusinessMessage();
        message.setMessageId(3001L);
        message.setReceiverUserName("other");
        when(messageMapper.selectMessageById(3001L)).thenReturn(message);

        ServiceException exception = assertThrows(ServiceException.class, () -> service.markRead(3001L));

        assertEquals("消息不存在或无权访问", exception.getMessage());
    }

    @Test
    void listCurrentUserMessagesUsesCurrentUsername()
    {
        SecurityContextHolder.setUserName("admin");
        when(messageMapper.selectMessageList(any(BusinessMessage.class))).thenReturn(Collections.<BusinessMessage>emptyList());

        List<BusinessMessage> list = service.selectCurrentUserMessages(new BusinessMessage());

        assertEquals(0, list.size());
        verify(messageMapper).selectMessageList(argThat(query -> query != null && "admin".equals(query.getReceiverUserName())));
    }
}
