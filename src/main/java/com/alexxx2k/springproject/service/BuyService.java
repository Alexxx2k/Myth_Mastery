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

    // ========== СУЩЕСТВУЮЩИЕ МЕТОДЫ ==========

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

    // ========== НОВЫЕ МЕТОДЫ ДЛЯ РАБОТЫ СО СТАТУСАМИ ==========

    /**
     * Получить название шага по buyStepId
     */
    public String getStepNameByBuyStepId(Long buyStepId) {
        if (buyStepId == null) {
            return "Новый";
        }

        try {
            // 1. Найти BuyStep по ID
            Optional<BuyStep> buyStepOpt = buyStepService.getBuyStepById(buyStepId);
            if (buyStepOpt.isEmpty()) {
                return "Неизвестный шаг #" + buyStepId;
            }

            // 2. Из BuyStep получить stepId
            Long stepId = buyStepOpt.get().stepId();

            // 3. Найти Step по stepId и получить название
            Optional<Step> stepOpt = stepService.getStepById(stepId);
            if (stepOpt.isEmpty()) {
                return "Шаг #" + stepId;
            }

            // 4. Вернуть название шага
            return stepOpt.get().name();

        } catch (Exception e) {
            return "Ошибка получения статуса";
        }
    }

    /**
     * Получить или создать BuyStep для выбранного Step
     * @param stepId ID шага из таблицы step
     * @return buyStepId для использования в заказе
     */
    public Long getOrCreateBuyStepIdForStep(Long stepId) {
        if (stepId == null) {
            return null;
        }

        // Ищем существующий BuyStep с этим stepId
        List<BuyStep> existingBuySteps = buyStepService.getBuyStepsByStepId(stepId);

        if (!existingBuySteps.isEmpty()) {
            // Берем первый подходящий
            return existingBuySteps.get(0).id();
        } else {
            // Создаем новый BuyStep
            BuyStep newBuyStep = buyStepService.createBuyStep(
                    new BuyStep(null, stepId, null, null)
            );
            return newBuyStep.id();
        }
    }
}