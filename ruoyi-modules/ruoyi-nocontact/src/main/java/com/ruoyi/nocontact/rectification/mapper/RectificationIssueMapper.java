package com.ruoyi.nocontact.rectification.mapper;

import com.ruoyi.nocontact.rectification.domain.RectificationIssue;
import com.ruoyi.nocontact.rectification.domain.RectificationLog;
import java.util.List;
import java.util.Map;

public interface RectificationIssueMapper
{
    List<RectificationIssue> selectIssueList(RectificationIssue issue);

    int countIssueList(RectificationIssue issue);

    RectificationIssue selectIssueById(Long issueId);

    RectificationIssue selectIssueByScope(RectificationIssue issue);

    RectificationIssue selectActiveIssueBySourceWarningId(Long sourceWarningId);

    int insertIssue(RectificationIssue issue);

    int updateIssue(RectificationIssue issue);

    int deleteIssueByIds(Long[] issueIds);

    int updateIssueStatus(RectificationIssue issue);

    int insertLog(RectificationLog log);

    List<RectificationLog> selectLogsByIssueId(Long issueId);

    List<Map<String, Object>> selectStatusStats(RectificationIssue issue);

    List<Map<String, Object>> selectRegionStats(RectificationIssue issue);
}
