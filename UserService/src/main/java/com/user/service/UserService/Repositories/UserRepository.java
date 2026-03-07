package com.user.service.UserService.Repositories;

import com.user.service.UserService.Payload.UserProjection;
import com.user.service.UserService.entities.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User,String> , JpaSpecificationExecutor<User> {


    List<UserProjection> findAllProjectedBy(Specification<User> spec, Pageable pageable);

}
