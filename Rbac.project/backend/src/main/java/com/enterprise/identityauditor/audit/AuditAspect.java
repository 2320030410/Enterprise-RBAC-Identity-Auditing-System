package com.enterprise.identityauditor.audit;

import com.enterprise.identityauditor.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Arrays;

@Aspect
@Component
public class AuditAspect {

    private static final Logger logger = LoggerFactory.getLogger(AuditAspect.class);

    @Autowired
    private AuditLogService auditLogService;

    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        String action = auditable.action();
        String description = auditable.description();
        String resource = auditable.resource();

        // Retrieve current authenticated user
        Long userId = null;
        String username = "ANONYMOUS";

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            userId = userDetails.getId();
            username = userDetails.getUsername();
        }

        // Retrieve HTTP request info if available
        String ipAddress = "127.0.0.1";
        String requestUri = resource;

        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                ipAddress = getClientIp(request);
                if (requestUri == null || requestUri.isEmpty()) {
                    requestUri = request.getRequestURI();
                }
            }
        } catch (Exception e) {
            logger.debug("Could not resolve HTTP request attributes: {}", e.getMessage());
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getDeclaringClass().getSimpleName() + "." + method.getName();

        if (description == null || description.isEmpty()) {
            description = "Executed " + methodName;
        }

        // Execute target method
        try {
            Object result = joinPoint.proceed();

            // Record success log
            auditLogService.recordLog(
                    userId,
                    username,
                    action,
                    description,
                    requestUri,
                    "SUCCESS",
                    ipAddress,
                    "Execution completed normally: " + methodName
            );

            return result;
        } catch (AccessDeniedException ade) {
            auditLogService.recordLog(
                    userId,
                    username,
                    action,
                    description + " [UNAUTHORIZED ACCESS ATTEMPT]",
                    requestUri,
                    "UNAUTHORIZED",
                    ipAddress,
                    "Access denied: " + ade.getMessage()
            );
            throw ade;
        } catch (Throwable ex) {
            auditLogService.recordLog(
                    userId,
                    username,
                    action,
                    description + " [FAILED: " + ex.getMessage() + "]",
                    requestUri,
                    "FAILURE",
                    ipAddress,
                    "Exception during execution: " + ex.getClass().getSimpleName() + " - " + ex.getMessage()
            );
            throw ex;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || "unknown".equalsIgnoreCase(xfHeader)) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
