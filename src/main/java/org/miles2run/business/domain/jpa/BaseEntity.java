package org.miles2run.business.domain.jpa;

import org.hibernate.annotations.Type;

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

    @Column(updatable = false)
    @Type(type = "org.jadira.usertype.dateandtime.legacyjdk.PersistentDate")
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
