package com.pm.servicecatalog;

import com.pm.servicecatalog.application.dto.CategoryResponse;
import com.pm.servicecatalog.application.dto.CreateCategoryRequest;
import com.pm.servicecatalog.application.dto.CreateServiceRequest;
import com.pm.servicecatalog.application.dto.ServiceItemResponse;
import com.pm.servicecatalog.application.dto.UpdateCategoryRequest;
import com.pm.servicecatalog.application.dto.UpdateServiceRequest;
import com.pm.servicecatalog.application.event.DomainEventPublisher;
import com.pm.servicecatalog.application.service.CreateCategoryService;
import com.pm.servicecatalog.application.service.CreateServiceItemService;
import com.pm.servicecatalog.application.service.DeleteCategoryService;
import com.pm.servicecatalog.application.service.DeleteServiceItemService;
import com.pm.servicecatalog.application.service.GetCatalogService;
import com.pm.servicecatalog.application.service.UpdateCategoryService;
import com.pm.servicecatalog.application.service.UpdateServiceItemService;
import com.pm.servicecatalog.domain.model.ServiceCategory;
import com.pm.servicecatalog.domain.model.ServiceItem;
import com.pm.servicecatalog.domain.ports.out.ServiceCategoryRepository;
import com.pm.servicecatalog.domain.ports.out.ServiceItemRepository;
import com.pm.servicecatalog.shared.exception.CategoryNotFoundException;
import com.pm.servicecatalog.shared.exception.ServiceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ApplicationServiceTest {

    @Mock
    private ServiceCategoryRepository categoryRepository;

    @Mock
    private ServiceItemRepository serviceRepository;

    @Mock
    private DomainEventPublisher eventPublisher;

    private CreateCategoryService createCategoryService;
    private UpdateCategoryService updateCategoryService;
    private DeleteCategoryService deleteCategoryService;

    private CreateServiceItemService createServiceItemService;
    private UpdateServiceItemService updateServiceItemService;
    private DeleteServiceItemService deleteServiceItemService;

    private GetCatalogService getCatalogService;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        createCategoryService =
                new CreateCategoryService(
                        categoryRepository,
                        eventPublisher
                );

        updateCategoryService =
                new UpdateCategoryService(
                        categoryRepository,
                        eventPublisher
                );

        deleteCategoryService =
                new DeleteCategoryService(
                        categoryRepository,
                        eventPublisher
                );

        createServiceItemService =
                new CreateServiceItemService(
                        serviceRepository,
                        categoryRepository,
                        eventPublisher
                );

        updateServiceItemService =
                new UpdateServiceItemService(
                        serviceRepository,
                        categoryRepository,
                        eventPublisher
                );

        deleteServiceItemService =
                new DeleteServiceItemService(
                        serviceRepository,
                        eventPublisher
                );

        getCatalogService =
                new GetCatalogService(
                        categoryRepository,
                        serviceRepository
                );
    }

    @Test
    void createCategory_shouldSavePublishEventAndReturnResponse() {

        UUID categoryId = UUID.randomUUID();

        ServiceCategory category =
                ServiceCategory.create(
                        "Plumbing",
                        "Plumbing services"
                );

        ReflectionTestUtils.setField(
                category,
                "id",
                categoryId
        );

        when(categoryRepository.save(any(ServiceCategory.class)))
                .thenAnswer(invocation -> {

                    ServiceCategory savedCategory =
                            invocation.getArgument(0);

                    ReflectionTestUtils.setField(
                            savedCategory,
                            "id",
                            categoryId
                    );

                    return savedCategory;
                });

        CategoryResponse response =
                createCategoryService.createCategory(
                        new CreateCategoryRequest(
                                "Plumbing",
                                "Plumbing services"
                        )
                );

        assertEquals(
                categoryId,
                response.id()
        );

        assertEquals(
                "Plumbing",
                response.name()
        );

        assertEquals(
                "Plumbing services",
                response.description()
        );

        assertTrue(response.active());

        assertNotNull(response.services());

        assertTrue(response.services().isEmpty());

        ArgumentCaptor<ServiceCategory> categoryCaptor =
                ArgumentCaptor.forClass(ServiceCategory.class);

        verify(categoryRepository).save(
                categoryCaptor.capture()
        );

        ServiceCategory savedCategory =
                categoryCaptor.getValue();

        assertEquals(
                "Plumbing",
                savedCategory.getName()
        );

        assertEquals(
                "Plumbing services",
                savedCategory.getDescription()
        );

        assertTrue(
                savedCategory.isActive()
        );

        verify(eventPublisher).publish(any());
    }

    @Test
    void updateCategory_shouldUpdateSaveAndPublishEvent() {

        UUID categoryId = UUID.randomUUID();

        ServiceCategory category =
                ServiceCategory.create(
                        "Plumbing",
                        "Old description"
                );

        ReflectionTestUtils.setField(
                category,
                "id",
                categoryId
        );

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(categoryRepository.save(category))
                .thenReturn(category);

        CategoryResponse response =
                updateCategoryService.updateCategory(
                        categoryId,
                        new UpdateCategoryRequest(
                                "Electrical",
                                "Electrical services"
                        )
                );

        assertEquals("Electrical", response.name());
        assertEquals(
                "Electrical services",
                response.description()
        );
        assertTrue(response.active());

        verify(categoryRepository).save(category);
        verify(eventPublisher).publish(any());
    }

    @Test
    void deleteCategory_shouldDeactivateSaveAndPublishEvent() {

        UUID categoryId = UUID.randomUUID();

        ServiceCategory category =
                ServiceCategory.create(
                        "Plumbing",
                        "Plumbing services"
                );

        ReflectionTestUtils.setField(
                category,
                "id",
                categoryId
        );

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(categoryRepository.save(category))
                .thenReturn(category);

        deleteCategoryService.deleteCategory(categoryId);

        assertFalse(category.isActive());

        verify(categoryRepository).save(category);
        verify(eventPublisher).publish(any());
    }

    @Test
    void createService_shouldRequireExistingCategory() {

        UUID categoryId = UUID.randomUUID();

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.empty());

        assertThrows(
                CategoryNotFoundException.class,
                () -> createServiceItemService.createService(
                        new CreateServiceRequest(
                                "Pipe Repair",
                                "Repair pipes",
                                categoryId
                        )
                )
        );

        verify(serviceRepository, never())
                .save(any());

        verify(eventPublisher, never())
                .publish(any());
    }

    @Test
    void createService_shouldSavePublishEventAndReturnResponse() {

        UUID categoryId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();

        ServiceCategory category =
                ServiceCategory.create(
                        "Plumbing",
                        "Plumbing services"
                );

        ReflectionTestUtils.setField(
                category,
                "id",
                categoryId
        );

        ServiceItem service =
                ServiceItem.create(
                        "Pipe Repair",
                        "Repair damaged pipes",
                        categoryId
                );

        ReflectionTestUtils.setField(
                service,
                "id",
                serviceId
        );

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(serviceRepository.save(any(ServiceItem.class)))
                .thenReturn(service);

        ServiceItemResponse response =
                createServiceItemService.createService(
                        new CreateServiceRequest(
                                "Pipe Repair",
                                "Repair damaged pipes",
                                categoryId
                        )
                );

        assertEquals(serviceId, response.id());
        assertEquals("Pipe Repair", response.name());
        assertEquals(
                "Repair damaged pipes",
                response.description()
        );
        assertEquals(categoryId, response.categoryId());
        assertEquals("Plumbing", response.categoryName());
        assertTrue(response.active());

        verify(serviceRepository).save(any(ServiceItem.class));
        verify(eventPublisher).publish(any());
    }

    @Test
    void updateService_shouldUpdateSaveAndPublishEvent() {

        UUID serviceId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        ServiceCategory category =
                ServiceCategory.create(
                        "Electrical",
                        "Electrical services"
                );

        ReflectionTestUtils.setField(
                category,
                "id",
                categoryId
        );

        ServiceItem service =
                ServiceItem.create(
                        "Old Service",
                        "Old description",
                        categoryId
                );

        ReflectionTestUtils.setField(
                service,
                "id",
                serviceId
        );

        when(serviceRepository.findById(serviceId))
                .thenReturn(Optional.of(service));

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(serviceRepository.save(service))
                .thenReturn(service);

        ServiceItemResponse response =
                updateServiceItemService.updateService(
                        serviceId,
                        new UpdateServiceRequest(
                                "New Service",
                                "New description",
                                categoryId
                        )
                );

        assertEquals("New Service", response.name());
        assertEquals(
                "New description",
                response.description()
        );
        assertEquals(categoryId, response.categoryId());
        assertEquals("Electrical", response.categoryName());

        verify(serviceRepository).save(service);
        verify(eventPublisher).publish(any());
    }

    @Test
    void deleteService_shouldDeactivateSaveAndPublishEvent() {

        UUID serviceId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        ServiceItem service =
                ServiceItem.create(
                        "Pipe Repair",
                        "Repair pipes",
                        categoryId
                );

        ReflectionTestUtils.setField(
                service,
                "id",
                serviceId
        );

        when(serviceRepository.findById(serviceId))
                .thenReturn(Optional.of(service));

        when(serviceRepository.save(service))
                .thenReturn(service);

        deleteServiceItemService.deleteService(serviceId);

        assertFalse(service.isActive());

        verify(serviceRepository).save(service);
        verify(eventPublisher).publish(any());
    }

    @Test
    void getCatalog_shouldReturnOnlyActiveCategories() {

        UUID categoryId = UUID.randomUUID();

        ServiceCategory category =
                ServiceCategory.create(
                        "Plumbing",
                        "Plumbing services"
                );

        ReflectionTestUtils.setField(
                category,
                "id",
                categoryId
        );

        when(categoryRepository.findAllActive())
                .thenReturn(List.of(category));

        List<CategoryResponse> result =
                getCatalogService.getAllCategories();

        assertEquals(1, result.size());
        assertEquals(categoryId, result.get(0).id());
        assertEquals("Plumbing", result.get(0).name());
        assertTrue(result.get(0).active());
    }

    @Test
    void getCategoryWithServices_shouldReturnActiveServices() {

        UUID categoryId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();

        ServiceCategory category =
                ServiceCategory.create(
                        "Plumbing",
                        "Plumbing services"
                );

        ReflectionTestUtils.setField(
                category,
                "id",
                categoryId
        );

        ServiceItem service =
                ServiceItem.create(
                        "Pipe Repair",
                        "Repair damaged pipes",
                        categoryId
                );

        ReflectionTestUtils.setField(
                service,
                "id",
                serviceId
        );

        when(categoryRepository.findActiveById(categoryId))
                .thenReturn(Optional.of(category));

        when(serviceRepository.findByCategoryIdAndActiveTrue(
                categoryId
        )).thenReturn(List.of(service));

        CategoryResponse result =
                getCatalogService.getCategoryWithServices(
                        categoryId
                );

        assertEquals(categoryId, result.id());
        assertEquals("Plumbing", result.name());
        assertEquals(1, result.services().size());

        ServiceItemResponse serviceResponse =
                result.services().get(0);

        assertEquals(serviceId, serviceResponse.id());
        assertEquals("Pipe Repair", serviceResponse.name());
        assertEquals("Plumbing", serviceResponse.categoryName());
        assertTrue(serviceResponse.active());
    }

    @Test
    void getServiceById_shouldRejectInactiveService() {

        UUID serviceId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        ServiceItem service =
                ServiceItem.create(
                        "Pipe Repair",
                        "Repair pipes",
                        categoryId
                );

        ReflectionTestUtils.setField(
                service,
                "id",
                serviceId
        );

        service.deactivate();

        when(serviceRepository.findById(serviceId))
                .thenReturn(Optional.of(service));

        assertThrows(
                ServiceNotFoundException.class,
                () -> getCatalogService.getServiceById(serviceId)
        );

        verify(categoryRepository, never())
                .findActiveById(any());
    }
}