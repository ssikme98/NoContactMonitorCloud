package com.ruoyi.nocontact.rectification.mapper;

import com.ruoyi.nocontact.rectification.domain.RectificationIssue;
import com.ruoyi.nocontact.rectification.domain.RectificationLog;
import java.util.List;
import java.util.Map;

public interface RectificationIssueMapper
{
    List<RectificationIssue> selectIssueList(RectificationIssue issue);

    RectificationIssue selectIssueById(Long issueId);

    int insertIssue(RectificationIssue issue);

    int updateIssue(RectificationIssue issue);

    int deleteIssueByIds(Long[] issueIds);

    int updateIssueStatus(RectificationIssue issue);

    int insertLog(RectificationLog log);

    List<RectificationLog> selectLogsByIssueId(Long issueId);

    List<Map<String, Object>> selectStatusStats();

    List<Map<String, Object>> selectRegionStats();
}
