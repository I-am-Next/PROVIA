package com.pm.servicecatalog.config;

import com.pm.servicecatalog.domain.ports.in.CreateCategoryUseCase;
import com.pm.servicecatalog.domain.ports.in.CreateServiceUseCase;
import com.pm.servicecatalog.domain.ports.in.DeleteCategoryUseCase;
import com.pm.servicecatalog.domain.ports.in.DeleteServiceItemUseCase;
import com.pm.servicecatalog.domain.ports.in.GetCatalogUseCase;
import com.pm.servicecatalog.domain.ports.in.UpdateCategoryUseCase;
import com.pm.servicecatalog.domain.ports.in.UpdateServiceItemUseCase;
import com.pm.servicecatalog.presentation.controller.AdminCatalogController;
import com.pm.servicecatalog.presentation.controller.CatalogController;
import com.pm.servicecatalog.security.JwtAuthenticationFilter;
import com.pm.servicecatalog.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        CatalogController.class,
        AdminCatalogController.class
})
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private GetCatalogUseCase getCatalogUseCase;

    @MockitoBean
    private CreateCategoryUseCase createCategoryUseCase;

    @MockitoBean
    private UpdateCategoryUseCase updateCategoryUseCase;

    @MockitoBean
    private DeleteCategoryUseCase deleteCategoryUseCase;

    @MockitoBean
    private CreateServiceUseCase createServiceUseCase;

    @MockitoBean
    private UpdateServiceItemUseCase updateServiceUseCase;

    @MockitoBean
    private DeleteServiceItemUseCase deleteServiceUseCase;


    // =========================================================
    // PUBLIC CATALOG ENDPOINTS
    // =========================================================

    @Test
    void getCategories_shouldBePublic() throws Exception {

        mockMvc.perform(
                get("/catalog/categories")
        ).andExpect(status().isOk());
    }

    @Test
    void getCategory_shouldBePublic() throws Exception {

        mockMvc.perform(
                get("/catalog/categories/" + UUID.randomUUID())
        ).andExpect(status().isOk());
    }

    @Test
    void getCategoriesBatch_shouldBePublic() throws Exception {

        UUID categoryId1 = UUID.randomUUID();
        UUID categoryId2 = UUID.randomUUID();

        when(getCatalogUseCase.getCategoriesByIds(
                List.of(categoryId1, categoryId2)
        )).thenReturn(List.of());

        mockMvc.perform(
                get("/catalog/categories/batch")
                        .param("ids", categoryId1.toString())
                        .param("ids", categoryId2.toString())
        ).andExpect(status().isOk());
    }

    @Test
    void getService_shouldBePublic() throws Exception {

        mockMvc.perform(
                get("/catalog/services/" + UUID.randomUUID())
        ).andExpect(status().isOk());
    }


    // =========================================================
    // CREATE CATEGORY
    // =========================================================

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_shouldAllowAdmin() throws Exception {

        mockMvc.perform(
                post("/catalog/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Plumbing"
                                }
                                """)
        ).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createCategory_shouldRejectNonAdmin() throws Exception {

        mockMvc.perform(
                post("/catalog/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Plumbing"
                                }
                                """)
        ).andExpect(status().isForbidden());
    }

    @Test
    void createCategory_shouldRejectUnauthenticated() throws Exception {

        mockMvc.perform(
                post("/catalog/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Plumbing"
                                }
                                """)
        ).andExpect(status().isForbidden());
    }


    // =========================================================
    // UPDATE CATEGORY
    // =========================================================

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCategory_shouldAllowAdmin() throws Exception {

        UUID categoryId = UUID.randomUUID();

        mockMvc.perform(
                put("/catalog/admin/categories/" + categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Updated Plumbing"
                                }
                                """)
        ).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateCategory_shouldRejectNonAdmin() throws Exception {

        UUID categoryId = UUID.randomUUID();

        mockMvc.perform(
                put("/catalog/admin/categories/" + categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Updated Plumbing"
                                }
                                """)
        ).andExpect(status().isForbidden());
    }

    @Test
    void updateCategory_shouldRejectUnauthenticated() throws Exception {

        UUID categoryId = UUID.randomUUID();

        mockMvc.perform(
                put("/catalog/admin/categories/" + categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Updated Plumbing"
                                }
                                """)
        ).andExpect(status().isForbidden());
    }


    // =========================================================
    // DELETE CATEGORY
    // =========================================================

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_shouldAllowAdmin() throws Exception {

        UUID categoryId = UUID.randomUUID();

        mockMvc.perform(
                delete("/catalog/admin/categories/" + categoryId)
        ).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteCategory_shouldRejectNonAdmin() throws Exception {

        UUID categoryId = UUID.randomUUID();

        mockMvc.perform(
                delete("/catalog/admin/categories/" + categoryId)
        ).andExpect(status().isForbidden());
    }

    @Test
    void deleteCategory_shouldRejectUnauthenticated() throws Exception {

        UUID categoryId = UUID.randomUUID();

        mockMvc.perform(
                delete("/catalog/admin/categories/" + categoryId)
        ).andExpect(status().isForbidden());
    }


    // =========================================================
    // CREATE SERVICE
    // =========================================================

    @Test
    @WithMockUser(roles = "ADMIN")
    void createService_shouldAllowAdmin() throws Exception {

        mockMvc.perform(
                post("/catalog/admin/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Pipe Repair",
                                    "categoryId": "00000000-0000-0000-0000-000000000001"
                                }
                                """)
        ).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createService_shouldRejectNonAdmin() throws Exception {

        mockMvc.perform(
                post("/catalog/admin/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Pipe Repair",
                                    "categoryId": "00000000-0000-0000-0000-000000000001"
                                }
                                """)
        ).andExpect(status().isForbidden());
    }

    @Test
    void createService_shouldRejectUnauthenticated() throws Exception {

        mockMvc.perform(
                post("/catalog/admin/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Pipe Repair",
                                    "categoryId": "00000000-0000-0000-0000-000000000001"
                                }
                                """)
        ).andExpect(status().isForbidden());
    }


    // =========================================================
    // UPDATE SERVICE
    // =========================================================

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateService_shouldAllowAdmin() throws Exception {

        UUID serviceId = UUID.randomUUID();

        mockMvc.perform(
                put("/catalog/admin/services/" + serviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Updated Pipe Repair",
                                    "categoryId": "00000000-0000-0000-0000-000000000001"
                                }
                                """)
        ).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateService_shouldRejectNonAdmin() throws Exception {

        UUID serviceId = UUID.randomUUID();

        mockMvc.perform(
                put("/catalog/admin/services/" + serviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Updated Pipe Repair",
                                    "categoryId": "00000000-0000-0000-0000-000000000001"
                                }
                                """)
        ).andExpect(status().isForbidden());
    }

    @Test
    void updateService_shouldRejectUnauthenticated() throws Exception {

        UUID serviceId = UUID.randomUUID();

        mockMvc.perform(
                put("/catalog/admin/services/" + serviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Updated Pipe Repair",
                                    "categoryId": "00000000-0000-0000-0000-000000000001"
                                }
                                """)
        ).andExpect(status().isForbidden());
    }


    // =========================================================
    // DELETE SERVICE
    // =========================================================

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteService_shouldAllowAdmin() throws Exception {

        UUID serviceId = UUID.randomUUID();

        mockMvc.perform(
                delete("/catalog/admin/services/" + serviceId)
        ).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteService_shouldRejectNonAdmin() throws Exception {

        UUID serviceId = UUID.randomUUID();

        mockMvc.perform(
                delete("/catalog/admin/services/" + serviceId)
        ).andExpect(status().isForbidden());
    }

    @Test
    void deleteService_shouldRejectUnauthenticated() throws Exception {

        UUID serviceId = UUID.randomUUID();

        mockMvc.perform(
                delete("/catalog/admin/services/" + serviceId)
        ).andExpect(status().isForbidden());
    }
}