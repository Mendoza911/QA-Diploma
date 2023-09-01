package org.example.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SQLHelper {
    private static QueryRunner runner = new QueryRunner();

    private SQLHelper() {
    }

    private static Connection getConn() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/app", "app", "pass");
    }

    @SneakyThrows

    public static String getUserPaymentId() { // сущность заказа
        var codeSQL = "SELECT payment_id FROM order_entity ORDER BY created DESC LIMIT 1";
        var conn = getConn();
        var paymentId = runner.query(conn, codeSQL, new ScalarHandler<String>());
        return paymentId;
    }


    @Test
    @SneakyThrows
    public static String[] getPayData() {// платежная организация
        var codeSQL = "SELECT * FROM payment_entity ORDER BY created DESC LIMIT 1";
        var conn = getConn();
        var result = runner.query(conn, codeSQL, new BeanHandler<>(PaymentEntity.class));
        return new String[] {result.getTransaction_id(), result.getStatus()};
    }

    @Data
    @NoArgsConstructor
    public static class PaymentEntity {
        private String id;
        private Integer amount;
        private String created;
        private String status;
        private String transaction_id;
    }
    @Test
    @SneakyThrows
    public static String[] getCreditData() {// сущность кредитного запроса
        var codeSQL = "SELECT * FROM credit_request_entity ORDER BY created DESC LIMIT 1";
        var conn = getConn();
        var result = runner.query(conn, codeSQL, new BeanHandler<>(CreditRequest.class));
        return new String[] {result.getBank_id(), result.getStatus()};
    }

    @Data
    @NoArgsConstructor
    public static class CreditRequest {
        private String id;
        private String bank_id;
        private String created;
        private String  status;
    }
}