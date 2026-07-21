package com.fmud.auth.application.port.in;

import com.fmud.auth.application.command.LoginCommand;
import com.fmud.auth.application.dto.LoginResultDto;

public interface LoginUseCase {
    LoginResultDto login(LoginCommand command);
}
