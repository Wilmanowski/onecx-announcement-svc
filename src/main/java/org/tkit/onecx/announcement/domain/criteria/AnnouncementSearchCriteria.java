package org.tkit.onecx.announcement.domain.criteria;

import java.time.OffsetDateTime;
import java.util.List;

import jakarta.validation.Valid;

import org.tkit.onecx.announcement.domain.models.Announcement;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@RegisterForReflection
public class AnnouncementSearchCriteria {

    private List<Announcement.Type> type;

    private List<Announcement.Priority> priority;

    private List<Announcement.Status> status;

    private @Valid OffsetDateTime startDateFrom;

    private @Valid OffsetDateTime startDateTo;

    private @Valid OffsetDateTime endDateFrom;

    private @Valid OffsetDateTime endDateTo;

    private @Valid String productName;

    private @Valid Integer pageNumber = 0;

    private @Valid Integer pageSize = 100;

    private @Valid String workspaceName;

    private Announcement.Appearance[] appearance;

    private String title;
}
