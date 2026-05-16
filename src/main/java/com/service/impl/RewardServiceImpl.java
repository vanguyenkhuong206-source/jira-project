package com.service.impl;

import com.entity.Reward;
import com.entity.User;
import com.entity.WasteReport;
import com.repository.RewardRepository;
import com.service.RewardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RewardServiceImpl implements RewardService {

    private final RewardRepository rewardRepository;

    public RewardServiceImpl(RewardRepository rewardRepository) {
        this.rewardRepository = rewardRepository;
    }

    @Override
    public Reward addPoints(User user, WasteReport report, int points, String description) {
        Reward reward = new Reward();
        reward.setUser(user);
        reward.setWasteReport(report);
        reward.setPoints(points);
        reward.setType("EARNED");
        reward.setDescription(description);
        return rewardRepository.save(reward);
    }

    @Override
    public List<Reward> getUserRewards(Long userId) {
        return rewardRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public int calculatePoints(WasteReport report) {
        int base = report.getWasteType().getBasePoints();
        double qty = report.getQuantityKg() != null ? report.getQuantityKg() : 1.0;
        return Math.min((int)(base + qty * 2), 200);
    }
}