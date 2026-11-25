package pl.edu.pg.eti.kask.aquarium.logging;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.security.enterprise.SecurityContext;
import pl.edu.pg.eti.kask.aquarium.fish.entity.Fish;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * Cross-cutting logging of operations on fish: after a successful call of a method marked with
 * {@link LogOperation} writes the user name, the operation name (the method) and the identifier of the fish
 * to the server log (java.util.logging, the standard logging mechanism). Example line:
 * {@code user=kevin operation=delete id=a1b2c3d4-0001-4000-8000-000000000001}.
 */
@Interceptor
@LogOperation
@Priority(Interceptor.Priority.APPLICATION)
public class LogOperationInterceptor {

    private static final Logger LOG = Logger.getLogger(LogOperationInterceptor.class.getName());

    private final SecurityContext securityContext;

    @Inject
    public LogOperationInterceptor(SecurityContext securityContext) {
        this.securityContext = securityContext;
    }

    @AroundInvoke
    public Object log(InvocationContext context) throws Exception {
        Object result = context.proceed();
        String user = securityContext.getCallerPrincipal() != null
                ? securityContext.getCallerPrincipal().getName() : "anonymous";
        LOG.info("user=" + user + " operation=" + context.getMethod().getName() + " id=" + resourceId(context));
        return result;
    }

    /** The fish is passed either as an object (create, update) or by its identifier (delete). */
    private static Object resourceId(InvocationContext context) {
        for (Object parameter : context.getParameters()) {
            if (parameter instanceof Fish fish) {
                return fish.getId();
            }
            if (parameter instanceof UUID id) {
                return id;
            }
        }
        return null;
    }
}
