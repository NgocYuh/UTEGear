package com.utegear.demo;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Explicit development operation; never registered as an automatic application service. */
public final class DemoBranchProvisioner {
    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwords;
    private final SecureRandom random = new SecureRandom();

    public DemoBranchProvisioner(JdbcTemplate jdbc, PasswordEncoder passwords) {
        this.jdbc = jdbc;
        this.passwords = passwords;
    }

    public record Branch(long storeId, String storeCode, String name) { }
    public record Account(long userId, String employeeCode, long storeId, String email, String password) { }
    public record Report(List<Branch> branches, List<Account> employees) { }

    // Caller must supply one transaction for the complete operation and report export.
    public Report provision() {
        List<Branch> branches = jdbc.query("SELECT id, name FROM stores ORDER BY id",
                (row, index) -> new Branch(row.getLong("id"), "CN-" + row.getLong("id"), row.getString("name")));
        if (branches.size() != 1 && branches.size() != 3) {
            throw new IllegalStateException("Expected one existing store, or the three previously provisioned demo stores");
        }
        if (jdbc.queryForObject("SELECT count(*) FROM stores WHERE active = false", Long.class) != 0) {
            throw new IllegalStateException("Demo stores must be active; existing stores will not be modified");
        }
        Long roleId = jdbc.queryForObject("SELECT id FROM roles WHERE name = 'EMPLOYEE'", Long.class);
        branches = new ArrayList<>(branches);
        if (branches.size() == 1) {
            for (String[] location : List.of(
                    new String[]{"UTEGear Demo Hà Nội", "Địa chỉ demo - Cầu Giấy, Hà Nội", "Hà Nội", "Cầu Giấy", "hanoi@demo.utegear.test"},
                    new String[]{"UTEGear Demo Đà Nẵng", "Địa chỉ demo - Hải Châu, Đà Nẵng", "Đà Nẵng", "Hải Châu", "danang@demo.utegear.test"})) {
                long id = insert("INSERT INTO stores (name,address,province,district,email,opening_time,closing_time,description,active,created_at,updated_at) VALUES (?,?,?,?,?, '08:00:00','22:00:00',?,true,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)",
                        location[0], location[1], location[2], location[3], location[4], "Chi nhánh development/demo, không phải địa điểm kinh doanh thật");
                branches.add(new Branch(id, "CN-" + id, location[0]));
            }
        } else if (jdbc.queryForObject("SELECT count(*) FROM stores WHERE email IN ('hanoi@demo.utegear.test','danang@demo.utegear.test')", Long.class) != 2) {
            throw new IllegalStateException("The existing three stores are not this demo setup; refusing to modify them");
        }
        List<Account> accounts = new ArrayList<>();
        for (Branch branch : branches) {
            for (int index = 1; index <= 3; index++) {
                String email = "employee.cn" + branch.storeId() + "." + index + "@demo.utegear.test";
                List<Long> existing = jdbc.query("SELECT u.id FROM users u JOIN user_roles ur ON ur.user_id=u.id WHERE lower(u.email)=? AND u.assigned_store_id=? AND u.enabled=true AND ur.role_id=?",
                        (row, n) -> row.getLong(1), email, branch.storeId(), roleId);
                if (!existing.isEmpty()) {
                    accounts.add(new Account(existing.getFirst(), "NV-" + existing.getFirst(), branch.storeId(), email, null));
                    continue;
                }
                if (jdbc.queryForObject("SELECT count(*) FROM users WHERE lower(email)=?", Long.class, email) != 0) {
                    throw new IllegalStateException("Demo email belongs to a different/disabled account: " + email);
                }
                byte[] bytes = new byte[18];
                random.nextBytes(bytes);
                String password = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
                long id = insert("INSERT INTO users (full_name,email,password_hash,enabled,assigned_store_id,created_at,updated_at) VALUES (?,?,?,true,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)",
                        "Nhân viên " + branch.storeCode() + " - " + index, email, passwords.encode(password), branch.storeId());
                jdbc.update("INSERT INTO user_roles (user_id,role_id) VALUES (?,?)", id, roleId);
                accounts.add(new Account(id, "NV-" + id, branch.storeId(), email, password));
            }
            if (jdbc.queryForObject("SELECT count(DISTINCT u.id) FROM users u JOIN user_roles ur ON ur.user_id=u.id WHERE u.assigned_store_id=? AND ur.role_id=?", Long.class, branch.storeId(), roleId) != 3) {
                throw new IllegalStateException("Store already has other employees; refusing to exceed three employees per store");
            }
        }
        return new Report(List.copyOf(branches), List.copyOf(accounts));
    }

    private long insert(String sql, Object... args) {
        GeneratedKeyHolder key = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            var statement = connection.prepareStatement(sql, new String[]{"id"});
            for (int i = 0; i < args.length; i++) statement.setObject(i + 1, args[i]);
            return statement;
        }, key);
        return key.getKey().longValue();
    }
}
