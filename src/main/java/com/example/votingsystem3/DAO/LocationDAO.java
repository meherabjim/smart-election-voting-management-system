package com.example.votingsystem3.DAO;

import com.example.votingsystem3.models.*;
import java.sql.*;
import java.util.*;

public class LocationDAO {

    public List<Division> getAllDivisions() {
        List<Division> list = new ArrayList<>();
        try(Connection conn = DBUtil.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT id, name FROM division ORDER BY name");
            while(rs.next()) list.add(new Division(rs.getInt(1), rs.getString(2)));
        } catch(Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<District> getDistrictsByDivision(int divisionId) {
        List<District> list = new ArrayList<>();
        try(Connection conn = DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT id, name FROM district WHERE division_id=? ORDER BY name");
            ps.setInt(1, divisionId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) list.add(new District(rs.getInt(1), rs.getString(2), divisionId));
        } catch(Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<Upazila> getUpazilasByDistrict(int districtId) {
        List<Upazila> list = new ArrayList<>();
        try(Connection conn = DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT id, name FROM upazila WHERE district_id=? ORDER BY name");
            ps.setInt(1, districtId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) list.add(new Upazila(rs.getInt(1), rs.getString(2), districtId));
        } catch(Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<PollingStation> getStationsByUpazila(int upazilaId) {
        List<PollingStation> list = new ArrayList<>();
        try(Connection conn = DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT id, name FROM polling_station WHERE upazila_id=? ORDER BY name");
            ps.setInt(1, upazilaId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) list.add(new PollingStation(rs.getInt(1), rs.getString(2), upazilaId));
        } catch(Exception e) { e.printStackTrace(); }
        return list;
    }
}
