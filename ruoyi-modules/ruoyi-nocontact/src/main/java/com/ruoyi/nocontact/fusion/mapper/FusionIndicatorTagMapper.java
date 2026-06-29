package com.ruoyi.nocontact.fusion.mapper;

import com.ruoyi.nocontact.fusion.domain.FusionIndicatorTag;
import java.util.List;

public interface FusionIndicatorTagMapper
{
    List<FusionIndicatorTag> selectTagList(FusionIndicatorTag tag);

    FusionIndicatorTag selectTagById(Long tagId);

    int insertTag(FusionIndicatorTag tag);

    int updateTag(FusionIndicatorTag tag);

    int deleteTagByIds(Long[] tagIds);
}
