package org.example.test;

import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.*;
import org.example.data.DataHelper;
import org.example.data.SQLHelper;
import org.example.page.RootPage;
import org.example.page.FormPage;
import org.openqa.selenium.devtools.v113.debugger.Debugger;

import java.sql.SQLData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.codeborne.selenide.Selenide.open;
import static org.example.data.SQLHelper.clearTables;
import static org.junit.jupiter.api.Assertions.*;


class PaymentTest {
    FormPage formPage;

    @BeforeAll
    static void setUpAll() {
        SelenideLogger.addListener("allure", new AllureSelenide());
    }

    @AfterAll
    static void tearDownAll() {
        SelenideLogger.removeListener("allure");
    }

    @BeforeEach
    void setup() throws SQLException {
        open("http://localhost:8080");
        var rootPage = new RootPage();
        formPage = rootPage.openPayPage(0);
        clearTables();
    }
    String getApprovedCard = DataHelper.getApprovedCard();
    String getDeclinedCard = DataHelper.getDeclinedCard();
    Integer indexPage = 0;
    String month = DataHelper.generateDate(2, "MM");
    String year = DataHelper.generateDate(2, "yy");
    String cvc = DataHelper.generateCVC(3);
    String usualName = DataHelper.generateUsualName();
    String nameWithDot = DataHelper.generateNameWithDot();
    String nameWithDash = DataHelper.generateNameWithDash();
    String minName = DataHelper.generateMinName();

    // Позитивные тесты

    @Test
    @DisplayName("Тест 1: Ввод допустимых значений в поля формы")
    void payTurAPPROVEDCard() throws InterruptedException {
        var approvedCardInfo = DataHelper.getCardInfo(getApprovedCard, usualName, cvc, month, year);
        formPage.getUser(approvedCardInfo);
        assertDoesNotThrow(() -> formPage.positiveNotification());
        SQLHelper.PaymentEntity paymentEntity = SQLHelper.getPayData();
        assertEquals("APPROVED", paymentEntity.getStatus());
        assertEquals(SQLHelper.getUserPaymentId(), paymentEntity.getTransaction_id());
    }

    @Test
    @DisplayName("Тест 2: Ввод допустимых значений в поля формы, значение поля Владелец содержит дефис")
    void payTurAPPROVEDCardOwnerWithDash() {
        var approvedCardInfo = DataHelper.getCardInfo(getApprovedCard, nameWithDash, cvc, month, year);
        formPage.getUser(approvedCardInfo);
        assertDoesNotThrow(() -> formPage.positiveNotification());
        SQLHelper.PaymentEntity paymentEntity = SQLHelper.getPayData();
        assertEquals("APPROVED", paymentEntity.getStatus());
        assertEquals(SQLHelper.getUserPaymentId(), paymentEntity.getTransaction_id());
    }

    @Test
    @DisplayName("Тест 3: Ввод допустимых значений в поля формы, значение поля Владелец не содержит дефис")
    void payTurAPPROVEDCardOwnerWitoutDash() {
        var approvedCardInfo = DataHelper.getCardInfo(getApprovedCard, usualName, cvc, month, year);
        formPage.getUser(approvedCardInfo);
        assertDoesNotThrow(() -> formPage.positiveNotification());
        SQLHelper.PaymentEntity paymentEntity = SQLHelper.getPayData();
        assertEquals("APPROVED", paymentEntity.getStatus());
        assertEquals(SQLHelper.getUserPaymentId(), paymentEntity.getTransaction_id());
    }

    @Test
    @DisplayName("Тест 4: Ввод допустимых значений в поля формы, значение поля Владелец содержит точку")
    void payTurAPPROVEDCardOwnerWithDot() {
        var approvedCardInfo = DataHelper.getCardInfo(getApprovedCard, nameWithDot, cvc, month, year);
        formPage.getUser(approvedCardInfo);
        assertDoesNotThrow(() -> formPage.positiveNotification());
        SQLHelper.PaymentEntity paymentEntity = SQLHelper.getPayData();
        assertEquals("APPROVED", paymentEntity.getStatus());
        assertEquals(SQLHelper.getUserPaymentId(), paymentEntity.getTransaction_id());
    }

    @Test
    @DisplayName("Тест 5: Ввод допустимых значений в поля формы, значение поля Владелец четыре символа")
    void payTurAPPROVEDCardOwnerMinName() {
        var approvedCardInfo = DataHelper.getCardInfo(getApprovedCard, minName, cvc, month, year);
        formPage.getUser(approvedCardInfo);
        assertDoesNotThrow(() -> formPage.positiveNotification());
        SQLHelper.PaymentEntity paymentEntity = SQLHelper.getPayData();
        assertEquals("APPROVED", paymentEntity.getStatus());
        assertEquals(SQLHelper.getUserPaymentId(), paymentEntity.getTransaction_id());
    }

    // Негативные тесты

    @Test
    @DisplayName("Тест 1:  Оплата DECLINED картой")
    void payTurDECLINEDCard() {
        var declinedCardInfo = DataHelper.getCardInfo(getDeclinedCard, usualName, cvc, month, year);
        formPage.getUser(declinedCardInfo);
        assertDoesNotThrow(() -> formPage.negativeNotification());
        SQLHelper.PaymentEntity paymentEntity = SQLHelper.getPayData();
        assertEquals("DECLINED", paymentEntity.getStatus());
        assertEquals(SQLHelper.getUserPaymentId(), paymentEntity.getTransaction_id());
    }

    @Test
    @DisplayName("Тест 2: Отправка незаполненных полей формы")
    void sendingBlankForm() {
        var approvedCardInfo = DataHelper.getCardInfo(" ", " ", " ", " ", " ");
        formPage.getUser(approvedCardInfo);
        assertAll(() -> formPage.getValidationMessage(0),
                () -> formPage.getValidationMessage(1),
                () -> formPage.getValidationMessage(2),
                () -> formPage.getValidationMessage(3),
                () -> formPage.getValidationMessage(4));
    }
    @Test
    @DisplayName("Тест 3: Отправка незаполненного поля Номер карты")
    void sendingFormWithoutCardNumber() {
        var approvedCardInfo = DataHelper.getCardInfo(" ", usualName, cvc, month, year);
        formPage.getUser(approvedCardInfo);
        formPage.getValidationMessage(0);
    }

    @Test
    @DisplayName("Тест 4: Отправка незаполненного поля Месяц")
    void sendingFormWithoutMonth() {
        var approvedCardInfo = DataHelper.getCardInfo(getApprovedCard, usualName, cvc, " ", year);
        formPage.getUser(approvedCardInfo);
        formPage.getValidationMessage(1);
    }
    @Test
    @DisplayName("Тест 5: Отправка незаполненного поля Год")
    void sendingFormWithoutYear() {
        var approvedCardInfo = DataHelper.getCardInfo(getApprovedCard, usualName, cvc, month, " ");
        formPage.getUser(approvedCardInfo);
        formPage.getValidationMessage(2);
    }
    @Test
    @DisplayName("Тест 6: Отправка незаполненного поля Владелец")
    void sendingFormWithoutOwner() {
        var approvedCardInfo = DataHelper.getCardInfo(getApprovedCard, " ", cvc, month, year);
        formPage.getUser(approvedCardInfo);
        formPage.getValidationMessage(3);
    }
    @Test
    @DisplayName("Тест 7: Отправка незаполненного поля CVC/CVV")
    void sendingFormWithoutCVC() {
        var approvedCardInfo = DataHelper.getCardInfo(getApprovedCard, usualName, cvc, month, year);
        formPage.getUser(approvedCardInfo);
        formPage.getValidationMessage(4);
    }
    @Test
    @DisplayName("Тест 8: Оплата просроченной картой, срок действия карты истек месяц назад")
    void paymentCardExpiredMonthAgo() {
        String lastMonth = DataHelper.generateDate(-1, "MM");
        var approvedCardInfo = DataHelper.getCardInfo(getApprovedCard, usualName, cvc, lastMonth, year);
        formPage.getUser(approvedCardInfo);
        formPage.getCardHasExpired(1);
    }
    @Test
    @DisplayName("Тест 9: Ввод значения в поле Месяц недопустимого формата (X вместо XX)")
    void enteringMonthNotValidFormat() {
        var approvedCardInfo = DataHelper.getCardInfo(getApprovedCard, usualName, cvc, "2", year);
        formPage.getUser(approvedCardInfo);
        formPage.getWrongFormat(1);
    }
    @Test
    @DisplayName("Тест 10: Ввод несуществующего значения в поле Месяц (20)")
    void eteringNonExistentMonth() {
        var approvedCardInfo = DataHelper.getCardInfo(getApprovedCard, usualName, cvc, "20", year);
        formPage.getUser(approvedCardInfo);
        formPage.getWrongFormatMonth(1);
    }
    @Test
    @DisplayName("Тест 11: Оплата просроченной картой, срок действия истек год назад")
    void cardExpiredYearAgo() {
        String lastYear = DataHelper.generateDate(-12, "yy");
        var approvedCardInfo = DataHelper.getCardInfo(getApprovedCard, usualName, cvc, month, lastYear);
        formPage.getUser(approvedCardInfo);
        formPage.getCardHasExpired(2);
    }
    @Test
    @DisplayName("Тест 12: Ввод недопустимого значения в поле Год (X вместо XX)")
    void enteringInvalidYear() {
        var approvedCardInfo = DataHelper.getCardInfo(getApprovedCard, usualName, cvc, month, "3");
        formPage.getUser(approvedCardInfo);
        formPage.getWrongFormat(2);
    }

    @Test
    @DisplayName("Тест 13: Ввод значения в поле Владелец на кириллице")
    void enteringOwnerOnCyrillic() {
        var approvedCardInfo = DataHelper.getCardInfo(getApprovedCard, "Анна Мария", cvc, month, year);
        formPage.getUser(approvedCardInfo);
        formPage.getWrongFormat(3);
    }
    @Test
    @DisplayName("Тест 14: Ввод значения в поле Владелец строчными латинскими буквами")
    void enteringOwnerLowercaseLetters() {
        var approvedCardInfo = DataHelper.getCardInfo(getApprovedCard, "anna maria", cvc, month, year);
        formPage.getUser(approvedCardInfo);
        formPage.getWrongFormat(3);
    }
    @Test
    @DisplayName("Тест 15: Ввод значения в поле Владелец цифрами")
    void enteringOwnerNumbers() {
        var approvedCardInfo = DataHelper.getCardInfo(getApprovedCard, "123456", cvc, month, year);
        formPage.getUser(approvedCardInfo);
        formPage.getWrongFormat(3);
    }
    @Test
    @DisplayName("Тест 16: Ввод значения в поле Владелец длинной менее допустимого")
    void enteringOwnerLengthLessThanAllowed() {
        var approvedCardInfo = DataHelper.getCardInfo(getApprovedCard, "A", cvc, month, year);
        formPage.getUser(approvedCardInfo);
        formPage.getWrongFormat(3);
    }
    @Test
    @DisplayName("Тест 17: Ввод буквенного значения в поле CVC/CVV")
    void enteringLiteralValueInCVC() {
        var approvedCardInfo = DataHelper.getCardInfo(getApprovedCard, usualName, "COD", month, year);
        formPage.getUser(approvedCardInfo);
        formPage.getValidationMessage(4);
    }
    @Test
    @DisplayName("Тест 18: Ввод значения в поле CVC/CVV длинной менее, чем допустимое")
    void enteringCVCLessThanAllowed() {
        var approvedCardInfo = DataHelper.getCardInfo(getApprovedCard, usualName, "72", month, year);
        formPage.getUser(approvedCardInfo);
        formPage.getWrongFormat(4);
    }
}