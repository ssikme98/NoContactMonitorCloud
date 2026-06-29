package com.ruoyi.nocontact.rectification.service;

import com.ruoyi.nocontact.rectification.domain.RectificationIssue;
import java.util.List;
import java.util.Map;

public interface IRectificationIssueService
{
    List<RectificationIssue> selectIssueList(RectificationIssue issue);

    RectificationIssue selectIssueById(Long issueId);

    int insertIssue(RectificationIssue issue, String operName);

    int updateIssue(RectificationIssue issue, String operName);

    int deleteIssueByIds(Long[] issueIds);

    int dispatch(Long issueId, RectificationIssue form, String operName);

    int submit(Long issueId, RectificationIssue form, String operName);

    int review(Long issueId, boolean approved, RectificationIssue form, String operName);

    int archive(Long issueId, String operName);

    Map<String, Object> dashboard();
}
