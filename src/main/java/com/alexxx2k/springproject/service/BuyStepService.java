package com.alexxx2k.springproject.service;

import com.alexxx2k.springproject.domain.dto.BuyStep;
import com.alexxx2k.springproject.domain.entities.BuyStepEntity;
import com.alexxx2k.springproject.repository.BuyStepRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BuyStepService {

    private final BuyStepRepository buyStepRepository;

    public BuyStepService(BuyStepRepository buyStepRepository) {
        this.buyStepRepository = buyStepRepository;
    }

    public List<BuyStep> getAllBuySteps() {
        return buyStepRepository.findAll().stream()
                .map(this::toDomainBuyStep)
                .toList();
    }

    public Optional<BuyStep> getBuyStepById(Long id) {
        return buyStepRepository.findById(id)
                .map(this::toDomainBuyStep);
    }

    public List<BuyStep> getBuyStepsByStepId(Long stepId) {
        return buyStepRepository.findByStepId(stepId).stream()
                .map(this::toDomainBuyStep)
                .toList();
    }

    @Transactional
    public BuyStep createBuyStep(BuyStep buyStep) {
        if (buyStep.dateEnd() != null && buyStep.dateStart() != null &&
                buyStep.dateEnd().isBefore(buyStep.dateStart())) {
            throw new IllegalArgumentException("Дата окончания не может быть раньше даты начала");
        }

        BuyStepEntity entity = new BuyStepEntity(
                null,
                buyStep.stepId(),
                buyStep.dateStart(),
                buyStep.dateEnd()
        );

        BuyStepEntity savedEntity = buyStepRepository.save(entity);
        return toDomainBuyStep(savedEntity);
    }

    @Transactional
    public BuyStep updateBuyStep(Long id, BuyStep buyStep) {
        BuyStepEntity existingEntity = buyStepRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Шаг покупки с ID " + id + " не найден"));

        if (buyStep.dateEnd() != null && buyStep.dateStart() != null &&
                buyStep.dateEnd().isBefore(buyStep.dateStart())) {
            throw new IllegalArgumentException("Дата окончания не может быть раньше даты начала");
        }

        existingEntity.setStepId(buyStep.stepId());
        existingEntity.setDateStart(buyStep.dateStart());
        existingEntity.setDateEnd(buyStep.dateEnd());

        BuyStepEntity savedEntity = buyStepRepository.save(existingEntity);
        return toDomainBuyStep(savedEntity);
    }

    @Transactional
    public void deleteBuyStep(Long id) {
        if (!buyStepRepository.existsById(id)) {
            throw new IllegalArgumentException("Шаг покупки с ID " + id + " не найден");
        }
        buyStepRepository.deleteById(id);
    }

    public List<BuyStep> getActiveBuySteps() {
        LocalDate currentDate = LocalDate.now();
        return buyStepRepository.findActiveSteps(currentDate).stream()
                .map(this::toDomainBuyStep)
                .toList();
    }

    public List<BuyStep> getBuyStepsByDateRange(LocalDate startDate, LocalDate endDate) {
        return buyStepRepository.findByDateRange(startDate, endDate).stream()
                .map(this::toDomainBuyStep)
                .toList();
    }

    public boolean existsByStepId(Long stepId) {
        return buyStepRepository.existsByStepId(stepId);
    }

    private BuyStep toDomainBuyStep(BuyStepEntity entity) {
        return new BuyStep(
                entity.getId(),
                entity.getStepId(),
                entity.getDateStart(),
                entity.getDateEnd()
        );
    }
}
