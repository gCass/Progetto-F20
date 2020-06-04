package model.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.catalog.ComponentCatalog;
import model.component.Component;
import model.configuration.Configuration;
import model.customer.Customer;

@SuppressWarnings("unused")
public class RdbConfigurationDAO implements InterfaceConfigurationDAO {

	private RdbOperation dbop;

	public RdbConfigurationDAO(RdbOperation dbop) {
		this.dbop = dbop;
	}

	public Configuration getConfiguration(String confId) {

		ResultSet rs = dbop.getConfiguration(confId);
		String ModelofC;
		String Name;
		String Email;
		ComponentCatalog catalog = new ComponentCatalog();
		List<Component> addedComponent = new ArrayList<Component>();
		int cont = 0;
		try {
			while (rs.next()) {
				if (cont == 0) {
					Name = rs.getString("Name");
					Email = rs.getString("EmailU");
					cont++;
				}
				ModelofC = rs.getString("ModelofC");
				addedComponent.add(catalog.getComponentByModel(ModelofC));
			}
			return new Configuration(addedComponent);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	public List<Configuration> getConfigurationByEmail(String email) {

		ResultSet rs = dbop.getConfigurationByEmail(email);
		Map<Integer, Configuration> confMap = new HashMap<>();
		String ModelofC;
		int Id;
		String Name;
		String Email;
		List<Configuration> configurations = null;
		ComponentCatalog catalog = new ComponentCatalog();
		Component c = null;
		try {
			while (rs.next()) {
				Id = rs.getInt("Id");
				if (!confMap.containsKey(Id)) {
					confMap.put(Id, new Configuration());
				}
				ModelofC = rs.getString("ModelofC");
				c = catalog.getComponentByModel(ModelofC);
				confMap.get(Id).addComponentWithoutChecking(c);
			}

			configurations = new ArrayList<Configuration>(confMap.values());
			return configurations;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	public boolean addConfiguration(Configuration conf, Customer user) {
		List<String> type = new ArrayList<String>();
		List<String> model = new ArrayList<String>();
		for (Component c : conf.getAddedComponents()) {
			type.add(c.getTypeComponent());
			model.add(c.getModel());
		}

		return dbop.addConfiguration(conf.getId(), conf.getName(), user.getEmail(), type, model);

	}

	public boolean updateConfiguration(Configuration conf, Customer user) {
		boolean flag1 = this.removeConfiguration(conf.getId());
		boolean flag2 = false;
		if (flag1) {
			flag2 = this.addConfiguration(conf, user);
		}

		return flag2;

	}

	public boolean removeConfiguration(int confId) {

		return dbop.removeConfiguration(confId);

	}

	@Override
	public boolean addUsers(String name, String cognome, String email, String password) {
		// TODO Auto-generated method stub
		return dbop.addUser(name, cognome, email, password);
	}

}
