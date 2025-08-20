package com.simpaylog.generatorcore.service;

import com.simpaylog.generatorcore.dto.FixedObligation;
import com.simpaylog.generatorcore.dto.UserProfile;

import java.time.LocalDate;
import java.util.List;

public interface FixedObligationPresetProvider {
    List<FixedObligation> generate(UserProfile profile, LocalDate anchorDate);
}
