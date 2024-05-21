package roomescape.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.Role;
import roomescape.service.dto.request.LoginMember;

import java.util.Objects;

public class CheckAdminInterceptor implements HandlerInterceptor {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        logger.trace("admin request = {}", request.getRequestURI());
        LoginMember member = getLoginMember(request);

        if (Role.ADMIN != member.role()) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return false;
        }

        return true;
    }

    private LoginMember getLoginMember(HttpServletRequest request) {
        return (LoginMember) Objects.requireNonNull(request)
                .getAttribute(CheckLoginInterceptor.LOGIN_MEMBER_REQUEST);
    }
}
