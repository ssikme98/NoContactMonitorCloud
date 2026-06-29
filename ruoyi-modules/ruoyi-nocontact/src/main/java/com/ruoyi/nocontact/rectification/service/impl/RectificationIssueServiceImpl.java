package com.ruoyi.nocontact.rectification.service.impl;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.nocontact.rectification.domain.RectificationIssue;
import com.ruoyi.nocontact.rectification.domain.RectificationLog;
import com.ruoyi.nocontact.rectification.mapper.RectificationIssueMapper;
import com.ruoyi.nocontact.rectification.service.IRectificationIssueService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RectificationIssueServiceImpl implements IRectificationIssueService
{
    @Autowired
    private RectificationIssueMapper issueMapper;

    @Override
    public List<RectificationIssue> selectIssueList(RectificationIssue issue)
    {
        return issueMapper.selectIssueList(issue);
    }

    @Override
    public RectificationIssue selectIssueById(Long issueId)
    {
        RectificationIssue issue = issueMapper.selectIssueById(issueId);
        if (issue != null)
        {
            issue.setLogs(issueMapper.selectLogsByIssueId(issueId));
        }
        return issue;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertIssue(RectificationIssue issue, String operName)
    {
        issue.setCreateBy(operName);
        issue.setCreateTime(DateUtils.getNowDate());
        if (StringUtils.isBlank(issue.getSourceType()))
        {
            issue.setSourceType("manual");
        }
        if (StringUtils.isBlank(issue.getIssueStatus()))
        {
            issue.setIssueStatus("pending_dispatch");
        }
        int rows = issueMapper.insertIssue(issue);
        insertLog(issue.getIssueId(), null, issue.getIssueStatus(), "新增问题", issue.getRemark(), operName);
        return rows;
    }

    @Override
    public int updateIssue(RectificationIssue issue, String operName)
    {
        issue.setUpdateBy(operName);
        issue.setUpdateTime(DateUtils.getNowDate());
        return issueMapper.updateIssue(issue);
    }

    @Override
    public int deleteIssueByIds(Long[] issueIds)
    {
        return issueMapper.deleteIssueByIds(issueIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int dispatch(Long issueId, RectificationIssue form, String operName)
    {
        RectificationIssue issue = requireIssue(issueId);
        requireStatus(issue, "pending_dispatch", "rejected");
        form.setIssueId(issueId);
        form.setIssueStatus("rectifying");
        form.setDispatchTime(DateUtils.getNowDate());
        form.setUpdateBy(operName);
        form.setUpdateTime(DateUtils.getNowDate());
        int rows = issueMapper.updateIssueStatus(form);
        insertLog(issueId, issue.getIssueStatus(), "rectifying", "分配整改", form.getReviewOpinion(), operName);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submit(Long issueId, RectificationIssue form, String operName)
    {
        RectificationIssue issue = requireIssue(issueId);
        requireStatus(issue, "rectifying");
        form.setIssueId(issueId);
        form.setIssueStatus("pending_review");
        form.setSubmitTime(DateUtils.getNowDate());
        form.setUpdateBy(operName);
        form.setUpdateTime(DateUtils.getNowDate());
        int rows = issueMapper.updateIssueStatus(form);
        insertLog(issueId, issue.getIssueStatus(), "pending_review", "提交整改", form.getRectificationResult(), operName);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int review(Long issueId, boolean approved, RectificationIssue form, String operName)
    {
        RectificationIssue issue = requireIssue(issueId);
        requireStatus(issue, "pending_review");
        form.setIssueId(issueId);
        form.setIssueStatus(approved ? "review_passed" : "rejected");
        form.setReviewTime(DateUtils.getNowDate());
        form.setUpdateBy(operName);
        form.setUpdateTime(DateUtils.getNowDate());
        int rows = issueMapper.updateIssueStatus(form);
        insertLog(issueId, issue.getIssueStatus(), form.getIssueStatus(), approved ? "审核通过" : "审核驳回", form.getReviewOpinion(), operName);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int archive(Long issueId, String operName)
    {
        RectificationIssue issue = requireIssue(issueId);
        requireStatus(issue, "review_passed");
        RectificationIssue form = new RectificationIssue();
        form.setIssueId(issueId);
        form.setIssueStatus("archived");
        form.setArchiveTime(DateUtils.getNowDate());
        form.setUpdateBy(operName);
        form.setUpdateTime(DateUtils.getNowDate());
        int rows = issueMapper.updateIssueStatus(form);
        insertLog(issueId, issue.getIssueStatus(), "archived", "归档", "问题关闭", operName);
        return rows;
    }

    @Override
    public Map<String, Object> dashboard()
    {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("statusStats", issueMapper.selectStatusStats());
        data.put("regionStats", issueMapper.selectRegionStats());
        return data;
    }

    private RectificationIssue requireIssue(Long issueId)
    {
        RectificationIssue issue = issueMapper.selectIssueById(issueId);
        if (issue == null)
        {
            throw new ServiceException("整改问题不存在");
        }
        return issue;
    }

    private void requireStatus(RectificationIssue issue, String... allowed)
    {
        for (String status : allowed)
        {
            if (status.equals(issue.getIssueStatus()))
            {
                return;
            }
        }
        throw new ServiceException("当前状态不允许执行该操作：" + issue.getIssueStatus());
    }

    private void insertLog(Long issueId, String fromStatus, String toStatus, String actionName, String opinion, String operName)
    {
        RectificationLog log = new RectificationLog();
        log.setIssueId(issueId);
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setActionName(actionName);
        log.setHandleBy(operName);
        log.setHandleTime(DateUtils.getNowDate());
        log.setHandleOpinion(opinion);
        log.setCreateBy(operName);
        log.setCreateTime(DateUtils.getNowDate());
        issueMapper.insertLog(log);
    }
}
