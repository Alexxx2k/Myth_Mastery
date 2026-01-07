package com.alexxx2k.springproject.integration;

import com.alexxx2k.springproject.domain.entities.CategoryEntity;
import com.alexxx2k.springproject.domain.entities.MythologyEntity;
import com.alexxx2k.springproject.domain.entities.ProductEntity;
import com.alexxx2k.springproject.repository.CategoryRepository;
import com.alexxx2k.springproject.repository.MythologyRepository;
import com.alexxx2k.springproject.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminProductManagementStoryTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MythologyRepository mythologyRepository;

    private CategoryEntity existingCategory;
    private MythologyEntity existingMythology;

    @BeforeEach
    void setupTestData() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        mythologyRepository.deleteAll();

        existingCategory = new CategoryEntity();
        existingCategory.setName("Реликвия");
        existingCategory.setHazard("Смертельная");
        existingCategory.setRarity("Уникальная");
        existingCategory = categoryRepository.save(existingCategory);

        existingMythology = new MythologyEntity();
        existingMythology.setName("Славянская");
        existingMythology = mythologyRepository.save(existingMythology);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void adminCanCompleteFullProductManagementFlow() throws Exception {
        mockMvc.perform(post("/products/create")
                        .with(csrf())
                        .param("categoryId", existingCategory.getId().toString())
                        .param("mythologyId", existingMythology.getId().toString())
                        .param("name", "Меч Зевса")
                        .param("price", "9999.99")
                        .param("description", "Легендарный меч верховного бога")
                        .param("pic", "zeus_sword.jpg"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"))
                .andExpect(flash().attribute("messageType", "success"))
                .andExpect(flash().attribute("message", "Продукт успешно добавлен!"));

        assertEquals(1, productRepository.count());

        List<ProductEntity> allProducts = productRepository.findAllWithAssociations();
        assertEquals(1, allProducts.size());

        ProductEntity savedProduct = allProducts.get(0);
        assertEquals("Меч Зевса", savedProduct.getName());
        assertEquals(new BigDecimal("9999.99"), savedProduct.getPrice());
        assertEquals(existingCategory.getId(), savedProduct.getCategory().getId());
        assertEquals(existingMythology.getId(), savedProduct.getMythology().getId());

        Long productId = savedProduct.getId();

        mockMvc.perform(post("/products/update/{id}", productId)
                        .with(csrf())
                        .param("categoryId", existingCategory.getId().toString())
                        .param("mythologyId", existingMythology.getId().toString())
                        .param("name", "Меч Зевса (улучшенный)")
                        .param("price", "12999.99")
                        .param("description", "Обновленная версия меча с дополнительными рунами")
                        .param("pic", "zeus_sword_v2.jpg"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products/edit/" + productId))
                .andExpect(flash().attribute("messageType", "success"))
                .andExpect(flash().attribute("message", "Продукт успешно обновлен!"));

        ProductEntity updatedProduct = productRepository.findByIdWithAssociations(productId).orElseThrow();
        assertEquals("Меч Зевса (улучшенный)", updatedProduct.getName());
        assertEquals(new BigDecimal("12999.99"), updatedProduct.getPrice());
        assertEquals("Обновленная версия меча с дополнительными рунами", updatedProduct.getDescription());
        assertEquals("zeus_sword_v2.jpg", updatedProduct.getPic());

        mockMvc.perform(post("/products/delete/{id}", productId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"))
                .andExpect(flash().attribute("messageType", "success"))
                .andExpect(flash().attribute("message", "Продукт успешно удален!"));

        assertEquals(0, productRepository.count());
        assertFalse(productRepository.existsById(productId));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void adminCanAddMultipleProducts() throws Exception {
        mockMvc.perform(post("/products/create")
                        .with(csrf())
                        .param("categoryId", existingCategory.getId().toString())
                        .param("mythologyId", existingMythology.getId().toString())
                        .param("name", "Меч Зевса")
                        .param("price", "9999.99")
                        .param("description", "Меч верховного бога")
                        .param("pic", "sword.jpg"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("messageType", "success"));

        mockMvc.perform(post("/products/create")
                        .with(csrf())
                        .param("categoryId", existingCategory.getId().toString())
                        .param("mythologyId", existingMythology.getId().toString())
                        .param("name", "Щит Афины")
                        .param("price", "7500.50")
                        .param("description", "Щит богини мудрости")
                        .param("pic", "shield.jpg"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("messageType", "success"));

        List<ProductEntity> allProducts = productRepository.findAllWithAssociations();
        assertEquals(2, allProducts.size());

        assertTrue(productRepository.existsByName("Меч Зевса"));
        assertTrue(productRepository.existsByName("Щит Афины"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void adminCannotAddProductWithDuplicateName() throws Exception {
        mockMvc.perform(post("/products/create")
                        .with(csrf())
                        .param("categoryId", existingCategory.getId().toString())
                        .param("mythologyId", existingMythology.getId().toString())
                        .param("name", "Уникальный товар")
                        .param("price", "1000.00")
                        .param("description", "Описание")
                        .param("pic", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("messageType", "success"));

        mockMvc.perform(post("/products/create")
                        .with(csrf())
                        .param("categoryId", existingCategory.getId().toString())
                        .param("mythologyId", existingMythology.getId().toString())
                        .param("name", "Уникальный товар")
                        .param("price", "2000.00")
                        .param("description", "Другое описание")
                        .param("pic", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("messageType", "error"))
                .andExpect(flash().attribute("message", containsString("уже существует")));

        assertEquals(1, productRepository.count());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void adminCanUpdateProductWithoutChangingName() throws Exception {
        mockMvc.perform(post("/products/create")
                        .with(csrf())
                        .param("categoryId", existingCategory.getId().toString())
                        .param("mythologyId", existingMythology.getId().toString())
                        .param("name", "Старый товар")
                        .param("price", "1000.00")
                        .param("description", "Старое описание")
                        .param("pic", "old.jpg"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("messageType", "success"));

        List<ProductEntity> allProducts = productRepository.findAllWithAssociations();
        assertEquals(1, allProducts.size());
        ProductEntity product = allProducts.get(0);

        mockMvc.perform(post("/products/update/{id}", product.getId())
                        .with(csrf())
                        .param("categoryId", existingCategory.getId().toString())
                        .param("mythologyId", existingMythology.getId().toString())
                        .param("name", "Старый товар")
                        .param("price", "2000.00")
                        .param("description", "Новое описание")
                        .param("pic", "new.jpg"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("messageType", "success"));

        ProductEntity updated = productRepository.findByIdWithAssociations(product.getId()).orElseThrow();
        assertEquals("Старый товар", updated.getName());
        assertEquals(new BigDecimal("2000.00"), updated.getPrice());
        assertEquals("Новое описание", updated.getDescription());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void adminCannotDeleteNonExistentProduct() throws Exception {
        mockMvc.perform(post("/products/delete/{id}", 999999L)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"))
                .andExpect(flash().attribute("messageType", "error"))
                .andExpect(flash().attribute("message", containsString("не найдена")));
    }
}
