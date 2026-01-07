package com.alexxx2k.springproject.service;

import com.alexxx2k.springproject.domain.dto.BuyStep;
import com.alexxx2k.springproject.domain.entities.BuyStepEntity;
import com.alexxx2k.springproject.repository.BuyStepRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuyStepServiceTest {

    @Mock
    private BuyStepRepository buyStepRepository;

    @InjectMocks
    private BuyStepService buyStepService;

    private BuyStepEntity testEntity;
    private BuyStep testBuyStep;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        testDate = LocalDate.of(2024, 1, 15);
        testEntity = new BuyStepEntity(1L, 100L, testDate, testDate.plusDays(7));
        testBuyStep = new BuyStep(1L, 100L, testDate, testDate.plusDays(7));
    }

    @Test
    void getAllBuySteps_ShouldReturnAllBuySteps() {
        when(buyStepRepository.findAll()).thenReturn(List.of(testEntity));

        List<BuyStep> result = buyStepService.getAllBuySteps();

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).stepId());
        verify(buyStepRepository, times(1)).findAll();
    }

    @Test
    void getBuyStepById_ShouldReturnBuyStep() {
        when(buyStepRepository.findById(1L)).thenReturn(Optional.of(testEntity));

        Optional<BuyStep> result = buyStepService.getBuyStepById(1L);

        assertTrue(result.isPresent());
        assertEquals(100L, result.get().stepId());
    }

    @Test
    void getBuyStepById_ShouldReturnEmptyWhenNotFound() {
        when(buyStepRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<BuyStep> result = buyStepService.getBuyStepById(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getBuyStepsByStepId_ShouldReturnBuySteps() {
        when(buyStepRepository.findByStepId(100L)).thenReturn(List.of(testEntity));

        List<BuyStep> result = buyStepService.getBuyStepsByStepId(100L);

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).stepId());
    }

    @Test
    void createBuyStep_ShouldCreateSuccessfully() {
        BuyStep newBuyStep = new BuyStep(null, 100L, testDate, testDate.plusDays(7));
        when(buyStepRepository.save(any(BuyStepEntity.class))).thenReturn(testEntity);

        BuyStep result = buyStepService.createBuyStep(newBuyStep);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(100L, result.stepId());
        verify(buyStepRepository, times(1)).save(any(BuyStepEntity.class));
    }

    @Test
    void createBuyStep_ShouldThrowException_WhenEndDateBeforeStartDate() {
        BuyStep invalidBuyStep = new BuyStep(null, 100L, testDate.plusDays(7), testDate);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> buyStepService.createBuyStep(invalidBuyStep)
        );

        assertTrue(exception.getMessage().contains("Дата окончания не может быть раньше даты начала"));
        verify(buyStepRepository, never()).save(any());
    }

    @Test
    void createBuyStep_WithNullDates_ShouldWork() {
        BuyStep buyStepWithNullDates = new BuyStep(null, 100L, null, null);
        BuyStepEntity savedEntity = new BuyStepEntity(1L, 100L, null, null);

        when(buyStepRepository.save(any(BuyStepEntity.class))).thenReturn(savedEntity);

        BuyStep result = buyStepService.createBuyStep(buyStepWithNullDates);

        assertNotNull(result);
        assertNull(result.dateStart());
        assertNull(result.dateEnd());
    }

    @Test
    void updateBuyStep_ShouldUpdateSuccessfully() {
        BuyStep updatedBuyStep = new BuyStep(1L, 200L, testDate.plusDays(1), testDate.plusDays(10));
        BuyStepEntity updatedEntity = new BuyStepEntity(1L, 200L, testDate.plusDays(1), testDate.plusDays(10));

        when(buyStepRepository.findById(1L)).thenReturn(Optional.of(testEntity));
        when(buyStepRepository.save(any(BuyStepEntity.class))).thenReturn(updatedEntity);

        BuyStep result = buyStepService.updateBuyStep(1L, updatedBuyStep);

        assertEquals(200L, result.stepId());
        assertEquals(testDate.plusDays(1), result.dateStart());
        verify(buyStepRepository, times(1)).save(any(BuyStepEntity.class));
    }

    @Test
    void updateBuyStep_ShouldThrowException_WhenNotFound() {
        when(buyStepRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> buyStepService.updateBuyStep(1L, testBuyStep)
        );

        assertTrue(exception.getMessage().contains("не найден"));
        verify(buyStepRepository, never()).save(any());
    }

    @Test
    void updateBuyStep_ShouldThrowException_WhenEndDateBeforeStartDate() {
        BuyStep invalidBuyStep = new BuyStep(1L, 100L, testDate.plusDays(7), testDate);

        when(buyStepRepository.findById(1L)).thenReturn(Optional.of(testEntity));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> buyStepService.updateBuyStep(1L, invalidBuyStep)
        );

        assertTrue(exception.getMessage().contains("Дата окончания не может быть раньше даты начала"));
        verify(buyStepRepository, never()).save(any());
    }

    @Test
    void deleteBuyStep_ShouldDeleteSuccessfully() {
        when(buyStepRepository.existsById(1L)).thenReturn(true);
        doNothing().when(buyStepRepository).deleteById(1L);

        buyStepService.deleteBuyStep(1L);

        verify(buyStepRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteBuyStep_ShouldThrowException_WhenNotFound() {
        when(buyStepRepository.existsById(1L)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> buyStepService.deleteBuyStep(1L)
        );

        assertTrue(exception.getMessage().contains("не найден"));
        verify(buyStepRepository, never()).deleteById(any());
    }

    @Test
    void getActiveBuySteps_ShouldReturnActiveSteps() {
        LocalDate fixedDate = LocalDate.of(2024, 1, 16);

        when(buyStepRepository.findActiveSteps(any(LocalDate.class))).thenReturn(List.of(testEntity));

        List<BuyStep> result = buyStepService.getActiveBuySteps();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(buyStepRepository, times(1)).findActiveSteps(any(LocalDate.class));
    }

    @Test
    void getBuyStepsByDateRange_ShouldReturnSteps() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        when(buyStepRepository.findByDateRange(startDate, endDate)).thenReturn(List.of(testEntity));

        List<BuyStep> result = buyStepService.getBuyStepsByDateRange(startDate, endDate);

        assertEquals(1, result.size());
        verify(buyStepRepository, times(1)).findByDateRange(startDate, endDate);
    }

    @Test
    void existsByStepId_ShouldReturnTrue() {
        when(buyStepRepository.existsByStepId(100L)).thenReturn(true);

        boolean result = buyStepService.existsByStepId(100L);

        assertTrue(result);
    }

    @Test
    void existsByStepId_ShouldReturnFalse() {
        when(buyStepRepository.existsByStepId(999L)).thenReturn(false);

        boolean result = buyStepService.existsByStepId(999L);

        assertFalse(result);
    }

    @Test
    void toDomainBuyStep_ShouldConvertEntityToDto() {
        when(buyStepRepository.findById(1L)).thenReturn(Optional.of(testEntity));

        BuyStep result = buyStepService.getBuyStepById(1L)
                .orElseThrow(() -> new IllegalArgumentException("Entity not found"));

        assertEquals(testEntity.getId(), result.id());
        assertEquals(testEntity.getStepId(), result.stepId());
        assertEquals(testEntity.getDateStart(), result.dateStart());
        assertEquals(testEntity.getDateEnd(), result.dateEnd());
    }
}
