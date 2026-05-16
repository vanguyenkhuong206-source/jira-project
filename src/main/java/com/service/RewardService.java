// RewardService.java
package com.service;

import com.entity.Reward;
import com.entity.User;
import com.entity.WasteReport;
import java.util.List;

public interface RewardService {
    Reward addPoints(User user, WasteReport report, int points, String description);
    List<Reward> getUserRewards(Long userId);
    int calculatePoints(WasteReport report);
}