Это пример приложения, которое может работать с Microsoft SQL Server.

Шаги для воспроизведения:

1. Установить MS SQL 2019.
    - В ходе настройки включили встроенную аутентификацию и добавили пользователя sa с паролем 123.
    - Не забыли открыть порт 1433 на фаерволе, как предлагает инсталлятор

2. С помощью SQL Management Studio залогиниться в MS SQL (или использовать какой-то другой клиент для выполнения запросов).
3. Создать новую базу, таблицу и заполнить её данными:

```sql
CREATE DATABASE test;

CREATE TABLE test.dbo.Persons (
    ID int IDENTITY(1,1) PRIMARY KEY,
    LastName varchar(255) NOT NULL,
    FirstName varchar(255),
    Age int,
);

INSERT INTO test.dbo.Persons (LastName, FirstName, Age) VALUES ('Oleg', 'Chirukhin', 35);
```
4. Скачать драйвер с официального сайта:

`https://docs.microsoft.com/en-us/sql/connect/jdbc/microsoft-jdbc-driver-for-sql-server?view=sql-server-ver15`

5. Положить файл `mssql-jdbc-10.2.0.jre11.jar` в директорию `lib` в Libercat.

6. В `conf/tomee.xml` настроить датасорс. Вот полное содержимое файла:

```xml
<tomee>
<Resource id="Tutorial" type="javax.sql.DataSource">  
  jdbcDriver = com.microsoft.sqlserver.jdbc.SQLServerDriver
  jdbcUrl = jdbc:sqlserver://localhost:1433;databaseName=test;encrypt=false;
  jtaManaged = true
  userName = sa
  password = 123
  testOnBorrow = true
  testOnReturn = true
  testWhileIdle = true
  validationQuery = SELECT 1
</Resource>
</tomee>
```

7. Внутри приложения, настроить `META-INF/persistence.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence version="2.0" xmlns="http://java.sun.com/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://java.sun.com/xml/ns/persistence http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd">
    <persistence-unit name="Tutorial" transaction-type="JTA">
        <class>com.airhacks.ping.entity.Persons</class>
        <jta-data-source>java:openejb/Resource/Tutorial</jta-data-source>
    </persistence-unit>
</persistence>
```

8. Создать сущность:

```java
@Entity
@Table(name = "Persons")
public class Persons {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private int id;

    @Column(name = "FirstName")
    private String firstName;

    @Column(name = "LastName")
    private String lastName;

    @Column(name = "Age")
    private String age;

    // ... Сгенерировать геттеры и сеттеры ...

    public String toString() {
        return "[Person id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + ", age=" + age + "]";
    }
}
```

9. Использовать эту сущность для отображения данных на странице:

```java
public class DBResource {

    @PersistenceUnit(unitName="Tutorial")
    private EntityManagerFactory factory;

    @GET
    @Path("/persons")
    public String persons() {

        EntityManager entityManager = factory.createEntityManager();
        List<Persons> persons = entityManager.createQuery("SELECT p FROM Persons p").getResultList();
        String result =  persons.stream()
                .map(Persons::toString)
                .collect(Collectors.joining(" | "));

        return result;
    }
}
```

10. PROFIT