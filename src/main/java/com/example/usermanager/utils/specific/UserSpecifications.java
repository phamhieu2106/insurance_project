package com.example.usermanager.utils.specific;

import com.example.usermanager.domain.entity.UserEntity;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecifications {
    public static final String USER_CODE = "userCode";
    public static final String USER_ROLE = "role";
    public static final String SOFT_DELETE = "softDelete";


    public static Specification<UserEntity> withKeywordAndRole(String keyword, String role) {
        Specification<UserEntity> spec = Specification.where(isSoftDeleteFalse());

        if (!keyword.isBlank()) spec = spec.and(withKeyword(keyword));
        if (!role.isBlank()) spec = spec.and(withRole(role));

        return spec;
    }

    private static Specification<UserEntity> withKeyword(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return null;
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get(USER_CODE)), "%" + keyword.toLowerCase() + "%"));
    }

    private static Specification<UserEntity> withRole(String role) {
        if (role == null || role.isEmpty()) {
            return null;
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(USER_ROLE), role);
    }

    private static Specification<UserEntity> isSoftDeleteFalse() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isFalse(root.get(SOFT_DELETE));
    }
}
