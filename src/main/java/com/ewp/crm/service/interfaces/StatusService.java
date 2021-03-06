package com.ewp.crm.service.interfaces;

import com.ewp.crm.models.SortedStatuses.SortingType;
import com.ewp.crm.models.Status;
import com.ewp.crm.models.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;
import java.util.Optional;

public interface StatusService {

    List<Status> getAll();

    List<Status> getStatusesWithSortedClients(@AuthenticationPrincipal User userFromSession);

    Optional<Status> get(Long id);

    Optional<Status> get(String name);

    Optional<Status> getFirstStatusForClient();

    Optional<Status> getRepeatedStatusForClient();

    Optional<Status> getStatusByName(String name);

	void add(Status status);

    void addInit(Status status);

    void update(Status status);

    void delete(Status status);

    void delete(Long id);

    Optional<Long> findMaxPosition();

    List<Status> getAllStatusesForStudents();

    void setNewOrderForChosenStatusForCurrentUser(SortingType newOrder, Long statusId, User currentUser);


}
