package com.alexxx2k.springproject.service;

import com.alexxx2k.springproject.domain.dto.Buy;
import com.alexxx2k.springproject.domain.entities.BuyEntity;
import com.alexxx2k.springproject.repository.BuyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BuyService {

    private final BuyRepository buyRepository;

    public BuyService(BuyRepository buyRepository) {
        this.buyRepository = buyRepository;
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
}
