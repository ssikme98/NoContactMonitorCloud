package com.ruoyi.job.task;

import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.domain.R;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.job.nocontact.RemoteNocontactWarningService;
import java.time.LocalDate;
import java.time.YearMonth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 营商无感预警定时任务。
 */
@Component("nocontactWarningTask")
public class NocontactWarningTask
{
    @Autowired
    private RemoteNocontactWarningService warningService;

    public void evaluateDueRules()
    {
        LocalDate today = LocalDate.now();
        evaluateScheduledRules(previousMonth(today));
        if (isQuarterDue(today))
        {
            evaluateScheduledRules(previousQuarter(today));
        }
        if (isYearDue(today))
        {
            evaluateScheduledRules(String.valueOf(today.getYear() - 1));
        }
    }

    public void evaluateScheduledRules(String periodKey)
    {
        R<Integer> result = warningService.evaluateScheduledRules(periodKey, SecurityConstants.INNER);
        if (result == null || R.isError(result))
        {
            throw new ServiceException(result == null ? "营商无感预警调度无响应" : result.getMsg());
        }
    }

    private String previousMonth(LocalDate today)
    {
        return YearMonth.from(today).minusMonths(1).toString();
    }

    private String previousQuarter(LocalDate today)
    {
        int quarter = (today.getMonthValue() - 1) / 3 + 1;
        int previousQuarter = quarter - 1;
        int year = today.getYear();
        if (previousQuarter == 0)
        {
            previousQuarter = 4;
            year--;
        }
        return year + "-Q" + previousQuarter;
    }

    private boolean isQuarterDue(LocalDate today)
    {
        return today.getMonthValue() == 1 || today.getMonthValue() == 4 || today.getMonthValue() == 7
                || today.getMonthValue() == 10;
    }

    private boolean isYearDue(LocalDate today)
    {
        return today.getMonthValue() == 1;
    }
}
