import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
public class Customer {
    @Id
    private int id;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;
}
