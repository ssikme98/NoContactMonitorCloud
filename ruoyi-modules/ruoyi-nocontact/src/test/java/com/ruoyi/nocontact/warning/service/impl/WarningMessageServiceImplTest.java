package com.ruoyi.nocontact.warning.service.impl;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.nocontact.warning.domain.WarningMessage;
import com.ruoyi.nocontact.warning.mapper.WarningMessageMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WarningMessageServiceImplTest
{
    private WarningMessageServiceImpl service;

    @Mock
    private WarningMessageMapper messageMapper;

    @BeforeEach
    void setUp()
    {
        service = new WarningMessageServiceImpl();
        ReflectionTestUtils.setField(service, "messageMapper", messageMapper);
    }

    @Test
    void pendingWarningCanMoveToProcessingAndWritesHandleLog()
    {
        when(messageMapper.selectMessageById(4001L)).thenReturn(message("pending"));
        when(messageMapper.updateMessageStatus(any(WarningMessage.class))).thenReturn(1);

        int rows = service.updateMessageStatus(4001L, "processing", "开始处理", "admin");

        assertEquals(1, rows);
        verify(messageMapper).insertHandleLog(any());
    }

    @Test
    void processingWarningCanClose()
    {
        when(messageMapper.selectMessageById(4001L)).thenReturn(message("processing"));
        when(messageMapper.updateMessageStatus(any(WarningMessage.class))).thenReturn(1);

        int rows = service.updateMessageStatus(4001L, "closed", "已核查", "admin");

        assertEquals(1, rows);
        verify(messageMapper).insertHandleLog(any());
    }

    @Test
    void closedWarningCannotChangeAgain()
    {
        when(messageMapper.selectMessageById(4001L)).thenReturn(message("closed"));

        assertThrows(ServiceException.class, () -> service.updateMessageStatus(4001L, "ignored", "忽略", "admin"));

        verify(messageMapper, never()).updateMessageStatus(any());
        verify(messageMapper, never()).insertHandleLog(any());
    }

    @Test
    void unsupportedStatusIsRejected()
    {
        when(messageMapper.selectMessageById(4001L)).thenReturn(message("pending"));

        assertThrows(ServiceException.class, () -> service.updateMessageStatus(4001L, "handled", "旧状态", "admin"));

        verify(messageMapper, never()).updateMessageStatus(any());
    }

    private WarningMessage message(String status)
    {
        WarningMessage message = new WarningMessage();
        message.setMessageId(4001L);
        message.setMessageStatus(status);
        return message;
    }
}
