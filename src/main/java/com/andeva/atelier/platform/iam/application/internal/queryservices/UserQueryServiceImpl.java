package com.andeva.atelier.platform.iam.application.internal.queryservices;

import com.andeva.atelier.platform.iam.domain.model.aggregates.User;
import com.andeva.atelier.platform.iam.domain.model.queries.GetUserByEmailQuery;
import com.andeva.atelier.platform.iam.domain.model.queries.GetUserByIdQuery;
import com.andeva.atelier.platform.iam.domain.repositories.UserRepository;
import com.andeva.atelier.platform.iam.application.queryservices.UserQueryService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;

    public UserQueryServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> handle(GetUserByIdQuery query) {
        return userRepository.findById(query.userId().value());
    }

    @Override
    public Optional<User> handle(GetUserByEmailQuery query) {
        return userRepository.findByEmail(query.email().value());
    }
}
