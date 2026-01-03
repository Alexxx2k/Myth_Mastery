package com.alexxx2k.springproject.service;

import com.alexxx2k.springproject.domain.dto.Product;
import com.alexxx2k.springproject.domain.entities.CategoryEntity;
import com.alexxx2k.springproject.domain.entities.MythologyEntity;
import com.alexxx2k.springproject.domain.entities.ProductEntity;
import com.alexxx2k.springproject.repository.CategoryRepository;
import com.alexxx2k.springproject.repository.MythologyRepository;
import com.alexxx2k.springproject.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final MythologyRepository mythologyRepository;
    private final YandexS3StorageService storageService;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          MythologyRepository mythologyRepository,
                          YandexS3StorageService storageService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.mythologyRepository = mythologyRepository;
        this.storageService = storageService;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAllWithAssociations().stream()
                .map(this::toDomainProduct)
                .toList();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findByIdWithAssociations(id)
                .map(this::toDomainProduct);
    }

    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId).stream()
                .map(this::toDomainProduct)
                .toList();
    }

    public List<Product> getProductsByMythology(Long mythologyId) {
        return productRepository.findByMythologyId(mythologyId).stream()
                .map(this::toDomainProduct)
                .toList();
    }

    @Transactional
    public Product createProduct(Product product, MultipartFile imageFile) throws Exception {
        validateProduct(product);

        if (productRepository.existsByName(product.name().trim())) {
            throw new IllegalArgumentException("Продукт с названием '" + product.name() + "' уже существует");
        }

        CategoryEntity category = getCategoryById(product.categoryId());
        MythologyEntity mythology = getMythologyById(product.mythologyId());
        String imageUrl = uploadImageIfPresent(imageFile);

        ProductEntity entity = createProductEntity(product, category, mythology, imageUrl);

        ProductEntity savedEntity = productRepository.save(entity);

        return toDomainProduct(savedEntity);
    }

    @Transactional
    public Product updateProduct(Long id, Product product, MultipartFile imageFile) throws Exception {
        ProductEntity existingEntity = getExistingProduct(id);

        validateProduct(product);

        if (productRepository.existsByNameAndIdNot(product.name(), id)) {
            throw new IllegalArgumentException("Продукт с названием '" + product.name() + "' уже существует");
        }

        CategoryEntity category = getCategoryById(product.categoryId());
        MythologyEntity mythology = getMythologyById(product.mythologyId());

        String imageUrl = handleImageUpdate(existingEntity.getImageKey(), imageFile);

        updateProductEntity(existingEntity, category, mythology, product, imageUrl);

        ProductEntity savedEntity = productRepository.save(existingEntity);

        return toDomainProduct(savedEntity);
    }

    @Transactional
    public void deleteProduct(Long id) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Продукт с ID " + id + " не найден"));

        deleteImageIfExists(product.getImageKey());

        productRepository.deleteById(id);
    }

    public boolean existsByName(String name) {
        return productRepository.existsByName(name);
    }

    private void validateProduct(Product product) {
        if (product.name() == null || product.name().trim().isEmpty()) {
            throw new IllegalArgumentException("Название продукта не может быть пустым");
        }

        if (product.categoryId() == null) {
            throw new IllegalArgumentException("Категория не указана");
        }

        if (product.mythologyId() == null) {
            throw new IllegalArgumentException("Мифология не указана");
        }

        if (product.price() != null && product.price().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Цена не может быть отрицательной");
        }
    }

    private CategoryEntity getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Категория с ID " + categoryId + " не найдена"));
    }

    private MythologyEntity getMythologyById(Long mythologyId) {
        return mythologyRepository.findById(mythologyId)
                .orElseThrow(() -> new IllegalArgumentException("Мифология с ID " + mythologyId + " не найдена"));
    }

    private ProductEntity getExistingProduct(Long id) {
        return productRepository.findByIdWithAssociations(id)
                .orElseThrow(() -> new IllegalArgumentException("Продукт с ID " + id + " не найден"));
    }

    private String uploadImageIfPresent(MultipartFile imageFile) throws Exception {
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                return storageService.uploadProductImage(imageFile);
            } catch (Exception e) {
                throw new IllegalArgumentException("Ошибка при загрузке изображения: " + e.getMessage(), e);
            }
        }
        return null;
    }

    private String handleImageUpdate(String currentImageUrl, MultipartFile newImageFile) throws Exception {
        if (newImageFile == null || newImageFile.isEmpty()) {
            return currentImageUrl;
        }

        deleteImageIfExists(currentImageUrl);

        try {
            return storageService.uploadProductImage(newImageFile);
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка при загрузке нового изображения: " + e.getMessage(), e);
        }
    }

    private void deleteImageIfExists(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                storageService.deleteProductImage(imageUrl);
            } catch (Exception e) {
                System.err.println("Ошибка при удалении изображения из хранилища: " + e.getMessage());
            }
        }
    }

    private ProductEntity createProductEntity(Product product, CategoryEntity category,
                                              MythologyEntity mythology, String imageUrl) {
        return new ProductEntity(
                null,
                category,
                mythology,
                product.name().trim(),
                product.price() != null ? product.price() : BigDecimal.ZERO,
                product.description() != null ? product.description() : "",
                imageUrl
        );
    }

    private void updateProductEntity(ProductEntity entity, CategoryEntity category,
                                     MythologyEntity mythology, Product product, String imageUrl) {
        entity.setCategory(category);
        entity.setMythology(mythology);
        entity.setName(product.name().trim());
        entity.setPrice(product.price() != null ? product.price() : BigDecimal.ZERO);
        entity.setDescription(product.description() != null ? product.description() : "");
        entity.setImageKey(imageUrl);
    }

    private Product toDomainProduct(ProductEntity entity) {
        return new Product(
                entity.getId(),
                entity.getCategory() != null ? entity.getCategory().getId() : null,
                entity.getCategory() != null ? entity.getCategory().getName() : null,
                entity.getMythology() != null ? entity.getMythology().getId() : null,
                entity.getMythology() != null ? entity.getMythology().getName() : null,
                entity.getName(),
                entity.getPrice(),
                entity.getDescription(),
                entity.getImageKey()
        );
    }
}