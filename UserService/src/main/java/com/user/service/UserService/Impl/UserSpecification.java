package com.user.service.UserService.Impl;

import com.user.service.UserService.entities.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<User> build(
            String name,
            String userId,
            String phone,
            String email,
            String lastId
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            //Search Filters
            if (name != null && !name.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), name.toLowerCase() + "%"));
            }
            if (userId != null && !userId.isEmpty()) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }
            if (email != null && !email.isEmpty()) {
                predicates.add(cb.equal(root.get("email"), email));
            }
            if (phone != null && !phone.isEmpty()) {
                predicates.add(cb.equal(root.get("phone"), phone));
            }


            // ------fetched records with an ID greater than the lastId to avoid offsets

            if (lastId != null && !lastId.isEmpty()) {
                predicates.add(cb.greaterThan(root.get("userId"), lastId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}