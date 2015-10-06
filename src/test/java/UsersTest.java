import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Hashtable;

public class UsersTest {
    Users allUsers = new Users();
    PreparedStatement preparedStatement = null;
    Connection connection = null;

    @Before
    public void beforeEach(){
        initializeDBConnection();
        setupTestUsers();
    }

    @After
    public void afterEach(){
        deleteAllUsers(connection);
    }

    @Test
    public void returnsFoundUserWhenCalledWithValidArguments(){
        Hashtable authDetails = getValidAuthDetails();
        User foundUser = allUsers.find(authDetails);
        assertEquals(foundUser.getClass().getName(), "User");
        assertEquals(foundUser.getUsername(), "foo_example");
        assertEquals(foundUser.getPassword(), "123456789");
    }

    @Test
    public void returnsNullWhenCalledWithInvalidArguments(){
        Hashtable authDetails = getInvalidAuthDetails();
        User foundUser = allUsers.find(authDetails);
        assertEquals(foundUser, null);
    }

    @Test
    public void returnsNullWhenCalledWithWrongUserDetailsHash(){
        Hashtable authDetails = getWrongUserDetailsHash();
        User foundUser = allUsers.find(authDetails);
        assertEquals(foundUser, null);
    }

    private Hashtable getWrongUserDetailsHash(){
        Hashtable authDetails = new Hashtable();
        authDetails.put("height", 10);
        authDetails.put("width", 10);
        return authDetails;
    }

    private void setupTestUsers(){
        try {
            preparedStatement = connection.prepareStatement("INSERT INTO users" +
                    "(username, password) VALUES(?, ?) RETURNING username, password");
            preparedStatement.setString(1, "foo_example");
            preparedStatement.setString(2, "123456789");
            preparedStatement.executeQuery();
            preparedStatement.setString(1, "bar_example");
            preparedStatement.setString(2, "123456789");
            preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Hashtable getValidAuthDetails(){
        Hashtable authDetails = new Hashtable();
        authDetails.put("username", "foo_example");
        authDetails.put("password", "123456789");
        return authDetails;
    }

    private Hashtable getInvalidAuthDetails(){
        Hashtable authDetails = new Hashtable();
        authDetails.put("username", "baz_example");
        authDetails.put("password", "123456789");
        return authDetails;
    }

    private void deleteAllUsers(Connection connection){
        try {
            preparedStatement = connection.prepareStatement("DELETE FROM users");
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initializeDBConnection(){
        if(this.connection == null){
            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                System.out.println("PostgreSQL JDBC Driver not Found!");
                e.printStackTrace();
                return;
            }
            String environment = System.getenv("ENV");
            try {
                this.connection = DriverManager.getConnection(
                        "jdbc:postgresql://localhost:5432/twitchblade_" + environment,
                        "chi6rag", "");
                System.out.println();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}