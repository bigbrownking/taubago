package org.app.courseapp.model.users;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "administrators")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Administrator extends User {
}