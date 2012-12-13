package transbit.tbits.upgrade;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Properties;

import transbit.tbits.common.Configuration;

public class AppUpgradRegistry {
	private static final String COMMON = "common";
	private static final String UPGRADE_CLASSES_PROPERTIES = "upgradeclasses.properties";
	Properties props = null;
	private static AppUpgradRegistry instance = null;

	public static AppUpgradRegistry get() throws Exception {
		if (instance == null)
			instance = new AppUpgradRegistry();
		return instance;
	}

	public ArrayList<IUpgrade> getUpgradeAppAfter(String folder, String sysType) {
		return getUpgradeApp(folder, sysType, "after");
	}

	public ArrayList<IUpgrade> getUpgradeAppBefore(String folder, String sysType) {
		return getUpgradeApp(folder, sysType, "before");
	}

	private ArrayList<IUpgrade> getUpgradeApp(String folder, String sysType,
			String order) {
		ArrayList<IUpgrade> upgrades = new ArrayList<IUpgrade>();
		String key = folder + "-" + COMMON + "-" + order;
		IUpgrade upgrade = getObjectForKey(key);
		if (upgrade != null) {
			upgrades.add(upgrade);
		}

		key = folder + "-" + sysType + "-" + order;
		upgrade = getObjectForKey(key);
		if (upgrade != null) {
			upgrades.add(upgrade);
		}
		return upgrades;
	}

	private IUpgrade getObjectForKey(String key) {
		String className = props.getProperty(key);
		IUpgrade obj = null;
		if ((className != null) && (className.trim().length() != 0)) {
			try {
				obj = (IUpgrade) Class.forName(className).newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return obj;
	}

	private AppUpgradRegistry() throws Exception {
		File file = Configuration.findPath("etc/" + UPGRADE_CLASSES_PROPERTIES);
		if (!file.exists())
			throw new Exception("File doesnt exist.");
		props = new Properties();
		props.load(new FileInputStream(file));
	}

}