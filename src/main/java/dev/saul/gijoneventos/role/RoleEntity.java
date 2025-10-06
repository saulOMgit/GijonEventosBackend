package dev.saul.gijoneventos.role;

import java.util.Set;

import dev.saul.gijoneventos.user.UserEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "roles")
@Data
//
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
//
public class RoleEntity {
    
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_role")
    private Long id;
    private String name;

    @JsonIgnore
    @ManyToMany(mappedBy = "roles")
    private Set<UserEntity> users;

    /**
     * Creates a predefined <b>Admin role</b>.
     * This is useful for initializing the DB with standard roles.
     * 
     * @return Role with administrator privileges (full access to patient management
     *         and all appointments).
     */
    public static RoleEntity adminRole() {
        return RoleEntity.builder()
                .name("ROLE_ADMIN")
           //     .description("Administrator with full access")
                .build();
    }
    /**
     * Creates a predefined <b>Client role</b>
     * This is useful for initializing the DB with standard roles.
     * 
     * @return Role with client privileges (limited access to only their own
     *         appointments)
     */
    public static RoleEntity clientRole() {
        return RoleEntity.builder()
                .name("ROLE_CLIENT")
        //        .description("Client with limited access to own data")
                .build();
    }

}
