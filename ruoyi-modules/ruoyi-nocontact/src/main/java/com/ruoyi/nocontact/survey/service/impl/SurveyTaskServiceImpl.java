package com.ruoyi.nocontact.survey.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.core.utils.uuid.IdUtils;
import com.ruoyi.nocontact.common.NocontactDataScopeHelper;
import com.ruoyi.nocontact.support.domain.BusinessMessage;
import com.ruoyi.nocontact.support.service.IBusinessMessageService;
import com.ruoyi.nocontact.support.service.impl.BusinessMessageServiceImpl;
import com.ruoyi.nocontact.survey.domain.SurveyEnterprise;
import com.ruoyi.nocontact.survey.domain.SurveyQuestionnaire;
import com.ruoyi.nocontact.survey.domain.SurveyTask;
import com.ruoyi.nocontact.survey.domain.SurveyTaskSample;
import com.ruoyi.nocontact.survey.domain.SurveyTaskSendRecord;
import com.ruoyi.nocontact.survey.domain.vo.SurveyTaskRegionStat;
import com.ruoyi.nocontact.survey.domain.vo.SurveyTaskResponseTrend;
import com.ruoyi.nocontact.survey.domain.vo.SurveyTaskTrackingDetail;
import com.ruoyi.nocontact.survey.domain.vo.SurveyTaskTrackingSummary;
import com.ruoyi.nocontact.survey.mapper.SurveyQuestionnaireMapper;
import com.ruoyi.nocontact.survey.mapper.SurveyTaskMapper;
import com.ruoyi.nocontact.survey.service.ISurveyTaskService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 调研任务Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class SurveyTaskServiceImpl implements ISurveyTaskService
{
    private static final String QUESTIONNAIRE_STATUS_PUBLISHED = "1";
    private static final String QUESTIONNAIRE_STATUS_COLLECTING = "3";
    private static final String TASK_STATUS_SAMPLED = "1";
    private static final String TASK_STATUS_DISPATCHED = "2";
    private static final String SAMPLE_STATUS_PENDING = "0";

    private static final String SOURCE_ALL = "all";
    private static final String SOURCE_GROUP = "group";
    private static final String SOURCE_ENTERPRISE = "enterprise";

    private static final String SAMPLING_RANDOM = "random";
    private static final String SAMPLING_STRATIFIED = "stratified";
    private static final String SAMPLING_SPECIFIED = "specified";

    private static final String CHANNEL_SMS = "sms";
    private static final String CHANNEL_SITE = "site";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private SurveyTaskMapper taskMapper;

    @Autowired
    private SurveyQuestionnaireMapper questionnaireMapper;

    @Autowired
    private IBusinessMessageService businessMessageService;

    @Override
    public List<SurveyTask> selectTaskList(SurveyTask task)
    {
        NocontactDataScopeHelper.applyDataScope(task, "u", "dept_id", "t", "create_by");
        return taskMapper.selectTaskList(task);
    }

    @Override
    public SurveyTask selectTaskById(Long taskId)
    {
        SurveyTask task = selectScopedTask(taskId);
        if (task != null)
        {
            task.setSendChannels(splitChannels(task.getSendChannelsText()));
            task.setSamples(taskMapper.selectSamplesByTaskId(taskId));
            task.setSendRecords(taskMapper.selectSendRecordsByTaskId(taskId));
        }
        return task;
    }

    @Override
    public SurveyTaskSample selectSampleById(Long sampleId)
    {
        SurveyTaskSample sample = taskMapper.selectSampleById(sampleId);
        if (sample == null)
        {
            return null;
        }
        return selectScopedTask(sample.getTaskId()) == null ? null : sample;
    }

    @Override
    public SurveyTaskTrackingSummary selectTrackingSummary(Long taskId)
    {
        requireTask(taskId);
        return taskMapper.selectTrackingSummary(taskId);
    }

    @Override
    public List<SurveyTaskRegionStat> selectTrackingRegionStats(Long taskId)
    {
        requireTask(taskId);
        return taskMapper.selectTrackingRegionStats(taskId);
    }

    @Override
    public List<SurveyTaskTrackingDetail> selectTrackingDetailList(SurveyTaskTrackingDetail detail)
    {
        requireTask(detail.getTaskId());
        return taskMapper.selectTrackingDetailList(detail);
    }

    @Override
    public List<SurveyTaskResponseTrend> selectTrackingTrend(Long taskId)
    {
        requireTask(taskId);
        return taskMapper.selectTrackingTrend(taskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertTask(SurveyTask task)
    {
        validateTask(task);
        task.setSendChannels(normalizeChannels(task.getSendChannels()));
        task.setSendChannelsText(StringUtils.join(task.getSendChannels(), ","));
        task.setStatus(TASK_STATUS_SAMPLED);
        Date now = DateUtils.getNowDate();
        task.setCreateTime(now);
        task.setSamplingBatchNo(buildSamplingBatchNo(now));
        task.setSamplingBatchTime(now);
        List<SurveyEnterprise> selected = selectSamples(task, taskMapper.selectEnterprisePool(task));
        if (selected.isEmpty())
        {
            throw new ServiceException("调研对象池为空，无法生成任务样本");
        }
        task.setSamplingFilterSnapshot(buildSamplingSnapshot(task, selected));
        int rows = taskMapper.insertTask(task);
        Date expireTime = DateUtils.addHours(DateUtils.getNowDate(), task.getTokenExpireHours() == null ? 168 : task.getTokenExpireHours());
        for (SurveyEnterprise enterprise : selected)
        {
            taskMapper.insertTaskSample(buildSample(task, enterprise, expireTime));
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int dispatchTask(Long taskId, String operName)
    {
        SurveyTask task = requireTask(taskId);
        if (!TASK_STATUS_SAMPLED.equals(task.getStatus()))
        {
            throw new ServiceException("只有已抽样任务允许发卷，当前状态不允许此操作");
        }
        List<SurveyTaskSample> samples = task.getSamples();
        if (StringUtils.isEmpty(samples))
        {
            throw new ServiceException("调研任务没有样本");
        }
        taskMapper.deleteSendRecordsByTaskId(taskId);
        List<SurveyTaskSendRecord> records = new ArrayList<SurveyTaskSendRecord>();
        String[] channels = normalizeChannels(task.getSendChannels());
        for (SurveyTaskSample sample : samples)
        {
            for (String channel : channels)
            {
                records.add(buildSendRecord(task, sample, channel));
            }
        }
        if (!records.isEmpty())
        {
            taskMapper.batchSendRecord(records);
        }
        taskMapper.updateSamplesSent(taskId);
        SurveyTask update = new SurveyTask();
        update.setTaskId(taskId);
        update.setStatus(TASK_STATUS_DISPATCHED);
        update.setExpectedStatus(task.getStatus());
        update.setDispatchTime(DateUtils.getNowDate());
        update.setUpdateBy(operName);
        update.setUpdateTime(DateUtils.getNowDate());
        int rows = taskMapper.updateTaskStatus(update);
        if (rows == 0)
        {
            throw new ServiceException("调研任务状态已变化，请刷新后重试");
        }
        promoteQuestionnaireToCollecting(task.getQuestionnaireId(), operName);
        createDispatchMessage(task, operName);
        return rows;
    }

    @Override
    public int deleteTaskByIds(Long[] taskIds)
    {
        for (Long taskId : taskIds)
        {
            requireTask(taskId);
        }
        return taskMapper.deleteTaskByIds(taskIds);
    }

    private void validateTask(SurveyTask task)
    {
        SurveyQuestionnaire questionnaire = questionnaireMapper.selectQuestionnaireById(task.getQuestionnaireId());
        if (questionnaire == null || (!QUESTIONNAIRE_STATUS_PUBLISHED.equals(questionnaire.getStatus())
                && !QUESTIONNAIRE_STATUS_COLLECTING.equals(questionnaire.getStatus())))
        {
            throw new ServiceException("调研任务只能绑定已发布或收集中问卷版本");
        }
        task.setSampleSource(StringUtils.defaultIfBlank(task.getSampleSource(), SOURCE_ALL));
        task.setSamplingMethod(StringUtils.defaultIfBlank(task.getSamplingMethod(), SAMPLING_RANDOM));
        if (SOURCE_GROUP.equals(task.getSampleSource()) && task.getGroupId() == null)
        {
            throw new ServiceException("按企业分组创建对象池时分组不能为空");
        }
        if (SOURCE_ENTERPRISE.equals(task.getSampleSource()) && ArrayUtils.isEmpty(task.getEnterpriseIds()))
        {
            throw new ServiceException("指定企业对象池不能为空");
        }
        if (!SAMPLING_SPECIFIED.equals(task.getSamplingMethod()) && (task.getSampleSize() == null || task.getSampleSize() <= 0))
        {
            throw new ServiceException("随机或分层抽样时样本数量必须大于0");
        }
        if (task.getTokenExpireHours() == null || task.getTokenExpireHours() <= 0)
        {
            task.setTokenExpireHours(168);
        }
    }

    private List<SurveyEnterprise> selectSamples(SurveyTask task, List<SurveyEnterprise> pool)
    {
        if (StringUtils.isEmpty(pool))
        {
            return new ArrayList<SurveyEnterprise>();
        }
        if (SAMPLING_SPECIFIED.equals(task.getSamplingMethod()) || SOURCE_ENTERPRISE.equals(task.getSampleSource()))
        {
            return new ArrayList<SurveyEnterprise>(pool);
        }
        if (SAMPLING_STRATIFIED.equals(task.getSamplingMethod()))
        {
            return stratifiedSample(pool, task.getSampleSize());
        }
        List<SurveyEnterprise> randomPool = new ArrayList<SurveyEnterprise>(pool);
        Collections.shuffle(randomPool);
        return randomPool.subList(0, Math.min(task.getSampleSize(), randomPool.size()));
    }

    private void createDispatchMessage(SurveyTask task, String operName)
    {
        BusinessMessage message = new BusinessMessage();
        message.setMessageType(BusinessMessageServiceImpl.SURVEY_DISPATCHED);
        message.setTitle("问卷任务已发放");
        message.setContent("调研任务“" + task.getTaskName() + "”已生成发送记录");
        message.setBusinessType("survey");
        message.setBusinessId(task.getTaskId());
        message.setJumpTarget("/survey/tracking?taskId=" + task.getTaskId());
        message.setReceiverUserName(operName);
        message.setCreateBy(operName);
        if (businessMessageService.createMessage(message) <= 0)
        {
            throw new ServiceException("问卷发卷消息创建失败");
        }
    }

    private void promoteQuestionnaireToCollecting(Long questionnaireId, String operName)
    {
        SurveyQuestionnaire update = new SurveyQuestionnaire();
        update.setQuestionnaireId(questionnaireId);
        update.setCurrentStatus(QUESTIONNAIRE_STATUS_PUBLISHED);
        update.setStatus(QUESTIONNAIRE_STATUS_COLLECTING);
        update.setUpdateBy(operName);
        update.setUpdateTime(DateUtils.getNowDate());
        questionnaireMapper.updateQuestionnaireStatus(update);
    }

    private String buildSamplingBatchNo(Date now)
    {
        return "TASK-" + DateUtils.dateTimeNow(DateUtils.YYYYMMDDHHMMSS) + "-" + IdUtils.fastSimpleUUID().substring(0, 6).toUpperCase();
    }

    private String buildSamplingSnapshot(SurveyTask task, List<SurveyEnterprise> selected)
    {
        Map<String, Object> snapshot = new LinkedHashMap<String, Object>();
        snapshot.put("sampleSource", task.getSampleSource());
        snapshot.put("samplingMethod", task.getSamplingMethod());
        snapshot.put("sampleSize", task.getSampleSize());
        snapshot.put("groupId", task.getGroupId());
        snapshot.put("enterpriseIds", task.getEnterpriseIds());
        snapshot.put("selectedEnterpriseIds", selected.stream().map(SurveyEnterprise::getEnterpriseId).collect(Collectors.toList()));
        snapshot.put("selectedEnterprises", selected.stream().map(this::samplingSnapshotItem).collect(Collectors.toList()));
        try
        {
            return objectMapper.writeValueAsString(snapshot);
        }
        catch (JsonProcessingException e)
        {
            throw new ServiceException("抽样筛选快照序列化失败");
        }
    }

    private Map<String, Object> samplingSnapshotItem(SurveyEnterprise enterprise)
    {
        Map<String, Object> row = new LinkedHashMap<String, Object>();
        row.put("enterpriseId", enterprise.getEnterpriseId());
        row.put("enterpriseName", enterprise.getEnterpriseName());
        row.put("creditCode", enterprise.getCreditCode());
        row.put("regionName", enterprise.getRegionName());
        row.put("industryCategory", enterprise.getIndustryCategory());
        row.put("enterpriseScale", enterprise.getEnterpriseScale());
        return row;
    }

    private List<SurveyEnterprise> stratifiedSample(List<SurveyEnterprise> pool, int sampleSize)
    {
        Map<String, List<SurveyEnterprise>> strata = pool.stream().collect(Collectors.groupingBy(this::strataKey, LinkedHashMap::new, Collectors.toList()));
        List<SurveyEnterprise> selected = new ArrayList<SurveyEnterprise>();
        for (List<SurveyEnterprise> group : strata.values())
        {
            Collections.shuffle(group);
            int count = Math.max(1, Math.round((float) group.size() * sampleSize / pool.size()));
            selected.addAll(group.subList(0, Math.min(count, group.size())));
        }
        if (selected.size() > sampleSize)
        {
            Collections.shuffle(selected);
            selected = new ArrayList<SurveyEnterprise>(selected.subList(0, sampleSize));
        }
        if (selected.size() < sampleSize)
        {
            Set<Long> selectedIds = selected.stream().map(SurveyEnterprise::getEnterpriseId).collect(Collectors.toSet());
            List<SurveyEnterprise> remaining = pool.stream()
                    .filter(item -> !selectedIds.contains(item.getEnterpriseId()))
                    .sorted(Comparator.comparing(SurveyEnterprise::getEnterpriseId))
                    .collect(Collectors.toList());
            for (SurveyEnterprise enterprise : remaining)
            {
                if (selected.size() >= sampleSize)
                {
                    break;
                }
                selected.add(enterprise);
            }
        }
        return selected;
    }

    private String strataKey(SurveyEnterprise enterprise)
    {
        return StringUtils.defaultString(enterprise.getRegionName()) + "|"
                + StringUtils.defaultString(enterprise.getIndustryCategory()) + "|"
                + StringUtils.defaultString(enterprise.getEnterpriseScale());
    }

    private SurveyTaskSample buildSample(SurveyTask task, SurveyEnterprise enterprise, Date expireTime)
    {
        String token = IdUtils.fastUUID();
        SurveyTaskSample sample = new SurveyTaskSample();
        sample.setTaskId(task.getTaskId());
        sample.setEnterpriseId(enterprise.getEnterpriseId());
        sample.setEnterpriseName(enterprise.getEnterpriseName());
        sample.setCreditCode(enterprise.getCreditCode());
        sample.setRegionName(enterprise.getRegionName());
        sample.setIndustryCategory(enterprise.getIndustryCategory());
        sample.setEnterpriseScale(enterprise.getEnterpriseScale());
        sample.setContactPhone(enterprise.getContactPhone());
        sample.setToken(token);
        sample.setTokenExpireTime(expireTime);
        sample.setQrContent("/survey/fill?token=" + token);
        sample.setStatus(SAMPLE_STATUS_PENDING);
        sample.setCreateTime(DateUtils.getNowDate());
        return sample;
    }

    private SurveyTaskSendRecord buildSendRecord(SurveyTask task, SurveyTaskSample sample, String channel)
    {
        SurveyTaskSendRecord record = new SurveyTaskSendRecord();
        record.setTaskId(task.getTaskId());
        record.setSampleId(sample.getSampleId());
        record.setEnterpriseId(sample.getEnterpriseId());
        record.setChannel(channel);
        record.setReceiver(CHANNEL_SMS.equals(channel) ? sample.getContactPhone() : sample.getEnterpriseName());
        record.setContent("请通过问卷链接完成调研：" + sample.getQrContent());
        record.setSendStatus("0");
        record.setSubmitStatus("0");
        record.setCreateTime(DateUtils.getNowDate());
        return record;
    }

    private String[] normalizeChannels(String[] channels)
    {
        if (ArrayUtils.isEmpty(channels))
        {
            return new String[] { CHANNEL_SMS, CHANNEL_SITE };
        }
        Set<String> rows = new HashSet<String>();
        for (String channel : channels)
        {
            if (CHANNEL_SMS.equals(channel) || CHANNEL_SITE.equals(channel))
            {
                rows.add(channel);
            }
            else
            {
                throw new ServiceException("不支持的发送渠道：" + channel + "，可选值：sms, site");
            }
        }
        if (rows.isEmpty())
        {
            throw new ServiceException("发送渠道不能为空");
        }
        return rows.toArray(new String[0]);
    }

    private String[] splitChannels(String channelsText)
    {
        if (StringUtils.isBlank(channelsText))
        {
            return new String[] { CHANNEL_SMS, CHANNEL_SITE };
        }
        Set<String> rows = new HashSet<String>();
        for (String channel : channelsText.split(","))
        {
            String trimmed = channel.trim();
            if (CHANNEL_SMS.equals(trimmed) || CHANNEL_SITE.equals(trimmed))
            {
                rows.add(trimmed);
            }
        }
        return rows.isEmpty() ? new String[] { CHANNEL_SMS, CHANNEL_SITE } : rows.toArray(new String[0]);
    }

    private SurveyTask selectScopedTask(Long taskId)
    {
        SurveyTask query = new SurveyTask();
        query.setTaskId(taskId);
        NocontactDataScopeHelper.applyDataScope(query, "u", "dept_id", "t", "create_by");
        return taskMapper.selectTaskByScope(query);
    }

    private SurveyTask requireTask(Long taskId)
    {
        SurveyTask task = selectTaskById(taskId);
        if (task == null)
        {
            throw new ServiceException("调研任务不存在");
        }
        return task;
    }
}
