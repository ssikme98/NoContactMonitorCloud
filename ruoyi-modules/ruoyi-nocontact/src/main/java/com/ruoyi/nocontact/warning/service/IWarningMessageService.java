package com.ruoyi.nocontact.warning.service;

import com.ruoyi.nocontact.warning.domain.WarningMessage;
import java.util.List;
import java.util.Map;

/**
 * 预警消息Service接口
 *
 * @author ruoyi
 */
public interface IWarningMessageService
{
    public List<WarningMessage> selectMessageList(WarningMessage message);

    public WarningMessage selectMessageById(Long messageId);

    public int updateMessageStatus(Long messageId, String status, String operName);

    public int deleteMessageByIds(Long[] messageIds);

    public Map<String, Object> selectDashboard();
}
