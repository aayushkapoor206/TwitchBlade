import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Tweet {
    private Integer id;
    private String body;
    private Integer userId;
    private DbConnection connection;
    private PreparedStatement tweetSavePreparedStatement;

    Tweet(String body, Integer userId, DbConnection connection){
        this.id = null;
        this.body = body;
        this.userId = userId;
        this.connection = connection;
    }

    private Tweet(Integer id, String body, Integer userId,
                  DbConnection connection){
        this.id = id;
        this.body = body;
        this.userId = userId;
        this.connection = connection;
    }

    public Tweet save(){
        prepareTweetSaveStatement();
        ResultSet res = null;
        res = insertUserIntoDB(this.body, this.userId);
        if(res != null) return getUserFromDBResult(res);
        return null;
    }

    public Integer getId(){
        return this.id;
    }

    public String getBody(){
        return this.body;
    }

    public Integer getUserId(){
        return this.userId;
    }

    private void prepareTweetSaveStatement(){
        try {
            this.tweetSavePreparedStatement = this.connection.prepareStatement("INSERT" +
                    " INTO tweets(body, user_id) VALUES(?, ?) RETURNING id, body," +
                    " user_id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private ResultSet insertUserIntoDB(String body, Integer userId){
        if(body == null   || userId == null) return null;
        if(body.isEmpty()) return null;
        ResultSet res = null;
        try {
            this.tweetSavePreparedStatement.setString(1, body);
            this.tweetSavePreparedStatement.setInt(2, userId);
            res = this.tweetSavePreparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    private Tweet getUserFromDBResult(ResultSet res){
        try {
            if(res.next()){
                Integer id = res.getInt("id");
                String body = res.getString("body");
                Integer userId = res.getInt("user_id");
                return new Tweet(id, body, userId, this.connection);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
