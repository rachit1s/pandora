package transbit.tbits.upgrade;

import java.sql.Connection;
import java.sql.SQLException;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.exception.TBitsException;
/**
 * A post or pre-hook of the Upgrade system needs to implement this Interface. 
 * This is either executed before or after the db upgrade but in the same transaction.
 * @author sandeepgiri
 *
 */
public interface IUpgrade {
	/**
	 * Runs the upgrade before or after the db upgrade.
	 * @param conn The connection object which have/will run the db scripts
	 * @param folder The folder whose name is the version number.
	 * @param sysType The flavour of tBits which is being upgraded.
	 * @return true if successful or false if not. The upgrade done as part of the batch scripts will be rolled back for this version.
	 * @throws SQLException
	 * @throws DatabaseException
	 * @throws TBitsException
	 */
	boolean upgrade(Connection conn, String folder, String sysType) throws SQLException, DatabaseException, TBitsException;
}
