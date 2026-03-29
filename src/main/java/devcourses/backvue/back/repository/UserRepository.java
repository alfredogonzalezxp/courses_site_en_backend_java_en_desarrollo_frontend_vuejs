package devcourses.backvue.back.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import devcourses.backvue.back.model.User;

/*
This code defines an interface that acts as your data 
access layer for User entities. You don't have to write 
the implementation class yourself; Spring creates it for 
you at runtime.

public interface UserRepository  This declares a standard
 Java interface.

extends JpaRepository<User, Long>: This is where the 
magic happens. By extending JpaRepository, your 
UserRepository automatically inherits a full set of 
standard CRUD (Create, Read, Update, Delete) methods, 
such as save(), findById(), findAll(), and deleteById().

The <User, Long> part tells Spring Data JPA two 
things:
User: This repository is responsible for managing User 
entities.
Long: The primary key (@Id) of the User entity is of 
type Long
*/

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}