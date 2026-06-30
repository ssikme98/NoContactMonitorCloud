package com.ruoyi.nocontact.rectification.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RectificationIssueJsonFormatTest
{
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deadlineAcceptsFrontendDatetimeFormat() throws Exception
    {
        RectificationIssue issue = objectMapper.readValue(
                "{\"issueTitle\":\"E2E整改问题\",\"deadline\":\"2026-07-16 12:00:00\"}",
                RectificationIssue.class);

        assertEquals("2026-07-16 12:00:00", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(issue.getDeadline()));
    }
}
