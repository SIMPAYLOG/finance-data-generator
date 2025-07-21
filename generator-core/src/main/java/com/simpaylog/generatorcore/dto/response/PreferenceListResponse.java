package com.simpaylog.generatorcore.dto.response;

import java.util.List;

public record PreferenceListResponse(
        List<PreferenceResponse> preferences
) {
}