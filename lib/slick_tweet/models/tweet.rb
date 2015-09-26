module SlickTweet
  class Tweet < Record
    attr_accessor :body
    attr_reader :created_at, :id, :user_id

    def initialize(id: nil, body: nil, user_id: nil, created_at: nil)
      @id = id || nil
      @body = body
      @user_id = user_id
      @created_at = created_at || nil
    end

    def save
      # cook statement for sql
      # execute statement
        # if successful, find saved object and return
        # - else
          # - do not save the object
          # - add errors
          # - return unsaved object with errors
      statement = 'INSERT INTO tweets(body, user_id) '
      statement << "VALUES('#{body}', '#{user_id}') "
      statement << "RETURNING id, body, user_id, created_at"
      begin
        res = $con.exec(statement).values.flatten
      rescue PG::Error => e
        puts e.message
        puts ''
        return nil
      end
      psql_to_tweet(res)
    end

    # SlickTweet::Tweet.count
    # returns the number of tweets in the database
    def self.count
      ($con.exec 'SELECT COUNT(*) FROM tweets').values.flatten[0].to_i
    end

    private
    
    # psql_to_tweet
    # postgres returns data in array
    # with all data types as strings
    # psql_to_tweet converts psql result to Tweet object
    def psql_to_tweet(psql_result)
      id         = psql_result[0].to_i
      body       = psql_result[1]
      user_id    = psql_result[2].to_i
      created_at = DateTime.new(*(psql_result[3].split(/[-|\s|:]/).map{|param| param.to_f}))
      Tweet.new(id: id, body: body, user_id: user_id, created_at: created_at)
    end

  end
end