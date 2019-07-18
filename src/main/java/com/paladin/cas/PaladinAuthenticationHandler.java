package com.paladin.cas;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.sql.DataSource;

import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;


public class PaladinAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

    private final JdbcTemplate jdbcTemplate;
    @SuppressWarnings("unused")
    private final DataSource dataSource;

    public PaladinAuthenticationHandler(String name, ServicesManager servicesManager, PrincipalFactory principalFactory, Integer order, DataSource dataSource) {
        super(name, servicesManager, principalFactory, order);
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private User queryUser(String username) {
        Map<String, Object> result = jdbcTemplate.queryForMap("SELECT `password`,salt, user_id AS id, state FROM sys_user WHERE account=?", username);
        if (result != null) {
            User user = new User();
            user.setId((String) result.get("id"));
            user.setPassword((String) result.get("password"));
            user.setSalt((String) result.get("salt"));
            user.setState((Integer) result.get("state"));
            return user;
        }
        return null;
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(UsernamePasswordCredential credential, String originalPassword)
            throws GeneralSecurityException, PreventedException {

        final String username = credential.getUsername();
        final String password = credential.getPassword();
        User user = null;

        try {
            user = queryUser(username);

            if (user == null) {
                throw new FailedLoginException("No records found for user " + username);
            }

            String dbPassword = user.getPassword();
            String salt = user.getSalt();

            if (dbPassword == null || dbPassword.length() == 0) {
                throw new FailedLoginException("No records found for user " + username);
            }

            if (1 != user.getState()) {
                throw new AccountDisabledException("Account has been disabled");
            }

            String parsePassword = new SimpleHash("md5", password, ByteSource.Util.bytes(salt), 1).toHex();
            if (!dbPassword.equals(parsePassword)) {
                throw new FailedLoginException("Password does not match value on record.");
            }

        } catch (final IncorrectResultSizeDataAccessException e) {
            if (e.getActualSize() == 0) {
                throw new AccountNotFoundException(username + " not found with SQL query");
            }
            throw new FailedLoginException("Multiple records found for " + username);
        } catch (final DataAccessException e) {
            throw new PreventedException("SQL exception while executing query for " + username, e);
        }

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("idcard", "3205831XXXX");

        final Principal principal = this.principalFactory.createPrincipal(username, attributes);
        return createHandlerResult(credential, principal, new ArrayList<>(0));
    }

    public static class User {
        private String id;
        private String password;
        private String salt;
        private int state = -1;

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getSalt() {
            return salt;
        }

        public void setSalt(String salt) {
            this.salt = salt;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getState() {
            return state;
        }

        public void setState(Integer state) {
            this.state = state != null ? state : -1;
        }
    }

}
