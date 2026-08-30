package com.andeva.atelier.platform.iam.application.commandservices;

import com.andeva.atelier.platform.iam.domain.model.commands.GeneratePasswordRecoveryTokenCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.ResetPasswordCommand;

public interface PasswordRecoveryCommandService {
    void handle(GeneratePasswordRecoveryTokenCommand command);
    void handle(ResetPasswordCommand command);
}
