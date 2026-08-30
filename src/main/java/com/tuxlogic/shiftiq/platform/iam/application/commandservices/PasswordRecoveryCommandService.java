package com.tuxlogic.shiftiq.platform.iam.application.commandservices;

import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.GeneratePasswordRecoveryTokenCommand;
import com.tuxlogic.shiftiq.platform.iam.domain.model.commands.ResetPasswordCommand;

public interface PasswordRecoveryCommandService {
    void handle(GeneratePasswordRecoveryTokenCommand command);
    void handle(ResetPasswordCommand command);
}
