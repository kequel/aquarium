package pl.edu.pg.eti.kask.aquarium.security;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Address the identity provider returns to after logging in (redirectURI in {@link OpenIdConfig}).
 * The OpenID mechanism has already logged the user in when this servlet is called, it only shows the home page.
 */
@WebServlet("/callback")
public class CallbackServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/aquarium/list.xhtml");
    }
}
