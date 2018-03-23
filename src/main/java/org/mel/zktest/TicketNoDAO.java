package org.mel.zktest;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketNoDAO extends CrudRepository<TicketNo, Long> {
}
