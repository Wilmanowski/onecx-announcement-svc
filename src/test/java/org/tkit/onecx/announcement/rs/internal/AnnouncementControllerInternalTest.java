package org.tkit.onecx.announcement.rs.internal;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.jboss.resteasy.reactive.RestResponse.Status.CREATED;

import java.time.OffsetDateTime;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.tkit.onecx.announcement.rs.internal.controller.AnnouncementControllerInternal;
import org.tkit.onecx.announcement.rs.internal.mapper.AnnouncementMapper;
import org.tkit.onecx.announcement.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.announcement.rs.internal.model.*;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(AnnouncementControllerInternal.class)
@WithDBData(value = "data/test-internal.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
@GenerateKeycloakClient(clientName = "testClient", scopes = { "ocx-an:read", "ocx-an:write", "ocx-an:delete", "ocx-an:all" })
class AnnouncementControllerInternalTest extends AbstractTest {

    @Test
    void getAnnouncementsByCriteriaAllTest() {
        AnnouncementSearchCriteriaDTO criteria = new AnnouncementSearchCriteriaDTO();

        var data = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(AnnouncementPageResultDTO.class);

        Assertions.assertThat(data).isNotNull();
        Assertions.assertThat(data.getTotalElements()).isEqualTo(7);
        Assertions.assertThat(data.getStream()).isNotNull().hasSize(7);
    }

    @Test
    void getAnnouncementsByMultipleSelectedFiltersTest() {
        AnnouncementSearchCriteriaDTO criteria = new AnnouncementSearchCriteriaDTO();
        criteria.setStatus(List.of(AnnouncementStatusDTO.ACTIVE, AnnouncementStatusDTO.INACTIVE));
        criteria.setType(List.of(AnnouncementTypeDTO.EVENT, AnnouncementTypeDTO.INFO));
        criteria.setPriority(List.of(AnnouncementPriorityTypeDTO.LOW, AnnouncementPriorityTypeDTO.NORMAL));

        var data = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(AnnouncementPageResultDTO.class);

        Assertions.assertThat(data.getTotalElements()).isEqualTo(7);
        Assertions.assertThat(data.getStream()).extracting(AnnouncementDTO::getId)
                .containsExactlyInAnyOrder("a1", "a2", "a3", "a4", "a5", "a6", "a7");
    }

    @Test
    void getAnnouncementsByEmptySelectedFiltersTest() {
        AnnouncementSearchCriteriaDTO criteria = new AnnouncementSearchCriteriaDTO();
        criteria.setStatus(List.of());
        criteria.setType(List.of());
        criteria.setPriority(List.of());

        var data = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(AnnouncementPageResultDTO.class);

        Assertions.assertThat(data.getTotalElements()).isEqualTo(7);
        Assertions.assertThat(data.getStream()).hasSize(7);
    }

    @Test
    void getAnnouncementsByCombinedSelectedFiltersTest() {
        AnnouncementSearchCriteriaDTO criteria = new AnnouncementSearchCriteriaDTO();
        criteria.setProductName("product2");
        criteria.setStatus(List.of(AnnouncementStatusDTO.ACTIVE));
        criteria.setType(List.of(AnnouncementTypeDTO.EVENT, AnnouncementTypeDTO.INFO));
        criteria.setPriority(List.of(AnnouncementPriorityTypeDTO.LOW, AnnouncementPriorityTypeDTO.NORMAL));

        var data = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(AnnouncementPageResultDTO.class);

        Assertions.assertThat(data.getTotalElements()).isEqualTo(5);
        Assertions.assertThat(data.getStream()).extracting(AnnouncementDTO::getId)
                .containsExactlyInAnyOrder("a2", "a3", "a5", "a6", "a7");
    }

    @Test
    void searchAnnouncementBannersByCriteriaTest() {
        AnnouncementBannerSearchCriteriaDTO criteriaDTO = new AnnouncementBannerSearchCriteriaDTO();
        criteriaDTO.setCurrentDate(OffsetDateTime.parse("2023-03-10T12:15:50-04:00"));
        criteriaDTO.setAppearance(null);
        var data = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .body(criteriaDTO)
                .post("banner/search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(AnnouncementPageResultDTO.class);

        Assertions.assertThat(data).isNotNull();
        Assertions.assertThat(data.getTotalElements()).isEqualTo(2);
        Assertions.assertThat(data.getStream()).isNotNull().hasSize(2);

        criteriaDTO.setWorkspaceName("workspace6");
        criteriaDTO.setAppearance(List.of());
        data = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .body(criteriaDTO)
                .post("banner/search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(AnnouncementPageResultDTO.class);

        Assertions.assertThat(data).isNotNull();
        Assertions.assertThat(data.getTotalElements()).isEqualTo(2);
        Assertions.assertThat(data.getStream()).isNotNull().hasSize(2);

        criteriaDTO.setWorkspaceName(null);
        criteriaDTO.setProductName("product1");
        criteriaDTO.setCurrentDate(OffsetDateTime.parse("2020-03-10T12:15:50-04:00"));
        criteriaDTO.setAppearance(List.of(AnnouncementAppearanceDTO.ALL));

        data = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .body(criteriaDTO)
                .post("banner/search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(AnnouncementPageResultDTO.class);

        Assertions.assertThat(data).isNotNull();
        Assertions.assertThat(data.getTotalElements()).isEqualTo(1);
        Assertions.assertThat(data.getStream()).isNotNull().hasSize(1);
    }

    @Test
    void getAnnouncementsByCriteriaTest() {
        AnnouncementSearchCriteriaDTO criteria = new AnnouncementSearchCriteriaDTO();
        criteria.setProductName("product2");
        criteria.setStatus(List.of(AnnouncementStatusDTO.ACTIVE));
        criteria.setPriority(List.of(AnnouncementPriorityTypeDTO.NORMAL));
        criteria.setType(List.of(AnnouncementTypeDTO.EVENT));
        criteria.setTitle("title2");
        criteria.setStartDateFrom(OffsetDateTime.parse("2000-03-10T12:15:50-04:00"));
        criteria.setStartDateTo(OffsetDateTime.parse("2023-03-10T12:15:50-04:00"));
        criteria.setEndDateFrom(OffsetDateTime.parse("2000-03-10T12:15:50-04:00"));
        criteria.setEndDateTo(OffsetDateTime.parse("2023-03-10T12:15:50-04:00"));
        criteria.setAppearance(List.of(AnnouncementAppearanceDTO.ALL));

        var data = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(AnnouncementPageResultDTO.class);

        Assertions.assertThat(data).isNotNull();
        Assertions.assertThat(data.getTotalElements()).isEqualTo(1);
        Assertions.assertThat(data.getStream()).isNotNull().hasSize(1);
    }

    @Test
    void getAnnouncementsByAppearanceCriteriaTest() {
        AnnouncementSearchCriteriaDTO criteria = new AnnouncementSearchCriteriaDTO();
        criteria.setAppearance(List.of(AnnouncementAppearanceDTO.ALL));

        var data = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(AnnouncementPageResultDTO.class);

        Assertions.assertThat(data).isNotNull();
        Assertions.assertThat(data.getTotalElements()).isEqualTo(6);
        Assertions.assertThat(data.getStream()).isNotNull().hasSize(6);

        //should find all with empty appearance list
        AnnouncementSearchCriteriaDTO criteria2 = new AnnouncementSearchCriteriaDTO();
        criteria2.setAppearance(null);

        data = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .body(criteria2)
                .post("search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(AnnouncementPageResultDTO.class);

        Assertions.assertThat(data).isNotNull();
        Assertions.assertThat(data.getTotalElements()).isEqualTo(7);
        Assertions.assertThat(data.getStream()).isNotNull().hasSize(7);
    }

    @Test
    void createAnnouncementTest() {

        // create announcement
        var createDto = new CreateAnnouncementRequestDTO();

        createDto.setProductName("product0");
        createDto.setTitle("basePath");
        createDto.setStartDate(OffsetDateTime.parse("2000-03-10T12:15:50-04:00"));

        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .body(createDto)
                .post()
                .then()
                .statusCode(CREATED.getStatusCode())
                .extract()
                .body().as(AnnouncementDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getProductName()).isNotNull().isEqualTo(createDto.getProductName());
        assertThat(dto.getTitle()).isNotNull().isEqualTo(createDto.getTitle());
        assertThat(dto.getStartDate()).isNotNull().isEqualTo(createDto.getStartDate());
        assertThat(dto.getAppearance()).isEqualTo(AnnouncementAppearanceDTO.ALL);

        // create announcement without body
        var exception = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .when()
                .contentType(APPLICATION_JSON)
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(exception.getErrorCode()).isEqualTo(AnnouncementMapper.ErrorKeys.CONSTRAINT_VIOLATIONS.name());
        assertThat(exception.getDetail()).isEqualTo("createAnnouncement.createAnnouncementRequestDTO: must not be null");

        // create announcement with existing name
        dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient")).when()
                .contentType(APPLICATION_JSON)
                .body(createDto)
                .post()
                .then()
                .statusCode(CREATED.getStatusCode())
                .extract().as(AnnouncementDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getProductName()).isNotNull().isEqualTo(createDto.getProductName());
        assertThat(dto.getTitle()).isNotNull().isEqualTo(createDto.getTitle());
    }

    @Test
    void deleteAnnouncementTest() {
        // delete
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .delete("a1")
                .then().statusCode(NO_CONTENT.getStatusCode());

        // check if exists
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .get("a1")
                .then().statusCode(NOT_FOUND.getStatusCode());

        // delete
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .delete("a1")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());
    }

    @Test
    void getAnnouncementTest() {
        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .get("a2")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(AnnouncementDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getProductName()).isEqualTo("product2");
        assertThat(dto.getTitle()).isEqualTo("title2");

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .get("does-not-exists")
                .then().statusCode(NOT_FOUND.getStatusCode());
    }

    @Test
    void updateAnnouncementTest() {

        var updateDto = new UpdateAnnouncementRequestDTO();

        updateDto.setTitle("test01");
        updateDto.setStartDate(OffsetDateTime.parse("2000-03-10T12:15:50-04:00"));
        updateDto.setModificationCount(0);

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .body(updateDto)
                .when()
                .put("does-not-exists")
                .then()
                .statusCode(NOT_FOUND.getStatusCode());

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .body(updateDto)
                .when()
                .put("a1")
                .then().statusCode(OK.getStatusCode());

        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient")).contentType(APPLICATION_JSON)
                .body(updateDto)
                .when()
                .get("a1")
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(AnnouncementDTO.class);

        // update theme with wrong modificationCount
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .body(updateDto)
                .when()
                .put("a1")
                .then().statusCode(BAD_REQUEST.getStatusCode());

        assertThat(dto).isNotNull();
        assertThat(dto.getTitle()).isEqualTo(updateDto.getTitle());
        assertThat(dto.getStartDate()).isEqualTo(updateDto.getStartDate());
    }

    @Test
    void updateAnnouncementWithoutBodyTest() {

        var exception = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .when()
                .put("update_create_new")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getErrorCode()).isEqualTo(AnnouncementMapper.ErrorKeys.CONSTRAINT_VIOLATIONS.name());
        assertThat(exception.getDetail()).isEqualTo("updateAnnouncementById.updateAnnouncementRequestDTO: must not be null");
        assertThat(exception.getInvalidParams()).isNotNull().asInstanceOf(InstanceOfAssertFactories.LIST).hasSize(1);

    }

    @Test
    void getAllAnnouncementProductsTest() {
        var dto = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .get("products")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(AnnouncementProductsDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getProductNames()).isNotNull().asInstanceOf(InstanceOfAssertFactories.LIST).hasSize(2);
    }

    @Test
    void getAnnouncementsByCriteriaOrg200Test() {
        AnnouncementSearchCriteriaDTO criteria = new AnnouncementSearchCriteriaDTO();
        criteria.setProductName("product2");
        criteria.setWorkspaceName("workspace2");
        criteria.setStatus(List.of(AnnouncementStatusDTO.ACTIVE));
        criteria.setPriority(List.of(AnnouncementPriorityTypeDTO.NORMAL));
        criteria.setType(List.of(AnnouncementTypeDTO.EVENT));
        criteria.setStartDateFrom(OffsetDateTime.parse("2000-03-10T12:15:50-04:00"));
        criteria.setStartDateTo(OffsetDateTime.parse("2023-03-10T12:15:50-04:00"));
        criteria.setEndDateFrom(OffsetDateTime.parse("2000-03-10T12:15:50-04:00"));
        criteria.setEndDateTo(OffsetDateTime.parse("2023-03-10T12:15:50-04:00"));

        var data = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org2"))
                .body(criteria)
                .post("search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(AnnouncementPageResultDTO.class);

        Assertions.assertThat(data).isNotNull();
        Assertions.assertThat(data.getTotalElements()).isEqualTo(1);
        Assertions.assertThat(data.getStream()).isNotNull().hasSize(1);
        Assertions.assertThat(data.getStream().get(0)).isNotNull();
        Assertions.assertThat(data.getStream().get(0).getId()).isEqualTo("a3-200");
    }
}
