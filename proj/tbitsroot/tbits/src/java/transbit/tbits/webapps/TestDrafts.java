/*
 * TestDrafts.java
 *
 * Created on July 16, 2006, 12:45 AM
 */

package transbit.tbits.webapps;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Hashtable;
import javax.servlet.*;
import javax.servlet.http.*;
import org.jfree.chart.block.Arrangement;
import transbit.tbits.config.DraftConfig;
import transbit.tbits.domain.UserDraft;
import transbit.tbits.domain.Field;
/**
 *
 * @author Administrator
 * @version
 */
public class TestDrafts extends HttpServlet {
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        crudeProcessing(out);
        out.close();
    }
    
    protected void crudeProcessing(PrintWriter out) {
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Servlet TestDrafts</title>");
        out.println("</head>");
        out.println("<body>");
        
        try{
            ArrayList<UserDraft> drafts = UserDraft.lookupByUserId(2);
            if(drafts.size() > 0) {
                for (UserDraft draft : drafts) {
                    String draftString = draft.getDraft();
                    Field f ;
                    Hashtable draftsHash = DraftConfig.xmlDeSerialize(draftString);
                    out.println("<br>Subject : " + draftsHash.get(Field.SUBJECT));
                    out.println("<br>Summary: " + draftsHash.get(Field.DESCRIPTION));
                    out.println("<hr>");
                    
                }
            } else
                out.print("No drafts.");
        } catch(Exception exp) {
            out.println("Exception Occurred.");
            exp.printStackTrace();
        }
        
        out.println("</body>");
        out.println("</html>");
        
    }
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Short description";
    }
    public static void main(String[] args) {
        TestDrafts testDrafts = new TestDrafts();
        PrintWriter pw = new PrintWriter(System.out, true);
        testDrafts.crudeProcessing(pw);
        System.out.println("DONE!");
    }
    // </editor-fold>
}
