package org.mel.zktest;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class TicketNo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String no;

    public TicketNo(String no) {
        this.no = no;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
