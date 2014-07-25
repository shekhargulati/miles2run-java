package org.miles2run.business.domain.jpa;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by shekhargulati on 07/07/14.
 */
@MappedSuperclass
public abstract class BaseEntity {


    @Id
    @TableGenerator(name = "id_generator", table = "id_gen", allocationSize = 100)
    @GeneratedValue(generator = "id_generator")
    Long id;

    @Version
    Long version;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    Date createdAt = new Date();


    public Long getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
}
