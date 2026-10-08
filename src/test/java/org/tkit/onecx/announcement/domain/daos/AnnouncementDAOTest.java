package org.tkit.onecx.announcement.domain.daos;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Expression;

import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import org.tkit.onecx.announcement.domain.criteria.AnnouncementSearchCriteria;
import org.tkit.onecx.announcement.test.AbstractTest;
import org.tkit.quarkus.jpa.exceptions.DAOException;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class AnnouncementDAOTest extends AbstractTest {

    @Inject
    AnnouncementDAO dao;

    @InjectMock
    EntityManager em;

    @BeforeEach
    void beforeAll() {
        Mockito.when(em.getCriteriaBuilder()).thenThrow(new RuntimeException("Test technical error exception"));
    }

    @Test
    void methodExceptionTests() {
        methodExceptionTests(() -> dao.findById(null),
                AnnouncementDAO.ErrorKeys.FIND_ENTITY_BY_ID_FAILED);
        methodExceptionTests(() -> dao.findProductsWithAnnouncements(),
                AnnouncementDAO.ErrorKeys.ERROR_FIND_PRODUCTS_WITH_ANNOUNCEMENTS);
        methodExceptionTests(() -> dao.loadAnnouncementByCriteria(null),
                AnnouncementDAO.ErrorKeys.ERROR_LOAD_ANNOUNCEMENT_BY_CRITERIA);
        methodExceptionTests(() -> dao.loadAnnouncementBannersByCriteria(null),
                AnnouncementDAO.ErrorKeys.ERROR_LOAD_ANNOUNCEMENT_BY_CRITERIA);
        methodExceptionTests(() -> dao.findWorkspacesWithAnnouncements(),
                AnnouncementDAO.ErrorKeys.ERROR_FIND_WORKSPACES_WITH_ANNOUNCEMENTS);
    }

    @Test
    void emptyFiltersDoNotAddPredicates() {
        HibernateCriteriaBuilder cb = Mockito.mock(HibernateCriteriaBuilder.class, Mockito.RETURNS_DEEP_STUBS);

        Mockito.doReturn(cb).when(em).getCriteriaBuilder();
        Mockito.doThrow(new RuntimeException("Stop after evaluating filters"))
                .when(cb).desc(Mockito.any(Expression.class));

        AnnouncementSearchCriteria criteria = new AnnouncementSearchCriteria();
        criteria.setPriority(List.of());
        criteria.setStatus(List.of());
        criteria.setType(List.of());

        Assertions.assertThrows(DAOException.class, () -> dao.loadAnnouncementByCriteria(criteria));
    }

    void methodExceptionTests(Executable fn, Enum<?> key) {
        var exc = Assertions.assertThrows(DAOException.class, fn);
        Assertions.assertEquals(key, exc.key);
    }

}
