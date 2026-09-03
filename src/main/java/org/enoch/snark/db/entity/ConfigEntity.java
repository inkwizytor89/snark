package org.enoch.snark.db.entity;

import jakarta.persistence.*;
import lombok.*;
import org.enoch.snark.config.ConfigTerm;

@Entity
@Table(
        name = "config",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_config",
                        columnNames = {"module", "thread", "config_key"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String module;

    private String thread;

    @Column(name = "config_key")
    private String key;

    private String value;

    public ConfigEntity(ConfigTerm configTerm) {
        this.module = configTerm.getModule();
        this.thread = configTerm.getThread();
        this.key = configTerm.getKey();
        this.value = configTerm.getValue();
    }
}