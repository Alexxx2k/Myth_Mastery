package com.alexxx2k.springproject.service;

import com.alexxx2k.springproject.domain.dto.BuyProduct;
import com.alexxx2k.springproject.domain.entities.BuyProductEntity;
import com.alexxx2k.springproject.domain.entities.BuyEntity;
import com.alexxx2k.springproject.domain.entities.ProductEntity;
import com.alexxx2k.springproject.repository.BuyProductRepository;
import com.alexxx2k.springproject.repository.BuyRepository;
import com.alexxx2k.springproject.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class BuyProductService {

    private final BuyProductRepository buyProductRepository;
    private final BuyRepository buyRepository;
    private final ProductRepository productRepository;

    public BuyProductService(BuyProductRepository buyProductRepository,
                             BuyRepository buyRepository,
                             ProductRepository productRepository) {
        this.buyProductRepository = buyProductRepository;
        this.buyRepository = buyRepository;
        this.productRepository = productRepository;
    }

    public List<BuyProduct> getAllBuyProducts() {
        return buyProductRepository.findAllWithDetails().stream()
                .map(this::toDomainBuyProduct)
                .toList();
    }

    public List<BuyProduct> getBuyProductsByBuyId(Long buyId) {
        return buyProductRepository.findByBuyIdWithDetails(buyId).stream()
                .map(this::toDomainBuyProduct)
                .toList();
    }

    public Optional<BuyProduct> getBuyProductById(Long id) {
        return buyProductRepository.findById(id)
                .map(this::toDomainBuyProduct);
    }

    public Optional<BuyProduct> getBuyProductByIdWithDetails(Long id) {
        return buyProductRepository.findById(id)
                .map(this::toDomainBuyProduct);
    }

    @Transactional
    public Long createBuyWithProducts(Long customerId, String description,
                                      List<CartItem> cartItems) {
        BuyEntity buy = new BuyEntity();
        buy.setCustomerId(customerId);
        buy.setDescription(description);
        buy.setBuyStepId(1L); // Статус "Новый"

        BuyEntity savedBuy = buyRepository.save(buy);

        for (CartItem item : cartItems) {
            ProductEntity product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Товар с ID " + item.productId() + " не найден"));

            BuyProductEntity bp = new BuyProductEntity();
            bp.setBuy(savedBuy);
            bp.setProduct(product);
            bp.setAmount(item.amount());

            buyProductRepository.save(bp);
        }

        return savedBuy.getId();
    }

    @Transactional
    public BuyProduct addProductToBuy(Long buyId, Long productId, Integer amount) {
        BuyEntity buy = buyRepository.findById(buyId)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));

        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));

        Optional<BuyProductEntity> existing = buyProductRepository
                .findByBuyIdAndProductId(buyId, productId);

        if (existing.isPresent()) {
            BuyProductEntity entity = existing.get();
            entity.setAmount(entity.getAmount() + amount);
            BuyProductEntity saved = buyProductRepository.save(entity);
            return toDomainBuyProduct(saved);
        } else {
            BuyProductEntity entity = new BuyProductEntity();
            entity.setBuy(buy);
            entity.setProduct(product);
            entity.setAmount(amount);

            BuyProductEntity saved = buyProductRepository.save(entity);
            return toDomainBuyProduct(saved);
        }
    }

    @Transactional
    public BuyProduct updateProductAmount(Long id, Integer amount) {
        BuyProductEntity entity = buyProductRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Позиция не найдена"));

        if (amount <= 0) {
            throw new IllegalArgumentException("Количество должно быть больше 0");
        }

        entity.setAmount(amount);
        BuyProductEntity saved = buyProductRepository.save(entity);
        return toDomainBuyProduct(saved);
    }

    @Transactional
    public void removeProductFromBuy(Long id) {
        if (!buyProductRepository.existsById(id)) {
            throw new IllegalArgumentException("Позиция не найдена");
        }
        buyProductRepository.deleteById(id);
    }

    public BigDecimal getTotalPriceByBuyId(Long buyId) {
        List<BuyProductEntity> items = buyProductRepository.findByBuyId(buyId);
        return items.stream()
                .map(item -> {
                    if (item.getProduct() != null && item.getProduct().getPrice() != null) {
                        return item.getProduct().getPrice()
                                .multiply(BigDecimal.valueOf(item.getAmount()));
                    }
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getItemCountByBuyId(Long buyId) {
        return buyProductRepository.findByBuyId(buyId).size();
    }

    public boolean existsByBuyIdAndProductId(Long buyId, Long productId) {
        return buyProductRepository.existsByBuyIdAndProductId(buyId, productId);
    }

    private BuyProduct toDomainBuyProduct(BuyProductEntity entity) {
        BigDecimal productPrice = entity.getProduct() != null ?
                entity.getProduct().getPrice() : BigDecimal.ZERO;
        BigDecimal totalPrice = productPrice.multiply(BigDecimal.valueOf(entity.getAmount()));

        return new BuyProduct(
                entity.getId(),
                entity.getBuy() != null ? entity.getBuy().getId() : null,
                entity.getBuy() != null ? entity.getBuy().getDescription() : null,
                entity.getBuy() != null ? entity.getBuy().getCustomerId() : null,
                entity.getProduct() != null ? entity.getProduct().getId() : null,
                entity.getProduct() != null ? entity.getProduct().getName() : null,
                entity.getProduct() != null && entity.getProduct().getCategory() != null ?
                        entity.getProduct().getCategory().getName() : null,
                entity.getProduct() != null && entity.getProduct().getMythology() != null ?
                        entity.getProduct().getMythology().getName() : null,
                productPrice,
                entity.getAmount(),
                totalPrice
        );
    }

    public record CartItem(Long productId, Integer amount) {}
}
