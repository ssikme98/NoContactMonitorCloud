package com.ruoyi.nocontact.warning.engine;

import com.ruoyi.nocontact.warning.domain.WarningMessage;
import java.util.ArrayList;
import java.util.List;

/**
 * 预警生成计划：新增和更新分开，便于服务层落库。
 */
public class WarningGenerationPlan
{
    private final List<WarningMessage> messagesToInsert = new ArrayList<WarningMessage>();
    private final List<WarningMessage> messagesToUpdate = new ArrayList<WarningMessage>();

    public List<WarningMessage> getMessagesToInsert()
    {
        return messagesToInsert;
    }

    public List<WarningMessage> getMessagesToUpdate()
    {
        return messagesToUpdate;
    }
}
