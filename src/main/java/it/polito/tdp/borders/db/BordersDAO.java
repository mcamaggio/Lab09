package it.polito.tdp.borders.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polito.tdp.borders.model.Border;
import it.polito.tdp.borders.model.Country;
import it.polito.tdp.borders.model.CountryIdMap;

public class BordersDAO {

	public List<Country> loadAllCountries(CountryIdMap countryIdMap) {

		String sql = "SELECT ccode, StateAbb, StateNme FROM country ORDER BY StateAbb";
		List<Country> result = new ArrayList<Country>();
		
		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				Country c = new Country(rs.getInt("ccode"), rs.getString("StateAbb"), rs.getString("StateNme"));
				result.add(countryIdMap.get(c));
			}
			
			conn.close();
			return result;

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
	}

	public List<Border> getCountryPairs(CountryIdMap countryIdMap, int anno) {

		String sql = "SELECT state1no, state2no \r\n"
				+ "FROM contiguity \r\n"
				+ "WHERE contiguity.conttype = 1 AND contiguity.year <= ?";
		
		List<Border> result = new ArrayList<Border>();
		
		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			st.setInt(1, anno);
			ResultSet rs = st.executeQuery();
			
			while(rs.next()) {
				int c1Code = rs.getInt("state1no");
				int c2Code = rs.getInt("state2no");
				
				// The identity map guarantees the uniqueness of c1 and c2 objects
				Country c1 = countryIdMap.get(c1Code);
				Country c2 = countryIdMap.get(c2Code);
				
				// Just check that c1 and c2 abject really exist, otherwise skip them
				if(c1 != null && c2 != null) {
					result.add(new Border(c1, c2));
				} else {
					System.out.println("Error skipping " + String.valueOf(c1Code) + " - " + String.valueOf(c2Code));
				}
				
				Border b = new Border(c1, c2);
				result.add(b);
			}

			conn.close();
			return result;

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
	}
}
