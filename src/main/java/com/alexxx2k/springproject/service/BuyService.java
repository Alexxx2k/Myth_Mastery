package com.alexxx2k.springproject.service;

import com.alexxx2k.springproject.domain.dto.Buy;
import com.alexxx2k.springproject.domain.dto.BuyStep;
import com.alexxx2k.springproject.domain.dto.Step;
import com.alexxx2k.springproject.domain.entities.BuyEntity;
import com.alexxx2k.springproject.repository.BuyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BuyService {

    private final BuyRepository buyRepository;
    private final CustomerService customerService;
    private final BuyStepService buyStepService;
    private final StepService stepService;

    public BuyService(BuyRepository buyRepository,
                      CustomerService customerService,
                      BuyStepService buyStepService,
                      StepService stepService) {
        this.buyRepository = buyRepository;
        this.customerService = customerService;
        this.buyStepService = buyStepService;
        this.stepService = stepService;
    }

    public List<Buy> getAllBuys() {
        return buyRepository.findAll().stream()
                .map(this::toDomainBuy)
                .toList();
    }

    public Optional<Buy> getBuyById(Long id) {
        return buyRepository.findById(id)
                .map(this::toDomainBuy);
    }

    @Transactional
    public Buy createBuy(Long customerId, String description, Long buyStepId) {
        BuyEntity buy = new BuyEntity();
        buy.setCustomerId(customerId);
        buy.setDescription(description);
        buy.setBuyStepId(buyStepId);

        BuyEntity savedBuy = buyRepository.save(buy);
        return toDomainBuy(savedBuy);
    }

    @Transactional
    public Buy updateBuy(Long id, Long customerId, String description, Long buyStepId) {
        BuyEntity buy = buyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Заказ с ID " + id + " не найден"));

        buy.setCustomerId(customerId);
        buy.setDescription(description);
        buy.setBuyStepId(buyStepId);

        BuyEntity savedBuy = buyRepository.save(buy);
        return toDomainBuy(savedBuy);
    }

    @Transactional
    public void deleteBuy(Long id) {
        if (!buyRepository.existsById(id)) {
            throw new IllegalArgumentException("Заказ с ID " + id + " не найден");
        }
        buyRepository.deleteById(id);
    }

    private Buy toDomainBuy(BuyEntity entity) {
        return new Buy(
                entity.getId(),
                entity.getCustomerId(),
                entity.getBuyStepId(),
                entity.getDescription()
        );
    }

    public String getStepNameByBuyStepId(Long buyStepId) {
        if (buyStepId == null) {
            return "Новый";
        }

        try {
            Optional<BuyStep> buyStepOpt = buyStepService.getBuyStepById(buyStepId);
            if (buyStepOpt.isEmpty()) {
                return "Неизвестный шаг #" + buyStepId;
            }

            Long stepId = buyStepOpt.get().stepId();

            Optional<Step> stepOpt = stepService.getStepById(stepId);
            if (stepOpt.isEmpty()) {
                return "Шаг #" + stepId;
            }

            return stepOpt.get().name();

        } catch (Exception e) {
            return "Ошибка получения статуса";
        }
    }

    public Long getOrCreateBuyStepIdForStep(Long stepId) {
        if (stepId == null) {
            return null;
        }

        List<BuyStep> existingBuySteps = buyStepService.getBuyStepsByStepId(stepId);

        if (!existingBuySteps.isEmpty()) {
            return existingBuySteps.get(0).id();
        } else {
            BuyStep newBuyStep = buyStepService.createBuyStep(
                    new BuyStep(null, stepId, null, null)
            );
            return newBuyStep.id();
        }
    }
}
