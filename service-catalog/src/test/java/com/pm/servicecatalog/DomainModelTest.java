package com.pm.servicecatalog;

import com.pm.servicecatalog.domain.model.ServiceCategory;
import com.pm.servicecatalog.domain.model.ServiceItem;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DomainModelTest {

    @Test
    void serviceCategory_shouldCreateActiveCategory() {

        ServiceCategory category =
                ServiceCategory.create(
                        "Plumbing",
                        "Plumbing services"
                );

        assertNull(category.getId());
        assertEquals("Plumbing", category.getName());
        assertEquals(
                "Plumbing services",
                category.getDescription()
        );
        assertTrue(category.isActive());
    }

    @Test
    void serviceCategory_shouldRejectBlankName() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> ServiceCategory.create(
                                "   ",
                                "Description"
                        )
                );

        assertEquals(
                "Category name cannot be empty",
                exception.getMessage()
        );
    }

    @Test
    void serviceCategory_shouldUpdateAndChangeLifecycle() {

        ServiceCategory category =
                ServiceCategory.create(
                        "Plumbing",
                        "Old description"
                );

        category.update(
                "Electrical",
                "New description"
        );

        assertEquals("Electrical", category.getName());
        assertEquals(
                "New description",
                category.getDescription()
        );
        assertTrue(category.isActive());

        category.deactivate();

        assertFalse(category.isActive());

        category.activate();

        assertTrue(category.isActive());
    }

    @Test
    void serviceItem_shouldCreateActiveService() {

        UUID categoryId = UUID.randomUUID();

        ServiceItem service =
                ServiceItem.create(
                        "Pipe Repair",
                        "Repair damaged pipes",
                        categoryId
                );

        assertNull(service.getId());
        assertEquals("Pipe Repair", service.getName());
        assertEquals(
                "Repair damaged pipes",
                service.getDescription()
        );
        assertEquals(categoryId, service.getCategoryId());
        assertTrue(service.isActive());
    }

    @Test
    void serviceItem_shouldRejectBlankName() {

        UUID categoryId = UUID.randomUUID();

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> ServiceItem.create(
                                "",
                                "Description",
                                categoryId
                        )
                );

        assertEquals(
                "Service name cannot be empty",
                exception.getMessage()
        );
    }

    @Test
    void serviceItem_shouldRejectNullCategoryId() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> ServiceItem.create(
                                "Pipe Repair",
                                "Description",
                                null
                        )
                );

        assertEquals(
                "Category ID is required",
                exception.getMessage()
        );
    }

    @Test
    void serviceItem_shouldUpdateAndChangeLifecycle() {

        UUID originalCategoryId = UUID.randomUUID();
        UUID newCategoryId = UUID.randomUUID();

        ServiceItem service =
                ServiceItem.create(
                        "Pipe Repair",
                        "Old description",
                        originalCategoryId
                );

        service.update(
                "Emergency Pipe Repair",
                "New description",
                newCategoryId
        );

        assertEquals(
                "Emergency Pipe Repair",
                service.getName()
        );
        assertEquals(
                "New description",
                service.getDescription()
        );
        assertEquals(
                newCategoryId,
                service.getCategoryId()
        );
        assertTrue(service.isActive());

        service.deactivate();

        assertFalse(service.isActive());

        service.activate();

        assertTrue(service.isActive());
    }
}