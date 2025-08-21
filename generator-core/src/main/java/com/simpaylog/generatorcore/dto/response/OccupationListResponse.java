package com.simpaylog.generatorcore.dto.response;

import java.util.List;

public record OccupationListResponse(
    List<OccupationCategoryResponse> occupationsCategories
) {
}