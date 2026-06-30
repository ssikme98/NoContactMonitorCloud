package com.ruoyi.nocontact.support.mapper;

import com.ruoyi.nocontact.support.domain.BusinessMessage;
import java.util.List;

/**
 * 业务消息Mapper接口
 */
public interface BusinessMessageMapper
{
    List<BusinessMessage> selectMessageList(BusinessMessage message);

    BusinessMessage selectMessageById(Long messageId);

    int insertMessage(BusinessMessage message);

    int updateReadStatus(BusinessMessage message);

    int countUnreadByUserName(String userName);
}
