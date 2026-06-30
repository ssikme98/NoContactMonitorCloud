package com.ruoyi.nocontact.support.domain;

/**
 * 公共待办汇总项
 */
public class SupportTodoItem
{
    private String todoType;
    private String todoLabel;
    private Integer todoCount;
    private String jumpTarget;

    public SupportTodoItem()
    {
    }

    public SupportTodoItem(String todoType, String todoLabel, Integer todoCount, String jumpTarget)
    {
        this.todoType = todoType;
        this.todoLabel = todoLabel;
        this.todoCount = todoCount;
        this.jumpTarget = jumpTarget;
    }

    public String getTodoType()
    {
        return todoType;
    }

    public void setTodoType(String todoType)
    {
        this.todoType = todoType;
    }

    public String getTodoLabel()
    {
        return todoLabel;
    }

    public void setTodoLabel(String todoLabel)
    {
        this.todoLabel = todoLabel;
    }

    public Integer getTodoCount()
    {
        return todoCount;
    }

    public void setTodoCount(Integer todoCount)
    {
        this.todoCount = todoCount;
    }

    public String getJumpTarget()
    {
        return jumpTarget;
    }

    public void setJumpTarget(String jumpTarget)
    {
        this.jumpTarget = jumpTarget;
    }
}
