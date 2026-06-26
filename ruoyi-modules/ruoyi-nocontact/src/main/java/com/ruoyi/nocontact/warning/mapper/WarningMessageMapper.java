package com.ruoyi.nocontact.warning.mapper;

import com.ruoyi.nocontact.warning.domain.WarningMessage;
import java.util.List;
import java.util.Map;

/**
 * 预警消息Mapper接口
 *
 * @author ruoyi
 */
public interface WarningMessageMapper
{
    public List<WarningMessage> selectMessageList(WarningMessage message);

    public WarningMessage selectMessageById(Long messageId);

    public int insertMessage(WarningMessage message);

    public int updateMessageStatus(WarningMessage message);

    public int deleteMessageByIds(Long[] messageIds);

    public Map<String, Object> selectDashboardSummary();

    public List<Map<String, Object>> selectLevelStats();

    public List<Map<String, Object>> selectRegionStats();

    public List<Map<String, Object>> selectIndicatorStats();

    public List<Map<String, Object>> selectTrendStats();
}
