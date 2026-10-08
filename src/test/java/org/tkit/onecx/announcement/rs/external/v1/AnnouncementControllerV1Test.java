package org.tkit.onecx.announcement.rs.external.v1;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.tkit.onecx.announcement.rs.v1.controller.AnnouncementControllerV1;
import org.tkit.onecx.announcement.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.announcement.v1.model.*;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(AnnouncementControllerV1.class)
@WithDBData(value = "data/test-v1.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
@GenerateKeycloakClient(clientName = "testClient", scopes = { "ocx-an:read" })
class AnnouncementControllerV1Test extends AbstractTest {

    @Test
    void getAnnouncementsByCriteriaAllTest() {
        AnnouncementSearchCriteriaDTOV1 criteria = new AnnouncementSearchCriteriaDTOV1();

        var data = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post()
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(AnnouncementPageResultDTOV1.class);

        assertThat(data).isNotNull();
        assertThat(data.getTotalElements()).isEqualTo(5);
        assertThat(data.getStream()).isNotNull().hasSize(5);
    }

    @Test
    void getAnnouncementsByCriteriaTest() {
        AnnouncementSearchCriteriaDTOV1 criteria = new AnnouncementSearchCriteriaDTOV1();
        criteria.setProductName("product2");
        criteria.setStatus(List.of(StatusDTOV1.ACTIVE));
        criteria.setPriority(List.of(PriorityDTOV1.NORMAL));
        criteria.setType(List.of(TypeDTOV1.EVENT));
        criteria.setStartDateFrom(OffsetDateTime.parse("2000-03-10T12:15:50-04:00"));
        criteria.setStartDateTo(OffsetDateTime.parse("2023-03-10T12:15:50-04:00"));
        criteria.setEndDateFrom(OffsetDateTime.parse("2000-03-10T12:15:50-04:00"));
        criteria.setEndDateTo(OffsetDateTime.parse("2023-03-10T12:15:50-04:00"));

        var data = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post()
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(AnnouncementPageResultDTOV1.class);

        assertThat(data).isNotNull();
        assertThat(data.getTotalElements()).isEqualTo(2);
        assertThat(data.getStream()).isNotNull().hasSize(2);
    }

    @Test
    void getAnnouncementsByMultipleSelectedFiltersTest() {
        AnnouncementSearchCriteriaDTOV1 criteria = new AnnouncementSearchCriteriaDTOV1();
        criteria.setStatus(List.of(StatusDTOV1.ACTIVE, StatusDTOV1.INACTIVE));
        criteria.setType(List.of(TypeDTOV1.EVENT, TypeDTOV1.INFO));
        criteria.setPriority(List.of(PriorityDTOV1.LOW, PriorityDTOV1.NORMAL));

        var data = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post()
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(AnnouncementPageResultDTOV1.class);

        assertThat(data.getTotalElements()).isEqualTo(4);
        assertThat(data.getStream()).hasSize(4);
        assertThat(data.getStream().stream().map(AnnouncementDTOV1::getStatus).toList())
                .contains(StatusDTOV1.ACTIVE, StatusDTOV1.INACTIVE);
        assertThat(data.getStream().stream().map(AnnouncementDTOV1::getType).toList())
                .contains(TypeDTOV1.EVENT, TypeDTOV1.INFO);
    }

    @Test
    void getAnnouncementsByCriteriaNoBodyTest() {
        var data = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .post()
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(ProblemDetailResponseDTOV1.class);

        assertThat(data).isNotNull();
        assertThat(data.getDetail()).isEqualTo("getAnnouncementsByCriteria.announcementSearchCriteriaDTOV1: must not be null");
    }

    @Test
    void getAnnouncementsByCriteriaOrg1Test() {
        AnnouncementSearchCriteriaDTOV1 criteria = new AnnouncementSearchCriteriaDTOV1();
        criteria.setProductName("product2");
        criteria.setStatus(List.of(StatusDTOV1.ACTIVE));
        criteria.setPriority(List.of(PriorityDTOV1.NORMAL));
        criteria.setType(List.of(TypeDTOV1.EVENT));
        criteria.setStartDateFrom(OffsetDateTime.parse("2000-03-10T12:15:50-04:00"));
        criteria.setStartDateTo(OffsetDateTime.parse("2023-03-10T12:15:50-04:00"));
        criteria.setEndDateFrom(OffsetDateTime.parse("2000-03-10T12:15:50-04:00"));
        criteria.setEndDateTo(OffsetDateTime.parse("2023-03-10T12:15:50-04:00"));

        var data = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .header(APM_HEADER_PARAM, createToken("org1"))
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post()
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(AnnouncementPageResultDTOV1.class);

        assertThat(data).isNotNull();
        assertThat(data.getTotalElements()).isEqualTo(2);
        assertThat(data.getStream()).isNotNull().hasSize(2);
        assertThat(data.getStream().get(0).getEndDate()).isNotNull();
        assertThat(data.getStream().get(0).getStartDate()).isNotNull();
        assertThat(data.getStream().get(0).getStatus()).isNotNull();
        assertThat(data.getStream().get(0).getProductName()).isNotNull();
    }
}
