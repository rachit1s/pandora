/*
 * IndexerDaemonTest.java
 *
 * Created on October 16, 2006, 2:09 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package transbit.tbits.indexer;

/**
 *
 * @author Administrator
 */
public class IndexerDaemonTest {
    
    /** Creates a new instance of IndexerDaemonTest */
    public IndexerDaemonTest() {
    }
    public static void main(String[] args) {
        System.out.println("Running first Instance=============================");
        IndexerDaemon.main(args);
        
        System.out.println("Running second Instance=============================");
        IndexerDaemon.main(args);
        
        System.out.println("Running third Instance=============================");
        IndexerDaemon.main(args);
        
    }
}
