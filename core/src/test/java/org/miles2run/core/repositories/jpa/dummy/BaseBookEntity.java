package org.miles2run.core.repositories.jpa.dummy;

import javax.persistence.*;

@MappedSuperclass
public class BaseBookEntity {

    @Version
    private int version;
    @Id
    @TableGenerator(name = "id_generator", table = "id_gen", allocationSize = 100)
    @GeneratedValue(generator = "id_generator")
    private Long id;

    public Long getId() {
        return id;
    }

    public int getVersion() {
        return version;
    }
}
