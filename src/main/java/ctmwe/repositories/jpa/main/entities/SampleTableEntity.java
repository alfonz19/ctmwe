package ctmwe.repositories.jpa.main.entities;

import ctmwe.config.Config;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@NoArgsConstructor
@Entity
@Table(name = SampleTableEntity.TABLE_NAME, schema = Config.ENTITY_SCHEMA)
public class SampleTableEntity {

    public static final String TABLE_NAME = "sample_table";

    @Id
    @Column(name = "id")
    private UUID id = UUID.randomUUID();

    @Getter
    @Setter
    @Column(name = "value1", nullable = false)
    private String value1;
    @Getter
    @Setter
    @Column(name = "value2", nullable = false)
    private String value2;
}
