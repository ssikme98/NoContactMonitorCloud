package com.ruoyi.nocontact.rectification.service.impl;

import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.nocontact.common.NocontactDataScopeHelper;
import com.ruoyi.nocontact.rectification.domain.RectificationIssue;
import com.ruoyi.nocontact.rectification.domain.RectificationLog;
import com.ruoyi.nocontact.rectification.mapper.RectificationIssueMapper;
import com.ruoyi.nocontact.support.domain.BusinessMessage;
import com.ruoyi.nocontact.support.service.IBusinessMessageService;
import com.ruoyi.nocontact.support.service.impl.BusinessMessageServiceImpl;
import com.ruoyi.nocontact.rectification.service.IRectificationIssueService;
import com.ruoyi.nocontact.warning.domain.WarningMessage;
import com.ruoyi.nocontact.warning.service.IWarningMessageService;
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

    @Autowired
    private IWarningMessageService warningMessageService;

    @Autowired
    private IBusinessMessageService businessMessageService;

    @Override
    public List<RectificationIssue> selectIssueList(RectificationIssue issue)
    {
        NocontactDataScopeHelper.applyDataScope(issue, "r", "dept_id", "r", "create_by");
        return issueMapper.selectIssueList(issue);
    }

    @Override
    public RectificationIssue selectIssueById(Long issueId)
    {
        RectificationIssue issue = selectScopedIssue(issueId);
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
        if (issue.getDeptId() == null)
        {
            issue.setDeptId(resolveCurrentDeptId());
        }
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
    @Transactional(rollbackFor = Exception.class)
    public int createFromWarning(Long messageId, RectificationIssue form, String operName)
    {
        WarningMessage warning = warningMessageService.selectMessageById(messageId);
        if (warning == null)
        {
            throw new ServiceException("预警消息不存在");
        }
        if ("closed".equals(warning.getMessageStatus()) || "ignored".equals(warning.getMessageStatus()))
        {
            throw new ServiceException("当前预警状态不允许转入整改");
        }
        RectificationIssue existing = issueMapper.selectActiveIssueBySourceWarningId(messageId);
        if (existing != null)
        {
            throw new ServiceException("该预警已存在未关闭的整改问题");
        }

        RectificationIssue issue = new RectificationIssue();
        issue.setIssueTitle(StringUtils.defaultIfBlank(form.getIssueTitle(),
                StringUtils.defaultIfBlank(warning.getIndicatorName(), "预警问题") + "整改"));
        issue.setSourceType("warning");
        issue.setSourceWarningId(messageId);
        issue.setDeptId(warning.getDeptId() == null ? resolveCurrentDeptId() : warning.getDeptId());
        issue.setWarningLevel(warning.getWarningLevel());
        issue.setRegionName(warning.getRegionName());
        issue.setResponsibleUnitName(warning.getResponsibleUnitName());
        issue.setResponsiblePerson(form.getResponsiblePerson());
        issue.setSupervisorName(form.getSupervisorName());
        issue.setDeadline(form.getDeadline());
        issue.setIssueStatus("pending_dispatch");
        issue.setIndicatorId(warning.getIndicatorId());
        issue.setIndicatorName(warning.getIndicatorName());
        issue.setCurrentValue(warning.getCurrentValue());
        issue.setThresholdValue(warning.getThresholdValue());
        issue.setIssueDescription(StringUtils.defaultIfBlank(form.getIssueDescription(), buildWarningIssueDescription(warning)));
        issue.setRectificationResult(form.getRectificationResult());
        issue.setReviewOpinion(form.getReviewOpinion());
        issue.setAttachmentUrl(form.getAttachmentUrl());
        issue.setRemark(form.getRemark());

        int rows = insertIssue(issue, operName);
        if ("pending".equals(warning.getMessageStatus()))
        {
            warningMessageService.updateMessageStatus(messageId, "processing", "已转入问题整改流程", operName);
        }
        return rows;
    }

    @Override
    public int updateIssue(RectificationIssue issue, String operName)
    {
        requireIssue(issue.getIssueId());
        issue.setUpdateBy(operName);
        issue.setUpdateTime(DateUtils.getNowDate());
        return issueMapper.updateIssue(issue);
    }

    @Override
    public int deleteIssueByIds(Long[] issueIds)
    {
        for (Long issueId : issueIds)
        {
            requireIssue(issueId);
        }
        return issueMapper.deleteIssueByIds(issueIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int dispatch(Long issueId, RectificationIssue form, String operName)
    {
        RectificationIssue issue = requireIssue(issueId);
        requireStatus(issue, "pending_dispatch", "rejected");
        form.setIssueId(issueId);
        form.setIssueStatus("rejected".equals(issue.getIssueStatus()) ? "rework" : "pending_rectification");
        form.setExpectedStatus(issue.getIssueStatus());
        form.setDispatchTime(DateUtils.getNowDate());
        form.setUpdateBy(operName);
        form.setUpdateTime(DateUtils.getNowDate());
        int rows = issueMapper.updateIssueStatus(form);
        ensureStatusUpdated(rows);
        insertLog(issueId, issue.getIssueStatus(), form.getIssueStatus(),
                "rejected".equals(issue.getIssueStatus()) ? "重新分配整改" : "分配整改",
                form.getReviewOpinion(), operName);
        createDispatchMessage(issueId, form, operName);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int start(Long issueId, String operName)
    {
        RectificationIssue issue = requireIssue(issueId);
        requireStatus(issue, "pending_rectification", "rework");
        RectificationIssue form = new RectificationIssue();
        form.setIssueId(issueId);
        form.setIssueStatus("rectifying");
        form.setExpectedStatus(issue.getIssueStatus());
        form.setUpdateBy(operName);
        form.setUpdateTime(DateUtils.getNowDate());
        int rows = issueMapper.updateIssueStatus(form);
        ensureStatusUpdated(rows);
        insertLog(issueId, issue.getIssueStatus(), "rectifying",
                "rework".equals(issue.getIssueStatus()) ? "开始返工整改" : "开始整改", null, operName);
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
        form.setExpectedStatus(issue.getIssueStatus());
        form.setSubmitTime(DateUtils.getNowDate());
        form.setUpdateBy(operName);
        form.setUpdateTime(DateUtils.getNowDate());
        int rows = issueMapper.updateIssueStatus(form);
        ensureStatusUpdated(rows);
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
        form.setIssueStatus(approved ? "closed" : "rejected");
        form.setExpectedStatus(issue.getIssueStatus());
        form.setReviewTime(DateUtils.getNowDate());
        form.setUpdateBy(operName);
        form.setUpdateTime(DateUtils.getNowDate());
        int rows = issueMapper.updateIssueStatus(form);
        ensureStatusUpdated(rows);
        if (approved && issue.getSourceWarningId() != null)
        {
            warningMessageService.closeFromRectification(issue.getSourceWarningId(),
                    StringUtils.defaultIfBlank(form.getReviewOpinion(), "整改审核通过，关闭预警"), operName);
        }
        insertLog(issueId, issue.getIssueStatus(), form.getIssueStatus(), approved ? "审核通过并关闭" : "审核驳回",
                form.getReviewOpinion(), operName);
        if (!approved)
        {
            createRejectMessage(issueId, form, operName);
        }
        return rows;
    }

    @Override
    public Map<String, Object> dashboard()
    {
        RectificationIssue scope = new RectificationIssue();
        NocontactDataScopeHelper.applyDataScope(scope, "r", "dept_id", "r", "create_by");
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("statusStats", issueMapper.selectStatusStats(scope));
        data.put("regionStats", issueMapper.selectRegionStats(scope));
        return data;
    }

    private RectificationIssue requireIssue(Long issueId)
    {
        RectificationIssue issue = selectScopedIssue(issueId);
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

    private void ensureStatusUpdated(int rows)
    {
        if (rows == 0)
        {
            throw new ServiceException("整改问题状态已变化，请刷新后重试");
        }
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

    private void createDispatchMessage(Long issueId, RectificationIssue form, String operName)
    {
        BusinessMessage message = new BusinessMessage();
        message.setMessageType(BusinessMessageServiceImpl.RECTIFICATION_DISPATCHED);
        message.setTitle("整改任务已派发");
        message.setContent("问题整改任务已派发给 " + StringUtils.defaultIfBlank(form.getResponsibleUnitName(), "责任单位"));
        message.setBusinessType("rectification");
        message.setBusinessId(issueId);
        message.setJumpTarget("/nocontact/rectification/issue?issueId=" + issueId);
        message.setReceiverUserName(resolveRectificationReceiver(form, operName));
        message.setCreateBy(operName);
        businessMessageService.createMessage(message);
    }

    private void createRejectMessage(Long issueId, RectificationIssue form, String operName)
    {
        BusinessMessage message = new BusinessMessage();
        message.setMessageType(BusinessMessageServiceImpl.AUDIT_REJECTED);
        message.setTitle("整改审核已驳回");
        message.setContent(StringUtils.defaultIfBlank(form.getReviewOpinion(), "整改结果需重新处理"));
        message.setBusinessType("rectification");
        message.setBusinessId(issueId);
        message.setJumpTarget("/nocontact/rectification/issue?issueId=" + issueId);
        message.setReceiverUserName(resolveRectificationReceiver(form, operName));
        message.setCreateBy(operName);
        businessMessageService.createMessage(message);
    }

    private String resolveRectificationReceiver(RectificationIssue form, String operName)
    {
        if (StringUtils.isNotBlank(form.getResponsiblePerson()))
        {
            return form.getResponsiblePerson();
        }
        if (StringUtils.isNotBlank(form.getSupervisorName()))
        {
            return form.getSupervisorName();
        }
        return operName;
    }

    private String buildWarningIssueDescription(WarningMessage warning)
    {
        StringBuilder description = new StringBuilder("来源预警：");
        description.append(StringUtils.defaultIfBlank(warning.getRuleName(), "未命名规则"));
        if (StringUtils.isNotBlank(warning.getIndicatorName()))
        {
            description.append("；指标：").append(warning.getIndicatorName());
        }
        if (warning.getCurrentValue() != null || warning.getThresholdValue() != null)
        {
            description.append("；当前值：").append(warning.getCurrentValue() == null ? "-" : warning.getCurrentValue());
            description.append("；阈值：").append(warning.getThresholdValue() == null ? "-" : warning.getThresholdValue());
        }
        if (StringUtils.isNotBlank(warning.getRegionName()))
        {
            description.append("；地区：").append(warning.getRegionName());
        }
        return description.toString();
    }

    private RectificationIssue selectScopedIssue(Long issueId)
    {
        RectificationIssue query = new RectificationIssue();
        query.setIssueId(issueId);
        NocontactDataScopeHelper.applyDataScope(query, "r", "dept_id", "r", "create_by");
        return issueMapper.selectIssueByScope(query);
    }

    private Long resolveCurrentDeptId()
    {
        if (SecurityUtils.getLoginUser() == null || SecurityUtils.getLoginUser().getSysUser() == null)
        {
            return null;
        }
        return SecurityUtils.getLoginUser().getSysUser().getDeptId();
    }
}
