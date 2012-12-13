package transbit.tbits.authentication;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;


/**
 *  This response wrapper class extends the support class HttpServletResponseWrapper,
 *  which implements all the methods in the HttpServletResponse interface, as
 *  delegations to the wrapped response.
 *  You only need to override the methods that you need to change.
 *  You can get access to the wrapped response using the method getResponse()
 */
class ResponseWrapper extends HttpServletResponseWrapper {
    private boolean debug = false;
    public ResponseWrapper(HttpServletResponse response) {
        super(response);
    }
    //
    // You might, for example, wish to know what cookies were set on the response
    // as it went throught the filter chain. Since HttpServletRequest doesn't
    // have a get cookies method, we will need to store them locally as they
    // are being set.
    //
    /*
        protected Vector cookies = null;
     
        //
        // Create a new method that doesn't exist in HttpServletResponse
        //
        public Enumeration getCookies() {
            if (cookies == null)
                cookies = new Vector();
            return cookies.elements();
        }
     
        //
        // Override this method from HttpServletResponse to keep track
        // of cookies locally as well as in the wrapped response.
        //
        public void addCookie (Cookie cookie) {
            if (cookies == null)
                cookies = new Vector();
            cookies.add(cookie);
            ((HttpServletResponse)getResponse()).addCookie(cookie);
        }
     */
}