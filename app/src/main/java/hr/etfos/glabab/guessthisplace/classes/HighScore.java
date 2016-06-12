package hr.etfos.glabab.guessthisplace.classes;

public class HighScore {
    String username;
    String score;
    String numberOfGuesses;
    String position;

    public HighScore(String username, String score, String numberOfGuesses, String position)
    {
        this.username = username;
        this.score = score;
        this.numberOfGuesses = numberOfGuesses;
        this.position = position;
    }

    public String getUsername() {
        return username;
    }

    public String getScore() {
        return score;
    }

    public String getNumberOfGuesses() {
        return numberOfGuesses;
    }

    public String getPosition() {
        return position;
    }
}
