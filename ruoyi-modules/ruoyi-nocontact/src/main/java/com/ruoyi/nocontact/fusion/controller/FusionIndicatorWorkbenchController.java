package com.ruoyi.nocontact.fusion.controller;

import com.ruoyi.common.core.utils.DateUtils;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.annotation.RequiresPermissions;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.nocontact.fusion.domain.FusionIndicatorTag;
import com.ruoyi.nocontact.fusion.mapper.FusionIndicatorTagMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 指标体系辅助功能Controller
 */
@RestController
@RequestMapping("/fusion/indicator/workbench")
public class FusionIndicatorWorkbenchController extends BaseController
{
    @Autowired
    private FusionIndicatorTagMapper tagMapper;

    @RequiresPermissions("nocontact:fusion:indicator:list")
    @GetMapping("/algorithms")
    public AjaxResult algorithms()
    {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        addAlgorithm(list, "deduct", "扣分法", "低于或超过目标时按差值扣分，适合底线型指标");
        addAlgorithm(list, "efficacy", "功效系数法", "按基准值、目标值区间换算得分，适合连续型指标");
        addAlgorithm(list, "composite", "综合指数法", "多个维度按权重汇总，适合综合评价");
        addAlgorithm(list, "rank", "排名赋分法", "按地区或单位排名档位给分，适合横向比较");
        addAlgorithm(list, "threshold", "阈值达标法", "达到阈值得满分，未达标按规则扣分");
        return success(list);
    }

    @RequiresPermissions("nocontact:fusion:indicator:list")
    @PostMapping("/simulate")
    public AjaxResult simulate(@RequestBody Map<String, Object> body)
    {
        String algorithmType = String.valueOf(body.get("algorithmType"));
        BigDecimal current = decimal(body.get("currentValue"), BigDecimal.ZERO);
        BigDecimal target = decimal(body.get("targetValue"), new BigDecimal("100"));
        BigDecimal baseline = decimal(body.get("baselineValue"), BigDecimal.ZERO);
        BigDecimal fullScore = decimal(body.get("fullScore"), new BigDecimal("100"));
        BigDecimal score;
        if ("deduct".equals(algorithmType))
        {
            score = fullScore.subtract(target.subtract(current).abs());
        }
        else if ("efficacy".equals(algorithmType))
        {
            BigDecimal range = target.subtract(baseline);
            score = range.compareTo(BigDecimal.ZERO) == 0 ? fullScore : current.subtract(baseline).multiply(fullScore).divide(range, 2, RoundingMode.HALF_UP);
        }
        else if ("threshold".equals(algorithmType))
        {
            score = current.compareTo(target) >= 0 ? fullScore : BigDecimal.ZERO;
        }
        else
        {
            score = current.min(fullScore);
        }
        if (score.compareTo(BigDecimal.ZERO) < 0)
        {
            score = BigDecimal.ZERO;
        }
        if (score.compareTo(fullScore) > 0)
        {
            score = fullScore;
        }
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("score", score.setScale(2, RoundingMode.HALF_UP));
        result.put("algorithmType", algorithmType);
        return success(result);
    }

    @RequiresPermissions("nocontact:fusion:indicator:list")
    @GetMapping("/tag/list")
    public TableDataInfo tagList(FusionIndicatorTag tag)
    {
        startPage();
        return getDataTable(tagMapper.selectTagList(tag));
    }

    @RequiresPermissions("nocontact:fusion:indicator:query")
    @GetMapping("/tag/{tagId}")
    public AjaxResult tagInfo(@PathVariable Long tagId)
    {
        return success(tagMapper.selectTagById(tagId));
    }

    @RequiresPermissions("nocontact:fusion:indicator:add")
    @Log(title = "指标标签", businessType = BusinessType.INSERT)
    @PostMapping("/tag")
    public AjaxResult addTag(@Valid @RequestBody FusionIndicatorTag tag)
    {
        tag.setCreateBy(SecurityUtils.getUsername());
        tag.setCreateTime(DateUtils.getNowDate());
        return toAjax(tagMapper.insertTag(tag));
    }

    @RequiresPermissions("nocontact:fusion:indicator:edit")
    @Log(title = "指标标签", businessType = BusinessType.UPDATE)
    @PutMapping("/tag")
    public AjaxResult editTag(@Valid @RequestBody FusionIndicatorTag tag)
    {
        tag.setUpdateBy(SecurityUtils.getUsername());
        tag.setUpdateTime(DateUtils.getNowDate());
        return toAjax(tagMapper.updateTag(tag));
    }

    @RequiresPermissions("nocontact:fusion:indicator:remove")
    @Log(title = "指标标签", businessType = BusinessType.DELETE)
    @DeleteMapping("/tag/{tagIds}")
    public AjaxResult removeTag(@PathVariable Long[] tagIds)
    {
        return toAjax(tagMapper.deleteTagByIds(tagIds));
    }

    private void addAlgorithm(List<Map<String, Object>> list, String type, String name, String description)
    {
        Map<String, Object> algorithm = new HashMap<String, Object>();
        algorithm.put("algorithmType", type);
        algorithm.put("algorithmName", name);
        algorithm.put("description", description);
        list.add(algorithm);
    }

    private BigDecimal decimal(Object value, BigDecimal defaultValue)
    {
        if (value == null || "".equals(String.valueOf(value)))
        {
            return defaultValue;
        }
        return new BigDecimal(String.valueOf(value));
    }
}
