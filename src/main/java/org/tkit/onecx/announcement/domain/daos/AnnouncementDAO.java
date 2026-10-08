package org.tkit.onecx.announcement.domain.daos;

import static org.tkit.quarkus.jpa.utils.QueryCriteriaUtil.addSearchStringPredicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.*;

import org.tkit.onecx.announcement.domain.criteria.AnnouncementBannerSearchCriteria;
import org.tkit.onecx.announcement.domain.criteria.AnnouncementSearchCriteria;
import org.tkit.onecx.announcement.domain.models.Announcement;
import org.tkit.onecx.announcement.domain.models.Announcement_;
import org.tkit.quarkus.jpa.daos.AbstractDAO;
import org.tkit.quarkus.jpa.daos.Page;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.jpa.exceptions.DAOException;
import org.tkit.quarkus.jpa.models.AbstractTraceableEntity_;
import org.tkit.quarkus.jpa.models.TraceableEntity_;

import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class AnnouncementDAO extends AbstractDAO<Announcement> {

    // https://hibernate.atlassian.net/browse/HHH-16830#icft=HHH-16830
    @Override
    public Announcement findById(Object id) throws DAOException {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Announcement.class);
            var root = cq.from(Announcement.class);
            cq.where(cb.equal(root.get(TraceableEntity_.ID), id));
            return this.getEntityManager().createQuery(cq).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        } catch (Exception e) {
            throw new DAOException(ErrorKeys.FIND_ENTITY_BY_ID_FAILED, e, entityName, id);
        }
    }

    public PageResult<Announcement> loadAnnouncementByCriteria(AnnouncementSearchCriteria criteria) {
        try {
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<Announcement> cq = cb.createQuery(Announcement.class);
            Root<Announcement> root = cq.from(Announcement.class);
            cq.select(root).distinct(true);

            List<Predicate> predicates = new ArrayList<>();
            if (criteria.getProductName() != null) {
                predicates.add(cb.equal(root.get(Announcement_.PRODUCT_NAME), criteria.getProductName()));
            }
            if (criteria.getWorkspaceName() != null) {
                predicates.add(cb.equal(root.get(Announcement_.WORKSPACE_NAME), criteria.getWorkspaceName()));
            }
            addDatePredicates(criteria, cb, root, predicates);
            if (criteria.getPriority() != null && !criteria.getPriority().isEmpty()) {
                predicates.add(root.get(Announcement_.PRIORITY).in(criteria.getPriority()));
            }
            if (criteria.getStatus() != null && !criteria.getStatus().isEmpty()) {
                predicates.add(root.get(Announcement_.STATUS).in(criteria.getStatus()));
            }
            if (criteria.getType() != null && !criteria.getType().isEmpty()) {
                predicates.add(root.get(Announcement_.TYPE).in(criteria.getType()));
            }
            addSearchStringPredicate(predicates, cb, root.get(Announcement_.title), criteria.getTitle());
            if (criteria.getAppearance() != null && criteria.getAppearance().length > 0) {
                cq.where(cb.and(root.get(Announcement_.APPEARANCE).in(Arrays.stream(criteria.getAppearance()).toList())));
            }
            if (!predicates.isEmpty()) {
                cq.where(cb.and(predicates.toArray(new Predicate[0])));
            }

            //do query and sort resultList by Priority and creation-date
            cq.orderBy(cb.desc(root.get(AbstractTraceableEntity_.CREATION_DATE)));
            return createPageQuery(cq, Page.of(criteria.getPageNumber(), criteria.getPageSize())).getPageResult();
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_LOAD_ANNOUNCEMENT_BY_CRITERIA, ex);
        }

    }

    private void addDatePredicates(AnnouncementSearchCriteria criteria, CriteriaBuilder cb,
            Root<Announcement> root, List<Predicate> predicates) {
        if (criteria.getStartDateFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get(Announcement_.START_DATE),
                    criteria.getStartDateFrom().toLocalDateTime()));
        }
        if (criteria.getStartDateTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get(Announcement_.START_DATE),
                    criteria.getStartDateTo().toLocalDateTime()));
        }
        if (criteria.getEndDateFrom() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get(Announcement_.END_DATE),
                    criteria.getEndDateFrom().toLocalDateTime()));
        }
        if (criteria.getEndDateTo() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get(Announcement_.END_DATE),
                    criteria.getEndDateTo().toLocalDateTime()));
        }
    }

    public List<String> findProductsWithAnnouncements() {
        try {
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<String> cq = cb.createQuery(String.class);
            Root<Announcement> root = cq.from(Announcement.class);
            cq.select(root.get(Announcement_.PRODUCT_NAME)).distinct(true);
            cq.where(root.get(Announcement_.PRODUCT_NAME).isNotNull());
            return getEntityManager().createQuery(cq).getResultList();
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_FIND_PRODUCTS_WITH_ANNOUNCEMENTS, ex);
        }
    }

    public List<String> findWorkspacesWithAnnouncements() {
        try {
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<String> cq = cb.createQuery(String.class);
            Root<Announcement> root = cq.from(Announcement.class);
            cq.select(root.get(Announcement_.WORKSPACE_NAME)).distinct(true);
            cq.where(root.get(Announcement_.WORKSPACE_NAME).isNotNull());
            return getEntityManager().createQuery(cq).getResultList();
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_FIND_WORKSPACES_WITH_ANNOUNCEMENTS, ex);
        }
    }

    public PageResult<Announcement> loadAnnouncementBannersByCriteria(AnnouncementBannerSearchCriteria criteria) {

        try {
            CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
            CriteriaQuery<Announcement> cq = cb.createQuery(Announcement.class);
            Root<Announcement> root = cq.from(Announcement.class);
            cq.select(root).distinct(true);

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get(Announcement_.STATUS), Announcement.Status.ACTIVE));
            if (criteria.getProductName() != null) {
                predicates.add(cb.or(cb.equal(root.get(Announcement_.PRODUCT_NAME), criteria.getProductName()),
                        cb.isNull(root.get(Announcement_.PRODUCT_NAME))));
            }
            if (criteria.getWorkspaceName() != null) {
                predicates.add(cb.or(cb.equal(root.get(Announcement_.WORKSPACE_NAME), criteria.getWorkspaceName()),
                        cb.isNull(root.get(Announcement_.WORKSPACE_NAME))));
            }
            predicates.add(cb.lessThanOrEqualTo(root.get(Announcement_.START_DATE),
                    criteria.getCurrentDate().toLocalDateTime()));

            predicates.add(cb.or(cb.greaterThanOrEqualTo(root.get(Announcement_.END_DATE),
                    criteria.getCurrentDate().toLocalDateTime()),
                    cb.isNull(root.get(Announcement_.END_DATE))));
            if (criteria.getAppearance() != null && criteria.getAppearance().length > 0) {
                cq.where(cb.and(root.get(Announcement_.APPEARANCE).in(Arrays.stream(criteria.getAppearance()).toList())));
            }
            cq.where(cb.and(predicates.toArray(new Predicate[0])));

            //do query and sort resultList by Priority and creation-date
            cq.orderBy(cb.desc(root.get(AbstractTraceableEntity_.CREATION_DATE)));
            return createPageQuery(cq, Page.of(criteria.getPageNumber(), criteria.getPageSize())).getPageResult();
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_LOAD_ANNOUNCEMENT_BY_CRITERIA, ex);
        }
    }

    public enum ErrorKeys {

        FIND_ENTITY_BY_ID_FAILED,
        ERROR_LOAD_ANNOUNCEMENT_BY_CRITERIA,
        ERROR_FIND_PRODUCTS_WITH_ANNOUNCEMENTS,
        ERROR_FIND_WORKSPACES_WITH_ANNOUNCEMENTS
    }

}
