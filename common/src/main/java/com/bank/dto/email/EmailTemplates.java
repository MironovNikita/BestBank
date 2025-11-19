package com.bank.dto.email;

public class EmailTemplates {
    public static final String REGISTRATION_SUBJECT = "Поздравляем с регистрацией!";
    public static final String PASSWORD_CHANGE_SUBJECT = "Пароль успешно изменён";
    public static final String ACCOUNT_CHANGE_SUBJECT = "Данные аккаунта успешно изменены";
    public static final String CASH_OPERATION_SUBJECT = "Операция с наличными успешно завершена";
    public static final String TRANSFER_OPERATION_SUBJECT = "Перевод средств успешно совершён";

    public static final String REGISTRATION_TEXT = """
            Дорогой %s %s!
            
            Благодарим Вас за регистрацию в нашем банке.
            Желаем Вам успешного развития с продуктами нашего банка!""";

    public static final String PASSWORD_CHANGE_TEXT = """
            Дорогой пользователь!
            
            Ваш пароль был успешно обновлён.
            Если это были не Вы, срочно свяжитесь с нашим банком!""";

    public static final String ACCOUNT_CHANGE_TEXT = """
            Дорогой пользователь!
            
            Ваши данные были успешно обновлены.
            Если это были не Вы, срочно свяжитесь с нашим банком!""";

    public static final String CASH_OPERATION_TEXT = """
            Дорогой пользователь!
            
            Операция с наличными успешно проведена.
            Если это были не Вы, срочно свяжитесь с нашим банком!""";

    public static final String TRANSFER_CHANGE_TEXT = """
            Дорогой пользователь!
            
            Операция с наличными успешно проведена.
            Если это были не Вы, срочно свяжитесь с нашим банком!""";
}
