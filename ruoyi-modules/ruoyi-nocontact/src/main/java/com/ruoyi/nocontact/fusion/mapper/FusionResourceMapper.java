package com.ruoyi.nocontact.fusion.mapper;

import com.ruoyi.nocontact.fusion.domain.FusionResource;
import java.util.List;

public interface FusionResourceMapper
{
    List<FusionResource> selectResourceList(FusionResource resource);

    FusionResource selectResourceById(Long resourceId);

    int insertResource(FusionResource resource);

    int updateResource(FusionResource resource);

    int deleteResourceByIds(Long[] resourceIds);
}
