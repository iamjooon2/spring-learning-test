package nextstep.helloworld.auth.infrastructure;

import javax.servlet.http.HttpServletRequest;

public interface AuthorizationExtractor<T> {
    final String AUTHORIZATION = "Authorization";

    T extract(HttpServletRequest request);
}
